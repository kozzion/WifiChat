package nl.everlutions.wifichat.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import nl.everlutions.wifichat.R;
import nl.everlutions.wifichat.services.ServiceNSDCommunication;

import static nl.everlutions.wifichat.IConstants.IKEY_NSD_SERVICE_NAME;
import static nl.everlutions.wifichat.IConstants.NSD_DEFAULT_HOST_NAME;

public class JoinActivity extends AppCompatActivity {

    private ServiceNSDCommunication mServiceNSDCommunication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        setTitle("JOINED");

        String nsdServiceName = getIntent().getStringExtra(IKEY_NSD_SERVICE_NAME);

        if (nsdServiceName.isEmpty()) {
            nsdServiceName = NSD_DEFAULT_HOST_NAME;
        }

        mServiceNSDCommunication = new ServiceNSDCommunication(this);
        mServiceNSDCommunication.connectToService(nsdServiceName);
    }
}
