/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eindexserver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author catic
 */
// Represents one course used in Student class
public class Course {

    private String courseName;
    private int grade;
    private List<Integer> scores = new ArrayList<>();
    private List<Integer> maxScores = new ArrayList<>();
    private List<Integer> minScores = new ArrayList<>();
    private List<String> courseCategories = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("%s (grade: %d, scores: %s)", courseName, grade, scores);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }
    
    public List<Integer> getMaxScores() {
        return maxScores;
    }

    public void setMaxScores(List<Integer> maxScores) {
        this.maxScores = maxScores;
    }
    
    public List<Integer> getMinScores() {
        return minScores;
    }

    public void setMinScores(List<Integer> minScores) {
        this.minScores = minScores;
    }

    public List<String> getCourseCategories() {
        return courseCategories;
    }

    public void setCourseCategories(List<String> courseCategories) {
        this.courseCategories = courseCategories;
    }

}
