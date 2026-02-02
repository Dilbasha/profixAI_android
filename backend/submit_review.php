<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['booking_id']) || !isset($data['user_id']) || !isset($data['rating'])) {
    echo json_encode(['success' => false, 'message' => 'Booking ID, User ID, and rating are required']);
    exit();
}

$booking_id = intval($data['booking_id']);
$user_id = intval($data['user_id']);
$rating = intval($data['rating']);
$comment = isset($data['comment']) ? trim($data['comment']) : '';

if ($rating < 1 || $rating > 5) {
    echo json_encode(['success' => false, 'message' => 'Rating must be between 1 and 5']);
    exit();
}

try {
    // Get booking and verify it's completed
    $stmt = $pdo->prepare("SELECT * FROM bookings WHERE id = ? AND user_id = ?");
    $stmt->execute([$booking_id, $user_id]);
    $booking = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$booking) {
        echo json_encode(['success' => false, 'message' => 'Booking not found']);
        exit();
    }
    
    if ($booking['status'] !== 'completed') {
        echo json_encode(['success' => false, 'message' => 'Only completed bookings can be reviewed']);
        exit();
    }
    
    // Check if already reviewed
    $stmt = $pdo->prepare("SELECT id FROM reviews WHERE booking_id = ?");
    $stmt->execute([$booking_id]);
    if ($stmt->rowCount() > 0) {
        echo json_encode(['success' => false, 'message' => 'Booking already reviewed']);
        exit();
    }
    
    $provider_id = $booking['provider_id'];
    
    // Insert review
    $stmt = $pdo->prepare("INSERT INTO reviews (booking_id, user_id, provider_id, rating, comment) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$booking_id, $user_id, $provider_id, $rating, $comment]);
    
    // Update provider's rating
    $stmt = $pdo->prepare("SELECT AVG(rating) as avg_rating, COUNT(*) as total FROM reviews WHERE provider_id = ?");
    $stmt->execute([$provider_id]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $stmt = $pdo->prepare("UPDATE providers SET rating = ?, total_reviews = ? WHERE id = ?");
    $stmt->execute([round($result['avg_rating'], 1), $result['total'], $provider_id]);
    
    // ========================================
    // AUTO-UPDATE HONOR SCORE AFTER REVIEW
    // ========================================
    updateHonorScore($pdo, $provider_id);
    
    echo json_encode([
        'success' => true,
        'message' => 'Review submitted successfully'
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to submit review: ' . $e->getMessage()]);
}

/**
 * Calculate and update Honor Score for a provider
 */
function updateHonorScore($pdo, $providerId) {
    $scores = [
        'rating_score' => 0,
        'completion_score' => 0,
        'response_score' => 0,
        'review_bonus' => 0,
        'experience_bonus' => 0
    ];
    
    // 1. RATING SCORE (40% weight)
    $stmt = $pdo->prepare("SELECT AVG(rating) as avg_rating, COUNT(*) as review_count FROM reviews WHERE provider_id = ?");
    $stmt->execute([$providerId]);
    $reviewData = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $avgRating = floatval($reviewData['avg_rating'] ?? 0);
    $reviewCount = intval($reviewData['review_count'] ?? 0);
    
    if ($avgRating > 0) {
        $scores['rating_score'] = round(($avgRating / 5) * 40, 1);
    }
    
    // 2. COMPLETION RATE (30% weight)
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
    } else {
        $scores['completion_score'] = 15; // New provider
    }
    
    // 3. RESPONSE RATE (15% weight)
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
    
    // 4. REVIEW COUNT BONUS (10% weight)
    if ($reviewCount > 0) {
        $reviewBonus = min($reviewCount / 50, 1) * 10;
        $scores['review_bonus'] = round($reviewBonus, 1);
    }
    
    // 5. EXPERIENCE BONUS (5% weight)
    $stmt = $pdo->prepare("SELECT experience_years FROM providers WHERE id = ?");
    $stmt->execute([$providerId]);
    $provider = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $experience = intval($provider['experience_years'] ?? 0);
    $expBonus = min($experience / 10, 1) * 5;
    $scores['experience_bonus'] = round($expBonus, 1);
    
    // CALCULATE TOTAL
    $totalScore = round(
        $scores['rating_score'] + 
        $scores['completion_score'] + 
        $scores['response_score'] + 
        $scores['review_bonus'] + 
        $scores['experience_bonus'], 
        1
    );
    
    // Update in database
    $stmt = $pdo->prepare("UPDATE providers SET honor_score = ? WHERE id = ?");
    $stmt->execute([$totalScore, $providerId]);
    
    return $totalScore;
}
?>
