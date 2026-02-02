<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit();
}

$user_id = intval($data['user_id']);
$full_name = isset($data['full_name']) ? trim($data['full_name']) : null;
$phone = isset($data['phone']) ? trim($data['phone']) : null;
$address = isset($data['address']) ? trim($data['address']) : null;
$city = isset($data['city']) ? trim($data['city']) : null;
$pincode = isset($data['pincode']) ? trim($data['pincode']) : null;

try {
    $updates = [];
    $params = [];
    
    if ($full_name) { $updates[] = "full_name = ?"; $params[] = $full_name; }
    if ($phone) { $updates[] = "phone = ?"; $params[] = $phone; }
    if ($address !== null) { $updates[] = "address = ?"; $params[] = $address; }
    if ($city !== null) { $updates[] = "city = ?"; $params[] = $city; }
    if ($pincode !== null) { $updates[] = "pincode = ?"; $params[] = $pincode; }
    
    if (empty($updates)) {
        echo json_encode(['success' => false, 'message' => 'No fields to update']);
        exit();
    }
    
    $params[] = $user_id;
    $sql = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    
    echo json_encode([
        'success' => true,
        'message' => 'Profile updated successfully'
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Update failed: ' . $e->getMessage()]);
}
?>
