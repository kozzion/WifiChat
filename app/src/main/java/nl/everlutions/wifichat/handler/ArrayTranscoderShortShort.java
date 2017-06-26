package nl.everlutions.wifichat.handler;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static nl.everlutions.wifichat.utils.Utils.TAG;

/**
 * Created by jaapo on 30-5-2017.
 */

public class ArrayTranscoderShortShort {
    private short[] mCurrentArray;
    private Queue<short[]> mTargetQueue;
    private int mTargetSize;
    private int mWriteIndex;
    private BlockingQueue<short[]> mSourceQueue;
    private boolean mIsRunning;

    public ArrayTranscoderShortShort(BlockingQueue<short[]> sourceQueue, int targetSize, ArrayBlockingQueue<short[]> targetQueue) {
        mSourceQueue = sourceQueue;
        mTargetSize = targetSize;
        mTargetQueue = targetQueue;
        mCurrentArray = new short[targetSize];
        mWriteIndex = 0;
    }

    public void transCodeStop() {
        mIsRunning = false;
    }

    public void transCodeStart() {
        mIsRunning = true;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "tranCodeStart: ");
                Log.e(TAG, "mSourceQueue: " + mSourceQueue.size());
                Log.e(TAG, "mTargetQueue: " + mTargetQueue.size());
                while (mIsRunning) {
                    int readIndex = 0;
                    try {
                        short[] input = mSourceQueue.take();

                        int toWriteCount = input.length;
                        while (0 < toWriteCount) {
                            Log.e(TAG, "toWriteCount: " + toWriteCount);
                            Log.e(TAG, "mTargetQueue: " + mTargetQueue.size());
                            int spaceRemaining = mTargetSize - mWriteIndex;
                            if (mWriteIndex == mTargetSize) {
                                mTargetQueue.offer(mCurrentArray);
                                mCurrentArray = new short[mTargetSize];
                                mWriteIndex = 0;
                            } else if (toWriteCount <= spaceRemaining) {
                                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, toWriteCount);
                                mWriteIndex += toWriteCount;

                                if (mWriteIndex == mTargetSize) {
                                    mTargetQueue.offer(mCurrentArray);
                                    mCurrentArray = new short[mTargetSize];
                                    mWriteIndex = 0;
                                }
                                return;
                            } else {
                                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, spaceRemaining);
                                toWriteCount -= spaceRemaining;
                                readIndex += spaceRemaining;
                                mWriteIndex += spaceRemaining;
                            }
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "tranCodeInterrupted: ");
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


}
