<?php
// 1. Set Header to JSON (so Android understands the response)
header('Content-Type: application/json');

// 2. Check if the request is a POST request
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    // 3. Get the raw input text from Android
    $input_text = "";
    if (isset($_POST['text'])) {
        $input_text = $_POST['text'];
    } else {
        // Fallback: Read raw JSON body
        $json = file_get_contents('php://input');
        $data = json_decode($json, true);
        if (isset($data['text'])) {
            $input_text = $data['text'];
        }
    }

    // 4. Validate input
    if (!empty($input_text)) {
        
        // 5. Sanitize for Security
        $safe_text = escapeshellarg($input_text);

        // 6. Run the Python Script (USING YOUR SPECIFIC 64-BIT PATH)
        // ⚠️ We use the full path to avoid "Python not found" or "Permission" errors.
        // Note: Double backslashes (\\) are required in PHP strings for Windows paths.
        
        $python_path = "http://14.139.187.229:8081/oct/spic_730/profixai/classify.py";

        // We wrap the path in quotes \" just in case there are spaces, and add 2>&1 to capture errors
        $command = "\"$python_path\" classify.py $safe_text 2>&1";
        
        $output = shell_exec($command);

        // 7. Clean up the output
        // 7. Clean up the output
        $output = trim($output);
        
        // Split the output by new lines and take ONLY the last line
        $lines = explode("\n", $output);
        $intent = trim(end($lines));

        // 8. Send Response back to Android
        // Check if Python reported a crash/error
        if (strpos($intent, "Traceback") !== false || strpos($intent, "Error") !== false) {
             echo json_encode(array(
                "status" => "error", 
                "intent" => "unknown",
                "debug_message" => $intent // This helps us see WHY it crashed if it does
            ));
        } else {
            // Success! Send the detected service (e.g., "Electrician")
            echo json_encode(array(
                "status" => "success", 
                "intent" => $intent
            ));
        }

    } else {
        echo json_encode(array("status" => "error", "message" => "No text provided"));
    }

} else {
    echo json_encode(array("status" => "error", "message" => "Invalid Request Method"));
}
?>