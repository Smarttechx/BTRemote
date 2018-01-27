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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.BTRemote.adapter.RemoteAdapter;
import com.softdev.smarttechx.BTRemote.data.SaveData;
import com.softdev.smarttechx.BTRemote.model.Remotedevice;
import com.softdev.smarttechx.BTRemote.services.BluetoothBackgroundService;
import com.softdev.smarttechx.BTRemote.util.ItemDivider;
import com.softdev.smarttechx.BTRemote.views.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final Context context = this;
    public EmptyRecyclerView recyclerView;
    private RemoteAdapter adapter;
    private RelativeLayout image;
    private GridLayoutManager gridLayoutManager;
    SaveData saveData;
    public ArrayList<Remotedevice> remoteList;
    public ArrayList<String> mac_address;
    // public List<Device>device_list;
    int ID;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1;
    private final static String SERVICE_ID = "com.softdev.smarttechx.BTRemote.service.BluetoothBackgroundService";

    private ArrayList<String> checkMac = new ArrayList<String>();
    int control;
    private static final String WRITE_MSG = "com.softdev.smarttechx.BTRemote.services.write_msg";
    private UIUpdateReceiver mUIUpdateReceiver;
    private ScreenReceiver mScreenReceiver;
    private BluetoothAdapter mBluetoothAdapter = null;
    public String currentmac_address, authMsg, nickName, getaddress;
    private static final String STOP_SERVICE = "com.softdev.smarttechx.BTRemote.services.stop_service";
    private static final String CHECK_CONNECT = "com.softdev.smarttechx.BTRemote.services.check_connection";
    String Temp;
    IntentFilter newIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolBar();
        image = (RelativeLayout) findViewById(R.id.empty);
        recyclerView = (EmptyRecyclerView) findViewById(R.id.home_recycler_view);
        // temp=(TextView)findViewById(R.id.showTemp);
        mUIUpdateReceiver = new UIUpdateReceiver();
        mScreenReceiver = new ScreenReceiver();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        } else {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        }
        newIntent = new IntentFilter(Intent.ACTION_SCREEN_ON);
        newIntent.addAction(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                                1);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }

        saveData = new SaveData(context);
        remoteList = saveData.loadData();
        adapter = new RemoteAdapter(context, remoteList);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new ItemDivider(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //showActionBar();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(null);
        TextView mTitle = (TextView) findViewById(R.id.appName);
        mTitle.setText(getString(R.string.dash_name));
        ImageButton mAddBut = (ImageButton) findViewById(R.id.btnAdd);
        mAddBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mac_address = saveData.loadMac_Add();
                if (remoteList.size() > 0) {
                    image.setVisibility(View.GONE);
                } else {
                    ID = 0;
                    remoteList.clear();
                    checkMac.clear();
                }
                if (mac_address == null || mac_address.size() == 0) {
                    Toast.makeText(context, "Search for device", Toast.LENGTH_SHORT).show();
                } else {
                    authMsg = "";
                    currentmac_address = "";
                    nickName = "";
                    checkMac.clear();
                    AddRemote();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.UI_UPDATE_MESSAGE));
        registerReceiver(mUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.AUTH_ID));
        registerReceiver(mUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.CONNECT_STATUS));
        registerReceiver(mUIUpdateReceiver, new IntentFilter(BluetoothBackgroundService.SEND_CONNECT_STATUS));
        registerReceiver(mScreenReceiver, new IntentFilter(newIntent));
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mUIUpdateReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences Gp = getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor e = Gp.edit();
        e.putInt("ID", ID);
        e.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        mac_address = saveData.loadMac_Add();
        SharedPreferences Gp = getSharedPreferences("setting", MODE_PRIVATE);
        ID = Gp.getInt("ID", 0);
        if (remoteList.size() > 0) {
            image.setVisibility(View.GONE);

        } else {
            ID = 0;
            remoteList.clear();
            checkMac.clear();
            mac_address.clear();
        }
    }


    public void AddRemote() {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_edit_text, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText NickNameInput = (EditText) promptsView
                .findViewById(R.id.add_RemoteName);
        final EditText DeviceIDInput = (EditText) promptsView
                .findViewById(R.id.device_id);

        final Spinner Mac_Add = (Spinner) promptsView
                .findViewById(R.id.mac_address);
        // Toast.makeText(context,String.valueOf(mac_address.size()), Toast.LENGTH_SHORT).show();
        final ArrayAdapter<String> spinneradapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, mac_address);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        Mac_Add.setAdapter(spinneradapter);
        // deviceID.append(String.valueOf(Deviceid));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                currentmac_address = String.valueOf(Mac_Add.getSelectedItem().toString().substring(0, 17));
                                authMsg = DeviceIDInput.getText().toString();
                                nickName = NickNameInput.getText().toString();
                                for (Remotedevice device : remoteList) {
                                    checkMac.add(device.getMac_Add());
                                }
                                if (remoteList.size() == 0) {
                                    ID = 0;
                                }
                                if (checkMac.contains(currentmac_address)) {
                                    Toast.makeText(context, "Device Mac address already in use", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (isNumeric(DeviceIDInput.getText().toString().substring(0, 1))) {
                                        control = Integer.valueOf(DeviceIDInput.getText().toString().substring(0, 1));
                                        if (control % 2 == 0 && control <= 8) {
                                            startService(currentmac_address);
                                        } else {
                                            Toast.makeText(context, "Please confirm the Device ID", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "Please confirm the Device ID", Toast.LENGTH_SHORT).show();
                                    }

                                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_searchBT) {
            Intent serverIntent = new Intent(context, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
        finish();
        System.exit(0);

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

    public static boolean isNumeric(String text) {
        for (char c : text.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private void sendMsg(String message) {
        Intent i = new Intent(WRITE_MSG);
        if (message != null) {
            i.putExtra("DATA", message);
        }
        sendBroadcast(i);
    }

    public void checkConnection() {
        Intent i = new Intent(CHECK_CONNECT);
        sendBroadcast(i);
    }

    public void stopService() {
        Intent i = new Intent(STOP_SERVICE);
        sendBroadcast(i);
    }

    public void startService(String address) {
        if (!isMyServiceRunning(getBaseContext())) {
            Intent i = new Intent(MainActivity.this, BluetoothBackgroundService.class);
            i.putExtra(BluetoothBackgroundService.DEVICE_ADDRESS, address);
            startService(i);
        }
    }

    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                stopService();
                mBluetoothAdapter.disable();
                unregisterReceiver(mScreenReceiver);
                finish();
                System.exit(0);
            }
        }
    }

    private class UIUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothBackgroundService.CONNECT_STATUS)) {
                final String message = intent.getStringExtra(BluetoothBackgroundService.CONNECT_UPDATE_MESSAGE_DETAIL);
                if (message.contains("Connected to")) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    sendMsg(authMsg + ",0,~");
                } else if (message.contains("connecting")) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
            if (intent.getAction().equals(BluetoothBackgroundService.AUTH_ID)) {
                String authID = intent.getStringExtra(BluetoothBackgroundService.MSG_DATA);
                if (authID.contains("0")) {
                    Toast.makeText(context, "Device authentication failed", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Device ID do not match Device", Toast.LENGTH_SHORT).show();
                    stopService();
                } else if (authID.contains("1") && !checkMac.contains(getaddress)) {
                    Toast.makeText(context, "Device authentication successful", Toast.LENGTH_SHORT).show();
                    Remotedevice remote = new Remotedevice(String.valueOf(ID), nickName, authMsg, currentmac_address);
                    image.setVisibility(View.GONE);
                    remoteList.add(remote);// recyclerView.setAdapter(adapter);
                    adapter.notifyItemInserted(ID);
                    recyclerView.scrollToPosition(ID);
                    saveData.save(remoteList);
                    ID++;
                    stopService();
                }
            }
        }
    }

}
