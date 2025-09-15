package com.example.connectfour;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A background listener that continuously reads messages from the server
 * and dispatches them to a {@link ServerHandler}. The {@link ServerHandler}
 * is implemented in each of the Activity classes that represent login or
 * playing game.
 */
public class ServerListener implements Runnable {

    private final BufferedReader reader;
    private volatile ServerHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ServerListener(BufferedReader reader, ServerHandler handler) {
        this.reader = reader;
        this.handler = handler;
    }

    public void setHandler(ServerHandler handler) {
        this.handler = handler;
    }

    public void stop() {
        running.set(false);
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                ServerHandler h = this.handler;
                if (h != null) {
                    h.handleServerMessage(line);
                }
            }
        } catch (IOException e) {
            // Only log if we didn’t stop intentionally
            if (running.get()) {
                System.err.println("ServerListener stopped due to error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            // If a reader returns null then it means that that the connection to the Server
            // has closed and should be handled. Each activity should implement its handleServerDisconnected();
            // that should basically restore the app to starting state
            ServerHandler h = this.handler;
            if (h != null) {
                h.handleServerDisconnected();
            }
        }
    }
}