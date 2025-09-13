/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexclient;

import java.io.BufferedReader;
import java.io.IOException;
//import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author catic
 */
public class ServerListener implements Runnable {

    private BufferedReader reader;
    private volatile ServerHandler handler;  // abstract type
    private volatile boolean running = true;

    public ServerListener(BufferedReader reader, ServerHandler handler) {
        this.reader = reader;
        this.handler = handler;
    }

    public void setServerHandler(ServerHandler handler) {
        this.handler = handler;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                final String msg = line;
                java.awt.EventQueue.invokeLater(() -> {
                    ServerHandler h = this.handler;
                    if (h != null) {
                        h.handleServerMessage(msg);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
