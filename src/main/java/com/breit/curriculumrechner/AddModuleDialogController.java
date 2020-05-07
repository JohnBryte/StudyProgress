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
    @FXML
    private ChoiceBox choiceBox;

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


    private int ectsTotal = 180;


    public void initialize() {
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (choiceBox.getItems().get((Integer) t1).equals("Bachelor")){
                    ectsTotal = 180;
                } else {
                    ectsTotal = 120;
                }
            }
        });

        rows.addListener((ListChangeListener<? super Row>) c -> {
            addCourseButton.disableProperty().unbind();
            BooleanExpression allEmpty = rows.stream()
                    .map(r -> BooleanExpression.booleanExpression(r.allFieldsFilledProperty()))
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
            Course course = new Course(moduleName, row.getName(), Integer.parseInt(row.getEcts()), Integer.parseInt(row.getSemester()), color);
            courses.add(course);
        }
//        CourseData.getInstance().setCourses(courses);
        return courses;
    }

    public int getEctsTotal(){
        return ectsTotal;
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
        Label semesterLabel = new Label("Semester: ");
        TextField newSemesterField = new TextField();
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

        newSemesterField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                try{
                    if(newValue.isEmpty()){
                        newSemesterField.setText("");
                    } else {
                        Integer.parseInt(newValue);
                    }

                } catch (NumberFormatException e){
                    newSemesterField.setText(oldValue);
                }
            }
        });

        List<TextField> temp = new ArrayList<TextField>();
        temp.add(newNameField);
        temp.add(newEctsField);
        temp.add(newSemesterField);

        textFields.add(temp);
        buttons.add(newAddCourseButton);

        Row row = new Row();
        row.nameProperty().bind(newNameField.textProperty());
        row.ectsProperty().bind(newEctsField.textProperty());
        row.semesterProperty().bind(newSemesterField.textProperty());

        row.allFieldsFilledProperty().bind(Bindings.createBooleanBinding(()->
                newNameField.getText().trim().isEmpty() || newEctsField.getText().trim().isEmpty() || newSemesterField.getText().trim().isEmpty(),
                newNameField.textProperty(), newEctsField.textProperty(), newSemesterField.textProperty()));

        row.lastElementProperty().bind(Bindings.size(rows));

        rows.add(row);

        box.getChildren().addAll(nameLabel, newNameField, ectsLabel, newEctsField, semesterLabel, newSemesterField, newDeleteCourseButton);
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
