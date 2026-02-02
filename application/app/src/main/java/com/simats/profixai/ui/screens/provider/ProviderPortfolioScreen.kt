package com.simats.profixai.ui.screens.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPortfolioScreen(navController: NavController, providerId: Int) {
    var portfolio by remember { mutableStateOf<List<PortfolioImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var selectedImage by remember { mutableStateOf<PortfolioImage?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PortfolioImage?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    fun loadPortfolio() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getProviderPortfolio(ProviderIdRequest(provider_id = providerId))
                if (response.isSuccessful && response.body()?.success == true) {
                    portfolio = response.body()?.portfolio ?: emptyList()
                }
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }
    
    LaunchedEffect(providerId) { loadPortfolio() }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                uploadMessage = null
                try {
                    val imagePart = ImageUploadHelper.createImagePart(context, uri, "image")
                    val providerIdPart = ImageUploadHelper.createIdPart(providerId)
                    val descriptionPart = "".toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    if (imagePart != null) {
                        val response = RetrofitClient.apiService.uploadPortfolioImage(
                            providerIdPart, descriptionPart, imagePart
                        )
                        if (response.isSuccessful && response.body()?.success == true) {
                            uploadMessage = "Image uploaded successfully!"
                            loadPortfolio()
                        } else {
                            uploadMessage = response.body()?.message ?: "Upload failed"
                        }
                    } else {
                        uploadMessage = "Failed to process image"
                    }
                } catch (e: Exception) {
                    uploadMessage = "Error: ${e.message}"
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this portfolio image?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.deletePortfolioImage(
                                    DeletePortfolioRequest(
                                        portfolio_id = showDeleteDialog!!.id,
                                        provider_id = providerId
                                    )
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    loadPortfolio()
                                }
                            } catch (e: Exception) { }
                            showDeleteDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Full screen image viewer
    if (selectedImage != null) {
        Dialog(onDismissRequest = { selectedImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { selectedImage = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "${RetrofitClient.BASE_URL}${selectedImage!!.image_url}",
                    contentDescription = "Portfolio Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Portfolio") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Add, "Add Image")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MechanicColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                containerColor = MechanicColor
            ) {
                Icon(Icons.Default.AddAPhoto, "Upload Image", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1A1A2E))
        ) {
            // Upload message
            if (uploadMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uploadMessage!!.contains("success")) Green500.copy(alpha = 0.2f) else Red500.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        uploadMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = if (uploadMessage!!.contains("success")) Green500 else Red500
                    )
                }
            }
            
            // Info text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Blue500.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Blue500, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Upload images of your previous work to showcase your skills to customers",
                        fontSize = 13.sp,
                        color = Blue500
                    )
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MechanicColor)
                }
            } else if (portfolio.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Collections,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = Gray500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No portfolio images yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the + button to add images of your work",
                            fontSize = 14.sp,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(portfolio) { item ->
                        PortfolioGridItem(
                            item = item,
                            onClick = { selectedImage = item },
                            onDelete = { showDeleteDialog = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioGridItem(
    item: PortfolioImage,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "${RetrofitClient.BASE_URL}${item.image_url}",
                contentDescription = item.description ?: "Portfolio Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Delete button overlay
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
