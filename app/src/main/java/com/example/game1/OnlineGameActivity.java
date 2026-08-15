package com.example.game1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OnlineGameActivity extends AppCompatActivity {

    private TicTacToeBoard boardView;
    private TextView textPlayer1, textPlayer2, textP1Score, textP2Score;

    private DatabaseReference gameRef;
    private ValueEventListener gameListener;

    private String roomID;
    private boolean isHost;
    private boolean isMyTurn = false;
    private boolean gameActive = true;

    private int p1Wins = 0, p2Wins = 0;
    private int[][] board = new int[3][3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // 1. Setup Fullscreen Window
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Reusing your awesome local layout!
            setContentView(R.layout.activity_local);

            // 2. Read Intents from Matchmaking
            roomID = getIntent().getStringExtra("roomID");
            isHost = getIntent().getBooleanExtra("isHost", false);
            isMyTurn = isHost; // Host always starts first

            // 3. Link UI Elements
            textPlayer1 = findViewById(R.id.textPlayer1);
            textPlayer2 = findViewById(R.id.textPlayer2);
            textP1Score = findViewById(R.id.textP1Score);
            textP2Score = findViewById(R.id.textP2Score);
            boardView = findViewById(R.id.boardView);

            // 4. Set Labels based on who is playing
            textPlayer1.setText(isHost ? "YOU (X)" : "OPPONENT (X)");
            textPlayer2.setText(isHost ? "OPPONENT (O)" : "YOU (O)");

            textP1Score.setText("Wins: 0");
            textP2Score.setText("Wins: 0");

            // 5. Connect to Firebase Room
            gameRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);

            // 6. Start the Game Engine
            boardView.setOnCellClickListener(this::onCellClicked);
            updateTurnGlow();
            listenForMoves();

        } catch (Exception e) {
            // BUG TRACKER: This prevents the silent bounce-back and shows exactly what crashed!
            new AlertDialog.Builder(this)
                    .setTitle("Game Crash Prevented")
                    .setMessage("Bug Tracker:\n\n" + e.toString() + "\n\nLine: " + e.getStackTrace()[0].toString())
                    .setPositiveButton("Exit", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            e.printStackTrace();
        }
    }

    private void onCellClicked(int row, int col) {
        // Stop if it's not your turn, game is over, or cell is taken
        if (!isMyTurn || !gameActive || board[row][col] != 0) return;

        int symbol = isHost ? 1 : 2; // 1 = X, 2 = O

        // 1. Update local board instantly so it feels fast
        board[row][col] = symbol;
        boardView.setCell(row, col, symbol);

        // 2. Upload move to Firebase for the other player
        String moveKey = row + "_" + col;
        gameRef.child("moves").child(moveKey).setValue(symbol);

        // 3. Pass turn control over the network
        gameRef.child("hostTurn").setValue(!isHost);
        isMyTurn = false;

        updateTurnGlow();
        checkGameState();
    }

    private void listenForMoves() {
        gameListener = gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!gameActive) return;

                // 1. Check whose turn it is across the network
                Boolean hostTurn = snapshot.child("hostTurn").getValue(Boolean.class);
                if (hostTurn != null) {
                    isMyTurn = (isHost && hostTurn) || (!isHost && !hostTurn);
                    updateTurnGlow();
                }

                // 2. Read enemy moves and draw them on your screen
                DataSnapshot movesSnapshot = snapshot.child("moves");
                for (DataSnapshot move : movesSnapshot.getChildren()) {
                    String[] coords = move.getKey().split("_");
                    int r = Integer.parseInt(coords[0]);
                    int c = Integer.parseInt(coords[1]);
                    int symbol = move.getValue(Integer.class);

                    // If a new move came in from the opponent, draw it!
                    if (board[r][c] == 0) {
                        board[r][c] = symbol;
                        boardView.setCell(r, c, symbol);
                        checkGameState();
                    }
                }

                // 3. Listen for reset commands from the opponent
                Boolean isReset = snapshot.child("reset").getValue(Boolean.class);
                if (isReset != null && isReset) {
                    resetBoardVisually();
                    gameRef.child("reset").removeValue(); // Clear the reset flag
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineGameActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTurnGlow() {
        // Dim the text of the player who is waiting
        textPlayer1.setAlpha(isHost == isMyTurn ? 1.0f : 0.4f);
        textPlayer2.setAlpha(isHost != isMyTurn ? 1.0f : 0.4f);
    }

    private void checkGameState() {
        // Check Rows, Columns, and Diagonals
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
                endGame(board[i][0]);
                return;
            }
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
                endGame(board[0][i]);
                return;
            }
        }
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            endGame(board[0][0]);
            return;
        }
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            endGame(board[0][2]);
            return;
        }

        // Check for Draw
        boolean isDraw = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) isDraw = false;
            }
        }
        if (isDraw) {
            endGame(0);
        }
    }

    private void endGame(int winnerSymbol) {
        gameActive = false;
        String message = "IT'S A DRAW!";

        if (winnerSymbol == 1) { // Host (X) Won
            if (isHost) {
                message = "YOU WON!";
                p1Wins++;
                textP1Score.setText("Wins: " + p1Wins);
            } else {
                message = "OPPONENT WON!";
                p1Wins++;
                textP2Score.setText("Wins: " + p1Wins);
            }
        } else if (winnerSymbol == 2) { // Guest (O) Won
            if (!isHost) {
                message = "YOU WON!";
                p2Wins++;
                textP1Score.setText("Wins: " + p2Wins);
            } else {
                message = "OPPONENT WON!";
                p2Wins++;
                textP2Score.setText("Wins: " + p2Wins);
            }
        }

        // Show a standard Android popup to restart or leave (Replace with your custom dialog later!)
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> triggerNetworkReset())
                .setNegativeButton("Leave Room", (dialog, which) -> finish())
                .show();
    }

    private void triggerNetworkReset() {
        // Tell Firebase to clear the moves and trigger a reset for both players
        gameRef.child("moves").removeValue();
        gameRef.child("reset").setValue(true);
    }

    private void resetBoardVisually() {
        gameActive = true;
        isMyTurn = isHost; // Host always starts new rounds

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }

        if (boardView != null) boardView.resetBoard();
        updateTurnGlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the connection when the player leaves
        if (gameRef != null && gameListener != null) {
            gameRef.removeEventListener(gameListener);
            // Optionally delete the room if the host leaves
            if (isHost) {
                gameRef.removeValue();
            }
        }
    }
}