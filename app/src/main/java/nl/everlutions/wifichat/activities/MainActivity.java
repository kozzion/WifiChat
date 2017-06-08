package nl.everlutions.wifichat.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.services.ServiceAudioSample;
import nl.everlutions.wifichat.services.ServiceNSDCommunication;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.status)
    TextView mStatusView;
    @BindView(R.id.chatInput)
    EditText mChatInputView;

    @BindView(R.id.host_btn)
    Button mHostButton;
    @BindView(R.id.btn_flood)
    Button mFloodButton;
    @BindView(R.id.btn_play)
    Button mButtonPlay;
    @BindView(R.id.btn_record)
    Button mButtonRecord;


    @BindView(R.id.discover_btn)
    Button mDiscoverButton;
    @BindView(R.id.scrollview)
    ScrollView mScrollView;

    private Handler mUpdateHandler;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    public ServiceAudioSample mServiceAudioSample;
    public ServiceNSDCommunication mServiceNSDCommunication;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //New code
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mServiceAudioSample = new ServiceAudioSample();

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mServiceNSDCommunication = new ServiceNSDCommunication(mUpdateHandler, this);
        //TODO crash duplicate handler error on line 64 ServiceNSDCommunication
//        mServiceNSDCommunication.addMessageHandler(0, ServiceNSDCommunication.MessageHandlerTypeAudio, new MessageHandlerAudioPlay(mServiceAudioSample));
//        mServiceNSDCommunication.addMessageHandler(0, ServiceNSDCommunication.MessageHandlerTypeChat, new MessageHandlerChat(this));

    }

    @OnClick(R.id.new_design_btn)
    public void clickNewDesign() {
        startActivity(new Intent(this, StartActivity.class));
    }

    @OnClick(R.id.btn_flood)
    public void clickFlood() {
    }

    @OnClick(R.id.host_btn)
    public void clickHost(View v) {
        // Register service

        if (!mServiceNSDCommunication.mIsServerRunning) {
            mServiceNSDCommunication.startServer();
            mHostButton.setText("Unhost.");
        } else {
            mServiceNSDCommunication.stopServer();
            mHostButton.setText("Hosting");
        }
    }

    @OnClick(R.id.discover_btn)
    public void clickDiscover(View v) {
        Log.e(TAG, "clickDiscover ");

        if (!mServiceNSDCommunication.mIsDiscovering) {
            mServiceNSDCommunication.startDiscovering();
            Log.e(TAG, "Discovering...");
            mDiscoverButton.setText("Dscvrng...");
        } else {
            mServiceNSDCommunication.stopDiscovering();
            mDiscoverButton.setText("Discover");
            Log.e(TAG, "StopDiscovering...");
        }
    }

    @OnClick(R.id.connect_btn)
    public void clickConnect(View v) {
        List<String> serviceList = mServiceNSDCommunication.getServiceKeyList();
        mServiceNSDCommunication.connectToService(serviceList.get(0));

    }

    @OnClick(R.id.btn_play)
    public void clickPlay(View v) {
        Log.e(TAG, String.format("Click: " + mServiceAudioSample.mIsPlaying));
        if (mServiceAudioSample.mIsPlaying) {
            Log.e(TAG, String.format("Click: queuePlaySamples back"));
            mServiceAudioSample.playAudioStop();
            mButtonPlay.setText("Stop playing");
        } else {
            Log.e(TAG, String.format("Click: stop  start"));
            mServiceAudioSample.playAudioStart();
            mButtonPlay.setText("Start playing");
        }
    }

    @OnClick(R.id.btn_record)
    public void clickRecord(View v) {
        Log.e(TAG, String.format("Click: " + mServiceAudioSample.mIsRecording));
        if (mServiceAudioSample.mIsRecording) {
            Log.e(TAG, String.format("Click: stop"));
            mServiceAudioSample.recordAudioStop();
            mButtonRecord.setText("Start recording");
        } else {
            Log.e(TAG, String.format("Click: start"));
            mServiceAudioSample.recordAudioStart();
            mButtonRecord.setText("Stop recording");
        }
    }

    @OnClick(R.id.send_btn)
    public void clickSend(View v) {
        final String messageString = mChatInputView.getText().toString();
        if (!messageString.isEmpty()) {
            mServiceNSDCommunication.sendMessage(messageString);
            mChatInputView.setText("");
        }
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onPause() {
        //mServiceNSDCommunication.stopDiscovering(); TODO
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //mServiceNSDCommunication.onDestroy();

        super.onDestroy();
    }

}
