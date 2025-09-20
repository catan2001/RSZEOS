package com.example.connectfour;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Main activity for the Connect Four client application.
 *
 * <p>This activity provides the user interface for connecting to a game server,
 * displaying the list of connected players, and handling messages received
 * from the server.</p>
 *
 * <p>This class focuses only on user interaction and UI updates.
 * All networking details (socket, streams, listening loop) are delegated
 * to {@link ServerConnection} and {@link ServerListener}.</p>
 */
public class MainActivity extends AppCompatActivity implements ServerHandler {

    private ListView listView;
    private Button buttonConnectToServer;
    private Button buttonRequestUser;
    private EditText editTextPort, editTextUsername, editTextServerIp;

    private ArrayList<String> players;
    private ArrayAdapter<String> adapter;

    private ServerConnection serverConnection;
    private Thread connectionThread;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupWindowInsets();
        setupButtonListeners();
        initializePlayerList();
    }

    private void initViews() {
        listView = findViewById(R.id.listViewPlayers);
        buttonConnectToServer = findViewById(R.id.buttonConnect);
        buttonRequestUser = findViewById(R.id.buttonRequestUser);
        editTextPort = findViewById(R.id.editTextPort);
        editTextServerIp = findViewById(R.id.editTextServerIp);
        editTextUsername = findViewById(R.id.editTextUsername);

        // We need to disable this since it is not used until the player
        // connects to the Server. Otherwise, the player could accidentally send
        // null.
        buttonRequestUser.setEnabled(false);
        listView.setEnabled(false);
        players = new ArrayList<>();

        // Create a specific ArrayAdapter that makes the list selectable but a single choice.
        // This is also necessary to be changed in AndroidManifest.xml
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, players);
        listView.setAdapter(adapter);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // This method uses the Lambda functions to create a listener which connects
    // the button click with the specific methods related to it.
    private void setupButtonListeners() {
        buttonConnectToServer.setOnClickListener(view -> connectToServer());
        buttonRequestUser.setOnClickListener(view -> sendRequestToServer());
    }

    // This method is responsible for connecting to the Server. A player
    // requests to connect to server by typing username, ip address and
    // port number of the Server. The app checks if this is empty or not
    // and uses this information to connect to server.
    private void connectToServer() {
        username = editTextUsername.getText().toString().trim();
        String serverIp = editTextServerIp.getText().toString().trim();
        String portText = editTextPort.getText().toString().trim();

        if (username.isEmpty() || serverIp.isEmpty() || portText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidIp(serverIp)) {
            Toast.makeText(this, "Invalid IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
            return;
        }

        // It is necessary to run the connection in the new thread since it is a blocking operation
        // The ServerConnection method called here in it's constructor tries to connect to a specific
        // server blocking the main UI thread. Due to this it is necessary to wrap the construction
        // around the new thread.
        connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // create a new server connection, if not in new thread -> then it block the main UI thread
                    serverConnection = new ServerConnection(serverIp, port, MainActivity.this);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }, "Connection-Thread");

        connectionThread.start();
    }

    private boolean isValidIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false; // not a number
            }
        }
        return true;
    }


    // After the player connects to the Server and the Server updates
    // the list of active players ready to play the game,
    // the player can select one of the players and send it a
    // request to start a new game. The player2 should recieve a message
    // from the Server saying that a player1 requests a new game and it
    // should be able to accept or decline the request. The player1
    // recieves the respond from the Server (player2).
    private void sendRequestToServer() {
        int position = listView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            String selectedPlayer = players.get(position);
            serverConnection.sendMessage("CLIENT_REQUEST_GAME:" + selectedPlayer);
            Toast.makeText(this, "Request sent to " + selectedPlayer, Toast.LENGTH_SHORT).show();

            // re-enable after 2 seconds (or after server response)
            buttonRequestUser.postDelayed(() -> buttonRequestUser.setEnabled(true), 2000);
        } else {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayerList() {
        players.clear();
        adapter.notifyDataSetChanged();
    }

    // List of active players is connected via adapter to players. To update you just need
    // to call this method after you update players list.
    private void updatePlayerList() {
        adapter.notifyDataSetChanged();
    }

    // Main hangleServerMessage method that is an implementation for this main acrivity. Similar to this there is
    // handleServerMessage for games activity. Since it handles Server message there is a standardized
    // way of communication, the message must start with SERVER if it is comming from the server,
    // and CLIENT if it comes from the client. This is a way of handling any potential bugs regarding
    // the communication protocol
    @Override
    public void handleServerMessage(String message) {
        runOnUiThread(() -> {
            // Checker if the message recieved is following the protocol
            // SERVER - server message
            if (!message.startsWith("SERVER")) {
                Log.d("MainActivity", "Invalid server message: " + message);
            } else if (message.startsWith("SERVER_LOGIN_OK")) {
                handleLogin(message);
            } else if (message.startsWith("SERVER_LOGIN_FAILED_CONNECTION_ISSUE")) {
                handleFailedLoginCI(message);
            } else if (message.startsWith("SERVER_LOGIN_FAILED_USERNAME_ISSUE")) {
                handleFailedUsername();
            } else if (message.startsWith("SERVER_PLAYER_LIST:")) {
                handlePlayerList(message);
            } else if (message.startsWith("SERVER_PLAYER_JOINED:")) {
                handlePlayerJoined(message);
            } else if (message.startsWith("SERVER_PLAYER_LEFT:")) {
                handlePlayerLeft(message);
            } else if (message.startsWith("SERVER_PLAYER_STARTED_GAME:")) {
                handlePlayerStartedGame(message);
            } else if (message.startsWith("SERVER_PLAYER_ACCEPTED")) {
                handlePlayerAcceptedGame(message);
            } else if (message.startsWith("SERVER_PLAYER_REJECTED")) {
                handlePlayerRejectedGame(message);
            } else if (message.startsWith("SERVER_INCOMING_REQUEST")) {
                handleIncomingRequest(message);
            } else if (message.startsWith("SERVER_PLAYER_AVAILABLE:")) {
                handlePlayerAvailable(message);
            } else if (message.startsWith("SERVER_PLAYER_NOT_AVAILABLE")) {
                handlePlayerNotAvailable(message);
            } else if (message.startsWith("SERVER_REQUEST_CANCELLED")) {
                handleRequestCancelled(message);
            } else if (message.startsWith("SERVER_GAME_READY")) {
                handleGameReady(message);
            } else {
                Toast.makeText(this, "MainActivity: Unknown server message: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Enable user to send a request to other players after login
    private void handleLogin(String message) {
        buttonRequestUser.setEnabled(true);
        listView.setEnabled(true);

        // Send username with a clear tag, This is fine because sendMessage
        // creates it's own thread for sending the message which avoids
        // the conflict of running on same UI thread.
        serverConnection.sendMessage("CLIENT_USERNAME:" + username);
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
    }

    private void handleFailedLoginCI(String message) {
        // If there is some error after connecting to the server,
        // the server sends this message to notify the player.
        buttonRequestUser.setEnabled(false);
        listView.setEnabled(false);
        Toast.makeText(this, "Login failed: Connection issue", Toast.LENGTH_LONG).show();
    }

    private void handleFailedUsername() {
        // self explanatory.
        buttonRequestUser.setEnabled(false);
        listView.setEnabled(false);
        Toast.makeText(this, "Login failed: Username is already used", Toast.LENGTH_LONG).show();
    }

    private void handlePlayerList(String message) {
        // Replace the whole list of players everytime a player logs
        // after this the server is sending a message if there are some log changes
        String listStr = message.substring("SERVER_PLAYER_LIST:".length()).trim();
        players.clear();
        if (!listStr.isEmpty()) {
            String[] playerArray = listStr.split(",");
            for (String player : playerArray) { players.add(player.trim()); }
        }
        updatePlayerList();
    }

    private void handlePlayerJoined(String message) {
        // After the player connects and receives the full list of all players,
        // the server sends it a message if a new player joins the server.
        // The app then updates the list of all users that are ready to play.
        String newPlayer = message.substring("SERVER_PLAYER_JOINED:".length()).trim();
        if (!players.contains(newPlayer)) {
            players.add(newPlayer);
            updatePlayerList();
        }
    }

    private void handlePlayerLeft(String message) {
        // After the player connects and receives the full list of all players,
        // the server sends it a message if some player leaves the server.
        // The app then updates the list of all users that are ready to play.
        String leftPlayer = message.substring("SERVER_PLAYER_LEFT:".length()).trim();
        players.remove(leftPlayer);
        updatePlayerList();
    }

    private void handlePlayerStartedGame(String message) {
        // After the player connects and receives the full list of all players,
        // the server sends it a message if some player starts a game.
        // The app then updates the list of all users that are ready to play.
        // TODO: what if a player has started a game and then inside of it the player
        // TODO: disconnects from the server? There would be double remove
        String busyPlayer = message.substring("SERVER_PLAYER_STARTED_GAME:".length()).trim();
        players.remove(busyPlayer);
        updatePlayerList();
        Toast.makeText(this, busyPlayer + " started a game.", Toast.LENGTH_SHORT).show();
    }

    private void handlePlayerAcceptedGame(String message) {
        // This one may be unnecessary since the current player will not see the list and update
        // but for now lets use it nonetheless.
        String acceptedPlayer = message.substring("SERVER_PLAYER_ACCEPTED".length()).trim();
        players.remove(acceptedPlayer);
        updatePlayerList();
        Toast.makeText(this, acceptedPlayer + " accepted your request!", Toast.LENGTH_LONG).show();
    }

    private void handlePlayerRejectedGame(String message) {
        // self explanatory
        String rejectedPlayer = message.substring("SERVER_PLAYER_REJECTED".length()).trim();
        Toast.makeText(this, rejectedPlayer + " rejected your request.", Toast.LENGTH_LONG).show();
        // clear the choice user selected, this is better because it forces the player to reach out again
        listView.clearChoices();
        adapter.notifyDataSetChanged(); // eliminates the problem with highlight
    }

    private void handleIncomingRequest(String message) {
        // ping the current player that someone is requesting to start a game
        String requestPlayer = message.substring("SERVER_INCOMING_REQUEST:".length()).trim();
        showIncomingRequestDialog(requestPlayer);
    }

    private void handlePlayerNotAvailable(String message) {
        // ping the current player that the requested player is not available
        // but maybe this one is necessary since the user will only interact
        // using the app and not from a terminal...
        String requestedPlayer = message.substring("SERVER_PLAYER_NOT_AVAILABLE:".length()).trim();
        Toast.makeText(this, requestedPlayer + " -> player you tried to reach is not available", Toast.LENGTH_SHORT).show();
    }

    private void handleRequestCancelled(String message) {
        // ping the current player that even if he accepts the request by the user
        String requestPlayer = message.substring("SERVER_REQUEST_CANCELLED:".length()).trim();
        Toast.makeText(this, requestPlayer + " -> player that reached out to you is not available", Toast.LENGTH_SHORT).show();
    }

    private void handleGameReady(String message) {
        String payload = message.substring("SERVER_GAME_READY:".length()).trim();
        String[] parts = payload.split(";");
        int isPlayerOne = Integer.parseInt(parts[0]);
        String opponentUsername = parts[1];
        startTheGame(isPlayerOne, opponentUsername);
    }

    private void handlePlayerAvailable(String message) {
        // When the player finishes playing, the Server gets a message and sends
        // to all of the users this message to add again this user into available players.
        String availablePlayer = message.substring("SERVER_PLAYER_AVAILABLE:".length()).trim();
        if (!players.contains(availablePlayer)) {
            players.add(availablePlayer);
            updatePlayerList();
        }
    }

    // This is a small helper method that creates a new object that
    // represents a small dialog that user can interact with to accept or decline
    // request to a new game from other user
    private void showIncomingRequestDialog(String requestPlayer) {
        // TODO: what happens if a user that sent the request disconnects?
        new AlertDialog.Builder(this).setTitle("Incoming Game Request")
                .setMessage(requestPlayer + " wants to play with you. Accept?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    serverConnection.sendMessage("CLIENT_ACCEPT_GAME:" + requestPlayer);
                    Toast.makeText(this, "Accepted request from " + requestPlayer, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Reject", (dialog, which) -> {
                    serverConnection.sendMessage("CLIENT_REJECT_GAME:" + requestPlayer);
                    Toast.makeText(this, "Rejected request from " + requestPlayer, Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    // This one is called when both players are ready to play
    private void startTheGame(int isPlayerOne, String opponentUsername) {
        // When you send this the Server should respond with SERVER_GAME_READY if the
        // other player is still in the lobby, if not then it returns SERVER_REQUEST_CANCELLED.
        // This is a handshake method. It also creates a singleton and initialize so that
        // next activity can use this the serverConnection.
        ConnectionManager.getInstance().setConnection(serverConnection, connectionThread);
        Intent intent = new Intent(this, MainGameLoop.class);
        intent.putExtra("IS_PLAYER_ONE", isPlayerOne); // this is used if the
        intent.putExtra("PLAYER_USERNAME", username); // we use this to display the username of the player
        intent.putExtra("OPPONENT_USERNAME", opponentUsername); // we use this to display the username of the opponent

        // If you want to wait for a result later
        activityLauncher.launch(intent);
    }

    // This creates an activity launcher for starting another activity (MainGameLoop will be called)
    // and also it sets up the callback mechanism when the game activity finishes and returns
    // control back to the lobby
    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            // a callback lambda method that runs when the launched activity
            // returns. It basically sets back the handler of the listener to be the one
            // implemented in this class and notifies all other users that the player is
            // available again
            result -> {
                // This block runs when MainGameLoop finishes
                if (result.getResultCode() == RESULT_OK) {
                    // Change the handler in listener so that it uses
                    // the one implemented in this class.
                    serverConnection.setListenerHandler(this);
                    // Send a message back to the server that we are free
                    serverConnection.sendMessage("CLIENT_PLAYER_AVAILABLE:" + username);
                    // also immediately re-enable the request button & list
                    buttonRequestUser.setEnabled(true);
                    listView.setEnabled(true);
                    Toast.makeText(this, "You are back to the Lobby again.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Overrides/implements the handling method handleServerDisconnected which is used
    // when the server disconnects. This method restores the app to it's previous state if
    // the current app is running from MainActivity - lobby)
    @Override
    public void handleServerDisconnected() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Disconnected from server", Toast.LENGTH_LONG).show();
            buttonRequestUser.setEnabled(false);
            listView.setEnabled(false);
            players.clear();
            adapter.notifyDataSetChanged();
        });
    }

    // This method is called on the exit and it closes the connection and cleans
    // all of the garbage from threads.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverConnection != null) {
            serverConnection.close();
        }
        if (connectionThread != null && connectionThread.isAlive()) {
            connectionThread.interrupt();
        }
    }
}