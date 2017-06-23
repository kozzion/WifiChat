package nl.everlutions.wifichat.handler;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import nl.everlutions.wifichat.services.ConnectionMessage;

import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_RESULT;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE;
import static nl.everlutions.wifichat.services.ServiceMain.ACTIVITY_MESSAGE_TYPE_SHOW_CHAT;
import static nl.everlutions.wifichat.services.ServiceMain.FILTER_TO_UI;

/**
 * Created by jaapo on 6-6-2017.
 */

public class MessageHandlerCommandChat implements IMessageHandlerByteArray {

    private final LocalBroadcastManager mBroadCastManager;

    public MessageHandlerCommandChat(Context context) {
        mBroadCastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void handle(byte[] messageBytes) {
        String chatMessage = new String(messageBytes);
        Intent intent = new Intent(FILTER_TO_UI);
        intent.putExtra(ACTIVITY_MESSAGE_RESULT, chatMessage);
        intent.putExtra(ACTIVITY_MESSAGE_TYPE, ACTIVITY_MESSAGE_TYPE_SHOW_CHAT);
        mBroadCastManager.sendBroadcast(intent);
    }

    public static ConnectionMessage createConnectionMessageFromChatMessage(String chatMessage) {
        return new ConnectionMessage(ConnectionMessage.TYPE_COMMAND_CHAT, chatMessage.getBytes());
    }
}
