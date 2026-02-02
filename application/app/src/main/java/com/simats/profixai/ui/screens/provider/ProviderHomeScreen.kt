package com.simats.profixai.ui.screens.provider

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(navController: NavController, providerId: Int) {
    var provider by remember { mutableStateOf<Provider?>(null) }
    var stats by remember { mutableStateOf<ProviderStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    // Function to reload data
    fun loadData() {
        scope.launch {
            try {
                val profileResponse = RetrofitClient.apiService.getProviderProfile(ProviderIdRequest(provider_id = providerId))
                if (profileResponse.isSuccessful && profileResponse.body()?.success == true) {
                    provider = profileResponse.body()?.provider
                }
                
                val statsResponse = RetrofitClient.apiService.getProviderStats(ProviderIdRequest(provider_id = providerId))
                if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                    stats = statsResponse.body()?.stats
                }
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }
    
    LaunchedEffect(providerId) { loadData() }
    
    Scaffold(
        containerColor = Color(0xFF1A1A2E),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF2A2A3E)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Blue500,
                        unselectedIconColor = Gray400,
                        unselectedTextColor = Gray400,
                        indicatorColor = Blue500
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Bookings") },
                    label = { Text("Bookings") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Blue500,
                        unselectedIconColor = Gray400,
                        unselectedTextColor = Gray400,
                        indicatorColor = Blue500
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
                    label = { Text("Earnings") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Blue500,
                        unselectedIconColor = Gray400,
                        unselectedTextColor = Gray400,
                        indicatorColor = Blue500
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Blue500,
                        unselectedIconColor = Gray400,
                        unselectedTextColor = Gray400,
                        indicatorColor = Blue500
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> ProviderDashboardContent(navController, providerId, provider, stats, isLoading, Modifier.padding(paddingValues))
            1 -> ProviderBookingsContent(navController, providerId, Modifier.padding(paddingValues))
            2 -> ProviderEarningsContent(navController, providerId, stats, Modifier.padding(paddingValues))
            3 -> ProviderProfileContent(navController, providerId, provider, { loadData() }, Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun ProviderDashboardContent(
    navController: NavController,
    providerId: Int,
    provider: Provider?,
    stats: ProviderStats?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize().background(Color(0xFF1A1A2E)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Blue500)
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().background(Color(0xFF1A1A2E)).verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E))))
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        imageUrl = provider?.profile_image,
                        name = provider?.full_name ?: "P",
                        size = 64.dp,
                        fontSize = 24.sp,
                        backgroundColor = Blue500,
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Welcome back!", fontSize = 14.sp, color = Gray400)
                        Text(provider?.full_name ?: "Provider", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(provider?.service_name ?: "", fontSize = 14.sp, color = Gray400)
                    }
                    // AI Chat Icon
                    IconButton(
                        onClick = { navController.navigate("provider_ai_chat/$providerId") }
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    // Notifications Icon
                    IconButton(
                        onClick = { navController.navigate("provider_notifications/$providerId") }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Cards
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DarkStatCard(Modifier.weight(1f), "Pending", "${stats?.pending_bookings ?: 0}", Icons.Default.Pending, Orange500)
                DarkStatCard(Modifier.weight(1f), "Completed", "${stats?.completed_bookings ?: 0}", Icons.Default.CheckCircle, Green500)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DarkStatCard(Modifier.weight(1f), "Earnings", "₹${(stats?.total_earnings ?: 0.0).toInt()}", Icons.Default.CurrencyRupee, Blue500)
                DarkStatCard(Modifier.weight(1f), "Rating", "${stats?.average_rating ?: 0.0}", Icons.Default.Star, Amber500)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick Actions
            Text("Quick Actions", Modifier.padding(horizontal = 16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
            ) {
                Column {
                    DarkQuickActionItem(Icons.Default.CalendarMonth, "My Schedule", "Manage your availability") {
                        navController.navigate("provider_schedule/$providerId")
                    }
                    Divider(color = Color(0xFF3A3A4E))
                    DarkQuickActionItem(Icons.Default.AccountBalanceWallet, "Earnings History", "View your earnings") {
                        navController.navigate("earnings_history/$providerId")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProviderBookingsContent(navController: NavController, providerId: Int, modifier: Modifier = Modifier) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    fun loadBookings() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getProviderBookings(ProviderIdRequest(provider_id = providerId))
                if (response.isSuccessful && response.body()?.success == true) {
                    bookings = response.body()?.bookings ?: emptyList()
                }
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }
    
    LaunchedEffect(providerId) { loadBookings() }
    
    val filteredBookings = when (selectedTab) {
        0 -> bookings.filter { it.status == "pending" }
        1 -> bookings.filter { it.status in listOf("accepted", "in_progress") }
        2 -> bookings.filter { it.status == "completed" }
        else -> bookings
    }
    
    Column(modifier = modifier.fillMaxSize().background(Color(0xFF1A1A2E))) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E))))
                .padding(20.dp)
        ) {
            Text("My Bookings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        // Tabs
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
            ) { Text("Pending", Modifier.padding(16.dp)) }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                selectedContentColor = Blue500,
                unselectedContentColor = Gray400
            ) { Text("Active", Modifier.padding(16.dp)) }
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                selectedContentColor = Blue500,
                unselectedContentColor = Gray400
            ) { Text("Completed", Modifier.padding(16.dp)) }
        }
        
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Blue500) }
        } else if (filteredBookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Event, null, Modifier.size(64.dp), Gray500)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No bookings found", color = Gray400)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBookings) { booking ->
                    DarkProviderBookingCard(booking, providerId) { newStatus ->
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.updateBookingStatus(
                                    UpdateBookingStatusRequest(booking_id = booking.id, status = newStatus, provider_id = providerId)
                                )
                                if (response.isSuccessful) { loadBookings() }
                            } catch (e: Exception) { }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderEarningsContent(
    navController: NavController,
    providerId: Int,
    stats: ProviderStats?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E))))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Total Earnings", fontSize = 14.sp, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "₹${(stats?.total_earnings ?: 0.0).toInt()}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Green500, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${stats?.completed_bookings ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Completed Jobs", fontSize = 12.sp, color = Gray400)
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.TrendingUp, null, tint = Blue500, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    val avgEarning = if ((stats?.completed_bookings ?: 0) > 0) {
                        (stats?.total_earnings ?: 0.0) / stats!!.completed_bookings
                    } else 0.0
                    Text("₹${avgEarning.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Avg per Job", fontSize = 12.sp, color = Gray400)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // View Full History Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { navController.navigate("earnings_history/$providerId") },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = Blue500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Earnings History", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("View all transactions", fontSize = 13.sp, color = Gray400)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = Gray500)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Rating Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Amber500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Your Rating", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("Based on customer reviews", fontSize = 13.sp, color = Gray400)
                    }
                }
                Text(
                    "${stats?.average_rating ?: 0.0}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Amber500
                )
            }
        }
    }
}

@Composable
fun ProviderBookingCard(booking: Booking, onStatusUpdate: (String) -> Unit) {
    val context = LocalContext.current
    val statusColor = when (booking.status) {
        "pending" -> Orange500
        "accepted", "in_progress" -> Blue600
        "completed" -> Green500
        else -> Gray500
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = booking.user_image,
                    name = booking.user_name ?: "U",
                    size = 48.dp,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.user_name ?: "Customer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Text(booking.service_name ?: "", fontSize = 13.sp, color = Gray600)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(booking.status.replaceFirstChar { it.uppercase() }, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, color = statusColor)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray200)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), Gray500)
                Text(" ${booking.booking_date}", fontSize = 13.sp, color = Gray700)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), Gray500)
                Text(" ${booking.booking_time}", fontSize = 13.sp, color = Gray700)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), Gray500)
                Text(" ${booking.address}", fontSize = 13.sp, color = Gray700, maxLines = 1)
            }
            
            // View on Map button
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Blue600.copy(alpha = 0.1f))
                    .clickable {
                        val fullAddress = "${booking.address}, ${booking.city ?: ""}, ${booking.pincode ?: ""}"
                        val encodedAddress = Uri.encode(fullAddress)
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddress"))
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?q=$encodedAddress"))
                            context.startActivity(browserIntent)
                        }
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Map, null, Modifier.size(16.dp), Blue600)
                Spacer(Modifier.width(6.dp))
                Text("View on Map", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Blue600)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("₹${booking.total_amount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MechanicColor)
                
                when (booking.status) {
                    "pending" -> Row {
                        OutlinedButton(onClick = { onStatusUpdate("cancelled") }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)) {
                            Text("Reject")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { onStatusUpdate("accepted") }, colors = ButtonDefaults.buttonColors(containerColor = Green500)) {
                            Text("Accept")
                        }
                    }
                    "accepted" -> Button(onClick = { onStatusUpdate("in_progress") }, colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                        Text("Start Job")
                    }
                    "in_progress" -> Button(onClick = { onStatusUpdate("completed") }, colors = ButtonDefaults.buttonColors(containerColor = Green500)) {
                        Text("Mark Complete")
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderProfileContent(
    navController: NavController,
    providerId: Int,
    provider: Provider?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isUploading = true
            uploadMessage = null
            scope.launch {
                try {
                    val imagePart = ImageUploadHelper.createImagePart(context, uri, "image")
                    if (imagePart == null) {
                        uploadMessage = "Failed to process image"
                        isUploading = false
                        return@launch
                    }
                    val providerIdBody = ImageUploadHelper.createIdPart(providerId)
                    
                    val response = RetrofitClient.apiService.uploadProviderImage(providerIdBody, imagePart)
                    if (response.isSuccessful && response.body()?.success == true) {
                        uploadMessage = "Photo updated!"
                        onRefresh()
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

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF1A1A2E)).verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E))))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    ProfileAvatar(
                        imageUrl = provider?.profile_image,
                        name = provider?.full_name ?: "P",
                        size = 80.dp,
                        fontSize = 28.sp,
                        backgroundColor = Blue500,
                        textColor = Color.White
                    )
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).clip(CircleShape).background(Blue500)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(Modifier.size(16.dp), Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CameraAlt, null, Modifier.size(16.dp), Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(provider?.full_name ?: "Provider", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(provider?.service_name ?: "", fontSize = 14.sp, color = Gray400)
                
                if (uploadMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uploadMessage!!, fontSize = 12.sp, color = if (uploadMessage!!.contains("updated")) Green500 else Red500)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
        ) {
            Column(Modifier.padding(20.dp)) {
                DarkProviderInfoRow(Icons.Default.Phone, "Phone", provider?.phone ?: "-")
                Divider(Modifier.padding(vertical = 12.dp), color = Color(0xFF3A3A4E))
                DarkProviderInfoRow(Icons.Default.CurrencyRupee, "Hourly Rate", "₹${provider?.hourly_rate?.toInt() ?: 0}")
                Divider(Modifier.padding(vertical = 12.dp), color = Color(0xFF3A3A4E))
                DarkProviderInfoRow(Icons.Default.Star, "Experience", "${provider?.experience_years ?: 0} years")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Menu Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
        ) {
            Column {
                DarkProfileMenuItem(Icons.Default.Edit, "Edit Profile") { navController.navigate("edit_provider_profile/$providerId") }
                Divider(color = Color(0xFF3A3A4E))
                DarkProfileMenuItem(Icons.Default.Collections, "My Portfolio") { navController.navigate("provider_portfolio/$providerId") }
                Divider(color = Color(0xFF3A3A4E))
                DarkProfileMenuItem(Icons.Default.AccountBalanceWallet, "Earnings History") { navController.navigate("earnings_history/$providerId") }
                Divider(color = Color(0xFF3A3A4E))
                DarkProfileMenuItem(Icons.Default.Notifications, "Notifications") { navController.navigate("provider_notifications/$providerId") }
                Divider(color = Color(0xFF3A3A4E))
                DarkProfileMenuItem(Icons.Default.Help, "Help & Support") { navController.navigate("help_provider") }
                Divider(color = Color(0xFF3A3A4E))
                DarkProfileMenuItem(Icons.Default.Info, "About ProFIX AI") { navController.navigate("about") }
                Divider(color = Color(0xFF3A3A4E))
                DarkProfileMenuItem(Icons.Default.Logout, "Logout", Red500) {
                    navController.navigate("role_selection") { popUpTo(0) { inclusive = true } }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProviderInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(24.dp), MechanicColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Gray500)
            Text(value, fontSize = 16.sp, color = Gray900)
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, color: Color = Gray900, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), Gray400)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text(text = title, fontSize = 14.sp, color = Gray600)
        }
    }
}

@Composable
fun DarkProviderBookingCard(booking: Booking, providerId: Int = 0, onStatusUpdate: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSharingLocation by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf<String?>(null) }
    
    val statusColor = when (booking.status) {
        "pending" -> Orange500
        "accepted", "in_progress" -> Blue600
        "completed" -> Green500
        else -> Gray500
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = booking.user_image,
                    name = booking.user_name ?: "U",
                    size = 48.dp,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.user_name ?: "Customer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(booking.service_name ?: "", fontSize = 13.sp, color = Gray400)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(booking.status.replaceFirstChar { it.uppercase() }, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, color = statusColor)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFF3A3A4E))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), Gray500)
                Text(" ${booking.booking_date}", fontSize = 13.sp, color = Gray400)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), Gray500)
                Text(" ${booking.booking_time}", fontSize = 13.sp, color = Gray400)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), Gray500)
                Text(" ${booking.address}", fontSize = 13.sp, color = Gray400, maxLines = 1)
            }
            
            // View on Map button
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Blue600.copy(alpha = 0.15f))
                    .clickable {
                        val fullAddress = "${booking.address}, ${booking.city ?: ""}, ${booking.pincode ?: ""}"
                        val encodedAddress = Uri.encode(fullAddress)
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddress"))
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?q=$encodedAddress"))
                            context.startActivity(browserIntent)
                        }
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Map, null, Modifier.size(16.dp), Blue600)
                Spacer(Modifier.width(6.dp))
                Text("View on Map", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Blue600)
            }
            
            // Location Sharing Status
            if (locationMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    locationMessage!!,
                    fontSize = 12.sp,
                    color = if (locationMessage!!.contains("success", ignoreCase = true)) Green500 else Orange500,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("₹${booking.total_amount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Blue500)
                
                when (booking.status) {
                    "pending" -> Row {
                        OutlinedButton(onClick = { onStatusUpdate("cancelled") }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)) {
                            Text("Reject")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { onStatusUpdate("accepted") }, colors = ButtonDefaults.buttonColors(containerColor = Green500)) {
                            Text("Accept")
                        }
                    }
                    "accepted", "in_progress" -> Column(horizontalAlignment = Alignment.End) {
                        // Location Logic
                        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                        
                        val locationPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                                // Permission granted, try sharing again (user needs to click again to be safe/simple, or we could auto-trigger)
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
                                                        locationMessage = "✓ Location shared"
                                                    } else {
                                                        locationMessage = "Failed to share"
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
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) {
                            if (isSharingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share Loc", fontSize = 12.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Status Button
                        Button(
                            onClick = { 
                                val nextStatus = if (booking.status == "accepted") "in_progress" else "completed"
                                onStatusUpdate(nextStatus) 
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = if (booking.status == "accepted") Blue600 else Green500)
                        ) {
                            Text(if (booking.status == "accepted") "Start Job" else "Mark Complete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DarkProviderInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(24.dp), Blue500)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Gray500)
            Text(value, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun DarkProfileMenuItem(icon: ImageVector, title: String, color: Color = Color.White, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), Gray500)
    }
}

@Composable
fun DarkStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 14.sp, color = Gray400)
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MechanicColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Gray900)
            Text(text = subtitle, fontSize = 14.sp, color = Gray600)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Gray400)
    }
}

@Composable
fun DarkQuickActionItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, fontSize = 14.sp, color = Gray400)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Gray500)
    }
}
