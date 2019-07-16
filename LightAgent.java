package kr.ac.kumoh.s20120499.management;

import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ADD_LIGHTPEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CERTIFICATION;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.DELET_LIGHTPEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.INFO_NEW_BLOCK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.READY;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.REQ_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.TRANSACTION;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.VIRTUAL_PORT;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class LightAgent{

    private String name;
    private String address;
    private int port;
    private PeerInfo virtualNode;
    public static boolean confirm = false;
    public static boolean endMine = false;
    public static boolean certificationSuccess = false;
    public static BlockData data = null;

    private ServerSocket serverSocket;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    
    private PeerInfo peerInfo;
    private static final int PEERSERVERPORT = 8000;
    private static final String peerServerIP = "localhost";
    private List<Block> blockchainPiece = new ArrayList<>();

    private boolean listening = true;

    public LightAgent() {
    }
    
    LightAgent(final String name, final String address, final int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }
    
    public void setBlockchainPiece(List<Block> blockchainPiece) {
    	this.blockchainPiece = blockchainPiece;
    }
    
    public List<Block> getBlockchainPiece(){
    	return this.blockchainPiece;
    }

    void startHost() {
        executor.execute(() -> {
            try{
            	serverSocket = new ServerSocket(port);
                System.out.println(String.format("Server %s started", serverSocket.getLocalPort()));
                
            	Socket peerServerClient = new Socket(peerServerIP,PEERSERVERPORT);
    			final ObjectOutputStream out = new ObjectOutputStream(peerServerClient.getOutputStream());
    			peerInfo = new PeerInfo(this.getName(),this.getAddress(),this.getPort());
    			out.writeObject(new Message.MessageBuilder().withType(ADD_LIGHTPEER).withPeerInfo(peerInfo).withSender(this.getPort()).withReceiver(PEERSERVERPORT).build());
    			final ObjectInputStream in = new ObjectInputStream(peerServerClient.getInputStream());
                Object fromPeer = in.readObject();
                if (fromPeer instanceof Message) {
                    final Message msg = (Message) fromPeer;
                    if(VIRTUAL_PORT == msg.type)
                    {
                    	this.virtualNode = msg.peerInfo;
                    	Socket s = new Socket(peerServerIP,this.virtualNode.getPort());
            			final ObjectOutputStream sout = new ObjectOutputStream(s.getOutputStream());
            			peerInfo = new PeerInfo(this.getName(),this.getAddress(),this.getPort());
            			sout.writeObject(new Message.MessageBuilder()
            					.withType(ADD_LIGHTPEER)
            					.withPeerInfo(peerInfo)
            					.withSender(this.getPort())
            					.withReceiver(this.virtualNode.getPort()).build());
            			try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
            			s.close();
                    }
                }
    			peerServerClient.close();
    			
                listening = true;
                while (listening) {
                    final LightAgentServerThread thread = new LightAgentServerThread(LightAgent.this, serverSocket.accept());
                    thread.start();
                }
                serverSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Could not listen to port " + port);
            } 
        });
    }

    void stopHost() {
        listening = false;
        try {
        	
        	Socket peerServerClient = new Socket(peerServerIP,this.virtualNode.getPort());
			final ObjectOutputStream out = new ObjectOutputStream(peerServerClient.getOutputStream());
			peerInfo = new PeerInfo(this.getName(),this.getAddress(),this.getPort());
			out.writeObject(new Message.MessageBuilder().withType(DELET_LIGHTPEER).withPeerInfo(peerInfo).withSender(this.getPort()).withReceiver(this.virtualNode.getPort()).build());
			try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
			peerServerClient.close();
			
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void sendTransaction(BlockData blockData) {
    	data = blockData;
    	sendToMyVirtualNode(TRANSACTION,data);
    }
    
    void sendCertificationTransaction(String certificationTransaction) {
    	BlockData blockData = new BlockData(certificationTransaction, new PeerInfo(this.getName(),this.getAddress(),this.getPort()));
    	sendToMyVirtualNode(CERTIFICATION, blockData);
    }
    
    boolean checkTransaction() {
    	if(confirm) {
    		confirm = false;      // Æ®·£Àè¼Ç ÄÁÆß ¼º°ø
    		data = null;
    		return true;
    	}
    	else  
    		return false;             // ½ÇÆÐ
    }
    
    boolean checkCertification() {
    	if(certificationSuccess) {
    		certificationSuccess = false;
    		return true;
    	}
    	else return false;
    }

    private void sendToMyVirtualNode(Message.MESSAGE_TYPE type, final BlockData blockData) {
    	sendMessage(type,virtualNode.getAddress(),virtualNode.getPort(),blockData);
    	//fullNodePeerList.forEach(peer -> sendMessage(type, peer.getAddress(), peer.getPort(), blockData));
    }

    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port, BlockData... blockData) {
        try (
                final Socket peer = new Socket(host, port);
        		final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(peer.getInputStream())) {
            Object fromPeer;
            while ((fromPeer = in.readObject()) != null) {
                if (fromPeer instanceof Message) {
                    final Message msg = (Message) fromPeer;
                    System.out.println(String.format("%d received: %s", this.port, msg.toString()));
                    if (READY == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withType(type)
                                .withReceiver(port)
                                .withSender(this.port)
                                .withTransaction(Arrays.asList(blockData)).build());
                        break;
                    }
                }
            }
            peer.close();
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unknown host %s %d", host, port));
        } catch (IOException e) {
            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", getPort(), port));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}