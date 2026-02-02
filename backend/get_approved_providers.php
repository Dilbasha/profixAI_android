<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

try {
    // Get all approved/verified providers
    $stmt = $pdo->query("
        SELECT p.*, s.name as service_name 
        FROM providers p 
        LEFT JOIN services s ON p.service_id = s.id 
        WHERE p.is_verified = 1
        ORDER BY p.created_at DESC
    ");
    $providers = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'providers' => $providers
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error fetching approved providers: ' . $e->getMessage()
    ]);
}
?>
