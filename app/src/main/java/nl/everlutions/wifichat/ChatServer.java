package nl.everlutions.wifichat;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import static android.content.ContentValues.TAG;

/**
 * Created by jaapo on 8-5-2017.
 */

public class ChatServer {
    private final ILogger mILogger;
    ServerSocket mServerSocket = null;
    Thread mThread = null;

    public ChatServer(Handler handler, ILogger logger) {
        mThread = new Thread(new ServerThread());
        mThread.start();
        mILogger = logger;
    }

    public void tearDown() {
        mThread.interrupt();
        try {
            mServerSocket.close();
        } catch (IOException ioe) {
            mILogger.sendLog("Error when closing server socket.");
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {

            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                mServerSocket = new ServerSocket(0);
                mServerSocket.getLocalPort());

                while (!Thread.currentThread().isInterrupted()) {
                    mILogger.sendLog("ServerSocket Created, awaiting connection");
                    setSocket(mServerSocket.accept());
                    mILogger.sendLog("Connected.");
                    if (mChatClient == null) {
                        int port = mSocket.getPort();
                        InetAddress address = mSocket.getInetAddress();
                        connectToServer(address, port);
                    }
                }
            } catch (IOException e) {
                mILogger.sendLog("Error creating ServerSocket: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}