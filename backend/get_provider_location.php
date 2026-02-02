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

if (!isset($data['booking_id'])) {
    echo json_encode(['success' => false, 'message' => 'Booking ID is required']);
    exit();
}

$booking_id = intval($data['booking_id']);

try {
    // Get booking details to verify status and get provider_id
    $bookingStmt = $pdo->prepare("
        SELECT b.id, b.provider_id, b.status, b.address, b.city,
               p.full_name as provider_name, p.phone as provider_phone
        FROM bookings b
        JOIN providers p ON b.provider_id = p.id
        WHERE b.id = ?
    ");
    $bookingStmt->execute([$booking_id]);
    $booking = $bookingStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$booking) {
        echo json_encode(['success' => false, 'message' => 'Booking not found']);
        exit();
    }
    
    // Only allow tracking for accepted or in_progress bookings
    if (!in_array($booking['status'], ['accepted', 'in_progress'])) {
        echo json_encode([
            'success' => false, 
            'message' => 'Location tracking is only available for accepted or in-progress bookings',
            'can_track' => false
        ]);
        exit();
    }
    
    // Get provider's current location
    $locationStmt = $pdo->prepare("
        SELECT latitude, longitude, is_sharing, updated_at
        FROM provider_locations
        WHERE provider_id = ? AND is_sharing = TRUE
    ");
    $locationStmt->execute([$booking['provider_id']]);
    $location = $locationStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$location) {
        echo json_encode([
            'success' => true,
            'can_track' => true,
            'location_available' => false,
            'message' => 'Provider location not available yet',
            'provider_name' => $booking['provider_name'],
            'provider_phone' => $booking['provider_phone'],
            'destination_address' => $booking['address'] . ', ' . $booking['city']
        ]);
        exit();
    }
    
    echo json_encode([
        'success' => true,
        'can_track' => true,
        'location_available' => true,
        'latitude' => floatval($location['latitude']),
        'longitude' => floatval($location['longitude']),
        'last_updated' => $location['updated_at'],
        'provider_name' => $booking['provider_name'],
        'provider_phone' => $booking['provider_phone'],
        'destination_address' => $booking['address'] . ', ' . $booking['city']
    ]);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
