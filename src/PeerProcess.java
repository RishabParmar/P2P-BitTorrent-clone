import Models.PeerInfoModel;
import Utils.ChokingService;
import Utils.ConnectionHandler;
import Utils.Constants;
import Utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PeerProcess {
    private static boolean isTerminationRequested = false;
    private static List<String> peerList;
    private static ServerSocket serverSocket;
    public static void main(String[] args) throws IOException {
        Constants.setPeerId(Integer.parseInt(args[0]));
        createPeerAndSocket(Constants.SELF_PEER_ID);
        setAndInitializeDefaults();
        for(int i = 1; i < peerList.size(); i += 3) {
            if(Integer.parseInt(peerList.get(i)) < Constants.SELF_PEER_ID)
                new Thread(new ConnectionHandler(new Socket(peerList.get(i-1), Integer.parseInt(peerList.get(i))))).start();
        }
        while(!isTerminationRequested) {
            Socket client = serverSocket.accept();
            new Thread(new ConnectionHandler(client)).start();
        }

        System.out.println("Done!");
        System.exit(-1);
    }

    private static void setAndInitializeDefaults() {

        setCommonInfoAsConstants();
        String fileDirectoryPath = System.getProperty("user.dir") + "/out/production/P2P-BitTorrent-clone/peer_" + Constants.SELF_PEER_ID;
        Constants.setFileDirectoryPath(fileDirectoryPath);
        new File(Constants.FILE_DIRECTORY_PATH).mkdirs();
        //ConnectionHandler.addNewPeer(new PeerInfoModel(Constants.SELF_PEER_ID));

        FileUtils.splitFile(Constants.FILE_DIRECTORY_PATH + "/" + "original.jpg", Constants.FILE_DIRECTORY_PATH);
        Arrays.fill(ConnectionHandler.getPeers().get(Constants.SELF_PEER_ID).getPieces(), true);
        setInterval();
    }

    private static void setInterval(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(terminateProcess()) stopProcessAndExit();
                ChokingService.chokeCurrentPreferredNeighbors();
                ChokingService.unChokePreferredNeighbors();
            }
        }, Constants.UNCHOKING_INTERVAL*1000, Constants.UNCHOKING_INTERVAL*1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ChokingService.chokeOptimisticallyUnchokedNeighbor();
                ChokingService.unchokeNeighborOptimistically();
            }
        }, Constants.OPTIMISTIC_UNCHOKING_INTERVAL*1000, Constants.OPTIMISTIC_UNCHOKING_INTERVAL*1000);
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

    public static void setCommonInfoAsConstants() {
        try {
            HashMap<String, String> commonInfoMap = new HashMap<>();
            File fileName = new File("src\\Common.cfg");
            Scanner myReader = new Scanner(fileName);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split("\\s+");
                commonInfoMap.put(data[0], data[1]);
            }
            Constants.setPreferredNeighborCount(Integer.parseInt(commonInfoMap.get("NumberOfPreferredNeighbors")));
            Constants.setUnchokingInterval(Integer.parseInt(commonInfoMap.get("UnchokingInterval ")));
            Constants.setOptimisticUnchokingInterval(Integer.parseInt(commonInfoMap.get("OptimisticUnchokingInterval ")));
            Constants.FILE_PIECES_COUNT = Integer.parseInt(commonInfoMap.get("PieceSize"));
            Constants.PIECE_SIZE_IN_BYTES = Integer.parseInt(commonInfoMap.get("FileSize "));
            myReader.close();
        } catch (Exception e) {
            System.err.println("Something went wrong with Common.cfg file!");
            e.printStackTrace();
        }
    }

    public static void createPeerAndSocket(int peerId) {
        try {
            List<String> list = new ArrayList<>();
            boolean hasDiscoveredCurrentPeer = false;
            File fileName = new File("src\\PeerInfo.cfg");
            Scanner myReader = new Scanner(fileName);
            while (myReader.hasNextLine()) {
                // 0 = peerId, 1 = hostName, 2 = portNumber, 3 = hasFile
                String[] data = myReader.nextLine().split("\\s+");
                if (Integer.parseInt(data[0]) == peerId) {
                    Constants.setPeerId(peerId);
                    serverSocket = new ServerSocket(Integer.parseInt(data[2]));
                } else {
                    list.add(data[0]);
                    list.add(data[1]);
                }
                if (Integer.parseInt(data[3]) == 1) {
                    Constants.HAS_FILE = true;
                } else {
                    list.add(data[2]);
                }
            }
            peerList = list;
            myReader.close();
        } catch (Exception e) {
            System.err.println("Something went wrong with PeerInfo.cfg file!");
            e.printStackTrace();
        }
    }
}
