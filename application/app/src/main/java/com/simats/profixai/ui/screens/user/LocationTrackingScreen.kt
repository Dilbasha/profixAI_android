package com.simats.profixai.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTrackingScreen(navController: NavController, bookingId: Int) {
    var locationData by remember { mutableStateOf<LocationResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // Default location (will be updated when provider shares)
    var providerLocation by remember { mutableStateOf(LatLng(17.385044, 78.486671)) } // Hyderabad default
    
    // Camera position state for the map
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(providerLocation, 15f)
    }
    
    // Fetch location data and auto-refresh every 5 seconds
    LaunchedEffect(bookingId) {
        while (true) {
            try {
                val response = RetrofitClient.apiService.getProviderLocation(
                    GetLocationRequest(booking_id = bookingId)
                )
                if (response.isSuccessful) {
                    locationData = response.body()
                    errorMessage = null
                    
                    // Update provider location if available
                    val data = response.body()
                    if (data?.location_available == true && data.latitude != null && data.longitude != null) {
                        providerLocation = LatLng(data.latitude, data.longitude)
                        // Animate camera to new position
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(providerLocation, 16f)
                    }
                } else {
                    errorMessage = "Failed to get location"
                }
            } catch (e: Exception) {
                errorMessage = "Connection error: ${e.message}"
            } finally {
                isLoading = false
            }
            delay(5000) // Refresh every 5 seconds
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Track Provider", fontSize = 18.sp)
                        if (locationData?.provider_name != null) {
                            Text(locationData?.provider_name ?: "", fontSize = 12.sp, fontWeight = FontWeight.Normal)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Call button
                    if (locationData?.provider_phone != null) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${locationData?.provider_phone}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, "Call", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00838F), // Dark Teal
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
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF0D9997))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Getting provider location...", color = Gray600)
                        }
                    }
                }
                
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = Red500
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(errorMessage!!, color = Gray600)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { isLoading = true }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                locationData?.can_track == false -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOff,
                                null,
                                modifier = Modifier.size(80.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Tracking Not Available",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                locationData?.message ?: "Location tracking is only available for accepted bookings",
                                fontSize = 14.sp,
                                color = Gray600,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {
                    // Show the embedded Google Map
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Google Map - Takes up most of the screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    isMyLocationEnabled = false,
                                    mapType = MapType.NORMAL
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = true,
                                    compassEnabled = true
                                )
                            ) {
                                // Provider marker
                                if (locationData?.location_available == true) {
                                    Marker(
                                        state = MarkerState(position = providerLocation),
                                        title = locationData?.provider_name ?: "Provider",
                                        snippet = "Provider is here"
                                    )
                                }
                            }
                            
                            // Status overlay at top of map
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (locationData?.location_available == true) 
                                        Color(0xFF4CAF50).copy(alpha = 0.95f) 
                                    else Color(0xFFFFA000).copy(alpha = 0.95f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (locationData?.location_available == true) Icons.Default.CheckCircle else Icons.Default.Pending,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (locationData?.location_available == true) "Live" else "Waiting...",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Refresh indicator
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        null,
                                        tint = Gray600,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto-refresh", color = Gray600, fontSize = 11.sp)
                                }
                            }
                        }
                        
                        // Bottom info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Provider info row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00838F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            locationData?.provider_name ?: "Provider",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gray900
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (locationData?.location_available == true) Green500 else Orange500
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (locationData?.location_available == true) "Sharing location" else "Waiting for location",
                                                fontSize = 12.sp,
                                                color = if (locationData?.location_available == true) Green500 else Orange500
                                            )
                                        }
                                    }
                                    
                                    // Open in Maps button
                                    if (locationData?.location_available == true) {
                                        OutlinedButton(
                                            onClick = {
                                                val lat = locationData?.latitude
                                                val lng = locationData?.longitude
                                                if (lat != null && lng != null) {
                                                    val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(Provider)")
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                    mapIntent.setPackage("com.google.android.apps.maps")
                                                    context.startActivity(mapIntent)
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Navigate", fontSize = 12.sp)
                                        }
                                    }
                                }
                                
                                // Destination address
                                if (locationData?.destination_address != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = Gray200)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            Icons.Default.Home,
                                            null,
                                            tint = Color(0xFF0D9997),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Your Location", fontSize = 11.sp, color = Gray500)
                                            Text(
                                                locationData?.destination_address ?: "",
                                                fontSize = 13.sp,
                                                color = Gray700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
