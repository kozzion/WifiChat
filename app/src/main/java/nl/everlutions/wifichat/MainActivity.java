package nl.everlutions.wifichat;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ILogger {


    @BindView(R.id.status)
    TextView mStatusView;
    @BindView(R.id.chatInput)
    EditText mChatInputView;
    @BindView(R.id.discover_btn)
    Button mDiscoverButton;
    @BindView(R.id.register_btn)
    Button mRegisterButton;

    NsdHelper mNsdHelper;

    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;
    private boolean mIsDiscovering;

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

        mConnection = new ChatConnection(mUpdateHandler, this);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

    }

    @OnClick(R.id.register_btn)
    public void clickAdvertise(View v) {
        // Register service
        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            sendLog("ServerSocket isn't bound.");
        }
    }

    @OnClick(R.id.discover_btn)
    public void clickDiscover(View v) {
        sendLog("clickDiscover ");

        if (!mIsDiscovering) {
            mDiscoverButton.setText("Discovering...");
            mNsdHelper.discoverServices();
        } else {
            mDiscoverButton.setText("Discover");
            mNsdHelper.stopDiscovery();
        }
        mIsDiscovering = !mIsDiscovering;


    }

    @OnClick(R.id.connect_btn)
    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            sendLog("Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            sendLog("No service to connect to!");
        }
    }

    @OnClick(R.id.send_btn)
    public void clickSend(View v) {
        final String messageString = mChatInputView.getText().toString();
        if (!messageString.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mConnection.sendMessage(messageString);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mChatInputView.setText("");
                        }
                    });
                }
            }).start();
        }
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }

    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }

    @Override
    public void sendLog(String s) {
        Log.e(TAG, s);
        addChatLine(s);
    }
}
