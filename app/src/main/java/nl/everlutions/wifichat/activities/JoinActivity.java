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
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_SHOW_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_SERVICE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_JOIN;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_SEND_REQUEST_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_RESULT;

public class JoinActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private LocalBroadcastManager mBroadCastManager;

    @BindView(R.id.join_chat_input)
    EditText mJoinChatInputView;
    @BindView(R.id.join_output)
    TextView mJoinOutputView;
    private BroadcastReceiver mBroadcastReciever;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);
        setTitle("JOINED");

        String nsdServiceName = getIntent().getStringExtra(IKEY_NSD_SERVICE_NAME);

        if (nsdServiceName.isEmpty()) {
            nsdServiceName = NSD_DEFAULT_HOST_NAME;
        }

        Intent intent = new Intent(FILTER_TO_SERVICE);
        intent.putExtra(SERVICE_MESSAGE_TYPE, SERVICE_MESSAGE_TYPE_JOIN);
        intent.putExtra(SERVICE_RESULT, nsdServiceName);

        Log.e(TAG, "onCreate: trying to join service: " + nsdServiceName);

        mBroadCastManager = LocalBroadcastManager.getInstance(this);
        mBroadCastManager.sendBroadcast(intent);
        mBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };
    }

    private void handleIntent(Intent intent) {
        String messageType = intent.getStringExtra(ACTIVITY_MESSAGE_TYPE);

        Log.e("TAG", "onReceive: " + messageType);
        switch (messageType) {
            case ACTIVITY_MESSAGE_TYPE_SHOW_CHAT:
                String chatMessage = intent.getStringExtra(ServiceMain.ACTIVITY_MESSAGE_RESULT);
                addToTextLine(chatMessage);
                break;
            default:
                Log.e(TAG, "OnReceive message Unhandled! " + messageType);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReciever),
                new IntentFilter(ServiceMain.FILTER_TO_UI)
        );
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReciever);
        super.onPause();
    }

    @OnClick(R.id.join_send_btn)
    public void onSendClicked() {
        String input = mJoinChatInputView.getText().toString();
        if (!input.isEmpty()) {
            sendMessageToService(input);
            addToTextLine(input);
            mJoinChatInputView.setText("");
        }
    }

    private void sendMessageToService(String input) {
        Intent intent = new Intent(FILTER_TO_SERVICE);
        intent.putExtra(SERVICE_MESSAGE_TYPE, SERVICE_MESSAGE_TYPE_SEND_REQUEST_CHAT);
        intent.putExtra(SERVICE_RESULT, input);
        mBroadCastManager.sendBroadcast(intent);
    }

    private void addToTextLine(String input) {
        mJoinOutputView.append("\n" + input);
    }
}
