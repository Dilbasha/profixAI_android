package com.simats.profixai.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileScreen(navController: NavController, userId: Int) {
    // --- Data State ---
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    // --- Error State ---
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var pincodeError by remember { mutableStateOf<String?>(null) }

    // --- UI State ---
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // --- Delete Account State (NEW) ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load current profile
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getUserProfile(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.let { user ->
                    fullName = user.full_name
                    phone = user.phone
                    address = user.address ?: ""
                    city = user.city ?: ""
                    pincode = user.pincode ?: ""
                }
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    // --- DELETE CONFIRMATION POPUP (NEW) ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = Red500) },
            text = {
                Text("Are you sure? This will permanently delete your account and all data. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeletingAccount = true
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.deleteUserAccount(UserIdRequest(user_id = userId))
                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_LONG).show()
                                    // Log out and go to role selection
                                    navController.navigate("role_selection") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                    isDeletingAccount = false
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                isDeletingAccount = false
                            } finally {
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    enabled = !isDeletingAccount
                ) {
                    if (isDeletingAccount) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0D9997))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Gray50)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)

                        Spacer(modifier = Modifier.height(20.dp))

                        // 1. Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                if (it.matches(Regex("^[a-zA-Z ]*$"))) {
                                    fullName = it
                                    nameError = null
                                }
                            },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D9997),
                                focusedLabelColor = Color(0xFF0D9997)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    phone = it
                                    phoneError = null
                                }
                            },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = phoneError != null,
                            supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D9997),
                                focusedLabelColor = Color(0xFF0D9997)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Address
                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                addressError = null
                            },
                            label = { Text("Address") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = addressError != null,
                            supportingText = { addressError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D9997),
                                focusedLabelColor = Color(0xFF0D9997)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                            // 4. City
                            OutlinedTextField(
                                value = city,
                                onValueChange = {
                                    city = it
                                    cityError = null
                                },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                isError = cityError != null,
                                supportingText = { if (cityError != null) Text(cityError!!, color = MaterialTheme.colorScheme.error) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0D9997),
                                    focusedLabelColor = Color(0xFF0D9997)
                                )
                            )

                            // 5. Pincode (Auto-Fix Logic)
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { input ->
                                    val sanitized = input.filter { it.isDigit() }
                                    pincode = sanitized.take(6)
                                    if (pincode.length == 6) pincodeError = null
                                },
                                label = { Text("Pincode") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = pincodeError != null,
                                supportingText = { if (pincodeError != null) Text(pincodeError!!, color = MaterialTheme.colorScheme.error) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0D9997),
                                    focusedLabelColor = Color(0xFF0D9997)
                                )
                            )
                        }
                    }
                }

                if (message != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        message!!,
                        color = if (message!!.contains("success", ignoreCase = true)) Green500 else Red500,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        // Validation
                        var isValid = true
                        if (fullName.isBlank()) { nameError = "Required"; isValid = false }
                        else if (!fullName.matches(Regex("^[a-zA-Z ]+$"))) { nameError = "Letters only"; isValid = false }
                        if (phone.length != 10) { phoneError = "10 digits required"; isValid = false }
                        if (address.isBlank()) { addressError = "Required"; isValid = false }
                        if (city.isBlank()) { cityError = "Required"; isValid = false }
                        if (pincode.length != 6) { pincodeError = "6 digits"; isValid = false }

                        if (!isValid) return@Button

                        scope.launch {
                            isSaving = true
                            message = null
                            try {
                                val response = RetrofitClient.apiService.updateUserProfile(
                                    UpdateUserProfileRequest(
                                        user_id = userId,
                                        full_name = fullName,
                                        phone = phone,
                                        address = address,
                                        city = city,
                                        pincode = pincode
                                    )
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    message = "Profile updated successfully!"
                                    navController.popBackStack()
                                } else {
                                    message = response.body()?.message ?: "Update failed"
                                }
                            } catch (e: Exception) {
                                message = "Error: ${e.message}"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997)),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- DELETE ACCOUNT BUTTON (NEW) ---
                TextButton(
                    onClick = { showDeleteDialog = true }, // Triggers the popup
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Red500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Red500)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}