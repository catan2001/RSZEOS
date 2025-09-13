/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexserver;

/**
 *
 * @author catic
 */
import java.io.*;
import java.util.*;

public class UsersDatabase {
    private List<User> users;

    // Load users from file when class is constructed
    public UsersDatabase(String filename) {
        users = new ArrayList<>();
        loadUsersFromFile(filename);
    }

    private void loadUsersFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Expected format: username:password:role
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    users.add(new User(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
    }

    // Save users back to file (if you allow modifications)
    public void saveUsersToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (User u : users) {
                pw.println(u.getUsername() + ":" + u.getPassword() + ":" + u.getRole());
            }
        } catch (IOException e) {
            System.err.println("Error saving users file: " + e.getMessage());
        }
    }

    public User findUser(String username, String role) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && 
                u.getRole().equalsIgnoreCase(role)) {
                return u;
            }
        }
        return null;
    }   

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(String username, String role) {
        users.removeIf(u -> u.getUsername().equalsIgnoreCase(username) && 
                             u.getRole().equalsIgnoreCase(role));
    }

    public List<User> getAllUsers() {
        return Collections.unmodifiableList(users);
    }
}
