<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($data['provider_id']);

try {
    $stmt = $pdo->prepare("SELECT p.*, s.name as service_name, s.icon as service_icon FROM providers p JOIN services s ON p.service_id = s.id WHERE p.id = ?");
    $stmt->execute([$provider_id]);
    
    if ($stmt->rowCount() > 0) {
        $provider = $stmt->fetch(PDO::FETCH_ASSOC);
        unset($provider['password']);
        
        echo json_encode([
            'success' => true,
            'provider' => $provider
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Provider not found']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch profile: ' . $e->getMessage()]);
}
?>
