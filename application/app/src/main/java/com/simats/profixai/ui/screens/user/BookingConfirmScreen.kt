package com.simats.profixai.ui.screens.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmScreen(
    navController: NavController,
    userId: Int,
    providerId: Int
) {
    var provider by remember { mutableStateOf<Provider?>(null) }
    var bookingDate by remember { mutableStateOf("") }
    var bookingTime by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var estimatedHours by remember { mutableStateOf("1") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()
    
    // Date Picker Dialog
    // --- UPDATED DATE PICKER WITH AVAILABILITY CHECK ---
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // 1. Format the date the user picked
            val selectedDateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            // 2. Show loading (optional, but good UX)
            isLoading = true

            // 3. Call API to check this specific month
            scope.launch {
                try {
                    val response = RetrofitClient.apiService.getProviderAvailability(
                        GetAvailabilityRequest(
                            provider_id = providerId,
                            year = year,
                            month = month + 1 // Calendar is 0-11, API needs 1-12
                        )
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        val availabilityList = response.body()?.availability ?: emptyList()

                        // 4. CHECK: Is this specific date marked "unavailable"?
                        val isUnavailable = availabilityList.any {
                            it.date == selectedDateStr && it.status == "unavailable"
                        }

                        if (isUnavailable) {
                            // BLOCK THE SELECTION
                            android.widget.Toast.makeText(
                                context,
                                "Provider is unavailable on $selectedDateStr. Please choose another date.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            bookingDate = "" // Clear date so they can't submit
                        } else {
                            // ACCEPT THE SELECTION
                            bookingDate = selectedDateStr
                        }
                    } else {
                        // If API fails to return list, we usually assume available or show error
                        bookingDate = selectedDateStr
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Network error checking availability", android.widget.Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }
    
    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            bookingTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )
    
    LaunchedEffect(providerId) {
        try {
            val response = RetrofitClient.apiService.getProviderDetails(
                ProviderIdRequest(provider_id = providerId)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                provider = response.body()?.provider
            }
        } catch (e: Exception) { }
    }
    
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Green500, modifier = Modifier.size(48.dp)) },
            title = { Text("Booking Confirmed!", fontWeight = FontWeight.Bold) },
            text = { Text("Your booking has been submitted successfully. The provider will contact you soon.") },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("user_home/$userId") {
                            popUpTo("user_home/$userId") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997))
                ) { Text("Go to Home") }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking") },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Gray50)
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                // Provider Info Card
                if (provider != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileAvatar(
                                imageUrl = provider!!.profile_image,
                                name = provider!!.full_name,
                                size = 56.dp,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider!!.full_name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Text(provider!!.service_name ?: "", fontSize = 14.sp, color = Gray600)
                            }
                            Text(
                                "₹${provider!!.hourly_rate.toInt()}/hr",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9997)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Booking Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date Picker
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = Color(0xFF0D9997))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Select Date", fontSize = 12.sp, color = Gray500)
                            Text(
                                if (bookingDate.isBlank()) "Tap to select" else bookingDate,
                                fontSize = 16.sp,
                                color = if (bookingDate.isBlank()) Gray400 else Gray900
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Gray400)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Time Picker
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = Color(0xFF0D9997))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Select Time", fontSize = 12.sp, color = Gray500)
                            Text(
                                if (bookingTime.isBlank()) "Tap to select" else bookingTime,
                                fontSize = 16.sp,
                                color = if (bookingTime.isBlank()) Gray400 else Gray900
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Gray400)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Hours selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, null, tint = Color(0xFF0D9997))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estimated Hours", fontSize = 12.sp, color = Gray500)
                        }
                        // Hour selector buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { 
                                    val current = estimatedHours.toIntOrNull() ?: 1
                                    if (current > 1) estimatedHours = (current - 1).toString()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, null, tint = Color(0xFF0D9997))
                            }
                            Text(
                                estimatedHours,
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            IconButton(
                                onClick = { 
                                    val current = estimatedHours.toIntOrNull() ?: 1
                                    if (current < 12) estimatedHours = (current + 1).toString()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color(0xFF0D9997))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Service Location", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0D9997)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9997)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { input ->
                            // Logic: Allow update ONLY if digits and max 6 chars
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                pincode = input
                            }
                        },
                        label = { Text("Pincode") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9997)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0D9997)
                    )
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = Red500, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Total & Confirm
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    val hours = estimatedHours.toIntOrNull() ?: 1
                    val total = (provider?.hourly_rate ?: 0.0) * hours
                    
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Estimated Total", fontSize = 16.sp, color = Gray600)
                        Text("₹${total.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9997))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            when {
                                bookingDate.isBlank() -> errorMessage = "Please select booking date"
                                bookingTime.isBlank() -> errorMessage = "Please select booking time"
                                address.isBlank() -> errorMessage = "Please enter service address"
                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.apiService.createBooking(
                                                CreateBookingRequest(
                                                    user_id = userId,
                                                    provider_id = providerId,
                                                    booking_date = bookingDate,
                                                    booking_time = bookingTime,
                                                    address = address,
                                                    city = city,
                                                    pincode = pincode,
                                                    description = description,
                                                    estimated_hours = hours
                                                )
                                            )
                                            if (response.isSuccessful && response.body()?.success == true) {
                                                showSuccess = true
                                            } else {
                                                errorMessage = response.body()?.message ?: "Booking failed"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Network error: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
