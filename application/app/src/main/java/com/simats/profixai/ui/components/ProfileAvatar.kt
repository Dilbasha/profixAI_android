package com.simats.profixai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.profixai.network.RetrofitClient
import com.simats.profixai.ui.theme.Blue600

/**
 * Reusable avatar component that shows profile image if available,
 * otherwise shows initials in a colored circle
 */
@Composable
fun ProfileAvatar(
    imageUrl: String?,
    name: String,
    size: Dp = 50.dp,
    fontSize: TextUnit = 16.sp,
    backgroundColor: Color = Blue600.copy(alpha = 0.1f),
    textColor: Color = Blue600,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrEmpty()) {
        AsyncImage(
            model = "${RetrofitClient.BASE_URL}$imageUrl",
            contentDescription = name,
            modifier = modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
