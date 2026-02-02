<?php
/**
 * Delete Provider Account
 * Permanently deletes provider and related data
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

if (!isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($data['provider_id']);

try {
    $pdo->beginTransaction();
    
    // Delete reviews for this provider
    $stmt = $pdo->prepare("DELETE FROM reviews WHERE provider_id = ?");
    $stmt->execute([$provider_id]);
    
    // Delete notifications for this provider
    $stmt = $pdo->prepare("DELETE FROM notifications WHERE provider_id = ?");
    $stmt->execute([$provider_id]);
    
    // Delete bookings for this provider
    $stmt = $pdo->prepare("DELETE FROM bookings WHERE provider_id = ?");
    $stmt->execute([$provider_id]);
    
    // Finally delete the provider
    $stmt = $pdo->prepare("DELETE FROM providers WHERE id = ?");
    $stmt->execute([$provider_id]);
    
    if ($stmt->rowCount() > 0) {
        $pdo->commit();
        echo json_encode(['success' => true, 'message' => 'Account deleted successfully']);
    } else {
        $pdo->rollBack();
        echo json_encode(['success' => false, 'message' => 'Provider not found']);
    }
} catch (PDOException $e) {
    $pdo->rollBack();
    echo json_encode(['success' => false, 'message' => 'Failed to delete account: ' . $e->getMessage()]);
}
?>
