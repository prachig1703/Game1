package com.example.game1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TicTacToeBoard extends View {

    private final Paint sparkPaint;
    private final Paint gridPaint;
    private final Paint xPaint;
    private final Paint oPaint;
    private float gridAnimProgress = 0f;
    private final float[][] cellAnimProgress = new float[3][3];
    private int[][] board = new int[3][3];
    private int cellSize;
    private final Paint winLinePaint;
    private boolean isWinner = false;
    private int winStartRow, winStartCol, winEndRow, winEndCol;
    private float winLineProgress = 0f;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width;
        if (size == 0) {
            size = height;
        }
        if (size == 0) size = 600; // Fallback size if both are zero
        setMeasuredDimension(size, size);
    }

    public interface OnCellClickListener {
        void onCellClick(int row, int col);
    }
    private OnCellClickListener listener;

    public TicTacToeBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Required to make the glowing shadow effect render correctly
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Glowing Grid Lines
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#4B0082")); // Dark Indigo
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(12);
        gridPaint.setShadowLayer(20, 0, 0, Color.parseColor("#8A2BE2"));

        // Glowing X (Cyan)
        xPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xPaint.setColor(Color.parseColor("#00FFFF"));
        xPaint.setStyle(Paint.Style.STROKE);
        xPaint.setStrokeWidth(18);
        xPaint.setStrokeCap(Paint.Cap.ROUND);
        xPaint.setShadowLayer(25, 0, 0, Color.parseColor("#00FFFF"));

        // Glowing O (Pink)
        oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oPaint.setColor(Color.parseColor("#FF00FF"));
        oPaint.setStyle(Paint.Style.STROKE);
        oPaint.setStrokeWidth(18);
        oPaint.setStrokeCap(Paint.Cap.ROUND);
        oPaint.setShadowLayer(25, 0, 0, Color.parseColor("#FF00FF"));

        // Glowing Winning Line
        winLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        winLinePaint.setStrokeWidth(12);
        winLinePaint.setStrokeCap(Paint.Cap.ROUND);
        winLinePaint.setStyle(Paint.Style.STROKE);

        // Sparkle Paint
        sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparkPaint.setStyle(Paint.Style.FILL);

        animateGrid();
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.listener = listener;
    }

    // Called by ComputerActivity to place a move and trigger animation
    public void setCell(int row, int col, int player) {
        board[row][col] = player;
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(400); // Speed of the X/O drawing animation
        anim.addUpdateListener(a -> {
            cellAnimProgress[row][col] = (float) a.getAnimatedValue();
            invalidate(); // Redraws the screen
        });
        anim.start();
    }

    public void resetBoard() {
        board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            java.util.Arrays.fill(cellAnimProgress[i], 0f);
        }
        isWinner = false;
        winLineProgress = 0f;
        animateGrid(); // Re-draw the board lines fastly
    }

    public void drawWinningLine(int startRow, int startCol, int endRow, int endCol, int player) {
        isWinner = true;
        winStartRow = startRow;
        winStartCol = startCol;
        winEndRow = endRow;
        winEndCol = endCol;

        int color = player == 1 ? android.graphics.Color.parseColor("#00FFFF") : android.graphics.Color.parseColor("#FF00FF");
        winLinePaint.setColor(color);
        winLinePaint.setShadowLayer(30, 0, 0, color);

        // Also set the spark color to match the winning player!
        sparkPaint.setColor(color);
        sparkPaint.setShadowLayer(20, 0, 0, color);

        // Animate the line drawing over 600 milliseconds for a better cinematic effect
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.addUpdateListener(a -> {
            winLineProgress = (float) a.getAnimatedValue();
            invalidate(); // Redraws the board to update the line and sparks
        });
        animator.start();
    }

    private void animateGrid() {
        gridAnimProgress = 0f;
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(800); // Speed of the grid drawing
        anim.addUpdateListener(a -> {
            gridAnimProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.start();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = w / 3;
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull android.graphics.Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();

        // 1. Draw Animated Grid Lines
        float drawLength = width * gridAnimProgress;
        canvas.drawLine(cellSize, 0, cellSize, drawLength, gridPaint); // Vert 1
        canvas.drawLine(cellSize * 2, 0, cellSize * 2, drawLength, gridPaint); // Vert 2
        canvas.drawLine(0, cellSize, drawLength, cellSize, gridPaint); // Horiz 1
        canvas.drawLine(0, cellSize * 2, drawLength, cellSize * 2, gridPaint); // Horiz 2

        // 2. Draw X's and O's
        for (int r = 0; r < 3; r++) {
            int c = 0;
            while (c < 3) {
                if (board[r][c] != 0) {
                    drawSymbol(canvas, r, c, board[r][c], cellAnimProgress[r][c]);
                }
                c++;
            }
        }

        // 3. Draw Winning Line if exists
        // Draw Winning Line and Spark Celebration
        // Draw Winning Line (Clean Version)
        if (isWinner) {
            float startX = (winStartCol * cellSize) + (cellSize / 2f);
            float startY = (winStartRow * cellSize) + (cellSize / 2f);
            float endX = (winEndCol * cellSize) + (cellSize / 2f);
            float endY = (winEndRow * cellSize) + (cellSize / 2f);

            // Calculate the current tip of the line based on the animation progress
            float currentEndX = startX + (endX - startX) * winLineProgress;
            float currentEndY = startY + (endY - startY) * winLineProgress;

            canvas.drawLine(startX, startY, currentEndX, currentEndY, winLinePaint);
        }
    }

    private void drawSymbol(Canvas canvas, int row, int col, int player, float progress) {
        int padding = (int) (cellSize * 0.25); // Keeps symbols from touching the grid
        int left = col * cellSize + padding;
        int top = row * cellSize + padding;
        int right = (col + 1) * cellSize - padding;
        int bottom = (row + 1) * cellSize - padding;

        if (player == 2) { // Draw O
            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawArc(rect, -90, 360 * progress, false, oPaint);
        } else if (player == 1) { // Draw X
            float p1 = Math.min(progress * 2, 1f);
            float p2 = Math.max((progress - 0.5f) * 2, 0f);

            // First diagonal line
            canvas.drawLine(left, top, left + (right - left) * p1, top + (bottom - top) * p1, xPaint);
            // Second diagonal line
            if (p2 > 0) {
                canvas.drawLine(right, top, right - (right - left) * p2, top + (bottom - top) * p2, xPaint);
            }
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (cellSize == 0) return true;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);
            if (col >= 0 && col < 3 && row >= 0 && row < 3 && listener != null) {
                performClick();
                listener.onCellClick(row, col);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
