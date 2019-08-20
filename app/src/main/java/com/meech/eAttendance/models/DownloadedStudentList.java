package com.meech.eAttendance.models;

import com.orm.SugarRecord;

public class DownloadedStudentList extends SugarRecord {
    String studname;
    String regnumber;
    String prog;
    String proglevel;
    String year;
    String semester;

    public DownloadedStudentList() {
    }

    public DownloadedStudentList(String studname, String regnumber, String prog, String proglevel, String year, String semester) {
        this.studname=studname;
        this.regnumber=regnumber;
        this.prog=prog;
        this.proglevel=proglevel;
        this.year=year;
        this.semester=semester;
    }

    public String getStudname() {
        return studname;
    }

    public void setStudname(String studname) {
        this.studname = studname;
    }

    public String getRegnumber() {
        return regnumber;
    }

    public void setRegnumber(String regnumber) {
        this.regnumber = regnumber;
    }

    public String getProg() {
        return prog;
    }

    public void setProg(String prog) {
        this.prog = prog;
    }

    public String getProglevel() {
        return proglevel;
    }

    public void setProglevel(String proglevel) {
        this.proglevel = proglevel;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
