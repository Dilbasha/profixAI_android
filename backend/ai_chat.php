<?php
/**
 * AI Chat Endpoint for ProFIX AI
 * Connects to Google Gemini API for AI-powered chat responses
 * Fetches all database context to give AI comprehensive knowledge
 */

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

// ====================================

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->message) || trim($data->message) === '') {
    echo json_encode(["success" => false, "message" => "Message is required"]);
    exit();
}

$userMessage = trim($data->message);
$userType = isset($data->user_type) ? $data->user_type : 'user';
$userId = isset($data->user_id) ? intval($data->user_id) : 0;
$providerId = isset($data->provider_id) ? intval($data->provider_id) : 0;

// Build comprehensive database context
$dbContext = "";

try {
    // ============ SERVICES ============
    $stmt = $pdo->query("SELECT id, name, description, icon FROM services ORDER BY id");
    $services = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $dbContext .= "\n\n=== AVAILABLE SERVICES ===\n";
    foreach ($services as $service) {
        $dbContext .= "- {$service['name']}: {$service['description']}\n";
    }

    // ============ ALL PROVIDERS ============
    $stmt = $pdo->query("
        SELECT 
            p.id, p.full_name, p.service_name, p.hourly_rate, p.experience_years,
            p.city, p.rating, p.is_available, p.phone, p.description,
            p.total_jobs, p.is_verified
        FROM providers p 
        WHERE p.is_verified = 1 
        ORDER BY p.rating DESC
        LIMIT 100
    ");
    $providers = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    $dbContext .= "\n\n=== ALL SERVICE PROVIDERS ===\n";
    foreach ($providers as $provider) {
        $available = $provider['is_available'] ? 'Available Now' : 'Busy';
        $dbContext .= "Provider #{$provider['id']}: {$provider['full_name']}\n";
        $dbContext .= "  - Service: {$provider['service_name']}\n";
        $dbContext .= "  - Rate: Rs.{$provider['hourly_rate']}/hour\n";
        $dbContext .= "  - Experience: {$provider['experience_years']} years\n";
        $dbContext .= "  - Rating: {$provider['rating']}/5 ({$provider['total_jobs']} jobs completed)\n";
        $dbContext .= "  - Location: {$provider['city']}\n";
        $dbContext .= "  - Status: {$available}\n";
        $dbContext .= "  - About: {$provider['description']}\n\n";
    }

    // ============ PLATFORM STATISTICS ============
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM users");
    $totalUsers = $stmt->fetch()['count'];
    
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM providers WHERE is_verified = 1");
    $totalProviders = $stmt->fetch()['count'];
    
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM bookings");
    $totalBookings = $stmt->fetch()['count'];
    
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM bookings WHERE status = 'completed'");
    $completedBookings = $stmt->fetch()['count'];
    
    $dbContext .= "\n\n=== PLATFORM STATISTICS ===\n";
    $dbContext .= "- Total Registered Users: {$totalUsers}\n";
    $dbContext .= "- Verified Service Providers: {$totalProviders}\n";
    $dbContext .= "- Total Bookings Made: {$totalBookings}\n";
    $dbContext .= "- Successfully Completed Services: {$completedBookings}\n";

    // ============ SERVICE CATEGORY SUMMARY ============
    $stmt = $pdo->query("
        SELECT service_name, COUNT(*) as count, AVG(rating) as avg_rating, AVG(hourly_rate) as avg_rate 
        FROM providers 
        WHERE is_verified = 1 
        GROUP BY service_name
    ");
    $serviceStats = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    $dbContext .= "\n\n=== SERVICE CATEGORIES SUMMARY ===\n";
    foreach ($serviceStats as $stat) {
        $avgRating = round($stat['avg_rating'], 1);
        $avgRate = round($stat['avg_rate']);
        $dbContext .= "- {$stat['service_name']}: {$stat['count']} providers, Avg Rating: {$avgRating}/5, Avg Rate: Rs.{$avgRate}/hr\n";
    }

    // ============ USER-SPECIFIC CONTEXT (if user is logged in) ============
    if ($userType === 'user' && $userId > 0) {
        $stmt = $pdo->prepare("SELECT full_name, email, phone, city FROM users WHERE id = ?");
        $stmt->execute([$userId]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($user) {
            $dbContext .= "\n\n=== CURRENT USER INFO ===\n";
            $dbContext .= "- Name: {$user['full_name']}\n";
            $dbContext .= "- City: {$user['city']}\n";
            
            // User's booking history
            $stmt = $pdo->prepare("
                SELECT b.*, p.full_name as provider_name, p.service_name
                FROM bookings b
                JOIN providers p ON b.provider_id = p.id
                WHERE b.user_id = ?
                ORDER BY b.created_at DESC
                LIMIT 10
            ");
            $stmt->execute([$userId]);
            $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            if (count($bookings) > 0) {
                $dbContext .= "\n=== USER'S RECENT BOOKINGS ===\n";
                foreach ($bookings as $booking) {
                    $dbContext .= "- {$booking['service_name']} by {$booking['provider_name']} on {$booking['booking_date']} - Status: {$booking['status']}\n";
                }
            }
        }
    }

    // ============ PROVIDER-SPECIFIC CONTEXT (if provider is logged in) ============
    if ($userType === 'provider' && $providerId > 0) {
        $stmt = $pdo->prepare("SELECT * FROM providers WHERE id = ?");
        $stmt->execute([$providerId]);
        $provider = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($provider) {
            $dbContext .= "\n\n=== YOUR PROVIDER PROFILE ===\n";
            $dbContext .= "- Name: {$provider['full_name']}\n";
            $dbContext .= "- Service: {$provider['service_name']}\n";
            $dbContext .= "- Your Rate: Rs.{$provider['hourly_rate']}/hour\n";
            $dbContext .= "- Your Rating: {$provider['rating']}/5\n";
            $dbContext .= "- Total Jobs: {$provider['total_jobs']}\n";
            $dbContext .= "- Earnings: Rs.{$provider['earnings']}\n";
            
            // Provider's recent bookings
            $stmt = $pdo->prepare("
                SELECT b.*, u.full_name as user_name
                FROM bookings b
                JOIN users u ON b.user_id = u.id
                WHERE b.provider_id = ?
                ORDER BY b.created_at DESC
                LIMIT 10
            ");
            $stmt->execute([$providerId]);
            $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            if (count($bookings) > 0) {
                $dbContext .= "\n=== YOUR RECENT BOOKINGS ===\n";
                foreach ($bookings as $booking) {
                    $dbContext .= "- Job for {$booking['user_name']} on {$booking['booking_date']} at {$booking['booking_time']} - Status: {$booking['status']}\n";
                }
            }
            
            // Provider's earnings stats
            $stmt = $pdo->prepare("
                SELECT 
                    COUNT(*) as pending_count,
                    (SELECT COUNT(*) FROM bookings WHERE provider_id = ? AND status = 'completed') as completed_count
                FROM bookings 
                WHERE provider_id = ? AND status = 'pending'
            ");
            $stmt->execute([$providerId, $providerId]);
            $stats = $stmt->fetch(PDO::FETCH_ASSOC);
            
            $dbContext .= "\n=== YOUR STATS ===\n";
            $dbContext .= "- Pending Bookings: {$stats['pending_count']}\n";
            $dbContext .= "- Completed Jobs: {$stats['completed_count']}\n";
        }
    }

} catch (Exception $e) {
    // Continue without full context if database fails
    $dbContext = "\n[Database context unavailable: {$e->getMessage()}]";
}

// Build system context based on user type
if ($userType === 'provider') {
    $systemContext = "You are ProFIX AI Assistant, an intelligent helper for the ProFIX AI home services platform. You are helping a SERVICE PROVIDER who offers their services on the platform.

You have FULL ACCESS to the platform database and can provide personalized advice based on their profile, bookings, and performance.

Help them with:
- Understanding their current bookings and schedule
- Tips for getting more bookings based on their rating and competition
- Pricing strategies compared to other providers
- Customer service best practices
- Managing their availability
- Growing their business

{$dbContext}

Keep responses helpful, specific, and actionable. Reference their actual data when relevant. Do NOT use markdown formatting - use plain text only.";
} else {
    $systemContext = "You are ProFIX AI Assistant, an intelligent helper for the ProFIX AI home services platform. You are helping a USER who needs home services.

You have FULL ACCESS to the platform database including all providers, their ratings, prices, and availability. Use this to give accurate, personalized recommendations.

Help them with:
- Finding the RIGHT service provider for their specific needs
- Comparing providers by rating, price, experience
- Understanding their booking history
- Recommending providers in their city
- Answering questions about services and pricing

{$dbContext}

Keep responses friendly and helpful. When recommending providers, mention their name, service, rate, and rating. Reference their actual booking history when relevant. Do NOT use markdown formatting - use plain text only.";
}

// Build request body matching Google's documentation format
$requestBody = [
    "contents" => [
        [
            "parts" => [
                ["text" => $systemContext . "\n\nUser: " . $userMessage]
            ]
        ]
    ]
];

// Make API request using cURL with x-goog-api-key header
$ch = curl_init($GEMINI_API_URL);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($requestBody));
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'x-goog-api-key: ' . $GEMINI_API_KEY
]);
curl_setopt($ch, CURLOPT_TIMEOUT, 60);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);
curl_close($ch);

if ($curlError) {
    echo json_encode([
        "success" => false, 
        "message" => "Connection error: " . $curlError
    ]);
    exit();
}

$responseData = json_decode($response, true);

if ($httpCode !== 200) {
    $errorMessage = "API error (HTTP $httpCode)";
    if (isset($responseData['error']['message'])) {
        $errorMessage = $responseData['error']['message'];
    }
    echo json_encode([
        "success" => false, 
        "message" => $errorMessage
    ]);
    exit();
}

// Extract AI response
$aiResponse = "";
if (isset($responseData['candidates'][0]['content']['parts'][0]['text'])) {
    $aiResponse = $responseData['candidates'][0]['content']['parts'][0]['text'];
} else {
    $aiResponse = "I'm sorry, I couldn't generate a response. Please try again.";
}

echo json_encode([
    "success" => true,
    "response" => $aiResponse
]);
?>
