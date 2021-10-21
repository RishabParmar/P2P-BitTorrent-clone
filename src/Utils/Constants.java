package Utils;

import java.util.HashMap;

public class Constants {
    public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    public static int PEER_ID;
    public static int NUMBER_OF_PREFFERED_NEIGHBORS;
    public static int UNCHOKING_INTERVAL;
    public static int OPTIMISTIC_UNCHOKING_INTERVAL;
    public static String FILE_NAME;
    public static int FILE_SIZE;
    public static int PIECE_SIZE;

    // Call when main runs
    public void setPeerId(int peerId) {
        PEER_ID = peerId;
    }
    public void setCommonInfo(HashMap<String, String> commonInfoMap) {
        NUMBER_OF_PREFFERED_NEIGHBORS = Integer.parseInt(commonInfoMap.get("NumberOfPreferredNeighbors"));
        UNCHOKING_INTERVAL = Integer.parseInt(commonInfoMap.get("UnchokingInterval"));
        OPTIMISTIC_UNCHOKING_INTERVAL = Integer.parseInt(commonInfoMap.get("OptimisticUnchokingInterval"));
        FILE_NAME = commonInfoMap.get("FileName");
        FILE_SIZE = Integer.parseInt(commonInfoMap.get("FileSize"));
        PIECE_SIZE = Integer.parseInt(commonInfoMap.get("PieceSize"));
    }
//    public void setPeerId(int peerId) {
//        PEER_ID = peerId;
//    }
//    public void setPeerId(int peerId) {
//        PEER_ID = peerId;
//    }
//    public void setPeerId(int peerId) {
//        PEER_ID = peerId;
//    }
//    public void setPeerId(int peerId) {
//        PEER_ID = peerId;
//    }public void setPeerId(int peerId) {
//        PEER_ID = peerId;
//    }

}
