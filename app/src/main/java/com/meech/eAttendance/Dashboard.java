package com.meech.eAttendance;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.meech.eAttendance.models.DownloadedStudentList;
import com.meech.eAttendance.models.StudentReg;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.meech.eAttendance.JSONParser.jsonArray;
import static com.meech.eAttendance.SplashActivity.MY_PERMISSIONS_REQUEST_READ_AND_WRITE;

public class Dashboard extends AppCompatActivity {
    SweetAlertDialog pDialog;
    SweetAlertDialog sDialog;
    final Context context = this;
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    String serverResponse;
    public static final String TAG_NAME = "StudName";
    public static final String TAG_REGNO = "RegNo";
    public static final String TAG_PROG= "Prog";
    public static final String TAG_YEAR= "Year";
    public static final String TAG_SEM= "Sem";

    String notuploaded = "1";
    String isuploaded = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Toast.makeText(context, "No network activity", Toast.LENGTH_LONG).show();
        }

    }

    public void scan(View view) {

            List<DownloadedStudentList> downloadedStudentLists = DownloadedStudentList.listAll(DownloadedStudentList.class);
            //Log if DB has any data
            Log.e("##size", " "+downloadedStudentLists.size());

            if(downloadedStudentLists.size() > 0){

                Intent i = new Intent(Dashboard.this, MainActivity.class);
                startActivity(i);
            }else{

                sDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.WARNING_TYPE);
                sDialog.setTitleText("SORRY");
                sDialog.setContentText("Download students list first.");
                sDialog.setConfirmText("Ok");
                sDialog.setCancelable(false);
                sDialog.show();
            }

    }

    public void upload(View view) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){

            List<StudentReg> list = StudentReg.find(StudentReg.class, "isuploaded = ?", notuploaded);
            Log.e("##size", ""+list.size());

            if(list.size() > 0){

                new UploadRecords().execute();
            }else{

                sDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.WARNING_TYPE);
                sDialog.setTitleText("SORRY");
                sDialog.setContentText("No new records to upload.");
                sDialog.setConfirmText("OK");
                sDialog.setCancelable(false);
                sDialog.show();
            }

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }

    }

    public void download(View view) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            //check for storage permission
            if (ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Dashboard.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_AND_WRITE);
            }
            //if permission has already been granted
            else {
                Intent i = new Intent(Dashboard.this, Download.class);
                startActivity(i);
            }

            /*List<DownloadedStudentList> downloadedStudentLists;
            downloadedStudentLists=DownloadedStudentList.listAll(DownloadedStudentList.class);

            //Log if DB has any data
            Log.e("##size", " "+downloadedStudentLists.size());

            if(downloadedStudentLists.size() > 0){

                for(int j=0; j < downloadedStudentLists.size(); j++){
                    downloadedStudentLists.get(j).delete();
                }
                new GetStudentList().execute();
                Toast.makeText(context, "deleted first", Toast.LENGTH_SHORT).show();

            }else{

                new GetStudentList().execute();
                Toast.makeText(context, "was empty", Toast.LENGTH_SHORT).show();
            }*/

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }

    }

    public void profile(View view) {

                Intent j = new Intent(Dashboard.this, Profile.class);
                startActivity(j);
    }

    private class UploadRecords extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Uploading records. Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            ArrayList<StudentReg> studentRegs = new ArrayList<StudentReg>();
            List<StudentReg> students;
            students = StudentReg.find(StudentReg.class, "isuploaded = ?", notuploaded);
            //log size of db
            Log.e("##size", ""+students.size());

            for (int j=0; j<students.size(); j++) {

                String regno = students.get(j).getRegno();
                String lec = students.get(j).getLecturer();
                String prog = students.get(j).getProgramme();
                String unit = students.get(j).getUnit();
                Integer attend = students.get(j).getAttendance();

                List<NameValuePair> Details = new ArrayList<NameValuePair>();
                Details.add(new BasicNameValuePair("RegNumber", regno));
                Details.add(new BasicNameValuePair("Lecturer", lec));
                Details.add(new BasicNameValuePair("UnitProg", prog));
                Details.add(new BasicNameValuePair("Unit", unit));
                Details.add(new BasicNameValuePair("Attendance", String.valueOf(attend)));

                jsonObject = jsonParser.makeHttpRequest1(Utility.url_uploadrecords, "POST", Details);
            }

            try {
                serverResponse = jsonObject.getString(TAG_RESPONSE_CODE);
                Log.e("##Response: ", "Server response " + serverResponse);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismissWithAnimation();


            if (jsonObject == null) {

                ServerError();

            } else {

                if (serverResponse.equals("204")) {

                    sDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.SUCCESS_TYPE);
                    sDialog.setTitleText("SUCCESS");
                    sDialog.setContentText("Record uploaded successfully.");
                    sDialog.setConfirmText("OK");
                    sDialog.setCancelable(false);
                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                            List<StudentReg> studentRegs;
                            studentRegs = StudentReg.find(StudentReg.class,"isuploaded = ?",notuploaded);
                            Log.e("##size", ""+studentRegs.size());

                            for (int j=0; j<studentRegs.size(); j++) {

                                studentRegs.get(j).setIsuploaded(isuploaded);
                                studentRegs.get(j).save();
                            }
                            sDialog.dismiss();

                        }
                    });
                    sDialog.show();
                } else {

                    sDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("SORRY");
                    sDialog.setContentText("Record upload failed, try again later.");
                    sDialog.setConfirmText("OK");
                    sDialog.setCancelable(false);
                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent j = new Intent(getApplicationContext(), Dashboard.class);
                            startActivity(j);
                        }
                    });
                    sDialog.show();

                }

            }
        }
    }

    private class GetStudentList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Loading.Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... arg0) {
            JSONParser jsonParser = new JSONParser();

            // Creating service handler class instance
            List<NameValuePair> students = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            jsonArray = jsonParser.makeHttpRequest(Utility.url_returnallstudents, "GET", students);
            Log.e("##requests", ""+jsonArray);

            try {
                //Check your log cat for JSON response
                Log.d("students returned are..", jsonArray.toString());
            }catch(Exception e){
                e.printStackTrace();
            }

            if (jsonArray != null) {

                try {
                    if (jsonArray.length() > 0) {

                        // looping through Students list
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);

                            // Storing each json item in variable
                            String name = c.getString(TAG_NAME);
                            String regno = c.getString(TAG_REGNO);
                            String prog = c.getString(TAG_PROG);
                            String year = c.getString(TAG_YEAR);
                            String sem = c.getString(TAG_SEM);

                            Log.e("##size", "name"+name);
                            Log.e("##size", "regno"+regno);
                            Log.e("##size", "prog"+prog);
                            Log.e("##size", "year"+year);
                            Log.e("##size", "sem"+sem);

                            DownloadedStudentList list = new DownloadedStudentList();
                            list.setStudname(name);
                            list.setRegnumber(regno);
                            list.setProg(prog);
                            list.setYear(year);
                            list.setSemester(sem);
                            list.save();

                        }

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

                ServerError();

            }
            else {

                List<DownloadedStudentList> downloadedStudentLists =
                        DownloadedStudentList.listAll(DownloadedStudentList.class);
                //Log if DB has any data
                Log.e("##error", "size " + downloadedStudentLists.size());

                try {
                    if (downloadedStudentLists.size() != 0) {

                        sDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.SUCCESS_TYPE);
                        sDialog.setTitleText("SUCCESS");
                        sDialog.setContentText("Students list download successful!");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.show();

                    } else {

                        sDialog = new SweetAlertDialog(Dashboard.this, SweetAlertDialog.ERROR_TYPE);
                        sDialog.setTitleText("SORRY");
                        sDialog.setContentText("Students list download failed!");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
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

        if (id == R.id.logout) {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_AND_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted,
                    Intent i = new Intent(Dashboard.this, Download.class);
                    startActivity(i);
                } else {
                    // permission denied, boo!
                }

            }
        }
    }
}
