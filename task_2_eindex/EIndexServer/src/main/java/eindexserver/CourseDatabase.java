/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author catic
 */
public class CourseDatabase {

    private final String baseDir;
    private final String sP;

    public CourseDatabase() {
        this.sP = System.getProperty("file.separator");
        this.baseDir = "data" + sP + "courses";
    }

    /**
     * Reads all course names from courses.txt
     *
     * @return List of course names
     */
    public List<String> getAllCourseNames() {
        List<String> courseNames = new ArrayList<>();
        String filename = baseDir + sP + "courses.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Each line is NAME_OF_COURSE:CATEGORY_FIELD;MAX_POINTS|CATEGORY_FIELD;MAX_POINTS...
                    String[] parts = line.split(":", 2); // split into [courseName, categories...]
                    if (parts.length > 0) {
                        courseNames.add(parts[0].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading courses file: " + e.getMessage());
        }

        return courseNames;
    }

    /**
     *
     * @param searchName
     * @return
     */
    public Course getCourseByName(String searchName) {
        String filename = baseDir + sP + "courses.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(":", 2);
                String courseName = parts[0].trim();

                if (courseName.equalsIgnoreCase(searchName)) {
                    Course course = new Course();
                    course.setCourseName(courseName);
                    course.setGrade(0); // reset grade
                    course.getScores().clear(); // reset scores
                    course.getCourseCategories().clear();
                    course.getMaxScores().clear();
                    course.getMinScores().clear();

                    if (parts.length > 1) {
                        String[] categories = parts[1].split("\\|");
                        for (String cat : categories) {
                            String[] catParts = cat.split(";", 2); // split into category + min?max
                            if (catParts.length == 2) {
                                String catName = catParts[0].trim();

                                String[] minMax = catParts[1].split("\\?");
                                if (minMax.length == 2) {
                                    int minPoints = Integer.parseInt(minMax[0].trim());
                                    int maxPoints = Integer.parseInt(minMax[1].trim());

                                    course.getCourseCategories().add(catName);
                                    course.getMinScores().add(minPoints);
                                    course.getMaxScores().add(maxPoints);
                                } else {
                                    System.err.println("Invalid min?max format for category: " + cat);
                                }
                            }
                        }
                    }

                    return course;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading courses file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in courses file: " + e.getMessage());
        }

        return null; // not found
    }
}
