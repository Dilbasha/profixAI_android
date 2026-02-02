package com.simats.profixai.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProviderProfileScreen(navController: NavController, providerId: Int) {
    val tealColor = Color(0xFF009688)
    val darkBg = Color(0xFF102A2E)
    val cardBg = Color(0xFF1A383D)
    
    var provider by remember { mutableStateOf<Provider?>(null) }
    var stats by remember { mutableStateOf<ProviderStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(providerId) {
        try {
            // Fetch provider details
            val response = RetrofitClient.apiService.getProviderDetails(ProviderIdRequest(providerId))
            if (response.isSuccessful && response.body()?.success == true) {
                provider = response.body()?.provider
            }
            
            // Fetch provider stats
            val statsResponse = RetrofitClient.apiService.getProviderStats(ProviderIdRequest(providerId))
            if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                stats = statsResponse.body()?.stats
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load provider: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tealColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBg)
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = tealColor
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Error, null, Modifier.size(64.dp), Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = Color.White)
                }
            } else if (provider != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Profile Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
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
                                provider!!.full_name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Text(
                                provider!!.service_name ?: "Service Provider",
                                fontSize = 16.sp,
                                color = tealColor
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Status Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (provider!!.is_available == 1) Color(0xFF4CAF50) else Color.Red
                                        )
                                )
                                Text(
                                    if (provider!!.is_available == 1) "Available" else "Offline",
                                    fontSize = 14.sp,
                                    color = if (provider!!.is_available == 1) Color(0xFF4CAF50) else Color.Red
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                if (provider!!.is_verified == 1) {
                                    Icon(Icons.Default.Verified, null, Modifier.size(18.dp), tealColor)
                                    Text("Verified", fontSize = 14.sp, color = tealColor)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem("Rating", "${provider!!.rating ?: 0.0}/5", Icons.Default.Star, Color(0xFFFFC107))
                                StatItem("Jobs", "${stats?.completed_bookings ?: provider!!.total_jobs ?: 0}", Icons.Default.WorkHistory, tealColor)
                                StatItem("Exp", "${provider!!.experience_years ?: 0} yrs", Icons.Default.Timeline, Color(0xFF2196F3))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Contact Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Contact Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            InfoRow(Icons.Default.Phone, "Phone", provider!!.phone ?: "Not provided")
                            InfoRow(Icons.Default.Email, "Email", provider!!.email ?: "Not provided")
                            InfoRow(Icons.Default.LocationOn, "City", provider!!.city ?: "Not provided")
                            InfoRow(Icons.Default.Home, "Address", provider!!.address ?: "Not provided")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Service Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Service Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            InfoRow(Icons.Default.Category, "Service", provider!!.service_name ?: "N/A")
                            InfoRow(Icons.Default.CurrencyRupee, "Hourly Rate", "₹${provider!!.hourly_rate ?: 0}/hr")
                            InfoRow(Icons.Default.WorkHistory, "Experience", "${provider!!.experience_years ?: 0} years")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Performance Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Performance Stats", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MiniStatCard("Pending", "${stats?.pending_bookings ?: 0}", Color(0xFFFFA000), cardBg)
                                MiniStatCard("Completed", "${stats?.completed_bookings ?: 0}", Color(0xFF4CAF50), cardBg)
                                MiniStatCard("Earnings", "₹${stats?.total_earnings ?: 0}", tealColor, cardBg)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // About Section
                    if (!provider!!.description.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("About", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    provider!!.description ?: "",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, Modifier.size(24.dp), color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), Color(0xFF009688))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            Text(value, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, color: Color, bgColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}
