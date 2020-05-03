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

    public final BooleanProperty nameAndEcts = new SimpleBooleanProperty();
    public final BooleanProperty nameAndEctsProperty(){ return nameAndEcts; }
    public final boolean getNameAndEcts() {
        return nameAndEctsProperty().get();
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
