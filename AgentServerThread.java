package kr.ac.kumoh.s20120499.management;

import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ADD_LIGHTPEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CERTIFICATION;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CERTIFICATION_FAILURE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CERTIFICATION_SUCCESS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.DELET_LIGHTPEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.END_ADD;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.INFO_NEW_BLOCK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.JUST_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.JUST_RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.READY;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.REQ_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.TRANSACTION;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.VIRTUAL_INFO;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.VIRTUAL_INFO_RESPONSE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ALL_BLOCKCHAIN_CLEAR;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ALL_BLOCKCHAIN_CLEAR_OK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.GET_BACKUP_CHAIN;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.GET_BACKUP_CHAIN_OK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.BACKUP_ACTIVITY;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.PriorityQueue;

public class AgentServerThread extends Thread {
    private Socket client;
    private Agent agent;

    AgentServerThread(Agent agent, final Socket client) {
        super(agent.getName() + System.currentTimeMillis());
        this.agent = agent;
        this.client = client;
    }

    @Override
    public void run() {
        try (
                final ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
            Message message = new Message.MessageBuilder().withSender(agent.getPort()).withType(READY).build();
            out.writeObject(message);
            message = null;
            Object fromClient;
            while ((fromClient = in.readObject()) != null) {
                if (fromClient instanceof Message) {
                    final Message msg = (Message) fromClient;
                    System.out.println(String.format("%d received: %s", agent.getPort(), fromClient.toString()));
                    if (INFO_NEW_BLOCK == msg.type) {
                        if (msg.blocks.isEmpty() || msg.blocks.size() > 1) {
                            System.err.println("Invalid block received: " + msg.blocks);
                        }
                        synchronized (agent) {
                            if(agent.addBlock(msg.blocks.get(0)) == 1) {
                            	message = new Message.MessageBuilder().withSender(agent.getPort()).withType(END_ADD).build();
                            	out.writeObject(message);
                            	agent.sendBlockchainPiece();
                            }
                        }
                        break;
                    } else if (REQ_ALL_BLOCKS == msg.type) {
                    	synchronized (agent) {
                    		out.writeObject(new Message.MessageBuilder()
                    				.withSender(agent.getPort())
                    				.withType(RSP_ALL_BLOCKS)
                    				.withBlocks(agent.getBlockchain())
                    				.build());
                    	}
                        break;
                    } else if (TRANSACTION == msg.type) {                    	
                    	synchronized(PeerServer.transactionPool) {
                    		BlockData data = msg.blockData.get(0);
                    		data.setIndex(PeerServer.transactionCount++);
                    		PeerServer.transactionPool.offer(data);
                    		System.out.println(PeerServer.transactionPool.toString() + "");
                    	}
                    	break;
                    } else if (JUST_ALL_BLOCKS == msg.type) {
                    	out.writeObject(new Message.MessageBuilder()
                                .withSender(agent.getPort())
                                .withType(JUST_RSP_ALL_BLOCKS)
                                .withBlocks(agent.getBlockchain())
                                .build());
                    	break;
                    } else if (ADD_LIGHTPEER == msg.type) {
                    	synchronized (agent) {
                    		agent.setMyLightPeerList(msg.peerInfo);
                    		agent.sendBlockchainPiece();
                    	}
                    	break;
                    } else if (DELET_LIGHTPEER == msg.type) {
                    	synchronized (agent) {
                    		agent.delMyLightPeerList(msg.peerInfo);
                    		synchronized(PeerServer.lightPeerList) {
                        		for(int i = 0; i < PeerServer.lightPeerList.size(); i++)
                        			if(PeerServer.lightPeerList.get(i).getName().equals(msg.peerInfo.getName()))
                        				PeerServer.lightPeerList.remove(i);
                        	}
                    		for(int i = 0; i < PeerServer.VIRTUAL_NODE_COUNT; i++) {
                    			if(agent.getPort() == PeerServer.virtualNodePort[i])
                    				PeerServer.lightNodeCountList[i]--;
                    		}
                    		agent.sendBlockchainPiece();
                    	}
                    	System.out.println("LIGHT PEER COUNT: " + PeerServer.lightPeerList.size() + "");
                    	System.out.println("LIGHT PEER COUNT: " + agent.getMyLightPeerList().size() + "");
                    	break;
                    } else if(CERTIFICATION == msg.type) {
                    	synchronized(PeerServer.transactionPosition) {
                    		System.out.println(msg.blockData.get(0).getHash());
                    		int index = PeerServer.transactionPosition.get(msg.blockData.get(0).getHash());
                    		Block block = agent.getBlockchain().get(index);
                    		PriorityQueue<BlockData> blockData = block.getBlockData();
                    		boolean exist = false;
                    		for(BlockData data:blockData) {
                    			if(data.getHash().equals(msg.blockData.get(0).getHash())) {
                    				exist = true;
                    				PeerServer.transactionPosition.remove(msg.blockData.get(0).getHash());
                    				break;
                    			}
                    		}
                    		if(exist) {
                    			sendMessage(CERTIFICATION_SUCCESS,msg.blockData.get(0).getPeerInfo().getAddress(),msg.blockData.get(0).getPeerInfo().getPort());
                    		} else {
                    			sendMessage(CERTIFICATION_FAILURE,msg.blockData.get(0).getPeerInfo().getAddress(),msg.blockData.get(0).getPeerInfo().getPort());
                    		}
                    		break;
                    	}
                    } else if(VIRTUAL_INFO == msg.type) {
                    	out.writeObject(new Message.MessageBuilder()
                                .withSender(agent.getPort())
                                .withType(VIRTUAL_INFO_RESPONSE)
                                .withPeerList(agent.getMyLightPeerList())
                                .build());
                    	break;
                    } else if(ALL_BLOCKCHAIN_CLEAR == msg.type) {
                    	synchronized(agent) {
                    		agent.deleteBlockchain();
                    	}
                    	out.writeObject(new Message.MessageBuilder()
                                .withSender(agent.getPort())
                                .withType(ALL_BLOCKCHAIN_CLEAR_OK)
                                .build());
                    	break;
                    } else if(GET_BACKUP_CHAIN == msg.type) {
                    	synchronized(agent) {
                    		agent.getBlockchainPiece();
                    	}
                    	out.writeObject(new Message.MessageBuilder()
                                .withSender(agent.getPort())
                                .withType(GET_BACKUP_CHAIN_OK)
                                .build());
                    	break;
                    } else if(BACKUP_ACTIVITY == msg.type) {
                    	synchronized(agent) {
                    		agent.setBlockchain();
                    	}
                    	break;
                    }
                }
            }
            client.close();
        } catch (ClassNotFoundException | IOException e) {
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
                    System.out.println(String.format("%d received: %s", agent.getPort(), msg.toString()));
                    if (READY == msg.type) {
                    	out.writeObject(new Message.MessageBuilder()
                                .withSender(agent.getPort())
                                .withType(type)
                                .build());
                        break;
                    }
                }
            }
            peer.close();
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unknown host %s %d", host, port));
        } catch (IOException e) {
            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", agent.getPort(), port));
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
