package com.simats.profixai.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.AiChatRequest
import com.simats.profixai.network.RetrofitClient
import kotlinx.coroutines.launch

data class ProviderChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderAiChatScreen(navController: NavController, providerId: Int) {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        ProviderChatMessage(
            "Hello! I'm ProFIX AI Assistant for service providers. I can help you with tips for getting more bookings, pricing strategies, customer service best practices, and growing your business. How can I assist you today?",
            isUser = false
        )
    )) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    val darkGreen = Color(0xFF1B5E20)
    val lightGreen = Color(0xFF4CAF50)
    
    // Scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ProFIX AI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Business assistant", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ProviderChatBubble(message = message, accentColor = lightGreen)
                }
                
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = lightGreen,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Thinking...", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            // Input area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Ask for business tips...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = lightGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank() && !isLoading) {
                                val userMessage = messageText.trim()
                                messageText = ""
                                messages = messages + ProviderChatMessage(userMessage, isUser = true)
                                isLoading = true
                                
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.sendAiChat(
                                            AiChatRequest(
                                                message = userMessage, 
                                                user_type = "provider",
                                                provider_id = providerId
                                            )
                                        )
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            val aiResponse = response.body()?.response ?: "Sorry, I couldn't process that."
                                            messages = messages + ProviderChatMessage(aiResponse, isUser = false)
                                        } else {
                                            messages = messages + ProviderChatMessage(
                                                response.body()?.message ?: "Sorry, something went wrong. Please try again.",
                                                isUser = false
                                            )
                                        }
                                    } catch (e: Exception) {
                                        messages = messages + ProviderChatMessage(
                                            "Error: ${e.message ?: "Unknown error"}. Make sure ai_chat.php is uploaded to your server.",
                                            isUser = false
                                        )
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        containerColor = lightGreen,
                        contentColor = Color.White,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderChatBubble(message: ProviderChatMessage, accentColor: Color) {
    val alignment = if (message.isUser) Arrangement.End else Arrangement.Start
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) accentColor else Color.White
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
        
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B5E20)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Handyman,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
