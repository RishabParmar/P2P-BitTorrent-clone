package Models;

import Core.PeerProcess;
import Utils.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

    public void unpackAndValidateHeader(byte[] handShakeHeaderBytes) throws Exception {
        byte[] headerBytes = Arrays.copyOfRange(handShakeHeaderBytes , 0, 18);
        String header = new String(headerBytes);
        int senderPeedId;
        if(!header.equals(Constants.HANDSHAKE_HEADER))
            throw new IOException("Handshake message is not valid!!!");
        // Extracting the peerId of the sender
        senderPeedId = getSenderPeerId(handShakeHeaderBytes);
        // Validating the peer
        if(!PeerProcess.listOfPeerIds.contains(senderPeedId)) {
            throw new Exception("Unidentified peer detected! Closing connection...");
        }
    }
}