/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author catic
 */
public class StudentsDatabase {

    private final String baseDir;
    String sP;

    public StudentsDatabase() {
        sP = System.getProperty("file.separator");
        this.baseDir = "data/students";
    }

    /**
     * Load only basic student summaries: Name, Index, JMBG
     * @return 
     */
    public List<Student> getStudentSummaries() {
        List<Student> students = new ArrayList<>();
        File usersFile = new File("data" + sP + "users.txt");

        if (!usersFile.exists()) {
            System.err.println("users.txt not found");
            return students;
        }

        Set<String> studentUsernames = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(":");
                if (parts.length == 3 && parts[2].equalsIgnoreCase("student")) {
                    studentUsernames.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users.txt: " + e.getMessage());
            return students;
        }

        for (String username : studentUsernames) {
            File infoFile = new File("data" + sP + "students" + sP + username + sP + "info.txt");
            if (!infoFile.exists()) {
                System.err.println("Missing info.txt for user " + username);
                continue;
            }

            try (BufferedReader infoReader = new BufferedReader(new FileReader(infoFile))) {
                String infoLine = infoReader.readLine();
                if (infoLine != null) {
                    String[] parts = infoLine.split(":");
                    if (parts.length >= 6) {
                        Student student = new Student();
                        student.setName(parts[0].trim());
                        student.setLastName(parts[1].trim());
                        student.setIndex(parts[2].trim());
                        student.setJmbg(parts[3].trim());
                        student.setUsername(parts[4].trim());
                        students.add(student);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading info.txt for user " + username + ": " + e.getMessage());
            }
        }

        return students;
    }

    /**
     * Load full student info (name, index, username, password, courses)
     * @param username
     * @return 
     */
    public Student getStudentDetails(String username) {
        File studentDir = new File(baseDir, username);
        File infoFile = new File(studentDir, "info.txt");
        File coursesFile = new File(studentDir, "courses.txt");

        if (!infoFile.exists()) {
            System.err.println("info.txt missing for " + username);
            return null;
        }

        Student student = new Student();
        student.setUsername(username);

        try (BufferedReader infoReader = new BufferedReader(new FileReader(infoFile))) {
            String infoLine = infoReader.readLine();
            if (infoLine != null) {
                String[] parts = infoLine.split(":");
                if (parts.length >= 5) {
                    student.setName(parts[0].trim());
                    student.setLastName(parts[1].trim());
                    student.setIndex(parts[2].trim());
                    student.setJmbg(parts[3].trim());
                    student.setUsername(parts[4].trim());
                    student.setPassword(parts[5].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading info.txt for " + username + ": " + e.getMessage());
        }

        if (coursesFile.exists()) {
            try (BufferedReader courseReader = new BufferedReader(new FileReader(coursesFile))) {
                String line;
                List<Course> courses = new ArrayList<>();
                while ((line = courseReader.readLine()) != null) {
                    System.out.println(line);
                    String[] parts = line.split(";"); // split into course name and the rest
                    System.out.println(parts[0]);
                    System.out.println(parts[1]);
                    if (parts.length == 2) {
                        String courseName = parts[0].trim();
                        String categoriesPart = parts[1].trim();

                        Course course = new Course();
                        course.setCourseName(courseName);

                        String[] catScorePairs = categoriesPart.split("\\|");
                        List<String> categories = new ArrayList<>();
                        List<Integer> scores = new ArrayList<>();
                        for (String catScore : catScorePairs) {
                            System.out.println("catScore: " + catScore);
                            String[] cs = catScore.split(":");
                            if (cs.length == 2) {
                                categories.add(cs[0].trim());
                                scores.add(Integer.valueOf(cs[1].trim()));
                            }
                        }
                        course.setCourseCategories(categories);
                        course.setScores(scores);
                        courses.add(course);
                        System.out.println("Size of cat: " + course.getCourseCategories().size());
                    }
                }
                student.setCourses(courses);
            } catch (IOException e) {
                System.err.println("Error reading courses.txt for " + username + ": " + e.getMessage());
            }
        }

        return student;
    }
}
