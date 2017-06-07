package nl.everlutions.wifichat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity implements ILogger {

    private NsdHelper mNsdHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        mNsdHelper = new NsdHelper(this, this);
        mNsdHelper.startDiscoverServices();
    }

    @OnClick(R.id.btn_host)
    public void onHostButtonClick() {
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
    }

    @Override
    public void log(String s) {

    }
}
