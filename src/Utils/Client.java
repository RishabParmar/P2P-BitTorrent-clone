package Utils;

import Models.HandshakeRequestModel;
import Models.RequestModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        //Socket socket = new Socket("localhost", 6666);

        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        try{
            socket = new Socket("10.20.168.247", 6008);
            System.out.println("Connection Established!");
        } catch (Exception e) {
            System.out.println("Error establishing connection to server!");
            return;
        }
        try {
            out = socket.getOutputStream();
            sendHandshake(out);
        } catch (IOException ex) {
            System.out.println("Can't get socket input stream. ");
        }

        try {
            in = socket.getInputStream();
            byte[] headerBytes = new byte[32];
            in.read(headerBytes);
            int  peerId = HandshakeRequestModel.getPeerHandshakeModel(headerBytes);
            boolean[] bitfield = new boolean[128];
            bitfield[128] = true;
            out.write(new RequestModel<boolean[]>(Enums.MessageTypes.BITFIELD, bitfield).getBytes());
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static void sendHandshake(OutputStream out) {
        try{
            HandshakeRequestModel handshakeRequestModel = new HandshakeRequestModel();
            out.write(handshakeRequestModel.getBytes());
        } catch (Exception e) {
            //TODO: logger
        }

    }
}
