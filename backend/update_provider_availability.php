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

$provider_id = isset($data['provider_id']) ? intval($data['provider_id']) : 0;
$date = isset($data['date']) ? $data['date'] : '';
$status = isset($data['status']) ? $data['status'] : 'available';
$start_time = isset($data['start_time']) ? $data['start_time'] : '09:00';
$end_time = isset($data['end_time']) ? $data['end_time'] : '17:00';

if ($provider_id <= 0) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

if (empty($date)) {
    echo json_encode(['success' => false, 'message' => 'Date is required']);
    exit();
}

// Validate status
$valid_statuses = ['available', 'unavailable', 'partial'];
if (!in_array($status, $valid_statuses)) {
    echo json_encode(['success' => false, 'message' => 'Invalid status. Must be: available, unavailable, or partial']);
    exit();
}

// Validate date format
if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) {
    echo json_encode(['success' => false, 'message' => 'Invalid date format. Use YYYY-MM-DD']);
    exit();
}

try {
    // Use INSERT ON DUPLICATE KEY UPDATE for upsert
    $stmt = $pdo->prepare("
        INSERT INTO provider_availability (provider_id, date, status, start_time, end_time)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
            status = VALUES(status),
            start_time = VALUES(start_time),
            end_time = VALUES(end_time),
            updated_at = CURRENT_TIMESTAMP
    ");
    
    $stmt->execute([$provider_id, $date, $status, $start_time, $end_time]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Availability updated successfully'
    ]);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
