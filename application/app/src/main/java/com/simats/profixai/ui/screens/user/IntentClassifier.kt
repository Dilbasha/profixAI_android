package com.simats.profixai.ui.screens.user

import android.util.Log
import com.simats.profixai.network.ApiService // Import your ApiService
import com.simats.profixai.network.PredictIntentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntentClassifier(
    private val apiService: ApiService // Pass ApiService instead of Context
) {

    // Suspend function: It waits for the server without blocking the app
    suspend fun classify(text: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Send text to PHP
                val response = apiService.predictIntent(PredictIntentRequest(text))

                // 2. Check result
                if (response.isSuccessful && response.body() != null) {
                    val intent = response.body()!!.intent
                    Log.d("ProFixAI", "Server Predicted: $intent")
                    intent // Return the intent (e.g., "hardware_issue")
                } else {
                    Log.e("ProFixAI", "Server Error: ${response.code()}")
                    "unknown"
                }
            } catch (e: Exception) {
                Log.e("ProFixAI", "Network Error: ${e.message}")
                "unknown"
            }
        }
    }
}