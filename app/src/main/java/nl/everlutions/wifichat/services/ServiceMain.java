package nl.everlutions.wifichat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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

    private LocalBroadcastManager mBroadCasterManager;
    private BroadcastReceiver mBroadCastReceiver;


    static final public String FILTER_TO_SERVICE = "nl.everlutions.wifichat.services.FILTER_TO_SERVICE";
    static final public String SERVICE_RESULT = "nl.everlutions.wifichat.services.SERVICE_RESULT";
    static final public String FILTER_DISCOVERY = "nl.everlutions.wifichat.services.FILTER_DISCOVERY";

    static final public String ACTIVITY_MESSAGE_RESULT = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_RESULT";

    static final public String ACTIVITY_MESSAGE_TYPE = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE";
    static final public String ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND";
    static final public String ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST = "nl.everlutions.wifichat.services.ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST";

    static final public String SERVICE_MESSAGE_TYPE = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE";
    static final public String SERVICE_MESSAGE_TYPE_HOST = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_HOST";
    static final public String SERVICE_MESSAGE_TYPE_STOP_HOST = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_TYPE_STOP_HOST";

    static final public String SERVICE_MESSAGE_HOST_NAME = "nl.everlutions.wifichat.services.SERVICE_MESSAGE_HOST_NAME";

    public void sendTestSeconds(String message) {
        Intent intent = new Intent(SERVICE_RESULT);
        if (message != null)
            intent.putExtra(ACTIVITY_MESSAGE_RESULT, message);
        mBroadCasterManager.sendBroadcast(intent);
    }

    public void sendDiscoveryResult(NsdServiceInfo infoObject, String messageType) {
        Log.e(TAG, "sendDiscoveryResult: " + infoObject.toString());
        Intent intent = new Intent(FILTER_DISCOVERY);
        intent.putExtra(ACTIVITY_MESSAGE_RESULT, infoObject);
        intent.putExtra(ACTIVITY_MESSAGE_TYPE, messageType);
        mBroadCasterManager.sendBroadcast(intent);
    }


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        private int mRunningSeconds;
        private boolean mIsRunning;

        public ServiceHandler(Looper looper) {
            super(looper);
            Log.e(TAG, "ServiceHandler: called threadID: " + Thread.currentThread().getName());
        }

        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: called threadID: " + Thread.currentThread().getName());
        }
    }


    ServiceAudioSample mServiceAudioSample;
    ServiceAudioCorrelatorPearson mServiceAudioCorrelatorPearson;
    ServiceNSDCommunication mServiceNSDCommunication;
    ServiceNSDDiscovery mServiceNSDDiscovery;
    //ServiceNSDRegister mServiceNSDRegister;

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


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: serviceMain threadID: " + Thread.currentThread().getName());
        int hoi = Process.THREAD_PRIORITY_BACKGROUND;
        HandlerThread thread = new HandlerThread("Service Background Thread", hoi);
        thread.start();
        Log.e(TAG, "onCreate: serviceBACK threadID: " + thread.getName());

        mBroadCasterManager = LocalBroadcastManager.getInstance(this);
        mBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleServiceMessage(intent);
            }
        };

        mServiceAudioSample = new ServiceAudioSample();
        mServiceAudioCorrelatorPearson = new ServiceAudioCorrelatorPearson();
        mServiceNSDCommunication = new ServiceNSDCommunication(this);

        mServiceNSDDiscovery = new ServiceNSDDiscovery(this, new ServiceNSDDiscovery.Listener() {
            @Override
            public void updateHostItems(NsdServiceInfo hostItem) {
                sendDiscoveryResult(hostItem, ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND);
            }

            @Override
            public void hostItemLost(NsdServiceInfo hostItem) {
                sendDiscoveryResult(hostItem, ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST);
            }
        });
        mServiceNSDDiscovery.shouldStartDiscovery();

        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadCastReceiver),
                new IntentFilter(ServiceMain.FILTER_TO_SERVICE)
        );
    }

    private void handleServiceMessage(Intent intent) {
        String serviceMessageType = intent.getStringExtra(ServiceMain.SERVICE_MESSAGE_TYPE);
        Log.e(TAG, "handleServiceMessage: " + serviceMessageType);
        switch (serviceMessageType) {
            case SERVICE_MESSAGE_TYPE_HOST:
                String hostName = intent.getStringExtra(SERVICE_MESSAGE_HOST_NAME);
                mServiceNSDCommunication.startServer(hostName);
                break;
            case SERVICE_MESSAGE_TYPE_STOP_HOST:
                mServiceNSDCommunication.stopServer();
                break;
            default:
                Log.e(TAG, "service message NOT handled");
        }
    }


}
