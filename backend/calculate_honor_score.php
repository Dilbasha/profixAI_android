<?php
/**
 * Calculate and Update Provider Honor Score
 * 
 * Honor Score is calculated based on:
 * - Average Rating (40% weight)
 * - Completion Rate (30% weight)
 * - Response Rate (15% weight)
 * - Review Count Bonus (10% weight)
 * - Experience Bonus (5% weight)
 */

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

$data = json_decode(file_get_contents("php://input"));

// If provider_id is provided, calculate for specific provider
// Otherwise calculate for all providers
$providerId = isset($data->provider_id) ? intval($data->provider_id) : null;

try {
    if ($providerId) {
        // Calculate for single provider
        $score = calculateHonorScore($pdo, $providerId);
        updateHonorScore($pdo, $providerId, $score);
        
        echo json_encode([
            "success" => true,
            "provider_id" => $providerId,
            "honor_score" => $score['total'],
            "breakdown" => $score
        ]);
    } else {
        // Calculate for all verified providers
        $stmt = $pdo->query("SELECT id FROM providers WHERE is_verified = 1");
        $providers = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $updated = 0;
        foreach ($providers as $provider) {
            $score = calculateHonorScore($pdo, $provider['id']);
            updateHonorScore($pdo, $provider['id'], $score);
            $updated++;
        }
        
        echo json_encode([
            "success" => true,
            "message" => "Updated honor scores for $updated providers"
        ]);
    }
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Error: " . $e->getMessage()
    ]);
}

function calculateHonorScore($pdo, $providerId) {
    $scores = [
        'rating_score' => 0,
        'completion_score' => 0,
        'response_score' => 0,
        'review_bonus' => 0,
        'experience_bonus' => 0,
        'total' => 0
    ];
    
    // 1. RATING SCORE (40% weight, max 40 points)
    // Scale: 5 stars = 40 points, 1 star = 8 points
    $stmt = $pdo->prepare("SELECT AVG(rating) as avg_rating, COUNT(*) as review_count FROM reviews WHERE provider_id = ?");
    $stmt->execute([$providerId]);
    $reviewData = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $avgRating = floatval($reviewData['avg_rating'] ?? 0);
    $reviewCount = intval($reviewData['review_count'] ?? 0);
    
    if ($avgRating > 0) {
        $scores['rating_score'] = round(($avgRating / 5) * 40, 1);
    }
    
    // 2. COMPLETION RATE (30% weight, max 30 points)
    // Completed jobs vs total accepted jobs
    $stmt = $pdo->prepare("
        SELECT 
            COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed,
            COUNT(CASE WHEN status IN ('accepted', 'in_progress', 'completed', 'cancelled') THEN 1 END) as total_accepted
        FROM bookings WHERE provider_id = ?
    ");
    $stmt->execute([$providerId]);
    $bookingData = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $completed = intval($bookingData['completed'] ?? 0);
    $totalAccepted = intval($bookingData['total_accepted'] ?? 0);
    
    if ($totalAccepted > 0) {
        $completionRate = $completed / $totalAccepted;
        $scores['completion_score'] = round($completionRate * 30, 1);
    } elseif ($completed == 0 && $totalAccepted == 0) {
        // New provider, give benefit of doubt
        $scores['completion_score'] = 15; // 50% of max
    }
    
    // 3. RESPONSE RATE (15% weight, max 15 points)
    // Accepted + Rejected vs Pending (shows responsiveness)
    $stmt = $pdo->prepare("
        SELECT 
            COUNT(CASE WHEN status != 'pending' THEN 1 END) as responded,
            COUNT(*) as total
        FROM bookings WHERE provider_id = ?
    ");
    $stmt->execute([$providerId]);
    $responseData = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $responded = intval($responseData['responded'] ?? 0);
    $totalBookings = intval($responseData['total'] ?? 0);
    
    if ($totalBookings > 0) {
        $responseRate = $responded / $totalBookings;
        $scores['response_score'] = round($responseRate * 15, 1);
    } else {
        $scores['response_score'] = 7.5; // New provider
    }
    
    // 4. REVIEW COUNT BONUS (10% weight, max 10 points)
    // More reviews = more trust (caps at 50 reviews)
    if ($reviewCount > 0) {
        $reviewBonus = min($reviewCount / 50, 1) * 10;
        $scores['review_bonus'] = round($reviewBonus, 1);
    }
    
    // 5. EXPERIENCE BONUS (5% weight, max 5 points)
    $stmt = $pdo->prepare("SELECT experience_years FROM providers WHERE id = ?");
    $stmt->execute([$providerId]);
    $provider = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $experience = intval($provider['experience_years'] ?? 0);
    // Cap at 10 years for max bonus
    $expBonus = min($experience / 10, 1) * 5;
    $scores['experience_bonus'] = round($expBonus, 1);
    
    // CALCULATE TOTAL
    $scores['total'] = round(
        $scores['rating_score'] + 
        $scores['completion_score'] + 
        $scores['response_score'] + 
        $scores['review_bonus'] + 
        $scores['experience_bonus'], 
        1
    );
    
    return $scores;
}

function updateHonorScore($pdo, $providerId, $scores) {
    $stmt = $pdo->prepare("UPDATE providers SET honor_score = ? WHERE id = ?");
    $stmt->execute([$scores['total'], $providerId]);
}

/**
 * Get Honor Badge based on score
 */
function getHonorBadge($score) {
    if ($score >= 90) return ['badge' => 'Elite', 'color' => '#FFD700'];      // Gold
    if ($score >= 75) return ['badge' => 'Expert', 'color' => '#C0C0C0'];     // Silver
    if ($score >= 60) return ['badge' => 'Professional', 'color' => '#CD7F32']; // Bronze
    if ($score >= 40) return ['badge' => 'Rising Star', 'color' => '#4CAF50']; // Green
    return ['badge' => 'New', 'color' => '#9E9E9E'];                           // Gray
}
?>
