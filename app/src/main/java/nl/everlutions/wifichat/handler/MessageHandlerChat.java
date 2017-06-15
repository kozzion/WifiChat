package nl.everlutions.wifichat.handler;

import nl.everlutions.wifichat.activities.MainActivity;

/**
 * Created by jaapo on 6-6-2017.
 */

public class MessageHandlerChat implements IMessageHandlerByteArray
{
    MainActivity mMainActivity;

    public MessageHandlerChat(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
    }

    @Override
    public void handle(byte[] messageBytes) {
        //TODO run on ui thread

    }
}
