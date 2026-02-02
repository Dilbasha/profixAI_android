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
fun TermsOfServiceScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
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
                "Terms of Service",
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
            
            LegalSection(
                title = "1. Acceptance of Terms",
                content = "By accessing and using ProFIX AI, you accept and agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use our services.\n\nThese terms apply to all users of the platform, including customers seeking services and service providers offering their expertise."
            )
            
            LegalSection(
                title = "2. Description of Service",
                content = "ProFIX AI is an online platform that connects customers with verified service providers for home services including but not limited to cleaning, electrical work, painting, salon services, carpentry, and mechanical repairs.\n\nWe act as an intermediary and are not directly responsible for the quality of work performed by service providers."
            )
            
            LegalSection(
                title = "3. User Registration",
                content = "To use our services, you must:\n• Be at least 18 years of age\n• Provide accurate and complete registration information\n• Maintain the security of your account credentials\n• Notify us immediately of any unauthorized use\n\nWe reserve the right to suspend or terminate accounts that violate these terms."
            )
            
            LegalSection(
                title = "4. Service Provider Obligations",
                content = "Service providers on our platform agree to:\n• Maintain valid licenses and certifications as required\n• Provide accurate information about their skills and experience\n• Arrive on time for scheduled appointments\n• Perform services professionally and ethically\n• Maintain appropriate insurance coverage"
            )
            
            LegalSection(
                title = "5. Customer Obligations",
                content = "Customers using our platform agree to:\n• Provide accurate service requirements\n• Be present or available during scheduled service times\n• Pay for services as agreed\n• Treat service providers with respect\n• Provide honest and fair reviews"
            )
            
            LegalSection(
                title = "6. Booking and Payments",
                content = "All bookings made through ProFIX AI are subject to provider availability. Payments are processed securely through our platform. The agreed-upon amount will be charged after service completion.\n\nService providers receive payment after the customer confirms service completion, minus applicable platform fees."
            )
            
            LegalSection(
                title = "7. Limitation of Liability",
                content = "ProFIX AI is not liable for:\n• Quality of work performed by service providers\n• Damage to property during service delivery\n• Personal injury during service delivery\n• Disputes between customers and service providers\n\nOur maximum liability is limited to the amount paid for the specific service in question."
            )
            
            LegalSection(
                title = "8. Modifications",
                content = "We reserve the right to modify these terms at any time. Changes will be effective immediately upon posting. Continued use of the platform after changes constitutes acceptance of the modified terms."
            )
            
            LegalSection(
                title = "9. Contact Information",
                content = "For questions about these Terms of Service, please contact us at:\n\nEmail: legal@profixai.com\nPhone: +91 1800-123-4567"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LegalSection(title: String, content: String) {
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
