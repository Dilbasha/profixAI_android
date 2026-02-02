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
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
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
                "Privacy Policy",
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
            
            PrivacySection(
                title = "1. Information We Collect",
                content = "We collect information you provide directly to us, including:\n\n• Personal Information: Name, email address, phone number, and address\n• Identity Verification: Aadhaar number (for service providers)\n• Payment Information: Transaction details and payment method information\n• Profile Information: Photos, skills, experience, and service descriptions\n• Usage Data: How you interact with our platform"
            )
            
            PrivacySection(
                title = "2. How We Use Your Information",
                content = "We use the information we collect to:\n\n• Provide, maintain, and improve our services\n• Connect customers with appropriate service providers\n• Process transactions and send related information\n• Send notifications about bookings and updates\n• Verify service provider identities and qualifications\n• Respond to customer support requests\n• Detect and prevent fraud or abuse"
            )
            
            PrivacySection(
                title = "3. Information Sharing",
                content = "We may share your information with:\n\n• Service Providers/Customers: Contact details necessary for service delivery\n• Payment Processors: For secure payment processing\n• Legal Authorities: When required by law or to protect rights\n• Business Partners: With your consent, for promotional purposes\n\nWe do not sell your personal information to third parties."
            )
            
            PrivacySection(
                title = "4. Data Security",
                content = "We implement appropriate security measures to protect your personal information, including:\n\n• Encryption of sensitive data in transit and at rest\n• Regular security assessments and audits\n• Access controls and authentication requirements\n• Secure data storage practices\n\nHowever, no method of transmission over the Internet is 100% secure."
            )
            
            PrivacySection(
                title = "5. Data Retention",
                content = "We retain your personal information for as long as:\n\n• Your account is active\n• Needed to provide you services\n• Required by law or for legitimate business purposes\n\nYou may request deletion of your account and personal data at any time."
            )
            
            PrivacySection(
                title = "6. Your Rights",
                content = "You have the right to:\n\n• Access your personal information\n• Correct inaccurate data\n• Request deletion of your data\n• Opt-out of marketing communications\n• Export your data in a portable format\n\nContact us to exercise any of these rights."
            )
            
            PrivacySection(
                title = "7. Cookies and Tracking",
                content = "We use cookies and similar technologies to:\n\n• Remember your preferences\n• Analyze usage patterns\n• Improve user experience\n• Provide personalized content\n\nYou can control cookie settings through your device or browser."
            )
            
            PrivacySection(
                title = "8. Children's Privacy",
                content = "Our services are not intended for individuals under 18 years of age. We do not knowingly collect personal information from children. If we become aware of such collection, we will take steps to delete the information."
            )
            
            PrivacySection(
                title = "9. Changes to This Policy",
                content = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new policy on this page and updating the \"Last Updated\" date."
            )
            
            PrivacySection(
                title = "10. Contact Us",
                content = "If you have questions about this Privacy Policy, please contact us at:\n\nEmail: privacy@profixai.com\nPhone: +91 1800-123-4567\nAddress: ProFIX AI Technologies Pvt. Ltd., Bangalore, India"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
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
