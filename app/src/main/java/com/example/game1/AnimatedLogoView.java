package com.example.game1;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class AnimatedLogoView extends View {

    private Paint gridPaint, xPaint, oPaint;
    private float gridProgress = 0f, xProgress = 0f, oProgress = 0f;

    public AnimatedLogoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Enable neon glow

        // White glowing grid
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#FFFFFF"));
        gridPaint.setStrokeWidth(15);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setShadowLayer(20, 0, 0, Color.parseColor("#FFFFFF"));

        // Cyan glowing X
        xPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xPaint.setColor(Color.parseColor("#00FFFF"));
        xPaint.setStrokeWidth(15);
        xPaint.setStyle(Paint.Style.STROKE);
        xPaint.setShadowLayer(20, 0, 0, Color.parseColor("#00FFFF"));

        // Magenta glowing O
        oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oPaint.setColor(Color.parseColor("#FF00FF"));
        oPaint.setStrokeWidth(15);
        oPaint.setStyle(Paint.Style.STROKE);
        oPaint.setShadowLayer(20, 0, 0, Color.parseColor("#FF00FF"));
    }

    // This method runs the animations in sequence
    public void startAnimation(Runnable onAnimationComplete) {
        // 1. Draw Grid
        ValueAnimator gridAnim = ValueAnimator.ofFloat(0f, 1f);
        gridAnim.setDuration(600);
        gridAnim.addUpdateListener(a -> { gridProgress = (float) a.getAnimatedValue(); invalidate(); });

        // 2. Draw Xs
        ValueAnimator xAnim = ValueAnimator.ofFloat(0f, 1f);
        xAnim.setDuration(500);
        xAnim.addUpdateListener(a -> { xProgress = (float) a.getAnimatedValue(); invalidate(); });

        // 3. Draw Os
        ValueAnimator oAnim = ValueAnimator.ofFloat(0f, 1f);
        oAnim.setDuration(500);
        oAnim.addUpdateListener(a -> { oProgress = (float) a.getAnimatedValue(); invalidate(); });

        // Play them one after the other
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(gridAnim, xAnim, oAnim);
        set.start();

        // Tell the main screen when the animation is completely finished
        postDelayed(onAnimationComplete, 1800);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float halfW = w / 2;
        float halfH = h / 2;
        float padding = w * 0.15f;

        // Draw 2x2 Grid (Expanding outwards from the center)
        canvas.drawLine(halfW, halfH - (halfH * gridProgress), halfW, halfH + (halfH * gridProgress), gridPaint);
        canvas.drawLine(halfW - (halfW * gridProgress), halfH, halfW + (halfW * gridProgress), halfH, gridPaint);

        // Draw Xs in Top-Left and Bottom-Right
        if (xProgress > 0) {
            drawX(canvas, 0, 0, halfW, halfH, padding, xProgress);
            drawX(canvas, halfW, halfH, w, h, padding, xProgress);
        }

        // Draw Os in Top-Right and Bottom-Left
        if (oProgress > 0) {
            drawO(canvas, halfW, 0, w, halfH, padding, oProgress);
            drawO(canvas, 0, halfH, halfW, h, padding, oProgress);
        }
    }

    private void drawX(Canvas canvas, float left, float top, float right, float bottom, float pad, float progress) {
        float startX = left + pad, startY = top + pad;
        float endX = right - pad, endY = bottom - pad;
        canvas.drawLine(startX, startY, startX + (endX - startX) * progress, startY + (endY - startY) * progress, xPaint);

        float startX2 = right - pad, startY2 = top + pad;
        float endX2 = left + pad, endY2 = bottom - pad;
        canvas.drawLine(startX2, startY2, startX2 + (endX2 - startX2) * progress, startY2 + (endY2 - startY2) * progress, xPaint);
    }

    private void drawO(Canvas canvas, float left, float top, float right, float bottom, float pad, float progress) {
        RectF rect = new RectF(left + pad, top + pad, right - pad, bottom - pad);
        canvas.drawArc(rect, -90, 360 * progress, false, oPaint);
    }
}