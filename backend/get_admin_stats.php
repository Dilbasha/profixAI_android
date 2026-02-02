<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

try {
    // Get total users count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM users");
    $totalUsers = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get total providers count (all)
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM providers");
    $totalProviders = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get pending providers count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM providers WHERE verification_status = 'pending'");
    $pendingProviders = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get approved providers count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM providers WHERE is_verified = 1");
    $approvedProviders = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get total bookings count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM bookings");
    $totalBookings = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get completed bookings count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM bookings WHERE status = 'completed'");
    $completedBookings = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get pending bookings count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM bookings WHERE status = 'pending'");
    $pendingBookings = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get in-progress bookings count
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM bookings WHERE status IN ('accepted', 'in_progress')");
    $activeBookings = $stmt->fetch(PDO::FETCH_ASSOC)['count'];
    
    // Get total revenue from completed bookings
    $stmt = $pdo->query("SELECT COALESCE(SUM(total_amount), 0) as total FROM bookings WHERE status = 'completed'");
    $totalRevenue = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Get total hours worked (estimated_hours from completed bookings)
    $stmt = $pdo->query("SELECT COALESCE(SUM(estimated_hours), 0) as total FROM bookings WHERE status = 'completed'");
    $totalHoursWorked = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Get recent bookings for activity feed (last 5)
    $stmt = $pdo->query("
        SELECT b.id, b.status, b.booking_date, b.total_amount, 
               u.full_name as user_name, p.full_name as provider_name, s.name as service_name
        FROM bookings b
        LEFT JOIN users u ON b.user_id = u.id
        LEFT JOIN providers p ON b.provider_id = p.id
        LEFT JOIN services s ON b.service_id = s.id
        ORDER BY b.created_at DESC
        LIMIT 5
    ");
    $recentActivity = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'stats' => [
            'total_users' => (int)$totalUsers,
            'total_providers' => (int)$totalProviders,
            'pending_providers' => (int)$pendingProviders,
            'approved_providers' => (int)$approvedProviders,
            'total_bookings' => (int)$totalBookings,
            'completed_bookings' => (int)$completedBookings,
            'pending_bookings' => (int)$pendingBookings,
            'active_bookings' => (int)$activeBookings,
            'total_revenue' => (float)$totalRevenue,
            'total_hours_worked' => (float)$totalHoursWorked
        ],
        'recent_activity' => $recentActivity
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error fetching admin stats: ' . $e->getMessage()
    ]);
}
?>
