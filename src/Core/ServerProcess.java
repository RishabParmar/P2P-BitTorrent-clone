package Core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerProcess implements Runnable{
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PeerProcess.peer.peerId);
            while(true) {
                Socket client = serverSocket.accept();
                System.out.println("Client: " + client);
                new Thread(new ConnectionHandler(client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
