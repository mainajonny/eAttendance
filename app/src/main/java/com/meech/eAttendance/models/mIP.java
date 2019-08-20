package com.meech.eAttendance.models;

import com.orm.SugarRecord;

public class mIP extends SugarRecord {

    String ipaddress;

    public mIP(){

    }

    public mIP(String ipaddress){
        this.ipaddress = ipaddress;

    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }
}
