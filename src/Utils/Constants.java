package Utils;

public class Constants {
    public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    public static int SELF_PEER_ID;
    public static final int FILE_PIECES_COUNT = 306;
    // Call when main runs
    public static void setPeerId(int peerId) {
        SELF_PEER_ID = peerId;
    }
}
