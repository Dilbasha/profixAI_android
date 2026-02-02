<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($data['provider_id']);

try {
    // Get total bookings
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM bookings WHERE provider_id = ?");
    $stmt->execute([$provider_id]);
    $totalBookings = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Get pending bookings
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM bookings WHERE provider_id = ? AND status = 'pending'");
    $stmt->execute([$provider_id]);
    $pendingBookings = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Get completed bookings
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM bookings WHERE provider_id = ? AND status = 'completed'");
    $stmt->execute([$provider_id]);
    $completedBookings = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Get total earnings
    $stmt = $pdo->prepare("SELECT SUM(total_amount) as total FROM bookings WHERE provider_id = ? AND status = 'completed'");
    $stmt->execute([$provider_id]);
    $totalEarnings = $stmt->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;
    
    // Get average rating
    $stmt = $pdo->prepare("SELECT AVG(rating) as avg_rating FROM reviews WHERE provider_id = ?");
    $stmt->execute([$provider_id]);
    $avgRating = round($stmt->fetch(PDO::FETCH_ASSOC)['avg_rating'] ?? 0, 1);
    
    echo json_encode([
        'success' => true,
        'stats' => [
            'total_bookings' => $totalBookings,
            'pending_bookings' => $pendingBookings,
            'completed_bookings' => $completedBookings,
            'total_earnings' => $totalEarnings,
            'average_rating' => $avgRating
        ]
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch stats: ' . $e->getMessage()]);
}
?>
