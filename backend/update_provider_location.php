<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['provider_id']) || !isset($data['latitude']) || !isset($data['longitude'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID, latitude, and longitude are required']);
    exit();
}

$provider_id = intval($data['provider_id']);
$booking_id = isset($data['booking_id']) ? intval($data['booking_id']) : null;
$latitude = floatval($data['latitude']);
$longitude = floatval($data['longitude']);
$is_sharing = isset($data['is_sharing']) ? (bool)$data['is_sharing'] : true;

try {
    // Check if location record exists
    $checkStmt = $pdo->prepare("SELECT id FROM provider_locations WHERE provider_id = ?");
    $checkStmt->execute([$provider_id]);
    $exists = $checkStmt->fetch();
    
    if ($exists) {
        // Update existing location
        $stmt = $pdo->prepare("
            UPDATE provider_locations 
            SET latitude = ?, longitude = ?, booking_id = ?, is_sharing = ?, updated_at = NOW()
            WHERE provider_id = ?
        ");
        $stmt->execute([$latitude, $longitude, $booking_id, $is_sharing, $provider_id]);
    } else {
        // Insert new location record
        $stmt = $pdo->prepare("
            INSERT INTO provider_locations (provider_id, booking_id, latitude, longitude, is_sharing)
            VALUES (?, ?, ?, ?, ?)
        ");
        $stmt->execute([$provider_id, $booking_id, $latitude, $longitude, $is_sharing]);
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Location updated successfully'
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
