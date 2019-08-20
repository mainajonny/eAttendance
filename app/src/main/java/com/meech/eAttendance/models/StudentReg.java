package com.meech.eAttendance.models;

import com.orm.SugarRecord;

public class StudentReg extends SugarRecord {

    String regno;
    String programme;
    String unit;
    String lecturer;
    int attendance;
    String isuploaded;

    public StudentReg(){

    }

    public StudentReg(String regno, String programme, String unit, String lecturer, int attendance, String isuploaded) {
        this.regno = regno;
        this.programme = programme;
        this.unit = unit;
        this.lecturer = lecturer;
        this.attendance = attendance;
        this.isuploaded = isuploaded;
    }

    public String getRegno() {
        return regno;
    }

    public void setRegno(String regno) {
        this.regno = regno;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    public String getIsuploaded() {
        return isuploaded;
    }

    public void setIsuploaded(String isuploaded) {
        this.isuploaded = isuploaded;
    }
}
