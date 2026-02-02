package com.simats.profixai.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.R
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ServiceCategory(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(navController: NavController, userId: Int) {
    var isLoading by remember { mutableStateOf(true) }
    var providers by remember { mutableStateOf<List<Provider>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    val serviceCategories = listOf(
        ServiceCategory(1, "Cleaner", Icons.Default.CleaningServices, CleanerColor, "🧹"),
        ServiceCategory(2, "Electrician", Icons.Default.ElectricalServices, ElectricianColor, "⚡"),
        ServiceCategory(3, "Painter", Icons.Default.FormatPaint, PainterColor, "🎨"),
        ServiceCategory(4, "Salon", Icons.Default.ContentCut, SalonColor, "💇"),
        ServiceCategory(5, "Carpenter", Icons.Default.Carpenter, CarpenterColor, "🪚"),
        ServiceCategory(6, "Mechanic", Icons.Default.Build, MechanicColor, "🔧")
    )
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getProviders(GetProvidersRequest())
            if (response.isSuccessful && response.body()?.success == true) {
                providers = response.body()?.providers ?: emptyList()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    val tealColor = Color(0xFF0D9997)
    val blueColor = Color(0xFF3B82F6)
    
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = tealColor) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = tealColor,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = tealColor
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarMonth, "Bookings") },
                    label = { Text("Bookings") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = tealColor,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = tealColor
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = tealColor,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = tealColor
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeContent(navController, userId, serviceCategories, providers, isLoading, Modifier.padding(paddingValues))
            1 -> UserBookingsContent(navController, userId, Modifier.padding(paddingValues))
            2 -> UserProfileContent(navController, userId, Modifier.padding(paddingValues))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    navController: NavController,
    userId: Int,
    serviceCategories: List<ServiceCategory>,
    providers: List<Provider>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSortDialog by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("rating_desc") }
    
    val sortedProviders = remember(providers, selectedSort, searchQuery) {
        var filtered = if (searchQuery.isBlank()) providers else {
            providers.filter { 
                it.full_name.contains(searchQuery, ignoreCase = true) ||
                (it.service_name?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
        when (selectedSort) {
            "rating_desc" -> filtered.sortedByDescending { it.rating }
            "price_asc" -> filtered.sortedBy { it.hourly_rate }
            "price_desc" -> filtered.sortedByDescending { it.hourly_rate }
            "newest" -> filtered // Assuming default order is newest
            else -> filtered
        }
    }
    
    // Sort Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort By", fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF1A1A2E),
            titleContentColor = Color.White,
            text = {
                Column {
                    SortOption("Rating (High to Low)", selectedSort == "rating_desc") { 
                        selectedSort = "rating_desc"; showSortDialog = false 
                    }
                    SortOption("Price (Low to High)", selectedSort == "price_asc") { 
                        selectedSort = "price_asc"; showSortDialog = false 
                    }
                    SortOption("Price (High to Low)", selectedSort == "price_desc") { 
                        selectedSort = "price_desc"; showSortDialog = false 
                    }
                    SortOption("Newest First", selectedSort == "newest") { 
                        selectedSort = "newest"; showSortDialog = false 
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSortDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997))
                ) { Text("Apply") }
            }
        )
    }
    
    Column(
        modifier = modifier.fillMaxSize().background(Gray50).verticalScroll(rememberScrollState())
    ) {
        // Header with Search
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF0D9997), Color(0xFF3B82F6))))
                .padding(24.dp)
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Hello! 👋", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("Find Services", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { navController.navigate("user_ai_chat/$userId") },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.SmartToy, "AI Chat", tint = Color.White)
                        }
                        IconButton(
                            onClick = { navController.navigate("user_notifications/$userId") },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search services or providers...", color = Gray500) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Gray500) },
                    trailingIcon = {
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.FilterList, "Filter", tint = Color(0xFF0D9997))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0D9997),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
        
        // Promotional Banner Slider
        PromoBannerSlider()
        
        // Services Grid - 2 rows x 3 columns
        Column(modifier = Modifier.padding(16.dp)) {
            Text("All Services", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                serviceCategories.take(3).forEach { cat ->
                    ServiceCategoryCardSmall(cat, Modifier.weight(1f)) {
                        navController.navigate("providers_list/$userId/${cat.id}/${cat.name}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                serviceCategories.drop(3).take(3).forEach { cat ->
                    ServiceCategoryCardSmall(cat, Modifier.weight(1f)) {
                        navController.navigate("providers_list/$userId/${cat.id}/${cat.name}")
                    }
                }
            }
        }
        
        // Available Servicemen - 2 per row
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Available Servicemen", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
                if (selectedSort != "rating_desc") {
                    Text(
                        when(selectedSort) {
                            "price_asc" -> "Price ↑"
                            "price_desc" -> "Price ↓"
                            "newest" -> "New"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF0D9997)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0D9997))
                }
            } else if (sortedProviders.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, null, Modifier.size(48.dp), Gray400)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (searchQuery.isBlank()) "No servicemen available yet" else "No results found", color = Gray500)
                    }
                }
            } else {
                // 2 providers per row
                sortedProviders.take(6).chunked(2).forEach { rowProviders ->
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        rowProviders.forEach { provider ->
                            ProviderCardGrid(
                                provider = provider,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("provider_details/$userId/${provider.id}") }
                            )
                        }
                        // Fill empty space if odd number
                        if (rowProviders.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SortOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF0D9997).copy(alpha = 0.2f) else Color(0xFF2A2A3E)),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D9997)) else null
    ) {
        Text(text, modifier = Modifier.padding(16.dp), color = Color.White)
    }
}

@Composable
fun ProviderCardGrid(provider: Provider, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: Avatar with availability indicator
            Box {
                ProfileAvatar(
                    imageUrl = provider.profile_image,
                    name = provider.full_name,
                    size = 56.dp,
                    fontSize = 20.sp
                )
                // Availability indicator
                if (provider.is_available == 1) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Green500)
                    )
                }
            }
            
            // Middle section: Name and service
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = provider.full_name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = provider.service_name ?: "",
                    fontSize = 11.sp,
                    color = Color(0xFF0D9997),
                    maxLines = 1
                )
            }
            
            // Bottom section: Rating and Price
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Star, null, Modifier.size(12.dp), Amber500)
                    Text(" ${provider.rating}", fontSize = 11.sp, color = Gray600)
                    Text(" • ", fontSize = 11.sp, color = Gray400)
                    Text("${provider.experience_years}yr exp", fontSize = 11.sp, color = Gray600)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D9997).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "₹${provider.hourly_rate.toInt()}/hr",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9997)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCategoryCardSmall(category: ServiceCategory, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(category.color.copy(alpha = 0.1f)), Alignment.Center) {
                Icon(category.icon, category.name, Modifier.size(22.dp), category.color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Gray900)
        }
    }
}

@Composable
fun UserBookingsContent(navController: NavController, userId: Int, modifier: Modifier = Modifier) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getUserBookings(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                bookings = response.body()?.bookings ?: emptyList()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    Column(modifier = modifier.fillMaxSize().background(Gray50)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF0D9997), Color(0xFF3B82F6))))
                .padding(24.dp)
        ) {
            Text("My Bookings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF0D9997)) }
        } else if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(80.dp), Gray400)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No bookings yet", fontSize = 18.sp, color = Gray600)
                }
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                bookings.forEach { booking ->
                    UserBookingCard(booking = booking, navController = navController)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(navController: NavController, userId: Int, modifier: Modifier = Modifier) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isUploading = true
            uploadMessage = null
            scope.launch {
                try {
                    val imagePart = com.simats.profixai.network.ImageUploadHelper.createImagePart(context, uri, "image")
                    if (imagePart == null) {
                        uploadMessage = "Failed to process image"
                        isUploading = false
                        return@launch
                    }
                    val userIdBody = com.simats.profixai.network.ImageUploadHelper.createIdPart(userId)
                    
                    val response = RetrofitClient.apiService.uploadUserImage(userIdBody, imagePart)
                    if (response.isSuccessful && response.body()?.success == true) {
                        uploadMessage = "Photo updated successfully!"
                        // Refresh user data
                        val profileResponse = RetrofitClient.apiService.getUserProfile(UserIdRequest(user_id = userId))
                        if (profileResponse.isSuccessful && profileResponse.body()?.success == true) {
                            user = profileResponse.body()?.user
                        }
                    } else {
                        uploadMessage = response.body()?.message ?: "Upload failed"
                    }
                } catch (e: Exception) {
                    uploadMessage = "Error: ${e.message}"
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getUserProfile(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                user = response.body()?.user
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    Column(modifier = modifier.fillMaxSize().background(Gray50).verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF0D9997), Color(0xFF3B82F6))))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile avatar with upload button
                Box {
                    ProfileAvatar(
                        imageUrl = user?.profile_image,
                        name = user?.full_name ?: "U",
                        size = 80.dp,
                        fontSize = 28.sp,
                        backgroundColor = Color.White,
                        textColor = Color(0xFF0D9997)
                    )
                    
                    // Camera button overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D9997))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(user?.full_name ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(user?.email ?: "", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                
                // Upload message
                if (uploadMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        uploadMessage!!,
                        fontSize = 12.sp,
                        color = if (uploadMessage!!.contains("success")) Green500 else Red500
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                UserProfileDetailRow(Icons.Default.Phone, "Phone", user?.phone ?: "-")
                Divider(Modifier.padding(vertical = 12.dp), color = Gray200)
                UserProfileDetailRow(Icons.Default.LocationCity, "City", user?.city ?: "-")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                UserProfileMenuItem(Icons.Default.Edit, "Edit Profile") { 
                    navController.navigate("edit_user_profile/$userId")
                }
                Divider(color = Gray200)
                UserProfileMenuItem(Icons.Default.History, "Booking History") { 
                    navController.navigate("booking_history/$userId")
                }
                Divider(color = Gray200)
                UserProfileMenuItem(Icons.Default.Notifications, "Notifications") { 
                    navController.navigate("user_notifications/$userId")
                }
                Divider(color = Gray200)
                UserProfileMenuItem(Icons.Default.Help, "Help & Support") { 
                    navController.navigate("help_user")
                }
                Divider(color = Gray200)
                UserProfileMenuItem(Icons.Default.Info, "About ProFIX AI") { 
                    navController.navigate("about")
                }
                Divider(color = Gray200)
                UserProfileMenuItem(Icons.Default.Logout, "Logout", Red500) {
                    navController.navigate("role_selection") { popUpTo(0) { inclusive = true } }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun UserProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(24.dp), Color(0xFF0D9997))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Gray500)
            Text(value, fontSize = 16.sp, color = Gray900)
        }
    }
}

@Composable
private fun UserProfileMenuItem(icon: ImageVector, title: String, tint: Color = Gray700, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, Modifier.size(24.dp), tint)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = tint, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, "Go", tint = Gray400)
    }
}

@Composable
private fun UserBookingCard(booking: Booking, navController: NavController) {
    val statusColor = when (booking.status) {
        "pending" -> Orange500
        "accepted" -> Color(0xFF0D9997)
        "in_progress" -> Amber500
        "completed" -> Green500
        "cancelled" -> Red500
        else -> Gray500
    }
    
    val canTrackLocation = booking.status in listOf("accepted", "in_progress")
    
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(booking.provider_image, booking.provider_name ?: "P", 50.dp, 18.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.provider_name ?: "Provider", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Text(booking.service_name ?: "", fontSize = 14.sp, color = Gray600)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(
                        booking.status.replaceFirstChar { it.uppercase() },
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray200)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                Text(booking.booking_date, fontSize = 14.sp, color = Gray700)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                Text(booking.booking_time, fontSize = 14.sp, color = Gray700)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("${booking.estimated_hours} hour(s)", fontSize = 14.sp, color = Gray600)
                Text("₹${booking.total_amount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9997))
            }
            
            // Track Location button for accepted/in_progress bookings
            if (canTrackLocation) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate("location_tracking/${booking.id}") },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997))
                ) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Track Provider Location", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PromoBannerSlider() {
    val bannerImages = listOf(
        R.drawable.promo_banner_1,
        R.drawable.promo_banner_2,
        R.drawable.promo_banner_3
    )
    
    val pagerState = rememberPagerState(pageCount = { bannerImages.size })
    val scope = rememberCoroutineScope()
    
    // Auto-scroll every 3 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % bannerImages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Image(
                    painter = painterResource(id = bannerImages[page]),
                    contentDescription = "Promo Banner ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Page indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(bannerImages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF0D9997) else Gray400)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                )
            }
        }
    }
}
