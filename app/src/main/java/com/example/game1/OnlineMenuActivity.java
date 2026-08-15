package com.example.game1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class OnlineMenuActivity extends AppCompatActivity {

    // This handles the result when the scanner finishes
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        try {
            if(result.getContents() == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String scannedRoomID = result.getContents();
                Toast.makeText(this, "Connecting to: " + scannedRoomID, Toast.LENGTH_SHORT).show();

                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(scannedRoomID);

                roomRef.child("status").get().addOnCompleteListener(task -> {
                    try {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // Success! Connect to the game
                            roomRef.child("status").setValue("playing");
                            Intent intent = new Intent(OnlineMenuActivity.this, OnlineGameActivity.class);
                            intent.putExtra("roomID", scannedRoomID);
                            intent.putExtra("isHost", false);
                            startActivity(intent);
                        } else if (!task.isSuccessful()) {
                            Toast.makeText(this, "Firebase Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Room not found. Did the host close it?", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception innerE) {
                        Toast.makeText(this, "Launch Error: " + innerE.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Scanner Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_online_menu);
         com.google.firebase.FirebaseApp.initializeApp(this);

        AppCompatButton btnCreateRoom = findViewById(R.id.btnCreateRoom);
        AppCompatButton btnJoinRoom = findViewById(R.id.btnJoinRoom);

        // 1. Create Room (Goes to QR Screen)
        btnCreateRoom.setOnClickListener(v -> {
            Intent intent = new Intent(OnlineMenuActivity.this, CreateRoomActivity.class);
            startActivity(intent);
        });

        // 2. Join Room (Instantly opens the camera scanner)
        btnJoinRoom.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan Host's QR Code to Join");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);

            options.setCaptureActivity(CustomScannerActivity.class);

            barcodeLauncher.launch(options);
        });
    }
}