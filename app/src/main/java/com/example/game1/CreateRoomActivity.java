package com.example.game1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.UUID;

public class CreateRoomActivity extends AppCompatActivity {

    private DatabaseReference roomRef;
    private ValueEventListener playerJoinListener;
    private String myRoomID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_room);

        ImageView imageQR = findViewById(R.id.imageQR);

        try {
            // 1. Generate unique short Room ID
            myRoomID = "NEON_" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

            // 2. Generate QR Code image
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(myRoomID, BarcodeFormat.QR_CODE, 500, 500);
            imageQR.setImageBitmap(bitmap);

            // 3. Register Room inside the Firebase cloud database
            roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(myRoomID);
            roomRef.child("status").setValue("waiting");
            roomRef.child("hostTurn").setValue(true); // Host plays first (X)

            // 4. Listen for the Guest to scan the code and join
            playerJoinListener = roomRef.child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status = snapshot.getValue(String.class);
                    if (status != null && status.equals("playing")) {
                        // Guest has joined! Remove listener and start online game
                        roomRef.child("status").removeEventListener(playerJoinListener);

                        Intent intent = new Intent(CreateRoomActivity.this, OnlineGameActivity.class);
                        intent.putExtra("roomID", myRoomID);
                        intent.putExtra("isHost", true); // You are player 1
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks if the user leaves the screen before anyone joins
        if (roomRef != null && playerJoinListener != null) {
            roomRef.child("status").removeEventListener(playerJoinListener);
        }
    }
}