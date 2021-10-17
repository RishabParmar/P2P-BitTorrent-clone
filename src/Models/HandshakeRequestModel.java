package Models;

import Utils.Constants;

public class HandshakeRequestModel {

    String header = Constants.HANDSHAKE_HEADER;
    byte zeroBits[] = new byte[10];
    int peerId = Constants.PEER_ID;
}