package com.simats.profixai.ui.screens.user

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavController, userId: Int) {
    // --- State Variables ---
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Menu & Delete States ---
    var moreMenuExpanded by remember { mutableStateOf(false) } // Controls the 3-dot menu
    var showDeleteDialog by remember { mutableStateOf(false) } // Controls the popup
    var isDeleting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Load User Data ---
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getUserProfile(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                user = response.body()?.user
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // --- DELETE CONFIRMATION DIALOG (Same as before) ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure? This will permanently delete your account, bookings, and history. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.deleteUserAccount(UserIdRequest(user_id = userId))
                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                                    navController.navigate("role_selection") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                    isDeleting = false
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                isDeleting = false
                            } finally {
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }

    // --- MAIN UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                // 👇 ADDED THIS ACTIONS BLOCK FOR THE 3 DOTS
                actions = {
                    IconButton(onClick = { moreMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                    }

                    // The Dropdown Menu
                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Account", color = Color.Red) },
                            onClick = {
                                moreMenuExpanded = false
                                showDeleteDialog = true // Trigger the dialog
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D9997),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0D9997))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF0D9997))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.full_name ?: "U").take(2).uppercase(),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9997)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user?.full_name ?: "User Name",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = user?.email ?: "email@example.com",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // 2. INFO CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone", value = user?.phone ?: "-")
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                        ProfileInfoRow(icon = Icons.Default.LocationCity, label = "City", value = user?.city ?: "-")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. MENU LIST (Cleaned up - No Red Button Here)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        ProfileMenuItem(icon = Icons.Default.Edit, title = "Edit Profile") {
                            navController.navigate("edit_user_profile/$userId")
                        }
                        Divider(color = Color(0xFFEEEEEE))

                        ProfileMenuItem(icon = Icons.Default.History, title = "Booking History") {
                            navController.navigate("user_bookings/$userId")
                        }
                        Divider(color = Color(0xFFEEEEEE))

                        ProfileMenuItem(icon = Icons.Default.Notifications, title = "Notifications") {
                            // Navigate
                        }
                        Divider(color = Color(0xFFEEEEEE))

                        ProfileMenuItem(icon = Icons.Default.HelpOutline, title = "Help & Support") {
                            // Navigate
                        }
                        Divider(color = Color(0xFFEEEEEE))

                        ProfileMenuItem(icon = Icons.Default.Info, title = "About ProFIX AI") {
                            // Navigate
                        }
                        Divider(color = Color(0xFFEEEEEE))

                        // Logout
                        ProfileMenuItem(
                            icon = Icons.Default.Logout,
                            title = "Logut",
                            tint = Color.Red
                        ) {
                            navController.navigate("role_selection") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        // 🛑 No Delete Button here anymore! It's in the top right menu.
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0D9997), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, tint: Color = Color.Gray, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, color = if(tint == Color.Red) Color.Red else Color.Black, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}