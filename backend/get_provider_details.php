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
    $stmt = $pdo->prepare("SELECT p.*, s.name as service_name, s.icon as service_icon 
                           FROM providers p 
                           JOIN services s ON p.service_id = s.id 
                           WHERE p.id = ?");
    $stmt->execute([$provider_id]);
    
    if ($stmt->rowCount() > 0) {
        $provider = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Get reviews with user profile image
        $reviewStmt = $pdo->prepare("SELECT r.*, u.full_name as user_name, u.profile_image as user_image 
                                      FROM reviews r 
                                      JOIN users u ON r.user_id = u.id 
                                      WHERE r.provider_id = ? 
                                      ORDER BY r.created_at DESC 
                                      LIMIT 10");
        $reviewStmt->execute([$provider_id]);
        $reviews = $reviewStmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Get availability for the next 30 days
        $today = date('Y-m-d');
        $end_date = date('Y-m-d', strtotime('+30 days'));
        
        $availStmt = $pdo->prepare("
            SELECT id, date, status, 
                   TIME_FORMAT(start_time, '%H:%i') as start_time, 
                   TIME_FORMAT(end_time, '%H:%i') as end_time
            FROM provider_availability 
            WHERE provider_id = ? AND date BETWEEN ? AND ?
            ORDER BY date ASC
        ");
        $availStmt->execute([$provider_id, $today, $end_date]);
        $availability = $availStmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Get portfolio images
        $portfolioStmt = $pdo->prepare("SELECT id, image_url, description, created_at FROM provider_portfolio WHERE provider_id = ? ORDER BY created_at DESC");
        $portfolioStmt->execute([$provider_id]);
        $portfolio = $portfolioStmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode([
            'success' => true,
            'provider' => $provider,
            'reviews' => $reviews,
            'availability' => $availability,
            'portfolio' => $portfolio
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Provider not found']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to fetch provider: ' . $e->getMessage()]);
}
?>
