/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package eindexclient;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author catic
 */
public class ClientAdminGUI extends javax.swing.JFrame implements ServerHandler {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ClientAdminGUI.class.getName());
    private final EIndexClient app;
    private final PrintWriter writer;
    private String selectedStudentUsername;
    // Used for Adding User
    private Course selectedCourse;
    //private List<Course> newStudentCourses;
    private List<Course> takenCourses;
    DefaultListModel<String> modelAddedCourses;
    DefaultListModel<String> modelUserList = new DefaultListModel<>();

    /**
     * Creates new form ClientAdminGUI
     */
    public ClientAdminGUI(EIndexClient app, PrintWriter writer) {
        this.app = app;
        this.writer = writer;
        //newStudentCourses = new ArrayList<>();
        takenCourses = new ArrayList<>();
        selectedCourse = new Course();
        modelAddedCourses = new DefaultListModel<>();
        initComponents();

        // Request initial student list from server
        writer.println("GET_STUDENTS");
        writer.println("GET_AVAILABLE_COURSES");
    }

    @Override
    public void handleServerMessage(String message) {
        System.out.println(message);
        java.awt.EventQueue.invokeLater(() -> {
            if (message.startsWith("STUDENTS_LIST:")) {
                String[] student_info = message.split(":");
                modelUserList.addElement(student_info[1] + " " + student_info[2] + " " + student_info[3] + " " + student_info[4]);
                listActiveStudents.setModel(modelUserList);

            } else if (message.startsWith("REFRESH_STUDENTS")) {
                modelUserList.clear();
                listActiveStudents.setModel(modelUserList);
                writer.println("GET_STUDENTS");
            } else if (message.startsWith("STUDENT_DETAILS:")) {
                // Example: STUDENT_DETAILS:Marko:Petrovic:123:2021/001:marko:secret
                String[] parts = message.split(":");
                textFieldName.setText(parts[1]);
                textFieldLastName.setText(parts[2]);
                textFieldIndexNumber.setText(parts[3]);
                textFieldID.setText(parts[4]);
                textFieldUsername.setText(parts[5]);
                textFieldPassword.setText(parts[6]);
            } else if (message.startsWith("ASSIGNED_COURSES:")) {
                String[] parts = message.split(":", 2);  // split only into 2 parts
                DefaultListModel<String> model = new DefaultListModel<>();

                if (parts.length > 1) {
                    String[] courses = parts[1].split(";"); // now split by semicolon
                    for (String course : courses) {
                        model.addElement(course);
                    }
                }
                listAssignedCourses.setModel(model);
            } else if (message.startsWith("COURSE_SCORES:")) {
                String courseInfo = message.split(":")[1]; // get part after "COURSE_SCORES:"
                String[] categoryScores = courseInfo.split("\\|"); // split into category;score;min?max

                // Create a table model with four columns: Category, Score, Min, Max
                DefaultTableModel tableModel = new DefaultTableModel(
                        new Object[]{"Category", "Score", "Min", "Max"}, 0);

                int total = 0;
                for (String cs : categoryScores) {
                    String[] parts = cs.split(";");
                    if (parts.length == 3) {
                        String category = parts[0].trim();
                        int score = Integer.parseInt(parts[1].trim());

                        // Split min and max (e.g., "10?20")
                        String[] minMax = parts[2].split("\\?");
                        int min = Integer.parseInt(minMax[0].trim());
                        int max = Integer.parseInt(minMax[1].trim());

                        total += score;
                        tableModel.addRow(new Object[]{category, score, min, max});
                    }
                }

                tableSelectedCourse.setModel(tableModel);
                textAreaScore.setText(String.valueOf(total));
                textAreaMark.setText(String.valueOf(calcGrade(total)));
            } else if (message.startsWith("AVAILABLE_COURSES:")) {
                //AVAILABLE_COURSES:Mathematics|English...
                String coursesList = message.split(":")[1]; // get part after "AVAILABLE_COURSES:"
                String[] courses = coursesList.split("\\|"); // split into separate courses, ...
                for (String courseName : courses) {
                    comboBoxNewCourse1.addItem(courseName);
                }
            } else if (message.startsWith("REFRESH_COURSES")) {
                comboBoxNewCourse1.removeAllItems();
                writer.println("GET_AVAILABLE_COURSES");
            } else if (message.startsWith("DETAILED_COURSE:")) {
                String detailsPart = message.split(":", 2)[1]; // "Homework;10?20|Exam;30?50|Project;15?30"
                String[] splitParts = detailsPart.split("\\|");

                DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Category", "Score", "Min Score", "Max Score"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 1; // Only "Score" column is editable
                    }
                };
                tableModel.setRowCount(0);
                for (String splitPart : splitParts) {
                    String[] categoryAndPoints = splitPart.split(";"); // "Homework", "10?20"
                    String category = categoryAndPoints[0].trim();
                    int minScore = 0;
                    int maxScore = 0;

                    if (categoryAndPoints.length == 2) {
                        String[] minMax = categoryAndPoints[1].split("\\?");
                        if (minMax.length == 2) {
                            minScore = Integer.parseInt(minMax[0].trim());
                            maxScore = Integer.parseInt(minMax[1].trim());
                        }
                    }
                    tableModel.addRow(new Object[]{category, 0, minScore, maxScore});
                }
                tableCourses1.setModel(tableModel);
            }
        });
    }

    private int calcGrade(int score) {
        if (score <= 50) {
            return 5;
        }
        if (score <= 60) {
            return 6;
        }
        if (score <= 70) {
            return 7;
        }
        if (score <= 80) {
            return 8;
        }
        if (score <= 90) {
            return 9;
        }
        return 10;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        labelUsername = new javax.swing.JLabel();
        labelPassword = new javax.swing.JLabel();
        textFieldPassword = new javax.swing.JTextField();
        buttonStudentUpdate = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listAssignedCourses = new javax.swing.JList<>();
        labelAssignedCourses = new javax.swing.JLabel();
        labelCourseName = new javax.swing.JLabel();
        textFieldCourseName = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableSelectedCourse = new javax.swing.JTable();
        labelEditAllCourseFields = new javax.swing.JLabel();
        labelScore = new javax.swing.JLabel();
        buttonCoursesAdd = new javax.swing.JButton();
        labelMark = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        textAreaMark = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        textAreaScore = new javax.swing.JTextArea();
        labelActiveStudents = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listActiveStudents = new javax.swing.JList<>();
        labelName = new javax.swing.JLabel();
        labelLastName = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        labelID = new javax.swing.JLabel();
        labelCourses = new javax.swing.JLabel();
        labelIndexNumber = new javax.swing.JLabel();
        labelInspectStudentSubTitle = new javax.swing.JLabel();
        textFieldIndexNumber = new javax.swing.JTextField();
        textFieldName = new javax.swing.JTextField();
        textFieldUsername = new javax.swing.JTextField();
        textFieldLastName = new javax.swing.JTextField();
        textFieldEditAllCourseFields = new javax.swing.JTextField();
        textFieldID = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        tableCourses = new javax.swing.JTable();
        labelAdminPanelTitle = new javax.swing.JLabel();
        labelEditCourses = new javax.swing.JLabel();
        labelSelectedCourse = new javax.swing.JLabel();
        buttonStudentAdd = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        textFieldName1 = new javax.swing.JTextField();
        textFieldLastName1 = new javax.swing.JTextField();
        textFieldID1 = new javax.swing.JTextField();
        textFieldIndexNumber1 = new javax.swing.JTextField();
        textFieldUsername1 = new javax.swing.JTextField();
        textFieldPassword1 = new javax.swing.JTextField();
        comboBoxNewCourse1 = new javax.swing.JComboBox<>();
        labelNewCourse1 = new javax.swing.JLabel();
        labelName1 = new javax.swing.JLabel();
        labelLastName1 = new javax.swing.JLabel();
        labelID1 = new javax.swing.JLabel();
        labelIndexNumber1 = new javax.swing.JLabel();
        labelUsername1 = new javax.swing.JLabel();
        labelPassword1 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        listAssignedCourses1 = new javax.swing.JList<>();
        labelAssignedCourses1 = new javax.swing.JLabel();
        buttonCoursesAdd1 = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        tableCourses1 = new javax.swing.JTable();
        jScrollPane10 = new javax.swing.JScrollPane();
        textAreaScore1 = new javax.swing.JTextArea();
        labelScore1 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        textAreaMark1 = new javax.swing.JTextArea();
        labelMark1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        labelAdminUsername = new javax.swing.JLabel();
        labelAdminPassword = new javax.swing.JLabel();
        textFieldAdminUsername = new javax.swing.JTextField();
        textFieldAdminPassword = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        labelCourses1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setForeground(java.awt.Color.black);

        labelUsername.setText("Username");

        labelPassword.setText("Password");

        textFieldPassword.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldPassword.setPreferredSize(new java.awt.Dimension(100, 25));

        buttonStudentUpdate.setText("Update Student");
        buttonStudentUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStudentUpdateActionPerformed(evt);
            }
        });

        listAssignedCourses.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listAssignedCoursesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listAssignedCourses);

        labelAssignedCourses.setText("Assigned Courses");

        labelCourseName.setText("Course Name");

        tableSelectedCourse.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tableSelectedCourse);

        labelEditAllCourseFields.setText("Edit Course Fields");

        labelScore.setText("Score");

        buttonCoursesAdd.setText("Add Course");
        buttonCoursesAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCoursesAddActionPerformed(evt);
            }
        });

        labelMark.setText("Mark");

        jScrollPane4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane4.setHorizontalScrollBar(null);
        jScrollPane4.setInheritsPopupMenu(true);

        textAreaMark.setEditable(false);
        textAreaMark.setColumns(20);
        textAreaMark.setRows(1);
        textAreaMark.setAutoscrolls(false);
        jScrollPane4.setViewportView(textAreaMark);

        jScrollPane5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane5.setHorizontalScrollBar(null);
        jScrollPane5.setInheritsPopupMenu(true);

        textAreaScore.setEditable(false);
        textAreaScore.setColumns(20);
        textAreaScore.setRows(1);
        textAreaScore.setAutoscrolls(false);
        jScrollPane5.setViewportView(textAreaScore);

        labelActiveStudents.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        labelActiveStudents.setText("Active Students");

        listActiveStudents.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listActiveStudents.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listActiveStudentsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listActiveStudents);

        labelName.setText("Name");

        labelLastName.setText("Last Name");

        labelID.setText("ID");

        labelCourses.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        labelCourses.setText("Categories preview");

        labelIndexNumber.setText("Index Number");

        labelInspectStudentSubTitle.setFont(new java.awt.Font("Cantarell", 1, 20)); // NOI18N
        labelInspectStudentSubTitle.setText("Inspect Student");

        textFieldIndexNumber.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldIndexNumber.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldName.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldName.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldUsername.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldUsername.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldLastName.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldLastName.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldEditAllCourseFields.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldEditAllCourseFieldsActionPerformed(evt);
            }
        });

        textFieldID.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldID.setPreferredSize(new java.awt.Dimension(100, 25));

        tableCourses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane7.setViewportView(tableCourses);

        labelAdminPanelTitle.setFont(new java.awt.Font("Cantarell", 1, 24)); // NOI18N
        labelAdminPanelTitle.setText("Admin Panel");

        labelEditCourses.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        labelEditCourses.setText("Add Courses");

        labelSelectedCourse.setFont(new java.awt.Font("Cantarell", 0, 16)); // NOI18N
        labelSelectedCourse.setText("Selected Course");

        buttonStudentAdd.setText("Add Student");
        buttonStudentAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStudentAddActionPerformed(evt);
            }
        });

        jSeparator2.setForeground(new java.awt.Color(0, 51, 0));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel1.setText("Add Student");

        textFieldName1.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldName1.setPreferredSize(new java.awt.Dimension(100, 25));
        textFieldName1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldName1ActionPerformed(evt);
            }
        });

        textFieldLastName1.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldLastName1.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldID1.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldID1.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldIndexNumber1.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldIndexNumber1.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldUsername1.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldUsername1.setPreferredSize(new java.awt.Dimension(100, 25));

        textFieldPassword1.setMinimumSize(new java.awt.Dimension(100, 25));
        textFieldPassword1.setPreferredSize(new java.awt.Dimension(100, 25));

        comboBoxNewCourse1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxNewCourse1ActionPerformed(evt);
            }
        });

        labelNewCourse1.setText("Select Course");

        labelName1.setText("Name");

        labelLastName1.setText("Last Name");

        labelID1.setText("ID");

        labelIndexNumber1.setText("Index Number");

        labelUsername1.setText("Username");

        labelPassword1.setText("Password");

        listAssignedCourses1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listAssignedCourses1MouseClicked(evt);
            }
        });
        listAssignedCourses1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listAssignedCourses1ValueChanged(evt);
            }
        });
        jScrollPane9.setViewportView(listAssignedCourses1);

        labelAssignedCourses1.setText("Assigned Courses");

        buttonCoursesAdd1.setText("Add Course");
        buttonCoursesAdd1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCoursesAdd1ActionPerformed(evt);
            }
        });

        tableCourses1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane8.setViewportView(tableCourses1);

        jScrollPane10.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane10.setHorizontalScrollBar(null);
        jScrollPane10.setInheritsPopupMenu(true);

        textAreaScore1.setEditable(false);
        textAreaScore1.setColumns(20);
        textAreaScore1.setRows(1);
        textAreaScore1.setAutoscrolls(false);
        jScrollPane10.setViewportView(textAreaScore1);

        labelScore1.setText("Score");

        jScrollPane11.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane11.setHorizontalScrollBar(null);
        jScrollPane11.setInheritsPopupMenu(true);

        textAreaMark1.setEditable(false);
        textAreaMark1.setColumns(20);
        textAreaMark1.setRows(1);
        textAreaMark1.setAutoscrolls(false);
        jScrollPane11.setViewportView(textAreaMark1);

        labelMark1.setText("Mark");

        jLabel2.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel2.setText("Add Admin");

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);

        labelAdminUsername.setText("Username");

        labelAdminPassword.setText("Password");

        textFieldAdminPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldAdminPasswordActionPerformed(evt);
            }
        });

        jButton1.setText("Add Admin");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("How to Use:\n\n...\n");
        jScrollPane12.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setText("You can add new courses by specifying course name and\nits course fields. After you specify the course fields press ENTER \nto get the preview\n\nCourse fields must be in format:\nCATEGORY?MIN_POINTS:MAX_POINTS | ...\nFor example:\n\nT1?15:30|T2?15:30|Z1?6:10|Z2?15:30");
        jScrollPane13.setViewportView(jTextArea2);

        labelCourses1.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        labelCourses1.setText("How to Add Courses");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(labelEditCourses))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelAdminPanelTitle)
                                    .addComponent(jLabel2)))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(29, 29, 29)
                                        .addComponent(jButton1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(labelAdminUsername)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(labelAdminPassword)
                                                .addGap(8, 8, 8)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(textFieldAdminPassword)
                                            .addComponent(textFieldAdminUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                                    .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(9, 9, 9)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(labelSelectedCourse)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelActiveStudents)
                                            .addComponent(labelInspectStudentSubTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(labelName, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelLastName, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelID, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelIndexNumber, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelUsername, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelPassword, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(textFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textFieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textFieldIndexNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textFieldUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textFieldLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelAssignedCourses)))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(180, 180, 180)
                                .addComponent(labelScore)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(buttonStudentUpdate))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelMark)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addContainerGap(10, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelIndexNumber1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(textFieldIndexNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelID1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(textFieldID1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelName1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(textFieldName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(labelLastName1)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(textFieldLastName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(labelScore1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(labelMark1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(labelPassword1)
                                                    .addComponent(labelUsername1))
                                                .addGap(27, 27, 27)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(textFieldPassword1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(textFieldUsername1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(labelNewCourse1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(comboBoxNewCourse1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(buttonStudentAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonCoursesAdd1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelAssignedCourses1)))
                                .addGap(12, 12, 12)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelCourseName)
                                    .addComponent(labelEditAllCourseFields)
                                    .addComponent(buttonCoursesAdd))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(textFieldCourseName, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                                    .addComponent(textFieldEditAllCourseFields))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelCourses)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelCourses1)
                                .addGap(341, 341, 341))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(labelAdminPanelTitle)
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textFieldAdminUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelAdminUsername))
                                .addGap(11, 11, 11)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelAdminPassword)
                                    .addComponent(textFieldAdminPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1)
                                .addGap(26, 26, 26)
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(labelInspectStudentSubTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelName)
                                    .addComponent(labelAssignedCourses))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(textFieldLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelLastName))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(textFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelID))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(textFieldIndexNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelIndexNumber))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(textFieldUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelUsername))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(textFieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelPassword)))
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(labelSelectedCourse)
                                .addGap(4, 4, 4)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelScore)
                                        .addComponent(labelMark))
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonStudentUpdate)
                                .addGap(4, 4, 4))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(labelActiveStudents)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelAssignedCourses1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(textFieldID1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelID1))
                            .addComponent(buttonCoursesAdd1, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(textFieldIndexNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelIndexNumber1))
                            .addComponent(buttonStudentAdd)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textFieldName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelName1)))
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(labelNewCourse1)
                                            .addComponent(comboBoxNewCourse1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(7, 7, 7)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(textFieldUsername1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelUsername1)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(31, 31, 31)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(labelMark1)
                                            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(labelScore1)
                                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textFieldPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelPassword1)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(textFieldLastName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelLastName1)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelEditCourses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCourses)
                    .addComponent(labelCourses1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCourseName)
                    .addComponent(textFieldCourseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelEditAllCourseFields)
                    .addComponent(textFieldEditAllCourseFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonCoursesAdd)
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonStudentUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStudentUpdateActionPerformed
        try {
            // Collect student info from text fields
            String name = textFieldName.getText().trim();
            String lastName = textFieldLastName.getText().trim();
            String id = textFieldID.getText().trim();
            String indexNumber = textFieldIndexNumber.getText().trim();
            String username = textFieldUsername.getText().trim();
            String password = textFieldPassword.getText().trim();

            String selectedCourseName = listAssignedCourses.getSelectedValue();
            // Collect scores from tableSelectedCourse
            StringBuilder scoresBuilder = new StringBuilder();
            DefaultTableModel model = (DefaultTableModel) tableSelectedCourse.getModel();

            for (int i = 0; i < model.getRowCount(); i++) {
                String category = model.getValueAt(i, 0).toString(); // first col = category
                String score = model.getValueAt(i, 1).toString();    // second col = score
                scoresBuilder.append(category)
                        .append(";")
                        .append(score);

                if (i < model.getRowCount() - 1) {
                    scoresBuilder.append("|");
                }
            }

            String scores = scoresBuilder.toString();

            String message = "UPDATE_STUDENT:"
                    + selectedStudentUsername
                    + ";" + username
                    + ";" + password
                    + ";" + id
                    + ";" + indexNumber
                    + ";" + name
                    + ";" + lastName
                    + ";" + selectedCourseName
                    + ";" + scores;

            JOptionPane.showMessageDialog(this, "Student update sent to server!");
            writer.println(message);
            System.out.println(message);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error while updating student: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_buttonStudentUpdateActionPerformed

    private void buttonCoursesAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCoursesAddActionPerformed
        String courseName = textFieldCourseName.getText().trim();
        String fieldsInput = textFieldEditAllCourseFields.getText().trim();

        // Validate empty fields
        if (courseName.isEmpty() || fieldsInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course name and fields cannot be empty!",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Split categories
        String[] categories = fieldsInput.split("\\|");
        int totalMax = 0;
        int totalMin = 0;

        // Prepare the validated string to send
        List<String> validFields = new ArrayList<>();

        for (String cat : categories) {
            String[] parts = cat.split("\\?");
            if (parts.length != 2) {
                JOptionPane.showMessageDialog(this, "Invalid format. Each field must be CATEGORY?MIN:MAX",
                        "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String catName = parts[0].trim();
            String[] minMax = parts[1].split(":");
            if (minMax.length != 2) {
                JOptionPane.showMessageDialog(this, "Invalid format. Each field must have MIN:MAX",
                        "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int minPoints, maxPoints;
            try {
                minPoints = Integer.parseInt(minMax[0].trim());
                maxPoints = Integer.parseInt(minMax[1].trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Min and Max points must be integers",
                        "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validation rules
            if (minPoints < 0 || minPoints > 100 || maxPoints < 0 || maxPoints > 100) {
                JOptionPane.showMessageDialog(this, "Points must be between 0 and 100",
                        "Value Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (minPoints > maxPoints) {
                JOptionPane.showMessageDialog(this, "Min points cannot be greater than Max points",
                        "Value Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            totalMin += minPoints;
            totalMax += maxPoints;

            validFields.add(catName + ";" + minPoints + "?" + maxPoints);
        }

        // Total validation
        if (totalMin < 51) {
            JOptionPane.showMessageDialog(this, "Sum of min points must be at least 51",
                    "Value Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (totalMax != 100) {
            JOptionPane.showMessageDialog(this, "Sum of max points must be exactly 100",
                    "Value Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String messageToServer = "ADD_COURSE:" + courseName + ":" + String.join("|", validFields);

        try {
            writer.println(messageToServer);
            JOptionPane.showMessageDialog(this, "Course added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to send message to server: " + e.getMessage(),
                    "Network Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_buttonCoursesAddActionPerformed

    private void listActiveStudentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listActiveStudentsMouseClicked
        // Reset Table
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Category", "Score", "Min Score", "Max Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only "Score" column is editable
            }
        };
        tableSelectedCourse.setModel(tableModel);
        textAreaScore.setText(String.valueOf(0));
        textAreaMark.setText(String.valueOf(5));

        String[] selectedStudentParts = listActiveStudents.getSelectedValue().split(" ");

        if (selectedStudentParts != null) {
            selectedStudentUsername = selectedStudentParts[3];
            System.out.println("selectedStudentUsername: " + selectedStudentUsername);
            writer.println("GET_STUDENT_DETAILS:" + selectedStudentUsername);
        }
    }//GEN-LAST:event_listActiveStudentsMouseClicked

    private void listAssignedCoursesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAssignedCoursesMouseClicked
        String courseName = listAssignedCourses.getSelectedValue();
        // Since Student is already selected, the database can preload its data
        // and hold it until other student is selected
        if (courseName != null && selectedStudentUsername != null) {
            writer.println("GET_COURSE_SCORES:" + courseName);
        }
    }//GEN-LAST:event_listAssignedCoursesMouseClicked

    private void textFieldName1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldName1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textFieldName1ActionPerformed

    private void buttonStudentAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStudentAddActionPerformed
        String name = textFieldName1.getText().trim();
        String lastName = textFieldLastName1.getText().trim();
        String id = textFieldID1.getText().trim();
        String indexNumber = textFieldIndexNumber1.getText().trim();
        String username = textFieldUsername1.getText().trim();
        String password = textFieldPassword1.getText().trim();

        // CHECK IF THERE IS ALREADY A USERNAME
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Name is blank!.");
            return;
        }
        if (lastName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Last Name is blank!.");
            return;
        }
        if (!id.matches("\\d{13}")) {
            JOptionPane.showMessageDialog(this, "ID must be exactly 13 digits.");
            return;
        }

        if (!indexNumber.matches("[Ee][1-3][/-](200[0-9]|201[0-9]|202[0-3])")) {
            JOptionPane.showMessageDialog(this, "Index number must be in format E1-2015 or e2/2020.");
            return;
        }

        if (username.isBlank()) {
            JOptionPane.showMessageDialog(this, "Username is blank!.");
            return;
        }
        if (password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Password is blank!.");
            return;
        }

        // Start building base message
        String message = "ADD_STUDENT:"
                + name + ";"
                + lastName + ";"
                + id + ";"
                + indexNumber + ";"
                + username + ";"
                + password + ":";

        // Handle courses
        List<String> courseStrings = new ArrayList<>();
        for (Course c : takenCourses) {
            // Course base part
            String coursePart = c.getCourseName() + "?";

            List<String> catScorePairs = new ArrayList<>();
            for (int i = 0; i < c.getCourseCategories().size(); i++) {
                String category = c.getCourseCategories().get(i);
                Integer score = (i < c.getScores().size()) ? c.getScores().get(i) : 0;
                catScorePairs.add(category + "|" + score);
            }

            // Join category|score parts
            coursePart += String.join(".", catScorePairs);
            courseStrings.add(coursePart);
        }

        // Append courses (semicolon-separated)
        if (!courseStrings.isEmpty()) {
            message += String.join(";", courseStrings);
        }

        // For now just print (later send to server socket/output stream)
        System.out.println("Sending message to server: " + message);
        writer.println(message);
    }//GEN-LAST:event_buttonStudentAddActionPerformed

    private void listAssignedCourses1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAssignedCourses1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_listAssignedCourses1MouseClicked

    private void listAssignedCourses1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listAssignedCourses1ValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_listAssignedCourses1ValueChanged

    private void buttonCoursesAdd1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCoursesAdd1ActionPerformed
        String courseName = (String) comboBoxNewCourse1.getSelectedItem();
        if (courseName != null) {
            // Update selectedCourse with new course info
            selectedCourse = new Course();
            selectedCourse.setCourseName(courseName);

            // Clear old data
            selectedCourse.getCourseCategories().clear();
            selectedCourse.getScores().clear();
            selectedCourse.getMinScores().clear();
            selectedCourse.getMaxScores().clear();

            DefaultTableModel model = (DefaultTableModel) tableCourses1.getModel();
            int rowCount = model.getRowCount();
            int total = 0;
            boolean isPass = true;
            for (int i = 0; i < rowCount; i++) {
                String category = (String) model.getValueAt(i, 0);
                String scoreStr = model.getValueAt(i, 1).toString();
                String minStr = model.getValueAt(i, 2).toString();
                String maxStr = model.getValueAt(i, 3).toString();

                int score = Integer.parseInt(scoreStr);
                int minScore = Integer.parseInt(minStr);
                int maxScore = Integer.parseInt(maxStr);
                total += score;

                if (score < minScore) {
                    isPass = false;
                }

                if (score < 0 || score > maxScore) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Score for " + category + " must be between " + 0 + " and " + maxScore,
                            "Invalid Score",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                selectedCourse.getCourseCategories().add(category);
                selectedCourse.getScores().add(score);
                selectedCourse.getMinScores().add(minScore);
                selectedCourse.getMaxScores().add(maxScore);
            }
            textAreaScore1.setText(String.valueOf(total));

            if (isPass) {
                textAreaMark1.setText(String.valueOf(calcGrade(total)));
            } else {
                textAreaMark1.setText("5");
            }

            System.out.println("Updated selectedCourse:");
            System.out.println("Course Name: " + selectedCourse.getCourseName());
            System.out.println("Categories: " + selectedCourse.getCourseCategories());
            System.out.println("Scores: " + selectedCourse.getScores());
            System.out.println("Min Scores: " + selectedCourse.getMinScores());
            System.out.println("Max Scores: " + selectedCourse.getMaxScores());
        }
        takenCourses.add(selectedCourse);
        modelAddedCourses.addElement(selectedCourse.getCourseName());
        listAssignedCourses1.setModel(modelAddedCourses);
    }//GEN-LAST:event_buttonCoursesAdd1ActionPerformed

    private void comboBoxNewCourse1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxNewCourse1ActionPerformed
        textAreaMark1.setText("5");
        textAreaScore1.setText("0");
        String courseName = (String) comboBoxNewCourse1.getSelectedItem();
        if (courseName != null)
            writer.println("GET_COURSE_DETAILS:" + courseName);
    }//GEN-LAST:event_comboBoxNewCourse1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String username = textFieldAdminUsername.getText();
        String password = textFieldAdminPassword.getText();
        if (username.isBlank()) {
            JOptionPane.showMessageDialog(this, "Username is blank!.");
            return;
        }
        if (password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Password is blank!.");
            return;
        }
        System.out.println("ADD_ADMIN:" + username + "|" + password);
        writer.println("ADD_ADMIN:" + username + "|" + password);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void textFieldAdminPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldAdminPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textFieldAdminPasswordActionPerformed

    private void textFieldEditAllCourseFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldEditAllCourseFieldsActionPerformed
        String input = textFieldEditAllCourseFields.getText().trim();
        if (input.isEmpty()) {
            ((DefaultTableModel) tableCourses.getModel()).setRowCount(0);
            return;
        }

        String[] categories = input.split("\\|");
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Category", "Min Score", "Max Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int totalMin = 0;
        int totalMax = 0;
        boolean invalidInput = false;

        for (String cat : categories) {
            try {
                String[] parts = cat.split("\\?");
                if (parts.length != 2) {
                    invalidInput = true;
                    break;
                }

                String catName = parts[0].trim();
                String[] minMax = parts[1].split(":");
                if (minMax.length != 2) {
                    invalidInput = true;
                    break;
                }

                int minScore = Integer.parseInt(minMax[0].trim());
                int maxScore = Integer.parseInt(minMax[1].trim());

                // Validation
                if (minScore < 0 || minScore > 100 || maxScore < 0 || maxScore > 100) {
                    invalidInput = true;
                    break;
                }
                if (minScore > maxScore) {
                    invalidInput = true;
                    break;
                }

                totalMin += minScore;
                totalMax += maxScore;

                model.addRow(new Object[]{catName, minScore, maxScore});
            } catch (Exception ex) {
                invalidInput = true;
                break;
            }
        }

        // Validate total scores
        if (invalidInput || totalMax != 100 || totalMin < 51) {
            JOptionPane.showMessageDialog(null,
                    "Invalid course input!\n- Max scores must sum to 100\n- Min scores must sum at least 51\n- Scores must be 0-100\n- Min cannot be higher than Max",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ((DefaultTableModel) tableCourses.getModel()).setRowCount(0); // Clear table on error
            return;
        }

        tableCourses.setModel(model);

    }//GEN-LAST:event_textFieldEditAllCourseFieldsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCoursesAdd;
    private javax.swing.JButton buttonCoursesAdd1;
    private javax.swing.JButton buttonStudentAdd;
    private javax.swing.JButton buttonStudentUpdate;
    private javax.swing.JComboBox<String> comboBoxNewCourse1;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JLabel labelActiveStudents;
    private javax.swing.JLabel labelAdminPanelTitle;
    private javax.swing.JLabel labelAdminPassword;
    private javax.swing.JLabel labelAdminUsername;
    private javax.swing.JLabel labelAssignedCourses;
    private javax.swing.JLabel labelAssignedCourses1;
    private javax.swing.JLabel labelCourseName;
    private javax.swing.JLabel labelCourses;
    private javax.swing.JLabel labelCourses1;
    private javax.swing.JLabel labelEditAllCourseFields;
    private javax.swing.JLabel labelEditCourses;
    private javax.swing.JLabel labelID;
    private javax.swing.JLabel labelID1;
    private javax.swing.JLabel labelIndexNumber;
    private javax.swing.JLabel labelIndexNumber1;
    private javax.swing.JLabel labelInspectStudentSubTitle;
    private javax.swing.JLabel labelLastName;
    private javax.swing.JLabel labelLastName1;
    private javax.swing.JLabel labelMark;
    private javax.swing.JLabel labelMark1;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelName1;
    private javax.swing.JLabel labelNewCourse1;
    private javax.swing.JLabel labelPassword;
    private javax.swing.JLabel labelPassword1;
    private javax.swing.JLabel labelScore;
    private javax.swing.JLabel labelScore1;
    private javax.swing.JLabel labelSelectedCourse;
    private javax.swing.JLabel labelUsername;
    private javax.swing.JLabel labelUsername1;
    private javax.swing.JList<String> listActiveStudents;
    private javax.swing.JList<String> listAssignedCourses;
    private javax.swing.JList<String> listAssignedCourses1;
    private javax.swing.JTable tableCourses;
    private javax.swing.JTable tableCourses1;
    private javax.swing.JTable tableSelectedCourse;
    private javax.swing.JTextArea textAreaMark;
    private javax.swing.JTextArea textAreaMark1;
    private javax.swing.JTextArea textAreaScore;
    private javax.swing.JTextArea textAreaScore1;
    private javax.swing.JTextField textFieldAdminPassword;
    private javax.swing.JTextField textFieldAdminUsername;
    private javax.swing.JTextField textFieldCourseName;
    private javax.swing.JTextField textFieldEditAllCourseFields;
    private javax.swing.JTextField textFieldID;
    private javax.swing.JTextField textFieldID1;
    private javax.swing.JTextField textFieldIndexNumber;
    private javax.swing.JTextField textFieldIndexNumber1;
    private javax.swing.JTextField textFieldLastName;
    private javax.swing.JTextField textFieldLastName1;
    private javax.swing.JTextField textFieldName;
    private javax.swing.JTextField textFieldName1;
    private javax.swing.JTextField textFieldPassword;
    private javax.swing.JTextField textFieldPassword1;
    private javax.swing.JTextField textFieldUsername;
    private javax.swing.JTextField textFieldUsername1;
    // End of variables declaration//GEN-END:variables
}
