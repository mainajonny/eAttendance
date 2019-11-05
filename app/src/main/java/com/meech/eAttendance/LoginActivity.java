package com.meech.eAttendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @BindView(com.meech.eAttendance.R.id.input_email) EditText _emailText;
    @BindView(com.meech.eAttendance.R.id.input_password) EditText _passwordText;
    @BindView(com.meech.eAttendance.R.id.btn_login) Button _loginButton;
    SweetAlertDialog pDialog;
    SweetAlertDialog sDialog;
    JSONObject jsonObject;
    JSONParser jsonParser=new JSONParser();
    String serverResponse;
    public static final String TAG_RESPONSE_CODE="responseCode";
    SharedPreferences Loginpref;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.meech.eAttendance.R.layout.activity_login);
        ButterKnife.bind(this);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                login();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
            //Authenticate
            new SendPostRequest().execute();

    }

    public class SendPostRequest extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.primary);
            pDialog.getProgressHelper().setRimColor(R.color.primary_darker);
            pDialog.setTitleText(getString(R.string.authenticating));
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            _emailText = findViewById(R.id.input_email);
            _passwordText = findViewById(R.id.input_password);

            String email = _emailText.getText().toString();
            String password = _passwordText.getText().toString();

            List<NameValuePair> RegistrationDetails = new ArrayList<NameValuePair>();
            RegistrationDetails.add(new BasicNameValuePair("LecEmail", email));
            RegistrationDetails.add(new BasicNameValuePair("LecPassword", password));

            Loginpref = getSharedPreferences("com.meech.eAttendance", MODE_PRIVATE);
            SharedPreferences.Editor editor = Loginpref.edit();
            editor.putString("Email", email);
            editor.putString("Passw", password);
            editor.apply();

            if(editor.commit()) {

                jsonObject = jsonParser.makeHttpRequest1(Utility.url_LoginAuthentication, "POST", RegistrationDetails);

                try {
                    serverResponse = jsonObject.getString(TAG_RESPONSE_CODE);
                    Log.e("##Response: ", "Server response: " + serverResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismissWithAnimation();

            if (jsonObject==null){

                ConnectNetwork();
                _loginButton.setEnabled(true);
            }else {

                if (serverResponse.equals("200")){

                    Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                    startActivity(intent);
                    finish();

                }else if (serverResponse.equals("202")){
                    InactiveUser();
                    _loginButton.setEnabled(true);

                }else {
                    WrongCredentials();
                    _loginButton.setEnabled(true);
                }


            }
        }
    }

    public void ConnectNetwork(){
        sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Error!");
        sDialog.setContentText("No network response.");
        sDialog.setConfirmText("OK");
        sDialog.setCancelable(false);
        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sDialog.dismissWithAnimation();

            }
        });
        sDialog.show();
    }

    public void InactiveUser(){
        sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Error!");
        sDialog.setContentText("That user is deactivated");
        sDialog.setCancelable(false);
        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sDialog.dismissWithAnimation();
            }
        });
        sDialog.show();

        //Toast.makeText(this, "Wrong credentials", Toast.LENGTH_SHORT).show();
    }

    public void WrongCredentials(){
        sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Error!");
        sDialog.setContentText("Wrong Email or Password");
        sDialog.setCancelable(false);
        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sDialog.dismissWithAnimation();
                    }
                });
        sDialog.show();

        //Toast.makeText(this, "Wrong credentials", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginFailed() {
        //Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        sDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Invalid Email or Password");
        sDialog.setConfirmText("OK");
        sDialog.setCancelable(false);
        sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                });
        sDialog.show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 /*|| password.length() > 10*/) {
            _passwordText.setError("Invalid password");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
