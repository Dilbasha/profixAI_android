<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['full_name']) || !isset($data['email']) || !isset($data['phone']) || !isset($data['password'])) {
    echo json_encode(['success' => false, 'message' => 'All fields are required']);
    exit();
}

$full_name = trim($data['full_name']);
$email = trim($data['email']);
$phone = trim($data['phone']);
$password = $data['password'];
$address = isset($data['address']) ? trim($data['address']) : '';
$city = isset($data['city']) ? trim($data['city']) : '';
$pincode = isset($data['pincode']) ? trim($data['pincode']) : '';

// Check if email already exists
$stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
$stmt->execute([$email]);
if ($stmt->rowCount() > 0) {
    echo json_encode(['success' => false, 'message' => 'Email already registered']);
    exit();
}

// Check if phone already exists
$stmt = $pdo->prepare("SELECT id FROM users WHERE phone = ?");
$stmt->execute([$phone]);
if ($stmt->rowCount() > 0) {
    echo json_encode(['success' => false, 'message' => 'Phone number already registered']);
    exit();
}

try {
    $stmt = $pdo->prepare("INSERT INTO users (full_name, email, phone, password, address, city, pincode) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([$full_name, $email, $phone, $password, $address, $city, $pincode]);
    
    $userId = $pdo->lastInsertId();
    
    echo json_encode([
        'success' => true,
        'message' => 'Registration successful',
        'user' => [
            'id' => $userId,
            'full_name' => $full_name,
            'email' => $email,
            'phone' => $phone
        ]
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Registration failed: ' . $e->getMessage()]);
}
?>
