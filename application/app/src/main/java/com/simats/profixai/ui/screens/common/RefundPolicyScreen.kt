package com.simats.profixai.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refund Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue600,
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
                .padding(16.dp)
        ) {
            Text(
                "Refund Policy",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            
            Text(
                "Last Updated: January 1, 2026",
                fontSize = 12.sp,
                color = Gray500,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            
            // Important Notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blue600.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Customer Satisfaction Guarantee",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "At ProFIX AI, we strive to ensure complete customer satisfaction. If you're not satisfied with the service provided, we're here to help resolve the issue.",
                        fontSize = 14.sp,
                        color = Gray700,
                        lineHeight = 22.sp
                    )
                }
            }
            
            RefundSection(
                title = "1. Eligibility for Refund",
                content = "You may be eligible for a refund if:\n\n• The service provider did not show up for the scheduled appointment\n• The service was significantly different from what was booked\n• The work quality was unsatisfactory and the provider refuses to rectify\n• You cancelled the booking before the provider accepted it\n• Technical issues on our platform prevented service delivery"
            )
            
            RefundSection(
                title = "2. Non-Refundable Situations",
                content = "Refunds will not be provided in the following cases:\n\n• Customer was not available at the scheduled time\n• Cancellation after the provider has started traveling to the location\n• Minor dissatisfaction with subjective aspects of service\n• Services completed as described but expectations not met\n• Customer provided incorrect address or contact information"
            )
            
            RefundSection(
                title = "3. Refund Request Process",
                content = "To request a refund:\n\n1. Open the app and go to 'My Bookings'\n2. Select the relevant booking\n3. Tap 'Report Issue' or 'Request Refund'\n4. Provide detailed description of the issue\n5. Include photos if applicable\n6. Submit the request within 48 hours of service completion\n\nOur support team will review your request within 2-3 business days."
            )
            
            RefundSection(
                title = "4. Refund Amount",
                content = "Refund amounts are determined based on:\n\n• Full Refund: Service not provided at all\n• Partial Refund: Service partially completed or quality issues\n• No Refund: Service completed satisfactorily\n\nThe platform fee may or may not be refundable depending on the circumstances."
            )
            
            RefundSection(
                title = "5. Refund Timeline",
                content = "Once approved, refunds will be processed as follows:\n\n• Original Payment Method: 5-7 business days\n• App Wallet Credit: Instant\n• Bank Transfer: 7-10 business days\n\nProcessing times may vary based on your bank or payment provider."
            )
            
            RefundSection(
                title = "6. Disputes",
                content = "If you disagree with a refund decision:\n\n• You can appeal within 7 days of the decision\n• Provide additional evidence or documentation\n• Our escalation team will review the case\n• Final decisions are made within 5 business days\n\nWe aim to be fair to both customers and service providers."
            )
            
            RefundSection(
                title = "7. Service Provider Refunds",
                content = "For service providers:\n\n• If a customer cancels after you've started traveling, you may receive a cancellation fee\n• Disputes about withheld payments can be raised within 7 days\n• Platform fees are non-refundable for completed services"
            )
            
            RefundSection(
                title = "8. Contact Support",
                content = "For refund-related queries, contact us at:\n\nEmail: support@profixai.com\nPhone: +91 1800-123-4567\nIn-App: Profile → Help & Support → Contact Us\n\nSupport Hours: Mon-Sat, 9 AM - 6 PM IST"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RefundSection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                content,
                fontSize = 14.sp,
                color = Gray700,
                lineHeight = 22.sp
            )
        }
    }
}
