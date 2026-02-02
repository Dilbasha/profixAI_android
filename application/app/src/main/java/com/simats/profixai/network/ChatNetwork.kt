package com.simats.profixai.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Data Models
// Python expects: {"message": "..."}
data class ChatRequest(val message: String)

// Python returns: {"reply": "..."}
// WE MUST USE "reply" here to match your app.py
data class ChatResponse(val reply: String)

// 2. API Interface
interface ChatApiService {
    @POST("chat") // Matches @app.route("/chat", ...)
    suspend fun getBotResponse(@Body request: ChatRequest): ChatResponse
}

// 3. Retrofit Instance
object ChatClient {
    // Your Render URL
    private const val BASE_URL = "https://profix-chatbot.onrender.com/"

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }
}