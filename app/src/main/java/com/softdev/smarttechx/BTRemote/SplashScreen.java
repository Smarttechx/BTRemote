package com.softdev.smarttechx.BTRemote;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;


public class SplashScreen extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = SplashScreen.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter = null;
    private final int SPLASH_DISPLAY_LENGTH = 1500;
    private BTReceiver mBTReceiver;
    IntentFilter btIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btIntent =new IntentFilter(mBluetoothAdapter.ACTION_STATE_CHANGED);

        mBTReceiver = new BTReceiver();
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }

    }
    @Override
    public void onStart(){
        super.onStart();
        registerReceiver(mBTReceiver, new IntentFilter(btIntent));
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
            else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent startActivityIntent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(startActivityIntent);
                        SplashScreen.this.finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);
            }
    }
    @Override
    public void onStop(){
        super.onStop();
        unregisterReceiver(mBTReceiver);
    }

    private class BTReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final  int state= intent.getIntExtra(mBluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR);
                if (state==mBluetoothAdapter.STATE_ON){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent startActivityIntent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(startActivityIntent);
                            SplashScreen.this.finish();
                        }
                    }, SPLASH_DISPLAY_LENGTH);
                }

            }
        }
    }
}