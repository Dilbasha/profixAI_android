import os
import sys

# --- SILENCE TENSORFLOW LOGS ---
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'  
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0' 

# Redirect standard error to null
sys.stderr = open(os.devnull, 'w')

import tensorflow as tf
import numpy as np

# --- CONFIGURATION ---
SERVICE_MAP = {
    "Cleaner": ["clean", "dust", "mop", "wash", "sweep", "garbage", "trash", "messy", "housekeeping","cleaner"],
    "Electrician": ["fan", "light", "bulb", "switch", "socket", "wire", "fuse", "mcb", "shock", "current", "power", "voltage","electrician"],
    "Painter": ["paint", "wall", "color", "whitewash", "stain", "brush", "roller", "exterior", "interior","painter"],
    "Salon": ["hair", "cut", "shave", "beard", "facial", "massage", "makeup", "beauty", "style", "grooming","hair stylist","salon"],
    "Carpenter": ["wood", "door", "window", "furniture", "table", "chair", "bed", "lock", "handle", "cupboard", "shelf","carpenter"],
    "Mechanic": ["car", "bike", "scooter", "vehicle", "engine", "brake", "clutch", "gear", "oil", "tire", "puncture", "start","mechanic"]
}

# 1. Get Input
if len(sys.argv) > 1:
    input_text = sys.argv[1].lower()
else:
    sys.exit()

# 2. STRATEGY A: Keyword Matching
detected_service = None
for service, keywords in SERVICE_MAP.items():
    for word in keywords:
        if word in input_text:
            detected_service = service
            break
    if detected_service:
        break

if detected_service:
    print(detected_service)
    sys.exit()

# 3. STRATEGY B: AI Fallback
try:
    # Load the model
    model = tf.keras.models.load_model('profix_brain_model') 
    
    predictions = model.predict([input_text], verbose=0)
    score = predictions[0] 

    # Threshold Check (Optional but recommended): 
    # If the model is not confident (e.g. 50/50 split), don't guess.
    # Assuming standard softmax: if highest score is < 0.6, it's unsure.
    if np.max(score) < 0.6:
        print("I am trained for Profix AI related services only")
    elif score[0] > score[1]:
        print("Mechanic")
    else:
        print("Electrician")

except Exception:
    # ⚠️ THIS IS THE CHANGE YOU REQUESTED ⚠️
    # If model fails, crashes, or file is missing:
    print("I am trained for Profix AI related services only")