package com.softdev.smarttechx.BTRemote;

import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.softdev.smarttechx.BTRemote.data.SaveData;
import com.softdev.smarttechx.BTRemote.services.BluetoothBackgroundService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends AppCompatActivity {
    String remote_Name, remote_ID, devicename, repeat1, repeat2, setHour1, setMin1, setHour2, setMin2, datetime, updateTime;
    int no_of_control_device;
    private static final String WRITE_MSG = "com.softdev.smarttechx.BTRemote.services.write_msg";
    private static final String TAG = "update";
    private UIUpdateReceiver mUIUpdateReceiver;
    String updateResp, mac_address, controlData;
    CheckBox cR1, cR2;
    Button setAlarm1, setAlarm2, resetAlarm1, resetAlarm2;
    TextView dateTime;
    ToggleButton Alarm_One, Second_Alarm;
    SaveData savedate;
    Button setTimer1, setTimer2, setRepA1, setRepA2;
    String remainResp, aBut0, aBut1, aBut2, aBut3, aBut4, aBut5, aBut6, aBut7, bBut0, bBut1, bBut2, bBut3, bBut4, bBut5, bBut6, bBut7;
    private ScreenReceiver mScreenReceiver;
    private BluetoothAdapter mBluetoothAdapter = null;
    IntentFilter newIntent;
    private static final String STOP_SERVICE = "com.softdev.smarttechx.BTRemote.services.stop_service";
    private static final String CHECK_CONNECT = "com.softdev.smarttechx.BTRemote.services.check_connection";
    private final static String SERVICE_ID = "com.softdev.smarttechx.BTRemote.service.BluetoothBackgroundService";
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        setupToolBar();
        savedate = new SaveData(this);
        Bundle getDevice = getIntent().getExtras();
        remote_Name = getDevice.getString("name");
        remote_ID = getDevice.getString("id");
        devicename = getDevice.getString("conname");
        mac_address = getDevice.getString("mac");
        no_of_control_device = getDevice.getInt("control");
        setTimer1 = (Button) findViewById(R.id.setTimer1);
        setTimer2 = (Button) findViewById(R.id.setTimer2);
        setRepA1 = (Button) findViewById(R.id.a1RepeatDate);
        setRepA2 = (Button) findViewById(R.id.a2RepeatDate);
        dateTime = (TextView) findViewById(R.id.dateTime);
        setAlarm1 = (Button) findViewById(R.id.butSetAlarm_one);
        setAlarm2 = (Button) findViewById(R.id.butSetAlarm2);
        resetAlarm1 = (Button) findViewById(R.id.butResetAlarm1);
        resetAlarm2 = (Button) findViewById(R.id.butResetAlarm2);
        cR1 = (CheckBox) findViewById(R.id.checkR1);
        cR2 = (CheckBox) findViewById(R.id.checkR2);
        cR1.setChecked(true);
        cR2.setChecked(true);
        mUIUpdateReceiver = new UIUpdateReceiver();
        mScreenReceiver = new ScreenReceiver();
        newIntent = new IntentFilter(Intent.ACTION_SCREEN_ON);
        newIntent.addAction(Intent.ACTION_SCREEN_OFF);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        createSwitchButtonAlarm(no_of_control_device);
        createToggleButtonAlarm1(no_of_control_device);
        timerSetup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        if (id == R.id.action_synctime) {
            //synchorise time from hardware
            Calendar getCal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            String synctime = df.format(getCal.getTime());
            sendMsg(remote_ID + ",*," + synctime + ",~"); //device_id,*,Year,Month,Day,Hour,Minute,Second,~
            //update(update);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //showActionBar();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(null);
        TextView mTitle = (TextView) findViewById(R.id.appName);
        mTitle.setText(getString(R.string.dash_name));

    }

    private void timerSetup() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Calendar getCal = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss - EEEE");
                        updateTime = df.format(getCal.getTime());
                        dateTime.setText(updateTime);
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000);

        setAlarm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String AState = getStateAlarm2(no_of_control_device, Second_Alarm);
                    if (AState.contains("null")) {
                        AState = AState.replace("null", "-");
                    }
                    if (cR2.isChecked()) {
                        setRepA2.setText("00");
                        if (setTimer2.getText().toString().isEmpty()) {
                            setTimer2.setText("00:00");
                        }
                        String time = setTimer2.getText().toString().substring(0, 2) + "," + setTimer2.getText().toString().substring(3) + ",00".toString();
                        sendMsg(remote_ID + ",@,2,1,Y," + AState + time + ",~");
                        // Toast.makeText(TimerActivity.this, remote_ID + ",@,2,1,Y," + AState + time + ",~", Toast.LENGTH_SHORT).show();
                    } else if (!cR2.isChecked()) {
                        if (setTimer2.getText().toString().isEmpty() || setRepA2.getText().toString().isEmpty()) {
                            setTimer2.setText("00:00");
                            setRepA2.setText("00");
                        }
                        String time = setTimer2.getText().toString().substring(0, 2) + "," + setTimer2.getText().toString().substring(3) + "," + setRepA2.getText().toString();
                        sendMsg(remote_ID + ",@,2,1,N," + AState + time + ",~");
                    }


                } catch (Exception e) {

                }

            }
        });
        setAlarm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String AState = getStateAlarm1(no_of_control_device, Alarm_One);
                if (AState.contains("null")) {
                    AState = AState.replace("null", "-");
                }
                if (cR1.isChecked()) {
                    setRepA1.setText("00");
                    if (setTimer1.getText().toString().isEmpty()) {
                        //Toast.makeText(TimerActivity.this, "", Toast.LENGTH_SHORT).show();
                        setTimer1.setText("00:00");
                    }
                    String time = setTimer1.getText().toString().substring(0, 2) + "," + setTimer1.getText().toString().substring(3) + ",00".toString();
                    sendMsg(remote_ID + ",@,1,1,Y," + AState + time + ",~");
                    // Toast.makeText(TimerActivity.this, remote_ID + ",@,1,1,Y," + AState + time + ",~", Toast.LENGTH_SHORT).show();

                } else if (!cR1.isChecked()) {
                    if (setTimer1.getText().toString().isEmpty() || setRepA1.getText().toString().isEmpty()) {
                        //   Toast.makeText(TimerActivity.this, "", Toast.LENGTH_SHORT).show();
                        setTimer1.setText("00:00");
                        setRepA1.setText("00");
                    }
                    String time = setTimer1.getText().toString().substring(0, 2) + "," + setTimer1.getText().toString().substring(3) + "," + setRepA1.getText().toString();
                    sendMsg(remote_ID + ",@,1,1,N," + AState + time + ",~");
                    // Toast.makeText(TimerActivity.this, remote_ID + ",@,1,1,N," + AState + time + ",~", Toast.LENGTH_SHORT).show();
                }

            }
        });

        setTimer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerDialog(setTimer1);
            }
        });
        setTimer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerDialog(setTimer2);

            }
        });

        setRepA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog(setRepA1);
            }
        });
        setRepA2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog(setRepA2);

            }
        });

        resetAlarm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(TimerActivity.this, remote_ID + ",@,1,0,~", Toast.LENGTH_SHORT).show();
                sendMsg(remote_ID + ",@,1,0,~");
            }
        });

        resetAlarm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(remote_ID + ",@,2,0,~");
                // Toast.makeText(TimerActivity.this, remote_ID + ",@,2,0,~", Toast.LENGTH_SHORT).show();

            }
        });
    }


    public void TimerDialog(final Button but) {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        but.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    public void DateDialog(final Button but) {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        but.setText(String.valueOf(dayOfMonth));

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void createToggleButtonAlarm1(int num) {
        ToggleButton newToggleButton;
        for (int i = 0; i < num; i++) {
            newToggleButton = new ToggleButton(this);
            newToggleButton.setChecked(true);
            newToggleButton.setTextOn("On");
            newToggleButton.setTextOff("Off");
            newToggleButton.setId(10 + i);
            final int toggle_id2 = newToggleButton.getId();
            newToggleButton.setPadding(0, 0, 5, 5);
            newToggleButton.setHeight(10);
            newToggleButton.setWidth(10);
            newToggleButton.setGravity(Gravity.CENTER);
            TextView labelText = new TextView(this);
            LinearLayout text = new LinearLayout(this);
            text.setOrientation(LinearLayout.VERTICAL);
            labelText.setText(getString(R.string.Alarm, i));
            labelText.setTextSize(2, 14);
            labelText.setPadding(0, 0, 5, 5);
            labelText.setGravity(Gravity.CENTER);
            labelText.setTextColor(Color.WHITE);
            text.setGravity(Gravity.CENTER);
            text.addView(labelText);
            text.addView(newToggleButton);
            LinearLayout layout1 = (LinearLayout) findViewById(R.id.alarm1ButLayout1);
            LinearLayout layout2 = (LinearLayout) findViewById(R.id.alarm1ButLayout2);
            if (i % 2 != 0) {
                layout1.addView(text);
            } else if (i % 2 == 0) {
                layout2.addView(text);
            }
            Alarm_One = (ToggleButton) findViewById(toggle_id2);
            Alarm_One.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (toggle_id2 == 10) {
                        if (isChecked) {
                            aBut0 = "A";
                        } else {
                            aBut0 = "a";
                        }

                    } else if (toggle_id2 == 11) {
                        if (isChecked) {
                            aBut1 = "B";
                        } else {
                            aBut1 = "b";
                        }
                    } else if (toggle_id2 == 12) {
                        if (isChecked) {
                            aBut2 = "C";

                        } else {
                            aBut2 = "c";
                        }
                    } else if (toggle_id2 == 13) {
                        if (isChecked) {
                            aBut3 = "D";
                        } else {
                            aBut3 = "d";
                        }
                    } else if (toggle_id2 == 14) {
                        if (isChecked) {
                            aBut4 = "E";
                        } else {
                            aBut4 = "e";
                        }
                    } else if (toggle_id2 == 15) {
                        if (isChecked) {
                            aBut5 = "F";
                        } else {
                            aBut5 = "f";
                        }
                    } else if (toggle_id2 == 16) {
                        if (isChecked) {
                            aBut6 = "G";
                        } else {
                            aBut6 = "g";
                        }
                    } else if (toggle_id2 == 17) {
                        if (isChecked) {
                            aBut7 = "H";
                        } else {
                            aBut7 = "h";
                        }
                    }
                }
            });

        }

    }

    private void createSwitchButtonAlarm(int num) {
        ToggleButton newSwitch;
        for (int j = 0; j < num; j++) {
            newSwitch = new ToggleButton(this);
            newSwitch.setChecked(true);
            newSwitch.setTextOn("On");
            newSwitch.setTextOff("Off");
            newSwitch.setId(20 + j);
            final int toggle_id = newSwitch.getId();
            newSwitch.setPadding(0, 0, 5, 5);
            newSwitch.setHeight(10);
            newSwitch.setWidth(10);
            newSwitch.setGravity(Gravity.CENTER);
            TextView labelText = new TextView(this);
            LinearLayout text = new LinearLayout(this);
            text.setOrientation(LinearLayout.VERTICAL);
            labelText.setText(getString(R.string.Alarm, j));
            labelText.setTextSize(2, 14);
            labelText.setPadding(0, 0, 5, 5);
            labelText.setGravity(Gravity.CENTER);
            labelText.setTextColor(Color.WHITE);
            text.setGravity(Gravity.CENTER);
            text.addView(labelText);
            text.addView(newSwitch);
            LinearLayout layout11 = (LinearLayout) findViewById(R.id.alarm2ButLayout1);
            LinearLayout layout12 = (LinearLayout) findViewById(R.id.alarm2ButLayout2);
            if (j % 2 != 0) {
                layout11.addView(text);
            } else if (j % 2 == 0) {
                layout12.addView(text);
            }
            Second_Alarm = (ToggleButton) findViewById(toggle_id);
            Second_Alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (toggle_id == 20) {
                        if (isChecked) {
                            bBut0 = "A";
                        } else {
                            bBut0 = "a";
                        }

                    } else if (toggle_id == 21) {
                        if (isChecked) {
                            bBut1 = "B";
                        } else {
                            bBut1 = "b";
                        }
                    } else if (toggle_id == 22) {
                        if (isChecked) {
                            bBut2 = "C";

                        } else {
                            bBut2 = "c";
                        }
                    } else if (toggle_id == 23) {
                        if (isChecked) {
                            bBut3 = "D";
                        } else {
                            bBut3 = "d";
                        }
                    } else if (toggle_id == 24) {
                        if (isChecked) {
                            bBut4 = "E";
                        } else {
                            bBut4 = "e";
                        }
                    } else if (toggle_id == 25) {
                        if (isChecked) {
                            bBut5 = "F";
                        } else {
                            bBut5 = "f";
                        }
                    } else if (toggle_id == 26) {
                        if (isChecked) {
                            bBut6 = "G";
                        } else {
                            bBut6 = "g";
                        }
                    } else if (toggle_id == 27) {
                        if (isChecked) {
                            bBut7 = "H";
                        } else {
                            bBut7 = "h";
                        }
                    }
                }
            });

        }

    }

    public void checkConnection() {
        Intent i = new Intent(CHECK_CONNECT);
        sendBroadcast(i);
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        registerReceiver(mUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.UI_UPDATE_MESSAGE));
        registerReceiver(mUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.SEND_CONNECT_STATUS));
        registerReceiver(mScreenReceiver, new IntentFilter(newIntent));
        checkConnection();

        //conStatus.setText(devicename);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mUIUpdateReceiver);
    }

    public boolean isMyServiceRunning(Context c) {
        ActivityManager manager = (ActivityManager) c.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_ID.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startService(String address) {
        if (!isMyServiceRunning(getBaseContext())) {
            Intent i = new Intent(TimerActivity.this, BluetoothBackgroundService.class);
            i.putExtra(BluetoothBackgroundService.DEVICE_ADDRESS, address);
            startService(i);
        }
    }


    private class UIUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothBackgroundService.SEND_CONNECT_STATUS)) {
                Boolean status = intent.getBooleanExtra(BluetoothBackgroundService.SEND_CHECK_CONNECT, false);
                if (status) {
                    sendMsg(remote_ID + ",0,~");
                } else {
                    startService(mac_address);
                }
            }
            if (intent.getAction().equals(BluetoothBackgroundService.UI_UPDATE_MESSAGE)) {
                String result = intent.getStringExtra(BluetoothBackgroundService.MSG_DATA);
                if (result.substring(0, 1).contains(" ")) {
                    result = result.replace(" ", "");
                }
                if (result.substring(0, 2).contains("a") || result.substring(0, 2).contains("A")) {
                    Toast.makeText(TimerActivity.this, "Getting Update..", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "respone " + result);
                    savedate.save_update(result);
                    updateTimeAlarm1(result);
                    updateTimeAlarm2(result);

                } else {
                    controlData = savedate.loadUpdate();
                    Log.d(TAG, "respone from save " + controlData);
                    updateTimeAlarm1(controlData);
                    updateTimeAlarm2(controlData);
                }
            }

        }
    }

    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                stopService();
                mBluetoothAdapter.disable();
                finish();
                unregisterReceiver(mScreenReceiver);
                System.exit(0);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void stopService() {
        Intent i = new Intent(STOP_SERVICE);
        sendBroadcast(i);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void sendMsg(String message) {
        Intent i = new Intent(WRITE_MSG);
        if (message != null) {
            i.putExtra("DATA", message);
        }
        this.sendBroadcast(i);
    }

    public void updateTimeAlarm1(String response) {

        if (response.contains("day")) {
            datetime = response.substring(response.indexOf('.') + 4, response.indexOf("day") + 3);
            datetime = updateTime;
            remainResp = response.replace(response.substring(0, response.indexOf("day") + 4), "");
            if (remainResp.substring(0, 1).contains("0") && remainResp.substring(2, 3).contains("0")) {
                remainResp = remainResp.replace(remainResp.substring(0, 12), "");
            } else if (remainResp.substring(0, 1).contains("1")) {
                String getR = remainResp.substring(2, 13);
                if (getR.substring(0, 1).contains("0") && getR.substring(1, 2).contains("0")) {
                    repeat1 = "00";
                    cR1.setChecked(true);
                } else if (!(getR.substring(0, 1).contains("0") && getR.substring(1, 2).contains("0"))) {
                    cR1.setChecked(false);
                    repeat1 = getR.substring(0, 2);
                }
                setHour1 = getR.substring(3, getR.indexOf(":"));
                setMin1 = getR.substring(getR.indexOf(":") + 1, getR.indexOf(":") + 3);
                setTimer1.setText(setHour1 + ":" + setMin1);
                setRepA1.setText(repeat1);
                remainResp = remainResp.substring(11, 20);
                // Toast.makeText(this, remainResp, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Timer 1 " + remainResp);
                for (int no = 0; no < no_of_control_device; no++) {
                    Log.d(TAG, "Timer 1 loop " + remainResp);
                    if (remainResp.substring(0, 1).contains("a")) {
                        Alarm_One = (ToggleButton) findViewById(10);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("a,", "");
                    } else if (remainResp.substring(0, 1).contains("A")) {
                        Alarm_One = (ToggleButton) findViewById(10);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("A,", "");
                    } else {
                        aBut0 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                    if (remainResp.substring(0, 1).contains("b")) {
                        Alarm_One = (ToggleButton) findViewById(11);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("b,", "");
                    } else if (remainResp.substring(0, 1).contains("B")) {
                        Alarm_One = (ToggleButton) findViewById(11);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("B,", "");
                    } else {
                        aBut1 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                    if (remainResp.substring(0, 1).contains("c")) {
                        Alarm_One = (ToggleButton) findViewById(12);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("c,", "");
                    } else if (remainResp.substring(0, 1).contains("C")) {
                        Alarm_One = (ToggleButton) findViewById(12);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("C,", "");
                    } else {
                        aBut2 = "-";
                        remainResp = remainResp.replace("-,", "");

                    }
                    if (remainResp.substring(0, 1).contains("d")) {
                        Alarm_One = (ToggleButton) findViewById(13);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("d,", "");
                    } else if (remainResp.substring(0, 1).contains("D")) {
                        Alarm_One = (ToggleButton) findViewById(13);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("D,", "");
                    } else {
                        aBut3 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                    if (remainResp.substring(0, 1).contains("e")) {
                        Alarm_One = (ToggleButton) findViewById(14);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("e,", "");
                    } else if (remainResp.substring(0, 1).contains("E")) {
                        Alarm_One = (ToggleButton) findViewById(14);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("E,", "");
                    } else {
                        aBut4 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                    if (remainResp.substring(0, 1).contains("f")) {
                        Alarm_One = (ToggleButton) findViewById(15);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("f,", "");
                    } else if (remainResp.substring(0, 1).contains("F")) {
                        Alarm_One = (ToggleButton) findViewById(15);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("F,", "");
                    } else {
                        aBut5 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                    if (remainResp.substring(0, 1).contains("g")) {
                        Alarm_One = (ToggleButton) findViewById(16);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("g,", "");
                    } else if (remainResp.substring(0, 1).contains("G")) {
                        Alarm_One = (ToggleButton) findViewById(16);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("G,", "");
                    } else {
                        aBut6 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                    if (remainResp.substring(0, 1).contains("h")) {
                        Alarm_One = (ToggleButton) findViewById(17);
                        Alarm_One.setChecked(false);
                        remainResp = remainResp.replace("h,", "");
                    } else if (remainResp.substring(0, 1).contains("H")) {
                        Alarm_One = (ToggleButton) findViewById(17);
                        Alarm_One.setChecked(true);
                        remainResp = remainResp.replace("H,", "");
                    } else {
                        aBut7 = "-";
                        remainResp = remainResp.replace("-,", "");
                    }
                }
                remainResp = "";
            }
        }
        // dateTime.setText(datetime);
    }

    public void updateTimeAlarm2(String response) {
        if (response.contains("day")) {
            updateResp = response.substring(response.indexOf("day") + 4);
            if (updateResp.substring(0, 1).contains("1")) {
                updateResp = updateResp.substring(11 + (no_of_control_device * 2));
            } else if (updateResp.substring(0, 1).contains("0")) {
                updateResp = updateResp.substring(4 + (no_of_control_device * 2));
            }
            // Toast.makeText(this, updateResp, Toast.LENGTH_SHORT).show();
            if (updateResp.substring(0, 1).contains("0") && updateResp.substring(2, 3).contains("0")) {
                updateResp = updateResp.replace(updateResp.substring(0, 12), "");
            } else if (updateResp.substring(0, 1).contains("1")) {
                cR2.setChecked(true);
                String getR = updateResp.substring(2, 13);
                if (getR.substring(0, 1).contains("0") && getR.substring(1, 2).contains("0")) {
                    repeat2 = "00";
                    cR2.setChecked(true);

                } else if (!(getR.substring(0, 1).contains("0") && getR.substring(1, 2).contains("0"))) {
                    repeat2 = getR.substring(0, 2);
                    cR2.setChecked(false);
                }
                setHour2 = getR.substring(3, getR.indexOf(":"));
                setMin2 = getR.substring(getR.indexOf(":") + 1, getR.indexOf(":") + 3);
                setTimer2.setText(setHour2 + ":" + setMin2);
                setRepA2.setText(repeat2);

                updateResp = updateResp.substring(11);
                // Toast.makeText(this, updateResp, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Timer 2 " + updateResp);
                for (int no = 0; no < no_of_control_device; no++) {
                    Log.d(TAG, "Timer 2 loop " + updateResp);
                    if (updateResp.substring(0, 1).contains("a")) {
                        Second_Alarm = (ToggleButton) findViewById(20);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("a,", "");
                    } else if (updateResp.substring(0, 1).contains("A")) {
                        Second_Alarm = (ToggleButton) findViewById(20);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("A,", "");
                    } else {
                        bBut0 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }
                    if (updateResp.substring(0, 1).contains("b")) {
                        Second_Alarm = (ToggleButton) findViewById(21);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("b,", "");
                    } else if (updateResp.substring(0, 1).contains("B")) {
                        Second_Alarm = (ToggleButton) findViewById(21);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("B,", "");
                    } else {
                        bBut1 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }
                    if (updateResp.substring(0, 1).contains("c")) {
                        Second_Alarm = (ToggleButton) findViewById(22);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("c,", "");
                    } else if (updateResp.substring(0, 1).contains("C")) {
                        Second_Alarm = (ToggleButton) findViewById(22);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("C,", "");
                    } else {
                        bBut2 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }
                    if (updateResp.substring(0, 1).contains("d")) {
                        Second_Alarm = (ToggleButton) findViewById(23);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("d,", "");
                    } else if (updateResp.substring(0, 1).contains("D")) {
                        Second_Alarm = (ToggleButton) findViewById(23);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("D,", "");
                    } else {

                        updateResp = updateResp.replace("-,", "");
                        bBut3 = "-";
                    }
                    if (updateResp.substring(0, 1).contains("e")) {
                        Second_Alarm = (ToggleButton) findViewById(24);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("e,", "");
                    } else if (updateResp.substring(0, 1).contains("E")) {
                        Second_Alarm = (ToggleButton) findViewById(24);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("E,", "");
                    } else {
                        bBut4 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }

                    if (updateResp.substring(0, 1).contains("f")) {
                        Second_Alarm = (ToggleButton) findViewById(25);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("f,", "");
                    } else if (updateResp.substring(0, 1).contains("F")) {
                        Second_Alarm = (ToggleButton) findViewById(25);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("F,", "");
                    } else {
                        bBut5 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }
                    if (updateResp.substring(0, 1).contains("g")) {
                        Second_Alarm = (ToggleButton) findViewById(26);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("g,", "");
                    } else if (updateResp.substring(0, 1).contains("G")) {
                        Second_Alarm = (ToggleButton) findViewById(26);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("G,", "");
                    } else {
                        bBut6 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }
                    if (updateResp.substring(0, 1).contains("h")) {
                        Second_Alarm = (ToggleButton) findViewById(27);
                        Second_Alarm.setChecked(false);
                        updateResp = updateResp.replace("h,", "");
                    } else if (updateResp.substring(0, 1).contains("H")) {
                        Second_Alarm = (ToggleButton) findViewById(27);
                        Second_Alarm.setChecked(true);
                        updateResp = updateResp.replace("H,", "");
                    } else {
                        bBut7 = "-";
                        updateResp = updateResp.replace("-,", "");
                    }
                }
                updateResp = "";
            }

        }

    }

    public String getStateAlarm2(int num, ToggleButton SwitchBut) {
        String state = "";
        for (int i = 0; i < num; i++) {
            SwitchBut = (ToggleButton) findViewById(20 + i);
            if ((i == 0) && (SwitchBut.getText() == bBut0)) {
                bBut0 = "-";
                state = state + bBut0 + ",";
            } else if ((i == 0) && (SwitchBut.getText() != bBut0)) {
                state = state + bBut0 + ",";
            }
            if ((i == 1) && (SwitchBut.getText() == bBut1)) {
                bBut1 = "-";
                state = state + bBut1 + ",";

            } else if ((i == 1) && (SwitchBut.getText() != bBut1)) {

                state = state + bBut1 + ",";

            }
            if ((i == 2) && (SwitchBut.getText() == bBut2)) {
                bBut2 = "-";
                state = state + bBut2 + ",";
            } else if ((i == 2) && (SwitchBut.getText() != bBut2)) {

                state = state + bBut2 + ",";
            }
            if ((i == 3) && (SwitchBut.getText() == bBut3)) {
                bBut3 = "-";
                state = state + bBut3 + ",";
            } else if ((i == 3) && (SwitchBut.getText() != bBut3)) {

                state = state + bBut3 + ",";
            }
            if ((i == 4) && (SwitchBut.getText() == bBut4)) {
                bBut4 = "-";
                state = state + bBut4 + ",";
            } else if ((i == 4) && (SwitchBut.getText() != bBut4)) {
                state = state + bBut4 + ",";
            }
            if ((i == 5) && (SwitchBut.getText() == bBut5)) {
                bBut5 = "-";
                state = state + bBut5 + ",";
            } else if ((i == 5) && (SwitchBut.getText() != bBut5)) {

                state = state + bBut5 + ",";
            }
            if ((i == 6) && (SwitchBut.getText() == bBut6)) {
                bBut6 = "-";
                state = state + bBut6 + ",";
            } else if ((i == 6) && (SwitchBut.getText() != bBut6)) {

                state = state + bBut6 + ",";
            }
            if ((i == 7) && (SwitchBut.getText() == bBut7)) {
                bBut7 = "-";
                state = state + bBut7 + ",";
            } else if ((i == 7) && (SwitchBut.getText() != bBut7)) {

                state = state + bBut7 + ",";
            }

        }
        return state;
    }

    public String getStateAlarm1(int num, ToggleButton toggleBut) {
        String state = "";
        for (int i = 0; i < num; i++) {
            toggleBut = (ToggleButton) findViewById(10 + i);
            if ((i == 0) && (toggleBut.getText() == aBut0)) {
                aBut0 = "-";
                state = state + aBut0 + ",";
            } else if ((i == 0) && (toggleBut.getText() != aBut0)) {
                state = state + aBut0 + ",";
            }
            if ((i == 1) && (toggleBut.getText() == aBut1)) {
                aBut1 = "-";
                state = state + aBut1 + ",";

            } else if ((i == 1) && (toggleBut.getText() != aBut1)) {

                state = state + aBut1 + ",";

            }
            if ((i == 2) && (toggleBut.getText() == aBut2)) {
                aBut2 = "-";
                state = state + aBut2 + ",";
            } else if ((i == 2) && (toggleBut.getText() != aBut2)) {

                state = state + aBut2 + ",";
            }
            if ((i == 3) && (toggleBut.getText() == aBut3)) {
                aBut3 = "-";
                state = state + aBut3 + ",";
            } else if ((i == 3) && (toggleBut.getText() != aBut3)) {

                state = state + aBut3 + ",";
            }
            if ((i == 4) && (toggleBut.getText() == aBut4)) {
                aBut4 = "-";
                state = state + aBut4 + ",";
            } else if ((i == 4) && (toggleBut.getText() != aBut4)) {
                state = state + aBut4 + ",";
            }
            if ((i == 5) && (toggleBut.getText() == aBut5)) {
                aBut5 = "-";
                state = state + aBut5 + ",";
            } else if ((i == 5) && (toggleBut.getText() != aBut5)) {

                state = state + aBut5 + ",";
            }
            if ((i == 6) && (toggleBut.getText() == aBut6)) {
                aBut6 = "-";
                state = state + aBut6 + ",";
            } else if ((i == 6) && (toggleBut.getText() != aBut6)) {

                state = state + aBut6 + ",";
            }
            if ((i == 7) && (toggleBut.getText() == aBut7)) {
                aBut7 = "-";
                state = state + aBut7 + ",";
            } else if ((i == 7) && (toggleBut.getText() != aBut7)) {

                state = state + aBut7 + ",";
            }

        }
        return state;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent controlIntent = new Intent(TimerActivity.this, ControlActivity.class);
        startActivity(controlIntent);
        finish();
    }

}
