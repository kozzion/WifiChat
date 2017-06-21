package nl.everlutions.wifichat.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.adapters.HostRecyclerListAdapter;
import nl.everlutions.wifichat.services.ServiceMain;
import nl.everlutions.wifichat.services.ServiceNSDDiscovery;
import nl.everlutions.wifichat.utils.ScreenUtils;

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST;

public class StartActivity extends AppCompatActivity {

    private ServiceNSDDiscovery mNsdDiscoveryManager;
    private HostRecyclerListAdapter mHostListAdapter;

    @BindView(R.id.start_input_host_name)
    EditText mHostInputView;
    @BindView(R.id.start_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.start_loader)
    ProgressBar mLoaderView;
    private BroadcastReceiver mDiscoveryReciever;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        ScreenUtils.hideKeyboardOnCreate(this);

        //Start intent service
        Intent msgIntent = new Intent(this, ServiceMain.class);
        startService(msgIntent);


        mDiscoveryReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NsdServiceInfo info = intent.getParcelableExtra(ServiceMain.ACTIVITY_MESSAGE_RESULT);
                String messageType = intent.getStringExtra(ACTIVITY_MESSAGE_TYPE);
                Log.e("TAG", "onReceive: " + messageType);
                if (messageType.equalsIgnoreCase(ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND)) {
                    updateHostItems(info);
                } else if (messageType.equalsIgnoreCase(ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST)) {
                    hostItemLost(info);
                }
            }
        };

        mHostListAdapter = new HostRecyclerListAdapter(this, mOnHostItemClickListener);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mHostListAdapter);
    }

    @OnClick(R.id.btn_host)
    public void onHostButtonClick() {
//        mNsdDiscoveryManager.shouldStopDiscovery();
        Intent intent = new Intent(this, HostActivity.class);
        intent.putExtra(IKEY_NSD_SERVICE_NAME, mHostInputView.getText().toString());
        startActivity(intent);
    }

    private View.OnClickListener mOnHostItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int position = mRecyclerView.getChildAdapterPosition(view);
            NsdServiceInfo hostItem = mHostListAdapter.getHostItem(position);
            String serviceName = hostItem.getServiceName();

            Intent intent = new Intent(StartActivity.this, JoinActivity.class);
            intent.putExtra(IKEY_NSD_SERVICE_NAME, serviceName);
            startActivity(intent);
        }
    };

    public void updateHostItems(final NsdServiceInfo hostItem) {
        showLoader(false);
        mHostListAdapter.addHostItem(hostItem);
    }

    public void hostItemLost(final NsdServiceInfo nsdServiceInfo) {
        mHostListAdapter.removeHostItem(nsdServiceInfo);
        showLoader(mHostListAdapter.getItemCount() == 0);
    }

    private void showLoader(boolean showLoader) {
        mLoaderView.setVisibility(showLoader ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mDiscoveryReciever),
                new IntentFilter(ServiceMain.FILTER_DISCOVERY)
        );
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDiscoveryReciever);
        super.onPause();
    }
}
