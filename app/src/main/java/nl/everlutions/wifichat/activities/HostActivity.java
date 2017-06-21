package nl.everlutions.wifichat.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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

    private BroadcastReceiver mBroadCastReceiver;

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


        mBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(ServiceMain.SERVICE_MESSAGE);
                Toast.makeText(context, "" + s, Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadCastReceiver),
                new IntentFilter(ServiceMain.SERVICE_RESULT)
        );
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastReceiver);
        super.onPause();
    }
}


