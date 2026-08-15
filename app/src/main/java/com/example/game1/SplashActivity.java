package com.example.game1;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AnimatedLogoView logoView = findViewById(R.id.logoView);
        TextView textLogo = findViewById(R.id.textLogo);
        AppCompatButton btnStart = findViewById(R.id.btnStart);

        // Calculate how far up the logo should move based on the phone's screen density
        float density = getResources().getDisplayMetrics().density;
        float moveUpDistance = -160 * density; // Moves up 160dp

        // SEQUENCE STEP 1: Start the drawing animation
        logoView.startAnimation(() -> {

            // SEQUENCE STEP 2: Shrink the logo and slide it up smoothly
            logoView.animate()
                    .scaleX(0.5f) // Shrink to 50% width
                    .scaleY(0.5f) // Shrink to 50% height
                    .translationY(moveUpDistance) // Slide up
                    .setDuration(800)
                    .withEndAction(() -> {

                        // SEQUENCE STEP 3: Fade in the Title Name
                        textLogo.animate()
                                .alpha(1f)
                                .setDuration(600)
                                .withEndAction(() -> {

                                    // SEQUENCE STEP 4: Fade in the Start Button
                                    btnStart.animate()
                                            .alpha(1f)
                                            .setDuration(600)
                                            .withEndAction(() -> {

                                                // SEQUENCE STEP 5: Start the continuous pulsing effect
                                                ObjectAnimator pulseAnim = ObjectAnimator.ofFloat(btnStart, "alpha", 0.6f, 1f);
                                                pulseAnim.setDuration(1000);
                                                pulseAnim.setRepeatCount(ValueAnimator.INFINITE);
                                                pulseAnim.setRepeatMode(ValueAnimator.REVERSE);
                                                pulseAnim.start();

                                            }).start();
                                }).start();
                    }).start();
        });

        // Handle the button click to start the game
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}