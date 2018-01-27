package com.softdev.smarttechx.BTRemote;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.softdev.smarttechx.BTRemote.data.SaveData;
import com.softdev.smarttechx.BTRemote.model.Remotedevice;
import com.softdev.smarttechx.BTRemote.services.BluetoothBackgroundService;

import org.w3c.dom.Text;

import java.util.List;

import static com.softdev.smarttechx.BTRemote.R.string.setTime;


public class ControlActivity extends AppCompatActivity {

    private final static String SERVICE_ID = "com.softdev.smarttechx.BTRemote.service.BluetoothBackgroundService";
    private static final String CHECK_CONNECT= "com.softdev.smarttechx.BTRemote.services.check_connection";
    private static final String STOP_SERVICE = "com.softdev.smarttechx.BTRemote.services.stop_service";
    private static final String WRITE_MSG = "com.softdev.smarttechx.BTRemote.services.write_msg";
    // Layout Views
    String remote_Name, remote_ID, mac_address, response, switchname;
    int no_of_control_device, saveid;
    TextView deviceName, conStatus, temp, switchName; ;
    String Temp, con_status;
    ToggleButton controlBut;
    SaveData savedate;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ScreenReceiver mScreenReceiver;
    private cUIUpdateReceiver mcUIUpdateReceiver;
    Remotedevice remote;
    SharedPreferences Gp;
    SharedPreferences GpDevice;
    IntentFilter newIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        savedate = new SaveData(ControlActivity.this);
        setupToolBar();
        mcUIUpdateReceiver = new cUIUpdateReceiver();
        mScreenReceiver = new ScreenReceiver();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        newIntent = new IntentFilter(Intent.ACTION_SCREEN_ON);
        newIntent.addAction(Intent.ACTION_SCREEN_OFF);
        deviceName = (TextView) findViewById(R.id.remoteName);
        conStatus = (TextView) findViewById(R.id.conStatus);
        temp = (TextView) findViewById(R.id.tempUpdate);
        setControl();

    }

    public  void setControl(){
        Gp = getSharedPreferences("getname", MODE_PRIVATE);
        GpDevice = getSharedPreferences("device", MODE_PRIVATE);
        Intent getDevice = getIntent();
        remote = (Remotedevice) getDevice.getSerializableExtra("remote");
        if(remote==null){
           GpDevice = getSharedPreferences("device", MODE_PRIVATE);
            remote_Name = GpDevice.getString("remote_name",null);
            remote_ID = GpDevice.getString("remote_id",null);
            no_of_control_device = GpDevice.getInt("no_device",0);
            mac_address= GpDevice.getString("mac_add",null);
            checkConnection();
        }
        else{
            remote_Name = remote.getNickName();
            remote_ID = remote.getDeviceid();
            no_of_control_device = Integer.valueOf(remote.getDeviceid().substring(0, 1));
            mac_address=remote.getMac_Add();
            startService(mac_address);
        }
        deviceName.setText(remote_Name);
        createToggleButton(no_of_control_device);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //showActionBar();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(null);
        TextView mTitle=(TextView)findViewById(R.id.appName);
        mTitle.setText(getString(R.string.dash_name));
        ImageButton mSetBut=(ImageButton)findViewById(R.id.btnsetTime);
        mSetBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resp = savedate.loadUpdate();
                Intent setTime = new Intent(ControlActivity.this, TimerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", remote_Name);
                bundle.putString("id", remote_ID);
                bundle.putString("conname",  con_status);
                bundle.putString("mac", mac_address);
                bundle.putInt("control", no_of_control_device);
                setTime.putExtras(bundle);
                startActivity(setTime);
                //finish();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mcUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.UI_UPDATE_MESSAGE));
        registerReceiver(mcUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.AUTH_ID));
        registerReceiver(mcUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.CONNECT_STATUS));
        registerReceiver(mcUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.SEND_CONNECT_STATUS));
        registerReceiver(mScreenReceiver, new IntentFilter(newIntent));
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mcUIUpdateReceiver);
    }
    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences.Editor e = GpDevice.edit();
        e.putString("remote_name", remote_Name);
        e.putString("remote_id", remote_ID);
        e.putString("mac_add", mac_address);
        e.putInt("no_device", no_of_control_device);
        e.apply();
       /* ActivityManager Am=(ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks= Am.getRunningTasks(1);
        if(!tasks.isEmpty()){
            ComponentName topActivity=tasks.get(0).topActivity;
            if(!topActivity.getPackageName().equals(this.getPackageName())){
                stopService();
                unregisterReceiver(mScreenReceiver);
                finish();
                mBluetoothAdapter.disable();
                System.exit(0);
            }
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();

        String update = savedate.loadUpdate();
        //Toast.makeText(this().getApplicationContext(), update, Toast.LENGTH_SHORT).show();
        if (update.length() > 0) {
            try {
                update(update);
            }
            catch (Exception e){

            }
        }
        String getTemp= savedate.loadTemp();
        temp.setText(getString(R.string.temp,getTemp));
    }



    public void checkConnection(){
        Intent i = new Intent(CHECK_CONNECT);
        sendBroadcast(i);
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
            Intent i = new Intent(ControlActivity.this, BluetoothBackgroundService.class);
            i.putExtra(BluetoothBackgroundService.DEVICE_ADDRESS, address);
            startService(i);
        }
    }

    public void stopService(){
        Intent i = new Intent(STOP_SERVICE);
        sendBroadcast(i);
    }

    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                stopService();
                unregisterReceiver(mScreenReceiver);
                finish();
                mBluetoothAdapter.disable();
                System.exit(0);
            }
        }
    }

    private class cUIUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothBackgroundService.UI_UPDATE_MESSAGE)) {
                response =intent.getStringExtra(BluetoothBackgroundService.MSG_DATA);
                if(response.contains(" ")){
                    response=response.replace(" ","");
                }
                String rest="&"+response;
                if(rest.endsWith("~")&&(rest.startsWith("&"))){
                    if(response.contains("\n")){
                        response=response.replace("\n","");
                    }
                    if(response.contains(" ")){
                        response=response.replace(" ","");
                    }
                    savedate.save_update(response);
                    update(response);
                    try {
                        Temp = response.substring((response.lastIndexOf(".")-2), (response.lastIndexOf(".") + 3));
                        if(Temp.contains(",")){
                            Temp=Temp.replace(",","");
                        }
                        temp.setText(getString(R.string.temp, Temp));
                        savedate.save_temp(Temp);
                        response = "";
                    }catch (Exception e){
                        //
                    }
                }
            }

            if (intent.getAction().equals(BluetoothBackgroundService.CONNECT_STATUS)) {
                final String message = intent.getStringExtra(BluetoothBackgroundService.CONNECT_UPDATE_MESSAGE_DETAIL);
                if (message.contains("Connected to")) {
                    sendMsg(remote_ID + ",0,~");
                    conStatus.setText("Connected");
                } else if (message.contains("connecting")) {
                    conStatus.setText(message);
                }
            }

            if (intent.getAction().equals(BluetoothBackgroundService.SEND_CONNECT_STATUS)) {
                Boolean status=intent.getBooleanExtra(BluetoothBackgroundService.SEND_CHECK_CONNECT,false);
                if(status==true){
                    con_status= "Connected";
                    conStatus.setText(con_status);
                    sendMsg(remote_ID + ",0,~");
                }
                else{
                    con_status= "Not Connected";
                    conStatus.setText(con_status);
                    startService(mac_address);
                }
            }
        }
    }

    private void createToggleButton(int num) {
        ToggleButton newToggleButton; TextView switchText;
        for (int i = 0; i < num; i++) {
            newToggleButton = new ToggleButton(this);
            newToggleButton.setChecked(true);
            newToggleButton.setTextOn("On");
            newToggleButton.setTextOff("Off");
            newToggleButton.setId(i);
            final int toggle_id = newToggleButton.getId();
            newToggleButton.setPadding(0, 0, 0, 10);
            switchText = new TextView(this);
            switchText.setText(getString(R.string.Switch, i));
            switchText.setId(10+i);
            switchText.setTextSize(2, 14);
            switchText.setPadding(2, 0, 0, 3);
            newToggleButton.setGravity(Gravity.CENTER);
            newToggleButton.setWidth(200);
            newToggleButton.setHeight(100);
            LinearLayout text = new LinearLayout(this);
            text.setOrientation(LinearLayout.VERTICAL);
            switchText.setGravity(Gravity.CENTER);
            switchText.setTextColor(Color.WHITE);
            text.setGravity(Gravity.CENTER);
            newToggleButton.setTextSize(2,20);
            Display display = getWindowManager().getDefaultDisplay();
            int width=display.getWidth();
            double ratio= ((float)(width))/300.0;
            int height = (int)(ratio*70);
            newToggleButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
           // newToggleButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.set));
            text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1.0f));
            text.setPadding(0,20,10,20);
            text.addView(switchText);
            text.addView(newToggleButton);
            LinearLayout layout1 = (LinearLayout) findViewById(R.id.control1Layout);
            LinearLayout layout2 = (LinearLayout) findViewById(R.id.control2Layout);
            LinearLayout layout3 = (LinearLayout) findViewById(R.id.control3Layout);
            LinearLayout layout4 = (LinearLayout) findViewById(R.id.control4Layout);

            if (i % 2 != 0 && i < 4) {
                layout1.addView(text);
            } else if (i % 2 == 0 && i < 4) {
                layout2.addView(text);
            } else if (i % 2 != 0 && i >= 4) {
                layout3.addView(text);
            } else if (i % 2 == 0 && i >= 4) {
                layout4.addView(text);
            }
            switchname=Gp.getString(remote_Name+remote_ID+String.valueOf(10+i), null);
            switchName=(TextView)findViewById(10+i);
            if(switchname==null){
                switchName.setText(getString(R.string.Switch, i));
            }
            else {
                switchName.setText(switchname);

            }
            //Toast.makeText(ControlActivity.this, "getting "+switchname, Toast.LENGTH_SHORT).show();
            controlBut = (ToggleButton) findViewById(toggle_id);
            controlBut.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (true) {
                        if (toggle_id == 0) {
                             promptEdit(toggle_id);
                        } else if (toggle_id == 1) {
                            promptEdit(toggle_id);
                        } else if (toggle_id == 2) {
                          promptEdit(toggle_id);
                        } else if (toggle_id == 3) {
                           promptEdit(toggle_id);
                        } else if (toggle_id == 4) {
                           promptEdit(toggle_id);
                        } else if (toggle_id == 5) {
                           promptEdit(toggle_id);
                        } else if (toggle_id == 6) {
                           promptEdit(toggle_id);
                        } else if (toggle_id == 7) {
                           promptEdit(toggle_id);
                        }
                    }
                    return true;
                }
            });
            controlBut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (toggle_id == 0) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",A,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",A,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",a,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",a,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 1) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",B,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",B,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",b,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",b,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 2) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",C,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",C,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",c,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",c,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 3) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",D,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",D,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",d,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",d,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 4) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",E,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",E,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",e,~");
                            //Toast.makeText(this().getApplicationContext(), remote_ID + ",e,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 5) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",F,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",F,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",f,~");
                            //  Toast.makeText(this().getApplicationContext(), remote_ID + ",f,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 6) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",G,~");
                            //  Toast.makeText(this().getApplicationContext(), remote_ID + ",G,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",g,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",g,~", Toast.LENGTH_SHORT).show();
                        }
                    } else if (toggle_id == 7) {
                        if (isChecked) {
                            sendMsg(remote_ID + ",H,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",H,~", Toast.LENGTH_SHORT).show();
                        } else {
                            sendMsg(remote_ID + ",h,~");
                            // Toast.makeText(this().getApplicationContext(), remote_ID + ",h,~", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }

    }

    private void sendMsg(String message) {
        Intent i = new Intent(WRITE_MSG);
        if (message != null) {
            i.putExtra("DATA", message);
        }
        sendBroadcast(i);
    }

    public void promptEdit(int idnum){
        switchName=(TextView)findViewById(10+idnum);
        saveid=idnum;
        //Toast.makeText(this, String.valueOf(10+saveid), Toast.LENGTH_SHORT).show();
        LayoutInflater li = LayoutInflater.from(ControlActivity.this);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
               ControlActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        switchname=Gp.getString(remote_Name+remote_ID+String.valueOf(10+saveid), null);
      //  Toast.makeText(ControlActivity.this, String.valueOf(10+saveid), Toast.LENGTH_SHORT).show();
        if(switchname!=null){
            userInput.setText(switchname);
        }
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // get user input and set it to result
                                // edit text
                                switchname=userInput.getText().toString();
                                switchName.setText(switchname);
                                SharedPreferences.Editor e = Gp.edit();
                                e.putString(remote_Name+remote_ID+String.valueOf(10+saveid),switchname);
                                e.commit();
                                //Toast.makeText(ControlActivity.this, "saving "+String.valueOf(10+saveid), Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void update(String response) {
        for (int no = 0; no < no_of_control_device; no++) {
            // Toast.makeText(this().getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            if (response.substring(0, 2).contains("a")) {
                controlBut = (ToggleButton) findViewById(0);
                controlBut.setChecked(false);
                response=response.replace("a,","");
            } else if (response.substring(0, 2).contains("A")) {
                controlBut = (ToggleButton) findViewById(0);
                controlBut.setChecked(true);
                response=response.replace("A,","");
            }
            if (response.substring(0, 2).contains("b")) {
                controlBut = (ToggleButton) findViewById(1);
                controlBut.setChecked(false);
                response=response.replace("b,","");
            } else if (response.substring(0,2).contains("B")) {
                controlBut = (ToggleButton) findViewById(1);
                controlBut.setChecked(true);
                response=response.replace("B,","");
            }
            if (response.substring(0,2).contains("c")) {
                controlBut = (ToggleButton) findViewById(2);
                controlBut.setChecked(false);
                response=response.replace("c,","");
            } else if (response.substring(0,2).contains("C")) {
                controlBut = (ToggleButton) findViewById(2);
                controlBut.setChecked(true);
                response=response.replace("C,","");
            }
            if (response.substring(0,2).contains("d")) {
                controlBut = (ToggleButton) findViewById(3);
                controlBut.setChecked(false);
                response=response.replace("d,","");
            } else if (response.substring(0,2).contains("D")) {
                controlBut = (ToggleButton) findViewById(3);
                controlBut.setChecked(true);
                response=response.replace("D,","");
            }
            if (response.substring(0,2).contains("e")) {
                controlBut = (ToggleButton) findViewById(4);
                controlBut.setChecked(false);
                response=response.replace("e,","");
            } else if (response.substring(0,2).contains("E")) {
                controlBut = (ToggleButton) findViewById(4);
                controlBut.setChecked(true);
                response=response.replace("E,","");
            }
            if (response.substring(0,2).contains("f")) {
                controlBut = (ToggleButton) findViewById(5);
                controlBut.setChecked(false);
                response=response.replace("f,","");
            } else if (response.substring(0,2).contains("F")) {
                controlBut = (ToggleButton) findViewById(5);
                controlBut.setChecked(true);
                response=response.replace("F,","");
            }
            if (response.substring(0,2).contains("g")) {
                controlBut = (ToggleButton) findViewById(6);
                controlBut.setChecked(false);
                response=response.replace("g,","");
            } else if (response.substring(0,2).contains("G")) {
                controlBut = (ToggleButton) findViewById(6);
                controlBut.setChecked(true);
                response=response.replace("G,","");
            }
            if (response.substring(0,2).contains("h")) {
                controlBut = (ToggleButton) findViewById(7);
                controlBut.setChecked(false);
                response=response.replace("h,","");
            } else if (response.substring(0,2).contains("H")) {
                controlBut = (ToggleButton) findViewById(7);
                controlBut.setChecked(true);
                response=response.replace("H,","");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id==android.R.id.home){
            Intent mainIntent=new Intent(ControlActivity.this,MainActivity.class);
            startActivity(mainIntent);
            stopService();
            finish();
        }
        if (id == R.id.action_sync) {
            checkConnection();
            String update = savedate.loadUpdate();
            update(update);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopService();
        finish();
    }
}
