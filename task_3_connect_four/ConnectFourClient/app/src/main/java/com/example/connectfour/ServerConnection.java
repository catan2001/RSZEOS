package com.example.connectfour;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private ServerListener listener;
    private Thread listenerThread;

    /**
     * Manages the client-side connection to the game server.
     *
     * This class encapsulates all networking logic for connecting
     * to the server, sending messages, and continuously listening
     * for incoming messages. It separates networking concerns from
     * the UI layer (e.g., {@link MainActivity}).
     */
    public ServerConnection(String serverIp, int port, ServerHandler handler) throws IOException {
        socket = new Socket(serverIp, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        listener = new ServerListener(reader, handler);
        listenerThread = new Thread(listener, "Server-Listener");
        listenerThread.start();
    }

    // We call the setHandler function to swap the new handler
    // to do so we set the listener.setHandler which just changes
    // the handler into a one from the parameter. This will be used in
    // Activites when changing them
    public void setListenerHandler(ServerHandler handler) {
        if (listener != null) {
            listener.setHandler(handler); // no new thread
        }
    }

    public void sendMessage(String message) {
        if (writer != null && message != null) {
            new Thread(() -> {
                writer.println(message);
                writer.flush();
                android.util.Log.d("ServerConnection", "Sent: " + message);
            }).start();
        } else {
            android.util.Log.e("ServerConnection", "Writer is null or message is null, nothing sent");
        }
    }

    public void close() {
        listener.stop();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}