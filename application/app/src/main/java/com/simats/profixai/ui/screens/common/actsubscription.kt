package com.simats.profixai.ui.screens.common

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.*
import com.simats.profixai.MainActivity

class ActSubscribePage : ComponentActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    // We use a list to support multiple products if needed
    private var productDetailsList = mutableListOf<ProductDetails>()

    // UI State variables
    private var isLoading by mutableStateOf(true)
    private var statusMessage by mutableStateOf("Initializing Billing...")

    companion object {
        private const val TAG = "BillingDebug"
        // MUST MATCH EXACTLY in Play Console -> Monetize -> Products -> Subscriptions
        private const val SUBSCRIPTION_SKU = "profixai_premium_subscription"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBillingClient()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SubscriptionScreenUI(
                        isLoading = isLoading,
                        statusMessage = statusMessage,
                        onSubscribeClick = { launchSubscriptionFlow() },
                        onSkipClick = { navigateToMain() } // Changed to Main directly for skip
                    )
                }
            }
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Connected. Querying products...")
                    querySubscriptionDetails()
                } else {
                    updateStatus("Billing connection failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                updateStatus("Billing disconnected. Retrying...")
                // In production, implement retry logic here
            }
        })
    }

    private fun querySubscriptionDetails() {
        // Create the query params
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, list ->
            runOnUiThread {
                isLoading = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (list.isNotEmpty()) {
                        productDetailsList.addAll(list)
                        statusMessage = "" // Clear message on success
                        Log.d(TAG, "Product Found: ${list[0].name}")
                    } else {
                        statusMessage = "No products found. Check ID: $SUBSCRIPTION_SKU"
                        Log.e(TAG, "Product List is Empty. Check Play Console.")
                    }
                } else {
                    statusMessage = "Query Failed: ${billingResult.debugMessage}"
                }
            }
        }
    }

    private fun launchSubscriptionFlow() {
        if (productDetailsList.isEmpty()) {
            Toast.makeText(this, "Product info not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val productDetails = productDetailsList[0]
        val offerToken = productDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken

        if (offerToken == null) {
            Toast.makeText(this, "No offer token found for subscription", Toast.LENGTH_SHORT).show()
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        runOnUiThread {
                            Toast.makeText(this, "Subscription Active!", Toast.LENGTH_LONG).show()
                            navigateToMain()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun updateStatus(msg: String) {
        runOnUiThread {
            statusMessage = msg
            isLoading = false
        }
    }
}

// --- UPDATED COMPOSE UI ---

@Composable
fun SubscriptionScreenUI(
    isLoading: Boolean,
    statusMessage: String,
    onSubscribeClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val tealColor = Color(0xFF0D9997)
    val goldColor = Color(0xFFFFC107)

    Box(modifier = Modifier.fillMaxSize().background(tealColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // ... (Icon and Title code remains same as your original) ...
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = goldColor
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Upgrade to Premium", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(30.dp))

            // ERROR / STATUS MESSAGE DISPLAY
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = if(statusMessage.contains("Found")) Color.Green else Color(0xFFFF8A80),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A383D))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    BenefitItem("Unlimited Service Requests")
                    BenefitItem("Priority Support")
                    BenefitItem("Ad-free Experience")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSubscribeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && statusMessage.isEmpty() // Only enable if loaded
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Subscribe Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onSkipClick) {
                Text("Skip for now", color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}