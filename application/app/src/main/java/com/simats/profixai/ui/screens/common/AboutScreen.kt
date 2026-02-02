package com.simats.profixai.ui.screens.common

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
        ) {
            // Header with Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Blue600, Blue700)
                        )
                    )
                    .padding(40.dp),
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
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "Logo",
                            modifier = Modifier.size(44.dp),
                            tint = Blue600
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "ProFIX AI",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        "Version 1.0.0",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About Description
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Our Mission",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "ProFIX AI connects you with trusted, verified service professionals in your area. Whether you need a cleaner, electrician, painter, or any other home service, we make it easy to find, book, and manage professional services.",
                        fontSize = 14.sp,
                        color = Gray600,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "We leverage AI technology to match you with the best service providers based on your needs, location, and preferences. Our platform ensures quality service through verified professionals and transparent reviews.",
                        fontSize = 14.sp,
                        color = Gray600,
                        lineHeight = 24.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Features
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Why Choose Us",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FeatureRow(Icons.Default.VerifiedUser, "Verified Professionals", "All providers undergo thorough verification")
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureRow(Icons.Default.Security, "Secure Payments", "Safe and encrypted payment processing")
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureRow(Icons.Default.Support, "24/7 Support", "Round-the-clock customer assistance")
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureRow(Icons.Default.Star, "Quality Guarantee", "Satisfaction guaranteed on every service")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legal Links
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Legal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LegalRow("Terms of Service") { navController.navigate("terms_of_service") }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Gray200)
                    LegalRow("Privacy Policy") { navController.navigate("privacy_policy") }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Gray200)
                    LegalRow("Refund Policy") { navController.navigate("refund_policy") }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Copyright
            Text(
                "© 2026 ProFIX AI. All rights reserved.",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = Gray500,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Made with ❤️ in India",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = Gray400,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Blue600.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = Blue600)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Gray900)
            Text(description, fontSize = 13.sp, color = Gray600)
        }
    }
}

@Composable
private fun LegalRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = Gray700)
        Icon(Icons.Default.ChevronRight, null, tint = Gray400)
    }
}
