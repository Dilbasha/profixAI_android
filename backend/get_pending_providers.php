<?php
require_once 'db.php';

try {
    $stmt = $pdo->query("SELECT p.*, s.name as service_name FROM providers p JOIN services s ON p.service_id = s.id WHERE p.verification_status = 'pending' ORDER BY p.created_at DESC");
    $providers = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'providers' => $providers
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch pending providers: ' . $e->getMessage()]);
}
?>
