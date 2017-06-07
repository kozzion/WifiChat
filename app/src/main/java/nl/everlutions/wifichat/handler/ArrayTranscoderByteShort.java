package nl.everlutions.wifichat.handler;

/**
 * Created by jaapo on 30-5-2017.
 */

public class ArrayTranscoderByteShort
{
    private short [] mCurrentArray;
    private ArrayTranscoderShortShort mHandler;
    private int mOutputSize;
    private int mWriteIndex;

    public ArrayTranscoderByteShort(int outputSize, ArrayTranscoderShortShort transcoder)
    {
        mOutputSize = outputSize;
        mHandler = transcoder;
        mCurrentArray = new short[outputSize];
        mWriteIndex = 0;
    }

    public void transCode(byte [] input, int toWriteCount)
    {

    }


}
