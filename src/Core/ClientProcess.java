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
                socket = new Socket(currentPeer.hostName, currentPeer.peerId);
                new Thread(new ConnectionHandler(socket));
                System.out.println("Connection Established!");
            } catch (Exception e) {
                System.out.println("Error establishing connection to server!");
                return;
            }
        }
    }

//    public static void main(String[] args) {
//        Socket socket = null;
//        InputStream in = null;
//        OutputStream out = null;
//        try{
//            socket = new Socket("localhost", 8089);
//            System.out.println("Connection Established!");
//        } catch (Exception e) {
//            System.out.println("Error establishing connection to server!");
//            return;
//        }
//
//    }
}