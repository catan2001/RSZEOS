/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package eindexserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author catic
 */
public class EIndexServer {

    private ServerSocket eIndexServerSocket;
    private int portNumber;
    private ArrayList<ConnectedServerClients> clients;

    public EIndexServer(int portNumber) throws IOException {
        this.clients = new ArrayList<>();
        try {
            this.portNumber = portNumber;
            this.eIndexServerSocket = new ServerSocket(portNumber);
        } catch (IOException ex) {
            Logger.getLogger(EIndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Getter and Setter for EIndexServerSocket
    public ServerSocket getEIndexServerSocket() {
        return eIndexServerSocket;
    }

    public void setEIndexServerSocket(ServerSocket eIndexServerSocket) {
        this.eIndexServerSocket = eIndexServerSocket;
    }

    // Getter and Setter for portNumber
    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    // Getter and Setter for clients
    public ArrayList<ConnectedServerClients> getClients() {
        return clients;
    }

    public void acceptClients() {
        Socket client = null;
        Thread thread;
        while (true) {
            try {
                System.out.println("The server is waiting for clients to connect...");
                client = this.eIndexServerSocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(EIndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
                System.out.println("Client connected!");
                ConnectedServerClients newClient = new ConnectedServerClients(client);
                clients.add(newClient);
                thread = new Thread(newClient);
                thread.start();
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        /* 
        Create and initialize the users.txt file if it does not exist 
        with ADMIN:ADMIN:admin 
         */
        final String sP = System.getProperty("file.separator");
        File usersFile = new File("data" + sP + "users.txt");
        usersFile.getParentFile().mkdirs(); // Ensure data/ exists
        List<String> lines = new ArrayList<>();

        if (usersFile.exists()) {
            lines = Files.readAllLines(usersFile.toPath());
        }
        // Check if ADMIN line exists, if not, add it at the beginning
        if (lines.isEmpty() || !lines.get(0).equals("ADMIN:ADMIN:admin")) {
            lines.add(0, "ADMIN:ADMIN:admin");
        }
        Files.write(usersFile.toPath(), lines);

        System.out.println("Starting the server...");
        EIndexServer server = new EIndexServer(8080);
        System.out.println("EIndexChatRoom started on port: " + server.getPortNumber());
        server.acceptClients();
    }
}
