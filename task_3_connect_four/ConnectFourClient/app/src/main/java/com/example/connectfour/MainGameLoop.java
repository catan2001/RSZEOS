package com.example.connectfour;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainGameLoop extends AppCompatActivity implements ServerHandler {
    private static final int ROWS = 6;
    private static final int COLS = 7;

    private String playerUsername;
    private String opponentUsername;
    private String winOrLose = "Lost";

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
                // TODO: Add the logic that should say to the other player that the user exited myb call CLIENT_RESTART:0
                serverConnection.sendMessage("CLIENT_RESTART_GAME:" + 0);
                setResult(RESULT_OK);
                finish();
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
        attachGridListeners(); // attach listeners to all of the grid
    }

    // This method initializes the board so that it looks like
    // ConnectFour game, it also populates each cell with empty
    // circle.
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

    // For user to be able to click on the cell of the grid
    // it is necessary to attach setOnclickListener on each of the cells
    // It is used just once when the MainGameLoop starts and it MUST
    // go AFTER the initializeBoard since the initialize removes
    // all of the previous views if there are just in case
    private void attachGridListeners() {
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

    // Instead of doing InitializeBoard and again attachGridListeners
    // we can just change the image resource since the restart of the game
    // from the players perspective is just a reinitialization of the color
    // of cells in the grid.
    private void reInitializeBoard() {
        GridLayout grid = findViewById(R.id.gridLayout);

        for (int i = 0; i < grid.getChildCount(); i++) {
            ImageView cell = (ImageView) grid.getChildAt(i);
            cell.setImageResource(R.drawable.circle_empty);
        }
    }

    // Sends a message to the Server with a specific column
    // the user used.
    private void dropDisc(int col) {
        serverConnection.sendMessage("CLIENT_MOVE:" + col);
    }

    @Override
    public void handleServerMessage(String message) {
        runOnUiThread(() -> {
            // Checker if the message recieved is following the protocol
            if (!message.startsWith("SERVER")) {
                Log.d("MainGameLoop", "Invalid server message: " + message);
            } else if (message.startsWith("SERVER_GAME_RESTARTS")) {
                reInitializeBoard(); // Reinitialize the board. This is needed just to visually reset
            } else if (message.startsWith("SERVER_GAME_EXIT")) {  // go back to MainActivity
                setResult(RESULT_OK);
                finish();
            } else if (message.startsWith("SERVER_GAME_WON")) {
                handleGameStatus("Won");
            } else if (message.startsWith("SERVER_GAME_LOST")) {
                handleGameStatus("Lost");
            } else if (message.startsWith("SERVER_GAME_DRAW")) {
                handleGameStatus("Draw");
            } else if (message.startsWith("SERVER_GAME_INVALID_MOVE")) {    // if there is already populated spot
                Toast.makeText(this, "Invalid move! Try a different column.", Toast.LENGTH_SHORT).show();
            } else if (message.startsWith("SERVER_GAME_WAIT_FOR_OPPONENTS_MOVE")) {
                Toast.makeText(this, "It's your opponents move! Hold on.", Toast.LENGTH_SHORT).show();
            } else if (message.startsWith("SERVER_GAME_MOVE")) {
                handleGameMove(message);
            } else if (message.startsWith("SERVER_OPPONENT_DISCONNECTED")) {  // go back to MainActivity
                Toast.makeText(this, "Opponent has disconnected.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
            else {
                Toast.makeText(this, "Unknown server message: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGameStatus(String status) {
        Toast.makeText(this, "You " + status + "!", Toast.LENGTH_SHORT).show();
        winOrLose = status;
        askAnotherGame(opponentUsername);
    }
    private void askAnotherGame(String opponentPlayer) {
        new AlertDialog.Builder(this).setTitle("You " + winOrLose + " a Game!")
                .setMessage("Do you want to play another game with " + opponentPlayer + "?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    Toast.makeText(this, "Accepted. New Game should start soon", Toast.LENGTH_SHORT).show();
                    serverConnection.sendMessage("CLIENT_RESTART_GAME:" + 1);
                })
                .setNegativeButton("Reject", (dialog, which) -> {
                    Toast.makeText(this, "Rejected. Going back to lobby.", Toast.LENGTH_SHORT).show();
                    serverConnection.sendMessage("CLIENT_RESTART_GAME:" + 0);
                })
                .setCancelable(false)
                .show();
    }

    // After the opponent client creates a move, a Server first checks if the move
    // is valid, if it is valid the player recieves a message that an opponent has
    // created a move. This is then handled here
    private void handleGameMove(String message) {
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
}