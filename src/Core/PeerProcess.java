package Core;

import Models.PeerModel;
import Utils.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class PeerProcess {

    // peer object is the current peer and contains all the necessary information
    public static List<Integer> listOfPeerIds = new ArrayList<>();
    public static PeerModel peer;

    public void createPeer(int peerId) {
        try {
            // Setting the peerId
            List<PeerModel> neighbors = new ArrayList<>();
            boolean hasDiscoveredCurrentPeer = false;
            File fileName = new File("src\\PeerInfo.cfg");
            Scanner myReader = new Scanner(fileName);
            while (myReader.hasNextLine()) {
                // 0 = peerId, 1 = hostName, 2 = portNumber, 3 = hasFile
                String[] data = myReader.nextLine().split("\\s+");
                PeerModel currentPeer = new PeerModel(Integer.parseInt(data[0]),
                        data[1],
                        Integer.parseInt(data[2]),
                        data[3].equals("1"),
                        null
                        );
                listOfPeerIds.add(currentPeer.peerId);
                if(peerId != currentPeer.peerId && !hasDiscoveredCurrentPeer) neighbors.add(currentPeer);
                else if(!hasDiscoveredCurrentPeer) {
                    peer = currentPeer;
                    hasDiscoveredCurrentPeer = true;
                }
            }
            // Set neighbors to the concerned peer running on the machine
            peer.neighbors = neighbors;
//            System.out.println("Peer information: ");
//            System.out.println(peer.peerId);
//            System.out.println(peer.neighbors);
//            System.out.println(peer.hasFile);
//            System.out.println(peer.hostName);
            System.out.println(listOfPeerIds);
            myReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Something went wrong with PeerInfo.cfg file!");
            e.printStackTrace();
        }
    }

    public void setCommonInfoAsConstants() {
        try {
            HashMap<String, String> commonInfoMap = new HashMap<>();
            File fileName = new File("src\\Common.cfg");
            Scanner myReader = new Scanner(fileName);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split("\\s+");
                commonInfoMap.put(data[0], data[1]);
            }
            System.out.println(commonInfoMap);
            myReader.close();
            // Updating the Constants file with Common.cfg information
            Constants constants = new Constants();
            constants.setCommonInfo(commonInfoMap);
        } catch (FileNotFoundException e) {
            System.err.println("Something went wrong with Common.cfg file!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        PeerProcess peerProcess = new PeerProcess();
        peerProcess.setCommonInfoAsConstants();
        peerProcess.createPeer(Integer.parseInt(args[0]));
        // Start server:
        ServerProcess.startServer();
    }
}