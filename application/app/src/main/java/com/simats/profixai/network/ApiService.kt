package com.simats.profixai.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    // ============ AUTH ENDPOINTS ============
    @POST("user_register.php")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<AuthResponse>
    
    @POST("user_login.php")
    suspend fun loginUser(@Body request: LoginRequest): Response<UserLoginResponse>

    @POST("provider_register.php")
    suspend fun registerProvider(@Body request: ProviderRegisterRequest): Response<AuthResponse>
    
    @POST("provider_login.php")
    suspend fun loginProvider(@Body request: LoginRequest): Response<ProviderLoginResponse>
    
    @POST("admin_login.php")
    suspend fun loginAdmin(@Body request: LoginRequest): Response<AdminLoginResponse>
    
    // ============ SERVICES ============
    @GET("get_services.php")
    suspend fun getServices(): Response<ServicesResponse>
    
    // ============ PROVIDERS ============
    @POST("get_providers.php")
    suspend fun getProviders(@Body request: GetProvidersRequest): Response<ProvidersResponse>
    
    @POST("get_provider_details.php")
    suspend fun getProviderDetails(@Body request: ProviderIdRequest): Response<ProviderDetailsResponse>
    
    @POST("get_provider_profile.php")
    suspend fun getProviderProfile(@Body request: ProviderIdRequest): Response<ProviderProfileResponse>
    
    @POST("update_provider_profile.php")
    suspend fun updateProviderProfile(@Body request: UpdateProviderProfileRequest): Response<BasicResponse>
    
    @POST("get_provider_stats.php")
    suspend fun getProviderStats(@Body request: ProviderIdRequest): Response<ProviderStatsResponse>
    
    // ============ IMAGE UPLOAD ============
    @Multipart
    @POST("upload_provider_image.php")
    suspend fun uploadProviderImage(
        @Part("provider_id") providerId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
    
    @Multipart
    @POST("upload_user_image.php")
    suspend fun uploadUserImage(
        @Part("user_id") userId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
    
    // ============ BOOKINGS ============
    @POST("create_booking.php")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<CreateBookingResponse>
    
    @POST("get_user_bookings.php")
    suspend fun getUserBookings(@Body request: UserIdRequest): Response<BookingsResponse>
    
    @POST("get_provider_bookings.php")
    suspend fun getProviderBookings(@Body request: ProviderIdRequest): Response<BookingsResponse>
    
    @POST("update_booking_status.php")
    suspend fun updateBookingStatus(@Body request: UpdateBookingStatusRequest): Response<BasicResponse>
    
    // ============ REVIEWS ============
    @POST("submit_review.php")
    suspend fun submitReview(@Body request: SubmitReviewRequest): Response<BasicResponse>
    
    // ============ PROFILES ============
    @POST("get_user_profile.php")
    suspend fun getUserProfile(@Body request: UserIdRequest): Response<UserProfileResponse>
    
    @POST("update_user_profile.php")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): Response<BasicResponse>
    
    // ============ ADMIN ============
    @GET("get_pending_providers.php")
    suspend fun getPendingProviders(): Response<ProvidersResponse>
    
    @POST("provider_action.php")
    suspend fun providerAction(@Body request: ProviderActionRequest): Response<BasicResponse>
    
    // ============ NOTIFICATIONS ============
    @POST("get_notifications.php")
    suspend fun getNotifications(@Body request: GetNotificationsRequest): Response<NotificationsResponse>
    
    @POST("mark_notification_read.php")
    suspend fun markNotificationRead(@Body request: MarkNotificationReadRequest): Response<BasicResponse>
    
    // ============ AVAILABILITY ============
    @POST("get_provider_availability.php")
    suspend fun getProviderAvailability(@Body request: GetAvailabilityRequest): Response<AvailabilityResponse>
    
    @POST("update_provider_availability.php")
    suspend fun updateProviderAvailability(@Body request: UpdateAvailabilityRequest): Response<BasicResponse>
    
    @POST("copy_availability.php")
    suspend fun copyAvailability(@Body request: CopyAvailabilityRequest): Response<BasicResponse>
    
    // ============ PORTFOLIO ============
    @Multipart
    @POST("upload_portfolio_image.php")
    suspend fun uploadPortfolioImage(
        @Part("provider_id") providerId: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<PortfolioUploadResponse>
    
    @POST("get_provider_portfolio.php")
    suspend fun getProviderPortfolio(@Body request: ProviderIdRequest): Response<PortfolioResponse>
    
    @POST("delete_portfolio_image.php")
    suspend fun deletePortfolioImage(@Body request: DeletePortfolioRequest): Response<BasicResponse>
    
    // ============ LOCATION TRACKING ============
    @POST("update_provider_location.php")
    suspend fun updateProviderLocation(@Body request: UpdateLocationRequest): Response<BasicResponse>
    
    @POST("get_provider_location.php")
    suspend fun getProviderLocation(@Body request: GetLocationRequest): Response<LocationResponse>
    
    // ============ ADMIN ============
    @GET("get_admin_stats.php")
    suspend fun getAdminStats(): Response<AdminStatsResponse>
    
    @GET("get_approved_providers.php")
    suspend fun getApprovedProviders(): Response<ProvidersResponse>
    
    // ============ AI CHAT ============
    @POST("ai_chat.php")
    suspend fun sendAiChat(@Body request: AiChatRequest): Response<AiChatResponse>
    // ... existing code ...

    // ============ AI INTENT (The Brain) ============
    @POST("predict_intent.php")
    suspend fun predictIntent(@Body request: PredictIntentRequest): Response<IntentResponse>
    
    // ============ SENTIMENT ANALYSIS ============
    @POST("analyze_reviews.php")
    suspend fun analyzeReviews(@Body request: ProviderIdRequest): Response<SentimentAnalysisResponse>

    // Delete User Account
    @POST("delete_user_account.php")
    suspend fun deleteUserAccount(@Body request: UserIdRequest): Response<BasicResponse>
    @POST("delete_provider_account.php")
    suspend fun deleteProviderAccount(@Body request: ProviderIdRequest): Response<BasicResponse>
}

// ============ REQUEST DATA CLASSES ============
data class LoginRequest(
    val email: String,
    val password: String
)

data class AiChatRequest(
    val message: String,
    val user_type: String = "user",
    val user_id: Int = 0,
    val provider_id: Int = 0
)

data class UserRegisterRequest(
    val full_name: String,
    val email: String,
    val phone: String,
    val password: String,
    val address: String = "",
    val city: String = "",
    val pincode: String = "",
    val dob: String? = null
)

data class ProviderRegisterRequest(
    val full_name: String,
    val email: String,
    val phone: String,
    val password: String,
    val service_id: Int,
    val hourly_rate: Double,
    val experience_years: Int = 0,
    val description: String = "",
    val address: String = "",
    val city: String = "",
    val pincode: String = "",
    val aadhaar: String = ""
)

data class GetProvidersRequest(
    val service_id: Int? = null,
    val city: String? = null
)

data class ProviderIdRequest(
    val provider_id: Int
)

data class UserIdRequest(
    val user_id: Int
)

data class CreateBookingRequest(
    val user_id: Int,
    val provider_id: Int,
    val booking_date: String,
    val booking_time: String,
    val address: String,
    val city: String = "",
    val pincode: String = "",
    val description: String = "",
    val estimated_hours: Int = 1
)

data class UpdateBookingStatusRequest(
    val booking_id: Int,
    val status: String,
    val provider_id: Int? = null
)

data class SubmitReviewRequest(
    val booking_id: Int,
    val user_id: Int,
    val rating: Int,
    val comment: String = ""
)

data class UpdateUserProfileRequest(
    val user_id: Int,
    val full_name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val pincode: String? = null
)

data class UpdateProviderProfileRequest(
    val provider_id: Int,
    val full_name: String? = null,
    val phone: String? = null,
    val hourly_rate: Double? = null,
    val experience_years: Int? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val is_available: Int? = null
)

data class ProviderActionRequest(
    val provider_id: Int,
    val action: String, // "approve" or "reject"
    val rejection_reason: String? = null
)
// Add to Request Classes
data class PredictIntentRequest(
    val text: String
)

// Add to Response Classes


// ============ RESPONSE DATA CLASSES ============
data class BasicResponse(
    val success: Boolean,
    val message: String
)
data class IntentResponse(
    val status: String,
    val intent: String
)
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)

data class UserLoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)

data class ProviderLoginResponse(
    val success: Boolean,
    val message: String,
    val provider: Provider? = null
)

data class AdminLoginResponse(
    val success: Boolean,
    val message: String,
    val admin: Admin? = null
)

data class ServicesResponse(
    val success: Boolean,
    val services: List<Service> = emptyList()
)

data class ProvidersResponse(
    val success: Boolean,
    val providers: List<Provider> = emptyList()
)

data class ProviderDetailsResponse(
    val success: Boolean,
    val provider: Provider? = null,
    val reviews: List<Review> = emptyList(),
    val availability: List<ProviderAvailability> = emptyList(),
    val portfolio: List<PortfolioImage> = emptyList()
)

data class ProviderProfileResponse(
    val success: Boolean,
    val provider: Provider? = null
)

data class ProviderStatsResponse(
    val success: Boolean,
    val stats: ProviderStats? = null
)

data class BookingsResponse(
    val success: Boolean,
    val bookings: List<Booking> = emptyList()
)

data class CreateBookingResponse(
    val success: Boolean,
    val message: String,
    val booking: BookingCreated? = null
)

data class ImageUploadResponse(
    val success: Boolean,
    val message: String,
    val image_url: String? = null
)

data class UserProfileResponse(
    val success: Boolean,
    val user: User? = null
)

data class AvailabilityResponse(
    val success: Boolean,
    val availability: List<ProviderAvailability> = emptyList(),
    val month: Int = 0,
    val year: Int = 0
)

data class AiChatResponse(
    val success: Boolean,
    val response: String? = null,
    val message: String? = null
)

data class SentimentAnalysisResponse(
    val success: Boolean,
    val positive_reviews: List<SentimentReview> = emptyList(),
    val negative_reviews: List<SentimentReview> = emptyList(),
    val positive_count: Int = 0,
    val negative_count: Int = 0,
    val total_reviews: Int = 0,
    val summary: String? = null,
    val message: String? = null
)

data class SentimentReview(
    val id: Int = 0,
    val rating: Int = 0,
    val comment: String? = null,
    val user_name: String? = null,
    val user_image: String? = null,
    val created_at: String? = null,
    val sentiment: String? = null
)

// ============ AVAILABILITY REQUEST CLASSES ============
data class GetAvailabilityRequest(
    val provider_id: Int,
    val year: Int,
    val month: Int
)

data class UpdateAvailabilityRequest(
    val provider_id: Int,
    val date: String,
    val status: String,
    val start_time: String,
    val end_time: String
)

data class CopyAvailabilityRequest(
    val provider_id: Int,
    val source_date: String,
    val target_dates: List<String>
)

// ============ AVAILABILITY MODEL ============
data class ProviderAvailability(
    val id: Int = 0,
    val provider_id: Int = 0,
    val date: String,
    val status: String = "available",
    val start_time: String = "09:00",
    val end_time: String = "17:00"
)

// ============ MODEL DATA CLASSES ============
data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val phone: String,
    val address: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val profile_image: String? = null
)

data class Provider(
    val id: Int,
    val full_name: String,
    val email: String,
    val phone: String,
    val service_id: Int,
    val service_name: String? = null,
    val service_icon: String? = null,
    val hourly_rate: Double,
    val experience_years: Int = 0,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val profile_image: String? = null,
    val is_verified: Int = 0,
    val verification_status: String = "pending",
    val rating: Double = 0.0,
    val total_reviews: Int = 0,
    val total_jobs: Int = 0,
    val is_available: Int = 1,
    val honor_score: Double = 0.0
)

data class Admin(
    val id: Int,
    val full_name: String,
    val email: String
)

data class Service(
    val id: Int,
    val name: String,
    val icon: String,
    val description: String? = null,
    val is_active: Boolean = true
)

data class Booking(
    val id: Int,
    val user_id: Int,
    val provider_id: Int,
    val service_id: Int,
    val booking_date: String,
    val booking_time: String,
    val address: String,
    val city: String? = null,
    val pincode: String? = null,
    val description: String? = null,
    val estimated_hours: Int = 1,
    val total_amount: Double,
    val status: String = "pending",
    val payment_status: String = "pending",
    val provider_name: String? = null,
    val provider_phone: String? = null,
    val provider_image: String? = null,
    val user_name: String? = null,
    val user_phone: String? = null,
    val user_image: String? = null,
    val service_name: String? = null,
    val service_icon: String? = null,
    val created_at: String? = null
)

data class BookingCreated(
    val id: Int,
    val total_amount: Double,
    val status: String
)

data class Review(
    val id: Int,
    val booking_id: Int,
    val user_id: Int,
    val provider_id: Int,
    val rating: Int,
    val comment: String? = null,
    val user_name: String? = null,
    val user_image: String? = null,
    val created_at: String? = null
)

data class ProviderStats(
    val total_bookings: Int = 0,
    val pending_bookings: Int = 0,
    val completed_bookings: Int = 0,
    val total_earnings: Double = 0.0,
    val average_rating: Double = 0.0
)

// ============ NOTIFICATION DATA CLASSES ============
data class GetNotificationsRequest(
    val user_id: Int? = null,
    val provider_id: Int? = null
)

data class MarkNotificationReadRequest(
    val notification_id: Int? = null,
    val user_id: Int? = null,
    val provider_id: Int? = null,
    val mark_all: Boolean = false
)

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<Notification> = emptyList()
)

data class Notification(
    val id: Int,
    val user_id: Int? = null,
    val provider_id: Int? = null,
    val type: String,
    val title: String,
    val message: String,
    val related_booking_id: Int? = null,
    val is_read: Boolean = false,
    val created_at: String? = null,
    val booking_date: String? = null,
    val booking_time: String? = null,
    val provider_name: String? = null,
    val user_name: String? = null,
    val service_name: String? = null
)

// ============ PORTFOLIO DATA CLASSES ============
data class PortfolioImage(
    val id: Int,
    val image_url: String,
    val description: String? = null,
    val created_at: String? = null
)

data class PortfolioResponse(
    val success: Boolean,
    val portfolio: List<PortfolioImage> = emptyList()
)

data class PortfolioUploadResponse(
    val success: Boolean,
    val message: String,
    val portfolio_item: PortfolioImage? = null
)

data class DeletePortfolioRequest(
    val portfolio_id: Int,
    val provider_id: Int
)

// ============ LOCATION TRACKING DATA CLASSES ============
data class UpdateLocationRequest(
    val provider_id: Int,
    val booking_id: Int? = null,
    val latitude: Double,
    val longitude: Double,
    val is_sharing: Boolean = true
)

data class GetLocationRequest(
    val booking_id: Int
)

data class LocationResponse(
    val success: Boolean,
    val can_track: Boolean = false,
    val location_available: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val last_updated: String? = null,
    val provider_name: String? = null,
    val provider_phone: String? = null,
    val destination_address: String? = null,
    val message: String? = null
)

// ============ ADMIN DATA CLASSES ============
data class AdminStatsResponse(
    val success: Boolean,
    val stats: AdminStats? = null,
    val recent_activity: List<RecentActivity>? = null,
    val message: String? = null
)

data class AdminStats(
    val total_users: Int = 0,
    val total_providers: Int = 0,
    val pending_providers: Int = 0,
    val approved_providers: Int = 0,
    val total_bookings: Int = 0,
    val completed_bookings: Int = 0,
    val pending_bookings: Int = 0,
    val active_bookings: Int = 0,
    val total_revenue: Double = 0.0,
    val total_hours_worked: Double = 0.0
)

data class RecentActivity(
    val id: Int,
    val status: String,
    val booking_date: String? = null,
    val total_amount: Double? = null,
    val user_name: String? = null,
    val provider_name: String? = null,
    val service_name: String? = null
)

