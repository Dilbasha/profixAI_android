<?php
// Get notifications for user or provider
header('Content-Type: application/json');
require_once 'db.php';

$data = json_decode(file_get_contents('php://input'), true);

$user_id = isset($data['user_id']) ? intval($data['user_id']) : null;
$provider_id = isset($data['provider_id']) ? intval($data['provider_id']) : null;

if (!$user_id && !$provider_id) {
    echo json_encode(['success' => false, 'message' => 'User ID or Provider ID required']);
    exit;
}

try {
    if ($user_id) {
        $stmt = $pdo->prepare("
            SELECT n.*, b.booking_date, b.booking_time, p.full_name as provider_name, s.name as service_name
            FROM notifications n
            LEFT JOIN bookings b ON n.related_booking_id = b.id
            LEFT JOIN providers p ON b.provider_id = p.id
            LEFT JOIN services s ON b.service_id = s.id
            WHERE n.user_id = ?
            ORDER BY n.created_at DESC
            LIMIT 50
        ");
        $stmt->execute([$user_id]);
    } else {
        $stmt = $pdo->prepare("
            SELECT n.*, b.booking_date, b.booking_time, u.full_name as user_name, s.name as service_name
            FROM notifications n
            LEFT JOIN bookings b ON n.related_booking_id = b.id
            LEFT JOIN users u ON b.user_id = u.id
            LEFT JOIN services s ON b.service_id = s.id
            WHERE n.provider_id = ?
            ORDER BY n.created_at DESC
            LIMIT 50
        ");
        $stmt->execute([$provider_id]);
    }
    
    $notifications = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Convert is_read to boolean
    foreach ($notifications as &$n) {
        $n['is_read'] = (bool)$n['is_read'];
    }
    
    echo json_encode(['success' => true, 'notifications' => $notifications]);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
