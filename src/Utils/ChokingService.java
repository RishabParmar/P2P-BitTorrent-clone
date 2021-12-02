package Utils;

import Models.PeerInfoModel;

import java.util.*;
import java.util.stream.Collectors;

public class ChokingService {
    private static Random random = new Random();

    public static void unChokePreferredNeighbors() {
        Map<Integer, PeerInfoModel> map = ConnectionHandler.getPeers();
        List<Integer> peerIds = getPreferredNeighbors(map);
        for(int peerId : peerIds) {
            PeerInfoModel peerInfo = map.get(peerId);
            if(!peerInfo.isPreferredNeighbour()) {
                ConnectionHandler.setPreferredNeighbourAndUnchoke(peerId);
            }
        }
    }

    private static List<Integer> getPreferredNeighbors(Map<Integer, PeerInfoModel> map) {
        Set<Integer> peerIds = map.keySet();
        peerIds.remove(Constants.SELF_PEER_ID);
        List<Integer> list = peerIds.stream().collect(Collectors.toList());
        Set<Integer> preferredNeighbors = new HashSet<>();
        for(int i = 0; i < Constants.PREFERRED_NEIGHBOR_COUNT; i++) {
            preferredNeighbors.add(list.get(random.nextInt(Constants.PREFERRED_NEIGHBOR_COUNT)));
        }

        return preferredNeighbors.stream().collect(Collectors.toList());
    }

    public static void chokeCurrentPreferredNeighbors() {
        Map<Integer, PeerInfoModel> map = ConnectionHandler.getPeers();
        List<Integer> peerIds = getPreferredNeighbors(map);
        for(int peerId : peerIds) {
            PeerInfoModel peerInfoModel = map.get(peerId);
            if(peerInfoModel.isPreferredNeighbour() || peerInfoModel.isOptimisticallyUnchoked()) continue;
            ConnectionHandler.removePreferredNeighbourAndChoke(peerId);
        }
    }

    public static void chokeOptimisticallyUnchokedNeighbor() {
        Map<Integer, PeerInfoModel> map = ConnectionHandler.getPeers();
        for(int peerId : map.keySet()) {
            if(map.get(peerId).isOptimisticallyUnchoked()) {
                ConnectionHandler.removeOptimisticallyUnchokedNeighborAndChoke(peerId);
            }
        }
    }

    public static void unchokeNeighborOptimistically() {
        Map<Integer, PeerInfoModel> map = ConnectionHandler.getPeers();
        List<Integer> list = new ArrayList<>();
        for(int peerId : map.keySet()) {
            if(peerId != Constants.SELF_PEER_ID && map.get(peerId).isChoke()) list.add(peerId);
        }
        ConnectionHandler.setNeighborOptimisticallyAndUnchoke(list.get(random.nextInt(list.size())));
    }
}
