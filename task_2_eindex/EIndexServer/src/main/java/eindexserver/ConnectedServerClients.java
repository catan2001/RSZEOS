/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author catic
 */
public class ConnectedServerClients implements Runnable {

    //atributi koji se koriste za komunikaciju sa klijentom
    private Socket socket;
    private String username;
    private String password;
    private String role;
    private BufferedReader reader;
    private PrintWriter writer;
    private ArrayList<ConnectedServerClients> connectedClients;
    private Student selectedStudent;

    // Getter and Setter for userName
    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public ConnectedServerClients(Socket socket) {
        this.socket = socket;

        try {
            // Use UTF-8 for consistent text encoding
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Initially, username, password and role are unknown
            this.username = "";
            this.password = "";
            this.role = "";
            this.selectedStudent = null;

        } catch (IOException ex) {
            Logger.getLogger(ConnectedServerClients.class.getName())
                    .log(Level.SEVERE, "Error initializing client streams", ex);
        }
    }

    private void getCredentials() throws IOException {
        // Reset credentials
        username = "";
        password = "";
        role = "";

        while (username.isBlank() || password.isBlank() || role.isBlank()) {
            String msg = reader.readLine();
            if (msg == null) {
                throw new IOException("Client disconnected before sending credentials.");
            }
            if (msg.startsWith("LOGIN:")) {
                // Split by colon
                String[] parts = msg.split(":", 4); // limit 4 to avoid extra colons breaking it
                if (parts.length < 4) {
                    writer.println("LOGIN_FAIL:Invalid format. Use LOGIN:ROLE:USERNAME:PASSWORD");
                    continue; // ask again
                }

                role = parts[1].trim();
                username = parts[2].trim();
                password = parts[3].trim();

                // Validate role
                if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("student")) {
                    writer.println("LOGIN_FAIL:Invalid role. Use 'admin' or 'student'");
                    role = ""; // reset to loop again
                }
            } else {
                writer.println("LOGIN_FAIL:Expected LOGIN message");
            }
        }

        // Save credentials in the object
        this.role = role.toLowerCase();
    }

    private boolean authenticateClient() throws IOException {
        // get credentials from the client
        getCredentials();
        // find user in DB/list
        String sP = System.getProperty("file.separator");
        UsersDatabase uDB = new UsersDatabase("data" + sP + "users.txt");
        User user = uDB.findUser(username, role);
        if (user == null) {
            writer.println("LOGIN_FAIL User not found. Please check your username and role.");
            System.out.println("User not found. Please check your username and role.");
            return false;
        }

        // check authentication
        if (!user.getPassword().equals(password)) {
            writer.println("LOGIN_FAIL Authentication failed. Wrong password.");
            System.out.println("Authentication failed. Wrong password.");
            return false;
        }

        return true;
    }

    private void StudentCommunication() {
        // Send back signal to start Student
        writer.println("LOGIN_OK_STUDENT");
        String message;
        StudentsDatabase studentsDB = new StudentsDatabase();
        try {
            String command;
            while ((command = reader.readLine()) != null) {
                System.out.println(command);
                switch (command.split(":")[0]) {
                    case "GET_STUDENT_DETAILS":
                        // Expected format: GET_STUDENT_DETAILS:USERNAME
                        String studentUsername = command.split(":")[1];
                        selectedStudent = studentsDB.getStudentDetails(studentUsername);
                        writer.println("STUDENT_DETAILS:" + selectedStudent.getStudentDetails());

                        String courses = "";
                        for (Course course : selectedStudent.getCourses()) {
                            courses = courses + course.getCourseName() + ";";
                        }
                        writer.println("ASSIGNED_COURSES:" + courses);
                        break;
                    case "GET_COURSE_SCORES":
                        // Expected format: GET_COURSE_SCORES:courseName
                        String courseName = command.split(":")[1].trim();
                        System.out.println(command);
                        Course foundCourse = null;
                        for (Course course : selectedStudent.getCourses()) {
                            if (course.getCourseName().equalsIgnoreCase(courseName)) {
                                foundCourse = course;
                                break;
                            }
                        }

                        if (foundCourse != null) {
                            List<String> categories = foundCourse.getCourseCategories();
                            List<Integer> scores = foundCourse.getScores();

                            // Load min/max info from /data/courses/courses.txt
                            File coursesFile = new File("data/courses/courses.txt");
                            String minMaxLine = null;
                            try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    if (line.startsWith(courseName + ":")) {
                                        minMaxLine = line;
                                        break;
                                    }
                                }
                            }

                            // Parse min/max from line like "Mathematics:T1;10?20|T2;14?20|Z1;15?30|Z2;16?30"
                            Map<String, String> minMaxMap = new HashMap<>();
                            if (minMaxLine != null) {
                                String[] parts = minMaxLine.split(":")[1].split("\\|");
                                for (String part : parts) {
                                    // Example part: "T1;10?20"
                                    String[] catMinMax = part.split(";");
                                    String category = catMinMax[0]; // e.g. "T1"
                                    String minMax = catMinMax[1];   // e.g. "10?20"
                                    minMaxMap.put(category, minMax);
                                }
                            }

                            StringBuilder response = new StringBuilder("COURSE_SCORES:");
                            for (int i = 0; i < categories.size(); i++) {
                                String category = categories.get(i);
                                int score = scores.get(i);
                                String minMax = minMaxMap.getOrDefault(category, "?");

                                // Build response like: T1;15;10?20
                                response.append(category)
                                        .append(";")
                                        .append(score)
                                        .append(";")
                                        .append(minMax);

                                if (i < categories.size() - 1) {
                                    response.append("|"); // add separator between category-score pairs
                                }
                            }
                            writer.println(response.toString());
                        } else {
                            writer.println("COURSE_SCORES:NOT_FOUND");
                        }

                        break;
                    case "EXIT":
                        writer.println("Goodbye Student!");
                        return;
                    default:
                        writer.println("Unknown command. Try again.");
                }
            }
        } catch (IOException e) {
            Logger.getLogger(ConnectedServerClients.class.getName())
                    .log(Level.SEVERE, "Error in AdminCommunication", e);
        }
    }

    private void AdminCommunication() {
        // Send back signal to start admin
        writer.println("LOGIN_OK_ADMIN");
        String message;
        StudentsDatabase studentsDB = new StudentsDatabase();
        try {
            String command;
            while ((command = reader.readLine()) != null) {
                System.out.println(command);
                switch (command.split(":")[0]) {
                    case "GET_STUDENTS":
                        // Example: Return list of students
                        List<Student> students = studentsDB.getStudentSummaries();
                        for (Student student : students) {
                            System.out.println("Student: " + student.getStudentSummary());
                            writer.println("STUDENTS_LIST:" + student.getStudentSummary());
                        }
                        break;
                    case "GET_STUDENT_DETAILS":
                        // Expected format: GET_STUDENT_DETAILS:USERNAME
                        String studentUsername = command.split(":")[1];
                        selectedStudent = studentsDB.getStudentDetails(studentUsername);
                        writer.println("STUDENT_DETAILS:" + selectedStudent.getStudentDetails());

                        String courses = "";
                        for (Course course : selectedStudent.getCourses()) {
                            courses = courses + course.getCourseName() + ";";
                        }
                        writer.println("ASSIGNED_COURSES:" + courses);
                        break;
                    case "GET_COURSE_SCORES":
                        // Expected format: GET_COURSE_SCORES:courseName
                        String courseName = command.split(":")[1].trim();
                        System.out.println(command);
                        Course foundCourse = null;
                        for (Course course : selectedStudent.getCourses()) {
                            if (course.getCourseName().equalsIgnoreCase(courseName)) {
                                foundCourse = course;
                                break;
                            }
                        }

                        if (foundCourse != null) {
                            List<String> categories = foundCourse.getCourseCategories();
                            List<Integer> scores = foundCourse.getScores();

                            // Load min/max info from /data/courses/courses.txt
                            File coursesFile = new File("data/courses/courses.txt");
                            String minMaxLine = null;
                            try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    if (line.startsWith(courseName + ":")) {
                                        minMaxLine = line;
                                        break;
                                    }
                                }
                            }

                            // Parse min/max from line like "Mathematics:T1;10?20|T2;14?20|Z1;15?30|Z2;16?30"
                            Map<String, String> minMaxMap = new HashMap<>();
                            if (minMaxLine != null) {
                                String[] parts = minMaxLine.split(":")[1].split("\\|");
                                for (String part : parts) {
                                    // Example part: "T1;10?20"
                                    String[] catMinMax = part.split(";");
                                    String category = catMinMax[0]; // e.g. "T1"
                                    String minMax = catMinMax[1];   // e.g. "10?20"
                                    minMaxMap.put(category, minMax);
                                }
                            }

                            StringBuilder response = new StringBuilder("COURSE_SCORES:");
                            for (int i = 0; i < categories.size(); i++) {
                                String category = categories.get(i);
                                int score = scores.get(i);
                                String minMax = minMaxMap.getOrDefault(category, "?");

                                // Build response like: T1;15;10?20
                                response.append(category)
                                        .append(";")
                                        .append(score)
                                        .append(";")
                                        .append(minMax);

                                if (i < categories.size() - 1) {
                                    response.append("|"); // add separator between category-score pairs
                                }
                            }
                            writer.println(response.toString());
                        } else {
                            writer.println("COURSE_SCORES:NOT_FOUND");
                        }
                        break;
                    case "ADD_ADMIN":
                        System.out.println(command);
                        String content_admin = command.substring("ADD_ADMIN:".length());
                        String[] parts_admin = content_admin.split("\\|");
                        if (parts_admin.length < 2) {
                            System.err.println("Invalid message, missing username/password");
                            return;
                        }
                        String usernameAdmin = parts_admin[0].trim();
                        String passwordAdmin = parts_admin[1].trim();

                        final String sP = System.getProperty("file.separator");
                        File usersFile = new File("data" + sP + "users.txt");
                        usersFile.getParentFile().mkdirs(); // Ensure data/ exists
                        try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile, true))) {
                            pw.println(usernameAdmin + ":" + passwordAdmin + ":admin");
                        }

                        break;
                    case "ADD_STUDENT":
                        String content = command.substring("ADD_STUDENT:".length());
                        String[] parts = content.split(":", 2);
                        if (parts.length < 2) {
                            System.err.println("Invalid message, missing course info");
                            return;
                        }

                        String[] studentInfo = parts[0].split(";");
                        if (studentInfo.length != 6) {
                            System.err.println("Invalid student info format");
                            return;
                        }

                        String name = studentInfo[0];
                        String lastName = studentInfo[1];
                        String id = studentInfo[2];
                        String index = studentInfo[3];
                        String usernameinfo = studentInfo[4];
                        String passwordInfo = studentInfo[5];
                        final String sP_1 = System.getProperty("file.separator");

                        File usersFile_1 = new File("data" + sP_1 + "users.txt");
                        usersFile_1.getParentFile().mkdirs(); // Ensure data/ exists
                        try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile_1, true))) {
                            pw.println(usernameinfo + ":" + passwordInfo + ":student");
                        }

                        File studentDir = new File("data" + sP_1 + "students" + sP_1, usernameinfo);
                        studentDir.mkdirs();

                        File infoFile = new File(studentDir, "info.txt");
                        try (PrintWriter pw = new PrintWriter(infoFile)) {
                            pw.println(name + ":" + lastName + ":" + index + ":" + id + ":" + usernameinfo + ":" + passwordInfo);
                        }

                        // 4. Create courses.txt
                        File coursesFile = new File(studentDir, "courses.txt");
                        try (PrintWriter pw = new PrintWriter(coursesFile)) {
                            String[] allCourses = parts[1].split(";");
                            for (String coursePart : allCourses) {
                                // coursePart example: Math?Homework|5.Exam|9
                                String[] courseSplit = coursePart.split("\\?", 2);
                                if (courseSplit.length != 2) {
                                    continue;
                                }

                                String courseNameInfo = courseSplit[0];
                                String categories = courseSplit[1].replace("|", ":");
                                categories = categories.replace(".", "|");
                                pw.println(courseNameInfo + ";" + categories);
                            }
                        }
                        System.out.println("Student " + usernameinfo + " added successfully.");
                        writer.println("REFRESH_STUDENTS");
                        break;
                    case "GET_AVAILABLE_COURSES":
                        List<String> availableCourses = new CourseDatabase().getAllCourseNames();
                        message = "AVAILABLE_COURSES:" + String.join("|", availableCourses);
                        writer.println(message);
                        break;

                    case "GET_COURSE_DETAILS":
                        Course course = new CourseDatabase().getCourseByName(command.split(":")[1].trim());
                        if (course != null) {
                            List<String> details = new ArrayList<>();
                            for (int i = 0; i < course.getCourseCategories().size(); i++) {
                                String catName = course.getCourseCategories().get(i);
                                int minPoints = course.getMinScores().get(i);
                                int maxPoints = course.getMaxScores().get(i);
                                details.add(catName + ";" + minPoints + "?" + maxPoints);
                            }
                            message = "DETAILED_COURSE:" + String.join("|", details);
                        } else {
                            message = "DETAILED_COURSE:NOT_FOUND";
                        }
                        writer.println(message);
                        break;
                    case "ADD_COURSE":
                        try {
                            // Message format: ADD_COURSE:Biology:T;10?20|Z;10?20|Project;35?60
                            String content_msg = command.substring("ADD_COURSE:".length()).trim();
                            if (content_msg.isEmpty()) {
                                System.err.println("Empty ADD_COURSE message");
                                break;
                            }

                            // Ensure data/courses/ folder exists
                            File coursesDir = new File("data/courses");
                            coursesDir.mkdirs();

                            File coursesFile1 = new File(coursesDir, "courses.txt");
                            // Append the course info in one line
                            try (PrintWriter pw = new PrintWriter(new FileWriter(coursesFile1, true))) {
                                pw.println(content_msg); // writes exactly as received
                            }

                            System.out.println("Course added: " + content_msg);
                        } catch (IOException e) {
                            System.err.println("Error adding course: " + e.getMessage());
                        }
                        writer.println("REFRESH_COURSES");
                        break;
                    case "UPDATE_STUDENT":
                        try {
                            String data = command.substring("UPDATE_STUDENT:".length());

                            String[] parts_update = data.split(";", 8);
                            String selectedStudentUsername = parts_update[0];
                            String username_update = parts_update[1];
                            String password_update = parts_update[2];
                            String id_update = parts_update[3];
                            String index_update = parts_update[4];
                            String firstName_update = parts_update[5];
                            String lastName_update = parts_update[6];

                            // Split last part into course + scores
                            String[] courseAndScores = parts_update[7].split(";", 2);
                            String courseName_update = courseAndScores[0];
                            String scores = courseAndScores[1]; // e.g. T1;20|T2;20|Z1;30|Z2;0

                            // 1. Update users.txt
                            File usersFile_update = new File("data/users.txt");
                            List<String> lines = new ArrayList<>();
                            try (BufferedReader br = new BufferedReader(new FileReader(usersFile_update))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    String[] tokens = line.split(":");
                                    if (tokens.length >= 2 && tokens[0].equals(selectedStudentUsername)) {
                                        // Replace with new username + password
                                        line = username_update + ":" + password_update + ":student";
                                    }
                                    lines.add(line);
                                }
                            }
                            try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile_update))) {
                                for (String l : lines) {
                                    pw.println(l);
                                }
                            }

                            if (!(new File("data/students/" + username_update).exists())) {
                                new File("data/students/" + username_update).mkdirs();
                            }

                            // --- Copy old courses.txt if it exists ---
                            File oldCoursesFile = new File("data/students/" + selectedStudentUsername + "/courses.txt");
                            File newCoursesFile = new File("data/students/" + username_update + "/courses.txt");

                            List<String> liness = new ArrayList<>();
                            if (oldCoursesFile.exists()) {
                                try (BufferedReader br = new BufferedReader(new FileReader(oldCoursesFile))) {
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        liness.add(line);
                                    }
                                }
                            }

                            // --- Update the specific course line ---
                            boolean courseFound = false;
                            for (int i = 0; i < liness.size(); i++) {
                                if (liness.get(i).startsWith(courseName_update + ";")) {
                                    liness.set(i, courseName_update + ";" + scores.replace(";", ":"));
                                    courseFound = true;
                                    break;
                                }
                            }
                            if (!courseFound) {
                                // if course not found, just append it
                                lines.add(courseName_update + ";" + scores.replace(";", ":"));
                            }

                            // --- Write to the new courses.txt ---
                            try (PrintWriter pw = new PrintWriter(new FileWriter(newCoursesFile))) {
                                for (String l : liness) {
                                    pw.println(l);
                                }
                            }

                            File infoFile_update = new File("data/students/" + username_update + "/info.txt");
                            try (PrintWriter pw = new PrintWriter(new FileWriter(infoFile_update))) {
                                pw.println(firstName_update + ":" + lastName_update + ":" + index_update + ":" + id_update + ":" + username_update + ":" + password_update);
                            }

                            System.out.println("Student " + username_update + " updated successfully.");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case "EXIT":
                        writer.println("Goodbye Admin!");
                        return;

                    default:
                        writer.println("Unknown command. Try again.");
                }
            }
        } catch (IOException e) {
            Logger.getLogger(ConnectedServerClients.class.getName())
                    .log(Level.SEVERE, "Error in AdminCommunication", e);
        }
    }

    private void ClientCommunicationEngine() {
        switch (this.role) {
            case "admin" ->
                AdminCommunication();
            case "student" ->
                StudentCommunication();
            default -> {
                // It should never enter this state
            }
        }
        // Error handling
    }

    @Override
    public void run() {
        try {
            // Ask client for it's credentials
            while (!authenticateClient()) {
                writer.println("AUTHENTICATION_FAILED");
            }

            ClientCommunicationEngine();

        } catch (IOException ex) {
            Logger.getLogger(ConnectedServerClients.class.getName())
                    .log(Level.SEVERE, "Error during client communication", ex);
        } finally {
            // Remove client from list when disconnected and clean the data
        }
    }
}
