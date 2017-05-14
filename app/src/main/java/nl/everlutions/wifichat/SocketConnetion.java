package nl.everlutions.wifichat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jaapo on 8-5-2017.
 */

public class SocketConnetion {

    private final ILogger mILogger;
    private final Socket mSocket;
    private final IMessageHandler mMessageHandler;
    private int QUEUE_CAPACITY = 10;
    private BlockingQueue<String> mWriteQueue;
    //private InetAddress mAddress;
    //private int mPort;

    private Thread mWriteThread;
    private Thread mReadThread;

    public SocketConnetion(ILogger ilogger, Socket socket, IMessageHandler messageHandler) {

        ilogger.log("Creating chatClient");
        if ((ilogger == null) ||(socket == null) || (messageHandler == null)){
            throw new RuntimeException("Error null argument");
        }

        this.mILogger = ilogger;
        this.mSocket = socket;
        this.mMessageHandler = messageHandler;

        //this.mAddress = socket.getInetAddress();
        //this.mPort = socket.getPort();
        mWriteQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        mWriteThread = new Thread(new WriteRunnable());
        mWriteThread.start();
        mReadThread = new Thread(new ReadRunnable());
        mReadThread.start();
    }



    class ReadRunnable implements Runnable {

        private BufferedReader input;

        @Override
        public void run() {

            try {
                input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {

                    String messageStr = null;
                    messageStr = input.readLine();
                    if (messageStr != null) {
                        mILogger.log("Read from the stream: " + messageStr);
                        mMessageHandler.handleMessage(messageStr.getBytes());
                    } else {
                        mILogger.log("messageStr is null");
                        break;
                    }
                }
                input.close();

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
                    String msg = mWriteQueue.take();
                    writeMessage(msg);
                } catch (InterruptedException ie) {
                    mILogger.log("Message sending loop interrupted, exiting");
                }
            }
        }
    }

    public void queueMessage(String message) {
        mWriteQueue.offer(message);
    }

    private void writeMessage(String message)
    {
        try {
            if (mSocket == null) {
                mILogger.log("Socket is null, wtf?");
            } else if (mSocket.getOutputStream() == null) {
                mILogger.log("Socket output stream is null, wtf?");
            }

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(mSocket.getOutputStream())), true);
            out.println(message);
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