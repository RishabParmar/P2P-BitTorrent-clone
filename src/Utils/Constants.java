package Utils;

public class Constants {
    public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    public static int SELF_PEER_ID;
    public static final int FILE_PIECES_COUNT = 306;
    public static final int PIECE_SIZE_IN_BYTES = 32768;
    public static String FILE_DIRECTORY_PATH = "";
    public static int PREFERRED_NEIGHBOR_COUNT;
    public static int UNCHOKING_INTERVAL;
    public static int OPTIMISTIC_UNCHOKING_INTERVAL;
    // Call when main runs
    public static void setPeerId(int peerId) {
        SELF_PEER_ID = peerId;
    }
    public static void setFileDirectoryPath(String path) {
        FILE_DIRECTORY_PATH = path;
    }

    public static void setPreferredNeighborCount(int count) {
        PREFERRED_NEIGHBOR_COUNT = count;
    }

    public static void setUnchokingInterval(int interval) {
        UNCHOKING_INTERVAL = interval;
    }

    public static void setOptimisticUnchokingInterval(int interval) {
        OPTIMISTIC_UNCHOKING_INTERVAL = interval;
    }
}
