package kr.ac.kumoh.s20120499.management;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    int sender;
    int receiver;
    int minIndex;
    int blockchainLength;
    MESSAGE_TYPE type;
    List<Block> blocks;
    PeerInfo peerInfo;
    List<PeerInfo> peerList;
    List<PeerInfo> lightPeerList;
    List<BlockData> blockData;
    HashMap<Integer, Integer> chainLengthInfo;
    

    public enum MESSAGE_TYPE {
        READY, INFO_NEW_BLOCK, REQ_ALL_BLOCKS, RSP_ALL_BLOCKS, DELET_PEER, ADD_PEER, PEER_LIST, MAKE_BLOCK,
        ADD_LIGHTPEER, DELET_LIGHTPEER, TRANSACTION, CONFIRM, PEER_LIST_AND_LIGHTPEER_LIST, END_MINE, END_ADD,
        JUST_ALL_BLOCKS, JUST_RSP_ALL_BLOCKS, VIRTUAL_PORT, CERTIFICATION, CERTIFICATION_SUCCESS, CERTIFICATION_FAILURE,
        BLOCKCHAIN_PIECE, VIRTUAL_INFO, VIRTUAL_INFO_RESPONSE, GET_BLOCKCHAIN_PIECE, SEND_BLOCKCHAIN_PIECE,
        ALL_BLOCKCHAIN_CLEAR, ALL_BLOCKCHAIN_CLEAR_OK, GET_BACKUP_CHAIN, GET_BACKUP_CHAIN_OK, BACKUP_ACTIVITY, BACKUP_ACTIVITY_OK
    }

    @Override
    public String toString() {
        return String.format("Message {type=%s, sender=%d, receiver=%d, blocks=%s, blockData=%s}", type, sender, receiver, blocks, blockData);
    }

    static class MessageBuilder {
        private final Message message = new Message();

        MessageBuilder withSender(final int sender) {
            message.sender = sender;
            return this;
        }
        
        MessageBuilder withBlockchainLength(final int blockchainLength) {
        	message.blockchainLength = blockchainLength;
        	return this;
        }
        
        MessageBuilder withChainLength(final HashMap<Integer, Integer> chainLengthInfo) {
        	message.chainLengthInfo = chainLengthInfo;
        	return this;
        }

        MessageBuilder withReceiver(final int receiver) {
            message.receiver = receiver;
            return this;
        }

        MessageBuilder withType(final MESSAGE_TYPE type) {
            message.type = type;
            return this;
        }

        MessageBuilder withBlocks(final List<Block> blocks) {
            message.blocks = blocks;
            return this;
        }
        
        MessageBuilder withPeerInfo(final PeerInfo peerInfo) {
            message.peerInfo = peerInfo;
            return this;
        }
        
        MessageBuilder withPeerList(final List<PeerInfo> peerList) {
            message.peerList = peerList;
            return this;
        }
        
        MessageBuilder withLightPeerList(final List<PeerInfo> lightPeerList) {
            message.lightPeerList = lightPeerList;
            return this;
        }
        
        MessageBuilder withTransaction(final List<BlockData> blockData) {
        	message.blockData = blockData;
        	return this;
        }
        
        MessageBuilder withMinIndex(final int minIndex) {
        	message.minIndex = minIndex;
        	return this;
        }
        
        Message build() {
            return message;
        }

    }
}
