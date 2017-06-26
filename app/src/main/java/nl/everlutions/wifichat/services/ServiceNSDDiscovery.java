package nl.everlutions.wifichat.services;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static nl.everlutions.wifichat.IConstants.NSD_SERVICE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_RESULT;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_SERVICE_DISCOVERY;

public class ServiceNSDDiscovery implements NsdManager.DiscoveryListener {

    private final String TAG = this.getClass().getSimpleName();

    private final NsdManager mNsdManager;
    private final LocalBroadcastManager mBroadCastManager;
    private Map<String, NsdServiceInfo> mNdsServiceInfoMap;


    public ServiceNSDDiscovery(Context context) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mNdsServiceInfoMap = new HashMap<>();
        mBroadCastManager = LocalBroadcastManager.getInstance(context);
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

                        String key = nsdServiceInfo.getServiceName();
                        Log.e(TAG, "onServiceResolved: " + key);
                        if (mNdsServiceInfoMap.containsKey(key)) {
                            Log.e(TAG, "Duplicate Service" + key);
                        }
                        mNdsServiceInfoMap.put(key, nsdServiceInfo);

                        sendDiscoveryResult(nsdServiceInfo, ACTIVITY_MESSAGE_TYPE_DISCOVERY_FOUND);

                    }
                });
            }
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
        String key = nsdServiceInfo.getServiceName();
        Log.e(TAG, "onServiceLost: " + key);
        if (mNdsServiceInfoMap.containsKey(key)) {
            Log.e(TAG, "service removed from map: " + key);
            mNdsServiceInfoMap.remove(key);
        }
        sendDiscoveryResult(nsdServiceInfo, ACTIVITY_MESSAGE_TYPE_DISCOVERY_LOST);

    }

    public NsdServiceInfo getServiceInfo(String service_key) {
        return mNdsServiceInfoMap.get(service_key);
    }

    public void sendDiscoveryResult(NsdServiceInfo infoObject, String messageType) {
        Log.e(TAG, "sendDiscoveryResult: " + infoObject.toString());
        Intent intent = new Intent(FILTER_TO_SERVICE_DISCOVERY);
        intent.putExtra(ACTIVITY_MESSAGE_RESULT, infoObject);
        intent.putExtra(ACTIVITY_MESSAGE_TYPE, messageType);
        mBroadCastManager.sendBroadcast(intent);
    }
}
