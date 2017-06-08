package nl.everlutions.wifichat.activities;


import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.adapters.HostRecyclerListAdapter;
import nl.everlutions.wifichat.services.ServiceNSDDiscovery;
import nl.everlutions.wifichat.utils.ScreenUtils;

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;

public class StartActivity extends AppCompatActivity implements ServiceNSDDiscovery.Listener {

    private ServiceNSDDiscovery mNsdDiscoveryManager;
    private HostRecyclerListAdapter mHostListAdapter;

    @BindView(R.id.start_input_host_name)
    EditText mHostInputView;
    @BindView(R.id.start_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.start_loader)
    ProgressBar mLoaderView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        ScreenUtils.hideKeyboardOnCreate(this);

        mNsdDiscoveryManager = new ServiceNSDDiscovery(this, this);
        mNsdDiscoveryManager.shouldStartDiscovery();

        mHostListAdapter = new HostRecyclerListAdapter(this, mOnHostItemClickListener);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mHostListAdapter);

    }

    @OnClick(R.id.btn_host)
    public void onHostButtonClick() {
        mNsdDiscoveryManager.shouldStopDiscovery();
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

    @Override
    public void updateHostItems(final NsdServiceInfo hostItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoader(false);
                mHostListAdapter.addHostItem(hostItem);
            }
        });
    }

    private void showLoader(boolean showLoader) {
        mLoaderView.setVisibility(showLoader ? View.VISIBLE : View.INVISIBLE);
    }
}
