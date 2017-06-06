package nl.everlutions.wifichat.handler;

import nl.everlutions.wifichat.handler.IMessageHandlerShortArray;

/**
 * Created by jaapo on 30-5-2017.
 */

public class ArrayTranscoderShortShort
{
    private short [] mCurrentArray;
    private IMessageHandlerShortArray mHandler;
    private int mOutputSize;
    private int mWriteIndex;

    public ArrayTranscoderShortShort(int outputSize, IMessageHandlerShortArray handler)
    {
        mOutputSize = outputSize;
        mHandler = handler;
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
                mHandler.handle(mCurrentArray);
                mCurrentArray = new short[mOutputSize];
                mWriteIndex = 0;
            }
            else if(toWriteCount <= spaceRemaining)
            {
                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, toWriteCount);
                mWriteIndex += toWriteCount;

                if( mWriteIndex == mOutputSize)
                {
                    mHandler.handle(mCurrentArray);
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
