package nl.everlutions.wifichat.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdServiceInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import nl.everlutions.wifichat.handler.IMessageHandlerByteArray;
import nl.everlutions.wifichat.handler.MessageHandlerAudioPlay;
import nl.everlutions.wifichat.handler.MessageHandlerCommandChat;
import nl.everlutions.wifichat.handler.MessageHandlerRequestChat;

import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_RESULT;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_CLIENT_JOINED;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_UI;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_HOST_NAME;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_HOST;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_JOIN;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_SEND_COMMAND_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_SEND_REQUEST_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_STOP_HOST;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_RESULT;

/**
 * Created by jaapo on 14-5-2017.
 */

public class ServiceNSDCommunication implements ICommunicationManager {
    private final LocalBroadcastManager mBroadCastManager;
    private final ServiceMain mServiceMain;

    private ServerSocket mServerListenSocket;
    private Thread mServerListenSocketThread;
    private SocketConnection mServerConnection;
    private Map<String, SocketConnection> mClientConnectionMap; //TODO use ids instead of strings a keys??
    private Map<Integer, Map<Integer, IMessageHandlerByteArray>> mHandlerMap;

    private final String TAG = this.getClass().getSimpleName();
    private final BroadcastReceiver mBroadCastReceiver;
    private int mSocketID;

    public ServiceNSDCommunication(ServiceMain serviceMain) {
        mServiceMain = serviceMain;
        mSocketID = 1;
        mClientConnectionMap = new HashMap<>();
        mHandlerMap = new HashMap<>();
        mBroadCastManager = LocalBroadcastManager.getInstance(serviceMain);

        mBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleServiceMessage(intent);
            }
        };

        LocalBroadcastManager.getInstance(serviceMain).registerReceiver((mBroadCastReceiver),
                new IntentFilter(ServiceMain.FILTER_TO_SERVICE_NSD_COMMUNICATION)
        );
    }

    private void handleServiceMessage(Intent intent) {
        String serviceMessageType = intent.getStringExtra(ServiceMain.SERVICE_MESSAGE_TYPE);
        Log.e(TAG, "handleServiceMessage: " + serviceMessageType);
        switch (serviceMessageType) {
            case SERVICE_MESSAGE_TYPE_HOST:
                String hostName = intent.getStringExtra(SERVICE_MESSAGE_HOST_NAME);
                startServer(hostName);
                break;
            case SERVICE_MESSAGE_TYPE_STOP_HOST:
                stopServer();
                break;
            case SERVICE_MESSAGE_TYPE_JOIN:
                String serviceKey = intent.getStringExtra(SERVICE_RESULT);
                connectToService(serviceKey);
                break;
            case SERVICE_MESSAGE_TYPE_SEND_REQUEST_CHAT:
                String chatInput = intent.getStringExtra(SERVICE_RESULT);
                handleSendToServer(MessageHandlerRequestChat.createConnectionMessageFromChatMessage(chatInput));
                break;
            case SERVICE_MESSAGE_TYPE_SEND_COMMAND_CHAT:
                String chatCommandInput = intent.getStringExtra(SERVICE_RESULT);
                handleSendToClients(MessageHandlerCommandChat.createConnectionMessageFromChatMessage(chatCommandInput));
                break;
            default:
                Log.e(TAG, "service message NOT handled");
        }
    }


    public void addMessageHandler(int socketID, int messageType, IMessageHandlerByteArray messageHandler) {
        if (!mHandlerMap.containsKey(socketID)) {
            mHandlerMap.put(socketID, new HashMap<Integer, IMessageHandlerByteArray>());
        }

        if (mHandlerMap.get(socketID).containsKey(messageType)) {
            throw new RuntimeException("Duplicate handler");
        } else {
            mHandlerMap.get(socketID).put(messageType, messageHandler);
        }
    }

    public void addClientSocket(Socket socket) {
        Log.e(TAG, "addClientSocket");
        String key = socket.getInetAddress().toString();

        if (mClientConnectionMap.containsKey(key)) {
            Log.e(TAG, "Duplciate connetion: " + key);
        }

        Log.e(TAG, "Connected: " + key);
        mClientConnectionMap.put(key, new SocketConnection(socket, mSocketID, this));
        broadcastClientJoined(key);

        addMessageHandler(mSocketID, ConnectionMessage.TYPE_REQUEST_CHAT, new MessageHandlerRequestChat(mServiceMain));
        addMessageHandler(mSocketID, ConnectionMessage.TYPE_REQUEST_AUDIO, new MessageHandlerAudioPlay(mServiceMain.mServiceAudioSample));
        //TODO add thread
        //TODO add handlers?
        mSocketID++;
    }

    private void broadcastClientJoined(String clientInetAddress) {
        Log.e(TAG, "broadcastClientJoined");
        Intent intent = new Intent(FILTER_TO_UI);
        intent.putExtra(ACTIVITY_MESSAGE_RESULT, clientInetAddress);
        intent.putExtra(ACTIVITY_MESSAGE_TYPE, ACTIVITY_MESSAGE_TYPE_CLIENT_JOINED);
        mBroadCastManager.sendBroadcast(intent);
    }


    @Override
    public void handle(int socketID, int messageType, byte[] byteMessage) {
        mHandlerMap.get(socketID).get(messageType).handle(byteMessage);
    }

    @Override
    public void handleSendToServer(ConnectionMessage connectionMessage) {
        mServerConnection.queueMessage(connectionMessage);
    }

    @Override
    public void handleSendToClients(ConnectionMessage connectionMessage) {
        for (String key : mClientConnectionMap.keySet()) {
            mClientConnectionMap.get(key).queueMessage(connectionMessage);
        }
    }

    public void startServer(String serviceName) {
        //TODO
        Log.e(TAG, "Server started");

        //TODO check and handle if mServerListenSocket is not null

        // Since discovery will happen via Nsd, we don't need to care which port is
        // used.  Just grab an available one and advertise it via Nsd.
        try {
            mServerListenSocket = new ServerSocket(0);
            //TODO check and handle if mServerListenSocketThread is not null
            mServerListenSocketThread = new Thread(new ServerRunnable());
            mServerListenSocketThread.start();

            mServiceMain.mServiceNSDRegister.registerService(mServerListenSocket.getLocalPort(), serviceName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        Log.e(TAG, "stopServer");
        mServiceMain.mServiceNSDRegister.stopOrUnregisterServer();
        mServerListenSocketThread.interrupt();
    }

    public void connectToService(final String serviceKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NsdServiceInfo info = mServiceMain.mServiceNSDDiscovery.getServiceInfo(serviceKey);
                try {
                    //TODO what if mServerConnection is not null
                    mServerConnection = new SocketConnection(new Socket(info.getHost(), info.getPort()), 0, ServiceNSDCommunication.this);
                    addMessageHandler(0, ConnectionMessage.TYPE_COMMAND_CHAT, new MessageHandlerCommandChat(mServiceMain));
                } catch (IOException e) {
                    Log.e(TAG, "IOE on connect to server" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

    }

//    public void sendMessage(String messageString) {
//        if (mIsServerRunning) {
//            for (SocketConnection client_connetion : mClientConnectionMap.values()) {
//                Log.e(TAG, "sendMessage to client");
//                client_connetion.queueMessage(messageString.getBytes());
//            }
//        } else {
//            Log.e(TAG, "sendMessage to server");
//            mServerConnection.queueMessage(messageString.getBytes());
//        }
//    }

//    public void onDestroy() {
//        mServiceMain.mServiceNSDRegister.stopOrUnregisterServer();
//        if (mIsServerRunning) {
//            mServerConnection.tearDown();
//        }
//    }

//    public void queueRecording(final short[] audioRecordBuffer, final int toWriteCount) {
//        Log.e(TAG, "queueRecording");
//        if (mServerConnection == null) {
//            Log.e(TAG, "This is not a connected client");
//            throw new RuntimeException("This is not a connected client");
//        } else {
//
//            byte[] bytes = new byte[audioRecordBuffer.length * 2];
//            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(audioRecordBuffer);
//            mServerConnection.queueMessage(bytes);
//        }
//    }


    class ServerRunnable implements Runnable {

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Log.e(TAG, "ServerSocket Created, awaiting connection");
                    addClientSocket(mServerListenSocket.accept());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}
