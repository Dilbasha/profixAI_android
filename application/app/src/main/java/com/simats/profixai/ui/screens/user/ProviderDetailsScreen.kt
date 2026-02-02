package com.simats.profixai.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailsScreen(
    navController: NavController,
    userId: Int,
    providerId: Int
) {
    var provider by remember { mutableStateOf<Provider?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var availability by remember { mutableStateOf<List<ProviderAvailability>>(emptyList()) }
    var portfolio by remember { mutableStateOf<List<PortfolioImage>>(emptyList()) }
    var selectedPortfolioImage by remember { mutableStateOf<PortfolioImage?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Sentiment analysis states
    var positiveReviews by remember { mutableStateOf<List<SentimentReview>>(emptyList()) }
    var negativeReviews by remember { mutableStateOf<List<SentimentReview>>(emptyList()) }
    var sentimentSummary by remember { mutableStateOf("") }
    var isAnalyzingSentiment by remember { mutableStateOf(false) }
    var selectedReviewTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(providerId) {
        try {
            val response = RetrofitClient.apiService.getProviderDetails(
                ProviderIdRequest(provider_id = providerId)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                provider = response.body()?.provider
                reviews = response.body()?.reviews ?: emptyList()
                availability = response.body()?.availability ?: emptyList()
                portfolio = response.body()?.portfolio ?: emptyList()
                
                // Fetch sentiment analysis
                if (reviews.isNotEmpty()) {
                    isAnalyzingSentiment = true
                    try {
                        val sentimentResponse = RetrofitClient.apiService.analyzeReviews(
                            ProviderIdRequest(provider_id = providerId)
                        )
                        if (sentimentResponse.isSuccessful && sentimentResponse.body()?.success == true) {
                            positiveReviews = sentimentResponse.body()?.positive_reviews ?: emptyList()
                            negativeReviews = sentimentResponse.body()?.negative_reviews ?: emptyList()
                            sentimentSummary = sentimentResponse.body()?.summary ?: ""
                        }
                    } catch (e: Exception) { }
                    isAnalyzingSentiment = false
                }
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    // Full screen portfolio image viewer
    if (selectedPortfolioImage != null) {
        Dialog(onDismissRequest = { selectedPortfolioImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { selectedPortfolioImage = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "${RetrofitClient.BASE_URL}${selectedPortfolioImage!!.image_url}",
                    contentDescription = "Portfolio Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = { selectedPortfolioImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D9997),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0D9997))
            }
        } else if (provider != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Gray50)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Header
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileAvatar(
                                imageUrl = provider!!.profile_image,
                                name = provider!!.full_name,
                                size = 100.dp,
                                fontSize = 36.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = provider!!.full_name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray900
                            )
                            
                            Text(
                                text = provider!!.service_name ?: "Service Provider",
                                fontSize = 16.sp,
                                color = Color(0xFF0D9997)
                            )
                            
                            // Honor Score Badge
                            if (provider!!.honor_score > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HonorScoreBadge(score = provider!!.honor_score)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    value = "${provider!!.rating}",
                                    label = "Rating",
                                    icon = Icons.Default.Star
                                )
                                StatItem(
                                    value = "${provider!!.total_jobs}",
                                    label = "Jobs",
                                    icon = Icons.Default.Work
                                )
                                StatItem(
                                    value = "${provider!!.experience_years}y",
                                    label = "Experience",
                                    icon = Icons.Default.Timeline
                                )
                            }
                        }
                    }
                    
                    // Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "About",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = provider!!.description ?: "No description provided.",
                                fontSize = 14.sp,
                                color = Gray600,
                                lineHeight = 22.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Gray200)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            DetailRow(
                                icon = Icons.Default.CurrencyRupee,
                                label = "Hourly Rate",
                                value = "₹${provider!!.hourly_rate.toInt()}/hour"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = provider!!.phone
                            )
                            if (!provider!!.city.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Location",
                                    value = "${provider!!.city}"
                                )
                            }
                        }
                    }
                    
                    // Availability Section
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Availability",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray900
                                )
                                if (provider!!.is_available == 1) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Green500.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "Available",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = Green500,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Gray500.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "Offline",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = Gray500,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (availability.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Gray100, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.EventBusy,
                                        null,
                                        tint = Gray400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "No schedule set",
                                        fontSize = 14.sp,
                                        color = Gray500
                                    )
                                }
                            } else {
                                // Show next 7 available days in a nicer format
                                val availableDays = availability
                                    .filter { it.status == "available" }
                                    .take(7)
                                
                                if (availableDays.isEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Red500.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.EventBusy,
                                            null,
                                            tint = Red500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "No available slots",
                                            fontSize = 14.sp,
                                            color = Red500
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Next available dates:",
                                        fontSize = 13.sp,
                                        color = Gray600,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        availableDays.forEach { avail ->
                                            AvailabilityDayChip(avail)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Portfolio Section
                    if (portfolio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Previous Work",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Gray900
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0D9997).copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "${portfolio.size} photos",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = Color(0xFF0D9997)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    portfolio.forEach { item ->
                                        Card(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedPortfolioImage = item },
                                            colors = CardDefaults.cardColors(containerColor = Gray100)
                                        ) {
                                            AsyncImage(
                                                model = "${RetrofitClient.BASE_URL}${item.image_url}",
                                                contentDescription = item.description ?: "Work Image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Reviews Section (always show)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reviews (${reviews.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray900
                                )
                                if (isAnalyzingSentiment) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                }
                            }
                            
                            if (reviews.isEmpty()) {
                                // Show no reviews message
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.RateReview,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Gray400
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No reviews yet",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Gray600
                                    )
                                    Text(
                                        text = "Be the first to review this provider!",
                                        fontSize = 13.sp,
                                        color = Gray500
                                    )
                                }
                            } else {
                                // AI Summary
                                if (sentimentSummary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                null,
                                                Modifier.size(16.dp),
                                                Color(0xFF0D9997)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = sentimentSummary,
                                                fontSize = 13.sp,
                                                color = Gray700,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Sentiment Tabs
                                if (positiveReviews.isNotEmpty() || negativeReviews.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = selectedReviewTab == 0,
                                            onClick = { selectedReviewTab = 0 },
                                            label = { Text("All (${reviews.size})") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF0D9997),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                        FilterChip(
                                            selected = selectedReviewTab == 1,
                                            onClick = { selectedReviewTab = 1 },
                                            leadingIcon = if (selectedReviewTab == 1) {{ Icon(Icons.Default.ThumbUp, null, Modifier.size(16.dp)) }} else null,
                                            label = { Text("Good (${positiveReviews.size})") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF4CAF50),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                        FilterChip(
                                            selected = selectedReviewTab == 2,
                                            onClick = { selectedReviewTab = 2 },
                                            leadingIcon = if (selectedReviewTab == 2) {{ Icon(Icons.Default.ThumbDown, null, Modifier.size(16.dp)) }} else null,
                                            label = { Text("Bad (${negativeReviews.size})") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFF44336),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                // Display reviews
                                when (selectedReviewTab) {
                                    0 -> reviews.take(5).forEach { review ->
                                        ReviewItem(review = review)
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    1 -> if (positiveReviews.isEmpty()) {
                                        Text("No positive reviews yet", color = Gray500, fontSize = 14.sp)
                                    } else positiveReviews.take(5).forEach { review ->
                                        SentimentReviewItem(review = review, isPositive = true)
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    2 -> if (negativeReviews.isEmpty()) {
                                        Text("No negative reviews - Great!", color = Gray500, fontSize = 14.sp)
                                    } else negativeReviews.take(5).forEach { review ->
                                        SentimentReviewItem(review = review, isPositive = false)
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Book Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "₹${provider!!.hourly_rate.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9997)
                            )
                            Text(
                                text = "per hour",
                                fontSize = 14.sp,
                                color = Gray500
                            )
                        }
                        
                        Button(
                            onClick = {
                                navController.navigate("booking_confirm/$userId/$providerId")
                            },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997))
                        ) {
                            Text(
                                text = "Book Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Amber500,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray900)
        Text(text = label, fontSize = 12.sp, color = Gray500)
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF0D9997), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Gray500)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray900)
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(
                imageUrl = review.user_image,
                name = review.user_name ?: "User",
                size = 40.dp,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.user_name ?: "User", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray900)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Amber500
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.created_at?.take(10) ?: "",
                        fontSize = 11.sp,
                        color = Gray400
                    )
                }
            }
        }
        if (!review.comment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = Gray600,
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 52.dp)
            )
        }
    }
}

@Composable
fun AvailabilityDayChip(availability: ProviderAvailability) {
    val date = try {
        LocalDate.parse(availability.date)
    } catch (e: Exception) {
        null
    }
    
    val dayName = date?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale.getDefault()) ?: ""
    val dayNum = date?.dayOfMonth?.toString() ?: ""
    val month = date?.month?.getDisplayName(TextStyle.SHORT, Locale.getDefault()) ?: ""
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.08f)),
        modifier = Modifier.width(85.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day and Month
            Text(
                text = "$dayName, $month",
                fontSize = 10.sp,
                color = Gray500
            )
            // Date Number
            Text(
                text = dayNum,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Green600
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Time Range - stacked vertically
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = availability.start_time.take(5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray700
                )
                Text(
                    text = "to",
                    fontSize = 9.sp,
                    color = Gray400
                )
                Text(
                    text = availability.end_time.take(5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray700
                )
            }
        }
    }
}

@Composable
fun SentimentReviewItem(review: SentimentReview, isPositive: Boolean) {
    val borderColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgColor = if (isPositive) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = review.user_image,
                    name = review.user_name ?: "User",
                    size = 40.dp,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = review.user_name ?: "User", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray900)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (index < review.rating) Color(0xFFFFC107) else Gray300
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = review.created_at?.take(10) ?: "",
                            fontSize = 11.sp,
                            color = Gray400
                        )
                    }
                }
                // Sentiment badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = borderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPositive) Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                            null,
                            Modifier.size(12.dp),
                            Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isPositive) "Good" else "Bad",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    fontSize = 13.sp,
                    color = Gray700,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }
        }
    }
}

@Composable
fun HonorScoreBadge(score: Double) {
    val (badgeName, badgeColor, badgeIcon) = when {
        score >= 90 -> Triple("Elite", Color(0xFFFFD700), Icons.Default.WorkspacePremium)      // Gold
        score >= 75 -> Triple("Expert", Color(0xFFC0C0C0), Icons.Default.EmojiEvents)          // Silver
        score >= 60 -> Triple("Professional", Color(0xFFCD7F32), Icons.Default.Verified)       // Bronze
        score >= 40 -> Triple("Rising Star", Color(0xFF4CAF50), Icons.Default.TrendingUp)      // Green
        else -> Triple("New", Color(0xFF9E9E9E), Icons.Default.NewReleases)                    // Gray
    }
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                badgeIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = badgeColor
            )
            Column {
                Text(
                    text = badgeName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
                Text(
                    text = "Honor Score: ${score.toInt()}/100",
                    fontSize = 11.sp,
                    color = Gray600
                )
            }
        }
    }
}

@Composable
fun HonorScoreCompact(score: Double, modifier: Modifier = Modifier) {
    val (badgeName, badgeColor) = when {
        score >= 90 -> Pair("Elite", Color(0xFFFFD700))
        score >= 75 -> Pair("Expert", Color(0xFFC0C0C0))
        score >= 60 -> Pair("Pro", Color(0xFFCD7F32))
        score >= 40 -> Pair("Rising", Color(0xFF4CAF50))
        else -> Pair("New", Color(0xFF9E9E9E))
    }
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = badgeColor
        )
        Text(
            text = "${score.toInt()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = badgeColor
        )
    }
}
