<?php
// Notification helper functions

function createNotification($pdo, $user_id, $provider_id, $type, $title, $message, $booking_id = null) {
    try {
        $stmt = $pdo->prepare("
            INSERT INTO notifications (user_id, provider_id, type, title, message, related_booking_id)
            VALUES (?, ?, ?, ?, ?, ?)
        ");
        $stmt->execute([
            $user_id ?: null,
            $provider_id ?: null,
            $type,
            $title,
            $message,
            $booking_id
        ]);
        return true;
    } catch (PDOException $e) {
        error_log("Notification error: " . $e->getMessage());
        return false;
    }
}

// Notification types and their corresponding icons/colors
// Types: booking_created, booking_accepted, booking_rejected, booking_started, booking_completed, booking_cancelled, payment_due, rating_request, provider_arrival
?>
