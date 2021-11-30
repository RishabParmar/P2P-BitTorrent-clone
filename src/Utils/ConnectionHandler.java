package Utils;

import Models.HandshakeRequestModel;
import Models.PeerInfoModel;
import Models.RequestModel;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class ConnectionHandler implements Runnable{
    Socket socket;
    InputStream in;
    OutputStream out;
    private int peerId;
    private static Map<Integer, PeerInfoModel> map = new HashMap<>();
    private static Random random = new Random();
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

            System.out.println("handshake initiated");
            handshake();
            System.out.println("handshake completed");
            sendBitfield();
            System.out.println("bitfield completed");

            byte[] inputStreamByteArray = new byte[ 32773 ];
            in.read(inputStreamByteArray);
            //byte[] inputStreamPayload = ByteStreams.toByteArray(in);

            System.out.println("new message arrived");
            processInputStreamPayload(inputStreamByteArray);

        } catch (Exception e) {
            //TODO: logger
        }
    }

    private void handshake() {
        try{
            HandshakeRequestModel handshakeRequestModel = new HandshakeRequestModel();
            out.write(handshakeRequestModel.getBytes());
            //TODO: read incoming handshake
            byte[] headerBytes = new byte[32];
            in.read(headerBytes);
            peerId = HandshakeRequestModel.getPeerHandshakeModel(headerBytes);
            addNewPeer(new PeerInfoModel(peerId));
        } catch (Exception e) {
            System.out.println("handshake error! " + e);
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
            System.out.println("qwertyuiopiu");
            int type = b[4];
            System.out.println("type: " + type);
            Enums.MessageTypes messageType = Enums.MessageTypes.values()[type];
            switch (messageType) {
                case BITFIELD:
                    processBitfieldRequest(b);
                    break;
                case INTERESTED:
                    map.get(peerId).setInterested(true);
                    break;
                case NOT_INTERESTED:
                    map.get(peerId).setInterested(false);
                    break;
                case UNCHOKE:
                    processIncomingUnchokeStatus();
                    break;
                case CHOKE:
                    processIncomingChokeStatus();
                    break;
                case HAVE:
                    processIncomingHaveStatus(b);
                    break;
                default:
                    System.out.println("invalid message type received");
            }
        } catch (Exception e) {
            System.out.println("error at process input stream " + e);
            //TODO: log
        }
    }

    private void processIncomingHaveStatus(byte[] b) {
        int index = ByteBuffer.wrap(new byte[]{b[5], b[6], b[7], b[8]}).getInt();
        map.get(peerId).getPieces()[index] = true;
        if(checkIfInterested()) sendInterested(false);
        else sendInterested(true);
    }

    private void processBitfieldRequest(byte[] bytes) {
        System.out.println(bytes.length );
        boolean[] pieces = new boolean[Constants.FILE_PIECES_COUNT];
        for(int i = 0; i <= pieces.length/8; i++) {
            BitSet set = BitSet.valueOf(new byte[] { bytes[i+5] });
            for (int j = 0; j < 8; j++) {
                if(set.get(j)) pieces[8*i+j] = true;
            }
        }
        map.get(peerId).setPieces(pieces);
        checkAndSendInterestedStatus();
    }

    private void checkAndSendInterestedStatus() {
        boolean[] selfPieces = map.get(Constants.SELF_PEER_ID).getPieces();
        boolean[] peerPieces = map.get(peerId).getPieces();

        for(int i = 0; i < selfPieces.length; i++) {
            if(!selfPieces[i] && peerPieces[i]) {
                sendInterested(true);
                return;
            }
        }
        //sendInterested(false);
    }

    private void sendInterested(boolean interested) {
        try {
            Enums.MessageTypes messageType = interested ? Enums.MessageTypes.INTERESTED : Enums.MessageTypes.NOT_INTERESTED;
            RequestModel requestModel = new RequestModel(messageType, null);
            out.write(requestModel.getBytes());
        } catch (Exception e) {
            System.out.println("Error sending interested! " + e);
        }
    }

    private synchronized void processIncomingUnchokeStatus() {
        boolean[] selfPieces = map.get(Constants.SELF_PEER_ID).getPieces();
        boolean[] peerPieces = map.get(peerId).getPieces();
        int[] requestedPieces = PeerInfoModel.getrequestedPieces();
        List<Integer> list = new ArrayList<>();
        //---begin lock
        for(int i = 0; i < selfPieces.length; i++) {
            if(!selfPieces[i] && peerPieces[i] && requestedPieces[i] <= 0) list.add(i);
        }
        if(list.isEmpty()) {
            System.out.println("corner case");
            return;
        }
        int randomPieceIndexToBeRequested = list.get(random.nextInt(list.size()));
        sendPieceRequest(randomPieceIndexToBeRequested);
        //---end lock
    }

    private synchronized void processIncomingChokeStatus() {
        int[] requestedPieces = PeerInfoModel.getrequestedPieces();
        for(int i = 0; i < requestedPieces.length; i++) {
            if(requestedPieces[i] == peerId) requestedPieces[i] = 0;
        }
    }

    private void sendPieceRequest(int index) {
        try {
            PeerInfoModel.getrequestedPieces()[index] = peerId;
            RequestModel<Integer> requestModel = new RequestModel(Enums.MessageTypes.REQUEST, index);
            out.write(requestModel.getBytes());
        } catch (Exception e) {
            System.out.println("error in sendPieceRequest! " + e);
        }
    }

    private boolean checkIfInterested() {
        boolean[] selfPieces = map.get(Constants.SELF_PEER_ID).getPieces();
        boolean[] peerPieces = map.get(peerId).getPieces();
        for(int i = 0; i < selfPieces.length; i++) {
            if(!selfPieces[i] && peerPieces[i]) return true;
        }
        return false;
    }
}
