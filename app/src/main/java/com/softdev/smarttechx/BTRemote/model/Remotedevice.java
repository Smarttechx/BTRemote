package com.softdev.smarttechx.BTRemote.model;

import java.io.Serializable;

/**
 * Created by SMARTTECHX on 9/17/2017.
 */

public class Remotedevice implements Serializable {

    String RemoteNickName;
    String DeviceID;
    String id;
    String Mac_Add;
    private static final long serialVersionUID = 1L;
    public Remotedevice( )
    {

    }

    public Remotedevice( String id,String nickName, String Deviceid, String Mac_add)
    {
        this.DeviceID=Deviceid;
        this.id=id;
        this.RemoteNickName=nickName;
        this.Mac_Add =Mac_add;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getNickName()
    {
        return RemoteNickName;
    }
    public String getDeviceid()
    {
        return DeviceID;
    }
    public String getMac_Add()
    {
        return Mac_Add;
    }
    public void setNickName(String name) {
        this.RemoteNickName = name;
    }
    public void setDeviceID(String deviceID) {
        this.DeviceID = deviceID;
    }
    public void setMacAdd(String mac_add) {
        this.Mac_Add = mac_add;
    }
}
