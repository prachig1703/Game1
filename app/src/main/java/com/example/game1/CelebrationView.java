package com.example.game1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CelebrationView extends View {

    private Paint paint;
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private boolean isAnimating = false;

    // Neon Colors
    private final String[] COLORS = {"#00FFFF", "#FF00FF", "#39FF14", "#FFFF00"};

    public CelebrationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
    }

    public void startCelebration() {
        particles.clear();
        int width = getWidth();
        int height = getHeight();

        // Spawn 40 particles from the Left side
        for (int i = 0; i < 40; i++) {
            particles.add(new Particle(0, height / 2f, true));
        }
        // Spawn 40 particles from the Right side
        for (int i = 0; i < 40; i++) {
            particles.add(new Particle(width, height / 2f, false));
        }

        isAnimating = true;

        // Run the physics engine for 1.5 seconds
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500);
        animator.addUpdateListener(a -> {
            updateParticles();
            invalidate();
        });
        animator.start();
    }
    public void resetCelebration(){
        particles.clear(); // this'll delete the foreworks
        isAnimating = false;// reset the animation if celebration
        invalidate();// force the screen to redraw board
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 1.2f; // Gravity pulling them down
            p.alpha -= 4; // Fade out over time
            if (p.alpha < 0) p.alpha = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnimating) return;

        for (Particle p : particles) {
            if (p.alpha > 0) {
                paint.setColor(Color.parseColor(p.colorHex));
                paint.setAlpha(p.alpha);
                paint.setShadowLayer(15, 0, 0, Color.parseColor(p.colorHex));
                canvas.drawCircle(p.x, p.y, p.radius, paint);
            }
        }
    }

    // Inner class holding the physics math for each firework spark
    private class Particle {
        float x, y, vx, vy, radius;
        int alpha = 255;
        String colorHex;

        Particle(float startX, float startY, boolean fromLeft) {
            this.x = startX;
            this.y = startY + (random.nextFloat() * 200 - 100); // Randomize vertical start point slightly

            // Shoot inward and upward
            this.vx = fromLeft ? (random.nextFloat() * 15 + 10) : -(random.nextFloat() * 15 + 10);
            this.vy = -(random.nextFloat() * 25 + 15);

            this.radius = random.nextFloat() * 12 + 6;
            this.colorHex = COLORS[random.nextInt(COLORS.length)];
        }
    }
}