/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.everlutions.wifichat;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NsdHelper {

    private ILogger mILogger;
    private NsdManager mNsdManager;
    private Map<String, NsdServiceInfo> mNdsServiceInfoMap;

    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;



    public static final String SERVICE_TYPE = "_http._tcp.";

//    public String mServiceName = "NsdChatEvert";



    public NsdHelper(ILogger logger, Context context) {
        mILogger = logger;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mNdsServiceInfoMap = new HashMap<>();

        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();
    }


    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                mILogger.log("Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                mILogger.log("Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    mILogger.log("Unknown Service Type: " + service.getServiceType());
                }
                else
                {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {

                String key = service.getHost().toString() + service.getServiceName();
                mILogger.log("service lost" + key);
                if(mNdsServiceInfoMap.containsKey(key))
                {
                    mILogger.log("service removed" + key);
                    mNdsServiceInfoMap.remove(key);
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                mILogger.log("Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mILogger.log("Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mILogger.log("Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                mILogger.log("Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mILogger.log("Resolve Succeeded. " + serviceInfo);

                String key = serviceInfo.getHost().toString() + serviceInfo.getServiceName();

                if (mNdsServiceInfoMap.containsKey(key))
                {
                    mILogger.log("Duplicate Service" + key);
                }
                else
                {
                    mNdsServiceInfoMap.put(key, serviceInfo);
                }
            }
        };
    }

    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                //TODO bookkeeping on services being registered
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
            
        };
    }

    public void registerService(int port, String serviceName) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        
    }

    public void startDiscoverServices() {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscoverServices() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getServiceInfo(String service_key)
    {
        return mNdsServiceInfoMap.get(service_key);
    }
    
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public List<String> getServiceKeyList()
    {
        return new ArrayList(mNdsServiceInfoMap.keySet());
    }
}
