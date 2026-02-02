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

if (!isset($data['portfolio_id']) || !isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Portfolio ID and Provider ID are required']);
    exit();
}

$portfolio_id = intval($data['portfolio_id']);
$provider_id = intval($data['provider_id']);

try {
    // First get the image URL to delete the file
    $stmt = $pdo->prepare("SELECT image_url FROM provider_portfolio WHERE id = ? AND provider_id = ?");
    $stmt->execute([$portfolio_id, $provider_id]);
    $portfolio_item = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$portfolio_item) {
        echo json_encode(['success' => false, 'message' => 'Portfolio item not found or unauthorized']);
        exit();
    }
    
    // Delete from database
    $deleteStmt = $pdo->prepare("DELETE FROM provider_portfolio WHERE id = ? AND provider_id = ?");
    $deleteStmt->execute([$portfolio_id, $provider_id]);
    
    // Delete the file
    if (file_exists($portfolio_item['image_url'])) {
        unlink($portfolio_item['image_url']);
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Portfolio image deleted successfully'
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to delete portfolio item: ' . $e->getMessage()]);
}
?>
