package nl.everlutions.wifichat.handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nl.everlutions.wifichat.AudioSampleManager;
import nl.everlutions.wifichat.ICommunicationManager;

/**
 * Created by jaapo on 6-6-2017.
 */

public class MessageHandlerAudioSend implements IMessageHandlerShortArray {


    ICommunicationManager mCommunicationManager;

    public MessageHandlerAudioSend(ICommunicationManager mCommunicationManager)
    {
        mCommunicationManager = mCommunicationManager;
    }

    @Override
    public void handle(short[] input) {
      
    }
}
