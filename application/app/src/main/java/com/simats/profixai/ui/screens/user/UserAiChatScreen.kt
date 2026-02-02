package com.simats.profixai.ui.screens.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.ChatClient
import com.simats.profixai.network.ChatRequest
import kotlinx.coroutines.launch

// Simple message model
data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAiChatScreen(navController: NavController, userId: Int) {
    val scope = rememberCoroutineScope()

    // 1. Chat State
    var messages by remember { mutableStateOf(listOf(
        ChatMessage("Hi! I'm ProFix AI. Ask me for a Cleaner, Electrician, or Mechanic.", false)
    )) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // 2. SERVICE MAPPING
    // These keys match the "reply" your Python code returns
    val serviceMap = mapOf(
        "Cleaner" to Pair(1, "Cleaner"),
        "Electrician" to Pair(2, "Electrician"),
        "Painter" to Pair(3, "Painter"),
        "Salon" to Pair(4, "Salon"),
        "Carpenter" to Pair(5, "Carpenter"),
        "Mechanic" to Pair(6, "Mechanic")
        // Note: Your Python code didn't have Plumber in the list, add it there if needed.
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ProFix AI Support", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D9997))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            // --- Chat History ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isLoading) {
                    item {
                        Text(
                            text = "ProFix AI is thinking...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            // --- Input Area ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type 'Fan broken' or 'Need paint'...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D9997),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    maxLines = 1,
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            val userText = inputText
                            messages = messages + ChatMessage(userText, true)
                            inputText = ""
                            isLoading = true

                            // 3. NETWORK CALL TO RENDER
                            scope.launch {
                                try {
                                    // Call your Render API
                                    val response = ChatClient.api.getBotResponse(ChatRequest(userText))

                                    // Get the "reply" string (e.g., "Electrician")
                                    val botReply = response.reply.trim()

                                    isLoading = false

                                    // Check valid service or error message
                                    handleAiAction(botReply, serviceMap, navController, userId) { replyText ->
                                        messages = messages + ChatMessage(replyText, false)
                                    }

                                } catch (e: Exception) {
                                    isLoading = false
                                    Log.e("ChatError", "Error: ${e.message}")
                                    messages = messages + ChatMessage("Connection error. Please check your internet.", false)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D9997)),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Send, "Send", tint = Color.White)
                }
            }
        }
    }
}

// 4. LOGIC HANDLER
fun handleAiAction(
    intent: String,
    serviceMap: Map<String, Pair<Int, String>>,
    navController: NavController,
    userId: Int,
    onReply: (String) -> Unit
) {
    // A. If Python returns a Service Name (e.g., "Electrician")
    if (serviceMap.containsKey(intent)) {
        val (id, name) = serviceMap[intent]!!
        onReply("I understood you need a $name. Redirecting you to providers...")

        // Navigate to the list
        navController.navigate("providers_list/$userId/$id/$name")
    }
    // B. If Python returns the "Not Trained" message
    else if (intent.contains("Profix AI services only", ignoreCase = true)) {
        onReply("I am sorry, but I only know about home services. Try asking for a 'Cleaner' or 'Electrician'.")
    }
    // C. Unknown or unexpected response
    else {
        onReply(intent) // Just show what the bot said
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isUser) Color(0xFF0D9997) else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 16.dp
            ),
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 15.sp
            )
        }
    }
}