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
$source_date = isset($data['source_date']) ? $data['source_date'] : '';
$target_dates = isset($data['target_dates']) ? $data['target_dates'] : [];

if ($provider_id <= 0) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

if (empty($source_date)) {
    echo json_encode(['success' => false, 'message' => 'Source date is required']);
    exit();
}

if (empty($target_dates) || !is_array($target_dates)) {
    echo json_encode(['success' => false, 'message' => 'Target dates array is required']);
    exit();
}

try {
    // Get source availability
    $stmt = $pdo->prepare("
        SELECT status, start_time, end_time 
        FROM provider_availability 
        WHERE provider_id = ? AND date = ?
    ");
    $stmt->execute([$provider_id, $source_date]);
    $source = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$source) {
        echo json_encode(['success' => false, 'message' => 'No availability found for source date']);
        exit();
    }
    
    // Copy to target dates
    $copied = 0;
    $stmt = $pdo->prepare("
        INSERT INTO provider_availability (provider_id, date, status, start_time, end_time)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
            status = VALUES(status),
            start_time = VALUES(start_time),
            end_time = VALUES(end_time),
            updated_at = CURRENT_TIMESTAMP
    ");
    
    foreach ($target_dates as $target_date) {
        if (preg_match('/^\d{4}-\d{2}-\d{2}$/', $target_date)) {
            $stmt->execute([
                $provider_id, 
                $target_date, 
                $source['status'], 
                $source['start_time'], 
                $source['end_time']
            ]);
            $copied++;
        }
    }
    
    echo json_encode([
        'success' => true,
        'message' => "Schedule copied to $copied days successfully"
    ]);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
