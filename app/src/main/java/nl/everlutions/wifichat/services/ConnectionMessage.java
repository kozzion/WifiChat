package nl.everlutions.wifichat.services;


public class ConnectionMessage {

    public static final int TYPE_REQUEST_AUDIO = 1;
    public static final int TYPE_REQUEST_CHAT = 2;
    public static final int TYPE_COMMAND_RECORD = 3;
    public static final int TYPE_COMMAND_CHAT = 4;

    public int type;
    public byte[] payload;

    public ConnectionMessage(int type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }
}
