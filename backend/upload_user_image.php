<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';
// Handle file upload for user profile image
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit();
}

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit();
}

if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(['success' => false, 'message' => 'No image uploaded or upload error']);
    exit();
}

$file = $_FILES['image'];
$allowed_types = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp', 'application/octet-stream'];
$max_size = 5 * 1024 * 1024; // 5MB

// Validate file size
if ($file['size'] > $max_size) {
    echo json_encode(['success' => false, 'message' => 'File too large. Maximum 5MB allowed']);
    exit();
}

// Create uploads directory if not exists
$upload_dir = __DIR__ . '/uploads/users/';
if (!file_exists($upload_dir)) {
    if (!mkdir($upload_dir, 0777, true)) {
        echo json_encode(['success' => false, 'message' => 'Failed to create upload directory']);
        exit();
    }
}

// Generate unique filename
$extension = pathinfo($file['name'], PATHINFO_EXTENSION);
if (empty($extension)) {
    $extension = 'jpg';
}
$filename = 'user_' . $user_id . '_' . time() . '.' . $extension;
$filepath = $upload_dir . $filename;
$db_path = 'uploads/users/' . $filename;

// Move uploaded file
if (move_uploaded_file($file['tmp_name'], $filepath)) {
    try {
        // Delete old image if exists
        $stmt = $pdo->prepare("SELECT profile_image FROM users WHERE id = ?");
        $stmt->execute([$user_id]);
        $old_image = $stmt->fetchColumn();
        
        if ($old_image && file_exists(__DIR__ . '/' . $old_image)) {
            unlink(__DIR__ . '/' . $old_image);
        }
        
        // Update database with new image path
        $stmt = $pdo->prepare("UPDATE users SET profile_image = ? WHERE id = ?");
        $stmt->execute([$db_path, $user_id]);
        
        echo json_encode([
            'success' => true,
            'message' => 'Image uploaded successfully',
            'image_url' => $db_path
        ]);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to save image. Check directory permissions.']);
}
?>
