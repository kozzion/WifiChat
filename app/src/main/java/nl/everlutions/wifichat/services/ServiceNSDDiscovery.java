package nl.everlutions.wifichat.services;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static nl.everlutions.wifichat.IConstants.NSD_SERVICE_TYPE;

public class ServiceNSDDiscovery implements NsdManager.DiscoveryListener, NsdManager.ResolveListener {

    private final String TAG = this.getClass().getSimpleName();

    private final NsdManager mNsdManager;
    private final Listener mListener;
    private Map<String, NsdServiceInfo> mNdsServiceInfoMap;


    public ServiceNSDDiscovery(Context context, Listener listener) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mListener = listener;
        mNdsServiceInfoMap = new HashMap<>();
    }

    public void shouldStartDiscovery() {
        mNsdManager.discoverServices(NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
    }

    public void shouldStopDiscovery() {
        mNsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onStartDiscoveryFailed(String s, int i) {
        Log.e(TAG, "onStartDiscoveryFailed: " + s);
    }

    @Override
    public void onStopDiscoveryFailed(String s, int i) {
        Log.e(TAG, "onStopDiscoveryFailed: " + s);
    }

    @Override
    public void onDiscoveryStarted(String s) {
        Log.e(TAG, "onDiscoveryStarted: " + s);
    }

    @Override
    public void onDiscoveryStopped(String s) {
        Log.e(TAG, "onDiscoveryStopped: " + s);
    }

    @Override
    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
        if (nsdServiceInfo != null) {
            Log.e(TAG, "onServiceFound: " + nsdServiceInfo.toString());
            if (!nsdServiceInfo.getServiceType().equals(ServiceNSDRegister.SERVICE_TYPE)) {
                Log.e(TAG, "unknown Service Type: " + nsdServiceInfo.getServiceType());
            } else {
                Log.e(TAG, "resolvingService()");
                mNsdManager.resolveService(nsdServiceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                        Log.e(TAG, "onResolveFailed: " + nsdServiceInfo.toString());
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                        Log.e(TAG, "onServiceResolved: " + nsdServiceInfo.toString());
                        mListener.updateHostItems(nsdServiceInfo);
                    }
                });
            }
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
        String key = nsdServiceInfo.getHost().toString() + nsdServiceInfo.getServiceName();
        Log.e(TAG, "onServiceLost: " + key);
        if (mNdsServiceInfoMap.containsKey(key)) {
            Log.e(TAG, "service removed" + key);
            mNdsServiceInfoMap.remove(key);
        }

        mListener.hostItemLost(nsdServiceInfo);
    }

    @Override
    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.e(TAG, "onResolveFailed: " + nsdServiceInfo.toString());
    }

    @Override
    public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
        String key = nsdServiceInfo.getHost().toString() + nsdServiceInfo.getServiceName();
        Log.e(TAG, "onServiceResolved: " + key);
        if (mNdsServiceInfoMap.containsKey(key)) {
            Log.e(TAG, "Duplicate Service" + key);
        } else {
            mNdsServiceInfoMap.put(key, nsdServiceInfo);
        }
    }

    public NsdServiceInfo getServiceInfo(String service_key) {
        return mNdsServiceInfoMap.get(service_key);
    }


    public interface Listener {
        void updateHostItems(NsdServiceInfo hostItem);

        void hostItemLost(NsdServiceInfo hostItem);
    }
}
