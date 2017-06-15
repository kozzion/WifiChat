package nl.everlutions.wifichat.handler;

import nl.everlutions.wifichat.services.ICommunicationManager;

/**
 * Created by jaapo on 6-6-2017.
 */

public class MessageHandlerAudioSend implements IMessageHandlerShortArray {


    ICommunicationManager mCommunicationManager;

    public MessageHandlerAudioSend(ICommunicationManager communicationManager)
    {
        mCommunicationManager = communicationManager;
    }

    @Override
    public void handle(short[] input) {
      
    }
}
