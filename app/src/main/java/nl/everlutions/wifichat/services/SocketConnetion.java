package nl.everlutions.wifichat.services;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import nl.everlutions.wifichat.ILogger;

/**
 * Created by jaapo on 8-5-2017.
 */

public class SocketConnetion {


    private static final int BUFFER_SIZE = 4096;

    private final ILogger mILogger;
    private final Socket mSocket;
    private final int mSocketID;
    private final ICommunicationManager mCommunicationManager;
    private final DataInputStream mDataInputStream;



    private int QUEUE_CAPACITY = 10;
    private BlockingQueue<byte []> mWriteQueue;
    //private InetAddress mAddress;
    //private int mPort;

    private Thread mWriteThread;
    private Thread mReadThread;


    private int mCurrentMessageLength;
    private int mCurrentMessageType ;
    //TODO temp
    Random random;

    public SocketConnetion(ILogger ilogger, Socket socket, int socketID, ICommunicationManager communicationManager) {

        ilogger.log("Creating chatClient");
        if ((ilogger == null) ||(socket == null) || (communicationManager == null)){
            throw new RuntimeException("Error null argument");
        }

        this.mILogger = ilogger;
        this.mSocket = socket;
        this.mSocketID = socketID;
        this.mCommunicationManager = communicationManager;
        try {
            this.mDataInputStream = new DataInputStream(mSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Stream wrap failed");
        }

        mWriteQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        mWriteThread = new Thread(new WriteRunnable());
        mWriteThread.start();
        mReadThread = new Thread(new ReadRunnable());
        mReadThread.start();

        random = new Random();
    }

    public void queueRandom() {
        byte [] byte_message = new byte [BUFFER_SIZE];
        random.nextBytes(byte_message);
        //TODO random chars
        queueMessage(byte_message);
    }


    class ReadRunnable implements Runnable {

        @Override
        public void run() {

            try {

                while (!Thread.currentThread().isInterrupted())
                {
                    int mCurrentMessageLength = mDataInputStream.readInt();
                    int mCurrentMessageType = mDataInputStream.readInt();
                    byte [] byteMessage = new byte [mCurrentMessageLength];
                    mDataInputStream.readFully(byteMessage);
                    mCommunicationManager.handle(mSocketID, mCurrentMessageType, byteMessage);
                }
            } catch (IOException e)
            {
                mILogger.log("Read loop error");
            }
        }
    }


    class WriteRunnable implements Runnable {

        @Override
        public void run() {
            //try {
            //} catch (UnknownHostException e) {
            //    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
            //} catch (IOException e) {
            //    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
            //}

            while (true) {
                try {
                    writeMessage(mWriteQueue.take());

                } catch (InterruptedException ie) {
                    mILogger.log("Message sending loop interrupted, exiting");
                }
            }
        }
    }

    public void queueMessage(byte [] message) {
        //TODO check that message is of buffersize and padd if not
        mWriteQueue.offer(message);
    }

    private void writeMessage(byte [] message)
    {
        try {
            if (mSocket == null) {
                mILogger.log("Socket is null, wtf?");
            } else if (mSocket.getOutputStream() == null) {
                mILogger.log("Socket output stream is null, wtf?");
            }
            OutputStream out = mSocket.getOutputStream();
            out.write(message);
            out.flush();
        } catch (UnknownHostException e) {
            mILogger.log("Unknown Host");
        } catch (IOException e) {
            mILogger.log("I/O Exception");
        } catch (Exception e) {
            mILogger.log("Error " + e.getMessage());
        }
        mILogger.log("Client sent message: " + message);
    }


    public void tearDown() {
        try {
            mSocket.close();
        } catch (IOException ioe) {
            mILogger.log( "Error when closing server socket.");
        }
    }
}