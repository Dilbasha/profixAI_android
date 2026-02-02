<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit();
}

$user_id = intval($data['user_id']);
$status = isset($data['status']) ? $data['status'] : null;

try {
    $sql = "SELECT b.*, p.full_name as provider_name, p.phone as provider_phone, p.profile_image as provider_image, 
                   s.name as service_name, s.icon as service_icon 
            FROM bookings b 
            JOIN providers p ON b.provider_id = p.id 
            JOIN services s ON b.service_id = s.id 
            WHERE b.user_id = ?";
    $params = [$user_id];
    
    if ($status) {
        $sql .= " AND b.status = ?";
        $params[] = $status;
    }
    
    $sql .= " ORDER BY b.created_at DESC";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'bookings' => $bookings
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch bookings: ' . $e->getMessage()]);
}
?>
