package com.simats.profixai.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLaborTrackingScreen(navController: NavController) {
    val tealColor = Color(0xFF009688)
    val darkBg = Color(0xFF102A2E)
    val cardBg = Color(0xFF1A383D)
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var providers by remember { mutableStateOf<List<Provider>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            // Fetch approved/verified providers for labor tracking
            val response = RetrofitClient.apiService.getApprovedProviders()
            if (response.isSuccessful && response.body()?.success == true) {
                providers = response.body()?.providers ?: emptyList()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    val filteredProviders = remember(providers, searchQuery, selectedFilter) {
        providers.filter { 
            it.full_name.contains(searchQuery, ignoreCase = true) ||
            (it.service_name?.contains(searchQuery, ignoreCase = true) == true)
        }.filter {
            when (selectedFilter) {
                "Active" -> it.is_available == 1
                "Inactive" -> it.is_available == 0
                else -> true
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(tealColor).padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Labor Tracking", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text("${providers.size} Providers", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
            
            // Search Bar
            Box(Modifier.padding(horizontal = 16.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by provider name...", color = Color.White.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filters
            Row(
                modifier = Modifier.padding(horizontal = 16.dp), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Inactive").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2196F3),
                            selectedLabelColor = Color.White,
                            containerColor = cardBg,
                            labelColor = Color.White
                        ),
                        border = null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.White) }
            } else if (filteredProviders.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, null, Modifier.size(64.dp), Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No providers found", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProviders) { provider ->
                        LaborerCard(provider, cardBg, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun LaborerCard(provider: Provider, bgColor: Color, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("admin_provider_profile/${provider.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(provider.profile_image, provider.full_name, 48.dp, 18.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(provider.full_name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(provider.service_name ?: "Worker", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Text(provider.city ?: "Site A", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
            }
            // Status Dot
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (provider.is_available == 1) Color(0xFF4CAF50) else Color.Red))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (provider.is_available == 1) "Active" else "Offline", fontSize = 12.sp, color = if (provider.is_available == 1) Color(0xFF4CAF50) else Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun MockLaborerCard(index: Int, bgColor: Color) {
    val names = listOf("John Smith", "Sarah Lee", "Michael Chen")
    val roles = listOf("Material Transport", "Inspection Duty", "Electrical Wiring")
    val sites = listOf("South Warehouse", "West End Site", "Downtown Tower")
    val statusColors = listOf(Color(0xFFFFC107), Color.Gray, Color(0xFF4CAF50))
    val statusTexts = listOf("On Break", "Clocked Out", "Active")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(names[index % 3], fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(sites[index % 3], fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Text(roles[index % 3], fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
            }
            // Status Dot
            Box(Modifier.size(8.dp).clip(CircleShape).background(statusColors[index % 3]))
            Spacer(modifier = Modifier.width(6.dp))
            Text(statusTexts[index % 3], fontSize = 12.sp, color = statusColors[index % 3])
        }
    }
}
