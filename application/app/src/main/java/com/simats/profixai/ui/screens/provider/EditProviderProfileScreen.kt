package com.simats.profixai.ui.screens.provider

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
fun EditProviderProfileScreen(navController: NavController, providerId: Int) {
    // --- Data State ---
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }

    // --- Error State ---
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var hourlyRateError by remember { mutableStateOf<String?>(null) }
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
    LaunchedEffect(providerId) {
        try {
            val response = RetrofitClient.apiService.getProviderProfile(ProviderIdRequest(provider_id = providerId))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.provider?.let { provider ->
                    fullName = provider.full_name
                    phone = provider.phone
                    hourlyRate = provider.hourly_rate.toInt().toString()
                    experienceYears = provider.experience_years.toString()
                    description = provider.description ?: ""
                    address = provider.address ?: ""
                    city = provider.city ?: ""
                    pincode = provider.pincode ?: ""
                    isAvailable = provider.is_available == 1
                }
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = Red500) },
            text = {
                Text("Are you sure? This will permanently delete your provider profile, bookings, reviews, and earnings history. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeletingAccount = true
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.deleteProviderAccount(ProviderIdRequest(provider_id = providerId))
                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_LONG).show()
                                    navController.navigate("role_selection") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, response.body()?.message ?: "Delete failed", Toast.LENGTH_SHORT).show()
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
                    containerColor = Color(0xFF2A2A3E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator(color = MechanicColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF1A1A2E))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Personal Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Full Name (Letters Only)
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                if (it.matches(Regex("^[a-zA-Z ]*$"))) {
                                    fullName = it
                                    nameError = null
                                }
                            },
                            label = { Text("Full Name", color = Gray400) },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it, color = Red500) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = providerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone (10 Digits)
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    phone = it
                                    phoneError = null
                                }
                            },
                            label = { Text("Phone Number", color = Gray400) },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = phoneError != null,
                            supportingText = { phoneError?.let { Text(it, color = Red500) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = providerTextFieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Service Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Service Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                            // Hourly Rate
                            OutlinedTextField(
                                value = hourlyRate,
                                onValueChange = {
                                    hourlyRate = it.filter { c -> c.isDigit() }
                                    hourlyRateError = null
                                },
                                label = { Text("Rate (₹/hr)", color = Gray400) },
                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null, tint = Gray400) },
                                modifier = Modifier.weight(1f),
                                isError = hourlyRateError != null,
                                supportingText = { if(hourlyRateError != null) Text(hourlyRateError!!, color = Red500) },
                                shape = RoundedCornerShape(12.dp),
                                colors = providerTextFieldColors()
                            )

                            // Experience
                            OutlinedTextField(
                                value = experienceYears,
                                onValueChange = { experienceYears = it.filter { c -> c.isDigit() } },
                                label = { Text("Exp (years)", color = Gray400) },
                                leadingIcon = { Icon(Icons.Default.WorkHistory, null, tint = Gray400) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = providerTextFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("About You", color = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            colors = providerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Availability Toggle
                        Row(
                            Modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Availability", fontWeight = FontWeight.Medium, color = Color.White)
                                Text(
                                    if (isAvailable) "You're available for bookings" else "You're currently offline",
                                    fontSize = 12.sp,
                                    color = if (isAvailable) Green500 else Gray400
                                )
                            }
                            Switch(
                                checked = isAvailable,
                                onCheckedChange = { isAvailable = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Green500
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Address Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Location", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Address
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; addressError = null },
                            label = { Text("Address", color = Gray400) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = addressError != null,
                            supportingText = { addressError?.let { Text(it, color = Red500) } },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            colors = providerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                            // City
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it; cityError = null },
                                label = { Text("City", color = Gray400) },
                                modifier = Modifier.weight(1f),
                                isError = cityError != null,
                                supportingText = { if(cityError != null) Text(cityError!!, color = Red500) },
                                shape = RoundedCornerShape(12.dp),
                                colors = providerTextFieldColors()
                            )

                            // Pincode (Auto-Fix Logic)
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { input ->
                                    val sanitized = input.filter { it.isDigit() }
                                    pincode = sanitized.take(6)
                                    if (pincode.length == 6) pincodeError = null
                                },
                                label = { Text("Pincode", color = Gray400) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = pincodeError != null,
                                supportingText = { if(pincodeError != null) Text(pincodeError!!, color = Red500) },
                                shape = RoundedCornerShape(12.dp),
                                colors = providerTextFieldColors()
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
                        // --- VALIDATION LOGIC ---
                        var isValid = true

                        if (fullName.isBlank()) { nameError = "Required"; isValid = false }
                        else if (!fullName.matches(Regex("^[a-zA-Z ]+$"))) { nameError = "Letters only"; isValid = false }

                        if (phone.length != 10) { phoneError = "10 digits required"; isValid = false }

                        if (hourlyRate.isBlank()) { hourlyRateError = "Required"; isValid = false }

                        if (address.isBlank()) { addressError = "Required"; isValid = false }
                        if (city.isBlank()) { cityError = "Required"; isValid = false }
                        if (pincode.length != 6) { pincodeError = "6 digits"; isValid = false }

                        if (!isValid) return@Button

                        scope.launch {
                            isSaving = true
                            message = null
                            try {
                                val response = RetrofitClient.apiService.updateProviderProfile(
                                    UpdateProviderProfileRequest(
                                        provider_id = providerId,
                                        full_name = fullName,
                                        phone = phone,
                                        hourly_rate = hourlyRate.toDoubleOrNull() ?: 0.0,
                                        experience_years = experienceYears.toIntOrNull() ?: 0,
                                        description = description,
                                        address = address,
                                        city = city,
                                        pincode = pincode,
                                        is_available = if (isAvailable) 1 else 0
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
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
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
                    onClick = { showDeleteDialog = true },
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

// Helper to keep colors consistent
@Composable
fun providerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Blue500,
    unfocusedBorderColor = Color(0xFF3A3A4E),
    focusedLabelColor = Blue500,
    focusedContainerColor = Color(0xFF2A2A3E),
    unfocusedContainerColor = Color(0xFF2A2A3E),
    errorBorderColor = Red500,
    errorLabelColor = Red500
)