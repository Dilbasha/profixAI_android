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

// Check for provider_id
if (!isset($_POST['provider_id'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required']);
    exit();
}

$provider_id = intval($_POST['provider_id']);
$description = isset($_POST['description']) ? $_POST['description'] : '';

// Check if file was uploaded
if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(['success' => false, 'message' => 'No image uploaded or upload error']);
    exit();
}

$file = $_FILES['image'];
$allowed_types = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg', 'application/octet-stream'];
$allowed_extensions = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
$max_size = 10 * 1024 * 1024; // 10MB

// Get file extension
$extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));

// Validate by MIME type OR extension (Android sometimes sends wrong MIME)
$valid_type = in_array($file['type'], $allowed_types) || in_array($extension, $allowed_extensions);

if (!$valid_type) {
    echo json_encode(['success' => false, 'message' => 'Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed. Received: ' . $file['type']]);
    exit();
}

// Validate file size
if ($file['size'] > $max_size) {
    echo json_encode(['success' => false, 'message' => 'File too large. Maximum 10MB allowed']);
    exit();
}

// Create upload directory if it doesn't exist
$upload_dir = 'uploads/portfolio/';
if (!file_exists($upload_dir)) {
    mkdir($upload_dir, 0777, true);
}

// Generate unique filename (use existing $extension from validation, default to jpg if empty)
if (empty($extension)) {
    $extension = 'jpg';
}
$filename = 'portfolio_' . $provider_id . '_' . time() . '_' . uniqid() . '.' . $extension;
$filepath = $upload_dir . $filename;

// Move uploaded file
if (!move_uploaded_file($file['tmp_name'], $filepath)) {
    echo json_encode(['success' => false, 'message' => 'Failed to save the image']);
    exit();
}

try {
    // Insert into database
    $stmt = $pdo->prepare("INSERT INTO provider_portfolio (provider_id, image_url, description) VALUES (?, ?, ?)");
    $stmt->execute([$provider_id, $filepath, $description]);
    
    $portfolio_id = $pdo->lastInsertId();
    
    echo json_encode([
        'success' => true,
        'message' => 'Portfolio image uploaded successfully',
        'portfolio_item' => [
            'id' => intval($portfolio_id),
            'image_url' => $filepath,
            'description' => $description
        ]
    ]);
} catch (PDOException $e) {
    // Delete uploaded file if database insert fails
    if (file_exists($filepath)) {
        unlink($filepath);
    }
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
