<?php
require_once 'db.php';
require_once 'notification_helper.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['provider_id']) || !isset($data['booking_date']) || 
    !isset($data['booking_time']) || !isset($data['address']) || !isset($data['estimated_hours'])) {
    echo json_encode(['success' => false, 'message' => 'All required fields must be provided']);
    exit();
}

$user_id = intval($data['user_id']);
$provider_id = intval($data['provider_id']);
$booking_date = $data['booking_date'];
$booking_time = $data['booking_time'];
$address = trim($data['address']);
$city = isset($data['city']) ? trim($data['city']) : '';
$pincode = isset($data['pincode']) ? trim($data['pincode']) : '';
$description = isset($data['description']) ? trim($data['description']) : '';
$estimated_hours = intval($data['estimated_hours']);

try {
    // Get provider's hourly rate, service_id and name
    $stmt = $pdo->prepare("SELECT p.hourly_rate, p.service_id, p.full_name, s.name as service_name FROM providers p JOIN services s ON p.service_id = s.id WHERE p.id = ?");
    $stmt->execute([$provider_id]);
    $provider = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$provider) {
        echo json_encode(['success' => false, 'message' => 'Provider not found']);
        exit();
    }
    
    // Get user name
    $stmt = $pdo->prepare("SELECT full_name FROM users WHERE id = ?");
    $stmt->execute([$user_id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    $total_amount = $provider['hourly_rate'] * $estimated_hours;
    $service_id = $provider['service_id'];
    
    $stmt = $pdo->prepare("INSERT INTO bookings (user_id, provider_id, service_id, booking_date, booking_time, address, city, pincode, description, estimated_hours, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([$user_id, $provider_id, $service_id, $booking_date, $booking_time, $address, $city, $pincode, $description, $estimated_hours, $total_amount]);
    
    $bookingId = $pdo->lastInsertId();
    
    // Create notification for provider (new booking request)
    createNotification(
        $pdo,
        null,
        $provider_id,
        'booking_created',
        'New Booking Request',
        'You have a new booking request from ' . ($user['full_name'] ?? 'a customer') . ' for ' . $provider['service_name'] . ' on ' . $booking_date,
        $bookingId
    );
    
    // Create notification for user (booking confirmed)
    createNotification(
        $pdo,
        $user_id,
        null,
        'booking_submitted',
        'Booking Submitted',
        'Your booking for ' . $provider['service_name'] . ' with ' . $provider['full_name'] . ' has been submitted. Waiting for confirmation.',
        $bookingId
    );
    
    echo json_encode([
        'success' => true,
        'message' => 'Booking created successfully',
        'booking' => [
            'id' => $bookingId,
            'total_amount' => $total_amount,
            'status' => 'pending'
        ]
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Booking failed: ' . $e->getMessage()]);
}
?>

