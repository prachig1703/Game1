package com.example.game1; // Make sure this matches your package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnOnline, btnComputer, btnLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize the Mobile Ads SDK
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {
        });


        // Initialize the buttons
        // Initialize the buttons
        btnOnline = findViewById(R.id.btnOnline);
        btnComputer = findViewById(R.id.btnComputer);
        btnLocal = findViewById(R.id.btnLocal);

        // 1. Play with Computer
        btnComputer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ComputerActivity.class);
                startActivity(intent);
            }
        });

        // 2. Global Multiplayer
        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OnlineMenuActivity.class);
                startActivity(intent);
            }
        });

        // 3. Play with Friend (Local)
        btnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocalActivity.class);
                startActivity(intent);
            }
        });
        // Find the Ad container and load a live advertisement
        com.google.android.gms.ads.AdView mAdView = findViewById(R.id.adView);
        com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}