package com.example.connectfour;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
     * <p>This class encapsulates all networking logic for connecting
     * to the server, sending messages, and continuously listening
     * for incoming messages. It separates networking concerns from
     * the UI layer (e.g., {@link MainActivity}).</p>
     *
     * <p>Responsibilities:</p>
     * <ul>
     *   <li>Establishes a TCP socket connection to the server using the provided IP and port.</li>
     *   <li>Creates input and output streams for communication with the server.</li>
     *   <li>Starts a {@link ServerListener} in a background thread to read messages
     *       from the server and forward them to a {@link ServerHandler}.</li>
     *   <li>Provides a simple method to send messages to the server via {@link #sendMessage(String)}.</li>
     *   <li>Handles resource cleanup and ensures the connection can be closed gracefully
     *       using {@link #close()}.</li>
     * </ul>
     *
     * <p>Typical usage:</p>
     * <pre>
     *   ServerConnection connection = new ServerConnection("192.168.0.10", 8080, handler);
     *   connection.sendMessage("Hello Server!");
     *   ...
     *   connection.close();
     * </pre>
     */
    public ServerConnection(String serverIp, int port, ServerHandler handler) throws IOException {
        socket = new Socket("192.168.124.8", 8080);
        //writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
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