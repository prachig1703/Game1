package com.example.game1;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.UUID;

public class QRConnectActivity extends AppCompatActivity {

    private ImageView imageQR;
    private TextView textNoQR;
    private String myRoomID = "";

    // This handles the result when the camera finishes scanning
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() == null) {
            Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show();
        } else {
            String scannedRoomID = result.getContents();
            Toast.makeText(this, "Connected to Room: " + scannedRoomID, Toast.LENGTH_LONG).show();

            // TODO: In the future, this is where you load the Game Board and sync with the database!
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide Status Bar
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_qr_connect);

        imageQR = findViewById(R.id.imageQR);
        textNoQR = findViewById(R.id.textNoQR);
        AppCompatButton btnGenerateQR = findViewById(R.id.btnGenerateQR);
        AppCompatButton btnScanQR = findViewById(R.id.btnScanQR);

        btnGenerateQR.setOnClickListener(v -> generateQR());
        btnScanQR.setOnClickListener(v -> startCameraScanner());
    }

    private void generateQR() {
        try {
            // 1. Create a unique, random ID for this match
            myRoomID = "NEON_ROOM_" + UUID.randomUUID().toString().substring(0, 8);

            // 2. Turn that ID into a QR Code image
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(myRoomID, BarcodeFormat.QR_CODE, 400, 400);

            // 3. Display it on the glowing screen
            imageQR.setImageBitmap(bitmap);
            textNoQR.setVisibility(View.GONE);

            Toast.makeText(this, "Room Created! Have friend scan code.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCameraScanner() {
        // Configure the sleek Android scanner
        ScanOptions options = new ScanOptions();
        options.setPrompt("Center QR Code to Join Match");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);

        // Launch the camera!
        barcodeLauncher.launch(options);
    }
}