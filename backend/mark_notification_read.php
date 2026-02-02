<?php
// Mark notifications as read
header('Content-Type: application/json');
require_once 'db.php';

$data = json_decode(file_get_contents('php://input'), true);

$notification_id = isset($data['notification_id']) ? intval($data['notification_id']) : null;
$user_id = isset($data['user_id']) ? intval($data['user_id']) : null;
$provider_id = isset($data['provider_id']) ? intval($data['provider_id']) : null;
$mark_all = isset($data['mark_all']) ? $data['mark_all'] : false;

try {
    if ($mark_all && ($user_id || $provider_id)) {
        // Mark all as read for user or provider
        if ($user_id) {
            $stmt = $pdo->prepare("UPDATE notifications SET is_read = TRUE WHERE user_id = ?");
            $stmt->execute([$user_id]);
        } else {
            $stmt = $pdo->prepare("UPDATE notifications SET is_read = TRUE WHERE provider_id = ?");
            $stmt->execute([$provider_id]);
        }
        echo json_encode(['success' => true, 'message' => 'All notifications marked as read']);
    } elseif ($notification_id) {
        // Mark single notification as read
        $stmt = $pdo->prepare("UPDATE notifications SET is_read = TRUE WHERE id = ?");
        $stmt->execute([$notification_id]);
        echo json_encode(['success' => true, 'message' => 'Notification marked as read']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Notification ID or mark_all required']);
    }
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
