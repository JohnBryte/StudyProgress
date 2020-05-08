package com.breit.curriculumrechner;

import com.breit.curriculumrechner.datamodel.Course;
import com.breit.curriculumrechner.datamodel.CourseData;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Controller {
    private Set<String> allModules = new HashSet<>();
    private int ectsEarned = 0;
    @FXML
    private TreeView courseTreeView;
    @FXML
    private TilePane tilePane;
    @FXML
    private Label ectsCounter;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Menu editMenu;
    @FXML
    private Label ectsTotal;

    private final ContextMenu contextMenu = new ContextMenu();
    private Map<String, List<Pair<Course, StackPane>>> stackpaneMapByModules = new HashMap<>();
    private Map<String, List<Pair<Course, StackPane>>> stackpaneMapBySemesters = new HashMap<>();


    public void initialize(){
        CheckBoxTreeItem<Object> root = new CheckBoxTreeItem<>("Root");
        courseTreeView.setRoot(root);

        editMenu.disableProperty().bind(Bindings.createBooleanBinding(() ->  {
                    final boolean empty = root.getChildren().isEmpty();
                    return empty;
                }, root.getChildren()
        ));

        if(CourseData.getInstance().getCourses() != null){
            loadCoursesIntoPane();
        }

        // set the cell factory
        courseTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
    }

    private void loadCoursesIntoPane(){


        for(List<Course> courseList : CourseData.getInstance().getCourses().values()){
            for (Course course : courseList){
                createCourseInPane(course);
//                createCoursePane(course);
            }
        }

        for(String module : CourseData.getInstance().getCourses().keySet()){
            addModuleToCheckBoxTree(module);
            for (Course course : CourseData.getInstance().getCourses().get(module)){
                addCourseToCheckBoxTree(course);
            }
        }

        // set the cell factory
//        courseTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        calculateEcts(courseTreeView.getRoot());
        ectsCounter.setText(Integer.toString(ectsEarned));
    }

/*    private void addCourseToModuleParentInCheckBoxTree(String module) {
        List<Course> temp = CourseData.getInstance().getCourses().get(module);
        Course course = CourseData.getInstance().getCourses().get(module).get(temp.size()-1);
        CheckBoxTreeItem<Object> currentCourse = createCheckBoxCourse(course);
        for(Object object : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem<Object> checkBoxModule = (CheckBoxTreeItem) object;
            if(checkBoxModule.getValue().toString().equals(module)){
                checkBoxModule.getChildren().add(currentCourse);
            }
        }
    }*/
    private void addCourseToCheckBoxTree(Course course) {
        String module = course.getModuleName();
        CheckBoxTreeItem<Object> currentCourse = createCheckBoxCourse(course);
        for(Object object : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem<Object> checkBoxModule = (CheckBoxTreeItem) object;
            if(checkBoxModule.getValue().toString().equals(module)){
                checkBoxModule.getChildren().add(currentCourse);
            }
        }
    }

    private void addModuleToCheckBoxTree(String moduleName) {
        CheckBoxTreeItem<Object> courseModule = new CheckBoxTreeItem<>(moduleName);
//        for(Course course : CourseData.getInstance().getCourses().get(module)){
//            CheckBoxTreeItem<Object> currentCourse = createCheckBoxCourse(course);
//            courseModule.getChildren().add(currentCourse);
//        }
        courseModule.setExpanded(true);
        courseTreeView.getRoot().getChildren().add(courseModule);
    }

    private CheckBoxTreeItem<Object> createCheckBoxCourse(Course course){
        CheckBoxTreeItem<Object> currentCourse = new CheckBoxTreeItem<>(course);
        currentCourse.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new EventHandler<CheckBoxTreeItem.TreeModificationEvent<Object>>() {
            @Override
            public void handle(CheckBoxTreeItem.TreeModificationEvent<Object> objectTreeModificationEvent) {
//                        courseClicked(course.getModuleName(), course.getCourseName());
                tickOffCourse(currentCourse);
                ectsCounter.setText(Integer.toString(ectsEarned));
            }
        });
        return currentCourse;
    }

    private void addOnMouseClick(TilePane modulePane, StackPane coursePane, Course course){
        coursePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton() == MouseButton.PRIMARY) {
                    courseClicked(course.getModuleName(), course.getCourseName());
                } else if(mouseEvent.getButton() == MouseButton.SECONDARY) {
//                    deleteCourse(modulePane, coursePane, course);
//                    openAddCourseDialog(modulePane, coursePane, course);
                    contextMenu.getItems().clear();
                    MenuItem editItem = new MenuItem("Edit");
                    editItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            openAddCourseDialog(modulePane, coursePane, course);
                        }
                    });
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            deleteCourse(modulePane, coursePane, course);
                        }
                    });

                    contextMenu.getItems().addAll(editItem, deleteItem);

                    coursePane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                        @Override
                        public void handle(ContextMenuEvent contextMenuEvent) {
                            contextMenu.show(coursePane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                        }
                    });

                }
            }
        });
    }

    private void deleteCourse(TilePane modulePane, StackPane coursePane, Course course){
        int idx = CourseData.getInstance().getCourses().get(course.getModuleName()).indexOf(course);
        deleteCourseFromPane(modulePane, coursePane, course, idx);
        deleteCourseFromCheckBoxTree(course.getModuleName(), idx);

        if(moduleIsEmpty(modulePane)){
            deleteModuleFromPane(modulePane, course.getModuleName());
        }
    }

    private boolean moduleIsEmpty(TilePane modulePane){
        return modulePane.getChildren().size() == 0;
    }

    private void deleteCourseFromPane(TilePane modulePane, StackPane coursePane, Course course, int i){

        stackpaneMapByModules.get(course.getModuleName()).remove(new Pair<Course, StackPane>(course,coursePane));
        System.out.println(CourseData.getInstance().getCourses().get(course.getModuleName()));
        System.out.println("idx: " + i);
        CourseData.getInstance().getCourses().get(course.getModuleName()).remove(course);
        coursePane.getChildren().clear();
        modulePane.getChildren().remove(i);
    }

    private void deleteCourseFromCheckBoxTree(String moduleName, int i){
        int moduleIdx = 0;
        boolean found = false;
        for(Object o : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem module = (CheckBoxTreeItem) o;
            if(module.getValue().equals(moduleName)){
                found = true;
                break;
            }
            moduleIdx++;
        }
        if(found){
            CheckBoxTreeItem moduleInTree = (CheckBoxTreeItem) courseTreeView.getRoot().getChildren().get(moduleIdx);
            moduleInTree.getChildren().remove(i);

        }
    }

    private void deleteModuleFromPane(TilePane modulePane, String moduleName){
        //Delete From Pane
        Pane temp = (Pane) modulePane.getParent().getParent();
        temp.getChildren().removeIf(o -> o.getId().equals("moduleStack" + moduleName));

        for(Object o : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem bla = (CheckBoxTreeItem) o;
            if(bla.getChildren().size() == 0){
                courseTreeView.getRoot().getChildren().remove(bla);
                break;
            }
        }
        allModules.remove(moduleName);
        CourseData.getInstance().getCourses().remove(moduleName);
    }

    private void edit(TilePane modulePane, StackPane coursePane, Course oldCourse, Course newCourse){
        int replacementIdx = CourseData.getInstance().getCourses().get(oldCourse.getModuleName()).indexOf(oldCourse);
        CourseData.getInstance().getCourses().get(oldCourse.getModuleName()).remove(oldCourse);
        CourseData.getInstance().getCourses().get(newCourse.getModuleName()).add(replacementIdx, newCourse);

        coursePane.getChildren().clear();
        Rectangle rect = drawRectangle(newCourse);
        createDescription(coursePane, newCourse, rect);
        drawCross(coursePane, newCourse, rect);

        String module = newCourse.getModuleName();
        CheckBoxTreeItem<Object> currentCourse = createCheckBoxCourse(newCourse);
        for(Object object : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem<Object> checkBoxModule = (CheckBoxTreeItem) object;
            if(checkBoxModule.getValue().toString().equals(module)){

                int idx = 0;
                for(TreeItem<Object> treeItem : checkBoxModule.getChildren()){
                    if(treeItem.getValue().toString().equals(oldCourse.getCourseName())){
                        checkBoxModule.getChildren().remove(idx);
                        CheckBoxTreeItem<Object> temp = createCheckBoxCourse(newCourse);
                        checkBoxModule.getChildren().add(idx, temp);
                        break;
                    }
                    idx++;
                }
            }
        }

        addOnMouseClick(modulePane, coursePane, newCourse);
    }

    private void createCourseInPane(Course course){
        StackPane coursePane = createCoursePane(course);
        StackPane moduleStack = null;// = new StackPane();
        if(!allModules.contains(course.getModuleName())){
            moduleStack = createStackPane(course);
        }

        for (Pair<Course, StackPane> pair : stackpaneMapByModules.get(course.getModuleName())){
            if (pair.getKey().equals(course)){
                TilePane framePane = createFramePane(course, pair.getValue());  //framePane

                if(allModules.contains(course.getModuleName()) && moduleStack != null){
                    moduleStack.getChildren().add(framePane);
                    setTitleOfModule(moduleStack, course.getModuleName());
                }

                framePane.getChildren().add(coursePane);
                addOnMouseClick(framePane, coursePane, course);
            }
        }
//        TilePane framePane = createFramePane(course, coursePane);  //framePane
//
//        if(allModules.contains(course.getModuleName()) && moduleStack != null){
//            moduleStack.getChildren().add(framePane);
//            setTitleOfModule(moduleStack, course.getModuleName());
//        }
//
//        framePane.getChildren().add(coursePane);
//        addOnMouseClick(framePane, coursePane, course);

    }

    //Create pane for all courses with same module
    private StackPane createCoursePane(Course currentCourse) {

        StackPane coursePane = new StackPane();
        // RECTANGLE of course
        Rectangle rect = drawRectangle(currentCourse);
        //description on coursePane rectangle
        createDescription(coursePane, currentCourse, rect);
        //draw cross on coursePane
        drawCross(coursePane, currentCourse, rect);

        putIntoStackPaneMaps(currentCourse, coursePane);
        return coursePane;



  /*      if(allModules.contains(module)){
            StackPane coursePane = new StackPane();
            // RECTANGLE of course
            Rectangle rect = drawRectangle(currentCourse);
            //description on coursePane rectangle
            createDescription(coursePane, currentCourse, rect);
            //draw cross on coursePane
            drawCross(coursePane, currentCourse, rect);
            return coursePane;
        } else {
            StackPane coursePane = new StackPane();
            // RECTANGLE of course
            Rectangle rect = drawRectangle(currentCourse);
            //description on coursePane rectangle
            createDescription(coursePane, currentCourse, rect);
            //draw cross on coursePane
            drawCross(coursePane, currentCourse, rect);

//            modulePane.getChildren().add(coursePane);
            allModules.add(module);
//            addOnMouseClick(modulePane, coursePane, currentCourse);
//            tilePane.getChildren().add(moduleStack);


//            if(stackpaneMapByModules.containsKey(module)){
//                stackpaneMapByModules.get(module).add(new Pair<Course, StackPane>(currentCourse, coursePane));
//            } else {
//                List<Pair<Course, StackPane>> tmp = new ArrayList<>();
//                tmp.add(new Pair<Course, StackPane>(currentCourse, coursePane));
//                stackpaneMapByModules.putIfAbsent(module, tmp);
//            }
            return coursePane;
        }*/
    }

    private void putIntoStackPaneMaps(Course currentCourse, StackPane coursePane) {
        String module = currentCourse.getModuleName();
        String semester = Integer.toString(currentCourse.getSemester());
        if(stackpaneMapByModules.containsKey(module)){
            stackpaneMapByModules.get(module).add(new Pair<Course, StackPane>(currentCourse, coursePane));
        } else {
            List<Pair<Course, StackPane>> tmp = new ArrayList<>();
            tmp.add(new Pair<Course, StackPane>(currentCourse, coursePane));
            stackpaneMapByModules.putIfAbsent(module, tmp);
        }

        if(stackpaneMapBySemesters.containsKey(semester)){
            stackpaneMapBySemesters.get(semester).add(new Pair<Course, StackPane>(currentCourse, coursePane));
        } else {
            List<Pair<Course, StackPane>> tmp = new ArrayList<>();
            tmp.add(new Pair<Course, StackPane>(currentCourse, coursePane));
            stackpaneMapBySemesters.putIfAbsent(semester, tmp);
        }

    }

    private void createDescription(StackPane stackPane, Course course, Rectangle rect) {
        stackPane.setId("stackPane" + course.getModuleName() + course.getCourseName());
        Text text = new Text(course.getCourseName() + "\n" + course.getEcts() + " Ects");
        if(backgroundIsDark(course)){
            text.setFill(Color.WHITE);
        } else {
            text.setFill(Color.BLACK);
        }
        text.setTextAlignment(TextAlignment.CENTER);
        stackPane.getChildren().add(rect);
        stackPane.getChildren().add(text);
    }

    private Rectangle drawRectangle(Course course) {
        Rectangle rectangle = new Rectangle();
        rectangle.setHeight(70);
        rectangle.setWidth(70);
        rectangle.setFill(course.getColor());
        rectangle.setArcHeight(50);
        rectangle.setArcWidth(50);
        //set ID
        rectangle.setId("rectangle" + course.getModuleName() + course.getCourseName());

        return rectangle;
    }

    private void setTitleOfModule(StackPane stack, String moduleName) {
//        StackPane stack = new StackPane();

        Text text = new Text(moduleName);
        text.setOpacity(0.5);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(0.5);
        text.setFill(Color.WHITE);
        stack.setAlignment(Pos.TOP_CENTER);

//        stack.getChildren().addAll(moduleGroup, text);
        stack.getChildren().add(text);
    }

    private void drawCross(StackPane stackPane, Course currentCourse, Rectangle rectangle) {
        Bounds boundsInScene = rectangle.localToScene(rectangle.getBoundsInLocal());

        double minX = boundsInScene.getMinX();
        double minY = boundsInScene.getMinX();;
        double maxX = boundsInScene.getMaxX();
        double maxY = boundsInScene.getMaxX();;
        Line strikeUpDown = new Line();
        strikeUpDown.setStartX(minX);
        strikeUpDown.setStartY(minY);
        strikeUpDown.setEndX(maxX);
        strikeUpDown.setEndY(maxY);

        Line strikeDownUp = new Line();
        strikeDownUp.setStartX(minX);
        strikeDownUp.setStartY(maxY);
        strikeDownUp.setEndX(maxX);
        strikeDownUp.setEndY(minY);

        StackPane cross = new StackPane();
        cross.getChildren().addAll(strikeDownUp, strikeUpDown);
        cross.setOpacity(-1);

        //Set ID
        cross.setId("cross" + currentCourse.getModuleName().replaceAll("\\s","") + currentCourse.getCourseName().replaceAll("\\s",""));
        stackPane.getChildren().add(cross);
    }

    public void courseClicked(String moduleName, String courseName){
        String targetModule = moduleName;
        String targetCourse = courseName;
        TreeItem<Course> root = courseTreeView.getRoot();
//        searchCourse(root, targetModule, targetCourse);
        CheckBoxTreeItem<Course> course = searchCourse(root, targetModule, targetCourse);
        selectCourse(course);
    }

    private CheckBoxTreeItem<Course> searchCourse(TreeItem<Course> root, String targetModule, String targetCourse){
        for(TreeItem<Course> module : root.getChildren()) {
            for(TreeItem<Course> course : module.getChildren()){
                if(course.getValue().getModuleName().equals(targetModule) && course.getValue().getCourseName().equals(targetCourse)) {
                    CheckBoxTreeItem<Course> result = (CheckBoxTreeItem<Course>) course;
                    return result;
                }
            }
        }
        return null;
    }

    private void selectCourse(CheckBoxTreeItem<Course> course){
        course.setSelected(!course.isSelected());
    }

    private void tickOffCourse(CheckBoxTreeItem<Object> currentCourse){
        Course course = (Course) currentCourse.getValue();
        if(currentCourse.isSelected()){
            ectsEarned += course.getEcts();
            for (Pair<Course, StackPane> pair : stackpaneMapByModules.get(course.getModuleName())){
                if(course.equals(pair.getKey())){
                    int sz =  pair.getValue().getChildren().size();
                    pair.getValue().getChildren().get(sz-1).setOpacity(1);
                }
            }
//            tilePane.lookup("#cross" + course.getModuleName().replaceAll("\\s","") + course.getCourseName().replaceAll("\\s","")).setOpacity(1);
        } else {
            ectsEarned -= course.getEcts();

            for (Pair<Course, StackPane> pair : stackpaneMapByModules.get(course.getModuleName())){
                if(course.equals(pair.getKey())){
                    int sz =  pair.getValue().getChildren().size();
                    pair.getValue().getChildren().get(sz-1).setOpacity(-1);
                }
            }

//            tilePane.lookup("#cross" + course.getModuleName().replaceAll("\\s","") + course.getCourseName().replaceAll("\\s","")).setOpacity(-1);
        }
    }

    private void calculateEcts(TreeItem<Course> root){
        for(TreeItem<Course> child : root.getChildren()){
            if(child.getChildren().isEmpty()){
                CheckBoxTreeItem<Course> temp = (CheckBoxTreeItem<Course>) child;
                if(temp.isSelected()){
                    ectsEarned += temp.getValue().getEcts();
                }
            } else{
                calculateEcts(child);
            }
        }
    }

    private void addCourseInTree(Course course){
//        Course course = new Course("Psycho", Integer.toString(new Random().nextInt()), 3, Color.CRIMSON);


        if(CourseData.getInstance().getCoursesByModules().containsKey(course.getModuleName())){
            System.out.println("VORHER: " + CourseData.getInstance().getCourses());
            CourseData.getInstance().setCourse(course);
//            List<Course> courseList = CourseData.getInstance().getCourses().get(course.getModuleName());
//            courseList.add(course);
            System.out.println("NACHHER: " + CourseData.getInstance().getCourses());
        } else {
            CourseData.getInstance().setCourse(course);
//            List<Course> temp = new ArrayList<>();
//            temp.add(course);
//            CourseData.getInstance().getCourses().putIfAbsent(course.getModuleName(), temp);
            addModuleToCheckBoxTree(course.getModuleName());
        }
        addCourseToCheckBoxTree(course);
        //fill new TreeItem with module and course and then draw them
        TreeItem<Object> module = new TreeItem<>(course.getModuleName());
        TreeItem<Object> treeItemCourse = new TreeItem<>(course);
        module.getChildren().add(treeItemCourse);
//        for(Course newCourse : CourseData.getInstance().getCourses().get(course.getModuleName())){
//            TreeItem<Object> treeItemCourse = new TreeItem<>(newCourse);
//            module.getChildren().add(treeItemCourse);
//        }
//        drawCourse(course);
    }

    @FXML
    private void showAddCourseDialog(){
        openAddCourseDialog(null, null,null);
    }

    private void openAddCourseDialog(TilePane modulePane, StackPane coursePane, Course course){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/addCourseDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch(IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        AddCourseDialogController controller = fxmlLoader.getController();
        dialog.getDialogPane().lookupButton(ButtonType.OK)
                .disableProperty()
                .bind(controller.inputsFullBinding());

        if(course != null){
            controller.prefillCourse(course);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            Course newCourse = controller.processResults();
            if (course != null && newCourse.getModuleName().equals(course.getModuleName())){
                //edit course
//                //same module different course name
//                if (course.getSemester() == newCourse.getSemester()){
//                    course.setSemester(newCourse.getSemester());
//                } else{
                edit(modulePane, coursePane, course, newCourse);
//                }

            } else {

                if(course != null){
                    deleteCourse(modulePane, coursePane, course);
                }
                //if not editing
                //es fehlt noch ein else if wenn editing aber nicht derselbe moduleName

                if(CourseData.getInstance().courseInModule(newCourse)){
                    System.out.println("OOPS COURSE ALREADY IN MODULE");
                } else {
                    createCourseInPane(newCourse);
//                    StackPane newCoursePane = createCoursePane(newCourse);
//                    TilePane framePane = createFramePane(newCourse, newCoursePane);
//                    StackPane moduleStack = createStackPane(newCourse);
//                    moduleStack.getChildren().add(framePane);
//
//                    addOnMouseClick(framePane, coursePane, newCourse);
                    addCourseInTree(newCourse);
//                    framePane.getChildren().add(newCoursePane);
//                    tilePane.getChildren().add(moduleStack);
                }


            }
            System.out.println("OK pressed");

        } else {
            System.out.println("Cancel pressed");
        }
    }

    private TilePane createFramePane(Course course, StackPane coursePane){
        String module = course.getModuleName();
        if(allModules.contains(module)){
            for(Node moduleNode : tilePane.getChildren()){
                if(moduleNode.getId().equals("moduleStack" + module)){
                    StackPane moduleStackPane = (StackPane) moduleNode;
                    int sz =  moduleStackPane.getChildren().size();
                    System.out.println(moduleStackPane.getChildren());
                    TilePane modulePane = (TilePane) moduleStackPane.getChildren().get(0);
//                    if(stackpaneMapByModules.containsKey(module)){
//                        stackpaneMapByModules.get(module).add(new Pair<Course, StackPane>(course, coursePane));
//                    } else {
//                        List<Pair<Course, StackPane>> tmp = new ArrayList<>();
//                        tmp.add(new Pair<Course, StackPane>(course, coursePane));
//                        stackpaneMapByModules.putIfAbsent(module, tmp);
//                    }
                    return modulePane;
                }
            }
        } else {
            TilePane modulePane = new TilePane();
            modulePane.setId("modulePane" + module.replaceAll("\\s",""));
            modulePane.setHgap(4);
            modulePane.setVgap(4);
            modulePane.setPrefColumns(2);
            modulePane.setAlignment(Pos.CENTER);
            allModules.add(module);

//            if(stackpaneMapByModules.containsKey(module)){
////                        List<StackPane> tmp = blubb.get(module);
////                        tmp.add(coursePane);
//                stackpaneMapByModules.get(module).add(new Pair<Course, StackPane>(course, coursePane));
//            } else {
//                List<Pair<Course, StackPane>> tmp = new ArrayList<>();
//                tmp.add(new Pair<Course, StackPane>(course, coursePane));
//                stackpaneMapByModules.putIfAbsent(module, tmp);
//            }
            return modulePane;
        }
        return null;
    }

    private StackPane createStackPane(Course course){
        String module = course.getModuleName();
        StackPane moduleStack = new StackPane();
//        setTitleOfModule(moduleStack, module);
        moduleStack.setId("moduleStack" + module);
        tilePane.getChildren().add(moduleStack);
        return moduleStack;
    }

    @FXML
    private void showAddModuleDialog(){
        List<Course> newModule = openAddModuleDialog();
        System.out.println(newModule);
        for(Course newCourse : newModule){
            if(CourseData.getInstance().courseInModule(newCourse)){
                System.out.println("OOPS COURSE ALREADY IN MODULE");
            } else {
                createCourseInPane(newCourse);
//                createCoursePane(newCourse);
                addCourseInTree(newCourse);
            }
        }
        System.out.println(allModules);
    }

    @FXML
    private void addCurriculumRechner(){
//        allModules.clear();
//        tilePane.getChildren().clear();
//        courseTreeView.getRoot().getChildren().clear();
//        CourseData.getInstance().getCourses().clear();

        List<Course> newModule = openAddModuleDialog();
        if (!newModule.isEmpty()){
            System.out.println(newModule);
            allModules.clear();
            tilePane.getChildren().clear();
            courseTreeView.getRoot().getChildren().clear();
            CourseData.getInstance().getCourses().clear();

            for (Course course : newModule){
                CourseData.getInstance().setCourse(course);
            }
//            CourseData.getInstance().setCourses(newModule);
            loadCoursesIntoPane();

        } else{
            System.out.println("List is empty");
        }

    }


    private List<Course> openAddModuleDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/addModuleDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch(IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
//            return;
        }

        List<Course> newCourses = new ArrayList<>();

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        AddModuleDialogController controller = fxmlLoader.getController();
        dialog.getDialogPane().lookupButton(ButtonType.OK)
                .disableProperty()
                .bind(controller.inputsFullBinding());

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            ectsTotal.setText("/" + Integer.toString(controller.getEctsTotal()));
            newCourses = controller.processResults();
            System.out.println("OK pressed");

        } else {
            System.out.println("Cancel pressed");
        }
        return newCourses;
    }

    @FXML
    private void openFile() throws IOException {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(new Stage());
        CourseData cd = CourseData.getInstance();
        if(cd.getCourses() != null){
            cd.getCourses().clear();
            allModules.clear();
            tilePane.getChildren().clear();
            courseTreeView.getRoot().getChildren().clear();
        }

        CourseData.getInstance().loadCourses(file.toString());
        loadCoursesIntoPane();
    }

    @FXML
    private void saveFile(){
        FileChooser fileChooser = new FileChooser();

        //Set extension for excel files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(mainBorderPane.getScene().getWindow());

        if(file != null){
            CourseData.getInstance().writeToFile(file);
        }
    }

    private boolean backgroundIsDark(Course course){
        Color color = course.getColor();
        double L = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
//        return (color.getRed()*0.299 + color.getGreen()*0.587 + color.getBlue()*0.114) < 0.186;
        return L < 0.4;
    }

    @FXML
    private void changeView(){
        CourseData.getInstance().changeView();
        System.out.println("!!!!!!!!! Changed view !!!!!!!!!!!");
//        System.out.println(CourseData.getInstance().getCourses());
        buildView();
    }


    private void buildView(){
        boolean isModuleView = CourseData.getInstance().getModuleView();

    }
}

