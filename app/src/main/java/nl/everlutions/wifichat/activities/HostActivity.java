package nl.everlutions.wifichat.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.services.ServiceMain;

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;
import static nl.everlutions.wifichat.IConstants.NSD_DEFAULT_HOST_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_CLIENT_JOINED;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_SHOW_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_SERVICE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_HOST_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_HOST;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_SEND_COMMAND_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_STOP_HOST;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_RESULT;

public class HostActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.host_output)
    TextView mHostOutputView;
    @BindView(R.id.host_chat_input)
    EditText mHostChatInputView;

    private BroadcastReceiver mBroadCastReceiver;
    private LocalBroadcastManager mBroadCastManager;
    private BroadcastReceiver mHostBroadcastReciever;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        ButterKnife.bind(this);

        String hostName = getIntent().getStringExtra(IKEY_NSD_SERVICE_NAME);

        if (hostName.isEmpty()) {
            hostName = NSD_DEFAULT_HOST_NAME;
        }

        Intent intent = new Intent(FILTER_TO_SERVICE);
        intent.putExtra(SERVICE_MESSAGE_TYPE, SERVICE_MESSAGE_TYPE_HOST);
        intent.putExtra(SERVICE_MESSAGE_HOST_NAME, hostName);

        mBroadCastManager = LocalBroadcastManager.getInstance(this);
        mBroadCastManager.sendBroadcast(intent);

        mHostBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };
    }

    @OnClick(R.id.host_send_btn)
    public void onSendClicked() {
        String input = mHostChatInputView.getText().toString();
        if (!input.isEmpty()) {
            sendMessageToClients(input);
            addToTextLine(input);
            mHostChatInputView.setText("");
        }
    }

    private void addToTextLine(String input) {
        mHostOutputView.append("\n" + input);
    }

    private void handleIntent(Intent intent) {
        String messageType = intent.getStringExtra(ACTIVITY_MESSAGE_TYPE);

        Log.e("TAG", "onReceive: " + messageType);
        switch (messageType) {
            case ACTIVITY_MESSAGE_TYPE_CLIENT_JOINED:
                String inetAddress = intent.getStringExtra(ServiceMain.ACTIVITY_MESSAGE_RESULT);
                addToTextLine("Client joined: " + inetAddress);
                break;
            case ACTIVITY_MESSAGE_TYPE_SHOW_CHAT:
                String chatMessage = intent.getStringExtra(ServiceMain.ACTIVITY_MESSAGE_RESULT);
                sendMessageToClients(chatMessage);
                addToTextLine(chatMessage);
                break;
            default:
                Log.e(TAG, "OnReceive message Unhandled! " + messageType);
        }
    }

    private void sendMessageToClients(String input) {
        Intent intent = new Intent(FILTER_TO_SERVICE);
        intent.putExtra(SERVICE_MESSAGE_TYPE, SERVICE_MESSAGE_TYPE_SEND_COMMAND_CHAT);
        intent.putExtra(SERVICE_RESULT, input);
        mBroadCastManager.sendBroadcast(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mHostBroadcastReciever),
                new IntentFilter(ServiceMain.FILTER_TO_UI)
        );
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHostBroadcastReciever);
        super.onPause();
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


