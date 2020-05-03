package com.breit.curriculumrechner.datamodel;

import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CourseData {
    private static CourseData instance = new CourseData();
    private Map<String, List<Course>> courses = FXCollections.observableHashMap();
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
                        case "Color":
                            color = Color.web(cell.getStringCellValue().trim());
                            break;
                    }
                }
                Course course = new Course(moduleName, courseName, ects, color);

                if(courses.containsKey(course.getModuleName())){
                    List<Course> temp = courses.get(moduleName);
                    temp.add(course);
                    courses.put(moduleName, temp);
                } else {
                    List<Course> temp = new ArrayList<>();
                    temp.add(course);
                    courses.putIfAbsent(moduleName, temp);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<Course>> getCourses() { return courses; }

    public boolean courseInModule(Course course){
        for (Course c : courses.get(course.getModuleName())){
            if (c.getCourseName().equals(course.getCourseName())){
                return true;
            }
        }
        return false;
    }

    public void setCourses(List<Course> list){
        String moduleName = list.get(0).getModuleName();
        courses.putIfAbsent(moduleName, list);
        System.out.println(courses);
    }

    public void writeToFile(File file){
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        int rowCount = 0;

        Row titles = sheet.createRow(rowCount);
        writeTitles(titles);

        for(String moduleName : courses.keySet()){
            for(Course course : courses.get(moduleName)){
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
        cell.setCellValue("Color");
    }


}
