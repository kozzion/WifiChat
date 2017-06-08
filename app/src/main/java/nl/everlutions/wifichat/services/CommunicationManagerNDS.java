package nl.everlutions.wifichat.services;

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

import nl.everlutions.wifichat.ILogger;
import nl.everlutions.wifichat.handler.IMessageHandlerByteArray;

/**
 * Created by jaapo on 14-5-2017.
 */

public class CommunicationManagerNDS implements ICommunicationManager {
    private final ILogger mLogger;
    private final Handler mHandler;
    private final NsdHelper mNsdHelper;


    public boolean mIsServerRunning;
    public boolean mIsDiscovering;

    public static final int MessageHandlerTypeAudio = 1;
    public static final int MessageHandlerTypeChat = 2;

    private ServerSocket mServerSocket;
    private Thread mServerThread;
    private SocketConnetion mServerConnection;
    private Map<String, SocketConnetion> mClientConnectionMap; //TODO use ids instead of strings a keys??
    private Map<Integer, Map<Integer, IMessageHandlerByteArray>> mHandlerMap;

    // TODO: temp
    int mBytesReceveidSinceLastUpdate;
    long mTimeOfLastMessage;
    long mTimeOfLastUodate;

    public CommunicationManagerNDS(ILogger iLogger, Handler handler, Context context) {
        this.mLogger = iLogger;
        this.mHandler = handler;
        this.mNsdHelper = new NsdHelper(iLogger, context);
        this.mClientConnectionMap = new HashMap<>();
        this.mHandlerMap = new HashMap<>();
    }

    public void addMessageHandler(int socketID, int messageType, IMessageHandlerByteArray messageHandler) {
        if (!mHandlerMap.containsKey(socketID)) {
            mHandlerMap.put(socketID, new HashMap<Integer, IMessageHandlerByteArray>());
        }

        if (!mHandlerMap.get(socketID).containsKey(messageType)) {
            throw new RuntimeException("Duplicate handler");
        } else {
            mHandlerMap.get(socketID).put(messageType, messageHandler);
        }
    }

    public void addClientSocket(Socket socket) {
        mLogger.log("addClientSocket");
        String key = socket.getInetAddress().toString();
        if (mClientConnectionMap.containsKey(key)) {
            mLogger.log("Duplciate connetion: " + key);
        }
        mLogger.log("Connected: " + key);
        mClientConnectionMap.put(key, new SocketConnetion(mLogger, socket, 0, this));

        //TODO add thread
        //TODO add handlers?
    }

    public void floodSocket() {
        //TODO temp
        mLogger.log("floodSocket");
        if (mServerConnection == null) {
            mLogger.log("This is not a connected client");
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
    public void handle(int socketID, int messageType, byte[] byteMessage) {
        mHandlerMap.get(socketID).get(messageType).handle(byteMessage);

        //TODO these just collect stats, make them better at that

        long currentTime = System.currentTimeMillis();


        mBytesReceveidSinceLastUpdate += byteMessage.length;

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

    @Override
    public void handleSendToServer(int messageType, byte[] byteMessage) {
        //TODO handle send to Server message
    }

    @Override
    public void handleSendToClients(int messageType, byte[] byteMessage) {
        //TODO handle send to Clients message
    }


    public void startServer() {
        //TODO
        mLogger.log("Server started");

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
        mLogger.log("Does nothing");
    }

    public void startDiscovering() {
        mLogger.log("startDiscovering");
        mNsdHelper.startDiscoverServices();
        mIsDiscovering = true;

    }

    public void stopDiscovering() {
        mLogger.log("stopDiscovering");
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
                    mServerConnection = new SocketConnetion(mLogger, new Socket(info.getHost(), info.getPort()), 0, CommunicationManagerNDS.this);
                } catch (IOException e) {
                    mLogger.log("IOE on connect to server" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void sendMessage(String messageString) {
        if (mIsServerRunning) {
            for (SocketConnetion client_connetion : mClientConnectionMap.values()) {
                mLogger.log("sendMessage to client");
                client_connetion.queueMessage(messageString.getBytes());
            }
        } else {
            mLogger.log("sendMessage to server");
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
        mLogger.log("queueRecording");
        if (mServerConnection == null) {
            mLogger.log("This is not a connected client");
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
                    mLogger.log("ServerSocket Created, awaiting connection");
                    addClientSocket(mServerSocket.accept());
                }
            } catch (IOException e) {
                mLogger.log("Error creating ServerSocket: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}
