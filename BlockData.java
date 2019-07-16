package kr.ac.kumoh.s20120499.management;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockData implements Serializable, Comparable<BlockData> {
	  private static final long serialVersionUID = 11L;

	    private int index;                  // 트랜잭션 번호
	    private Long timestamp;             // 게시글 등록 시간
	    private String hash;
	    private PeerInfo peerInfo;
	    private String creator;
	    private boolean certification = false;
	    
	    private String sellerID;            // 판매자 ID
	    private String sellerName;          // 판매자 이름 
	    private int sellerPhoneNum;         // 판매자 전화번호
	    private int postNum;                // 게시글 번호
	    private String postText;            // 게시글 정보
	    private String productPhotoHash1;   // 제품 사진 해쉬 값1 (이전에 등록된 사진)
	    private String productPhotoHash2;   // 제품 사진 해쉬 값2 (현재 제품 사진)
	    
	    private String buyerID;             // 구매자 ID
	    private String buyerName;           // 구매자 이름
	    private int buyerPhoneNum;          // 구매자 전화번호
	    
	    private String depositPhotoHash;    // 입금 사진 해쉬 값
	   
	    private int invoiceNum;             // 송장 번호
	    private String invoicePhotoHash;    // 택배 발송 사진 해쉬 값
	    
	    private String receptionPhotoHash;  // 수취 인증 사진 해쉬 값

	    // for jackson
	    public BlockData() {
	    }
	    
	    public BlockData(String certificationTransaction, PeerInfo peerInfo) {
	    	this.peerInfo = peerInfo;
	    	hash = calculateHash(certificationTransaction);
	    }
	    
	    public BlockData(String creator, PeerInfo peerInfo, String productPhotoHash1) {
	    	this.creator = creator;
	        this.peerInfo = peerInfo;
	        this.productPhotoHash1 = productPhotoHash1;
	        this.certification = true;
	        timestamp = System.currentTimeMillis();
	        hash = calculateHash(productPhotoHash1);
	    }
	    
	    public BlockData(String sellerID, String sellerName, int sellerPhoneNum, int postNum, String postText, String productPhotoHash1,String productPhotoHash2, PeerInfo peerInfo) {
	        this.sellerID = sellerID;
	        this.sellerName = sellerName;
	        this.sellerPhoneNum = sellerPhoneNum;
	        this.postNum = postNum;
	        this.postText = postText;
	        this.productPhotoHash1 = productPhotoHash1;
	        this.productPhotoHash2 = productPhotoHash2;
	        this.peerInfo = peerInfo;
	        this.timestamp = System.currentTimeMillis();
	        hash = calculateHash(sellerID 
	        		+ sellerName 
	        		+ String.valueOf(sellerPhoneNum) 
	        		+ String.valueOf(postNum) 
	        		+ String.valueOf(timestamp) 
	        		+ postText 
	        		+ productPhotoHash1
	        		+ productPhotoHash2
	        		+ peerInfo);
	    }
	    
	    public BlockData(String buyerID, String buyerName, int buyerPhoneNum, PeerInfo peerInfo) {
	    	this.buyerID = buyerID;
	    	this.buyerName = buyerName;
	    	this.buyerPhoneNum = buyerPhoneNum;
	    	this.peerInfo = peerInfo;
	        timestamp = System.currentTimeMillis();
	        hash = calculateHash(buyerID
	        		+ buyerName
	        		+ String.valueOf(buyerPhoneNum)
	        		+ String.valueOf(timestamp)
	        		+ peerInfo);
	    }
	    
	    public BlockData(String buyerID, String buyerName, int buyerPhoneNum, String depositPhotoHash, PeerInfo peerInfo) {
	    	this.buyerID = buyerID;
	    	this.buyerName = buyerName;
	    	this.buyerPhoneNum = buyerPhoneNum;
	    	this.depositPhotoHash = depositPhotoHash;
	    	this.peerInfo = peerInfo;
	    	timestamp = System.currentTimeMillis();
	    	hash = calculateHash(buyerID
	    			+ buyerName
	    			+ String.valueOf(buyerPhoneNum)
	    			+ String.valueOf(timestamp)
	    			+ depositPhotoHash
	    			+ peerInfo);
	    }
	    
	    public BlockData(String sellerID, String sellerName, int sellerPhoneNum, int invoiceNum, String invoicePhotoHash, PeerInfo peerInfo) {
	    	this.sellerID = sellerID;
	    	this.sellerName = sellerName;
	    	this.sellerPhoneNum = sellerPhoneNum;
	    	this.invoiceNum = invoiceNum;
	    	this.invoicePhotoHash = invoicePhotoHash;
	    	this.peerInfo = peerInfo;
	    	timestamp = System.currentTimeMillis();
	    	hash = calculateHash(sellerID
	    			+ sellerName
	    			+ String.valueOf(sellerPhoneNum)
	    			+ String.valueOf(invoiceNum)
	    			+ String.valueOf(timestamp)
	    			+ invoicePhotoHash
	    			+ peerInfo);
	    }
	    
	    public BlockData(String sellerID, String sellerName, int sellerPhoneNum, String buyerID, String buyerName, int buyerPhoneNum, String receptionPhotoHash, PeerInfo peerInfo) {
	    	this.sellerID = sellerID;
	    	this.sellerName = sellerName;
	    	this.sellerPhoneNum = sellerPhoneNum;
	    	this.buyerID = buyerID;
	    	this.buyerName = buyerName;
	    	this.buyerPhoneNum = buyerPhoneNum;
	    	this.receptionPhotoHash = receptionPhotoHash;
	    	this.peerInfo = peerInfo;
	    	timestamp = System.currentTimeMillis();
	    	hash = calculateHash(sellerID
	    			+ sellerName
	    			+ String.valueOf(sellerPhoneNum)
	    			+ String.valueOf(buyerPhoneNum)
	    			+ String.valueOf(timestamp)
	    			+ buyerID
	    			+ buyerName
	    			+ receptionPhotoHash
	    			+ peerInfo);
	    }

	    @Override
	    public String toString() {
	        return "BlockData{" +
	                "index=" + index +
	                ", timestamp=" + timestamp +
	                ", creator=" + creator +
	                ", hash='" + hash + '\'' +
	                ", peerInfo=" + peerInfo +
	                ", sellerID=" + sellerID +
	                ", sellerName=" + sellerName +
	                ", sellerPhoneNum=" + sellerPhoneNum +
	                ", postNum=" + postNum +
	                ", postText=" + postText +
	                ", productPhotoHash1='" + productPhotoHash1 + '\'' +
	                ", productPhotoHash2='" + productPhotoHash2 + '\'' +
	                ", buyerID=" + buyerID +
	                ", buyerName=" + buyerName +
	                ", buyerPhoneNum=" + buyerPhoneNum +
	                ", depositPhotoHash='" + depositPhotoHash + '\'' +
	                ", invoiceNum=" + invoiceNum +
	                ", invoicePhotoHash='" + invoicePhotoHash + '\'' +
	                ", receptionPhotoHash='" + receptionPhotoHash + '\'' +
	                '}';
	    }

	    
	    @Override
	    public int compareTo(BlockData blockData) {
	        if (this.timestamp > blockData.timestamp) {
	            return 1;
	        } else if (this.timestamp < blockData.timestamp) {
	            return -1;
	        }
	        return 0;
	    }


	    public String getCreator() {
	        return creator;
	    }

	    public int getIndex() {
	        return index;
	    }
	    
	    public void setIndex(int index) {
	    	this.index = index;
	    }

	    public long getTimestamp() {
	        return timestamp;
	    }

	    public String getHash() {
	        return hash;
	    }
	    
	    public PeerInfo getPeerInfo() {
	    	return peerInfo;
	    }
	    
	    public boolean getCertification() {
	    	return certification;
	    }

	    private String calculateHash(String text) {
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
