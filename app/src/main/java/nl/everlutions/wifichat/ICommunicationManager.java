package nl.everlutions.wifichat;

/**
 * Created by jaapo on 6-6-2017.
 */

public interface ICommunicationManager {

    void handle(int mSocketID, int type, byte [] byteMessage);
}
