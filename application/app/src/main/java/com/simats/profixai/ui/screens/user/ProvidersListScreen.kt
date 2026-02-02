package com.simats.profixai.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.simats.profixai.network.GetProvidersRequest
import com.simats.profixai.network.Provider
import com.simats.profixai.network.RetrofitClient
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersListScreen(
    navController: NavController,
    userId: Int,
    serviceId: Int,
    serviceName: String
) {
    var providers by remember { mutableStateOf<List<Provider>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(serviceId) {
        try {
            val response = RetrofitClient.apiService.getProviders(
                GetProvidersRequest(service_id = serviceId)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                providers = response.body()?.providers ?: emptyList()
            } else {
                errorMessage = "Failed to load providers"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(serviceName, fontWeight = FontWeight.SemiBold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Gray50)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF0D9997))
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(errorMessage!!, color = Gray600)
                        }
                    }
                }
                providers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = "No providers",
                                modifier = Modifier.size(80.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No providers available",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Gray600
                            )
                            Text(
                                text = "Check back later for $serviceName services",
                                fontSize = 14.sp,
                                color = Gray500
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "${providers.size} providers found",
                                fontSize = 14.sp,
                                color = Gray600
                            )
                        }
                        
                        items(providers) { provider ->
                            ProviderCard(
                                provider = provider,
                                onClick = {
                                    navController.navigate("provider_details/$userId/${provider.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            ProfileAvatar(
                imageUrl = provider.profile_image,
                name = provider.full_name,
                size = 64.dp,
                fontSize = 20.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.full_name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(16.dp),
                        tint = Amber500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${provider.rating}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray700
                    )
                    Text(
                        text = " (${provider.total_reviews} reviews)",
                        fontSize = 14.sp,
                        color = Gray500
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkHistory,
                        contentDescription = "Experience",
                        modifier = Modifier.size(16.dp),
                        tint = Gray500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${provider.experience_years} years exp",
                        fontSize = 14.sp,
                        color = Gray600
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${provider.hourly_rate.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9997)
                )
                Text(
                    text = "/hour",
                    fontSize = 12.sp,
                    color = Gray500
                )
                if (provider.honor_score > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HonorScoreCompact(score = provider.honor_score)
                }
            }
        }
    }
}
