<?php
/**
 * Delete User Account
 * Permanently deletes user and related data
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

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit();
}

$user_id = intval($data['user_id']);

try {
    $pdo->beginTransaction();
    
    // Delete reviews by this user
    $stmt = $pdo->prepare("DELETE FROM reviews WHERE user_id = ?");
    $stmt->execute([$user_id]);
    
    // Delete notifications for this user
    $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ?");
    $stmt->execute([$user_id]);
    
    // Delete bookings by this user
    $stmt = $pdo->prepare("DELETE FROM bookings WHERE user_id = ?");
    $stmt->execute([$user_id]);
    
    // Finally delete the user
    $stmt = $pdo->prepare("DELETE FROM users WHERE id = ?");
    $stmt->execute([$user_id]);
    
    if ($stmt->rowCount() > 0) {
        $pdo->commit();
        echo json_encode(['success' => true, 'message' => 'Account deleted successfully']);
    } else {
        $pdo->rollBack();
        echo json_encode(['success' => false, 'message' => 'User not found']);
    }
} catch (PDOException $e) {
    $pdo->rollBack();
    echo json_encode(['success' => false, 'message' => 'Failed to delete account: ' . $e->getMessage()]);
}
?>
