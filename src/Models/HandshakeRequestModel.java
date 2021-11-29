package Models;

import Utils.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HandshakeRequestModel {

    static final String header = Constants.HANDSHAKE_HEADER;

    public byte[] getBytes() {
        byte[] bytes = new byte[32];
        byte[] headerBytes = header.getBytes();
        byte[] peerIdBytes = ByteBuffer.allocate(4).putInt(Constants.SELF_PEER_ID).array();
        for(int i = 0, j = 0; i  < 32 && j < 4; i++) {
            if(i < headerBytes.length) bytes[i] = headerBytes[i];
            if(i > 27) bytes[i] = peerIdBytes[j++];
        }
        return bytes;
    }

    public static int getPeerHandshakeModel(byte[] bytes) throws IOException {
        if(bytes.length != 32) throw new IOException("Handshake message is not valid!!!  Length of byte array received is " + bytes.length);
        byte[] headerBytes = Arrays.copyOfRange(bytes , 0, 18);
        String header = new String(headerBytes);
        if(!header.equals(Constants.HANDSHAKE_HEADER)) throw new IOException("Handshake message is not valid!!!");
        int peerId;
        try {
            peerId = java.nio.ByteBuffer.wrap(bytes).getInt(28);
        } catch (Exception e) {
            throw new IOException("PeerID is not valid!!!");
        }
        return peerId;
    }

}