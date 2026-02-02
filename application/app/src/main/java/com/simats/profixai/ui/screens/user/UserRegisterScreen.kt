package com.simats.profixai.ui.screens.user

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.RetrofitClient
import com.simats.profixai.network.UserRegisterRequest
import com.simats.profixai.ui.theme.* // Ensure your theme colors are imported
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegisterScreen(navController: NavController) {
    // --- Form State ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    // --- Error State ---
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var pincodeError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }

    // --- UI State ---
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // --- Password Strength State ---
    var passwordStrengthScore by remember { mutableFloatStateOf(0f) }
    var passwordStrengthLabel by remember { mutableStateOf("") }
    var passwordStrengthColor by remember { mutableStateOf(Color.Gray) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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

    // --- Date Picker Logic ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis

                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            val selectedYear = calendar.get(Calendar.YEAR)

                            if (selectedYear > currentYear) {
                                dobError = "Date cannot be in the future"
                            } else if (currentYear - selectedYear < 13) {
                                dobError = "You must be at least 13 years old"
                            } else {
                                dob = formatter.format(Date(millis))
                                dobError = null
                            }
                        }
                    },
                    enabled = confirmEnabled.value
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D9997), Color(0xFF3B82F6))
                        )
                    )
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Join ServiceConnect today", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            // Form
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {

                // 1. FULL NAME
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        if (it.matches(Regex("^[a-zA-Z ]*$"))) {
                            fullName = it
                            nameError = null
                        }
                    },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. PHONE
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phone = it
                            phoneError = null
                        }
                    },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. DOB
                OutlinedTextField(
                    value = dob,
                    onValueChange = { },
                    label = { Text("Date of Birth") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = if (dobError != null) MaterialTheme.colorScheme.error else Color.Gray,
                        disabledLabelColor = Color.Black,
                        disabledLeadingIconColor = Color.Black
                    ),
                    isError = dobError != null,
                    supportingText = { dobError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. ADDRESS
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; addressError = null },
                    label = { Text("Address") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = addressError != null,
                    supportingText = { addressError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 6. CITY & PINCODE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it; cityError = null },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        isError = cityError != null,
                        supportingText = { if (cityError != null) Text(cityError!!, color = MaterialTheme.colorScheme.error) },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() }
                            if(sanitized.length <= 6) {
                                pincode = sanitized
                                pincodeError = null
                            }
                        },
                        label = { Text("Pincode") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        isError = pincodeError != null,
                        supportingText = { if (pincodeError != null) Text(pincodeError!!, color = MaterialTheme.colorScheme.error) },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 7. PASSWORD (Single line + Strength Logic)
                OutlinedTextField(
                    value = password,
                    onValueChange = { input ->
                        if (!input.contains(" ") && !input.contains("\n")) { // Blocks Space & Enter
                            password = input
                            passwordError = null
                            updatePasswordStrength(input) // Update strength live
                        }
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true, // Force single line
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                // --- Password Strength Visual ---
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
                            progress = animatedProgress, // <--- FIXED: No curly braces {}
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp),
                            color = animatedColor,
                            trackColor = Color.LightGray.copy(alpha = 0.5f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
                            text = "Must contain 1 Uppercase & 1 Special Char (@#\$%)",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 8. CONFIRM PASSWORD
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { input ->
                        if (!input.contains(" ") && !input.contains("\n")) {
                            confirmPassword = input
                            confirmPasswordError = null
                        }
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide" else "Show"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done // Final action
                    ),
                    isError = confirmPasswordError != null,
                    supportingText = { confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // SUBMIT BUTTON
                Button(
                    onClick = {
                        var isValid = true

                        // 1. Name Check
                        if (fullName.isBlank()) { nameError = "Name is required"; isValid = false }
                        else if (!fullName.matches(Regex("^[a-zA-Z ]+$"))) { nameError = "Letters only"; isValid = false }

                        // 2. Email Check
                        val gmailRegex = "^[A-Za-z0-9._%+-]+@gmail\\.com$".toRegex()
                        if (email.isBlank()) { emailError = "Required"; isValid = false }
                        else if (!email.matches(gmailRegex)) { emailError = "Only @gmail.com allowed"; isValid = false }

                        // 3. Phone Check
                        if (phone.length != 10) { phoneError = "10 digits required"; isValid = false }

                        // 4. DOB Check
                        if (dob.isBlank()) { dobError = "Required"; isValid = false }

                        // 5. Address Checks
                        if (address.isBlank()) { addressError = "Required"; isValid = false }
                        if (city.isBlank()) { cityError = "Required"; isValid = false }
                        if (pincode.length != 6) { pincodeError = "6 digits"; isValid = false }

                        // 6. Password Checks (Enhanced)
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

                        isLoading = true

                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.registerUser(
                                    UserRegisterRequest(
                                        full_name = fullName,
                                        email = email,
                                        phone = phone,
                                        password = password,
                                        address = address,
                                        city = city,
                                        pincode = pincode,
                                        dob = dob
                                    )
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    navController.navigate("user_login") {
                                        popUpTo("user_register") { inclusive = true }
                                    }
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997)),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ", color = Gray600)
                    Text(
                        text = "Sign In",
                        color = Color(0xFF0D9997),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}