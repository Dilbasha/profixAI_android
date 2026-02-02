package com.simats.profixai.ui.screens.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(navController: NavController, providerId: Int) {
    var provider by remember { mutableStateOf<Provider?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                uploadMessage = null
                try {
                    val imagePart = ImageUploadHelper.createImagePart(context, uri)
                    val idPart = ImageUploadHelper.createIdPart(providerId)

                    if (imagePart != null) {
                        val response = RetrofitClient.apiService.uploadProviderImage(idPart, imagePart)
                        if (response.isSuccessful && response.body()?.success == true) {
                            uploadMessage = "Image uploaded successfully!"
                            // Refresh profile
                            val profileResponse = RetrofitClient.apiService.getProviderProfile(ProviderIdRequest(provider_id = providerId))
                            if (profileResponse.isSuccessful && profileResponse.body()?.success == true) {
                                provider = profileResponse.body()?.provider
                            }
                        } else {
                            // Show detailed error
                            val errorBody = response.errorBody()?.string() ?: "No error body"
                            uploadMessage = response.body()?.message ?: "Upload failed: $errorBody"
                        }
                    } else {
                        uploadMessage = "Failed to process image"
                    }
                } catch (e: Exception) {
                    uploadMessage = "Error: ${e.message}"
                    e.printStackTrace()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    LaunchedEffect(providerId) {
        try {
            val response = RetrofitClient.apiService.getProviderProfile(ProviderIdRequest(provider_id = providerId))
            if (response.isSuccessful && response.body()?.success == true) {
                provider = response.body()?.provider
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MechanicColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MechanicColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Gray50)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.verticalGradient(colors = listOf(MechanicColor, MechanicColor.copy(alpha = 0.8f))))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Profile Image with upload button
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (provider?.profile_image != null && provider?.profile_image!!.isNotEmpty()) {
                                AsyncImage(
                                    model = "${RetrofitClient.BASE_URL}${provider?.profile_image}",
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(100.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        (provider?.full_name ?: "P").take(2).uppercase(),
                                        fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MechanicColor
                                    )
                                }
                            }

                            // Camera button overlay
                            IconButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CameraAlt, "Upload", tint = MechanicColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (uploadMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uploadMessage!!, fontSize = 12.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(provider?.full_name ?: "Provider", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(provider?.service_name ?: "", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${provider?.rating ?: 0.0}", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Rating", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${provider?.total_jobs ?: 0}", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Jobs", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${provider?.experience_years ?: 0}y", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Exp", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Account Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProviderDetailRow(Icons.Default.Email, "Email", provider?.email ?: "-")
                        Divider(Modifier.padding(vertical = 12.dp), color = Gray200)
                        ProviderDetailRow(Icons.Default.Phone, "Phone", provider?.phone ?: "-")
                        Divider(Modifier.padding(vertical = 12.dp), color = Gray200)
                        ProviderDetailRow(Icons.Default.CurrencyRupee, "Hourly Rate", "₹${provider?.hourly_rate?.toInt() ?: 0}")
                        Divider(Modifier.padding(vertical = 12.dp), color = Gray200)
                        ProviderDetailRow(Icons.Default.LocationCity, "City", provider?.city ?: "-")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        ProviderMenuItem(Icons.Default.Edit, "Edit Profile") { navController.navigate("edit_provider_profile/$providerId") }
                        Divider(color = Gray200)
                        ProviderMenuItem(Icons.Default.CalendarMonth, "My Bookings") { navController.navigate("provider_bookings/$providerId") }
                        Divider(color = Gray200)
                        ProviderMenuItem(Icons.Default.AccountBalanceWallet, "Earnings History") { navController.navigate("earnings_history/$providerId") }
                        Divider(color = Gray200)
                        ProviderMenuItem(Icons.Default.Notifications, "Notifications") { navController.navigate("provider_notifications/$providerId") }
                        Divider(color = Gray200)
                        ProviderMenuItem(Icons.Default.Settings, "Settings") { }
                        Divider(color = Gray200)
                        ProviderMenuItem(Icons.Default.Logout, "Logout", Red500) {
                            navController.navigate("role_selection") { popUpTo(0) { inclusive = true } }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProviderDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MechanicColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Gray500)
            Text(text = value, fontSize = 16.sp, color = Gray900)
        }
    }
}

@Composable
fun ProviderMenuItem(icon: ImageVector, title: String, tint: Color = Gray700, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, color = tint, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = Gray400)
    }
}
