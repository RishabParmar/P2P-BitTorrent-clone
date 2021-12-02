package Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class NetworkUtils {
    void socketFun() throws IOException {
        Socket clientSocket = new Socket("peer_1", 123);
        DataOutputStream outToPeer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromPeer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        String sentence = inFromPeer.readLine();
        outToPeer.writeBytes(""+'\n');
        String responseSentence = inFromPeer.readLine();
        clientSocket.close();
    }
}
