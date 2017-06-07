package nl.everlutions.wifichat.handler;

import java.util.Queue;

/**
 * Created by jaapo on 30-5-2017.
 */

public class ArrayTranscoderShortShort
{
    private short [] mCurrentArray;
    private  Queue<short[]> mTargetQueue;
    private int mOutputSize;
    private int mWriteIndex;

    public ArrayTranscoderShortShort(int outputSize, Queue<short[]> targetQueue)
    {
        mOutputSize = outputSize;
        mTargetQueue = targetQueue;
        mCurrentArray = new short[outputSize];
        mWriteIndex = 0;
    }

    public void transCode(short [] input, int toWriteCount)
    {
        //int toWriteCount = record.read(audioRecordBuffer, 0, audioRecordBuffer.length);
        //Log.e("transCode", "transCode");
        int readIndex = 0;
        while (0 < toWriteCount)
        {
            int spaceRemaining = mOutputSize - mWriteIndex;
            if( mWriteIndex == mOutputSize)
            {
                //Log.e("transCode", "queue all");
                mTargetQueue.offer(mCurrentArray);
                mCurrentArray = new short[mOutputSize];
                mWriteIndex = 0;
            }
            else if(toWriteCount <= spaceRemaining)
            {
                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, toWriteCount);
                mWriteIndex += toWriteCount;

                if( mWriteIndex == mOutputSize)
                {
                    mTargetQueue.offer(mCurrentArray);
                    mCurrentArray = new short[mOutputSize];
                    mWriteIndex = 0;
                }
                return;
            }
            else
            {
                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, spaceRemaining);
                toWriteCount -=  spaceRemaining;
                readIndex +=  spaceRemaining;
                mWriteIndex +=  spaceRemaining;
            }
        }
    }


}
