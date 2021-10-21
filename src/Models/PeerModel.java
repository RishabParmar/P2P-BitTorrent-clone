package Models;

import java.util.List;

public class PeerModel {
    public int peerId;
    public String hostName;
    public int port;
    public boolean hasFile;
    public List<PeerModel> neighbors;

    public PeerModel(int peerId, String hostName, int port, boolean hasFile, List<PeerModel> neighbors) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
        this.neighbors = neighbors;
    }
}
