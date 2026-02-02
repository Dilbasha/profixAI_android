<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['email']) || !isset($data['password'])) {
    echo json_encode(['success' => false, 'message' => 'Email and password are required']);
    exit();
}

$email = trim($data['email']);
$password = $data['password'];

try {
    $stmt = $pdo->prepare("SELECT p.*, s.name as service_name FROM providers p JOIN services s ON p.service_id = s.id WHERE p.email = ? AND p.password = ?");
    $stmt->execute([$email, $password]);
    
    if ($stmt->rowCount() > 0) {
        $provider = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($provider['verification_status'] === 'pending') {
            echo json_encode(['success' => false, 'message' => 'Your account is pending approval. Please wait for admin verification.']);
            exit();
        }
        
        if ($provider['verification_status'] === 'rejected') {
            $rejectionReason = $provider['rejection_reason'] ?? 'No reason provided';
            echo json_encode([
                'success' => false, 
                'message' => 'Your account has been rejected. Reason: ' . $rejectionReason
            ]);
            exit();
        }
        
        echo json_encode([
            'success' => true,
            'message' => 'Login successful',
            'provider' => [
                'id' => $provider['id'],
                'full_name' => $provider['full_name'],
                'email' => $provider['email'],
                'phone' => $provider['phone'],
                'service_id' => $provider['service_id'],
                'service_name' => $provider['service_name'],
                'hourly_rate' => $provider['hourly_rate'],
                'experience_years' => $provider['experience_years'],
                'rating' => $provider['rating'],
                'total_jobs' => $provider['total_jobs'],
                'is_available' => $provider['is_available'],
                'profile_image' => $provider['profile_image']
            ]
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Login failed: ' . $e->getMessage()]);
}
?>
