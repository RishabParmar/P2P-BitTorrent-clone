package Models;

import Core.PeerProcess;
import Utils.Constants;

import java.nio.ByteBuffer;

public class HandshakeRequestModel {

    public String header = Constants.HANDSHAKE_HEADER;
    public byte[] zeroBits = new byte[10];
    public int peerId = PeerProcess.peer.peerId;

    public byte[] getByteArray() {
        byte[] bytes = new byte[32];
        byte[] headerBytes = header.getBytes();
        byte[] peerIdBytes = ByteBuffer.allocate(4).putInt(peerId).array();
        for(int i = 0, j=0; i  < 32; i++) {
            if(i < headerBytes.length) bytes[i] = headerBytes[i];
            if(i >= bytes.length-4) bytes[i] = peerIdBytes[j++];
        }
        return bytes;
    }

    public int getSenderPeerId(byte[] receievedHandshakeHeader) {
        // Recovering string to bytes
        byte[] headerBbytes = new byte[18];
        for(int i=0;i<18;i++) {
            headerBbytes[i] = receievedHandshakeHeader[i];
        }
        String handshakeHeader = new String(headerBbytes);
        System.out.println("Unpacked header: " + handshakeHeader);
        // Recovering the peerId from handshake header bytes
        byte[] peerIdBytes = new byte[4];
        for(int i=0;i<4;i++) {
            peerIdBytes[i] = receievedHandshakeHeader[28+i];
        }
        int handShakePeerId = ByteBuffer.wrap(peerIdBytes).getInt();
        System.out.println("Unpacked peerId: " + handShakePeerId);
        return handShakePeerId;
    }
}