<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

$service_id = isset($data['service_id']) ? intval($data['service_id']) : null;
$city = isset($data['city']) ? trim($data['city']) : null;

try {
    // Check if honor_score column exists
    $columnCheck = $pdo->query("SHOW COLUMNS FROM providers LIKE 'honor_score'");
    $hasHonorScore = $columnCheck->rowCount() > 0;
    
    $sql = "SELECT p.*, s.name as service_name, s.icon as service_icon 
            FROM providers p 
            JOIN services s ON p.service_id = s.id 
            WHERE p.verification_status = 'verified' AND p.is_available = TRUE";
    $params = [];
    
    if ($service_id) {
        $sql .= " AND p.service_id = ?";
        $params[] = $service_id;
    }
    
    if ($city) {
        $sql .= " AND p.city LIKE ?";
        $params[] = "%$city%";
    }
    
    // Use honor_score only if column exists
    if ($hasHonorScore) {
        $sql .= " ORDER BY IFNULL(p.honor_score, 0) DESC, p.rating DESC, p.total_jobs DESC";
    } else {
        $sql .= " ORDER BY p.rating DESC, p.total_jobs DESC";
    }
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $providers = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'providers' => $providers
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch providers: ' . $e->getMessage()]);
}
?>
