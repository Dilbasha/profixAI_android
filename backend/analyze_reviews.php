<?php
/**
 * Review Sentiment Analysis Endpoint (Local Logic Version)
 * Analyzes reviews using local keyword matching + star ratings.
 * No API Key required.
 */

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

// --- LOCAL SENTIMENT DICTIONARIES ---
// You can add more words to these lists to improve accuracy
$POSITIVE_WORDS = [
    'great', 'good', 'excellent', 'amazing', 'awesome', 'fantastic', 'love', 'loved',
    'best', 'nice', 'friendly', 'fast', 'helpful', 'professional', 'recommended', 
    'perfect', 'clean', 'happy', 'satisfied', 'wonderful', 'beautiful', 'brilliant',
    'super', 'top', 'quick', 'efficient', 'smooth', 'easy', 'thank', 'thanks'
];

$NEGATIVE_WORDS = [
    'bad', 'terrible', 'awful', 'horrible', 'worst', 'poor', 'slow', 'rude', 
    'hate', 'hated', 'disappointed', 'disappointing', 'useless', 'waste', 'money',
    'dirty', 'mess', 'unprofessional', 'late', 'never', 'avoid', 'expensive', 
    'broken', 'damage', 'damaged', 'fail', 'failed', 'issue', 'problem', 'mistake'
];

/**
 * Helper function to analyze text sentiment
 */
function analyzeSentimentLocal($text, $rating, $posWords, $negWords) {
    // 1. Normalize text
    $text = strtolower($text);
    $score = 0;
    
    // 2. Count Positive matches
    foreach ($posWords as $word) {
        if (strpos($text, $word) !== false) {
            $score++;
        }
    }
    
    // 3. Count Negative matches
    foreach ($negWords as $word) {
        if (strpos($text, $word) !== false) {
            $score--;
        }
    }
    
    // 4. Hybrid Decision Logic (Rating + Text Score)
    
    // Strong signals from rating override text (usually)
    if ($rating == 5) {
        return ($score < -2) ? 'negative' : 'positive'; // Only negative if text is really bad
    }
    if ($rating <= 2) {
        return ($score > 2) ? 'positive' : 'negative'; // Only positive if text is glowing
    }
    
    // For 3 & 4 stars, rely on text score
    if ($rating == 4) {
        return ($score >= -1) ? 'positive' : 'negative'; // Bias towards positive
    }
    
    // For 3 stars (neutral ground)
    return ($score >= 0) ? 'positive' : 'negative';
}

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->provider_id)) {
    echo json_encode(["success" => false, "message" => "Provider ID is required"]);
    exit();
}

$providerId = intval($data->provider_id);

try {
    // Fetch all reviews for this provider
    $stmt = $pdo->prepare("
        SELECT r.id, r.rating, r.comment, r.created_at, u.full_name as user_name, u.profile_image as user_image
        FROM reviews r
        JOIN users u ON r.user_id = u.id
        WHERE r.provider_id = ?
        ORDER BY r.created_at DESC
    ");
    $stmt->execute([$providerId]);
    $reviews = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (empty($reviews)) {
        echo json_encode([
            "success" => true,
            "positive_reviews" => [],
            "negative_reviews" => [],
            "positive_count" => 0,
            "negative_count" => 0,
            "total_reviews" => 0,
            "summary" => "No reviews yet"
        ]);
        exit();
    }
    
    $positiveReviews = [];
    $negativeReviews = [];
    
    // --- MAIN ANALYSIS LOOP ---
    foreach ($reviews as $review) {
        $comment = $review['comment'] ?? '';
        $rating = intval($review['rating']);
        
        // Use our local function instead of API
        $sentiment = analyzeSentimentLocal($comment, $rating, $POSITIVE_WORDS, $NEGATIVE_WORDS);
        
        // Assign result
        $review['sentiment'] = $sentiment;
        
        if ($sentiment === 'positive') {
            $positiveReviews[] = $review;
        } else {
            $negativeReviews[] = $review;
        }
    }
    
    // --- GENERATE SUMMARY ---
    $posCount = count($positiveReviews);
    $negCount = count($negativeReviews);
    $total = count($reviews);
    
    // Simple logic to generate a sentence summary
    $percentagePos = ($total > 0) ? round(($posCount / $total) * 100) : 0;
    
    if ($percentagePos >= 90) {
        $summary = "Overwhelmingly positive feedback from customers.";
    } elseif ($percentagePos >= 70) {
        $summary = "Most customers are happy with the service.";
    } elseif ($percentagePos >= 40) {
        $summary = "Mixed feedback with some concerns raised.";
    } else {
        $summary = "Many customers reported issues with the service.";
    }
    
    // --- RETURN RESPONSE ---
    echo json_encode([
        "success" => true,
        "positive_reviews" => $positiveReviews,
        "negative_reviews" => $negativeReviews,
        "positive_count" => $posCount,
        "negative_count" => $negCount,
        "total_reviews" => $total,
        "summary" => $summary
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Error: " . $e->getMessage()
    ]);
}
?>