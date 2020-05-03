package com.breit.curriculumrechner;

import com.breit.curriculumrechner.datamodel.CourseData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/mainwindow.fxml"));
        primaryStage.setTitle("Curriculum Rechner");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        CourseData cd = CourseData.getInstance();
        try{
            cd.getInstance().loadCourses("courses.xlsx");
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
