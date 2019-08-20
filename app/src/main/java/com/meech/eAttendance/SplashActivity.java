package com.meech.eAttendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SplashActivity extends AppCompatActivity {
    final public static int MY_PERMISSIONS_REQUEST_CAMERA =1;
    final public static int MY_PERMISSIONS_REQUEST_READ_AND_WRITE =2;

    SweetAlertDialog sDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            sDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
            sDialog.setContentText("Change IP?");
            sDialog.setConfirmText("YES");
            sDialog.setCancelable(true);
            sDialog.setCancelText("NO");
            sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    Intent intent = new Intent(SplashActivity.this, ChangeIP.class);
                    startActivity(intent);
                    finish();
                    sDialog.dismissWithAnimation();
                }
            });
            sDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    sDialog.dismissWithAnimation();
                }
            });
            sDialog.show();

    }

}
