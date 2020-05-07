package com.breit.curriculumrechner;

import javafx.beans.property.*;

public class Row {
    private final StringProperty name = new SimpleStringProperty() ;
    public StringProperty nameProperty() {
        return name ;
    }
    public final String getName() {
        return nameProperty().get();
    }
    public final void setName(String name) {
        nameProperty().set(name);
    }

    public final StringProperty ects = new SimpleStringProperty();
    public StringProperty ectsProperty() {
        return ects ;
    }
    public final String getEcts() {
        return ectsProperty().get();
    }
    public final void setEcts(String ects) {
        ectsProperty().set(ects);
    }

    public final StringProperty semester = new SimpleStringProperty();
    public StringProperty semesterProperty() {
        return semester ;
    }
    public final String getSemester() {
        return semesterProperty().get();
    }
    public final void setSemester(String semester) {
        semesterProperty().set(semester);
    }

    public final BooleanProperty allFieldsFilled = new SimpleBooleanProperty();
    public final BooleanProperty allFieldsFilledProperty(){ return allFieldsFilled; }
    public final boolean getAllFieldsFilled() {
        return allFieldsFilledProperty().get();
    }

    public final IntegerProperty lastElement = new SimpleIntegerProperty();
    public final IntegerProperty lastElementProperty() { return lastElement; }
    public final IntegerProperty getLastElement() { return lastElement; }

    @Override
    public String toString() {
        return "Row{" +
                "name=" + name +
                ", ects=" + ects +
                '}';
    }
}
