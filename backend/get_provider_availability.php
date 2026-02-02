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
$year = isset($data['year']) ? intval($data['year']) : date('Y');
$month = isset($data['month']) ? intval($data['month']) : date('m');

if ($provider_id <= 0) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

try {
    // Get availability for the specified month
    $start_date = sprintf('%04d-%02d-01', $year, $month);
    $end_date = date('Y-m-t', strtotime($start_date));
    
    $stmt = $pdo->prepare("
        SELECT id, provider_id, date, status, 
               TIME_FORMAT(start_time, '%H:%i') as start_time, 
               TIME_FORMAT(end_time, '%H:%i') as end_time
        FROM provider_availability 
        WHERE provider_id = ? AND date BETWEEN ? AND ?
        ORDER BY date ASC
    ");
    $stmt->execute([$provider_id, $start_date, $end_date]);
    $availability = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'availability' => $availability,
        'month' => $month,
        'year' => $year
    ]);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
