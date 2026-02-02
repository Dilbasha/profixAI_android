<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['provider_id']) || !isset($data['action'])) {
    echo json_encode(['success' => false, 'message' => 'Provider ID and action are required']);
    exit();
}

$provider_id = intval($data['provider_id']);
$action = $data['action'];
$rejection_reason = isset($data['rejection_reason']) ? trim($data['rejection_reason']) : null;

if (!in_array($action, ['approve', 'reject'])) {
    echo json_encode(['success' => false, 'message' => 'Invalid action']);
    exit();
}

// If rejecting, require a reason
if ($action === 'reject' && empty($rejection_reason)) {
    echo json_encode(['success' => false, 'message' => 'Rejection reason is required']);
    exit();
}

try {
    $verification_status = ($action === 'approve') ? 'verified' : 'rejected';
    $is_verified = ($action === 'approve') ? 1 : 0;
    
    if ($action === 'reject') {
        $stmt = $pdo->prepare("UPDATE providers SET verification_status = ?, is_verified = ?, rejection_reason = ?, rejected_at = NOW() WHERE id = ?");
        $stmt->execute([$verification_status, $is_verified, $rejection_reason, $provider_id]);
    } else {
        $stmt = $pdo->prepare("UPDATE providers SET verification_status = ?, is_verified = ?, rejection_reason = NULL WHERE id = ?");
        $stmt->execute([$verification_status, $is_verified, $provider_id]);
    }
    
    if ($stmt->rowCount() > 0) {
        echo json_encode([
            'success' => true,
            'message' => 'Provider ' . ($action === 'approve' ? 'approved' : 'rejected') . ' successfully'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Provider not found']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Action failed: ' . $e->getMessage()]);
}
?>
