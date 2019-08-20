package com.meech.eAttendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.meech.eAttendance.models.DownloadedStudentList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.meech.eAttendance.JSONParser.jsonArray;

public class SemYear extends AppCompatActivity {
    SweetAlertDialog pDialog;
    SweetAlertDialog sDialog;
    RadioGroup radioGroup;
    RadioButton ys11,ys12,ys21,ys22,ys31,ys32,ys41,ys42;
    int curCheckedid;
    boolean isClickSet = true;
    SharedPreferences yearsem, getdetails, getdetails1;
    Button download;
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    String serverResponse;
    public static final String TAG_NAME = "StudName";
    public static final String TAG_REGNO = "RegNo";
    public static final String TAG_PROG= "Prog";
    public static final String TAG_YEAR= "Year";
    public static final String TAG_SEM= "Sem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sem_year);

        /*yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
        SharedPreferences.Editor edit = yearsem.edit();
        edit.clear();
        edit.apply();*/

        radioGroup = (RadioGroup) findViewById(R.id.semyeargroup);
        ys11 = findViewById(R.id.semyear1_1);
        ys12 = findViewById(R.id.semyear1_2);
        ys21 = findViewById(R.id.semyear2_1);
        ys22 = findViewById(R.id.semyear2_2);
        ys31 = findViewById(R.id.semyear3_1);
        ys32 = findViewById(R.id.semyear3_2);
        ys41 = findViewById(R.id.semyear4_1);
        ys42 = findViewById(R.id.semyear4_2);
        curCheckedid = ys11.getId();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                curCheckedid = radioGroup.getCheckedRadioButtonId();
                if (ys11.isChecked() && isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(1));
                    editor.putString("Sem", String.valueOf(1));
                    editor.apply();

                    isClickSet = true;

                } else if (ys12.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(1));
                    editor.putString("Sem", String.valueOf(2));
                    editor.apply();

                    isClickSet = true;

                } else if (ys21.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(2));
                    editor.putString("Sem", String.valueOf(1));
                    editor.apply();

                    isClickSet = true;

                } else if (ys22.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(2));
                    editor.putString("Sem", String.valueOf(2));
                    editor.apply();

                    isClickSet = true;

                } else if (ys31.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(3));
                    editor.putString("Sem", String.valueOf(1));
                    editor.apply();

                    isClickSet = true;

                } else if (ys32.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(3));
                    editor.putString("Sem", String.valueOf(2));
                    editor.apply();

                    isClickSet = true;

                } else if (ys41.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", String.valueOf(4));
                    editor.putString("Sem", String.valueOf(1));
                    editor.apply();

                    isClickSet = true;

                } else if (ys42.isChecked()&& isClickSet) {

                    yearsem = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = yearsem.edit();
                    editor.putString("Year", "4");
                    editor.putString("Sem", "2");
                    editor.commit();

                    isClickSet = true;

                }
            }
        });

        download = findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetStudentList().execute();
            }
        });

    }

    private class GetStudentList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(SemYear.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Loading.Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... arg0) {
            getdetails = SemYear.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            String pname = getdetails.getString("progname", null);
            String plevel = getdetails.getString("proglevel", null);
            String year = getdetails.getString("Year", null);
            String sem = getdetails.getString("Sem", null);

            List<NameValuePair> Details = new ArrayList<NameValuePair>();

            Details.add(new BasicNameValuePair("Prog", pname));
            Details.add(new BasicNameValuePair("ProgLevel", plevel));
            Details.add(new BasicNameValuePair("Year", year));
            Details.add(new BasicNameValuePair("Sem", sem));
            // getting JSON string from URL
            jsonObject = jsonParser.makeHttpRequest1(Utility.url_returnstudlist, "POST", Details);
            Log.e("##requests", ""+jsonObject);

            try {
                serverResponse=jsonObject.getString(TAG_RESPONSE_CODE);
                Log.e("##Response: ", "Server response "+serverResponse);

                jsonArray = new JSONArray(serverResponse);

                    if (jsonArray.length() > 0) {

                        // looping through Students list
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);
                            Log.e("##jsonobject", c.toString());

                            // Storing each json item in variable
                            String name = c.getString(TAG_NAME);
                            String regno = c.getString(TAG_REGNO);
                            String prog = c.getString(TAG_PROG);
                            String yr = c.getString(TAG_YEAR);
                            String sm = c.getString(TAG_SEM);

                            Log.e("##size", "name"+name);
                            Log.e("##size", "regno"+regno);
                            Log.e("##size", "prog"+prog);
                            Log.e("##size", "year"+yr);
                            Log.e("##size", "sem"+sm);

                            DownloadedStudentList list = new DownloadedStudentList();
                            list.setStudname(name);
                            list.setRegnumber(regno);
                            list.setProg(prog);
                            list.setYear(yr);
                            list.setSemester(sm);
                            list.save();

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            return null;


        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismissWithAnimation();

            if (jsonArray == null) {

                ServerError();

            }
            else {

                List<DownloadedStudentList> downloadedStudentLists =
                        DownloadedStudentList.listAll(DownloadedStudentList.class);
                //Log if DB has any data
                Log.e("##error", "size " + downloadedStudentLists.size());

                try {
                    if (downloadedStudentLists.size() != 0) {

                        sDialog = new SweetAlertDialog(SemYear.this, SweetAlertDialog.SUCCESS_TYPE);
                        sDialog.setTitleText("SUCCESS");
                        sDialog.setContentText("Proceed to scanning.");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent j = new Intent(SemYear.this, Dashboard.class);
                                startActivity(j);
                                finish();
                            }
                        });
                        sDialog.show();

                    } else {

                        sDialog = new SweetAlertDialog(SemYear.this, SweetAlertDialog.ERROR_TYPE);
                        sDialog.setTitleText("SORRY");
                        sDialog.setContentText("Students list download failed!");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(true);
                        sDialog.show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void ServerError(){
        sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Server error!");
        sDialog.setContentText("No server response.");
        sDialog.setConfirmText("Try again later?");
        sDialog.setCancelable(false);
        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sDialog.dismissWithAnimation();

            }
        });
        sDialog.show();
    }

}
