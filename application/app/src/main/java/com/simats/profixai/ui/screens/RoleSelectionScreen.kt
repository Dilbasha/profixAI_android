package com.simats.profixai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(navController: NavController) {
    val tealColor = Color(0xFF0D9997)
    val blueColor = Color(0xFF3B82F6)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(tealColor, blueColor)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // App Logo - Modern service icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HomeRepairService,
                    contentDescription = "Logo",
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "ProFIX AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your trusted service partner",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // User Card
            RoleCard(
                title = "I need a service",
                subtitle = "Find cleaners, electricians, painters & more",
                icon = Icons.Default.Search,
                iconBgColor = tealColor,
                onClick = { navController.navigate("user_login") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Provider Card
            RoleCard(
                title = "I provide services",
                subtitle = "Join as a service provider",
                icon = Icons.Default.Handyman,
                iconBgColor = blueColor,
                onClick = { navController.navigate("provider_login") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Admin Card
            RoleCard(
                title = "Admin Portal",
                subtitle = "Manage the platform",
                icon = Icons.Default.AdminPanelSettings,
                iconBgColor = Color(0xFF7C3AED),
                onClick = { navController.navigate("admin_login") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "By continuing, you agree to our Terms of Service",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(26.dp),
                    tint = iconBgColor
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Gray600
                )
            }
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBgColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go",
                    tint = iconBgColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
