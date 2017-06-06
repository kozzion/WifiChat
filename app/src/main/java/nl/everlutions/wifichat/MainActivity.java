package nl.everlutions.wifichat;

import android.Manifest;
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

public class MainActivity extends AppCompatActivity implements ILogger {


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

    public AudioSampleManager mAudioSampleManager;
    public CommunicationManagerNDS mCommunicationManagerNDS;

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
        mAudioSampleManager = new AudioSampleManager(this);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mCommunicationManagerNDS = new CommunicationManagerNDS(this, mUpdateHandler, this);
    }

    @OnClick(R.id.btn_flood)
    public void clickFlood(){
        mCommunicationManagerNDS.floodSocket();
    }

    @OnClick(R.id.host_btn)
    public void clickHost(View v) {
        // Register service

        if (!mCommunicationManagerNDS.mIsServerRunning) {
            mCommunicationManagerNDS.startServer();
            mHostButton.setText("Unhost.");
        } else {
            mCommunicationManagerNDS.stopServer();
            mHostButton.setText("Hosting");
        }
    }

    @OnClick(R.id.discover_btn)
    public void clickDiscover(View v) {
        log("clickDiscover ");

        if (!mCommunicationManagerNDS.mIsDiscovering) {
            mCommunicationManagerNDS.startDiscovering();
            log("Discovering...");
            mDiscoverButton.setText("Dscvrng...");
        } else {
            mCommunicationManagerNDS.stopDiscovering();
            mDiscoverButton.setText("Discover");
            log("StopDiscovering...");
        }
    }

    @OnClick(R.id.connect_btn)
    public void clickConnect(View v) {
        List<String> serviceList = mCommunicationManagerNDS.getServiceKeyList();
        mCommunicationManagerNDS.connectToService(serviceList.get(0));

    }

    @OnClick(R.id.btn_play)
    public void clickPlay(View v) {
        log(String.format("Click: " + mAudioSampleManager.mIsPlaying));
        if (mAudioSampleManager.mIsPlaying) {
            log(String.format("Click: queuePlaySamples back"));
            mAudioSampleManager.playAudioStop();
            mButtonPlay.setText("Stop playing");
        } else {
            log(String.format("Click: stop  start"));
            mAudioSampleManager.playAudioStart();
            mButtonPlay.setText("Start playing");
        }
    }
    @OnClick(R.id.btn_record)
    public void clickRecord(View v) {
        log(String.format("Click: " + mAudioSampleManager.mIsRecording));
        if (mAudioSampleManager.mIsRecording) {
            log(String.format("Click: stop"));
            mAudioSampleManager.recordAudioStop();
            mButtonRecord.setText("Start recording");
        } else {
            log(String.format("Click: start"));
            mAudioSampleManager.recordAudioStart();
            mButtonRecord.setText("Stop recording");
        }
    }

    @OnClick(R.id.send_btn)
    public void clickSend(View v) {
        final String messageString = mChatInputView.getText().toString();
        if (!messageString.isEmpty()) {
            mCommunicationManagerNDS.sendMessage(messageString);
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
        //mCommunicationManagerNDS.stopDiscovering(); TODO
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //mCommunicationManagerNDS.onDestroy();

        super.onDestroy();
    }

    @Override
    public void log(final String s) {
        Log.e("WifiChat", s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addChatLine(s);
            }
        });

    }
}
