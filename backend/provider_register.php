<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['full_name']) || !isset($data['email']) || !isset($data['phone']) || 
    !isset($data['password']) || !isset($data['service_id']) || !isset($data['hourly_rate'])) {
    echo json_encode(['success' => false, 'message' => 'All required fields must be provided']);
    exit();
}

$full_name = trim($data['full_name']);
$email = trim($data['email']);
$phone = trim($data['phone']);
$password = $data['password'];
$service_id = intval($data['service_id']);
$hourly_rate = floatval($data['hourly_rate']);
$experience_years = isset($data['experience_years']) ? intval($data['experience_years']) : 0;
$description = isset($data['description']) ? trim($data['description']) : '';
$address = isset($data['address']) ? trim($data['address']) : '';
$city = isset($data['city']) ? trim($data['city']) : '';
$pincode = isset($data['pincode']) ? trim($data['pincode']) : '';
$aadhaar = isset($data['aadhaar']) ? trim($data['aadhaar']) : '';

// Check if email already exists
$stmt = $pdo->prepare("SELECT id FROM providers WHERE email = ?");
$stmt->execute([$email]);
if ($stmt->rowCount() > 0) {
    echo json_encode(['success' => false, 'message' => 'Email already registered']);
    exit();
}

// Check if phone already exists
$stmt = $pdo->prepare("SELECT id FROM providers WHERE phone = ?");
$stmt->execute([$phone]);
if ($stmt->rowCount() > 0) {
    echo json_encode(['success' => false, 'message' => 'Phone number already registered']);
    exit();
}

try {
    $stmt = $pdo->prepare("INSERT INTO providers (full_name, email, phone, password, service_id, hourly_rate, experience_years, description, address, city, pincode, aadhaar) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([$full_name, $email, $phone, $password, $service_id, $hourly_rate, $experience_years, $description, $address, $city, $pincode, $aadhaar]);
    
    $providerId = $pdo->lastInsertId();
    
    echo json_encode([
        'success' => true,
        'message' => 'Registration successful. Please wait for admin approval.',
        'provider' => [
            'id' => $providerId,
            'full_name' => $full_name,
            'email' => $email,
            'verification_status' => 'pending'
        ]
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Registration failed: ' . $e->getMessage()]);
}
?>
