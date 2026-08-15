package com.example.game1;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import java.util.ArrayList;
import java.util.Random;

public class ComputerActivity extends AppCompatActivity {

    private TextView textPlayer1, textPlayer2, textP1Score, textP2Score;
    private int p1Wins = 0, aiWins = 0;
    private View sliderPill;
    private TextView textEasy, textMedium, textHard;
    private int currentDifficulty = 0; // 0=Easy, 1=Medium, 2=Hard
    private CelebrationView celebrationView;
    private TicTacToeBoard boardView;
    private boolean playerTurn = true;
    private final int[][] board = new int[3][3];
    private TextView textStatus;
    private TextView textDifficultyDesc;
    private boolean gameActive = true;
    private final Handler gameHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_computer);

        // Initializing Views
        textPlayer1 = findViewById(R.id.textPlayer1);
        textPlayer2 = findViewById(R.id.textPlayer2);
        textP1Score = findViewById(R.id.textP1Score);
        textP2Score = findViewById(R.id.textP2Score);
        textStatus = findViewById(R.id.textStatus);
        sliderPill = findViewById(R.id.sliderPill);
        textEasy = findViewById(R.id.textEasy);
        textMedium = findViewById(R.id.textMedium);
        textHard = findViewById(R.id.textHard);
        textDifficultyDesc = findViewById(R.id.textDifficultyDesc);
        boardView = findViewById(R.id.boardView);
        celebrationView = findViewById(R.id.celebrationView);
        AppCompatButton btnReset = findViewById(R.id.btnReset);

        updateTurnUI();

        if (boardView != null) {
            boardView.setOnCellClickListener(this::onCellClicked);
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetGame());
        }

        if (textEasy != null) textEasy.setOnClickListener(v -> setDifficulty(0));
        if (textMedium != null) textMedium.setOnClickListener(v -> setDifficulty(1));
        if (textHard != null) textHard.setOnClickListener(v -> setDifficulty(2));
    }

    private void setDifficulty(int level) {
        if (currentDifficulty == level) return;
        currentDifficulty = level;

        if (sliderPill != null) {
            sliderPill.animate().translationX(level * sliderPill.getWidth()).setDuration(300).start();
        }

        if (textEasy != null) textEasy.setTextColor(level == 0 ? 0xFF050505 : 0xFFFFFFFF);
        if (textMedium != null) textMedium.setTextColor(level == 1 ? 0xFF050505 : 0xFFFFFFFF);
        if (textHard != null) textHard.setTextColor(level == 2 ? 0xFF050505 : 0xFFFFFFFF);

        if (textDifficultyDesc != null) {
            switch (level) {
                case 0:
                    textDifficultyDesc.setText(R.string.easy_desc);
                    break;
                case 1:
                    textDifficultyDesc.setText(R.string.medium_desc);
                    break;
                default:
                    textDifficultyDesc.setText(R.string.hard_desc);
                    break;
            }
        }

        resetGame();
    }

    private void onCellClicked(int row, int col) {
        if (!gameActive || board[row][col] != 0 || !playerTurn) return;

        makeMove(row, col, 1);

        if (checkWin(1)) {
            endGame("You Win!", 1);
            return;
        } else if (isBoardFull()) {
            endGame("It's a Draw!", 0);
            return;
        }

        playerTurn = false;
        updateTurnUI();

        gameHandler.postDelayed(this::computerMove, 500);
    }

    private void makeMove(int row, int col, int player) {
        board[row][col] = player;
        if (boardView != null) {
            boardView.setCell(row, col, player);
        }
    }

    private void computerMove() {
        if (!gameActive) return;

        int[] move;
        if (currentDifficulty == 0) move = getEasyMove();
        else if (currentDifficulty == 1) move = getMediumMove();
        else move = getHardMove();

        makeMove(move[0], move[1], 2);

        if (checkWin(2)) {
            endGame("AI Wins!", 2);
        } else if (isBoardFull()) {
            endGame("It's a Draw!", 0);
        } else {
            playerTurn = true;
            updateTurnUI();
        }
    }

    private int[] getEasyMove() {
        ArrayList<int[]> emptyCells = getEmptyCells();
        if (emptyCells.isEmpty()) return new int[]{0, 0};
        return emptyCells.get(new Random().nextInt(emptyCells.size()));
    }

    private int[] getMediumMove() {
        int[] winMove = findWinningMove(2);
        if (winMove != null) return winMove;
        int[] blockMove = findWinningMove(1);
        if (blockMove != null) return blockMove;
        return getEasyMove();
    }

    private int[] findWinningMove(int player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = player;
                    boolean wins = checkWin(player);
                    board[i][j] = 0;
                    if (wins) return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private int[] getHardMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] move = new int[2];
        boolean found = false;
        int i = 0;
        while (i < 3) {
            int j = 0;
            while (j < 3) {
                if (board[i][j] == 0) {
                    board[i][j] = 2;
                    int score = minimax(board, 0, false);
                    board[i][j] = 0;
                    if (score > bestScore) {
                        bestScore = score;
                        move[0] = i; move[1] = j;
                        found = true;
                    }
                }
                j++;
            }
            i++;
        }
        return found ? move : getEasyMove();
    }

    private int minimax(int[][] currentBoard, int depth, boolean isMaximizing) {
        if (checkWin(2)) return 10 - depth;
        if (checkWin(1)) return -10 + depth;
        if (isBoardFull()) return 0;

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (currentBoard[i][j] == 0) {
                    currentBoard[i][j] = isMaximizing ? 2 : 1;
                    int score = minimax(currentBoard, depth + 1, !isMaximizing);
                    currentBoard[i][j] = 0;
                    if (isMaximizing) bestScore = Math.max(score, bestScore);
                    else bestScore = Math.min(score, bestScore);
                }
            }
        }
        return bestScore;
    }

    private ArrayList<int[]> getEmptyCells() {
        ArrayList<int[]> cells = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) cells.add(new int[]{i, j});
            }
        }
        return cells;
    }

    private boolean checkWin(int player) {
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == player && board[i][1] == player && board[i][2] == player) ||
                    (board[0][i] == player && board[1][i] == player && board[2][i] == player)) return true;
        }
        return (board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
                (board[0][2] == player && board[1][1] == player && board[2][0] == player);
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

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
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
            textTitle.setScaleX(0.1f);
            textTitle.setScaleY(0.1f);
            textTitle.animate().scaleX(1.3f).scaleY(1.3f).setDuration(400).withEndAction(() ->
                    textTitle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
            ).start();
        }

        if (btnHome != null) btnHome.setOnClickListener(v -> { dialog.dismiss(); finish(); });
        if (btnPlayAgain != null) btnPlayAgain.setOnClickListener(v -> { dialog.dismiss(); resetGame(); });

        dialog.show();
    }

    private void endGame(String message, int player) {
        gameActive = false;
        if (textStatus != null) textStatus.setText(message);

        String glowColor = "#FFFFFF";
        if (player == 1) {
            glowColor = "#00FFFF";
            p1Wins++;
            if (textP1Score != null) textP1Score.setText(getString(R.string.wins_format, p1Wins));
        } else if (player == 2) {
            glowColor = "#FF00FF";
            aiWins++;
            if (textP2Score != null) textP2Score.setText(getString(R.string.wins_format, aiWins));
        }

        if (player != 0) {
            int[] line = checkWinLine(player);
            if (line != null && boardView != null) {
                boardView.drawWinningLine(line[0], line[1], line[2], line[3], player);
                if (player == 1 && celebrationView != null) celebrationView.startCelebration();
            }
        }

        String finalGlowColor = glowColor;
        gameHandler.postDelayed(() -> showWinnerDialog(message, finalGlowColor), 1000);
    }

    private void resetGame() {
        gameActive = true;
        playerTurn = true;
        gameHandler.removeCallbacksAndMessages(null);
        updateTurnUI();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }
        if (boardView != null) boardView.resetBoard();
        if (celebrationView != null) celebrationView.resetCelebration();
    }

    private void updateTurnUI() {
        if (textStatus != null) {
            if (playerTurn) {
                textStatus.setText(R.string.your_turn);
                textStatus.setTextColor(0xFF00FFFF);
                textStatus.setShadowLayer(15, 0, 0, 0xFF00FFFF);
            } else {
                textStatus.setText(R.string.ai_thinking);
                textStatus.setTextColor(0xFFFF00FF);
                textStatus.setShadowLayer(15, 0, 0, 0xFFFF00FF);
            }
        }
        animatePlayerTurn(textPlayer1, "#00FFFF", playerTurn);
        animatePlayerTurn(textPlayer2, "#FF00FF", !playerTurn);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.removeCallbacksAndMessages(null);
    }
}
