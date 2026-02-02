package com.simats.profixai.ui.screens.provider

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderRegisterScreen(navController: NavController) {
    // --- Form State ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var selectedService by remember { mutableStateOf(1) }
    var hourlyRate by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }

    // --- Error State ---
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var hourlyRateError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var pincodeError by remember { mutableStateOf<String?>(null) }
    var aadhaarError by remember { mutableStateOf<String?>(null) }

    // --- UI State ---
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) } // For Service Dropdown

    // --- Password Strength State ---
    var passwordStrengthScore by remember { mutableFloatStateOf(0f) }
    var passwordStrengthLabel by remember { mutableStateOf("") }
    var passwordStrengthColor by remember { mutableStateOf(Color.Gray) }

    val scope = rememberCoroutineScope()

    val services = listOf(
        1 to "Cleaner",
        2 to "Electrician",
        3 to "Painter",
        4 to "Salon",
        5 to "Carpenter",
        6 to "Mechanic"
    )

    // --- Helper: Check Password Strength ---
    fun updatePasswordStrength(pass: String) {
        var score = 0
        if (pass.length >= 6) score++
        if (pass.any { it.isUpperCase() }) score++
        if (pass.any { !it.isLetterOrDigit() }) score++ // Checks for special char

        when (score) {
            0 -> {
                passwordStrengthScore = 0.1f
                passwordStrengthLabel = "Enter Password"
                passwordStrengthColor = Color.Gray
            }
            1 -> {
                passwordStrengthScore = 0.33f
                passwordStrengthLabel = "Weak"
                passwordStrengthColor = Color.Red
            }
            2 -> {
                passwordStrengthScore = 0.66f
                passwordStrengthLabel = "Medium"
                passwordStrengthColor = Color(0xFFFFA500) // Orange
            }
            3 -> {
                passwordStrengthScore = 1.0f
                passwordStrengthLabel = "Strong"
                passwordStrengthColor = Color(0xFF4CAF50) // Green
            }
        }
    }

    // --- Success Dialog ---
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(48.dp)) },
            title = { Text("Registration Submitted!", fontWeight = FontWeight.Bold) },
            text = { Text("Your registration is pending approval. You'll be able to login once an admin approves your account.") },
            confirmButton = {
                Button(
                    onClick = { navController.navigate("provider_login") { popUpTo("provider_register") { inclusive = true } } },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                ) { Text("Go to Login") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A2E))) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E))))
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text("Join as Provider", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Start earning today", fontSize = 16.sp, color = Gray400)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp)) {

            // 1. FULL NAME
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
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 2. EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("Email", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Gray400) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it, color = Red500) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 3. PHONE
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        phone = it
                        phoneError = null
                    }
                },
                label = { Text("Phone", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Gray400) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it, color = Red500) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 4. SERVICE DROPDOWN
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = services.find { it.first == selectedService }?.second ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Service Type", color = Gray400) },
                    leadingIcon = { Icon(Icons.Default.Category, null, tint = Gray400) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    services.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = { selectedService = id; expanded = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 5. HOURLY RATE & EXPERIENCE
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it; hourlyRateError = null },
                    label = { Text("Hourly Rate (₹)", color = Gray400) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hourlyRateError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = { Text("Experience (yrs)", color = Gray400) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
            }
            if (hourlyRateError != null) {
                Text(hourlyRateError!!, color = Red500, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 6. DESCRIPTION
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("About You (Optional)", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = Gray400) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 7. ADDRESS
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; addressError = null },
                label = { Text("Address", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Home, null, tint = Gray400) },
                modifier = Modifier.fillMaxWidth(),
                isError = addressError != null,
                supportingText = { addressError?.let { Text(it, color = Red500) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 8. CITY & PINCODE
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it; cityError = null },
                    label = { Text("City", color = Gray400) },
                    modifier = Modifier.weight(1f),
                    isError = cityError != null,
                    supportingText = { if (cityError != null) Text(cityError!!, color = Red500) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                Spacer(modifier = Modifier.width(12.dp))

                // PINCODE (Strict 6 digits)
                OutlinedTextField(
                    value = pincode,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            pincode = input
                            if (pincode.length == 6) pincodeError = null
                        }
                    },
                    label = { Text("Pincode", color = Gray400) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = pincodeError != null,
                    supportingText = { if (pincodeError != null) Text(pincodeError!!, color = Red500) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 9. AADHAAR (Strict 12 digits)
            OutlinedTextField(
                value = aadhaar,
                onValueChange = { input ->
                    // Logic: Digits only + Max 12 chars
                    if (input.length <= 12 && input.all { it.isDigit() }) {
                        aadhaar = input
                        aadhaarError = null
                    }
                },
                label = { Text("Aadhaar Number (Optional)", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = Gray400) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = aadhaarError != null,
                supportingText = { aadhaarError?.let { Text(it, color = Red500) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 10. PASSWORD (Single Line + Strength)
            OutlinedTextField(
                value = password,
                onValueChange = { input ->
                    if (!input.contains(" ") && !input.contains("\n")) {
                        password = input
                        passwordError = null
                        updatePasswordStrength(input)
                    }
                },
                label = { Text("Password", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Gray400) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = Gray400
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it, color = Red500) } },
                singleLine = true,
                maxLines = 1,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            // --- Password Strength Visual ---
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val animatedProgress by animateFloatAsState(targetValue = passwordStrengthScore, label = "Strength")
                val animatedColor by animateColorAsState(targetValue = passwordStrengthColor, label = "Color")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = animatedColor,
                        trackColor = Color.Gray.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = passwordStrengthLabel,
                        fontSize = 12.sp,
                        color = animatedColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (passwordStrengthScore < 1.0f) {
                    Text(
                        text = "Need 1 Uppercase & 1 Special Char (@#\$%)",
                        fontSize = 11.sp,
                        color = Gray400,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 11. CONFIRM PASSWORD
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { input ->
                    if (!input.contains(" ") && !input.contains("\n")) {
                        confirmPassword = input
                        confirmPasswordError = null
                    }
                },
                label = { Text("Confirm Password", color = Gray400) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Gray400) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = Gray400
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                isError = confirmPasswordError != null,
                supportingText = { confirmPasswordError?.let { Text(it, color = Red500) } },
                singleLine = true,
                maxLines = 1,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SUBMIT BUTTON
            Button(
                onClick = {
                    // --- VALIDATION LOGIC ---
                    var isValid = true

                    // Name
                    if (fullName.isBlank()) { nameError = "Required"; isValid = false }
                    else if (!fullName.matches(Regex("^[a-zA-Z ]+$"))) { nameError = "Letters only"; isValid = false }

                    // Email
                    val gmailRegex = "^[A-Za-z0-9._%+-]+@gmail\\.com$".toRegex()
                    if (email.isBlank()) { emailError = "Required"; isValid = false }
                    else if (!email.matches(gmailRegex)) { emailError = "Only @gmail.com addresses are allowed"; isValid = false }

                    // Phone
                    if (phone.length != 10) { phoneError = "10 digits required"; isValid = false }

                    // Address / City / Pincode
                    if (address.isBlank()) { addressError = "Required"; isValid = false }
                    if (city.isBlank()) { cityError = "Required"; isValid = false }
                    if (pincode.length != 6) { pincodeError = "6 digits"; isValid = false }

                    // Aadhaar (Optional but strict if entered)
                    if (aadhaar.isNotEmpty() && aadhaar.length != 12) {
                        aadhaarError = "Must be 12 digits"
                        isValid = false
                    }

                    // Rates
                    if (hourlyRate.isBlank()) { hourlyRateError = "Required"; isValid = false }

                    // Password Strict Checks
                    val hasUpperCase = password.any { it.isUpperCase() }
                    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

                    if (password.length < 6) {
                        passwordError = "Min 6 chars"; isValid = false
                    } else if (!hasUpperCase) {
                        passwordError = "Need 1 Capital Letter"; isValid = false
                    } else if (!hasSpecialChar) {
                        passwordError = "Need 1 Special Char"; isValid = false
                    }

                    if (password != confirmPassword) { confirmPasswordError = "Mismatch"; isValid = false }

                    if (!isValid) return@Button

                    // Submit
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.registerProvider(
                                ProviderRegisterRequest(
                                    full_name = fullName, email = email, phone = phone, password = password,
                                    service_id = selectedService, hourly_rate = hourlyRate.toDoubleOrNull() ?: 0.0,
                                    experience_years = experienceYears.toIntOrNull() ?: 0, description = description,
                                    address = address, city = city, pincode = pincode, aadhaar = aadhaar
                                )
                            )
                            if (response.isSuccessful && response.body()?.success == true) {
                                showSuccess = true
                            } else {
                                passwordError = response.body()?.message ?: "Registration failed"
                            }
                        } catch (e: Exception) {
                            passwordError = "Network error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), Color.White)
                else Text("Submit Registration", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already registered? ", color = Gray400)
                Text("Sign In", color = Blue500, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { navController.popBackStack() })
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper for consistent Dark Mode colors
@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Blue500,
    unfocusedBorderColor = Color(0xFF3A3A4E),
    focusedContainerColor = Color(0xFF2A2A3E),
    unfocusedContainerColor = Color(0xFF2A2A3E),
    errorBorderColor = Red500,
    errorLabelColor = Red500
)