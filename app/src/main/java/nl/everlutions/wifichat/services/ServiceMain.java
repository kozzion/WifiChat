package nl.everlutions.wifichat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Created by jaapo on 13-6-2017.
 */

public class ServiceMain extends Service {

    //https://stackoverflow.com/questions/14695537/android-update-activity-ui-from-service

    private LocalBroadcastManager mBroadCastManager;
    private BroadcastReceiver mBroadCastReceiver;

    static final public String FILTER_TO_SERVICE = "nl.everlutions.wifichat.services.FILTER_TO_SERVICE";
    static final public String FILTER_SERVICE_DISCOVERY = "nl.everlutions.wifichat.services.FILTER_SERVICE_DISCOVERY";
    static final public String FILTER_TO_UI = "nl.everlutions.wifichat.services.FILTER_TO_UI";

    static final public String ACTIVITY_MESSAGE_RESULT = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_RESULT";

    static final public String ACTIVITY_MESSAGE_TYPE = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE";
    static final public String ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND";
    static final public String ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST";
    static final public String ACTIVITY_MESSAGE_TYPE_CLIENT_JOINED = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE_CLIENT_JOINED";
    static final public String ACTIVITY_MESSAGE_TYPE_SHOW_CHAT = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE_SHOW_CHAT";

    static final public String SERVICE_RESULT = "nl.everlutions.wifichat.services.SERVICE_RESULT";
    static final public String SERVICE_MESSAGE_TYPE = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE";
    static final public String SERVICE_MESSAGE_TYPE_HOST = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_HOST";
    static final public String SERVICE_MESSAGE_TYPE_STOP_HOST = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_STOP_HOST";
    static final public String SERVICE_MESSAGE_TYPE_JOIN = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_JOIN";
    static final public String SERVICE_MESSAGE_TYPE_SEND_REQUEST_CHAT = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_SEND_REQUEST_CHAT";
    static final public String SERVICE_MESSAGE_TYPE_SEND_COMMAND_CHAT = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_SEND_COMMAND_CHAT";


    static final public String SERVICE_MESSAGE_HOST_NAME = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_HOST_NAME";

    ServiceNSDRegister mServiceNSDRegister;
    ServiceAudioSample mServiceAudioSample;
    ServiceAudioCorrelatorPearson mServiceAudioCorrelatorPearson;
    ServiceNSDCommunication mServiceNSDCommunication;
    ServiceNSDDiscovery mServiceNSDDiscovery;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: serviceMain threadID: " + Thread.currentThread().getName());
        int hoi = Process.THREAD_PRIORITY_BACKGROUND;
        HandlerThread thread = new HandlerThread("Service Background Thread", hoi);
        thread.start();
        Log.e(TAG, "onCreate: serviceBACK threadID: " + thread.getName());

        mBroadCastManager = LocalBroadcastManager.getInstance(this);
        mBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleServiceMessage(intent);
            }
        };


        mServiceAudioSample = new ServiceAudioSample();
        mServiceAudioCorrelatorPearson = new ServiceAudioCorrelatorPearson();
        mServiceNSDCommunication = new ServiceNSDCommunication(this);
        mServiceNSDRegister = new ServiceNSDRegister(this);

        mServiceNSDDiscovery = new ServiceNSDDiscovery(this);
        mServiceNSDDiscovery.shouldStartDiscovery();

        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadCastReceiver),
                new IntentFilter(ServiceMain.FILTER_TO_SERVICE)
        );
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        //Message msg = mServiceHandler.obtainMessage();
        //msg.arg1 = startId;
        //mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void handleServiceMessage(Intent intent) {
        String serviceMessageType = intent.getStringExtra(ServiceMain.SERVICE_MESSAGE_TYPE);
        Log.e(TAG, "handleServiceMessage: " + serviceMessageType);
        switch (serviceMessageType) {
            default:
                Log.e(TAG, "service message NOT handled");
        }
    }


}
