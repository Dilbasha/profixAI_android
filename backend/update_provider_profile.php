<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($data['provider_id']);
$full_name = isset($data['full_name']) ? trim($data['full_name']) : null;
$phone = isset($data['phone']) ? trim($data['phone']) : null;
$hourly_rate = isset($data['hourly_rate']) ? floatval($data['hourly_rate']) : null;
$experience_years = isset($data['experience_years']) ? intval($data['experience_years']) : null;
$description = isset($data['description']) ? trim($data['description']) : null;
$address = isset($data['address']) ? trim($data['address']) : null;
$city = isset($data['city']) ? trim($data['city']) : null;
$pincode = isset($data['pincode']) ? trim($data['pincode']) : null;
$is_available = isset($data['is_available']) ? ($data['is_available'] ? 1 : 0) : null;

try {
    $updates = [];
    $params = [];
    
    if ($full_name) { $updates[] = "full_name = ?"; $params[] = $full_name; }
    if ($phone) { $updates[] = "phone = ?"; $params[] = $phone; }
    if ($hourly_rate !== null) { $updates[] = "hourly_rate = ?"; $params[] = $hourly_rate; }
    if ($experience_years !== null) { $updates[] = "experience_years = ?"; $params[] = $experience_years; }
    if ($description !== null) { $updates[] = "description = ?"; $params[] = $description; }
    if ($address !== null) { $updates[] = "address = ?"; $params[] = $address; }
    if ($city !== null) { $updates[] = "city = ?"; $params[] = $city; }
    if ($pincode !== null) { $updates[] = "pincode = ?"; $params[] = $pincode; }
    if ($is_available !== null) { $updates[] = "is_available = ?"; $params[] = $is_available; }
    
    if (empty($updates)) {
        echo json_encode(['success' => false, 'message' => 'No fields to update']);
        exit();
    }
    
    $params[] = $provider_id;
    $sql = "UPDATE providers SET " . implode(", ", $updates) . " WHERE id = ?";
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
