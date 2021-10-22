package Core;

import Models.HandshakeRequestModel;
import Utils.Constants;
import org.apache.commons.lang.SerializationUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectionHandler implements Runnable{
    Socket socket;
    InputStream in;
    OutputStream out;
    int senderPeedId;
    private final String filePath = "src\\image.JPG";

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Connected");
        // Sending handshake for the first time after connection has been established
        try {
            out = socket.getOutputStream();
//            System.out.println("Sending this to you: " + Arrays.toString(sendHandshake()));
            out.write(sendHandshake());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // sendImageFile();
        // Reading the handshake header from the sender
        try {
            in = socket.getInputStream();
            byte[] handShakeHeaderBytes = new byte[32];
            in.read(handShakeHeaderBytes);
            System.out.println(Arrays.toString(handShakeHeaderBytes));
            byte[] headerBytes = Arrays.copyOfRange(handShakeHeaderBytes , 0, 18);
            String header = new String(headerBytes);
            if(!header.equals(Constants.HANDSHAKE_HEADER))
                throw new IOException("Handshake message is not valid!!!");
            // Extracting the peerId of the sender
            senderPeedId = new HandshakeRequestModel().getSenderPeerId(handShakeHeaderBytes);
            if(!PeerProcess.listOfPeerIds.contains(senderPeedId)) {
                System.err.println("Unidentified peer detected! Closing connection...");
                in.close();
                socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Something went wrong while reading the messages from the sender");
        }
        try{
//            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket");
        }
    }

    private byte[] sendHandshake() {
        return new HandshakeRequestModel().getByteArray();
    }

    private void sendImageFile() {
        try {
            in = new FileInputStream(filePath);
        } catch (IOException e) {
            System.out.println("Cannot get input or output stream of socket");
        }

        try {
            out = this.socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("File not found");
        }
        byte[] bytes = new byte[(int)(new File(filePath).length())];
        int count;
        try{
            while ((count = in.read(bytes)) > 0) {
                System.out.println("Bruh" +count);
                out.write(bytes, 0, count);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e);
        }
    }
}
