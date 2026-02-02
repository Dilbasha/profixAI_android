package com.simats.profixai.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.profixai.ui.screens.user.*
import com.simats.profixai.ui.screens.user.UserProfileScreen
import com.simats.profixai.ui.screens.provider.*
import com.simats.profixai.ui.screens.admin.*
import com.simats.profixai.ui.screens.common.NotificationsScreen
import com.simats.profixai.ui.screens.common.HelpScreen
import com.simats.profixai.ui.screens.common.AboutScreen
import com.simats.profixai.ui.screens.common.TermsOfServiceScreen
import com.simats.profixai.ui.screens.common.PrivacyPolicyScreen
import com.simats.profixai.ui.screens.common.RefundPolicyScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // ============ COMMON SCREENS ============
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        composable("role_selection") {
            RoleSelectionScreen(navController = navController)
        }
        
        // ============ USER SCREENS ============
        composable("user_login") {
            UserLoginScreen(navController = navController)
        }
        
        composable("user_register") {
            UserRegisterScreen(navController = navController)
        }
        
        composable("user_home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            UserHomeScreen(navController = navController, userId = userId)
        }
        
        composable(
            "providers_list/{userId}/{serviceId}/{serviceName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("serviceId") { type = NavType.IntType },
                navArgument("serviceName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: 0
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            ProvidersListScreen(
                navController = navController,
                userId = userId,
                serviceId = serviceId,
                serviceName = serviceName
            )
        }
        
        composable(
            "provider_details/{userId}/{providerId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("providerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val providerId = backStackEntry.arguments?.getInt("providerId") ?: 0
            ProviderDetailsScreen(
                navController = navController,
                userId = userId,
                providerId = providerId
            )
        }
        
        composable(
            "booking_confirm/{userId}/{providerId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("providerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val providerId = backStackEntry.arguments?.getInt("providerId") ?: 0
            BookingConfirmScreen(
                navController = navController,
                userId = userId,
                providerId = providerId
            )
        }
        
        composable("user_bookings/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            UserBookingsScreen(navController = navController, userId = userId)
        }
        
        composable("location_tracking/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId")?.toIntOrNull() ?: 0
            LocationTrackingScreen(navController = navController, bookingId = bookingId)
        }
        
        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            UserProfileScreen(navController = navController, userId = userId)
        }
        
        composable("edit_user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            EditUserProfileScreen(navController = navController, userId = userId)
        }
        
        composable("rating/{userId}/{bookingId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            val bookingId = backStackEntry.arguments?.getString("bookingId")?.toIntOrNull() ?: 0
            RatingScreen(navController = navController, userId = userId, bookingId = bookingId)
        }
        
        composable("booking_history/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            BookingHistoryScreen(navController = navController, userId = userId)
        }
        
        composable("user_notifications/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            NotificationsScreen(navController = navController, userId = userId, isProvider = false)
        }
        
        composable("user_ai_chat/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            UserAiChatScreen(navController = navController, userId = userId)
        }
        
        // ============ PROVIDER SCREENS ============
        composable("provider_login") {
            ProviderLoginScreen(navController = navController)
        }
        
        composable("provider_register") {
            ProviderRegisterScreen(navController = navController)
        }
        
        composable("provider_home/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            ProviderHomeScreen(navController = navController, providerId = providerId)
        }
        
        composable("provider_bookings/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            ProviderBookingsScreen(navController = navController, providerId = providerId)
        }
        
        composable("provider_profile/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            ProviderProfileScreen(navController = navController, providerId = providerId)
        }
        
        composable("edit_provider_profile/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            EditProviderProfileScreen(navController = navController, providerId = providerId)
        }
        
        composable("earnings_history/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            EarningsHistoryScreen(navController = navController, providerId = providerId)
        }
        
        composable("provider_notifications/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            NotificationsScreen(navController = navController, providerId = providerId, isProvider = true)
        }
        
        composable("provider_schedule/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            ProviderScheduleScreen(navController = navController, providerId = providerId)
        }
        
        composable("provider_portfolio/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            ProviderPortfolioScreen(navController = navController, providerId = providerId)
        }
        
        composable("provider_ai_chat/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")?.toIntOrNull() ?: 0
            ProviderAiChatScreen(navController = navController, providerId = providerId)
        }
        
        // ============ ADMIN SCREENS ============
        composable("admin_login") {
            AdminLoginScreen(navController = navController)
        }
        
        composable("admin_home/{adminId}") { backStackEntry ->
            val adminId = backStackEntry.arguments?.getString("adminId")?.toIntOrNull() ?: 0
            AdminHomeScreen(navController = navController, adminId = adminId)
        }
        
        composable("pending_approvals") {
            PendingApprovalsScreen(navController = navController)
        }
        
        composable("admin_dashboard_stats") {
            AdminDashboardStatsScreen(navController = navController)
        }
        
        composable("admin_labor_tracking") {
            AdminLaborTrackingScreen(navController = navController)
        }
        
        composable(
            "admin_provider_profile/{providerId}",
            arguments = listOf(navArgument("providerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getInt("providerId") ?: 0
            AdminProviderProfileScreen(navController = navController, providerId = providerId)
        }
        
        // ============ COMMON SCREENS ============
        composable("help_user") {
            HelpScreen(navController = navController, isProvider = false)
        }
        
        composable("help_provider") {
            HelpScreen(navController = navController, isProvider = true)
        }
        
        composable("about") {
            AboutScreen(navController = navController)
        }
        
        composable("terms_of_service") {
            TermsOfServiceScreen(navController = navController)
        }
        
        composable("privacy_policy") {
            PrivacyPolicyScreen(navController = navController)
        }
        
        composable("refund_policy") {
            RefundPolicyScreen(navController = navController)
        }
    }
}

