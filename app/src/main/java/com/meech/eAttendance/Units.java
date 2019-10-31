package com.meech.eAttendance;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.meech.eAttendance.SplashActivity.MY_PERMISSIONS_REQUEST_CAMERA;

public class Units extends AppCompatActivity {
    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    ArrayList<HashMap<String, String>> UnitsLists;
    final Context context = this;
    public static final String TAG_UNITNAME = "AssignUnit";
    public static final String TAG_UNITPROG = "AssignProg";
    public static final String TAG_UNITLEC = "AssignLec";
    public static final String TAG_UNITDEPT = "AssignDept";
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    ListAdapter adapter;
    JSONArray arr;
    String email, dept, serverResponse;
    SharedPreferences myunitpref, attendedunit;
    //This is to store the unit so that it can be passed to the sharedpref string
    String[] myunit, prog;
    ArrayList<String> myunit_passed, prog_passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            new QueryAssignedUnits().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerNotAvailable();
        }
    }

    private class QueryAssignedUnits extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Units.this, SweetAlertDialog.PROGRESS_TYPE);
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
            myunitpref = Units.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            email=myunitpref.getString("Email", null);
            dept=myunitpref.getString("departmentName", null);

            List<NameValuePair> Details = new ArrayList<NameValuePair>();
            Details.add(new BasicNameValuePair("AssignLecEmail", email));
            Details.add(new BasicNameValuePair("AssignDept", dept));


            jsonObject = jsonParser.makeHttpRequest1(Utility.url_returnassignedunits, "POST", Details);
            Log.e("##requests", ""+jsonObject);

            UnitsLists = new ArrayList<HashMap<String, String>>();
            myunit_passed = new ArrayList<String>();
            prog_passed = new ArrayList<String>();
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
                                String AssignUnit = c.getString(TAG_UNITNAME);
                                String AssignDept = c.getString(TAG_UNITDEPT);
                                String AssignProg = c.getString(TAG_UNITPROG);
                                String AssignLec = c.getString(TAG_UNITLEC);

                                //pass the unitname to the Array_list
                                myunit_passed.add(AssignUnit);
                                prog_passed.add(AssignProg);

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                map.put(TAG_UNITNAME, AssignUnit);
                                map.put(TAG_UNITDEPT, AssignDept);
                                map.put(TAG_UNITPROG, AssignProg);
                                map.put(TAG_UNITLEC, AssignLec);

                                // adding HashList to ArrayList
                                UnitsLists.add(map);
                                myunit = myunit_passed.toArray(new String[myunit_passed.size()]);
                                prog = prog_passed.toArray(new String[prog_passed.size()]);
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

                        sDialog = new SweetAlertDialog(Units.this, SweetAlertDialog.ERROR_TYPE);
                        sDialog.setTitleText("Oops!");
                        sDialog.setContentText("Unable to get units.");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sDialog.dismissWithAnimation();
                                Intent i = new Intent(getApplicationContext(), Departments.class);
                                startActivity(i);
                                finish();
                            }
                        });
                        sDialog.show();

                    } else {

                            adapter = new SimpleAdapter(
                                    Units.this, UnitsLists, R.layout.units_list, new String[]{
                                    TAG_UNITNAME, TAG_UNITPROG},
                                    new int[]{R.id.unittitle, R.id.programme});


                            final ListView listView = (ListView) findViewById(R.id.unitslist);
                            listView.setTextFilterEnabled(true);
                            try {
                                if (adapter.getCount() != 0) {

                                    listView.setAdapter(adapter);
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            //put the "unitname" in sharedpref to pass to scanner
                                            attendedunit = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);

                                            String Selectedunit = myunit[position];
                                            String programme = prog_passed.toString();

                                            SharedPreferences.Editor editor = attendedunit.edit();
                                            editor.putString("MyUnit", Selectedunit);
                                            editor.putString("unitprog", programme);
                                            editor.apply();

                                            if (editor.commit()) {
                                                checkcamera();
                                            }

                                        }
                                    });

                                } else {

                                    sDialog = new SweetAlertDialog(Units.this, SweetAlertDialog.WARNING_TYPE);
                                    sDialog.setTitleText("SORRY");
                                    sDialog.setContentText("You don't have any units in this department");
                                    sDialog.setConfirmText("OK");
                                    sDialog.setCancelable(false);
                                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            Intent j = new Intent(Units.this, Departments.class);
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

    public void checkcamera(){
        //check for camera permission
        if (ContextCompat.checkSelfPermission(Units.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Units.this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
        //if permission has already been granted
        else{
            Intent scan = new Intent(Units.this, Scanner.class);
            startActivity(scan);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted,
                    Intent intent = new Intent(this, Scanner.class);
                    startActivity(intent);
                } else {
                    // permission denied, boo!
                    Intent intent = new Intent(this, Units.class);
                    startActivity(intent);
                }

            }
        }
    }
}
