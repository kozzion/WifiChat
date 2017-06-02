package nl.everlutions.wifichat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        short[] shorts = new short[input.length / 2];
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        mHandler.transCode(shorts, shorts.length);
    }


}
