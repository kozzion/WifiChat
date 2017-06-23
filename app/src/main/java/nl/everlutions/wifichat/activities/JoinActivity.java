package nl.everlutions.wifichat.activities;


import android.content.Intent;
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

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;
import static nl.everlutions.wifichat.IConstants.NSD_DEFAULT_HOST_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_SERVICE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_JOIN;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_RESULT;

public class JoinActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private LocalBroadcastManager mBroadCastManager;

    @BindView(R.id.join_chat_input)
    EditText mJoinChatInputView;
    @BindView(R.id.join_output)
    TextView mJoinOutputView;

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
    }

    @OnClick(R.id.join_send_btn)
    public void onSendClicked() {
        String input = mJoinChatInputView.getText().toString();
        if (!input.isEmpty()) {
            //TODO: send message to Service
            addToTextLine(input);
            mJoinChatInputView.setText("");
        }
    }

    private void addToTextLine(String input) {
        mJoinOutputView.append("\n" + input);
    }
}
