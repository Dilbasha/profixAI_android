package com.simats.profixai.ui.screens.admin

import androidx.compose.foundation.background
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
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsScreen(navController: NavController) {
    var providers by remember { mutableStateOf<List<Provider>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Rejection dialog state
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    var isRejecting by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    fun loadProviders() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.getPendingProviders()
                if (response.isSuccessful && response.body()?.success == true) {
                    providers = response.body()?.providers ?: emptyList()
                } else {
                    errorMessage = response.body()?.toString() ?: "Failed to load"
                }
            } catch (e: Exception) { 
                errorMessage = "Error: ${e.message}"
            }
            finally { isLoading = false }
        }
    }
    
    fun rejectProvider(provider: Provider, reason: String) {
        scope.launch {
            isRejecting = true
            try {
                val response = RetrofitClient.apiService.providerAction(
                    ProviderActionRequest(
                        provider_id = provider.id,
                        action = "reject",
                        rejection_reason = reason
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    showRejectDialog = false
                    rejectionReason = ""
                    selectedProvider = null
                    loadProviders()
                }
            } catch (e: Exception) { }
            finally { isRejecting = false }
        }
    }
    
    LaunchedEffect(Unit) { loadProviders() }
    
    // Rejection Reason Dialog
    if (showRejectDialog && selectedProvider != null) {
        AlertDialog(
            onDismissRequest = { 
                if (!isRejecting) {
                    showRejectDialog = false 
                    rejectionReason = ""
                }
            },
            icon = { Icon(Icons.Default.Warning, null, tint = Red500) },
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
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Quick rejection reasons
                    Text("Quick reasons:", fontSize = 12.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Invalid documents", "Incomplete profile").forEach { reason ->
                            AssistChip(
                                onClick = { rejectionReason = reason },
                                label = { Text(reason, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Failed verification", "Policy violation").forEach { reason ->
                            AssistChip(
                                onClick = { rejectionReason = reason },
                                label = { Text(reason, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (rejectionReason.isNotBlank() && selectedProvider != null) {
                            rejectProvider(selectedProvider!!, rejectionReason)
                        }
                    },
                    enabled = rejectionReason.isNotBlank() && !isRejecting,
                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                ) {
                    if (isRejecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Reject Provider")
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Approvals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Gray800,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Gray50)) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gray800)
                }
                errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, Modifier.size(80.dp), Red500)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error loading data", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Gray700)
                        Text(errorMessage!!, fontSize = 14.sp, color = Gray500)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadProviders() }) { Text("Retry") }
                    }
                }
                providers.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(80.dp), Green500)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("All caught up!", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Gray700)
                        Text("No pending approvals", fontSize = 14.sp, color = Gray500)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("${providers.size} provider(s) awaiting approval", fontSize = 14.sp, color = Gray600)
                    }
                    items(providers) { provider ->
                        PendingProviderCard(
                            provider = provider,
                            onApprove = {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.providerAction(
                                            ProviderActionRequest(provider_id = provider.id, action = "approve")
                                        )
                                        loadProviders()
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
}

@Composable
fun PendingProviderCard(provider: Provider, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = provider.profile_image,
                    name = provider.full_name,
                    size = 56.dp,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(provider.full_name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Text(provider.service_name ?: "Service Provider", fontSize = 14.sp, color = Blue600)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray200)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, Modifier.size(16.dp), Gray500)
                Spacer(Modifier.width(8.dp)); Text(provider.email, fontSize = 14.sp, color = Gray700)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, Modifier.size(16.dp), Gray500)
                Spacer(Modifier.width(8.dp)); Text(provider.phone, fontSize = 14.sp, color = Gray700)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, null, Modifier.size(16.dp), Gray500)
                Spacer(Modifier.width(8.dp)); Text("₹${provider.hourly_rate.toInt()}/hour", fontSize = 14.sp, color = Gray700)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.WorkHistory, null, Modifier.size(16.dp), Gray500)
                Spacer(Modifier.width(8.dp)); Text("${provider.experience_years} years", fontSize = 14.sp, color = Gray700)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp)); Text("Reject")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp)); Text("Approve")
                }
            }
        }
    }
}
