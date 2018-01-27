package com.softdev.smarttechx.BTRemote.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.softdev.smarttechx.BTRemote.model.Remotedevice;
import com.softdev.smarttechx.BTRemote.util.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 9/18/2017.
 */

public class SaveData {
    private static final String SHARED_PREFS_FILE = "shared_prefs_file";
    private static final String REMOTE = "remote";
    private static final String MAC_ADD = "macadd";
    private static final String UPDATE = "update";
    private static final String TEMP = "temp";
    private static final String BUTNAME0 = "name0";
    private static final String BUTNAME1 = "name1";
    private static final String BUTNAME2 = "name2";
    private static final String BUTNAME3 = "name3";
    private static final String BUTNAME4 = "name4";
    private static final String BUTNAME5 = "name5";
    private static final String BUTNAME6 = "name6";
    private static final String BUTNAME7 = "name7";
    ;

    private SharedPreferences pref;
    // Editor for Shared preferences
    private SharedPreferences.Editor editormac;
    private SharedPreferences prefmac;
    private SharedPreferences.Editor editorUpdate;
    private SharedPreferences prefUpdate;
    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    private SharedPreferences prefTemp;
    // Editor for Shared preferences
    private SharedPreferences.Editor editorTemp;

    private SharedPreferences prefName;
    // Editor for Shared preferences
    private SharedPreferences.Editor editorName;

    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    public SaveData(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editor = pref.edit();
        prefmac = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editormac = prefmac.edit();
        prefUpdate = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorUpdate = prefUpdate.edit();
        prefTemp = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorTemp = prefTemp.edit();
        prefName = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorName = prefName.edit();
    }

    public void save(ArrayList<Remotedevice> remoteList) {

        try {
            editor.putString(REMOTE, ObjectSerializer.serialize(remoteList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();


    }

    public ArrayList<Remotedevice> loadData() {
        ArrayList<Remotedevice> remoteList = new ArrayList<>();
        if (pref != null) {
            try {
                remoteList = (ArrayList<Remotedevice>) ObjectSerializer.deserialize(pref.getString(REMOTE,
                        ObjectSerializer.serialize(new ArrayList<Remotedevice>())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            remoteList = new ArrayList<Remotedevice>();
        }
        return remoteList;
    }

    public void save_update(String update) {


        try {
            editorUpdate.putString(UPDATE, ObjectSerializer.serialize(update));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorUpdate.commit();


    }

    public String loadUpdate() {
        String updateList = new String();
        if (prefmac != null) {
            try {
                updateList = (String) ObjectSerializer.deserialize(prefUpdate.getString(UPDATE,
                        ObjectSerializer.serialize(new String())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            updateList = new String();
        }
        return updateList;
    }

    public void save_temp(String temp) {


        try {
            editorTemp.putString(TEMP, ObjectSerializer.serialize(temp));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorTemp.commit();


    }

    public String loadTemp() {
        String Temp = new String();
        if (prefmac != null) {
            try {
                Temp = (String) ObjectSerializer.deserialize(prefTemp.getString(TEMP,
                        ObjectSerializer.serialize(new String())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Temp = new String();
        }
        return Temp;
    }

    public void save_mac(ArrayList<String> MacList) {
        try {
            editormac.putString(MAC_ADD, ObjectSerializer.serialize(MacList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editormac.commit();


    }

    public ArrayList<String> loadMac_Add() {
        ArrayList<String> macList = new ArrayList<>();
        if (prefmac != null) {
            try {
                macList = (ArrayList<String>) ObjectSerializer.deserialize(prefmac.getString(MAC_ADD,
                        ObjectSerializer.serialize(new ArrayList<String>())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            macList = new ArrayList<String>();
        }
        return macList;
    }


    public void save_name(int id, String name) {
        if (id == 10) {
            try {
                editorName.putString(BUTNAME0, ObjectSerializer.serialize(name));
            } catch (IOException e) {
                e.printStackTrace();
            }
            editorName.commit();
        } else if (id == 11) {
            try {
                editorName.putString(BUTNAME1, ObjectSerializer.serialize(name));
            } catch (IOException e) {
                e.printStackTrace();
            }
            editorName.commit();
        }
    }

    public String loadName(int id) {
        String Name = new String();
        if (id == 10) {
            if (prefName != null) {
                try {
                    Name = (String) ObjectSerializer.deserialize(prefName.getString(BUTNAME0,
                            ObjectSerializer.serialize(new String())));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Name = new String();
            }

        }else  if (id == 11) {
            if (prefName != null) {
                try {
                    Name = (String) ObjectSerializer.deserialize(prefName.getString(BUTNAME1,
                            ObjectSerializer.serialize(new String())));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Name = new String();
            }

        }
        return Name;

    }
}
