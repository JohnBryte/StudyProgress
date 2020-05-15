package com.breit.curriculumrechner.datamodel;

import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class CourseData {
    private static CourseData instance = new CourseData();
    private Map<String, List<Course>> linkedMapModules = new LinkedHashMap<>();
    private Map<String, List<Course>> linkedMapSemester = new LinkedHashMap<>();
    private Map<String, List<Course>> coursesByModules = FXCollections.observableMap(linkedMapModules);
    private Map<String, List<Course>> coursesBySemester = FXCollections.observableMap(linkedMapSemester);
    private boolean moduleView = true;
//    private static String filename = "MyCourses.txt";

    public static CourseData getInstance(){
        return instance;
    }

    private CourseData() {}

    public void loadCourses(String filename) throws IOException {
//        courses = FXCollections.observableHashMap();
        try {
            File file = new File(filename);   //creating a new file instance
            FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file
            //creating Workbook instance that refers to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object

            Iterator<Row> itr = sheet.iterator();    //iterating over excel file
            while (itr.hasNext()) {
                Row row = itr.next();
                //skip first row
                if(row.getRowNum() == 0){
                    continue;
                }
                String moduleName = "";
                String courseName = "";
                int ects = 0;
                int semester = 0;
                Color color = Color.WHITE;
                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch(cell.getSheet().getRow(0).getCell(cell.getColumnIndex()).toString()){
                        case "Module":
                            moduleName = cell.getStringCellValue().trim();
                            break;
                        case "Course":
                            courseName = cell.getStringCellValue().trim();
                            break;
                        case "ECTS":
                            ects = (int) cell.getNumericCellValue();
                            break;
                        case "Semester":
                            semester = (int) cell.getNumericCellValue();
                            break;
                        case "Color":
                            color = Color.web(cell.getStringCellValue().trim());
                            break;
                    }
                }
                Course course = new Course(moduleName, courseName, ects, semester, color);
                System.out.println(course);
                //fill by modules
                if(coursesByModules.containsKey(course.getModuleName())){
                    List<Course> temp = coursesByModules.get(moduleName);
                    temp.add(course);
                    coursesByModules.put(moduleName, temp);
                } else {
                    List<Course> temp = new ArrayList<>();
                    temp.add(course);
                    coursesByModules.putIfAbsent(moduleName, temp);
                }

                //fill by semester
                if(coursesBySemester.containsKey(course.getSemester())){
                    List<Course> temp = coursesBySemester.get(Integer.toString(semester));
                    temp.add(course);
                    coursesBySemester.put(Integer.toString(semester), temp);
                } else {
                    List<Course> temp = new ArrayList<>();
                    temp.add(course);
                    coursesBySemester.putIfAbsent(Integer.toString(semester), temp);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println(this.getCoursesByModules());
        System.out.println(this.getCoursesBySemester());
    }

    public Map<String, List<Course>> getCoursesByModules(){
        return coursesByModules;
    }
    public Map<String, List<Course>> getCoursesBySemester(){
        return coursesBySemester;
    }

    public Map<String, List<Course>> getCourses() {
        if (moduleView){
            return coursesByModules;
        } else {
            return coursesBySemester;
        }
    }

    public boolean courseInModule(Course course){
        if(coursesByModules.containsKey(course.getModuleName())){
            for (Course c : coursesByModules.get(course.getModuleName())){
                if (c.getCourseName().equals(course.getCourseName())){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean courseInSemester(Course course){
        if(coursesBySemester.containsKey(course.getSemester())){
            for (Course c : coursesBySemester.get(course.getSemester())){
                if (c.getCourseName().equals(course.getCourseName())){
                    return true;
                }
            }
        }
        return false;
    }

    public void setCourse(Course course){
        setCourse(course, -1, -1);
    }

    public void setCourse(Course course, int moduleIdx, int semesterIdx){

        //module view
        String moduleName = course.getModuleName();
        if(coursesByModules.containsKey(moduleName)){
            if (moduleIdx == -1){
                coursesByModules.get(moduleName).add(course);
            } else {
                coursesByModules.get(moduleName).add(moduleIdx, course);
            }

        } else {
            List<Course> temp = new ArrayList<>();
            temp.add(course);
            coursesByModules.putIfAbsent(moduleName, temp);
        }
//        coursesByModules.putIfAbsent(course.getModuleName());
        System.out.println(coursesByModules);

        //semester view
        String semester = course.getSemester();
        if(coursesBySemester.containsKey(semester)){
            if (semesterIdx == -1){
                coursesBySemester.get(semester).add(course);
            } else {
                coursesBySemester.get(semester).add(semesterIdx, course);
            }

        } else {
            List<Course> temp = new ArrayList<>();
            temp.add(course);
            coursesBySemester.putIfAbsent(semester, temp);
        }
    }

    public boolean getModuleView(){
        return moduleView;
    }

    public void changeView(){
        if (moduleView){
            moduleView = false;
        } else {
            moduleView = true;
        }
    }

    public void writeToFile(File file){
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        int rowCount = 0;

        Row titles = sheet.createRow(rowCount);
        writeTitles(titles);

        for(String moduleName : coursesByModules.keySet()){
            for(Course course : coursesByModules.get(moduleName)){
                rowCount++;
                Row row = sheet.createRow(rowCount);
                writeCourse(course, row);

            }
        }

        try(FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())){
            wb.write(fos);
        } catch (IOException e) {
            System.out.println("Someting went wrong SAVING");
        }
    }

    private void writeCourse(Course course, Row row){
        Cell cell = row.createCell(0);
        cell.setCellValue(course.getModuleName());

        cell = row.createCell(1);
        cell.setCellValue(course.getCourseName());

        cell = row.createCell(2);
        cell.setCellValue(course.getEcts());

        cell = row.createCell(3);
        cell.setCellValue(course.getSemester());

        cell = row.createCell(4);
        cell.setCellValue(course.getColor().toString());
    }

    private void writeTitles(Row row){
        Cell cell = row.createCell(0);
        cell.setCellValue("Modules");

        cell = row.createCell(1);
        cell.setCellValue("Course");

        cell = row.createCell(2);
        cell.setCellValue("ECTS");

        cell = row.createCell(3);
        cell.setCellValue("Semester");

        cell = row.createCell(4);
        cell.setCellValue("Color");
    }


}
