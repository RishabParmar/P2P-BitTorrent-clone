package Utils;

import Models.HandshakeRequestModel;
import Models.PeerInfoModel;
import Models.RequestModel;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class ConnectionHandler implements Runnable{
    Socket socket;
    InputStream in;
    OutputStream out;
    private int peerId;
    private static Map<Integer, PeerInfoModel> map = new HashMap<>();
    private final String filePath = "C:\\Users\\W10\\Desktop\\UFL\\CN\\Project\\sample.jpg";
    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        System.out.println("Connected");
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();

            handshake(in, out);
            sendBitfield();


            byte[] inputStreamPayload = IOUtils.toByteArray(in);
            processInputStreamPayload(inputStreamPayload);

        } catch (Exception e) {
            //TODO: logger
        }
    }

    private void handshake(InputStream in, OutputStream out) {
        try{
            HandshakeRequestModel handshakeRequestModel = new HandshakeRequestModel();
            out.write(handshakeRequestModel.getBytes());
            //TODO: read incoming handshake
            byte[] headerBytes = new byte[32];
            in.read(headerBytes);
            peerId = HandshakeRequestModel.getPeerHandshakeModel(headerBytes);
            addNewPeer(new PeerInfoModel(peerId));
        } catch (Exception e) {
            //TODO: logger
            //TODO: stop the process
        }
    }

    private void sendBitfield() throws IOException {
        try {
            if(map.get(Constants.SELF_PEER_ID).getPieceCount() == 0) return;
            RequestModel requestModel = new RequestModel(Enums.MessageTypes.BITFIELD, map.get(Constants.SELF_PEER_ID).getPieces());
            out.write(requestModel.getBytes());
        } catch (Exception e) {
            System.out.println("bitfield error");
        }
    }

    public static void addNewPeer(PeerInfoModel peerInfoModel) {
        map.put(peerInfoModel.getPeerId(), peerInfoModel);
    }

    private void processInputStreamPayload(byte[] b) {
        try {
            int type = Byte.toUnsignedInt(b[4]);
            Enums.MessageTypes messageType = Enums.MessageTypes.values()[type];
            switch (messageType) {
                case BITFIELD:

                    break;
                default:
                    System.out.println("invalid message type received");
            }
        } catch (Exception e) {
            System.out.println("error at process input stream");
            //TODO: log
        }
    }

    private void processBitfieldRequest(byte[] bytes) {
        //BitSet set = new BitSet(Constants.FILE_PIECES_COUNT);
        int tempValue = 0;
        boolean[] pieces = new boolean[bytes.length * 8];
        for (int b = bytes.length - 1; b >= 5; b--) {
            for (int j = 0; j < 8; j++) {
                tempValue = (bytes[b] >> j) & 0x01;
                if (tempValue == 1) {
                    pieces[((bytes.length - 1 - b) * 8) + j] = true;
                }
            }
        }
        map.get(peerId).setPieces(pieces);
    }
}
