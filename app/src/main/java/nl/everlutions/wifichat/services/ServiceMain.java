package nl.everlutions.wifichat.services;

import android.app.Service;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Created by jaapo on 13-6-2017.
 */

public class ServiceMain extends Service {

    //https://stackoverflow.com/questions/14695537/android-update-activity-ui-from-service

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final IBinder mBinder = new LocalBinder();
    private LocalBroadcastManager mBroadCaster;

    static final public String SERVICE_RESULT = "nl.everlutions.wifichat.services.SERVICE_RESULT";
    static final public String FILTER_DISCOVERY = "nl.everlutions.wifichat.services.FILTER_DISCOVERY";

    static final public String SERVICE_MESSAGE = "nl.everlutions.wifichat.services.SERVICE_MESSAGE";

    public void sendTestSeconds(String message) {
        Intent intent = new Intent(SERVICE_RESULT);
        if (message != null)
            intent.putExtra(SERVICE_MESSAGE, message);
        mBroadCaster.sendBroadcast(intent);
    }

    public void sendDiscoveryResult(NsdServiceInfo infoObject) {
        Log.e(TAG, "sendDiscoveryResult: " + infoObject.toString());
        Intent intent = new Intent(FILTER_DISCOVERY);
        if (infoObject != null)
            intent.putExtra(SERVICE_MESSAGE, infoObject);
        mBroadCaster.sendBroadcast(intent);
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


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: serviceMain threadID: " + Thread.currentThread().getName());
        int hoi = Process.THREAD_PRIORITY_BACKGROUND;
        HandlerThread thread = new HandlerThread("Service Background Thread", hoi);
        thread.start();
        Log.e(TAG, "onCreate: serviceBACK threadID: " + thread.getName());

        mBroadCaster = LocalBroadcastManager.getInstance(this);

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mServiceHandler.sendMessage(new Message());

        mServiceAudioSample = new ServiceAudioSample();
        mServiceAudioCorrelatorPearson = new ServiceAudioCorrelatorPearson();
        mServiceNSDCommunication = new ServiceNSDCommunication(this);
        mServiceNSDDiscovery = new ServiceNSDDiscovery(this, new ServiceNSDDiscovery.Listener() {
            @Override
            public void updateHostItems(NsdServiceInfo hostItem) {
                sendDiscoveryResult(hostItem);
            }
        });
        mServiceNSDDiscovery.shouldStartDiscovery();
        //mServiceNSDRegister = new mServiceNSDRegister(this, this);
    }


    public class LocalBinder extends Binder {
        public ServiceMain getService() {
            return ServiceMain.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
