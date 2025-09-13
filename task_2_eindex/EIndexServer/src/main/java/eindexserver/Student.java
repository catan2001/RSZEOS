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
// Represents one student
public class Student {

    private String jmbg;
    private String name;
    private String lastName;
    private String index;
    private String username;
    private String password;
    private List<Course> courses = new ArrayList<>();

    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public String getStudentSummary() {
        return String.format("%s:%s:%s:%s", name, lastName, index, username);
    }

    public String getStudentDetails() {
        return String.format("%s:%s:%s:%s:%s:%s", name, lastName, index, jmbg, username, password);
    }
}
