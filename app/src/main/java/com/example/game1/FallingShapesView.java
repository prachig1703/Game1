package com.example.game1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class FallingShapesView extends View {

    private Paint paint;
    private Shape[] shapes;
    private Random random;
    private boolean isInitialized = false;

    // The neon colors for the background shapes
    private final String[] NEON_COLORS = {"#3300FFFF", "#33FF00FF", "#3339FF14"}; // The "33" adds transparency!

    public FallingShapesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        random = new Random();

        // Start the infinite animation loop
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000); // Speed of the loop doesn't matter, just keeps it ticking
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> {
            updateShapes();
            invalidate(); // Redraw every frame
        });
        animator.start();
    }

    private void updateShapes() {
        if (!isInitialized) return;
        for (Shape s : shapes) {
            s.y += s.speed;
            s.rotation += s.rotationSpeed;
            // If the shape falls off the bottom, reset it to the top
            if (s.y - s.size > getHeight()) {
                s.y = -s.size;
                s.x = random.nextFloat() * getWidth();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Create 25 random shapes when the screen size is calculated
        shapes = new Shape[25];
        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = new Shape(w, h);
        }
        isInitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInitialized) return;

        for (Shape s : shapes) {
            paint.setColor(Color.parseColor(s.colorHex));
            paint.setStrokeWidth(s.thickness);

            canvas.save();
            canvas.translate(s.x, s.y);
            canvas.rotate(s.rotation);

            if (s.isX) {
                // Draw X
                canvas.drawLine(-s.size, -s.size, s.size, s.size, paint);
                canvas.drawLine(s.size, -s.size, -s.size, s.size, paint);
            } else {
                // Draw O
                canvas.drawCircle(0, 0, s.size, paint);
            }
            canvas.restore();
        }
    }

    // Inner class to hold the data for each falling shape
    private class Shape {
        float x, y, speed, rotation, rotationSpeed, size, thickness;
        boolean isX;
        String colorHex;

        Shape(int screenWidth, int screenHeight) {
            this.x = random.nextFloat() * screenWidth;
            this.y = random.nextFloat() * screenHeight; // Start randomly scattered
            this.speed = 2f + random.nextFloat() * 5f; // Fall speed
            this.rotation = random.nextFloat() * 360;
            this.rotationSpeed = -2f + random.nextFloat() * 4f; // Spin speed
            this.size = 20f + random.nextFloat() * 40f;
            this.thickness = 3f + random.nextFloat() * 5f;
            this.isX = random.nextBoolean();
            this.colorHex = NEON_COLORS[random.nextInt(NEON_COLORS.length)];
        }
    }
}