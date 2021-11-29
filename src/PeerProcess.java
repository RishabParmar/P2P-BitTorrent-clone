import Models.PeerInfoModel;
import Utils.ConnectionHandler;
import Utils.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerProcess {
    int[] peers = new int[]{1001, 1002, 1003, 1004, 1005};

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6008);


        while(true) {
            Socket client = serverSocket.accept();
            new Thread(new ConnectionHandler(client)).start();
        }
    }

    private static void setAndInitializeDefaults() {
        //TODO: read config file
        Constants.setPeerId(1003);
        ConnectionHandler.addNewPeer(new PeerInfoModel(Constants.SELF_PEER_ID));
        //for(previour peer ids ) create socket connection and add them to map
    }
    private static void makeConnection() {

    }
}
