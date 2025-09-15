package com.example.connectfour;

/**
 * Callback interface for handling messages received from the server.
 */
public interface ServerHandler {
    /**
     * Called whenever a message is received from the server.
     * @param msg The server message.
     */
    void handleServerMessage(String msg);

    void handleServerDisconnected();
}