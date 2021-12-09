package Models;

import Utils.Constants;

import java.io.OutputStream;
import java.util.Arrays;

public class PeerInfoModel {
    private int peerId;
    private boolean[] pieces = new boolean[Constants.FILE_PIECES_COUNT];
    private static int[] requestedPieces = new int[Constants.FILE_PIECES_COUNT];
    private boolean choke;
    private boolean interested;
    private double uploadRate;
    private boolean optimisticallyUnchoked;
    private boolean preferredNeighbour;
    private int pieceCount;
    public OutputStream outputStream;

    public PeerInfoModel(int peerId) {
        this.peerId = peerId;
        this.choke = true;
        if(Constants.HAS_FILE) Arrays.fill(pieces, true);
    }

    public int getPeerId() {
        return this.peerId;
    }

    public void choke() {
        this.choke = true;
        this.optimisticallyUnchoked = false;
        this.preferredNeighbour = false;
        //TODO: reset requestedPieces array  to zero which are not downloaded
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    public void setoptimisticallyUnchoked(boolean flag) {
        this.optimisticallyUnchoked = flag;
        if(flag) {
            this.choke = false;
        }

    }

    public void setPreferredNeighbour() {
        this.preferredNeighbour = true;
        this.choke = false;
        //TODO: reset downloading speed to zero
    }

    public boolean isInterested() {
        return this.interested;
    }

    public boolean isChoke() {
        return this.choke;
    }

    public boolean isOptimisticallyUnchoked() {
        return this.optimisticallyUnchoked;
    }

    public boolean isPreferredNeighbour() {
        return this.preferredNeighbour;
    }

    public boolean[] getPieces() {
        return pieces;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void updatePieceArray(int position) {
        pieces[position] = true;
        pieceCount++;
    }

    public void setPieces(boolean[] pieces) {
        this.pieces = pieces;
    }

    public static int[] getrequestedPieces() {
        return requestedPieces;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

}
