package nl.everlutions.wifichat.services;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jaapo on 8-5-2017.
 */

public class SocketConnection {


    private static final int BUFFER_SIZE = 4096;

    private final Socket mSocket;
    private final int mSocketID;
    private final ICommunicationManager mCommunicationManager;
    private final DataInputStream mDataInputStream;
    private final DataOutputStream mDataOutputStream;


    private int QUEUE_CAPACITY = 10;
    private BlockingQueue<ConnectionMessage> mWriteQueue;
    //private InetAddress mAddress;
    //private int mPort;

    private Thread mWriteThread;
    private Thread mReadThread;


    private int mCurrentMessageLength;
    private int mCurrentMessageType;

    private final String TAG = this.getClass().getSimpleName();

    public SocketConnection(Socket socket, int socketID, ICommunicationManager communicationManager) {

        Log.e(TAG, "Creating chatClient");
        if ((socket == null) || (communicationManager == null)) {
            throw new RuntimeException("Error null argument");
        }

        this.mSocket = socket;
        this.mSocketID = socketID;
        this.mCommunicationManager = communicationManager;
        try {
            this.mDataInputStream = new DataInputStream(mSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Stream wrap failed");
        }
        try {
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("OutputStream wrap failed");
        }
        mWriteQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        mWriteThread = new Thread(new WriteRunnable());
        mWriteThread.start();
        mReadThread = new Thread(new ReadRunnable());
        mReadThread.start();

    }

    class ReadRunnable implements Runnable {

        @Override
        public void run() {

            try {

                while (!Thread.currentThread().isInterrupted()) {
                    int mCurrentMessageLength = mDataInputStream.readInt();
                    int mCurrentMessageType = mDataInputStream.readInt();
                    byte[] byteMessage = new byte[mCurrentMessageLength];
                    mDataInputStream.readFully(byteMessage);
                    mCommunicationManager.handle(mSocketID, mCurrentMessageType, byteMessage);
                }
            } catch (IOException e) {
                Log.e(TAG, "Read loop error");
            }
        }
    }


    class WriteRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    writeMessage(mWriteQueue.take());
                } catch (InterruptedException ie) {
                    Log.e(TAG, "Message sending loop interrupted, exiting");
                }
            }
        }
    }

    public void queueMessage(ConnectionMessage connectionMessage) {
        //TODO check that message is of buffersize and padd if not
        mWriteQueue.offer(connectionMessage);
    }

    private void writeMessage(ConnectionMessage message) {
        try {
            mDataOutputStream.writeInt(message.payload.length);
            mDataOutputStream.writeInt(message.type);
            mDataOutputStream.write(message.payload);
            mDataOutputStream.flush();
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown Host");
        } catch (IOException e) {
            Log.e(TAG, "I/O Exception");
        } catch (Exception e) {
            Log.e(TAG, "Error " + e.getMessage());
        }
        Log.e(TAG, "Client sent message: " + message);
    }


    public void tearDown() {
        try {
            mSocket.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Error when closing server socket.");
        }
    }
}