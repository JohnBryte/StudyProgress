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
    @FXML
    private CheckBox changeView;

    private boolean moduleView = CourseData.getInstance().getModuleView();
    private final ContextMenu contextMenu = new ContextMenu();
    private Map<String, List<Pair<Course, StackPane>>> stackpaneMapByModules = new LinkedHashMap<>();
    private Map<String, List<Pair<Course, StackPane>>> stackpaneMapBySemesters = new LinkedHashMap<>();


    public void initialize(){
        ectsTotal.setText("/ 180");
        setChangeViewText();

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

    private void setChangeViewText() {
        if (moduleView){
            changeView.setText("Change to semester view");
        } else {
            changeView.setText("Change to module view");
        }
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
        courseModule.setExpanded(true);
        courseTreeView.getRoot().getChildren().add(courseModule);
    }

    private CheckBoxTreeItem<Object> createCheckBoxCourse(Course course){
        CheckBoxTreeItem<Object> currentCourse = new CheckBoxTreeItem<>(course);
        currentCourse.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new EventHandler<CheckBoxTreeItem.TreeModificationEvent<Object>>() {
            @Override
            public void handle(CheckBoxTreeItem.TreeModificationEvent<Object> objectTreeModificationEvent) {
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

    private void deleteCourse(TilePane framePane, StackPane coursePane, Course course){
        int idx = CourseData.getInstance().getCoursesByModules().get(course.getModuleName()).indexOf(course);
//        int i = CourseData.getInstance().getCourses().get(course.getOrdering(moduleView)).indexOf(course);
        deleteCourseFromPane(framePane, coursePane, course);
        deleteCourseFromCheckBoxTree(course, idx);

        if(moduleIsEmpty(framePane)){
            deleteModuleFromPane(framePane, course);
        }
//        if(semesterIsEmpty())
    }

    private boolean moduleIsEmpty(TilePane modulePane){
        return modulePane.getChildren().size() == 0;
    }

    private void deleteCourseFromPane(TilePane modulePane, StackPane coursePane, Course course){
        stackpaneMapByModules.get(course.getModuleName()).remove(new Pair<Course, StackPane>(course,coursePane));
        CourseData.getInstance().getCoursesByModules().get(course.getModuleName()).remove(course);

        stackpaneMapBySemesters.get(course.getSemester()).remove(new Pair<Course, StackPane>(course,coursePane));
        int idx = CourseData.getInstance().getCoursesBySemester().get(course.getSemester()).indexOf(course);
        CourseData.getInstance().getCoursesBySemester().get(course.getSemester()).remove(course); //früher remove(idx)

        coursePane.getChildren().clear();
        modulePane.getChildren().remove(modulePane.getChildren().indexOf(coursePane));
    }

    private void changeCourseNameInTree(Course oldCourse, Course newCourse){
        for (Object o : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem module = (CheckBoxTreeItem) o;
            if (module.getValue().equals(oldCourse.getModuleName())){
                for (Object temp : module.getChildren()){
                    CheckBoxTreeItem course = (CheckBoxTreeItem) temp;
                    if (course.getValue().equals(oldCourse)){
                        course.setValue(newCourse);
                        break;
                    }
                }
                break;
            }
        }
    }

    private int moduleInTree(String moduleName){
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
        if (found){
            return moduleIdx;
        } else {
            return -1;
        }
    }

    private void deleteCourseFromCheckBoxTree(Course course, int i){
        String moduleName = course.getModuleName();
//        int moduleIdx = 0;
//        boolean found = false;
//        for(Object o : courseTreeView.getRoot().getChildren()){
//            CheckBoxTreeItem module = (CheckBoxTreeItem) o;
//            if(module.getValue().equals(moduleName)){
//                found = true;
//                break;
//            }
//            moduleIdx++;
//        }

        int moduleIdx = moduleInTree(moduleName);
        boolean found = true ? moduleIdx != -1 : false;
        if(found){
            CheckBoxTreeItem moduleInTree = (CheckBoxTreeItem) courseTreeView.getRoot().getChildren().get(moduleIdx);
            moduleInTree.getChildren().remove(i);
            if (moduleInTree.getChildren().isEmpty()){
                courseTreeView.getRoot().getChildren().remove(moduleIdx);
                CourseData.getInstance().getCoursesByModules().remove(course.getModuleName());
            }
        }
    }

    private void deleteModuleFromPane(TilePane framePane, Course course){
//        if (moduleView){
//            String moduleName = course.getModuleName();
//            Pane moduleStack = (Pane) framePane.getParent().getParent();
//            moduleStack.getChildren().removeIf(o -> o.getId().equals("orderingStack" + moduleName));
//            allModules.remove(moduleName);
//            CourseData.getInstance().getCoursesByModules().remove(moduleName);
//        } else{
//            String semester = course.getSemester();
//            Pane semesterStack = (Pane) framePane.getParent().getParent();
//            semesterStack.getChildren().removeIf(o -> o.getId().equals("orderingStack" + semester));
//            allModules.remove(semester);
//            CourseData.getInstance().getCoursesBySemester().remove(semester);
//        }

        String orderingName = course.getOrdering(moduleView);
        Pane orderingStack = (Pane) framePane.getParent().getParent();
        orderingStack.getChildren().removeIf(o -> o.getId().equals("orderingStack" + orderingName));
        allModules.remove(orderingName);
        System.out.println("FOO: " + CourseData.getInstance().getCoursesByModules());
        CourseData.getInstance().getCourses().remove(orderingName);
        System.out.println("BAR: " + CourseData.getInstance().getCoursesByModules());
//        if (moduleView){
//            CourseData.getInstance().getCoursesBySemester().get(course.getSemester()).remove(course);
//        } else {
//            System.out.println("FOO: " + CourseData.getInstance().getCoursesByModules());
//            CourseData.getInstance().getCoursesByModules().get(course.getModuleName()).remove(course);
//            System.out.println("BAR: " + CourseData.getInstance().getCoursesByModules());
//        }

        for(Object o : courseTreeView.getRoot().getChildren()){
            CheckBoxTreeItem bla = (CheckBoxTreeItem) o;
            if(bla.getChildren().size() == 0){
                courseTreeView.getRoot().getChildren().remove(bla);
                break;
            }
        }
    }

    private void edit(TilePane modulePane, StackPane coursePane, Course oldCourse, Course newCourse){
//        int i = CourseData.getInstance().getCourses().get(oldCourse.getOrdering(moduleView)).indexOf(oldCourse);
/*        if (moduleView){
            CourseData.getInstance().getCoursesByModules().get(oldCourse.getModuleName()).remove(oldCourse);
            CourseData.getInstance().getCoursesByModules().get(newCourse.getModuleName()).add(i, newCourse);

            int moduleIdx = stackpaneMapByModules.get(oldCourse.getModuleName()).indexOf(new Pair<Course, StackPane>(oldCourse, coursePane));
            System.out.println("IDX: " + stackpaneMapByModules.get(oldCourse.getModuleName()).indexOf(new Pair<Course, StackPane>(oldCourse, coursePane)));
            System.out.println(stackpaneMapByModules.get(oldCourse.getModuleName()));
            stackpaneMapByModules.get(oldCourse.getModuleName()).remove(new Pair<Course, StackPane>(oldCourse, coursePane));
            createCourseInPane(newCourse);
//            stackpaneMapByModules.get(newCourse.getModuleName()).add(moduleIdx, new Pair<Course, StackPane>(newCourse, newCoursePane));

        } else {
            CourseData.getInstance().getCoursesBySemester().get(Integer.toString(newCourse.getSemester())).remove(oldCourse);
            CourseData.getInstance().getCoursesBySemester().get(Integer.toString(newCourse.getSemester())).add(i, newCourse);

            int semesterIdx = stackpaneMapBySemesters.get(Integer.toString(oldCourse.getSemester())).indexOf(new Pair<Course, StackPane>(oldCourse, coursePane));
            stackpaneMapBySemesters.get(Integer.toString(oldCourse.getSemester())).remove(new Pair<Course, StackPane>(oldCourse, coursePane));
            StackPane newCoursePane = createCoursePane(newCourse);
            stackpaneMapBySemesters.get(Integer.toString(newCourse.getSemester())).add(semesterIdx, new Pair<Course, StackPane>(newCourse, newCoursePane));
        }*/

        System.out.println(CourseData.getInstance().getCourses().get(newCourse.getOrdering(moduleView)));
//        CourseData.getInstance().getCourses().get(newCourse.getOrdering(moduleView)).indexOf(new Pair<Course, StackPane>(oldCourse, coursePane));

//        int i = CourseData.getInstance().getCourses().get(oldCourse.getOrdering(moduleView)).indexOf(oldCourse);
//        CourseData.getInstance().getCourses().get(oldCourse.getOrdering(moduleView)).remove(oldCourse);
//        CourseData.getInstance().getCourses().get(newCourse.getOrdering(moduleView)).add(i, newCourse);

        System.out.println("VORHER MOD: " + CourseData.getInstance().getCoursesByModules());
        System.out.println("VORHER SEM: " + CourseData.getInstance().getCoursesBySemester());
        int i = CourseData.getInstance().getCoursesByModules().get(oldCourse.getModuleName()).indexOf(oldCourse);
        int j = CourseData.getInstance().getCoursesBySemester().get(oldCourse.getSemester()).indexOf(oldCourse);
        CourseData.getInstance().getCoursesByModules().get(oldCourse.getModuleName()).remove(oldCourse);
        CourseData.getInstance().getCoursesBySemester().get(oldCourse.getSemester()).remove(oldCourse);
        CourseData.getInstance().setCourse(newCourse, i, j);
        System.out.println("Nachher MOD: " + CourseData.getInstance().getCoursesByModules());
        System.out.println("Nachher SEM: " + CourseData.getInstance().getCoursesBySemester());
//        CourseData.getInstance().getCoursesBySemester().get(oldCourse.getSemester()).remove(oldCourse);
        System.out.println(CourseData.getInstance().getCoursesBySemester().get(newCourse.getSemester()));
/*        if (CourseData.getInstance().getCoursesBySemester().get(newCourse.getSemester()) == null){
            List<Course> temp = new ArrayList<>();
            temp.add(newCourse);
            CourseData.getInstance().getCoursesBySemester().putIfAbsent(newCourse.getSemester(), temp);
        } else {
            CourseData.getInstance().getCoursesBySemester().get(newCourse.getSemester()).add(newCourse);
        }*/




        int semesterIdx = stackpaneMapBySemesters.get(oldCourse.getSemester()).indexOf(new Pair<Course, StackPane>(oldCourse, coursePane));
        System.out.println();
        System.out.println("KURS: " + oldCourse.getModuleName() + " "+ oldCourse.getCourseName());
        System.out.println("IDX: " + semesterIdx);
        System.out.println("Semester: " + stackpaneMapBySemesters);
        stackpaneMapBySemesters.get(oldCourse.getSemester()).remove(new Pair<Course, StackPane>(oldCourse, coursePane));
        System.out.println("Semester After: " + stackpaneMapBySemesters);

        int moduleIdx = stackpaneMapByModules.get(oldCourse.getModuleName()).indexOf(new Pair<Course, StackPane>(oldCourse, coursePane));
        System.out.println("IDX: " + moduleIdx);
        System.out.println("Modules: " + stackpaneMapByModules);
        stackpaneMapByModules.get(oldCourse.getModuleName()).remove(new Pair<Course, StackPane>(oldCourse, coursePane));
        System.out.println("Modules After: " + stackpaneMapByModules);

        coursePane.getChildren().clear();
        Rectangle rect = drawRectangle(newCourse);
        createDescription(coursePane, newCourse, rect);
        drawCross(coursePane, newCourse, rect);

        putIntoStackPaneMaps(newCourse, coursePane, semesterIdx, moduleIdx);

        //TREE
        if (!oldCourse.getModuleName().equals(newCourse.getModuleName())){
            System.out.println("Editing Modul in Tree");
            deleteCourseFromCheckBoxTree(oldCourse, i);
            System.out.println(courseTreeView.getRoot().getChildren());
            int temp = moduleInTree(newCourse.getModuleName());
            boolean found = true ? temp != -1 : false;
            if (!found){
                System.out.println("EXISTIERT NICHT IM TREE");
                addModuleToCheckBoxTree(newCourse.getModuleName());
            }
            addCourseToCheckBoxTree(newCourse);
        } else if (oldCourse.getModuleName().equals(newCourse.getModuleName()) && !oldCourse.getCourseName().equals(newCourse.getCourseName())){
            System.out.println("Editing Kursname in Tree. Kein neues Modul im Tree");

            changeCourseNameInTree(oldCourse, newCourse);

//            deleteCourseFromCheckBoxTree(oldCourse, i);
//            int temp = moduleInTree(newCourse.getModuleName());
//            boolean found = true ? temp != -1 : false;
//            if (!found){
//                System.out.println("EXISTIERT NICHT IM TREE");
//                addModuleToCheckBoxTree(newCourse.getModuleName());
//            }
//            addCourseToCheckBoxTree(newCourse);
        } else {
            System.out.println("DO NOTHING");
        }

/*        String module = newCourse.getModuleName();
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
        }*/

        addOnMouseClick(modulePane, coursePane, newCourse);
        System.out.println();
        System.out.println("Semester: " + CourseData.getInstance().getCoursesBySemester());
        System.out.println("Modules: " + CourseData.getInstance().getCoursesByModules());
    }

    private void createCourseInPane(Course course){
        StackPane coursePane = createCoursePane(course);
        StackPane orderingStack = null;// = new StackPane();
        if(!allModules.contains(course.getOrdering(moduleView))){
            orderingStack = createStackPane(course);
        }


        Map<String, List<Pair<Course, StackPane>>> stackPanes;
        if (moduleView){
            stackPanes = stackpaneMapByModules;
        } else {
            stackPanes = stackpaneMapBySemesters;
        }

        for (Pair<Course, StackPane> pair : stackPanes.get(course.getOrdering(moduleView))) {
            if (pair.getKey().equals(course)) {
                TilePane framePane = createFramePane(course);  //framePane

                if (allModules.contains(course.getOrdering(moduleView)) && orderingStack != null) {
                    System.out.println("ORDERING STACK: " + allModules);
                    orderingStack.getChildren().add(framePane);
                    setTitleOfModule(orderingStack, course.getOrdering(moduleView));
/*                        if (course.getSemester().equals(allModules.toArray()[allModules.size()-1])){
                            orderingStack.getChildren().add(framePane);
                            setTitleOfModule(orderingStack, course.getOrdering(moduleView));
                        }else {
                            for (int i = 0; i < allModules.size(); i++){
                                if (course.getSemester().equals(allModules.toArray()[i])){
                                    System.out.println(orderingStack.getChildren());
                                    orderingStack.getChildren().add(framePane);
                                    setTitleOfModule(orderingStack, course.getOrdering(moduleView));
                                }
                            }
                        }*/
                }
                System.out.println("FRAMEPANE: " + framePane.getChildren());
                framePane.getChildren().add(pair.getValue());
                addOnMouseClick(framePane, coursePane, course);
            }
        }

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
    }

    private void putIntoStackPaneMaps(Course currentCourse, StackPane coursePane){
        putIntoStackPaneMaps(currentCourse, coursePane, -1, -1);
    }
    private void putIntoStackPaneMaps(Course currentCourse, StackPane coursePane, int semesterIdx, int moduleIdx) {
        String module = currentCourse.getModuleName();
        String semester = currentCourse.getSemester();
        if(stackpaneMapByModules.containsKey(module)){
            if(moduleIdx == -1){
                stackpaneMapByModules.get(module).add(new Pair<Course, StackPane>(currentCourse, coursePane));
            } else {
                stackpaneMapByModules.get(module).add(moduleIdx, new Pair<Course, StackPane>(currentCourse, coursePane));
            }

        } else {
            List<Pair<Course, StackPane>> tmp = new ArrayList<>();
            tmp.add(new Pair<Course, StackPane>(currentCourse, coursePane));
            stackpaneMapByModules.putIfAbsent(module, tmp);
        }

        if(stackpaneMapBySemesters.containsKey(semester)){
            if (semesterIdx == -1) {
                stackpaneMapBySemesters.get(semester).add(new Pair<Course, StackPane>(currentCourse, coursePane));
            } else {
                stackpaneMapBySemesters.get(semester).add(semesterIdx, new Pair<Course, StackPane>(currentCourse, coursePane));
            }

        } else {
            List<Pair<Course, StackPane>> tmp = new ArrayList<>();
            tmp.add(new Pair<Course, StackPane>(currentCourse, coursePane));
            stackpaneMapBySemesters.putIfAbsent(semester, tmp);
        }

    }

    private void createDescription(StackPane stackPane, Course course, Rectangle rect) {
        stackPane.setId("coursePane" + course.getModuleName() + course.getCourseName());
        Text text = new Text(course.getCourseName() + "\n" + course.getEcts() + " Ects");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(0.4);
        text.setFill(Color.WHITE);
//        if(backgroundIsDark(course)){
//            text.setFill(Color.WHITE);
//        } else {
//            text.setFill(Color.BLACK);
//        }
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

    private void setTitleOfModule(StackPane stack, String ordering) {
//        StackPane stack = new StackPane();

        Text text = new Text(ordering);
        if (!moduleView){
            text.setText(text.getText() + " Semester");
        }
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
        CheckBoxTreeItem<Course> course = searchCourseInTree(root, targetModule, targetCourse);
        selectCourse(course);
    }

    private CheckBoxTreeItem<Course> searchCourseInTree(TreeItem<Course> root, String targetModule, String targetCourse){
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

        Map<String, List<Pair<Course, StackPane>>> stackPanes;
        if (moduleView){
            stackPanes = stackpaneMapByModules;
        } else {
            stackPanes = stackpaneMapBySemesters;
        }
        if(currentCourse.isSelected()){
            ectsEarned += course.getEcts();
            for (Pair<Course, StackPane> pair : stackPanes.get(course.getOrdering(moduleView))){
                if(course.equals(pair.getKey())){
                    int sz =  pair.getValue().getChildren().size();
                    pair.getValue().getChildren().get(sz-1).setOpacity(1);
                }
            }
//            tilePane.lookup("#cross" + course.getModuleName().replaceAll("\\s","") + course.getCourseName().replaceAll("\\s","")).setOpacity(1);
        } else {
            ectsEarned -= course.getEcts();

            for (Pair<Course, StackPane> pair : stackPanes.get(course.getOrdering(moduleView))){
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
        boolean editing = true ? course != null : false;
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

        if(editing){
            controller.prefillCourse(course);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            Course newCourse = controller.processResults();
            if (editing && (newCourse.getOrdering(moduleView).equals(course.getOrdering(moduleView)))){
                //newCourse.getOrdering(moduleView).equals(course.getOrdering(moduleView))
                //edit course
                //same module different course name
                System.out.println("REAL EDITING");
                edit(modulePane, coursePane, course, newCourse);
            } else {
                if(editing){
                    System.out.println("DELETE COURSE");
                    deleteCourse(modulePane, coursePane, course);
                }
                //if not editing
                //es fehlt noch ein else if wenn editing aber nicht derselbe moduleName
                if(CourseData.getInstance().courseInModule(newCourse)){
                    System.out.println("OOPS COURSE ALREADY IN MODULE");
                } else {
                    createCourseInPane(newCourse);
                    addCourseInTree(newCourse);
                }
            }
            System.out.println("OK pressed");

        } else {
            System.out.println("Cancel pressed");
        }
    }

    private TilePane createFramePane(Course course){
        String courseOrdering = course.getOrdering(moduleView);
        if(allModules.contains(courseOrdering)){
            for(Node moduleNode : tilePane.getChildren()){
                if(moduleNode.getId().equals("orderingStack" + courseOrdering)){
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
            modulePane.setId("modulePane" + courseOrdering.replaceAll("\\s",""));
            modulePane.setHgap(4);
            modulePane.setVgap(4);
            if (moduleView){
                modulePane.setPrefColumns(2);
            } else {
                modulePane.setPrefColumns(6);
            }
            modulePane.setAlignment(Pos.CENTER);
            allModules.add(courseOrdering);
            return modulePane;
        }
        return null;
    }

    private StackPane createStackPane(Course course){
        String orderingBy = course.getOrdering(moduleView);
        StackPane orderingStack = new StackPane();
//        setTitleOfModule(moduleStack, module);
        orderingStack.setId("orderingStack" + orderingBy);
        if (moduleView){
            tilePane.getChildren().add(orderingStack);
        } else {
            if (tilePane.getChildren().isEmpty()){
                tilePane.getChildren().add(orderingStack);
            } else {
                int semester = Integer.parseInt(course.getSemester());
                boolean inserted = false;
                for (int i = 0; i < tilePane.getChildren().size(); i++){
                    int currentSemester = Integer.parseInt(tilePane.getChildren().get(i).getId().replace("orderingStack", ""));
                    if (semester < currentSemester){
                        tilePane.getChildren().add(i, orderingStack);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted){
                    tilePane.getChildren().add(orderingStack);
                }
            }
        }
        /*if (course.getSemester().equals(allModules.toArray()[allModules.size()-1])){
            orderingStack.getChildren().add(framePane);
            setTitleOfModule(orderingStack, course.getOrdering(moduleView));
        }else {
            for (int i = 0; i < allModules.size(); i++){
                if (course.getSemester().equals(allModules.toArray()[i])){
                    orderingStack.getChildren().add(framePane);
                    setTitleOfModule(orderingStack, course.getOrdering(moduleView));
                }
            }
        }*/
        return orderingStack;
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
            cd.getCoursesBySemester().clear();
            cd.getCoursesByModules().clear();
            allModules.clear();
            tilePane.getChildren().clear();
            stackpaneMapBySemesters.clear();
            stackpaneMapByModules.clear();
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
        moduleView = !moduleView;
        setChangeViewText();
        System.out.println("!!!!!!!!! Changed view !!!!!!!!!!!");
//        System.out.println(CourseData.getInstance().getCourses());
        System.out.println("ByModules: " + CourseData.getInstance().getCoursesByModules());
        System.out.println("BySemester: " + CourseData.getInstance().getCoursesBySemester());
        System.out.println();
        buildView();
    }


    private void buildView(){
        boolean isModuleView = CourseData.getInstance().getModuleView();
        tilePane.getChildren().clear();
        allModules.clear();
//        stackpaneMapBySemesters.clear();
//        stackpaneMapByModules.clear();


        Map<String, List<Pair<Course, StackPane>>> temp = new HashMap();
        if (moduleView){
            temp = stackpaneMapByModules;
        } else {
            temp = stackpaneMapBySemesters;
        }
        for (String ordering : temp.keySet()){
            for (Pair<Course, StackPane> p : temp.get(ordering)){

                Course course = p.getKey();
                StackPane coursePane = p.getValue();

                StackPane moduleStack = null;// = new StackPane();
                if(!allModules.contains(course.getOrdering(moduleView))){
                    moduleStack = createStackPane(course);
                }

                TilePane framePane = createFramePane(course);  //framePane

                if (allModules.contains(course.getOrdering(moduleView)) && moduleStack != null) {
                    moduleStack.getChildren().add(framePane);
                    setTitleOfModule(moduleStack, course.getOrdering(moduleView));
                }

                framePane.getChildren().add(coursePane);
                addOnMouseClick(framePane, coursePane, course);
            }
        }
    }
}

