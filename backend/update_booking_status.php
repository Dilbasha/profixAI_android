<?php
require_once 'db.php';
require_once 'notification_helper.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['booking_id']) || !isset($data['status'])) {
    echo json_encode(['success' => false, 'message' => 'Booking ID and status are required']);
    exit();
}

$booking_id = intval($data['booking_id']);
$status = $data['status'];
$provider_id = isset($data['provider_id']) ? intval($data['provider_id']) : null;

$valid_statuses = ['pending', 'accepted', 'in_progress', 'completed', 'cancelled'];
if (!in_array($status, $valid_statuses)) {
    echo json_encode(['success' => false, 'message' => 'Invalid status']);
    exit();
}

try {
    // Get booking details with user and provider info
    $stmt = $pdo->prepare("
        SELECT b.*, u.full_name as user_name, p.full_name as provider_name, s.name as service_name
        FROM bookings b
        JOIN users u ON b.user_id = u.id
        JOIN providers p ON b.provider_id = p.id
        JOIN services s ON b.service_id = s.id
        WHERE b.id = ?
    ");
    $stmt->execute([$booking_id]);
    $booking = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$booking) {
        echo json_encode(['success' => false, 'message' => 'Booking not found']);
        exit();
    }
    
    // Verify provider if provider_id is given
    if ($provider_id && $booking['provider_id'] != $provider_id) {
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        exit();
    }
    
    // Update booking status
    $stmt = $pdo->prepare("UPDATE bookings SET status = ? WHERE id = ?");
    $stmt->execute([$status, $booking_id]);
    
    // Create notifications based on status change
    switch ($status) {
        case 'accepted':
            // Notify user that booking is accepted
            createNotification(
                $pdo,
                $booking['user_id'],
                null,
                'booking_accepted',
                'Booking Confirmed',
                'Your booking for ' . $booking['service_name'] . ' with ' . $booking['provider_name'] . ' has been confirmed.',
                $booking_id
            );
            break;
            
        case 'in_progress':
            // Notify user that provider is on the way/started
            createNotification(
                $pdo,
                $booking['user_id'],
                null,
                'booking_started',
                'Service Started',
                $booking['provider_name'] . ' has started working on your ' . $booking['service_name'] . ' service.',
                $booking_id
            );
            break;
            
        case 'completed':
            // Notify user that job is completed and ask for rating
            createNotification(
                $pdo,
                $booking['user_id'],
                null,
                'booking_completed',
                'Task Completed!',
                'Your ' . $booking['service_name'] . ' service has been completed. Please rate your experience with ' . $booking['provider_name'] . '.',
                $booking_id
            );
            
            // Notify provider about completion
            createNotification(
                $pdo,
                null,
                $booking['provider_id'],
                'job_completed',
                'Job Completed',
                'You have successfully completed the ' . $booking['service_name'] . ' job for ' . $booking['user_name'] . '. Payment of ₹' . $booking['total_amount'] . ' is due.',
                $booking_id
            );
            
            // Increment provider's total_jobs
            $pdo->exec("UPDATE providers SET total_jobs = total_jobs + 1 WHERE id = " . $booking['provider_id']);
            
            // Auto-update honor score
            updateHonorScore($pdo, $booking['provider_id']);
            break;
            
        case 'cancelled':
            // Notify both parties
            createNotification(
                $pdo,
                $booking['user_id'],
                null,
                'booking_cancelled',
                'Booking Cancelled',
                'Your booking for ' . $booking['service_name'] . ' on ' . $booking['booking_date'] . ' has been cancelled.',
                $booking_id
            );
            
            createNotification(
                $pdo,
                null,
                $booking['provider_id'],
                'booking_cancelled',
                'Booking Cancelled',
                'The booking from ' . $booking['user_name'] . ' for ' . $booking['booking_date'] . ' has been cancelled.',
                $booking_id
            );
            
            // Auto-update honor score (cancellations affect completion rate)
            updateHonorScore($pdo, $booking['provider_id']);
            break;
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Booking status updated successfully'
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Update failed: ' . $e->getMessage()]);
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
        $scores['completion_score'] = 15;
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
        $scores['response_score'] = 7.5;
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
