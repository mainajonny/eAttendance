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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Departments extends AppCompatActivity {
    SweetAlertDialog sDialog;
    private SweetAlertDialog pDialog;
    ArrayList<HashMap<String, String>> DepartmentLists;
    final Context context = this;
    public static final String TAG_DEPTNAME = "DeptName";
    public static final String TAG_DEPTFACULTY = "DeptFaculty";
    public static final String TAG_DEPTHEAD = "DeptHead";
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    ListAdapter adapter;
    JSONArray arr;
    String name, serverResponse;
    SharedPreferences mypref, mydepts;
    //This is to store the dept so that it can be passed to the sharedpref string
    String[] department;
    ArrayList<String> department_passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departments);

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            new QueryDepartments().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }
    }

    private class QueryDepartments extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(Departments.this);
            pDialog.setMessage("Getting departments. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/

            pDialog = new SweetAlertDialog(Departments.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Getting departments. Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            //getting the faculty name from sharedprefs
            mypref = Departments.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            name=mypref.getString("facultyName", null);

            List<NameValuePair> DeptDetails = new ArrayList<NameValuePair>();
            DeptDetails.add(new BasicNameValuePair("FName", name));

            jsonObject = jsonParser.makeHttpRequest1(Utility.url_returndepartments, "POST", DeptDetails);
            Log.e("##requests", ""+jsonObject);

            DepartmentLists = new ArrayList<HashMap<String, String>>();

            try {
                serverResponse=jsonObject.getString(TAG_RESPONSE_CODE);
                Log.e("##Response: ", serverResponse);

                arr = new JSONArray(serverResponse);

                    department_passed = new ArrayList<String>();

                        if (arr.length() > 0) {

                            // looping through All departments
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject c = arr.getJSONObject(i);
                                Log.e("##jsonobject", c.toString());

                                // Storing each json item in variable
                                String DeptName = c.getString(TAG_DEPTNAME);
                                String DeptFaculty = c.getString(TAG_DEPTFACULTY);
                                String DeptHead = c.getString(TAG_DEPTHEAD);

                                //pass the deptname to the Array_list
                                department_passed.add(DeptName);

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                map.put(TAG_DEPTNAME, DeptName);
                                map.put(TAG_DEPTFACULTY, DeptFaculty);
                                map.put(TAG_DEPTHEAD, DeptHead);

                                // adding HashList to ArrayList
                                DepartmentLists.add(map);
                                department = department_passed.toArray(new String[department_passed.size()]);
                            }

                        }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismissWithAnimation();

            if (jsonObject==null){

                ServerError();
            }else {

                if (arr == null) {

                    sDialog = new SweetAlertDialog(Departments.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("Oops!");
                    sDialog.setContentText("Unable to get departments.");
                    sDialog.setConfirmText("OK");
                    sDialog.setCancelable(false);
                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sDialog.dismissWithAnimation();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                    sDialog.show();
                }else{

                    adapter = new SimpleAdapter(
                            Departments.this, DepartmentLists,
                            R.layout.department_list, new String[]{
                            TAG_DEPTNAME, TAG_DEPTFACULTY, TAG_DEPTHEAD},
                            new int[]{R.id.depttitle, R.id.deptfaculty,R.id.depthead});


                    final ListView listView = (ListView) findViewById(R.id.departmentlist);
                    listView.setTextFilterEnabled(true);
                    try {
                        if (adapter.getCount() != 0) {

                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    //put the "title" in sharedprref to pass to department.class
                                    mydepts = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);

                                    String SelectedDeptartment=department[position];

                                    SharedPreferences.Editor editor = mydepts.edit();
                                    editor.putString("departmentName", SelectedDeptartment);
                                    editor.apply();

                                    if(editor.commit()){
                                        //Toast.makeText(MainActivity.this, "Departments in: "+SelectedFaculty, Toast.LENGTH_SHORT).show();

                                        Intent i = new Intent(getApplicationContext(), Units.class);
                                        startActivity(i);
                                    }
                                }
                            });

                        } else {
                            /*final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    context);

                            // set title
                            alertDialogBuilder.setTitle("SORRY");
                            alertDialogBuilder.setCancelable(true);
                            // set dialog message
                            alertDialogBuilder
                                    .setMessage("You don't have any departments in this faculty")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent j = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(j);
                                        }
                                    });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();*/

                            sDialog = new SweetAlertDialog(Departments.this, SweetAlertDialog.WARNING_TYPE);
                            sDialog.setTitleText("SORRY");
                            sDialog.setContentText("You don't have any departments in this faculty");
                            sDialog.setConfirmText("Ok");
                            sDialog.setCancelable(false);
                            sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            Intent j = new Intent(Departments.this, MainActivity.class);
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
                        Intent i = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(i);
                        finish();
                    }
                });
        sDialog.show();

        //Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
    }

}
