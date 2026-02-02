package com.simats.profixai.ui.screens.provider

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderBookingsScreen(navController: NavController, providerId: Int) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf("all") }
    
    val scope = rememberCoroutineScope()
    
    fun loadBookings() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getProviderBookings(ProviderIdRequest(provider_id = providerId))
                if (response.isSuccessful && response.body()?.success == true) {
                    bookings = response.body()?.bookings ?: emptyList()
                }
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }
    
    LaunchedEffect(Unit) { loadBookings() }
    
    val filteredBookings = when (selectedTab) {
        0 -> bookings.filter { it.status == "pending" }
        1 -> bookings.filter { it.status in listOf("accepted", "in_progress") }
        2 -> bookings.filter { it.status == "completed" }
        else -> bookings
    }.let { list ->
        when (selectedFilter) {
            "highest" -> list.sortedByDescending { it.total_amount }
            else -> list
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1A1A2E))
        ) {
            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChipItem(
                    text = "All Requests",
                    selected = selectedFilter == "all",
                    icon = Icons.Default.FilterList,
                    onClick = { selectedFilter = "all" }
                )
                FilterChipItem(
                    text = "Highest Pay",
                    selected = selectedFilter == "highest",
                    icon = null,
                    onClick = { selectedFilter = "highest" }
                )
                FilterChipItem(
                    text = "Nearest",
                    selected = selectedFilter == "nearest",
                    icon = null,
                    onClick = { selectedFilter = "nearest" }
                )
            }
            
            // Tab Row for status
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF2A2A3E),
                contentColor = Blue500
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = Blue500,
                    unselectedContentColor = Gray400
                ) {
                    Text("Pending", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 0) FontWeight.Medium else FontWeight.Normal)
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = Blue500,
                    unselectedContentColor = Gray400
                ) {
                    Text("Active", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 1) FontWeight.Medium else FontWeight.Normal)
                }
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    selectedContentColor = Blue500,
                    unselectedContentColor = Gray400
                ) {
                    Text("Done", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 2) FontWeight.Medium else FontWeight.Normal)
                }
            }
            
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Blue500)
                }
                filteredBookings.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(80.dp), tint = Gray500)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No bookings", fontSize = 18.sp, color = Gray400)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredBookings) { booking ->
                        EnhancedBookingCard(
                            booking = booking,
                            providerId = providerId,
                            onAccept = {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.updateBookingStatus(
                                            UpdateBookingStatusRequest(booking_id = booking.id, status = "accepted", provider_id = providerId)
                                        )
                                        loadBookings()
                                    } catch (e: Exception) { }
                                }
                            },
                            onDecline = {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.updateBookingStatus(
                                            UpdateBookingStatusRequest(booking_id = booking.id, status = "cancelled", provider_id = providerId)
                                        )
                                        loadBookings()
                                    } catch (e: Exception) { }
                                }
                            },
                            onComplete = {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.updateBookingStatus(
                                            UpdateBookingStatusRequest(booking_id = booking.id, status = "completed", provider_id = providerId)
                                        )
                                        loadBookings()
                                    } catch (e: Exception) { }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    text: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Blue600 else Color(0xFF2A2A3E),
        contentColor = if (selected) Color.White else Gray400
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(16.dp))
            }
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EnhancedBookingCard(
    booking: Booking,
    providerId: Int = 0,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSharingLocation by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf<String?>(null) }
    
    val serviceIcon = when (booking.service_name?.lowercase()) {
        "cleaner" -> "🧹"
        "electrician" -> "⚡"
        "painter" -> "🎨"
        "salon" -> "💇"
        "carpenter" -> "🪚"
        "mechanic" -> "🔧"
        "plumber" -> "🔧"
        else -> "🛠️"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Service name, badge, price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Service Icon
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Blue600.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(serviceIcon, fontSize = 20.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = booking.service_name ?: "Service",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            
                            // Urgent badge for high-value bookings
                            if (booking.total_amount >= 500) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Orange500
                                ) {
                                    Text(
                                        "URGENT",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Text(
                            text = booking.description ?: "No description",
                            fontSize = 13.sp,
                            color = Gray400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${booking.total_amount.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Fixed Price",
                        fontSize = 11.sp,
                        color = Gray500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Distance and Estimated Hours chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Distance chip (placeholder since we don't have real distance data)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1A2E)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = Gray400
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${booking.city ?: "Nearby"}",
                            fontSize = 12.sp,
                            color = Gray300
                        )
                    }
                }
                
                // Estimated hours
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1A2E)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = Gray400
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Est. ${booking.estimated_hours ?: 1} hours",
                            fontSize = 12.sp,
                            color = Gray300
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
                
            // Customer info with profile image
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    imageUrl = booking.user_image,
                    name = booking.user_name ?: "Customer",
                    size = 32.dp,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        booking.user_name ?: "Customer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray200
                    )
                    Text(
                        "${booking.booking_date} at ${booking.booking_time}",
                        fontSize = 11.sp,
                        color = Gray500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Address with map icon and View on Map button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = Gray400
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        booking.address,
                        fontSize = 12.sp,
                        color = Gray300,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // View on Map button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Blue600.copy(alpha = 0.15f))
                        .clickable {
                            // Open Google Maps with the address
                            val fullAddress = "${booking.address}, ${booking.city ?: ""}, ${booking.pincode ?: ""}"
                            val encodedAddress = Uri.encode(fullAddress)
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddress"))
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                // If Google Maps is not installed, open in browser
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?q=$encodedAddress"))
                                context.startActivity(browserIntent)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = Blue500
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "View on Map",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue500
                    )
                }
            }
            
            // Location sharing status message
            if (locationMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    locationMessage!!,
                    fontSize = 12.sp,
                    color = if (locationMessage!!.contains("success", ignoreCase = true)) Green500 else Orange500,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (booking.status == "pending") {
                    // Decline Button
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray300),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gray600)
                    ) {
                        Text("Decline", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    
                    // Accept Button
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                    ) {
                        Text("Accept Job", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                if (booking.status == "accepted" || booking.status == "in_progress") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Location Logic
                        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                        
                        val locationPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                                locationMessage = "Permission granted. Tap Share again."
                            } else {
                                locationMessage = "Location permission needed"
                            }
                        }

                        // Share Location Button
                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    
                                    isSharingLocation = true
                                    locationMessage = "Fetching precise location..."
                                    
                                    // Use getCurrentLocation instead of lastLocation for better reliability
                                    fusedLocationClient.getCurrentLocation(
                                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 
                                        null
                                    ).addOnSuccessListener { location ->
                                        if (location != null) {
                                            scope.launch {
                                                try {
                                                    val response = RetrofitClient.apiService.updateProviderLocation(
                                                        UpdateLocationRequest(
                                                            provider_id = providerId,
                                                            booking_id = booking.id,
                                                            latitude = location.latitude,
                                                            longitude = location.longitude,
                                                            is_sharing = true
                                                        )
                                                    )
                                                    if (response.isSuccessful && response.body()?.success == true) {
                                                        locationMessage = "✓ Location shared successfully"
                                                    } else {
                                                        locationMessage = "Failed to share location"
                                                    }
                                                } catch (e: Exception) {
                                                    locationMessage = "Error: ${e.message}"
                                                } finally {
                                                    isSharingLocation = false
                                                }
                                            }
                                        } else {
                                            locationMessage = "GPS signal weak. Move outside."
                                            isSharingLocation = false
                                        }
                                    }.addOnFailureListener { e ->
                                        locationMessage = "Loc Error: ${e.message}"
                                        isSharingLocation = false
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            enabled = !isSharingLocation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) {
                            if (isSharingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share My Location", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Complete Button
                        Button(
                            onClick = onComplete,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) {
                            Text("Mark as Complete", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                if (booking.status == "completed") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Green500.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = Green500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Completed", color = Green500, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
