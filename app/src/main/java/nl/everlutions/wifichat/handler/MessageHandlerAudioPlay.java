package nl.everlutions.wifichat.handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nl.everlutions.wifichat.services.ServiceAudioSample;

/**
 * Created by jaapo on 6-6-2017.
 */

public class MessageHandlerAudioPlay implements IMessageHandlerByteArray {


    ServiceAudioSample mServiceAudioSample;

    public MessageHandlerAudioPlay(ServiceAudioSample serviceAudioSample) {
        mServiceAudioSample = serviceAudioSample;
    }

    @Override
    public void handle(byte[] input) {
        if (input.length % 2 != 0) {
            throw new RuntimeException("Not an audio message");
        }

        short[] shorts = new short[input.length / 2];
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
//        mServiceAudioSample.mTranscoderPlay.transCodeStart(shorts, shorts.length);
    }
}
