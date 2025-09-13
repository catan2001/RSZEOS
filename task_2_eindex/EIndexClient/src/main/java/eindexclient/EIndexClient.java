/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author catic
 */
public class EIndexClient {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final ServerListener listener;

    private JFrame currentWindow;
    private ClientLoginGUI loginGUI;
    private ClientAdminGUI adminGUI;
    private ClientStudentGUI studentGUI;

    public EIndexClient() throws IOException {
        // 1) Create ONE socket for the whole app
        this.socket = new Socket("127.0.0.1", 8080);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        // 2) Start with Login GUI as the current ServerHandler
        this.loginGUI = new ClientLoginGUI(this, this.writer); // pass app context
        this.currentWindow = loginGUI;

        // 3) Listener forwards every server line to the *current* handler
        this.listener = new ServerListener(reader, loginGUI);
        new Thread(listener, "Server-Listener").start();

        java.awt.EventQueue.invokeLater(() -> currentWindow.setVisible(true));
    }

    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Called from LoginGUI when server confirms an Admin login.
     */
    public void switchToAdmin() {
        if (adminGUI == null) {
            adminGUI = new ClientAdminGUI(this, this.writer);
        }
        swapWindow(adminGUI);
    }

    /**
     * Called from LoginGUI when server confirms a Student login.
     * @param username
     */
    public void switchToStudent(String username) {
        if (studentGUI == null) {
            studentGUI = new ClientStudentGUI(this, this.writer, username);
        }
        swapWindow(studentGUI);
    }

    private void swapWindow(ServerHandler newHandlerAsWindow) {
        // Update listener destination first
        listener.setServerHandler(newHandlerAsWindow);
        java.awt.EventQueue.invokeLater(() -> {
            if (currentWindow != null) {
                currentWindow.setVisible(false);
            }
            currentWindow = (JFrame) newHandlerAsWindow; // all handlers here are JFrames
            currentWindow.setLocationRelativeTo(null);
            currentWindow.setVisible(true);
        });
    }

    /**
     * Clean shutdown from any window.
     */
    public void close() {
        try {
            listener.stop();
        } catch (Exception ignored) {
        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }
        try {
            writer.close();
        } catch (Exception ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        try {
            new EIndexClient(); // entry point
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot connect to server: " + e.getMessage(),
                    "Connection error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
