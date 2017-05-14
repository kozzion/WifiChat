package nl.everlutions.wifichat;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jaapo on 14-5-2017.
 */

public class CommunicationManagerNDS implements IMessageHandler
{
    private final ILogger mILogger;
    private final Handler mHandler;
    private final NsdHelper mNsdHelper;


    public boolean mIsServerRunning;
    public boolean mIsDiscovering;

    ServerSocket mServerSocket;
    Thread mServerThread = null;
    SocketConnetion mServerConnection = null;
    Map<String, SocketConnetion> mClientConnectionMap = null;


    public CommunicationManagerNDS(ILogger iLogger, Handler handler, Context context)
    {
        this.mILogger = iLogger;
        this.mHandler = handler;
        this.mNsdHelper = new NsdHelper(iLogger, context);
        this.mClientConnectionMap = new HashMap<>();
    }

    public void addClientSocket(Socket socket)
    {
        mILogger.log("addClientSocket");
        String key = socket.getInetAddress().toString();
        if(mClientConnectionMap.containsKey(key))
        {
            mILogger.log("Duplciate connetion: " + key);
        }
        mILogger.log("Connected: " + key);
        mClientConnectionMap.put(key, new SocketConnetion(mILogger,socket,this));

        //TODO add thread
    }

    @Override
    public void handleMessage(byte[] messageBytes)
    {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", new String(messageBytes));

        Message message = new Message();
        message.setData(messageBundle);
        mHandler.sendMessage(message);
    }

    public void startServer() {
        //TODO
        mILogger.log("Server started");

        //TODO check and handle if mServerSocket is not null

        // Since discovery will happen via Nsd, we don't need to care which port is
        // used.  Just grab an available one and advertise it via Nsd.
        try {
            mServerSocket = new ServerSocket(0);
            //TODO check and handle if mServerThread is not null
            mServerThread = new Thread(new ServerRunnable());
            mServerThread.start();

            mNsdHelper.registerService(mServerSocket.getLocalPort(), "NDSCHAT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        mILogger.log("Does nothing");
    }

    public void startDiscovering()
    {
        mILogger.log("startDiscovering");
        mNsdHelper.startDiscoverServices();
        mIsDiscovering = true;

    }

    public void stopDiscovering() {
        mILogger.log("stopDiscovering");
        mNsdHelper.stopDiscoverServices();
        mIsDiscovering = false;
    }

    public void connectToService(final String serviceKey)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NsdServiceInfo info = mNsdHelper.getServiceInfo(serviceKey);
                try {
                    //TODO what if mServerConnection is not null
                    mServerConnection = new SocketConnetion(mILogger, new Socket(info.getHost(), info.getPort()), CommunicationManagerNDS.this);
                } catch (IOException e) {
                    mILogger.log("IOE on connect to server" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void sendMessage(String messageString)
    {
        if(mIsServerRunning)
        {
            for(SocketConnetion client_connetion : mClientConnectionMap.values())
            {
                mILogger.log("sendMessage to client");
                client_connetion.queueMessage(messageString);
            }
        }
        else        {
            mILogger.log("sendMessage to server");
            mServerConnection.queueMessage(messageString);
        }
    }

    public void onDestroy() {
        mNsdHelper.tearDown();
        if(mIsServerRunning) {
            mServerConnection.tearDown();
        }
    }

    public List<String> getServiceKeyList()
    {
        return mNsdHelper.getServiceKeyList();
    }

    class ServerRunnable implements Runnable {

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()){
                    mILogger.log("ServerSocket Created, awaiting connection");
                    addClientSocket(mServerSocket.accept());
                }
            } catch (IOException e) {
                mILogger.log("Error creating ServerSocket: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}
