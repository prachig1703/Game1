package com.example.game1;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LocalActivity extends AppCompatActivity {

    private TextView textP1Score, textP2Score;
    private int p1Wins = 0, p2Wins = 0;
    private CelebrationView celebrationView;
    private TicTacToeBoard boardView;
    private boolean playerXTurn = true; // True = Player 1 (X), False = Player 2 (O)
    private final int[][] board = new int[3][3];
    private boolean gameActive = true;
    private TextView textPlayer1, textPlayer2;
    private final Handler gameHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_local);

        textP1Score = findViewById(R.id.textP1Score);
        textP2Score = findViewById(R.id.textP2Score);
        celebrationView = findViewById(R.id.celebrationView);
        textPlayer1 = findViewById(R.id.textPlayer1);
        textPlayer2 = findViewById(R.id.textPlayer2);
        boardView = findViewById(R.id.boardView);
        AppCompatButton btnReset = findViewById(R.id.btnReset);

        if (boardView != null) {
            boardView.setOnCellClickListener(this::onCellClicked);
        }
        
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetGame());
        }

        updateTurnUI();
    }

    private void updateTurnUI() {
        animatePlayerTurn(textPlayer1, "#00FFFF", playerXTurn);
        animatePlayerTurn(textPlayer2, "#FF00FF", !playerXTurn);
    }

    private void animatePlayerTurn(TextView textView, String neonColor, boolean isActive) {
        if (textView == null) return;
        float targetScale = isActive ? 1.15f : 0.9f;
        int targetTextColor = isActive ? android.graphics.Color.parseColor(neonColor) : android.graphics.Color.parseColor("#444444");
        float targetGlow = isActive ? 20f : 0f;

        textView.animate().scaleX(targetScale).scaleY(targetScale).setDuration(300).start();
        textView.setTextColor(targetTextColor);

        android.animation.ValueAnimator glowAnim = android.animation.ValueAnimator.ofFloat(textView.getShadowRadius(), targetGlow);
        glowAnim.setDuration(300);
        glowAnim.addUpdateListener(animator -> textView.setShadowLayer((float) animator.getAnimatedValue(), 0, 0, android.graphics.Color.parseColor(neonColor)));
        glowAnim.start();
    }

    private void onCellClicked(int row, int col) {
        if (!gameActive || board[row][col] != 0) return;

        int currentPlayer = playerXTurn ? 1 : 2;
        board[row][col] = currentPlayer;
        if (boardView != null) {
            boardView.setCell(row, col, currentPlayer);
        }

        if (checkWin(currentPlayer)) {
            String winMessage = playerXTurn ? "Player 1 Wins!" : "Player 2 Wins!";
            endGame(winMessage, currentPlayer);
            return;
        } else if (isBoardFull()) {
            endGame("It's a Draw!", 0);
            return;
        }

        playerXTurn = !playerXTurn;
        updateTurnUI();
    }

    private int[] checkWinLine(int player) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return new int[]{i, 0, i, 2};
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return new int[]{0, i, 2, i};
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return new int[]{0, 0, 2, 2};
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return new int[]{0, 2, 2, 0};
        return null;
    }

    private boolean checkWin(int player) {
        return checkWinLine(player) != null;
    }

    private boolean isBoardFull() {
        int i = 0;
        while (i < 3) {
            int j = 0;
            while (j < 3) {
                if (board[i][j] == 0) return false;
                j++;
            }
            i++;
        }
        return true;
    }

    private void endGame(String message, int player) {
        gameActive = false;
        String glowColor = "#FFFFFF";

        if (player == 1) {
            glowColor = "#00FFFF";
            p1Wins++;
            if (textP1Score != null) textP1Score.setText(getString(R.string.wins_format, p1Wins));
        } else if (player == 2) {
            glowColor = "#FF00FF";
            p2Wins++;
            if (textP2Score != null) textP2Score.setText(getString(R.string.wins_format, p2Wins));
        }

        if (player != 0) {
            int[] line = checkWinLine(player);
            if (line != null && boardView != null) {
                boardView.drawWinningLine(line[0], line[1], line[2], line[3], player);
                if (celebrationView != null) celebrationView.startCelebration();
            }
        }

        String finalGlowColor = glowColor;
        gameHandler.postDelayed(() -> showWinnerDialog(message, finalGlowColor), 1000);
    }

    private void showWinnerDialog(String message, String glowColor) {
        if (isFinishing()) return;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_winner);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        TextView textTitle = dialog.findViewById(R.id.textWinTitle);
        AppCompatButton btnHome = dialog.findViewById(R.id.btnHome);
        AppCompatButton btnPlayAgain = dialog.findViewById(R.id.btnPlayAgain);

        if (textTitle != null) {
            textTitle.setText(message);
            textTitle.setShadowLayer(25, 0, 0, android.graphics.Color.parseColor(glowColor));
            textTitle.setScaleX(0.1f); textTitle.setScaleY(0.1f);
            textTitle.animate().scaleX(1.3f).scaleY(1.3f).setDuration(400).withEndAction(() ->
                    textTitle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
            ).start();
        }

        if (btnHome != null) btnHome.setOnClickListener(v -> { dialog.dismiss(); finish(); });
        if (btnPlayAgain != null) btnPlayAgain.setOnClickListener(v -> { dialog.dismiss(); resetGame(); });

        dialog.show();
    }

    private void resetGame() {
        gameActive = true;
        playerXTurn = true;
        gameHandler.removeCallbacksAndMessages(null);
        updateTurnUI();
        // Clear the internal memory board
        int i = 0;
        while (i < 3) {
            int j = 0;
            while (j < 3) {
                board[i][j] = 0;
                j++;
            }
            i++;
        }
        if (boardView != null) boardView.resetBoard();
        if (celebrationView != null) celebrationView.resetCelebration();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.removeCallbacksAndMessages(null);
    }
}
