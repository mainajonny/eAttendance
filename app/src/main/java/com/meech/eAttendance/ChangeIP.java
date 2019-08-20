package com.meech.eAttendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.meech.eAttendance.models.mIP;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangeIP extends AppCompatActivity {

    SweetAlertDialog pDialog;
    SweetAlertDialog sDialog;

    EditText mIPaddress;
    Button _change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ip);

        mIPaddress = findViewById(R.id.changeip);

        _change = findViewById(R.id.buttonchangeip);
        _change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveIP();

                Intent intent = new Intent(ChangeIP.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }

    public void SaveIP() {
        String ip = mIPaddress.getText().toString();

        mIP ipaddress = new mIP();
        ipaddress.setIpaddress(ip);
        ipaddress.update();

        List<mIP> ipList;
        ipList=mIP.listAll(mIP.class);

        //Log if DB has any data
        Log.e("##size", " "+ipList.size());

        Toast.makeText(this, "Using IP: "+ip, Toast.LENGTH_SHORT).show();
    }
}
