package nl.everlutions.wifichat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jaapo on 8-5-2017.
 */

public class SocketConnetion {

    private static final int BUFFER_SIZE = 4096;

    private final ILogger mILogger;
    private final Socket mSocket;
    private final IMessageHandler mMessageHandler;
    private int QUEUE_CAPACITY = 10;
    private BlockingQueue<byte []> mWriteQueue;
    //private InetAddress mAddress;
    //private int mPort;

    private Thread mWriteThread;
    private Thread mReadThread;

    //TODO temp
    Random random;

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
                while (!Thread.currentThread().isInterrupted()) {
                    byte [] readBuffer = new byte [BUFFER_SIZE];
                    int bytesRead = mSocket.getInputStream().read(readBuffer);

                    if (bytesRead != 0)
                    {
                        if(bytesRead == BUFFER_SIZE)
                        {
                            mMessageHandler.handleMessage(readBuffer);
                        }
                        else
                        {
                            byte [] readBufferShort = new byte [bytesRead];
                            System.arraycopy(readBuffer, 0, readBufferShort, 0 , bytesRead);
                            mMessageHandler.handleMessage(readBufferShort);
                        }

                    } else {
                        mILogger.log("Nothing was read");
                        break;
                    }
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