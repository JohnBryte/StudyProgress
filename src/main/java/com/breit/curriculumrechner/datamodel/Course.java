package com.breit.curriculumrechner.datamodel;

import javafx.scene.paint.Color;

public class Course {
    private String moduleName;
    private String courseName;
    private int ects;
    private int semester;
    private Color color;

    public Course(String moduleName, String courseName, int ects, int semester, Color color) {
        this.moduleName = moduleName;
        this.courseName = courseName;
        this.ects = ects;
        this.semester = semester;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getSemester() {
        return Integer.toString(semester);
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getEcts() {
        return ects;
    }

    public void setEcts(int ects) {
        this.ects = ects;
    }

    public String getOrdering(boolean moduleView){
        if (moduleView){
            return this.getModuleName();
        } else {
            return this.getSemester();
        }
    }

    @Override
    public String toString() {
        return courseName;
    }
}
