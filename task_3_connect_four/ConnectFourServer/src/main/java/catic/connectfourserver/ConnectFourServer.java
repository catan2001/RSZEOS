/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package catic.connectfourserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author catic
 *
 * ConnectFourServer is the entry point for hosting multiplayer games. - Listens
 * on a TCP port for client connections. - For each client, creates a
 * ConnectedClient handler running in its own thread. - Keeps track of all
 * connected players. - Provides a broadcast mechanism to notify all clients
 * about events (like a player disconnecting).
 */
public class ConnectFourServer {

    private final int portNumber;
    private final ServerSocket serverSocket;

    // list to store all connected clients and their game sessions
    private final List<ConnectedClient> clients = new ArrayList<>();
    private final List<GameSession> gameSessions = new ArrayList<>();

    public ConnectFourServer(int portNumber) throws IOException {
        this.portNumber = portNumber;
        this.serverSocket = new ServerSocket(portNumber);
        Logger.getLogger(ConnectFourServer.class.getName())
                .log(Level.INFO, "Server started on port {0}", portNumber);
    }

    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Returns all currently connected clients
     *
     * @return
     */
    public List<ConnectedClient> getClients() {
        return clients;
    }

    /**
     * Returns a ConnectedClient by username, or null if not found
     */
    public ConnectedClient getClientByUsername(String username) {
        for (ConnectedClient client : clients) {
            if (username.equals(client.getUsername())) {
                return client;
            }
        }
        return null;
    }

    /**
     * Starts listening for new clients and spawns a thread for each
     */
    public void acceptClients() {
        while (true) {
            try {
                Logger.getLogger(ConnectFourServer.class.getName())
                        .log(Level.INFO, "Waiting for clients...");
                Socket clientSocket = serverSocket.accept();
                Logger.getLogger(ConnectFourServer.class.getName())
                        .log(Level.INFO, "Client connected: {0}", clientSocket);

                // Create handler for the client
                ConnectedClient clientHandler = new ConnectedClient(clientSocket, this);
                clients.add(clientHandler);

                Thread clientThread = new Thread(clientHandler, "Client-Server" + clientSocket.getPort());
                clientThread.start();

            } catch (IOException ex) {
                Logger.getLogger(ConnectFourServer.class.getName())
                        .log(Level.SEVERE, "Error accepting client", ex);
            }
        }
    }

    /**
     * Broadcast a message to all connected clients It should not broadcast the
     * message to the same player
     *
     * @param message
     * @param playerSender
     */
    public void broadcastToAll(String message, String playerSender) {
        synchronized (clients) {
            for (ConnectedClient client : clients) {
                if (client.getUsername().equals(playerSender)) {
                    continue; // skip same user
                }
                if (client.isAlive()) {
                    client.sendMessage(message + playerSender);
                } else {
                    removeClient(client);
                }

            }
        }
    }

    /**
     * Broadcast a message to specific connected client It should not broadcast
     * the message to the same player
     *
     * @param message
     * @param playerSender
     * @param playerReciever
     */
    public void broadcastToSpecificClient(String message, String playerSender, String playerReciever) {
        ConnectedClient client = getClientByUsername(playerReciever);
        // check if the user is not the same as the player reciever
        if (!playerSender.equals(playerReciever)) {
            client.sendMessage(message + playerSender);
        }
    }

    /**
     * Remove a client from the list (e.g., when disconnected). Also notifies
     * all other clients.
     *
     * @param client
     */
    public void removeClient(ConnectedClient client) {
        synchronized (clients) {
            clients.remove(client);
            Logger.getLogger(ConnectFourServer.class.getName())
                    .log(Level.INFO, "Client removed: {0}", client.getUsername());
            broadcastToAll("SERVER_PLAYER_LEFT:", client.getUsername());
        }
    }

    /* Next part is for the GameSession */
    public void addGameSession(GameSession session) {
        synchronized (gameSessions) {
            gameSessions.add(session);
        }
    }

    public void removeGameSession(GameSession session) {
        synchronized (gameSessions) {
            gameSessions.remove(session);
        }
    }

// Find the game for a given player
    public GameSession getGameSessionForPlayer(String username) {
        synchronized (gameSessions) {
            for (GameSession session : gameSessions) {
                if (session.hasPlayer(username)) {
                    return session;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            ConnectFourServer server = new ConnectFourServer(8080);
            server.acceptClients();
        } catch (IOException ex) {
            Logger.getLogger(ConnectFourServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
