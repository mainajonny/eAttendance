package com.meech.eAttendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Download extends AppCompatActivity {
    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    JSONArray jsonArray;
    JSONParser jsonParser=new JSONParser();
    ArrayList<HashMap<String, String>> DepartmentLists;
    final Context context = this;
    public static final String TAG_DEPTNAME = "DeptName";
    public static final String TAG_DEPTFACULTY = "DeptFaculty";
    ListAdapter adapter;
    SharedPreferences mypreferences;
    //This is to store the title so that it can be passed to the sharedpref string
    String[] dept;
    ArrayList<String> department_passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            new Download.GetAllDepartments().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }

    }

    class GetAllDepartments extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Download.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Loading.Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... arg0) {

            // Creating service handler class instance
            List<NameValuePair> departments = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            jsonArray = jsonParser.makeHttpRequest(Utility.url_returnalldepartments, "GET", departments);
            Log.e("##requests", ""+jsonArray);

            DepartmentLists = new ArrayList<HashMap<String, String>>();

            try {
                //Check your log cat for JSON response
                Log.d("departments are..", jsonArray.toString());
            }catch (Exception e){
                e.printStackTrace();
            }

            if (jsonArray != null) {
                department_passed = new ArrayList<String>();

                try {
                    if (jsonArray.length() > 0) {

                        // looping through All departments
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);

                            // Storing each json item in variable
                            String name = c.getString(TAG_DEPTNAME);
                            String faculty = c.getString(TAG_DEPTFACULTY);

                            //pass the title to the Array_list
                            department_passed.add(name);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_DEPTNAME, name);
                            map.put(TAG_DEPTFACULTY, faculty);


                            // adding HashList to ArrayList
                            DepartmentLists.add(map);
                            dept = department_passed.toArray(new String[department_passed.size()]);
                        }

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {

            }
            return null;

        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismissWithAnimation();

            if (jsonArray == null) {

                //Toast.makeText(getApplicationContext(), "Server not available, please try again later", Toast.LENGTH_SHORT).show();
                ServerError();

            }
            else {

                adapter = new SimpleAdapter(
                Download.this, DepartmentLists,
                R.layout.downloaddept_list,
                new String[]{TAG_DEPTNAME, TAG_DEPTFACULTY},
                new int[]{R.id.depttitle, R.id.deptfaculty});


                final ListView listView = (ListView) findViewById(R.id.downloaddeptlist);
                listView.setTextFilterEnabled(true);
                try {
                    if (adapter.getCount() != 0) {

                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                //put the "title" in sharedprref to pass to courses.class
                                mypreferences = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);

                                String SelectedDept=dept[position];

                                SharedPreferences.Editor editor = mypreferences.edit();
                                editor.putString("deptName", SelectedDept);
                                editor.apply();

                                if(editor.commit()){
                                    //Toast.makeText(MainActivity.this, "Departments in: "+SelectedFaculty, Toast.LENGTH_SHORT).show();

                                    Intent i = new Intent(getApplicationContext(), Courses.class);
                                    startActivity(i);
                                }
                            }
                        });

                    } else {

                        sDialog = new SweetAlertDialog(Download.this, SweetAlertDialog.WARNING_TYPE);
                        sDialog.setTitleText("SORRY");
                        sDialog.setContentText("You don't have any departments at the moment");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent j = new Intent(Download.this, Dashboard.class);
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
    }
}
