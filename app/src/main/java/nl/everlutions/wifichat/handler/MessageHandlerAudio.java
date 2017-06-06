package nl.everlutions.wifichat.handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nl.everlutions.wifichat.AudioSampleManager;

/**
 * Created by jaapo on 6-6-2017.
 */

public class MessageHandlerAudio implements IMessageHandlerByteArray {

    AudioSampleManager mAudioSampleManager;

    public MessageHandlerAudio(AudioSampleManager audioSampleManager)
    {
        mAudioSampleManager = audioSampleManager;
    }

    @Override
    public void handle(byte[] input) {
        if(input.length % 2 != 0)        {
            throw new RuntimeException("Not an audio message");
        }

        short[] shorts = new short[input.length / 2];
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        mAudioSampleManager.mTranscoderPlay.transCode(shorts, shorts.length);
    }
}
