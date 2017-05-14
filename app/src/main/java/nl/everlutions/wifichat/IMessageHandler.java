package nl.everlutions.wifichat;

/**
 * Created by jaapo on 14-5-2017.
 */

public interface IMessageHandler
{
    void  handleMessage(byte [] messageBytes);
}
