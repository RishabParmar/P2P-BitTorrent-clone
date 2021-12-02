import Models.PeerInfoModel;
import Utils.ChokingService;
import Utils.ConnectionHandler;
import Utils.Constants;
import Utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PeerProcess {
    int[] peers = new int[]{1001, 1002, 1003, 1004, 1005};
    private static boolean isTerminationRequested = false;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        setAndInitializeDefaults();

        while(!isTerminationRequested) {
            Socket client = serverSocket.accept();
            new Thread(new ConnectionHandler(client)).start();
        }

        System.out.println("Done!");
        System.exit(-1);
    }

    private static void setAndInitializeDefaults() {
        Constants.setPeerId(1001);
        //TODO: read config file and set unchoking intervals
        Constants.setUnchokingInterval(5);
        Constants.setOptimisticUnchokingInterval(15);
        String fileDirectoryPath = System.getProperty("user.dir") + "/out/production/P2P-BitTorrent-clone/peer_" + Constants.SELF_PEER_ID;
        Constants.setFileDirectoryPath(fileDirectoryPath);
        new File(Constants.FILE_DIRECTORY_PATH).mkdirs();
        ConnectionHandler.addNewPeer(new PeerInfoModel(Constants.SELF_PEER_ID));
        //TODO: if(peer has file) --> below line + update bitfield of current peer
        //FileUtils.splitFile(Constants.FILE_DIRECTORY_PATH + "/" + "original.jpg", Constants.FILE_DIRECTORY_PATH);
        //TODO: for(previour peer ids ) create socket connection
        Socket socket1 = null;
        try {
            socket1 = new Socket("10.3.1.19", 6666);
            //Socket socket2 = new Socket("10.20.168.230", 1111);
            //new Thread(new ConnectionHandler(socket2)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new ConnectionHandler(socket1)).start();


        setInterval();
    }

    private static void setInterval(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Your database code here
                if(terminateProcess()) stopProcessAndExit();
                ChokingService.chokeCurrentPreferredNeighbors();
                ChokingService.unChokePreferredNeighbors();
            }
        }, Constants.UNCHOKING_INTERVAL*60*1000, Constants.UNCHOKING_INTERVAL*60*1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Your database code here
                ChokingService.chokeOptimisticallyUnchokedNeighbor();
                ChokingService.unchokeNeighborOptimistically();
            }
        }, Constants.OPTIMISTIC_UNCHOKING_INTERVAL*60*1000, Constants.OPTIMISTIC_UNCHOKING_INTERVAL*60*1000);
    }

    private static void stopProcessAndExit() {
        FileUtils.mergeFile();
        isTerminationRequested = true;
    }

    private static boolean terminateProcess() {
        Map<Integer, PeerInfoModel> map = ConnectionHandler.getPeers();
        for(int peerId : map.keySet()) {
            if(map.get(peerId).getPieceCount() != Constants.FILE_PIECES_COUNT) return false;
        }
        return true;
    }
}
