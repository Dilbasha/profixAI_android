<?php
require_once 'db.php';

try {
    $stmt = $pdo->query("SELECT * FROM services WHERE is_active = TRUE ORDER BY name");
    $services = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'services' => $services
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch services: ' . $e->getMessage()]);
}
?>
