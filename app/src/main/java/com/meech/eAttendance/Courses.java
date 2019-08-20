package com.meech.eAttendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Courses extends AppCompatActivity {
    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    ArrayList<HashMap<String, String>> ProgsLists;
    final Context context = this;
    public static final String TAG_PROGNAME = "ProgName";
    public static final String TAG_PROGLEVEL = "ProgLevel";
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    ListAdapter adapter;
    JSONArray arr;
    String dept, serverResponse;
    SharedPreferences mycoursepref, attendedunit;
    //This is to store the unit so that it can be passed to the sharedpref string
    String[] prog, level;
    ArrayList<String> prog_passed, level_passed;
    EditText ET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            new Courses.QueryCourses().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerNotAvailable();
        }

    }

    private class QueryCourses extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Courses.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Getting units. Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            //getting the faculty name from sharedprefs
            mycoursepref = Courses.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            dept=mycoursepref.getString("deptName", null);

            List<NameValuePair> Details = new ArrayList<NameValuePair>();
            Details.add(new BasicNameValuePair("ProgDept", dept));


            jsonObject = jsonParser.makeHttpRequest1(Utility.url_returnprogrammes, "POST", Details);
            Log.e("##requests", ""+jsonObject);

            ProgsLists = new ArrayList<HashMap<String, String>>();
            prog_passed = new ArrayList<String>();
            level_passed = new ArrayList<String>();
            try {
                serverResponse=jsonObject.getString(TAG_RESPONSE_CODE);
                Log.e("##Response: ", "Server response "+serverResponse);

                arr = new JSONArray(serverResponse);

                if (arr.length() > 0) {

                    // looping through All departments
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject c = arr.getJSONObject(i);
                        Log.e("##jsonobject", c.toString());

                        // Storing each json item in variable
                        String progname = c.getString(TAG_PROGNAME);
                        String proglevel = c.getString(TAG_PROGLEVEL);

                        //pass the unitname to the Array_list
                        prog_passed.add(progname);
                        level_passed.add(proglevel);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PROGNAME, progname);
                        map.put(TAG_PROGLEVEL, proglevel);

                        // adding HashList to ArrayList
                        ProgsLists.add(map);
                        prog = prog_passed.toArray(new String[prog_passed.size()]);
                        level = level_passed.toArray(new String[level_passed.size()]);
                    }

                }

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismissWithAnimation();

            if (jsonObject==null){

                ServerNotAvailable();
            }else {

                if (arr == null) {

                    sDialog = new SweetAlertDialog(Courses.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("Oops!");
                    sDialog.setContentText("Unable to get courses.");
                    sDialog.setConfirmText("OK");
                    sDialog.setCancelable(false);
                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sDialog.dismissWithAnimation();
                            Intent i = new Intent(getApplicationContext(), Download.class);
                            startActivity(i);
                            finish();
                        }
                    });
                    sDialog.show();

                } else {

                    adapter = new SimpleAdapter(
                            Courses.this, ProgsLists,
                            R.layout.courses_list, new String[]{
                            TAG_PROGNAME, TAG_PROGLEVEL},
                            new int[]{R.id.coursetitle, R.id.courselevel});


                    final ListView listView = (ListView) findViewById(R.id.courseslist);
                    listView.setTextFilterEnabled(true);
                    try {
                        if (adapter.getCount() != 0) {

                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    String Selectedprog = prog[position];
                                    String SelectedprogLevel = level[position];

                                    attendedunit = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = attendedunit.edit();
                                    editor.putString("progname", Selectedprog);
                                    editor.putString("proglevel", SelectedprogLevel);
                                    editor.apply();

                                    if (editor.commit()) {
                                        //Toast.makeText(context, "Successfully passed: "+Selectedprog+" and "+SelectedprogLevel, Toast.LENGTH_SHORT).show();

                                        Intent i = new Intent(getApplicationContext(), SemYear.class);
                                        startActivity(i);

                                    }

                                }
                            });

                        } else {

                            sDialog = new SweetAlertDialog(Courses.this, SweetAlertDialog.WARNING_TYPE);
                            sDialog.setTitleText("SORRY");
                            sDialog.setContentText("No courses found!");
                            sDialog.setConfirmText("OK");
                            sDialog.setCancelable(false);
                            sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent j = new Intent(Courses.this, Departments.class);
                                    startActivity(j);
                                    finish();
                                }
                            });
                            sDialog.show();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void ServerNotAvailable(){
        sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Server error!");
        sDialog.setContentText("No server response.");
        sDialog.setConfirmText("Try again later?");
        sDialog.setCancelable(false);
        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sDialog.dismissWithAnimation();
                Intent i = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(i);
                finish();
            }
        });
        sDialog.show();
    }

}
