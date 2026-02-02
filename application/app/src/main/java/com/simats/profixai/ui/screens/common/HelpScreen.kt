package com.simats.profixai.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController, isProvider: Boolean = false) {
    val primaryColor = if (isProvider) MechanicColor else Blue600
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Gray50)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // FAQ Section
            Text(
                "Frequently Asked Questions",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isProvider) {
                // Provider FAQs
                FaqCard("How do I accept a booking?", "When you receive a new booking request, go to the Bookings tab and tap 'Accept' on the pending booking. You can also reject if you're unavailable.")
                FaqCard("How do I update my availability?", "Go to Dashboard → My Schedule to set your available dates and working hours.")
                FaqCard("When do I get paid?", "Payments are processed after the customer marks the job as complete. You'll receive the amount directly to your registered account.")
                FaqCard("How can I improve my rating?", "Provide excellent service, arrive on time, communicate clearly with customers, and complete jobs professionally.")
                FaqCard("How do I upload my work portfolio?", "Go to Profile → My Portfolio to upload images of your previous work. This helps customers see your expertise.")
            } else {
                // User FAQs
                FaqCard("How do I book a service?", "Browse services on the home screen, select a provider, choose your preferred date and time, and confirm your booking.")
                FaqCard("Can I cancel a booking?", "You can cancel a booking before the provider accepts it. Once accepted, please contact the provider directly.")
                FaqCard("How are providers verified?", "All providers undergo verification including identity check and skill assessment before being approved on our platform.")
                FaqCard("Is my payment secure?", "Yes, all payments are processed securely. You only pay after confirming the service details.")
                FaqCard("How do I leave a review?", "After your service is completed, you'll be prompted to rate and review the provider. Your feedback helps other users!")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Contact Section
            Text(
                "Contact Us",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ContactRow(Icons.Default.Email, "Email", "support@profixai.com")
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray200)
                    ContactRow(Icons.Default.Phone, "Phone", "+91 1800-123-4567")
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray200)
                    ContactRow(Icons.Default.Schedule, "Hours", "Mon-Sat, 9AM - 6PM")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tips Section
            Text(
                "Quick Tips",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isProvider) {
                        TipRow("Keep your profile updated with recent work")
                        TipRow("Respond to booking requests promptly")
                        TipRow("Set accurate availability to avoid conflicts")
                        TipRow("Maintain professional communication")
                    } else {
                        TipRow("Check provider ratings before booking")
                        TipRow("Provide clear service requirements")
                        TipRow("Book in advance for guaranteed availability")
                        TipRow("Leave reviews to help other users")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaqCard(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    question,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray900
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Gray500
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    answer,
                    fontSize = 14.sp,
                    color = Gray600,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = Blue600)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Gray500)
            Text(value, fontSize = 16.sp, color = Gray900)
        }
    }
}

@Composable
private fun TipRow(tip: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            modifier = Modifier.size(18.dp),
            tint = Green500
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(tip, fontSize = 14.sp, color = Gray700)
    }
}
