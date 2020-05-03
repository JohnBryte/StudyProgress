package com.breit.curriculumrechner;

import com.breit.curriculumrechner.datamodel.Course;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class AddModuleDialogController {
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField moduleNameField = new TextField();
    @FXML
    private ColorPicker colorPickerField;
    @FXML
    private VBox root;
    @FXML
    private TextField courseNameField = new TextField();
    @FXML
    private TextField courseEctsField = new TextField();
    @FXML
    private Button addCourseButton;

    private ObservableList<Row> rows = FXCollections.observableArrayList();
    private List<List<TextField>> textFields = new ArrayList<>();
    private List<Button> buttons = new ArrayList<>();

    private BooleanBinding inputsFull;

    public BooleanBinding inputsFullBinding() {
        return inputsFull;
    }
    public final boolean getInputsFull(){
        return inputsFull.get();
    }



    public void initialize() {

        rows.addListener((ListChangeListener<? super Row>) c -> {
            addCourseButton.disableProperty().unbind();
            BooleanExpression allEmpty = rows.stream()
                    .map(r -> BooleanExpression.booleanExpression(r.nameAndEctsProperty()))
                    .reduce(new SimpleBooleanProperty(false), BooleanExpression::or);
            addCourseButton.disableProperty().bind(allEmpty.or(moduleNameField.textProperty().isEmpty()));
        });

        addCourseHBox();


        inputsFull = new BooleanBinding() {
            {
                bind(addCourseButton.disableProperty());
            }

            @Override
            protected boolean computeValue() {
                return addCourseButton.isDisabled();
            }
        };
    }

    public List<Course> processResults(){

        String moduleName = moduleNameField.getText().trim();
        Color color = colorPickerField.getValue();
        List<Course> courses = new ArrayList<>();
        for(Row row : rows){
            Course course = new Course(moduleName, row.getName(), Integer.parseInt(row.getEcts()), color);
            courses.add(course);
        }
//        CourseData.getInstance().setCourses(courses);
        return courses;
    }

    @FXML
    private void click(){
//        textFields.get(textFields.size()-1).get(0).setDisable(true);
//        textFields.get(textFields.size()-1).get(1).setDisable(true);
        addCourseHBox();

    }

    @FXML
    private void addCourseHBox(){
        HBox box = new HBox(10);
        Label nameLabel = new Label("Course name: ");
        TextField newNameField = new TextField();
        Label ectsLabel = new Label("Ects: ");
        TextField newEctsField = new TextField();
        Button newAddCourseButton = new Button("add Course");
        Button newDeleteCourseButton = new Button("delete Course");

        newEctsField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                try{
                    if(newValue.isEmpty()){
                        newEctsField.setText("");
                    } else {
                        Integer.parseInt(newValue);
                    }

                } catch (NumberFormatException e){
                    newEctsField.setText(oldValue);
                }
            }
        });

        List<TextField> temp = new ArrayList<TextField>();
        temp.add(newNameField);
        temp.add(newEctsField);

        textFields.add(temp);
        buttons.add(newAddCourseButton);

        Row row = new Row();
        row.nameProperty().bind(newNameField.textProperty());
        row.ectsProperty().bind(newEctsField.textProperty());
//        row.nameAndEctsProperty().bind(newNameField.textProperty().isEmpty().or(newEctsField.textProperty().isEmpty()));
        row.nameAndEctsProperty().bind(Bindings.createBooleanBinding(()->
                newNameField.getText().trim().isEmpty() || newEctsField.getText().trim().isEmpty(), newNameField.textProperty(), newEctsField.textProperty()));

        row.lastElementProperty().bind(Bindings.size(rows));

        rows.add(row);
//        newAddCourseButton.disableProperty().bind(
//                Bindings.isEmpty(row.nameProperty())
//            .or(Bindings.isEmpty(row.ectsProperty()))
//            .or(Bindings.isEmpty(moduleNameField.textProperty()))
//        );

//        newAddCourseButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
////                newAddCourseButton.disableProperty().unbind();
//                addCourseHBox();
//                newNameField.setDisable(true);
//                newEctsField.setDisable(true);
////                newAddCourseButton.setDisable(true);
//            }
//        });


        box.getChildren().addAll(nameLabel, newNameField, ectsLabel, newEctsField, newDeleteCourseButton);
        root.getChildren().addAll(box);

        newDeleteCourseButton.disableProperty().bind(
                Bindings.size(root.getChildren()).greaterThan(2).not()
        );


        newDeleteCourseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int idx = rows.indexOf(row);
                rows.remove(idx);

                //idx +1 bc root.getChildren() idx(0) is the first HBox with modulename
                root.getChildren().remove(idx+1);

                textFields.remove(idx);
                buttons.remove(idx);

//                textFields.get(textFields.size()-1).get(0).setDisable(false);
//                textFields.get(textFields.size()-1).get(1).setDisable(false);
            }
        });
    }
}
