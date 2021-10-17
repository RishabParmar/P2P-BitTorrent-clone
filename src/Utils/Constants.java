package Utils;

public class Constants {
    public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    public static int PEER_ID;

    // Call when main runs
    public void setPeerId(int peerId) {
        PEER_ID = peerId;
    }
}
