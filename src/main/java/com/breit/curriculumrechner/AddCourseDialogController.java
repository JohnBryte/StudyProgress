package com.breit.curriculumrechner;

import com.breit.curriculumrechner.datamodel.Course;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class AddCourseDialogController {
    @FXML
    private Label headerText;
    @FXML
    private TextField moduleNameField;
    @FXML
    private TextField courseNameField;
    @FXML
    private TextField ectsField;
    @FXML
    private TextField semesterField;
    @FXML
    private ColorPicker colorPickerField;
    private boolean editing = false;
    private BooleanBinding inputsFull;

    public BooleanBinding inputsFullBinding() {
        return inputsFull;
    }

    public void prefillCourse(Course course){
        moduleNameField.setText(course.getModuleName());
        courseNameField.setText(course.getCourseName());
        ectsField.setText(Integer.toString(course.getEcts()));
        semesterField.setText(course.getSemester());
        colorPickerField.setValue(course.getColor());
        editing = true;
    }

    public void initialize(){
        String headerString = editing ? "Edit this course" : "Add this course";
        System.out.println(headerString);


        ectsField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            try{
                if(newValue.isEmpty()){
                    ectsField.setText("");
                } else {
                    Integer.parseInt(newValue);
                }

            } catch (NumberFormatException e){
                ectsField.setText(oldValue);
            }
        });

        semesterField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            try{
                if(newValue.isEmpty()){
                    semesterField.setText("");
                } else {
                    Integer.parseInt(newValue);
                }

            } catch (NumberFormatException e){
                semesterField.setText(oldValue);
            }
        });

        inputsFull = new BooleanBinding() {
            {
                bind(moduleNameField.textProperty(), courseNameField.textProperty(), ectsField.textProperty(), semesterField.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return  moduleNameField.getText().trim().isEmpty() ||
                        courseNameField.getText().trim().isEmpty() ||
                        ectsField.getText().isEmpty() ||
                        semesterField.getText().isEmpty();
            }
        };
    }

    public Course processResults(){
        String moduleName = moduleNameField.getText().trim();
        String courseName = courseNameField.getText().trim();
        int ects = Integer.parseInt(ectsField.getText().trim());
        int semester = Integer.parseInt(semesterField.getText().trim());
        Color color = colorPickerField.getValue();

        Course newCourse = new Course(moduleName, courseName, ects, semester, color);
//        CourseData.getInstance().addTodoItem(newItem);
        return newCourse;
    }

}
