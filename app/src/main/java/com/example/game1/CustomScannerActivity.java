package com.example.game1;

import com.journeyapps.barcodescanner.CaptureActivity;

// We are literally just creating an empty wrapper class.
// This forces Android to treat the scanner as a part of YOUR app's memory space,
// rather than a separate system camera app, which stops the Samsung crash!
public class CustomScannerActivity extends CaptureActivity {
    // Leave this completely empty. The magic happens just by it existing.
}