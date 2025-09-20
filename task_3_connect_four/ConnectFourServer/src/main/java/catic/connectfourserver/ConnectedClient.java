/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 * Client connection handler for Connect Four server
 * Handles communication with a single client
 */
package catic.connectfourserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a single client connection to the Connect Four server Handles login,
 * messaging, and game coordination
 *
 * @author catic
 */
public class ConnectedClient implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ConnectedClient.class.getName());

    // Connection components
    private final Socket socket;
    private final ConnectFourServer server;
    private BufferedReader reader;
    private PrintWriter writer;

    // Client state
    private String username;
    private volatile boolean running = true; // used to check if the user is alive

    /* Protocol message constants */
    // For MainActivity:
    private static final String CLIENT_USERNAME_PREFIX = "CLIENT_USERNAME:";
    private static final String CLIENT_PLAYER_AVAILABLE_PREFIX = "CLIENT_PLAYER_AVAILABLE:";
    private static final String CLIENT_REQUEST_GAME_PREFIX = "CLIENT_REQUEST_GAME:";
    private static final String CLIENT_ACCEPT_GAME_PREFIX = "CLIENT_ACCEPT_GAME:";
    private static final String CLIENT_REJECT_GAME_PREFIX = "CLIENT_REJECT_GAME:";
    
    // In game for MainGameLoop
    private static final String CLIENT_MOVE_PREFIX = "CLIENT_MOVE:";
    private static final String CLIENT_RESTART_GAME_PREFIX = "CLIENT_RESTART_GAME:";

    public ConnectedClient(Socket socket, ConnectFourServer server) {
        this.socket = socket;
        this.server = server;
        this.username = ""; // Set after login

        try {
            this.reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to set up client streams", ex);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sends a message to this client
     *
     * @param message the message to send
     */
    public void sendMessage(String message) {
        writer.println(message);
    }

    @Override
    public void run() {
        try {
            handleLogin(); // First step: get the client logged in
            handleClientMessages(); // Then process their messages
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Client connection lost: " + username, e);
        } finally {
            try {
                disconnect(); // Clean up when done
            } catch (IOException ex) {
                System.getLogger(ConnectedClient.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            GameSession gameSession = server.getGameSessionForPlayer(this.username);
            if (gameSession != null) {
                gameSession.handleDisconnect(this);
                server.removeGameSession(gameSession);
            }
        }
    }

    private void handleLogin() throws IOException {
        // Welcome the client
        writer.println("SERVER_LOGIN_OK");
        String line = reader.readLine();
        LOGGER.log(Level.INFO, "Login attempt: {0}", line);
        if (line != null && line.startsWith(CLIENT_USERNAME_PREFIX)) {
            username = line.substring(CLIENT_USERNAME_PREFIX.length()).trim();
        } else {
            // Fallback if no proper username received
            username = "Guest" + socket.getPort();
        }
        LOGGER.log(Level.INFO, "User logged in: {0}", username);
        // Let everyone know about the new player
        server.broadcastToAll("SERVER_PLAYER_JOINED:", username);
        sendPlayerList();
    }

    private void sendPlayerList() {
        StringBuilder playerList = new StringBuilder();
        String separator = "";

        for (ConnectedClient client : server.getClients()) {
            String clientUsername = client.getUsername();
            if (!clientUsername.isEmpty() && !clientUsername.equals(username)) {
                playerList.append(separator).append(clientUsername);
                separator = ","; // Only add commas after the first item
            }
        }

        writer.println("SERVER_PLAYER_LIST:" + playerList.toString());
    }

    /**
     * Main message handling loop - processes commands from the client
     */
    private void handleClientMessages() throws IOException {
        String command;
        while (running && (command = reader.readLine()) != null) {
            LOGGER.log(Level.INFO, "Received from {0}: {1}", new Object[]{username, command});

            if (command.startsWith(CLIENT_REQUEST_GAME_PREFIX)) {
                handleGameRequest(command);
            } else if (command.startsWith(CLIENT_ACCEPT_GAME_PREFIX)) {
                handleGameAcceptance(command);
            } else if (command.startsWith(CLIENT_REJECT_GAME_PREFIX)) {
                handleGameRejection(command);
            } else if (command.startsWith(CLIENT_PLAYER_AVAILABLE_PREFIX)) {
                handlePlayerAvailable();
            } else if (command.startsWith(CLIENT_RESTART_GAME_PREFIX)) {
                handleGameRestart(command); 
            } else if (command.startsWith(CLIENT_MOVE_PREFIX)) {
                handleGameMove(command); 
            } else if (command.equalsIgnoreCase("EXIT")) {
                handleExit();
                break; // Exit the loop
            } else {
                sendMessage("SERVER_UNKNOWN_COMMAND");
            }
        }
    }

    /**
     * Handles a request to start a game with another player
     */
    private void handleGameRequest(String command) {
        String targetPlayer = command.substring(CLIENT_REQUEST_GAME_PREFIX.length()).trim();
        ConnectedClient target = server.getClientByUsername(targetPlayer);

        if (target != null) {
            target.sendMessage("SERVER_INCOMING_REQUEST:" + username);
        } else {
            sendMessage("SERVER_PLAYER_NOT_AVAILABLE:" + targetPlayer);
        }
    }

    private void handleGameAcceptance(String command) {
        String targetPlayer = command.substring(CLIENT_ACCEPT_GAME_PREFIX.length()).trim();
        ConnectedClient target = server.getClientByUsername(targetPlayer);

        if (target == null) {
            sendMessage("SERVER_PLAYER_NOT_AVAILABLE:" + targetPlayer);
            return;
        }
        // Notify the requesting player about the acceptance
        target.sendMessage("SERVER_PLAYER_ACCEPTED:" + username);
        // Set up the game for both players
        target.sendMessage("SERVER_GAME_READY:1;" + username);  // Player 1
        sendMessage("SERVER_GAME_READY:0;" + targetPlayer);     // Player 2
        notifyPlayersInGame(targetPlayer);
        
        // start the game player 1 and player 2 
        GameSession session = new GameSession(target, this);
        server.addGameSession(session);
    }

    private void notifyPlayersInGame(String targetPlayer) {
        for (ConnectedClient client : server.getClients()) {
            if (client != this && !client.getUsername().equals(targetPlayer)) {
                client.sendMessage("SERVER_PLAYER_STARTED_GAME:" + username);
                client.sendMessage("SERVER_PLAYER_STARTED_GAME:" + targetPlayer);
            }
        }
    }

    private void handleGameRejection(String command) {
        String targetPlayer = command.substring(CLIENT_REJECT_GAME_PREFIX.length()).trim();
        ConnectedClient target = server.getClientByUsername(targetPlayer);

        if (target != null) {
            target.sendMessage("SERVER_PLAYER_REJECTED:" + username);
        } else {
            sendMessage("SERVER_PLAYER_NOT_AVAILABLE:" + targetPlayer);
        }
    }

    private void handlePlayerAvailable() {
        // Let other clients know the player is back to a game
        for (ConnectedClient client : server.getClients()) {
            if (client != this) {
                client.sendMessage("SERVER_PLAYER_AVAILABLE:" + username);
            }
        }
    }

    private void handleGameMove(String command) {
        try {
            int column = Integer.parseInt(command.substring(CLIENT_MOVE_PREFIX.length()).trim());
            GameSession session = server.getGameSessionForPlayer(username);
            if (session != null) {
                session.handleMove(this, column);
            } else {
                sendMessage("SERVER_ERROR_PLAYER_NOT_IN_A_GAME");
            }
        } catch (NumberFormatException e) {
            sendMessage("SERVER_ERROR_INVALID_MOVE_FORMAT");
            // TODO: implement this also.
        }
    }
    
    // If any user declines restart of the game then 
    // the handleGameRestart method should notify opponent
    // that the player does not want to play and then the GameSession is killed
    // It should also then backpropagate the users to SERVER_GAME_EXIT
    private void handleGameRestart(String command) {
        GameSession session = server.getGameSessionForPlayer(username);
        int doesWantNewGame = Integer.parseInt(command.substring(CLIENT_RESTART_GAME_PREFIX.length()).trim());
        if(doesWantNewGame != 1) {
            session.endGameSession(); // Notifies the users
            server.removeGameSession(session);
            return;
        }
        // Set that the Player in the session wants a new
        // game, restart of the game
        session.setNewGameWanted(this);
        
        // Check if both users want to restart the game
        // This could maybe be better to handle separately due
        // to possible race condition?
        session.checkNewGameWanted();
    }
    
    public boolean isAlive() {
        return running && socket != null && !socket.isClosed();
    }

    private void handleExit() {
        sendMessage("SERVER_GOODBYE");
        running = false;
    }

    private void disconnect() throws IOException {
        running = false;
        server.removeClient(this);
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error closing socket for " + username, ex);
        }
        LOGGER.log(Level.INFO, "Client disconnected: {0}", username);
        writer.close();
        reader.close();
    }
}
