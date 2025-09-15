package com.example.connectfour;

public class ConnectionManager {
    private static ConnectionManager instance;

    private ServerConnection serverConnection;
    private Thread connectionThread;

    private ConnectionManager() {}

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public void setConnection(ServerConnection serverConnection, Thread connectionThread) {
        this.serverConnection = serverConnection;
        this.connectionThread = connectionThread;
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public Thread getConnectionThread() {
        return connectionThread;
    }
}
