package com.simats.profixai.ui.screens.admin

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
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController, adminId: Int) {
    var pendingCount by remember { mutableStateOf(0) }
    var pendingProviders by remember { mutableStateOf<List<Provider>>(emptyList()) }
    var adminStats by remember { mutableStateOf<AdminStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    fun loadData() {
        scope.launch {
            try {
                // Load pending providers
                val response = RetrofitClient.apiService.getPendingProviders()
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingProviders = response.body()?.providers ?: emptyList()
                    pendingCount = pendingProviders.size
                }
                
                // Load admin stats
                val statsResponse = RetrofitClient.apiService.getAdminStats()
                if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                    adminStats = statsResponse.body()?.stats
                }
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }
    
    LaunchedEffect(Unit) { loadData() }
    
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF009688),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF009688)
                    )
                )
                NavigationBarItem(
                    icon = { 
                        BadgedBox(badge = { if (pendingCount > 0) Badge { Text("$pendingCount") } }) {
                            Icon(Icons.Default.HowToReg, contentDescription = "Approvals")
                        }
                    },
                    label = { Text("Approvals") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF009688),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF009688)
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> AdminHubContent(navController, adminStats, Modifier.padding(paddingValues))
            1 -> AdminApprovalsContent(pendingProviders, isLoading, { loadData() }, Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun AdminHubContent(navController: NavController, adminStats: AdminStats?, modifier: Modifier = Modifier) {
    val tealColor = Color(0xFF009688)
    
    Column(
        modifier = modifier.fillMaxSize().background(tealColor).verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hello Admin", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            
            // Logout Button
            IconButton(
                onClick = { navController.navigate("role_selection") { popUpTo(0) { inclusive = true } } }
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Large Cards
        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Dashboard Card with real stats
            HubCard(
                title = "Dashboard",
                subtitle = "${adminStats?.total_bookings ?: 0} bookings",
                date = "${adminStats?.total_users ?: 0} users",
                bgColor = Color(0xFFFBE9C8), // Light Beige
                icon = Icons.Default.PieChart,
                textColor = Color.Black,
                onClick = { navController.navigate("admin_dashboard_stats") }
            )
            
            // Track Labours Card with real provider count
            HubCard(
                title = "track labours",
                subtitle = "${(adminStats?.total_hours_worked ?: 0.0).toInt()} hours worked",
                date = "${adminStats?.approved_providers ?: 0} providers",
                bgColor = Color(0xFFE0F2F1), // Light Cyan/Teal
                icon = Icons.Default.Group, 
                textColor = Color.Black,
                onClick = { navController.navigate("admin_labor_tracking") },
                isLabor = true
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HubCard(
    title: String,
    subtitle: String,
    date: String,
    bgColor: Color,
    icon: ImageVector,
    textColor: Color,
    onClick: () -> Unit,
    isLabor: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp)) {
             // Top Right Arrow
             Box(
                 modifier = Modifier.align(Alignment.TopEnd).border(1.dp, textColor, CircleShape).padding(8.dp)
             ) {
                 Icon(Icons.Default.ArrowOutward, null, Modifier.size(20.dp), textColor)
             }
             
             Column(Modifier.align(Alignment.CenterStart)) {
                 if (!isLabor) {
                     // 3D Chart Icon Placeholder area
                     Box(Modifier.size(60.dp).background(Color.Black.copy(0.05f), CircleShape), Alignment.Center) {
                         Icon(icon, null, Modifier.size(30.dp), textColor.copy(0.5f))
                     }
                     Spacer(modifier = Modifier.height(12.dp))
                 } else {
                     Spacer(modifier = Modifier.height(24.dp))
                     Box(Modifier.background(Color.White, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                         Text("Labour link", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                     }
                     Spacer(modifier = Modifier.height(12.dp))
                 }
                 
                 Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = textColor)
                 
                 Spacer(modifier = Modifier.height(4.dp))
                 
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), textColor.copy(0.6f))
                     Text(" $date", fontSize = 14.sp, color = textColor.copy(0.6f))
                 }
                 
                 Text(subtitle, fontSize = 14.sp, color = if(isLabor) Color(0xFF5C6BC0) else textColor.copy(0.6f))
             }
        }
    }
}

@Composable
fun AdminApprovalsContent(
    providers: List<Provider>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val tealColor = Color(0xFF009688)
    val cardBg = Color(0xFF1A383D)
    
    // Rejection dialog state
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    var isRejecting by remember { mutableStateOf(false) }
    
    // Rejection Dialog
    if (showRejectDialog && selectedProvider != null) {
        AlertDialog(
            onDismissRequest = { 
                if (!isRejecting) {
                    showRejectDialog = false 
                    rejectionReason = ""
                }
            },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFEF5350)) },
            title = { 
                Text(
                    "Reject ${selectedProvider?.full_name}?",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column {
                    Text(
                        "Please provide a reason for rejection. This will be shown to the provider when they try to login.",
                        fontSize = 14.sp,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("Enter rejection reason...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quick rejection reasons
                    Text("Quick reasons:", fontSize = 12.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { rejectionReason = "Invalid documents" },
                                label = { Text("Invalid documents", fontSize = 11.sp) }
                            )
                            AssistChip(
                                onClick = { rejectionReason = "Incomplete profile" },
                                label = { Text("Incomplete profile", fontSize = 11.sp) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { rejectionReason = "Failed verification" },
                                label = { Text("Failed verification", fontSize = 11.sp) }
                            )
                            AssistChip(
                                onClick = { rejectionReason = "Policy violation" },
                                label = { Text("Policy violation", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (rejectionReason.isNotBlank() && selectedProvider != null) {
                            scope.launch {
                                isRejecting = true
                                try {
                                    val response = RetrofitClient.apiService.providerAction(
                                        ProviderActionRequest(
                                            provider_id = selectedProvider!!.id,
                                            action = "reject",
                                            rejection_reason = rejectionReason
                                        )
                                    )
                                    if (response.isSuccessful) {
                                        showRejectDialog = false
                                        rejectionReason = ""
                                        selectedProvider = null
                                        onRefresh()
                                    }
                                } catch (e: Exception) { }
                                finally { isRejecting = false }
                            }
                        }
                    },
                    enabled = rejectionReason.isNotBlank() && !isRejecting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    if (isRejecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRejectDialog = false 
                        rejectionReason = ""
                    },
                    enabled = !isRejecting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Column(modifier = modifier.fillMaxSize().background(tealColor)) {
        // Header matching Admin Hub style
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pending Approvals", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            // Badge showing count
            if (providers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFC107), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("${providers.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
        
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.White) }
        } else if (providers.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(80.dp), Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("All caught up!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No pending approvals", color = Color.White.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers) { provider ->
                    AdminProviderCardWithDialog(
                        provider = provider, 
                        cardBg = cardBg,
                        onApprove = {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.providerAction(
                                        ProviderActionRequest(provider_id = provider.id, action = "approve")
                                    )
                                    if (response.isSuccessful) { onRefresh() }
                                } catch (e: Exception) { }
                            }
                        },
                        onReject = {
                            selectedProvider = provider
                            showRejectDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminProviderCardWithDialog(
    provider: Provider, 
    cardBg: Color, 
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = provider.profile_image,
                    name = provider.full_name,
                    size = 56.dp,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(provider.full_name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(provider.service_name ?: "", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(provider.email, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Icon(Icons.Default.Phone, null, Modifier.size(14.dp), Color.White.copy(alpha = 0.6f))
                Text(" ${provider.phone}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.CurrencyRupee, null, Modifier.size(14.dp), Color.White.copy(alpha = 0.6f))
                Text(" ${provider.hourly_rate.toInt()}/hr", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color(0xFFEF5350)))
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun AdminProviderCard(provider: Provider, cardBg: Color = Color(0xFF1A383D), onAction: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = provider.profile_image,
                    name = provider.full_name,
                    size = 56.dp,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(provider.full_name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(provider.service_name ?: "", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(provider.email, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Icon(Icons.Default.Phone, null, Modifier.size(14.dp), Color.White.copy(alpha = 0.6f))
                Text(" ${provider.phone}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.CurrencyRupee, null, Modifier.size(14.dp), Color.White.copy(alpha = 0.6f))
                Text(" ${provider.hourly_rate.toInt()}/hr", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onAction("reject") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color(0xFFEF5350)))
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reject")
                }
                Button(
                    onClick = { onAction("approve") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(20.dp), color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text(title, fontSize = 14.sp, color = Gray600)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuItem(icon: ImageVector, title: String, subtitle: String, badge: Int = 0, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), Gray800)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Gray900)
                if (badge > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge { Text("$badge") }
                }
            }
            Text(subtitle, fontSize = 14.sp, color = Gray600)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Gray400)
    }
}
