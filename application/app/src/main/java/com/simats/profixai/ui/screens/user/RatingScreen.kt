package com.simats.profixai.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    navController: NavController,
    userId: Int,
    bookingId: Int
) {
    var booking by remember { mutableStateOf<Booking?>(null) }
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Load booking details
    LaunchedEffect(bookingId) {
        try {
            val response = RetrofitClient.apiService.getUserBookings(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                booking = response.body()?.bookings?.find { it.id == bookingId }
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Green500, modifier = Modifier.size(48.dp)) },
            title = { Text("Thank You!", fontWeight = FontWeight.Bold) },
            text = { Text("Your feedback has been submitted successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("user_home/$userId") {
                            popUpTo("user_home/$userId") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997))
                ) { Text("Back to Home") }
            }
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00BFA5), Color(0xFF00897B))
                    )
                )
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    "Feedback",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        "How was your ${booking?.service_name ?: "service"}?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Rate your experience with ${booking?.provider_name ?: "the provider"} for the ${booking?.service_name ?: ""} service on ${booking?.booking_date ?: ""}.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Star Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Default.StarOutline,
                                contentDescription = "Star $i",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { rating = i },
                                tint = if (i <= rating) Amber500 else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        when (rating) {
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Very Good"
                            5 -> "Excellent!"
                            else -> "Tap to rate"
                        },
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Comment Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A3A))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                "Add a Comment",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                placeholder = { Text("Tell us more about your experience (optional)", color = Gray500) },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF2A4A4A),
                                    unfocusedContainerColor = Color(0xFF2A4A4A),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(errorMessage!!, color = Red500)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Submit Button
                    Button(
                        onClick = {
                            if (rating == 0) {
                                errorMessage = "Please select a rating"
                                return@Button
                            }
                            
                            isSubmitting = true
                            errorMessage = null
                            
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.submitReview(
                                        SubmitReviewRequest(
                                            booking_id = bookingId,
                                            user_id = userId,
                                            rating = rating,
                                            comment = comment
                                        )
                                    )
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        showSuccess = true
                                    } else {
                                        errorMessage = response.body()?.message ?: "Failed to submit review"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Network error: ${e.message}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997)),
                        enabled = !isSubmitting && rating > 0
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Submit Your Feedback", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
