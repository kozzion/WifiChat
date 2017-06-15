package nl.everlutions.wifichat.activities;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.services.ServiceMain;

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;
import static nl.everlutions.wifichat.IConstants.NSD_DEFAULT_HOST_NAME;

public class HostActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    //private ServiceNSDCommunication mServiceNSDCommunication;
   // public static String ACTION_RESP = "Boem";

    ServiceMain mService;
    private boolean mBound;


    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            ServiceMain.LocalBinder binder = (ServiceMain.LocalBinder) service;
            mService = binder.getService();

            Log.e(TAG, "onServiceConnected: ");
            long seconds = mService.getSecondsRunning();
            Toast.makeText(mService, "Host bind to service that runs: "+seconds, Toast.LENGTH_SHORT).show();
            mBound = true;

            // Start hosting
           // mService.
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        String hostName = getIntent().getStringExtra(IKEY_NSD_SERVICE_NAME);

        if (hostName.isEmpty()) {
            hostName = NSD_DEFAULT_HOST_NAME;
        }

        //mServiceNSDCommunication = new ServiceNSDCommunication(this);
        //mServiceNSDCommunication.startServer(hostName);

        Intent intent = new Intent(this, ServiceMain.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }


}


