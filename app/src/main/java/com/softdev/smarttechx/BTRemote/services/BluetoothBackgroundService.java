package com.softdev.smarttechx.BTRemote.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.softdev.smarttechx.BTRemote.MainActivity;
import com.softdev.smarttechx.BTRemote.R;
import com.softdev.smarttechx.BTRemote.model.Remotedevice;
import com.softdev.smarttechx.BTRemote.util.Constants;

import java.lang.ref.WeakReference;

public class BluetoothBackgroundService extends Service {
    private final static int THIS_SERVICE_ID = 111;


    public enum SERVICE_STATE {
        NONE,
        RUNNING,
        FINISHED,
        EXPIRED;

    }

    private ArrayAdapter<String> mConversationArrayAdapter;
    public static SERVICE_STATE mState = SERVICE_STATE.NONE;
    private String mConnectedDeviceName = null;
    String inMsg;

    // Message types sent from the BluetoothService Handler
    public final static String UI_UPDATE_STATE = "com.softdev.smarttechx.BTRemote.services.ui_update.state";
    public final static String CONNECT_STATUS = "com.softdev.smarttechx.BTRemote.services.connect_status";
    public final static String MSG_DATA = "com.softdev.smarttechx.BTRemote.services.message_data";
    public final static String UI_UPDATE_STATE_DETAIL = "com.softdev.smarttechx.BTRemote.services.ui_update.state_detail";
    public final static String UI_UPDATE_MESSAGE = "com.softdev.smarttechx.BTRemote.services.ui_update.message";
    public final static String SEND_CHECK_CONNECT = "com.softdev.smarttechx.BTRemote.services.send_check_connect";
    public final static String CONNECT_UPDATE_MESSAGE_DETAIL = "com.softdev.smarttechx.BTRemote.services.connect_update.message_detail";
    public final static String AUTH_ID = "com.softdev.smarttechx.BTRemote.services.auth_id";
    public final static String SEND_CONNECT_STATUS= "com.softdev.smarttechx.BTRemote.services.check_connection_status";
    public final static String DEVICE_ADDRESS = "com.softdev.smarttechx.BTRemote.services.device_address";
    private static final String WRITE_MSG = "com.softdev.smarttechx.BTRemote.services.write_msg";
    private static final String CHECK_CONNECT= "com.softdev.smarttechx.BTRemote.services.check_connection";
    private static final String STOP_SERVICE = "com.softdev.smarttechx.BTRemote.services.stop_service";

    private MStopReceiver mStopReceiver;
    private BluetoothHandler btHandler;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBtService = null;

    public BluetoothBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BluetoothHandler extends Handler {
        WeakReference<BluetoothBackgroundService> service;

        BluetoothHandler(BluetoothBackgroundService service) {
            this.service = new WeakReference<BluetoothBackgroundService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothBackgroundService bs = service.get();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            mConversationArrayAdapter.clear();
                            if (mState == SERVICE_STATE.RUNNING) {
                               bs.sendUIUpdate(CONNECT_STATUS, "Connected");
                            }
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            bs.sendUIUpdate(CONNECT_STATUS, "connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            bs.sendUIUpdate(CONNECT_STATUS, "not connected");
                            break;
                    }
                    break;

                case Constants.MESSAGE_READ:
                    if (mState == SERVICE_STATE.RUNNING) {
                       /* new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            }
                        }, 1000);*/
                        if (msg.obj != null) {
                            byte[] readBuf = (byte[]) msg.obj;
                            // construct a string from the valid bytes in the buffer
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            inMsg = inMsg + readMessage;
                           //Toast.makeText(BluetoothBackgroundService.this, inMsg, Toast.LENGTH_SHORT).show();
                            if (inMsg.contains("null")) {
                                inMsg = inMsg.replace("null", "");
                            }
                            if (inMsg.contains("\n")) {
                                inMsg = inMsg.replace("\n", "");
                            }
                            if (inMsg.startsWith("1") || inMsg.startsWith("0")) {
                                bs.sendData(AUTH_ID, inMsg);
                                if (inMsg.substring(0, 1).contains("1")) {
                                    inMsg = inMsg.replaceFirst("1", "");
                                } else if (inMsg.substring(0, 1).contains("0")) {
                                    inMsg = inMsg.replaceFirst("0", "");
                                }
                            }
                            String res= "&"+inMsg;
                            if ((res.startsWith("&")&& res.endsWith("~"))) {
                                if(inMsg.contains(" ")){
                                    inMsg=inMsg.replace(" ","");
                                }
                                bs.sendData(UI_UPDATE_MESSAGE, inMsg);
                                //Toast.makeText(BluetoothBackgroundService.this,"Msg: "+ inMsg, Toast.LENGTH_SHORT).show();
                                inMsg="";
                            }
                        }
                    }
                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    bs.sendUIUpdate(CONNECT_STATUS, "Connected to " + mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (msg.getData().getString((Constants.TOAST)) == "Connect Fail") {
                        bs.sendUIUpdate(CONNECT_STATUS, "Connection Fail");
                    }
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setState(SERVICE_STATE.NONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mStopReceiver = new MStopReceiver();
        registerReceiver(mStopReceiver, new IntentFilter(STOP_SERVICE));
        registerReceiver(mStopReceiver, new IntentFilter(CHECK_CONNECT));
        registerReceiver(mStopReceiver, new IntentFilter(WRITE_MSG));
        btHandler = new BluetoothHandler(this);
        if (mBtService == null) {
            mConversationArrayAdapter = new ArrayAdapter<String>(BluetoothBackgroundService.this, R.layout.message);
            mBtService = new BluetoothService(this, btHandler);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String address = null;
        if (mBtService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBtService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBtService.start();
            }
        }
        if (intent != null) {
            address = intent.getStringExtra(DEVICE_ADDRESS);
        }

        if (address == null || address.equals("")) {
            //invalid, cannot continue
            //throw new IllegalArgumentException("no device address provided");
            stop();
            return Service.START_NOT_STICKY;
        }
        if(mBtService.getState()!=BluetoothService.STATE_CONNECTED){
            connectDevice(address);
        }


        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }



    private void connectDevice(String address) {
        mBtService = new BluetoothService(this, btHandler);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBtService.connect(device, true);
        setState(SERVICE_STATE.RUNNING);

    }

    public void sendStateUpdate() {

        Intent i = new Intent(UI_UPDATE_STATE);

        i.putExtra(UI_UPDATE_STATE_DETAIL, mState.ordinal());

        sendBroadcast(i);
    }


    public void sendUIUpdate(String intent_id, String message) {

        Intent i = new Intent(intent_id);

        if (message != null) {
            i.putExtra(CONNECT_UPDATE_MESSAGE_DETAIL, message);
        }
        sendBroadcast(i);
    }

    public void sendData(String intent_id, String data) {

        Intent i = new Intent(intent_id);
        i.putExtra(MSG_DATA, data);
        sendBroadcast(i);
    }
    public void sendConnectStatus(String intent_id, Boolean status){
        Intent i = new Intent(intent_id);
        i.putExtra(SEND_CHECK_CONNECT, status);
        sendBroadcast(i);
    }

    private class MStopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STOP_SERVICE)) {
                stop();
            }
            if (intent.getAction().equals(WRITE_MSG)) {

                final String message = intent.getStringExtra("DATA");
                //Toast.makeText(BluetoothBackgroundService.this, message, Toast.LENGTH_SHORT).show();
                if (message.length() > 0) {
                    byte[] send = message.getBytes();
                    mBtService.write(send);
                }
            }
            if (intent.getAction().equals(CHECK_CONNECT)) {
                if (mBtService.getState() == BluetoothService.STATE_CONNECTED) {
                   sendConnectStatus(SEND_CONNECT_STATUS,true);
                } else {
                    sendConnectStatus(SEND_CONNECT_STATUS,false);
                } if (mBtService.getState() == BluetoothService.STATE_CONNECTED) {
                    sendConnectStatus(SEND_CONNECT_STATUS,true);
                } else {
                    sendConnectStatus(SEND_CONNECT_STATUS,false);
                }
            }
        }
    }

    public void stop() {

        setState(SERVICE_STATE.FINISHED);
        stopForeground(true);
        if (mStopReceiver != null) {
            unregisterReceiver(mStopReceiver);
            mStopReceiver = null;
        }

        //remove notification after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 2000);

        if (mBtService != null) {
            mBtService.stop();
        }
        BluetoothBackgroundService.this.stopSelf();
    }


    private void setState(SERVICE_STATE pState) {
        mState = pState;
        sendStateUpdate();
    }

    public static SERVICE_STATE getState() {

        return mState;

    }


}
