package nl.everlutions.wifichat;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jaapo on 14-5-2017.
 */

public class CommunicationManagerNDS implements IMessageHandler {
    private final MainActivity mMainActivity;
    private final Handler mHandler;
    private final NsdHelper mNsdHelper;


    public boolean mIsServerRunning;
    public boolean mIsDiscovering;

    ServerSocket mServerSocket;
    Thread mServerThread = null;
    SocketConnetion mServerConnection = null;
    Map<String, SocketConnetion> mClientConnectionMap = null;

    // TODO: temp
    int mBytesReceveidSinceLastUpdate;
    long mTimeOfLastMessage;
    long mTimeOfLastUodate;

    public CommunicationManagerNDS(MainActivity iLogger, Handler handler, Context context) {
        this.mMainActivity = iLogger;
        this.mHandler = handler;
        this.mNsdHelper = new NsdHelper(iLogger, context);
        this.mClientConnectionMap = new HashMap<>();


    }

    public void addClientSocket(Socket socket) {
        mMainActivity.log("addClientSocket");
        String key = socket.getInetAddress().toString();
        if (mClientConnectionMap.containsKey(key)) {
            mMainActivity.log("Duplciate connetion: " + key);
        }
        mMainActivity.log("Connected: " + key);
        mClientConnectionMap.put(key, new SocketConnetion(mMainActivity, socket, this));

        //TODO add thread


    }

    public void floodSocket() {
        //TODO temp
        mMainActivity.log("floodSocket");
        if (mServerConnection == null) {
            mMainActivity.log("This is not a connected client");
            throw new RuntimeException("This is not a connected client");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        mServerConnection.queueRandom();
                    }
                }
            }).start();
        }
    }


    @Override
    public void handleMessage(short[] messageBytes) {
        //todo: NEEDS REFACTOR
    }

    @Override
    public void handleMessage(byte[] messageBytes) {
        long currentTime = System.currentTimeMillis();
        mBytesReceveidSinceLastUpdate += messageBytes.length;

        mMainActivity.mAudioSampleManager.mTranscoderPlayBytes.transCode(messageBytes, messageBytes.length);

        if (1000 < currentTime - mTimeOfLastUodate) {
            double bytesPerSecond = mBytesReceveidSinceLastUpdate / ((currentTime - mTimeOfLastUodate) / 1000.0);

            Bundle messageBundle = new Bundle();
            messageBundle.putString("msg", "bytes per second: " + bytesPerSecond);

            Message message = new Message();
            message.setData(messageBundle);
            mHandler.sendMessage(message);
            mTimeOfLastUodate = currentTime;
            mBytesReceveidSinceLastUpdate = 0;
        }
        //mTimeOfLastMessage = System.currentTimeMillis();
    }

    public void startServer() {
        //TODO
        mMainActivity.log("Server started");

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
        mMainActivity.log("Does nothing");
    }

    public void startDiscovering() {
        mMainActivity.log("startDiscovering");
        mNsdHelper.startDiscoverServices();
        mIsDiscovering = true;

    }

    public void stopDiscovering() {
        mMainActivity.log("stopDiscovering");
        mNsdHelper.stopDiscoverServices();
        mIsDiscovering = false;
    }

    public void connectToService(final String serviceKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NsdServiceInfo info = mNsdHelper.getServiceInfo(serviceKey);
                try {
                    //TODO what if mServerConnection is not null
                    mServerConnection = new SocketConnetion(mMainActivity, new Socket(info.getHost(), info.getPort()), CommunicationManagerNDS.this);
                } catch (IOException e) {
                    mMainActivity.log("IOE on connect to server" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void sendMessage(String messageString) {
        if (mIsServerRunning) {
            for (SocketConnetion client_connetion : mClientConnectionMap.values()) {
                mMainActivity.log("sendMessage to client");
                client_connetion.queueMessage(messageString.getBytes());
            }
        } else {
            mMainActivity.log("sendMessage to server");
            mServerConnection.queueMessage(messageString.getBytes());
        }
    }

    public void onDestroy() {
        mNsdHelper.tearDown();
        if (mIsServerRunning) {
            mServerConnection.tearDown();
        }
    }

    public List<String> getServiceKeyList() {
        return mNsdHelper.getServiceKeyList();
    }

    public void queueRecording(final short[] audioRecordBuffer, final int toWriteCount) {
        //TODO temp
        mMainActivity.log("queueRecording");
        if (mServerConnection == null) {
            mMainActivity.log("This is not a connected client");
            throw new RuntimeException("This is not a connected client");
        } else {

            byte[] bytes = new byte[audioRecordBuffer.length * 2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(audioRecordBuffer);
            mServerConnection.queueMessage(bytes);
        }
    }

    class ServerRunnable implements Runnable {

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    mMainActivity.log("ServerSocket Created, awaiting connection");
                    addClientSocket(mServerSocket.accept());
                }
            } catch (IOException e) {
                mMainActivity.log("Error creating ServerSocket: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}
