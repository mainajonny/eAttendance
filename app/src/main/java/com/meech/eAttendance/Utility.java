package com.meech.eAttendance;

import com.meech.eAttendance.models.mIP;

import java.util.List;

/**
 * Created by Meech  5/25/2018.
 */
public class Utility {

    //Your current machine IP
    //private static String SERVER_IP ="http://192.168.1.11";
    private static List<mIP> ipaddress = mIP.listAll(mIP.class);
    private static String IP = ipaddress.get(0).getIpaddress();
    private static String SERVER_IP ="http://"+IP;

    //Playframework always listens to port 9000 so this is static
    private static String SERVER_PORT ="9000";

    //Match backend urls with the app urls for sending and receiving data
    public static String url_LoginAuthentication=SERVER_IP+":"+SERVER_PORT+"/AuthenticateUser";

    public static String url_returnfaculties=SERVER_IP+":"+SERVER_PORT+"/ReturnAllFaculty";

    public static String url_returndepartments=SERVER_IP+":"+SERVER_PORT+"/returnDepartments";

    public static String url_returnalldepartments=SERVER_IP+":"+SERVER_PORT+"/returnAllDepartments";

    public static String url_returnprogrammes=SERVER_IP+":"+SERVER_PORT+"/returnProgrammes";

    public static String url_returnstudlist=SERVER_IP+":"+SERVER_PORT+"/returnStudList";

    public static String url_returnassignedunits=SERVER_IP+":"+SERVER_PORT+"/returnAssignedUnits";

    public static String url_uploadrecords=SERVER_IP+":"+SERVER_PORT+"/uploadattendance";

    public static String url_returnallstudents=SERVER_IP+":"+SERVER_PORT+"/returnAllStudents";

    public static String url_returnprofiledetails=SERVER_IP+":"+SERVER_PORT+"/returnProfiledetails";

    public static String url_updateprofile=SERVER_IP+":"+SERVER_PORT+"/Updateprofile";


}
