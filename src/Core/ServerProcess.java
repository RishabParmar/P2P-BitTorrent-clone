package Core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerProcess {
    public static void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8089);
        while(true) {
            Socket client = serverSocket.accept();
            System.out.println("Client: " + client);
            new Thread(new ConnectionHandler(client)).start();
        }
    }
}
