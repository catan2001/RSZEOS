package com.example.connectfour;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainGameLoop extends AppCompatActivity implements ServerHandler {
    private static final int ROWS = 6;
    private static final int COLS = 7;

    private String playerUsername;
    private String opponentUsername;

    private ServerConnection serverConnection;
    private Thread connectionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game_loop);

        // Handle back presses with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Set result and finish the activity
                // Say the user is done playing, result goes back to lobby
                setResult(RESULT_OK);
                finish(); // Replaces super.onBackPressed()
            }
        });

        // Retrieve the data passed from startTheGame()
        Intent intent = getIntent();
        // We will use playerUsername and OpponentUsername to display on screen
        // and highlight which one is on the move
        int isPlayerOne = intent.getIntExtra("IS_PLAYER_ONE", 0); // TODO: use this one to highlight
        playerUsername = intent.getStringExtra("PLAYER_USERNAME");
        opponentUsername = intent.getStringExtra("OPPONENT_USERNAME");

        Log.d("MainGameLoop", "playerUsername: " + playerUsername);
        Log.d("MainGameLoop", "opponentUsername: " + opponentUsername);

        // TODO: do the same when going back from MainGameLoop to MainActivity.
        // retrieve data using singleton
        serverConnection = ConnectionManager.getInstance().getServerConnection();
        connectionThread = ConnectionManager.getInstance().getConnectionThread();

        // Change the handler in listener so that it uses
        // the one implemented in this class.
        serverConnection.setListenerHandler(this);
        initializeBoard();  // fill the grid and board

        // Set click listeners for each column
        GridLayout grid = findViewById(R.id.gridLayout);
        // Attach listener to all cells once
        for (int i = 0; i < grid.getChildCount(); i++) {
            final int index = i;
            grid.getChildAt(i).setOnClickListener(v -> {
                int col = index % COLS;
                dropDisc(col);
            });
        }
    }

    private void initializeBoard() {
        GridLayout grid = findViewById(R.id.gridLayout);
        grid.removeAllViews(); // clear previous views if any

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                ImageView cell = new ImageView(this);
                cell.setImageResource(R.drawable.circle_empty);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(r);
                params.columnSpec = GridLayout.spec(c);
                params.width = 120;
                params.height = 120;
                cell.setLayoutParams(params);
                grid.addView(cell);
            }
        }
    }

    private void dropDisc(int col) {
        serverConnection.sendMessage("CLIENT_MOVE:" + col);
    }

    @Override
    public void handleServerMessage(String message) {
        runOnUiThread(() -> {
            // Checker if the message recieved is following the protocol
            // SERVER - server message
            if (!message.startsWith("SERVER")) {
                // TODO: change this one to Logcat
                Toast.makeText(this, "Invalid server message: " + message, Toast.LENGTH_SHORT).show();
            } else if (message.startsWith("SERVER_GAME_RESTARTS")) {
                // Reinitialize the board. This is needed just to visually reset
                initializeBoard();
            } else if (message.startsWith("SERVER_GAME_WON")) {
                // TODO: handle when the player wins
                Toast.makeText(this, "You won!", Toast.LENGTH_SHORT).show();
            } else if (message.startsWith("SERVER_GAME_LOST")) {
                // TODO: handle when the player loses
                Toast.makeText(this, "You lost!", Toast.LENGTH_SHORT).show();
                // Ask a question to a user if he wants to play again
            } else if (message.startsWith("SERVER_GAME_INVALID_MOVE")) {
                // if there is already populated spot
                Toast.makeText(this, "Invalid move! Try a different column.", Toast.LENGTH_SHORT).show();
            } else if (message.startsWith("SERVER_GAME_WAIT_FOR_OPPONENTS_MOVE")) {
                Toast.makeText(this, "It's your opponents move! Hold on.", Toast.LENGTH_SHORT).show();
            } else if (message.startsWith("SERVER_GAME_MOVE")) {
                // After the opponent client creates a move, a Server first checks if the move
                // is valid, if it is valid the player recieves a message that an opponent has
                // created a move. This is then handled here
                String position = message.substring("SERVER_GAME_MOVE:".length()).trim();
                String[] parts = position.split(";");

                if (parts.length == 3) {
                    try {
                        int moveByPlayerOne = Integer.parseInt(parts[0]); // 1 if player one, 0 if player two
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);

                        // Update the visual side of the board
                        GridLayout grid = findViewById(R.id.gridLayout);
                        ImageView cell = (ImageView) grid.getChildAt(row * COLS + col);
                        if (moveByPlayerOne == 1) {
                            cell.setImageResource(R.drawable.circle_blue);
                        } else {
                            cell.setImageResource(R.drawable.circle_red);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid move data from server: " + position, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Malformed SERVER_GAME_MOVE: " + position, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "Unknown server message WHY: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void handleServerDisconnected() {
        runOnUiThread(() -> {
            // Optionally show a message to the user
            Toast.makeText(this, "Server disconnected. Returning to lobby.", Toast.LENGTH_SHORT).show();
            if (serverConnection != null) { serverConnection.close(); }
            if (connectionThread != null && connectionThread.isAlive()) { connectionThread.interrupt(); }
            // Finish this activity and return to the previous one
            setResult(RESULT_CANCELED); // TODO: ADD THIS TO A MAIN ACTIVITY
            finish();
        });
    }

    // This method is called on the exit
    // and it closes the connection and cleans
    // all of the garbage from threads.
    @Override protected void onDestroy() {
        super.onDestroy();
        if (serverConnection != null) { serverConnection.close(); }
        if (connectionThread != null && connectionThread.isAlive()) { connectionThread.interrupt(); }
    }
}