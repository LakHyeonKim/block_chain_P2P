package kr.ac.kumoh.s20120499.management;

import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ADD_PEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CONFIRM;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.DELET_PEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.END_ADD;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.END_MINE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.INFO_NEW_BLOCK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.JUST_RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.READY;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.REQ_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.BLOCKCHAIN_PIECE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.GET_BLOCKCHAIN_PIECE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.SEND_BLOCKCHAIN_PIECE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.Collections;

public class Agent{

    private String name;
    private String address;
    private int port;
    private List<Block> blockchain = new ArrayList<>();
    private List<Block> backUpBlockchain = new ArrayList<>();
    public static List<PeerInfo> peerList= new ArrayList<>();
    public static List<PeerInfo> lightPeerList= new ArrayList<>();
    private List<PeerInfo> myLightPeerList = new ArrayList<>();
    private static int next = -1;
    private int count = 0;
    private ServerSocket serverSocket;
    private ServerSocket pserverSocket;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(15);
    private ScheduledThreadPoolExecutor pexecutor = new ScheduledThreadPoolExecutor(2);
    private HashMap<Integer, Integer> chainLengthInfo = new HashMap<Integer, Integer>();
    
    private PeerInfo peerInfo;
  
    private boolean listening = true;
    private boolean plistening = true;

    public Agent() {
    }

    Agent(final String name, final String address, final int port, final Block root) {
        this.name = name;
        this.address = address;
        this.port = port;
        blockchain.add(root);
    }
    
    Agent(final String name, final String address, final int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }
    
    public HashMap<Integer, Integer> getBlockchainLengthHesh () {
    	return this.chainLengthInfo;
    }
    
    public void clearBlockchainLengthHesh() {
    	this.chainLengthInfo.clear();
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

    public List<Block> getBlockchain() {
        return this.blockchain;
    }
    
    public void setBlockchain() {
    	this.blockchain = backUpBlockchain;
    }
    
    public void deleteBlockchain() {
    	this.blockchain.clear();
    }
    
    public List<PeerInfo> getPeerList() {
    	return this.peerList;
    }
    
    public int getCount() {
    	return this.count;
    }
    
    public void setCount(int count) {
    	this.count = count;
    }
    
    public void setMyLightPeerList(PeerInfo peerInfo) {
    	this.myLightPeerList.add(peerInfo);
    }
    
    public List<PeerInfo> getMyLightPeerList() {
    	return this.myLightPeerList;
    }
    
    public void delMyLightPeerList(PeerInfo peerInfo) {
    	for(int i = 0; i < myLightPeerList.size(); i++)
    	{
    		if(myLightPeerList.get(i).getName().equals(peerInfo.getName()))
    			myLightPeerList.remove(i);
    	}
    }
    
    public void sendBlockchainPiece() {
    	float blockSize = blockchain.size();
    	float peerSize = myLightPeerList.size();
    	int pieceSize;
    	boolean flage = false;
    	if(peerSize == 0) return;
    	if(blockSize % peerSize == 0)
    		pieceSize = (int)(blockSize / peerSize);
    	else
    		pieceSize = (int)(blockSize / peerSize + 0.9);
    	for(int i = 0; i < peerSize; i++) {
    		List<Block> temp = new ArrayList<>();
    		for(int j = 0; j < pieceSize; j++) {
        		temp.add(blockchain.get((pieceSize * i) + j));
        		if(blockSize - 1 == (pieceSize * i) + j) {flage = true; break;}
        	}
    		sendMessage(BLOCKCHAIN_PIECE, myLightPeerList.get(i).getAddress(),myLightPeerList.get(i).getPort(),temp);
    		if(flage) break;
    	}	
    }
    
    public void getBlockchainPiece() {
    	int peerSize = myLightPeerList.size();
    	for(int i = 0; i < peerSize; i++) {
    		sendMessage(GET_BLOCKCHAIN_PIECE,myLightPeerList.get(i).getAddress(),myLightPeerList.get(i).getPort());
    	}
    	Collections.sort(backUpBlockchain,new Block());
    	System.out.println("---------------------------------------" + backUpBlockchain.toString());
    	int check = isBlockchainValid();
    	if(check == 0) {
    		PeerServer.backUpBlockchains.add(backUpBlockchain);
    	}
    	else System.out.println("Error code: " + check);
    }

	Block createBlock() {
        if (blockchain.isEmpty()) {
            return null;
        }

        Block previousBlock = getLatestBlock();
        if (previousBlock == null) {
            return null;
        }

        final int index = previousBlock.getIndex() + 1;
        synchronized(PeerServer.transactionPool) {
        	try {
        		final Block block = new Block(index, previousBlock.getHash(), name, PeerServer.transactionPool);
        		System.out.println(String.format("%s created new block %s", name, block.toString()));
        		broadcast(INFO_NEW_BLOCK, END_MINE, CONFIRM, block, PeerServer.transactionPool);
        		int size = PeerServer.transactionPool.size();
        		for(int i = 0; i < size; i++)
        		{
        			BlockData data = PeerServer.transactionPool.poll();
        			if(data.getCertification())
        			{
        				PeerServer.transactionPosition.put(data.getHash(),index);
        			}
        		}
        		PeerServer.transactionPool.clear();
        		return block;
        	}catch(Exception e)
        	{
        		System.out.println("CRAETE BLOCK ERROR");
        		return null;
        	}
        }
    }
    

    int addBlock(Block block) {
        if (isBlockValid(block)) {
            blockchain.add(block);
            return 1;
        }else if(block.getIndex() > getLatestBlock().getIndex()) {
        	broadcast(REQ_ALL_BLOCKS, null);
        	return 1;
        }else
        	return 1;
        /*else {
        	//broadcast(JUST_ALL_BLOCKS, null);
        	return -1;
        }*/
    }
    
    void startPeerServerHost() {
    	pexecutor.execute(() -> {
    		try{
    			pserverSocket = new ServerSocket(port+1);
    			System.out.println(String.format("PeerServer %s started", pserverSocket.getLocalPort()));
    			
    			Socket peerServerClient = new Socket(PeerServer.peerServerIP,PeerServer.PORT);
    			final ObjectOutputStream out = new ObjectOutputStream(peerServerClient.getOutputStream());
    			peerInfo = new PeerInfo(this.getName(),this.getAddress(),this.getPort());
    			out.writeObject(new Message.MessageBuilder().withType(ADD_PEER).withPeerInfo(peerInfo).withSender(this.getPort()).withReceiver(PeerServer.PORT).build());
    			peerServerClient.close();
    			
    			plistening = true;
    			while (plistening) {
    				final AgentPeerServerThread thread = new AgentPeerServerThread(pserverSocket.accept(), this);
    				thread.start();
    				try {
    	                thread.join();
    	                next = 1;    	              
    	            } catch (InterruptedException e1) {
    	                e1.printStackTrace();
    	            }
    			}
    			pserverSocket.close();
    		} catch (IOException e) {
    			System.err.println("Could not listen to port " + (port+1));
    		} 
    	});
    }

    void startHost() {
        executor.execute(() -> {
            try{
            	serverSocket = new ServerSocket(port);
                System.out.println(String.format("Server %s started", serverSocket.getLocalPort()));
                listening = true;
                while (listening) {
                    final AgentServerThread thread = new AgentServerThread(Agent.this, serverSocket.accept());
                    thread.start();
                }
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Could not listen to port " + port);
            } 
        });
        while(!(next > 0)) {System.out.print("");}
        broadcast(REQ_ALL_BLOCKS, null);
    }

    void stopHost() {
        listening = false;
        try {
        	Socket peerServerClient = new Socket(PeerServer.peerServerIP,PeerServer.PORT);
			final ObjectOutputStream out = new ObjectOutputStream(peerServerClient.getOutputStream());
			peerInfo = new PeerInfo(this.getName(),this.getAddress(),this.getPort());
			out.writeObject(new Message.MessageBuilder().withType(DELET_PEER).withPeerInfo(peerInfo).withSender(this.getPort()).withReceiver(PeerServer.PORT).build());
			peerServerClient.close();
			
            serverSocket.close();
            pserverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Block getLatestBlock() {
        if (blockchain.isEmpty()) {
            return null;
        }
        return blockchain.get(blockchain.size() - 1);
    }
    
    private int isBlockchainValid() {
    	if(backUpBlockchain.isEmpty())
    		return 1;
    	int size = backUpBlockchain.size();
    	for(int i = 1; i < size; i++) {
    		final Block block = backUpBlockchain.get(i - 1);
    		final Block latestBlock = backUpBlockchain.get(i);
            if (latestBlock == null) {
                return 2;
            }
            final int expected = block.getIndex() + 1;
            if (latestBlock.getIndex() != expected) {
                System.out.println(String.format("Invalid index. Expected: %s Actual: %s", expected, block.getIndex()));
                return 3;
            }
            if (!Objects.equals(block.getHash(), latestBlock.getPreviousHash())) {
            	if(i == 1)
                {
                	try {
                		block.setHash(Block.generateHash(Block.calculateHash(String.valueOf(block.getIndex()) + block.getPreviousHash() + String.valueOf(block.getTimestamp()))));
                		backUpBlockchain.set(i - 1, block);
                	}catch (Exception e) {
                		System.out.println("Hash certification error");
                	}
                } else {
                	try {
                		block.setHash(Block.generateHash(Block.calculateHash(String.valueOf(block.getIndex()) + block.getPreviousHash() + String.valueOf(block.getTimestamp())) + block.getBlockData().toString()));
                		backUpBlockchain.set(i - 1, block);
                	}catch (Exception e) {
                		System.out.println("Hash certification error");
                	}
                }
                latestBlock.setPreviousHash(block.getHash());
                backUpBlockchain.set(i, latestBlock);
                if(i == size - 1) {
                	try {
                		block.setHash(Block.generateHash(Block.calculateHash(String.valueOf(block.getIndex()) + block.getPreviousHash() + String.valueOf(block.getTimestamp())) + block.getBlockData().toString()));
                		backUpBlockchain.set(i, block);
                	}catch (Exception e) {
                		System.out.println("Hash certification error");
                	}
                }
                System.out.println("Unmatched hash code");
            }
            try {
            	if(!Block.validate(block.getHash())) {
            		System.out.println("Invalidate hash code");
            		return 4;
            	}
            }catch (Exception e) {
            	System.out.println("Verification failed");
            	return 5;
            }
    	}
    	return 0;
    }
    

    private boolean isBlockValid(final Block block) {
        final Block latestBlock = getLatestBlock();
        if (latestBlock == null) {
            return false;
        }
        final int expected = latestBlock.getIndex() + 1;
        if (block.getIndex() != expected) {
            System.out.println(String.format("Invalid index. Expected: %s Actual: %s", expected, block.getIndex()));
            return false;
        }
        if (!Objects.equals(block.getPreviousHash(), latestBlock.getHash())) {
            System.out.println("Unmatched hash code");
            return false;
        }
        try {
        	if(!Block.validate(block.getHash())) {
        		System.out.println("Invalidate hash code");
        		return false;
        	}
        }catch (Exception e) {
        	System.out.println("Verification failed");
        	return false;
        }
        return true;
    }

    private void broadcast(Message.MESSAGE_TYPE type, final Block block) {
    	peerList.forEach(peer -> sendMessage(type, peer.getAddress(), peer.getPort(), block));
    }
    
    private void broadcast(Message.MESSAGE_TYPE type1, Message.MESSAGE_TYPE type2, Message.MESSAGE_TYPE type3, final Block block, final PriorityQueue<BlockData> transactionPool) {
    	peerList.forEach(peer -> sendMessage(type1, peer.getAddress(), peer.getPort(), block));
    	//lightPeerList.forEach(lightPeer -> sendMessage(type2, lightPeer.getAddress(), lightPeer.getPort(), block));
    	transactionPool.forEach(toLightPeer -> sendMessage(type3, toLightPeer.getPeerInfo().getAddress(), toLightPeer.getPeerInfo().getPort(), toLightPeer));
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
                    } else if (RSP_ALL_BLOCKS == msg.type) {
                    	List<Block> tempBlockchain = new ArrayList<>(msg.blocks);
                    	if(blockchain.size() < tempBlockchain.size())
                            blockchain = tempBlockchain;
                        break;
                    } else if (JUST_RSP_ALL_BLOCKS == msg.type) {
                    	List<Block> tempBlockchain = new ArrayList<>(msg.blocks);
                    	blockchain = tempBlockchain;
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
    
    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port) {
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
                                .withSender(this.port).build());
                    } else if(SEND_BLOCKCHAIN_PIECE == msg.type) {
                    	backUpBlockchain.addAll(msg.blocks);
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
    
    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port, List<Block> blockchainPiece) {
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
                                .withBlocks(blockchainPiece).build());
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

    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port, Block... blocks) {
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
                                .withBlocks(Arrays.asList(blocks)).build());
                    } else if (RSP_ALL_BLOCKS == msg.type) {
                    	List<Block> tempBlockchain = new ArrayList<>(msg.blocks);
                    	if(blockchain.size() < tempBlockchain.size())
                            blockchain = tempBlockchain;
                        break;
                    } else if(END_ADD == msg.type) {
                    	//chainLength.put(msg.sender, msg.blockchainLength);
                    	++count;
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
