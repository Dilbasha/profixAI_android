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

// Debug: Log what we received
error_log("POST data: " . print_r($_POST, true));
error_log("FILES data: " . print_r($_FILES, true));

$provider_id = isset($_POST['provider_id']) ? intval($_POST['provider_id']) : 0;

if ($provider_id <= 0) {
    echo json_encode(['success' => false, 'message' => 'Provider ID is required. Received: ' . print_r($_POST, true)]);
    exit();
}

if (!isset($_FILES['image'])) {
    echo json_encode(['success' => false, 'message' => 'No image field in request']);
    exit();
}

if ($_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    $error_messages = [
        UPLOAD_ERR_INI_SIZE => 'File too large (php.ini limit)',
        UPLOAD_ERR_FORM_SIZE => 'File too large (form limit)',
        UPLOAD_ERR_PARTIAL => 'File partially uploaded',
        UPLOAD_ERR_NO_FILE => 'No file was uploaded',
        UPLOAD_ERR_NO_TMP_DIR => 'Missing temp folder',
        UPLOAD_ERR_CANT_WRITE => 'Failed to write to disk',
        UPLOAD_ERR_EXTENSION => 'Upload blocked by extension'
    ];
    $error_code = $_FILES['image']['error'];
    $message = isset($error_messages[$error_code]) ? $error_messages[$error_code] : 'Unknown error: ' . $error_code;
    echo json_encode(['success' => false, 'message' => $message]);
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
$upload_dir = __DIR__ . '/uploads/providers/';
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
$filename = 'provider_' . $provider_id . '_' . time() . '.' . $extension;
$filepath = $upload_dir . $filename;
$db_path = 'uploads/providers/' . $filename;

// Move uploaded file
if (move_uploaded_file($file['tmp_name'], $filepath)) {
    try {
        // Delete old image if exists
        $stmt = $pdo->prepare("SELECT profile_image FROM providers WHERE id = ?");
        $stmt->execute([$provider_id]);
        $old_image = $stmt->fetchColumn();
        
        if ($old_image && file_exists(__DIR__ . '/' . $old_image)) {
            unlink(__DIR__ . '/' . $old_image);
        }
        
        // Update database with new image path
        $stmt = $pdo->prepare("UPDATE providers SET profile_image = ? WHERE id = ?");
        $stmt->execute([$db_path, $provider_id]);
        
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
