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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Edit_profile extends AppCompatActivity {
    SweetAlertDialog pDialog;
    SweetAlertDialog sDialog;
    final Context context = this;
    public static final String TAG_RESPONSE_CODE="responseCode";
    JSONParser jsonParser=new JSONParser();
    JSONObject jsonObject;
    String serverResponse;

    EditText nm;
    TextView dept;
    EditText email;
    EditText passw;
    EditText newpass;
    EditText confirmpass;

    SharedPreferences details;
    String lecName, lecId, lecDept, lecEmail, lecPassword, pass, em;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nm = findViewById(R.id.name);
        dept = findViewById(R.id.dept);
        email = findViewById(R.id.email);
        passw = findViewById(R.id.pass);
        newpass = findViewById(R.id.newpass);
        confirmpass = findViewById(R.id.confirmpass);

        details = Edit_profile.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
        lecName=details.getString("Name", null);
        lecDept=details.getString("Dept", null);
        lecEmail=details.getString("Email", null);
        lecPassword=details.getString("Password", null);

        nm.setText(lecName);
        dept.setText(lecDept);
        email.setText(lecEmail);

    }

    public void dept(View view) {
        Toast.makeText(context, "Department is managed by your organisation", Toast.LENGTH_SHORT).show();
    }

    private class Updateprofile extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(Edit_profile.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText("Updating profile. Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {

            details = Edit_profile.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            lecId=details.getString("Id", null);

                String lecname = nm.getText().toString();
                String lecemail = email.getText().toString();
                String lecpass = newpass.getText().toString();

                List<NameValuePair> Details = new ArrayList<NameValuePair>();
                Details.add(new BasicNameValuePair("LecName", lecname));
                Details.add(new BasicNameValuePair("IdNo", lecId));
                Details.add(new BasicNameValuePair("LecDept", lecDept));
                Details.add(new BasicNameValuePair("LecEmail", lecemail));
                Details.add(new BasicNameValuePair("LecPassword", lecpass));

                jsonObject = jsonParser.makeHttpRequest1(Utility.url_updateprofile, "POST", Details);

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

                //check if login credentials changed and prompt logout
                details = Edit_profile.this.getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
                pass = details.getString("Passw", null);
                em = details.getString("Email", null);

                if (serverResponse.equals("215")) {

                    if(!newpass.getText().toString().equals(pass) || !email.getText().toString().equals(em)){

                        sDialog = new SweetAlertDialog(Edit_profile.this, SweetAlertDialog.SUCCESS_TYPE);
                        sDialog.setTitleText("Email or Password changed,");
                        sDialog.setContentText("please login again.");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        sDialog.show();
                    }else{

                        sDialog = new SweetAlertDialog(Edit_profile.this, SweetAlertDialog.SUCCESS_TYPE);
                        sDialog.setTitleText("SUCCESS");
                        sDialog.setContentText("Profile updated successfully.");
                        sDialog.setConfirmText("OK");
                        sDialog.setCancelable(false);
                        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent j = new Intent(Edit_profile.this, Profile.class);
                                startActivity(j);
                                finish();
                            }
                        });
                        sDialog.show();
                    }

                } else {
                    sDialog = new SweetAlertDialog(Edit_profile.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("SORRY");
                    sDialog.setContentText("Profile update failed, try again later.");
                    sDialog.setConfirmText("OK");
                    sDialog.setCancelable(false);
                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent j = new Intent(Edit_profile.this, Dashboard.class);
                            startActivity(j);
                            finish();
                        }
                    });
                    sDialog.show();
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

    public void save(View view) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            if (!validate()) {
                return;
            }
            new Updateprofile().execute();

        }else{
            /*Toast.makeText(context, "No network activity", Toast.LENGTH_SHORT).show();*/
            ServerError();
        }

    }

    public void cancel(View view) {

        overridePendingTransition(R.anim.sliding_in_anim, R.anim.sliding_out_anim);

        Intent i = new Intent(Edit_profile.this, Dashboard.class);
        startActivity(i);
    }

    public boolean validate() {
        boolean valid = true;

        String Email = email.getText().toString();
        String oldpass = passw.getText().toString();
        String Pass = newpass.getText().toString();
        String confirm = confirmpass.getText().toString();

        if (Email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            email.setError("Enter a valid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        if (oldpass.isEmpty()){
            newpass.setText(lecPassword);
            confirmpass.setText(lecPassword);
            Toast.makeText(context, "Password not changed", Toast.LENGTH_SHORT).show();

        }else if(!oldpass.equals(lecPassword)){
            passw.setError("Wrong Old password");
            valid = false;

        }else if (!Pass.equals(confirm)) {
            confirmpass.setError("Password mismatch");
            valid = false;

        }else if(Pass.length() < 6){
            newpass.setError("Password must have at least 6 characters");
            valid = false;

        } else{
            newpass.setError(null);
        }

        return valid;
    }
}
