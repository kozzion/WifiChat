package nl.everlutions.wifichat.activities;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.services.ServiceMain;

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;
import static nl.everlutions.wifichat.IConstants.NSD_DEFAULT_HOST_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_SERVICE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_HOST_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_HOST;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_STOP_HOST;

public class HostActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    //private ServiceNSDCommunication mServiceNSDCommunication;
    // public static String ACTION_RESP = "Boem";

    ServiceMain mService;
    private boolean mBound;

    private BroadcastReceiver mBroadCastReceiver;
    private LocalBroadcastManager mBroadCastManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        String hostName = getIntent().getStringExtra(IKEY_NSD_SERVICE_NAME);

        if (hostName.isEmpty()) {
            hostName = NSD_DEFAULT_HOST_NAME;
        }

        Intent intent = new Intent(FILTER_TO_SERVICE);
        intent.putExtra(SERVICE_MESSAGE_TYPE, SERVICE_MESSAGE_TYPE_HOST);
        intent.putExtra(SERVICE_MESSAGE_HOST_NAME, hostName);

        mBroadCastManager = LocalBroadcastManager.getInstance(this);
        mBroadCastManager.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        Intent intent = new Intent(FILTER_TO_SERVICE);
        intent.putExtra(SERVICE_MESSAGE_TYPE, SERVICE_MESSAGE_TYPE_STOP_HOST);
        mBroadCastManager.sendBroadcast(intent);
        super.onDestroy();
    }
}


