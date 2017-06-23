package nl.everlutions.wifichat.services;

/**
 * Created by jaapo on 6-6-2017.
 */

public interface ICommunicationManager {

    void handle(int socketID, int messageType, byte[] byteMessage);

    void handleSendToServer(ConnectionMessage connectionMessage);

    void handleSendToClients(ConnectionMessage connectionMessage);
}
