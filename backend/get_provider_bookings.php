<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($data['provider_id']);
$status = isset($data['status']) ? $data['status'] : null;

try {
    $sql = "SELECT b.*, u.full_name as user_name, u.phone as user_phone, u.profile_image as user_image,
                   s.name as service_name, s.icon as service_icon 
            FROM bookings b 
            JOIN users u ON b.user_id = u.id 
            JOIN services s ON b.service_id = s.id 
            WHERE b.provider_id = ?";
    $params = [$provider_id];
    
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
