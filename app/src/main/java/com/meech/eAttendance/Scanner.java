package com.meech.eAttendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.meech.eAttendance.models.DownloadedStudentList;
import com.meech.eAttendance.models.StudentReg;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Scanner extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    SharedPreferences records;
    SweetAlertDialog sDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                Scanner.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                            try {
                                List<StudentReg> studentRegList = StudentReg.listAll(StudentReg.class);
                                //Log if DB has any data
                                Log.e("##size", " " + studentRegList.size());

                                        records = Scanner.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);

                                String Lec = records.getString("Email", null);
                                String Unit = records.getString("MyUnit", null);
                                String Prog = records.getString("unitprog", null);
                                String RegNo = result.getText();
                                Integer Attend = 1;
                                String isuploaded = "1";

                                List<StudentReg> studentRegList1 = StudentReg.find(StudentReg.class, "regno = ?",RegNo);
                                List<StudentReg> studentRegList2 = StudentReg.find(StudentReg.class, "lecturer = ?", Lec);
                                List<StudentReg> studentRegList3 = StudentReg.find(StudentReg.class, "unit = ?", Unit);

                                List<DownloadedStudentList> studentslist =
                                        DownloadedStudentList.find(DownloadedStudentList.class, "regnumber = ?", RegNo);

                                if(studentslist.size() > 0){

                                    if (studentRegList1.size() > 0  && studentRegList2.size() > 0 && studentRegList3.size() > 0) {

                                        /*Integer prevatt = studentRegList1.get(0).getAttendance();
                                        Log.e("##attendancebefore", "" + studentRegList1.get(0).getAttendance());
                                        Integer NewAtt = prevatt + 1;
                                        studentRegList1.get(0).setAttendance(NewAtt);*/
                                        studentRegList1.get(0).setLecturer(Lec);
                                        studentRegList1.get(0).setUnit(Unit);
                                        studentRegList1.get(0).setRegno(RegNo);
                                        studentRegList1.get(0).setProgramme(Prog);
                                        studentRegList1.get(0).setIsuploaded(isuploaded);
                                        studentRegList1.get(0).save();

                                        /*Log.e("##attAfter", "" + studentRegList1.get(0).getAttendance());*/

                                        Toast.makeText(Scanner.this, "Success: " + RegNo, Toast.LENGTH_SHORT).show();
                                    }else {

                                        StudentReg studentReg = new StudentReg();
                                        studentReg.setRegno(RegNo);
                                        studentReg.setLecturer(Lec);
                                        studentReg.setUnit(Unit);
                                        studentReg.setProgramme(Prog);
                                        studentReg.setAttendance(Attend);
                                        studentReg.setIsuploaded(isuploaded);
                                        studentReg.save();

                                            Log.e("##student", "RegNo: " + RegNo);
                                            Log.e("##student", "Lec: " + Lec);
                                            Log.e("##student", "Prog: " + Prog);
                                            Log.e("##student", "Unit: " + Unit);
                                            Log.e("##student", "attend: " + Attend);
                                            Log.e("##student", "isuploaded: " + isuploaded);

                                            Toast.makeText(Scanner.this, "Success: " + RegNo, Toast.LENGTH_SHORT).show();
                                        }


                                } else{

                                        sDialog = new SweetAlertDialog(Scanner.this, SweetAlertDialog.WARNING_TYPE);
                                        sDialog.setTitleText("SORRY");
                                        sDialog.setContentText("Student not found!");
                                        sDialog.setConfirmText("Ok");
                                        sDialog.setCancelable(false);
                                        sDialog.show();

                                }


                            } catch (Exception e) {
                                Log.e("##error", "" + e.toString());
                                e.printStackTrace();
                            }
                        //Toast.makeText(Scanner.this, result.getText(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public void finish(View view) {

                Intent j = new Intent(Scanner.this, Dashboard.class);
                startActivity(j);
    }




}
