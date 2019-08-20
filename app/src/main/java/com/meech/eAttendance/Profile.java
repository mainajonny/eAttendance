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
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Profile extends AppCompatActivity {
    TextView Name;
    TextView Idno;
    TextView Dept;
    TextView Email;

    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    final Context context = this;
    public static final String TAG_NAME = "LecName";
    public static final String TAG_ID = "IdNo";
    public static final String TAG_DEPT = "LecDept";
    public static final String TAG_EMAIL = "LecEmail";
    public static final String TAG_PASSWORD = "LecPassword";
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    JSONArray arr;
    String email, serverResponse;
    SharedPreferences mypref, details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            new Getprofile().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }

    }

    private class Getprofile extends AsyncTask<String, String, String> {
        String LecName;
        String LecId;
        String LecDept;
        String LecEmail;
        String LecPassword;
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Profile.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            //getting the email from sharedprefs
            mypref = Profile.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            email=mypref.getString("Email", null);

            List<NameValuePair> profdetails = new ArrayList<NameValuePair>();
            profdetails.add(new BasicNameValuePair("LecEmail", email));

            jsonObject = jsonParser.makeHttpRequest1(Utility.url_returnprofiledetails, "POST", profdetails);
            Log.e("##requests", ""+jsonObject);

            try {
                serverResponse=jsonObject.getString(TAG_RESPONSE_CODE);
                Log.e("##Response: ", serverResponse);

                arr = new JSONArray(serverResponse);

                        if (arr.length() > 0) {

                            // looping through All lecs
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject c = arr.getJSONObject(i);
                                Log.e("##jsonobject", c.toString());

                                // Storing each json item in variable
                                LecName = c.getString(TAG_NAME);
                                LecId = c.getString(TAG_ID);
                                LecDept = c.getString(TAG_DEPT);
                                LecEmail = c.getString(TAG_EMAIL);
                                LecPassword = c.getString(TAG_PASSWORD);

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
                ServerError();
            }else{

                if (arr == null) {

                    sDialog = new SweetAlertDialog(Profile.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("Oops!");
                    sDialog.setContentText("Unable to get details.");
                    sDialog.setConfirmText("OK");
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
                }else{
                    Name = findViewById(R.id.Lecname);
                    Idno = findViewById(R.id.Lecid);
                    Dept = findViewById(R.id.Lecdept);
                    Email = findViewById(R.id.Lecemail);

                    Name.setText(LecName);
                    Idno.setText(LecId);
                    Dept.setText(LecDept);
                    Email.setText(LecEmail);

                    details = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);

                    SharedPreferences.Editor editor = details.edit();
                    editor.putString("Name", LecName);
                    editor.putString("Id", LecId);
                    editor.putString("Dept", LecDept);
                    editor.putString("Email", LecEmail);
                    editor.putString("Password", LecPassword);
                    editor.apply();
                }
            }
        }
    }

    public void edit(View view) {

        overridePendingTransition(R.anim.sliding_out_anim, R.anim.sliding_in_anim);

        Intent i = new Intent(Profile.this, Edit_profile.class);
        startActivity(i);
        finish();
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
