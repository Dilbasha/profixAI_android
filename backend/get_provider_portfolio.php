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

if (!isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($data['provider_id']);

try {
    $stmt = $pdo->prepare("SELECT id, image_url, description, created_at FROM provider_portfolio WHERE provider_id = ? ORDER BY created_at DESC");
    $stmt->execute([$provider_id]);
    $portfolio = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'portfolio' => $portfolio
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch portfolio: ' . $e->getMessage()]);
}
?>
