package kr.ac.kumoh.s20120499.management;

import java.io.Serializable;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Block implements Serializable, Comparator<Block> {
    private static final long serialVersionUID = 1L;
    private static final int NUM_BITS = 18;

    private int index;
    private Long timestamp;
    private String hash;
    private String previousHash;
    private String merkleRootHash;
    private String creator;
    private ArrayList<ArrayList<String>> merkleTree;
    private PriorityQueue<BlockData> data;

    // for jackson
    public Block() {
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                ", timestamp=" + timestamp +
                ", creator=" + creator +
                ", hash='" + hash + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", merkleRootHash='" + merkleRootHash + '\'' +
                ", merkleTree'" + merkleTree + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
    
    @Override 
    public int compare(Block a, Block b) { 
    	return a.getIndex() < b.getIndex() ? -1 : a.getIndex() > b.getIndex() ? 1:0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Block block = (Block) o;
        return index == block.index
                && timestamp.equals(block.timestamp)
                && hash.equals(block.hash)
                && previousHash.equals(block.previousHash)
                && creator.equals(block.creator)
                && data.equals(block.data)
                && merkleRootHash.equals(block.merkleRootHash);
    }
    
    public static boolean validate(String hash) throws NoSuchAlgorithmException, DigestException {
        return Hash.valid(NUM_BITS, hash);
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + hash.hashCode();
        result = 31 * result + previousHash.hashCode();
        result = 31 * result + creator.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + merkleRootHash.hashCode();
        return result;
    }
    
    public Block(int index, String preHash, String creator) throws NoSuchAlgorithmException, DigestException{
        this.index = index;
        this.previousHash = preHash;
        this.creator = creator;
        timestamp = System.currentTimeMillis();
        hash = generateHash(calculateHash(String.valueOf(index) + previousHash + String.valueOf(timestamp)));
    }

    public Block(int index, String preHash, String creator, PriorityQueue<BlockData> data) 
    		throws NoSuchAlgorithmException, DigestException{
    	this.merkleTree = new ArrayList<ArrayList<String>>();
        this.index = index;
        this.previousHash = preHash;
        this.creator = creator;
        this.data = data;
        this.merkleRootHash = getMerkleRoot(data);
        timestamp = System.currentTimeMillis();
        hash = generateHash(calculateHash(String.valueOf(index) + previousHash + String.valueOf(timestamp) + data.toString()));
    }
    
    private String getMerkleRoot(PriorityQueue<BlockData> transactions) {
		int count = transactions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<String>();
		for(BlockData transaction : transactions) {
			previousTreeLayer.add(transaction.getHash());
		}
		merkleTree.add(previousTreeLayer);
		ArrayList<String> treeLayer = previousTreeLayer;
		while(count > 1) {
			treeLayer = new ArrayList<String>();
			for(int i=1; i < previousTreeLayer.size(); i++) {
				treeLayer.add(calculateHash(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
			merkleTree.add(previousTreeLayer);
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}

    public String getCreator() {
        return creator;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
    	this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }
    
    public void setPreviousHash(String previousHash) {
    	this.previousHash = previousHash;
    }
    
    public static String generateHash(String text)
            throws NoSuchAlgorithmException, DigestException {

        String resource = text;
        return Hash.generate(NUM_BITS, resource);
    }
    
    public PriorityQueue<BlockData> getBlockData() {
    	return data;
    }

    public static String calculateHash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }

        final byte bytes[] = digest.digest(text.getBytes());
        final StringBuilder hexString = new StringBuilder();
        for (final byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}