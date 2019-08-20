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
import android.view.Menu;
import android.view.MenuItem;
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


public class MainActivity extends AppCompatActivity {
    //private ProgressDialog pDialog;
    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    JSONArray jsonArray;
    JSONParser jsonParser=new JSONParser();
    ArrayList<HashMap<String, String>> FacultyLists;
    final Context context = this;
    public static final String TAG_TITLE = "FName";
    public static final String TAG_INITIAL = "FInt";
    public static final String TAG_MAJOR= "FMajor";
    ListAdapter adapter;
    SharedPreferences mypreferences;
    //This is to store the title so that it can be passed to the sharedpref string
    String[] faculty;
    ArrayList<String> faculty_passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            new GetAllFaculty().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }
    }

    //This AsyncTask gets all the faculties from backend.
    class GetAllFaculty extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading.Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/

            pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Loading.Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... arg0) {

            // Creating service handler class instance
            List<NameValuePair> faculties = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            jsonArray = jsonParser.makeHttpRequest(Utility.url_returnfaculties, "GET", faculties);
            Log.e("##requests", ""+jsonArray);

            FacultyLists = new ArrayList<HashMap<String, String>>();

            try {
                //Check your log cat for JSON response
                Log.d("faculties are..", jsonArray.toString());
            }catch (Exception e){
                e.printStackTrace();
            }

            if (jsonArray != null) {
                faculty_passed = new ArrayList<String>();

                try {
                    if (jsonArray.length() > 0) {

                        // looping through All faculties
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);

                            // Storing each json item in variable
                            String title = c.getString(TAG_TITLE);
                            String initial = c.getString(TAG_INITIAL);
                            String major = c.getString(TAG_MAJOR);

                            //pass the title to the Array_list
                            faculty_passed.add(title);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_TITLE, title);
                            map.put(TAG_INITIAL, initial);
                            map.put(TAG_MAJOR, major);


                            // adding HashList to ArrayList
                            FacultyLists.add(map);
                            faculty = faculty_passed.toArray(new String[faculty_passed.size()]);
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
                        MainActivity.this, FacultyLists,
                        R.layout.faculty_list,
                        new String[]{TAG_TITLE, TAG_INITIAL, TAG_MAJOR},
                        new int[]{R.id.title, R.id.initial, R.id.major});


                final ListView listView = (ListView) findViewById(R.id.facultylist);
                listView.setTextFilterEnabled(true);
                try {
                    if (adapter.getCount() != 0) {

                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                //put the "title" in sharedprref to pass to department.class
                                mypreferences = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);

                                String SelectedFaculty=faculty[position];

                                SharedPreferences.Editor editor = mypreferences.edit();
                                editor.putString("facultyName", SelectedFaculty);
                                editor.apply();

                                if(editor.commit()){
                                    //Toast.makeText(MainActivity.this, "Departments in: "+SelectedFaculty, Toast.LENGTH_SHORT).show();

                                    Intent i = new Intent(getApplicationContext(), Departments.class);
                                    startActivity(i);
                                }
                            }
                        });

                    } else {

                        sDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                        sDialog.setTitleText("SORRY");
                        sDialog.setContentText("You don't have any faculties at the moment");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent j = new Intent(MainActivity.this, Dashboard.class);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
