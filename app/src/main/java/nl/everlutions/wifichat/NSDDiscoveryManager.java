package nl.everlutions.wifichat;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;

import static nl.everlutions.wifichat.IConstants.NSD_SERVICE_TYPE;

public class NSDDiscoveryManager implements NsdManager.DiscoveryListener {

    private final NsdManager mNsdManager;

    public NSDDiscoveryManager(ILogger iLogger, Handler handler, Context context) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void shouldStartDiscovery() {
        mNsdManager.discoverServices(NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
    }


    @Override
    public void onStartDiscoveryFailed(String s, int i) {
        Log.e("TAG", "onStartDiscoveryFailed: " + s);
    }

    @Override
    public void onStopDiscoveryFailed(String s, int i) {
        Log.e("TAG", "onStopDiscoveryFailed: " + s);
    }

    @Override
    public void onDiscoveryStarted(String s) {
        Log.e("TAG", "onDiscoveryStarted: " + s);
    }

    @Override
    public void onDiscoveryStopped(String s) {
        Log.e("TAG", "onDiscoveryStopped: " + s);
    }

    @Override
    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
        Log.e("TAG", "onServiceFound: " + nsdServiceInfo.toString());
    }

    @Override
    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
        Log.e("TAG", "onServiceLost: " + nsdServiceInfo.toString());
    }

    public interface Listener {

    }
}
