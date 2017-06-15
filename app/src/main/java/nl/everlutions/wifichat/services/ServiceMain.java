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
import android.util.Log;
import android.widget.Toast;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import static android.content.ContentValues.TAG;

/**
 * Created by jaapo on 13-6-2017.
 */

public class ServiceMain extends Service {

    private ObservableEmitter<Float> pressureObserver;
    private Observable<Float> pressureObservable;
    //https://stackoverflow.com/questions/14695537/android-update-activity-ui-from-service

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final IBinder mBinder = new LocalBinder();

    public long getSecondsRunning() {
        mServiceHandler.mIsRunning = false;
        return mServiceHandler.mRunningSeconds;
    }


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        private int mRunningSeconds;
        private boolean mIsRunning;

        public ServiceHandler(Looper looper) {
            super(looper);
            Log.e(TAG, "ServiceHandler: called threadID: "+ Thread.currentThread().getName());
        }

        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: called threadID: " + Thread.currentThread().getName());
            mIsRunning = true;
            while(mIsRunning) {
                try {
                    Thread.sleep(1000);
                    Log.e(TAG, "handleMessage: just slept " + Thread.currentThread().getName());
                    mRunningSeconds += 1;


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    @Override
    public Observable<Float> observePressure() {
        if(pressureObservable == null) {
            pressureObservable = Observable.create(emitter -> pressureObserver = emitter);
            pressureObservable = pressureObservable.share();
        }
        return pressureObservable;
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
        Log.e(TAG, "onCreate: serviceMain threadID: "+ Thread.currentThread().getName());
        int hoi = Process.THREAD_PRIORITY_BACKGROUND;
        HandlerThread thread = new HandlerThread("ServiceStartArguments", hoi);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
//        mServiceHandler.handleMessage(new Message());

        mServiceAudioSample = new ServiceAudioSample();
        mServiceAudioCorrelatorPearson = new ServiceAudioCorrelatorPearson();
        mServiceNSDCommunication = new ServiceNSDCommunication(this);
        mServiceNSDDiscovery = new ServiceNSDDiscovery(this, new ServiceNSDDiscovery.Listener() {
            @Override
            public void updateHostItems(NsdServiceInfo hostItem) {
                //TODO
            }
        });
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
