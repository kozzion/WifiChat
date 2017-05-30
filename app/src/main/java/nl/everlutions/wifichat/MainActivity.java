package nl.everlutions.wifichat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
    @BindView(R.id.discover_btn)
    Button mDiscoverButton;
    @BindView(R.id.scrollview)
    ScrollView mScrollView;


    CommunicationManagerNDS mCommunicationManagerNDS;

    private Handler mUpdateHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
        Toast.makeText(this, "need to code", Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.btn_record)
    public void clickRecord(View v) {
        Toast.makeText(this, "need to code", Toast.LENGTH_SHORT).show();
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
        mCommunicationManagerNDS.stopDiscovering();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mCommunicationManagerNDS.onDestroy();

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
