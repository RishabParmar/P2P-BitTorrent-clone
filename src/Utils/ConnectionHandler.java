package Utils;

import Models.HandshakeRequestModel;
import Models.PeerInfoModel;
import Models.RequestModel;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class ConnectionHandler implements Runnable{
    Socket socket;
    InputStream in;
    OutputStream out;
    private int peerId;
    private static Map<Integer, PeerInfoModel> map = new HashMap<>();
    boolean[] selfPieces;
    boolean[] peerPieces;
    int[] requestedPieces = PeerInfoModel.getrequestedPieces();
    private static Random random = new Random();
    private final String filePath = "C:\\Users\\W10\\Desktop\\UFL\\CN\\Project\\samp le.jpg";
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

//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//            int read;
//            while(true) {
//                byte[] inputStreamByteArray = new byte[33000];
//                read = in.read( inputStreamByteArray );
//                buffer.write( inputStreamByteArray, 0, read );
//                inputStreamByteArray = buffer.toByteArray();
//                System.out.println("new message arrived");
//                processInputStreamPayload(inputStreamByteArray);
//            }


            while(true) {
                byte[] inputStreamByteArray = new byte[ 33000 ];
                System.out.println("reading new message from inputstream");
                int x = in.read(inputStreamByteArray, 0, inputStreamByteArray.length);
                if(x <= 0) continue;
                System.out.println("new message arrived");
                processInputStreamPayload(inputStreamByteArray);
            }
        } catch (Exception e) {
            //TODO: logger
        }
    }

    private void handshake() {
        try{
            HandshakeRequestModel handshakeRequestModel = new HandshakeRequestModel();
            out.flush();
            out.write(handshakeRequestModel.getBytes());
//            out.flush();
            //TODO: read incoming handshake
            byte[] headerBytes = new byte[32];
            in.read(headerBytes);
            peerId = HandshakeRequestModel.getPeerHandshakeModel(headerBytes);
            addNewPeer(new PeerInfoModel(peerId));

            map.get(peerId).setOutputStream(out);
            selfPieces = map.get(Constants.SELF_PEER_ID).getPieces();

        } catch (Exception e) {
            System.out.println("handshake error! " + e);
            //TODO: logger
            //TODO: stop the process
        }
    }

    private void sendBitfield() throws IOException {
        try {
            //if(map.get(Constants.SELF_PEER_ID).getPieceCount() == 0) return;
            RequestModel requestModel = new RequestModel(Enums.MessageTypes.BITFIELD, map.get(Constants.SELF_PEER_ID).getPieces());
            out.flush();
            out.write(requestModel.getBytes());
//            out.flush();
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
            System.out.println("Type of incoming request: " + messageType);
            switch (messageType) {
                case BITFIELD:
                    processBitfieldRequest(b);
                    break;
                case INTERESTED:
                    map.get(peerId).setInterested(true);
                    setPreferredNeighbourAndUnchoke(peerId);
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
                    //processIncomingHaveStatus(b);
                    break;
                case PIECE:
                    processIncomingPieceStatus(b);
                    break;
                case REQUEST:
                    processIncomingRequestStatus(b);
                    break;
                default:
                    System.out.println("invalid message type received");
            }
        } catch (Exception e) {
            System.out.println("error at process input stream! " + e);
            //TODO: log
        }
    }

//    private void processIncomingInterestedStatus() {
//        PeerInfoModel peerInfoModel = map.get(peerId);
//        peerInfoModel.setInterested(true);
//        if(peerInfoModel.isOptimisticallyUnchoked() || peerInfoModel.isPreferredNeighbour()) {
//
//        }
//    }

    private void processIncomingRequestStatus(byte[] b) {
        try {
            byte[] indexbytes = new byte[]{b[5], b[6], b[7], b[8]};
            int pieceIndex = ByteBuffer.wrap(indexbytes).getInt();
            byte[] bytes = ArrayUtils.addAll(indexbytes, FileUtils.getBytes(pieceIndex));

            RequestModel requestModel = new RequestModel(Enums.MessageTypes.PIECE, bytes);
            if(map.get(peerId).isChoke()) return;
            byte[] bb = requestModel.getBytes();
//           for(byte b1 : bb) {
//               int x = (int)b1;
//               //System.out.println();
//           }
//            TimeUnit.MILLISECONDS.sleep(300);
            out.flush();
            out.write(bb);
//            out.flush();
            System.out.println("PIECE sent " + pieceIndex + " size: " + bb.length);
        } catch (Exception e) {
            System.out.println("error processing incomingRequestStatus! " + e);
        }
    }

    private void processIncomingPieceStatus(byte[] b) {
        try{
            //int pieceContentSize = ByteBuffer.wrap(new byte[]{b[0], b[1], b[2], b[3]}).getInt() - 5;
            int pieceIndex = ByteBuffer.wrap(new byte[]{b[5], b[6], b[7], b[8]}).getInt();
            //updateIncomingPiece(pieceContentSize, pieceIndex, Arrays.copyOfRange(b, 9, b.length));
            updateIncomingPiece(b, pieceIndex, 9);
            sendHaveMessage(pieceIndex);
            selectAndSendRandomPieceRequest();
        } catch (Exception e) {
            System.out.println("Error processing piece status! " + e);
        }
    }

    private void sendHaveMessage(int index) {
        try {
            RequestModel<Integer> requestModel = new RequestModel(Enums.MessageTypes.HAVE, index);
            byte[] bytes = requestModel.getBytes();
            for(int peerID : map.keySet()) {
                if(peerID == Constants.SELF_PEER_ID) continue;
                OutputStream outputStream = map.get(peerID).getOutputStream();
                outputStream.flush();
                outputStream.write(bytes);
//                outputStream.flush();
            }
        } catch (Exception e) {
            System.out.println("error in sendHaveMessages! " + e);
        }
    }

    private void updateIncomingPiece(byte[] b, int pieceIndex, int offset){
        //TODO: add to file
        FileUtils.createFile(b, pieceIndex, offset);
        map.get(Constants.SELF_PEER_ID).getPieces()[pieceIndex] = true;
    }

    private void processIncomingHaveStatus(byte[] b) {
        int index = ByteBuffer.wrap(new byte[]{b[5], b[6], b[7], b[8]}).getInt();
        map.get(peerId).getPieces()[index] = true;
        if(checkIfInterested()) sendInterested(true);
        else sendInterested(false);
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
        peerPieces = pieces;
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
            out.flush();
            out.write(requestModel.getBytes());
//            out.flush();
        } catch (Exception e) {
            System.out.println("Error sending interested! " + e);
        }
    }

    private void processIncomingUnchokeStatus() {
        selectAndSendRandomPieceRequest();
    }

    private synchronized void selectAndSendRandomPieceRequest() {

        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < selfPieces.length; i++) {
            if(!selfPieces[i] && peerPieces[i] && requestedPieces[i] <= 0) list.add(i);
        }
        if(list.isEmpty()) {
            System.out.println("corner case");
            return;
        }
        int randomPieceIndexToBeRequested = list.get(random.nextInt(list.size()));
        sendPieceRequest(randomPieceIndexToBeRequested);
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
            out.flush();
            out.write(requestModel.getBytes());
//            out.flush();
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

    public static Map<Integer, PeerInfoModel> getPeers() {
        return map;
    }

    public static void setPreferredNeighbourAndUnchoke(int peerId) {
        try {
            PeerInfoModel peerInfoModel = map.get(peerId);
            if(!peerInfoModel.isChoke()){
                peerInfoModel.setPreferredNeighbour();
            } else {
                peerInfoModel.setPreferredNeighbour();
                RequestModel requestModel = new RequestModel(Enums.MessageTypes.UNCHOKE, null);
                System.out.println("Sending unchoking");
                peerInfoModel.outputStream.flush();
                peerInfoModel.outputStream.write(requestModel.getBytes());
//                peerInfoModel.outputStream.flush();
                System.out.println("Unchoke sent");
                //peerInfoModel.getOutputStream().write(requestModel.getBytes());
            }
        } catch (Exception e) {
            System.out.println("Error unchoking peer " + peerId + "! " + e);
        }
    }

    public static void removePreferredNeighbourAndChoke(int peerId) {
        try {
            RequestModel requestModel = new RequestModel(Enums.MessageTypes.CHOKE, null);
            PeerInfoModel peerInfoModel = map.get(peerId);
            peerInfoModel.choke();
            peerInfoModel.getOutputStream().write(requestModel.getBytes());
        } catch (Exception e) {
            System.out.println("Error choking peer " + peerId + "! " + e);
        }
    }

    public static void setNeighborOptimisticallyAndUnchoke(int peerId) {
        try {
            RequestModel requestModel = new RequestModel(Enums.MessageTypes.UNCHOKE, null);
            PeerInfoModel peerInfoModel = map.get(peerId);
            peerInfoModel.setoptimisticallyUnchoked(true);
            peerInfoModel.getOutputStream().write(requestModel.getBytes());
        } catch (Exception e) {
            System.out.println("Error setting optimistically unchoke neighbor " + peerId + "! " + e);
        }
    }

    public static void removeOptimisticallyUnchokedNeighborAndChoke(int peerId) {
        try {
            PeerInfoModel peerInfoModel = map.get(peerId);
            peerInfoModel.setoptimisticallyUnchoked(false);
            if(peerInfoModel.isPreferredNeighbour()) {
                return;
            }
            peerInfoModel.choke();
            RequestModel requestModel = new RequestModel(Enums.MessageTypes.CHOKE, null);
            peerInfoModel.outputStream.write(requestModel.getBytes());
            System.out.println("interested sent");
            //peerInfoModel.getOutputStream().write(requestModel.getBytes());
        } catch (Exception e) {
            System.out.println("Error chocking optimistically unchoked neighbor " + peerId + "! " + e);
        }
    }
}
