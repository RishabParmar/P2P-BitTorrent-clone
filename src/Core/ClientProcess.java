package Core;

import Models.PeerModel;

import java.io.*;
import java.net.Socket;

public class ClientProcess {

    Socket socket = null;

    public static final String outputFilePath = "src\\Output\\result_image.JPG";

    public void createPeerConnections() {
        for(PeerModel currentPeer: PeerProcess.peer.neighbors) {
            try{
                socket = new Socket(currentPeer.hostName, currentPeer.port);
                System.out.println("Yolo!");
                new Thread(new ConnectionHandler(socket)).start();
                System.out.println("Connection Established!");
            } catch (Exception e) {
                System.out.println("Error establishing connection to server!");
                return;
            }
        }
    }
}