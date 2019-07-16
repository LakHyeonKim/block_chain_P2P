package kr.ac.kumoh.s20120499.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ADD_LIGHTPEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ADD_PEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.DELET_PEER;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.END_MINE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.MAKE_BLOCK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.PEER_LIST_AND_LIGHTPEER_LIST;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.READY;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.REQ_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.VIRTUAL_PORT;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.VIRTUAL_INFO;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.VIRTUAL_INFO_RESPONSE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ALL_BLOCKCHAIN_CLEAR;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.ALL_BLOCKCHAIN_CLEAR_OK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.GET_BACKUP_CHAIN;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.GET_BACKUP_CHAIN_OK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.BACKUP_ACTIVITY;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.BACKUP_ACTIVITY_OK;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PeerServer{
	
	public static final int PORT = 8000;
	public static String peerServerIP;
	public static final int THREAD_CNT = 30;
	public static final int VIRTUAL_NODE_COUNT = 7;
    public static PriorityQueue<BlockData> transactionPool = new PriorityQueue<BlockData>();
    public static int transactionCount = 0;
	private static ExecutorService selectPeer = Executors.newFixedThreadPool(1);
	private static ExecutorService startPeerThreadPool = Executors.newFixedThreadPool(1);
	static List<PeerInfo> peerList = new ArrayList<>();
	static List<PeerInfo> lightPeerList = new ArrayList<>();
	static String virtualNodeName[] = new String[VIRTUAL_NODE_COUNT];
	static int virtualNodePort[] = new int[VIRTUAL_NODE_COUNT];
	static int lightNodeCountList[] = new int[VIRTUAL_NODE_COUNT];
	private static VirtualPeer[] vp = new VirtualPeer[VIRTUAL_NODE_COUNT]; 
	private static Thread[] thread = new Thread[VIRTUAL_NODE_COUNT];
	public static HashMap<String, Integer> transactionPosition = new HashMap<String, Integer>();
	private List<Integer> hashCounts = new ArrayList<Integer>();
	private List<String> blockchainLastHashCode = new ArrayList<String>();
	public static List<List<Block>> backUpBlockchains = new ArrayList<List<Block>>();
	private List<List<Block>> tempBlockchain = new ArrayList<List<Block>>();
	private List<List<PeerInfo>> virtualPeerInfoList = new ArrayList<List<PeerInfo>>();
	
	private Frame mainFrame;
    private Label headerLabel;
    private Panel controlPanel;
    private JButton serverKillButton = new JButton("SERVER_KILL");
    private JButton restore = new JButton("CHAIN_RESTORE");
    private JTabbedPane tab;
    private JTabbedPane tab2;
    private Label virtualNode = new Label();
    private Label lightNode = new Label();
    private Label selected = new Label();
    private JTextField lightPeerCount = new JTextField(15);
    private JTextField peerCount = new JTextField(15);
    private JTextField selectedPeer = new JTextField(25);
    private JTextArea restoredChain = new JTextArea(15,59);
    private JScrollPane rsp = new JScrollPane(restoredChain);
    private JTextArea[] table = new JTextArea[VIRTUAL_NODE_COUNT];
    private JScrollPane[] sp = new JScrollPane[VIRTUAL_NODE_COUNT];
    private JTable[] virtualInfo = new JTable[VIRTUAL_NODE_COUNT];
    private DefaultTableModel[] model = new DefaultTableModel[VIRTUAL_NODE_COUNT]; 
    private JScrollPane[] sp2 = new JScrollPane[VIRTUAL_NODE_COUNT];
    private ScheduledExecutorService reloadScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService reloadScheduler2 = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService reloadScheduler3 = Executors.newSingleThreadScheduledExecutor();
    
    int count = 0;
    
    public static boolean stopSelect = false;
    public static int buttonCount = -1;
    public static void main(String[] args) {
		setPeerServerIP();
		init();
		selectPeer.execute(new SelectPeer());
		startPeerThreadPool.execute(new StartPeerThreadPool());
		createVirtualNodes();
		startGUI();
	}
    
    
    public PeerServer() {
        prepareGUI();
    }
    
    private static void init() {
    	for(int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
			virtualNodeName[i] = "krh" + (i + 1);
			virtualNodePort[i] = 1001 + (4 * i);
			lightNodeCountList[i] = 0;
		}
    }
    
    
    
    private static void startGUI() {
    	PeerServer awtControlDemo = new PeerServer();
        awtControlDemo.showTextField();
    }
    
    private static void createVirtualNodes() {
    	//AgentManager firstAgent = new AgentManager();
		//firstAgent.fristAddAgent("firstAgent", 9787);
		//virtualNodeName[0] = "firstAgent";
		//virtualNodePort[0] = 9787;
		//lightNodeCountList[0] = 0;
		for(int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
			vp[i] = new VirtualPeer(virtualNodeName[i],virtualNodePort[i],i);
			thread[i] = new Thread(vp[i]);
			thread[i].start();
			if(i == 0) {
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
    }
    
    private static void setPeerServerIP() {
		try {
			Socket socket = new Socket("www.kumoh.ac.kr",80);
			peerServerIP = socket.getLocalAddress().getHostAddress();
			System.out.println("PEERSERVER IP ADDRESS: " + peerServerIP);
			socket.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	private void prepareGUI() {
        // Frame 에 대한 셋팅
        mainFrame = new Frame("BLOCKCHAIN SERVER");
        mainFrame.setSize(1400, 800);
        mainFrame.setLayout(new GridLayout(2, 2));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
 
        // 상단에 있는 라벨
        headerLabel = new Label();
        headerLabel.setAlignment(Label.CENTER);
        headerLabel.setText("B__L__O__C__K__C__H__A__I__N");
        
        controlPanel = new Panel();
        virtualNode.setText("VIRTUAL NODE COUNT: ");
        lightNode.setText("  LIGHT NODE COUNT: ");
        selected.setText("MINING SELECT: ");
        
        controlPanel.add(virtualNode);
        controlPanel.add(peerCount);
        controlPanel.add(lightNode);
        controlPanel.add(lightPeerCount);
        controlPanel.add(selected);
        controlPanel.add(selectedPeer);
        controlPanel.add(serverKillButton);
        controlPanel.add(restore);
        controlPanel.add(rsp);
        
        ActionListener eventHandler = 
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(e.getSource() == serverKillButton){
                        	PeerServer.buttonCount = PeerServer.buttonCount * -1;
                        	if(PeerServer.buttonCount == 1) {
                        		PeerServer.stopSelect = true;
                        		try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                        		broadcast(ALL_BLOCKCHAIN_CLEAR,null);
                        		JOptionPane.showMessageDialog(null, "Server is killed!!!", "Notice", JOptionPane.CLOSED_OPTION);
                        		serverKillButton.setText("__RESTART__");
                        		serverKillButton.setEnabled(false);
                        	}                                
                        	else {
                        		PeerServer.stopSelect = false;
                        		selectPeer.execute(new SelectPeer());
                        		JOptionPane.showMessageDialog(null, "Server is restart!!!", "Notice", JOptionPane.CLOSED_OPTION);
                        		serverKillButton.setText("SERVER_KILL");
                        	}
                        }else{
                        	broadcast(GET_BACKUP_CHAIN,null);
                        	broadcast(BACKUP_ACTIVITY,null);
                        	for(int i = 0; i < backUpBlockchains.size(); i++) {
                        		restoredChain.append("VN_" + i + ", size: " + backUpBlockchains.get(i).size() + "blockchain: "+ backUpBlockchains.get(i).toString() + "\n");
                        	}
                        	JOptionPane.showMessageDialog(null, "Restore END!!!", "Notice", JOptionPane.CLOSED_OPTION);
                        	serverKillButton.setEnabled(true);
                        }
                        
                    }
                };
                
        serverKillButton.addActionListener(eventHandler);
        restore.addActionListener(eventHandler);
        
        
        tab = new JTabbedPane(JTabbedPane.TOP);
        tab.setAlignmentX(JTabbedPane.BOTTOM_ALIGNMENT);
        
        tab2 = new JTabbedPane(JTabbedPane.TOP);
        tab2.setAlignmentX(JTabbedPane.BOTTOM_ALIGNMENT);
        String column[] = {"Light Node ID", "Address", "Port"};
        
        
        
        for(int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
        	table[i] = new JTextArea();
        	model[i] = new DefaultTableModel(column,0);
        	virtualInfo[i] = new JTable(model[i]);
            sp[i] = new JScrollPane(table[i]);
            sp2[i] = new JScrollPane(virtualInfo[i]);
            if( i == 0 ) {
            	tab.addTab("First Node", sp[i]);
            	tab2.addTab("First Node", sp2[i]);
            }
            else {
            	tab.addTab("Virtual Node" + i, sp[i]);
            	tab2.addTab("Virtual Node" + i, sp2[i]);
            }
        }
       
        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.add(tab2);
        mainFrame.add(tab);
        
        mainFrame.setVisible(true);
    }
 
    private void showTextField() {
    	
    	Runnable reloader = new Runnable() {
			@Override
			public void run() {
				broadcast(REQ_ALL_BLOCKS,null);
				broadcast(VIRTUAL_INFO,null);
        		for(int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
        			table[i].setText(null);
        			for(int j = 0; j < tempBlockchain.get(i).size(); j++) {
        				table[i].append(tempBlockchain.get(i).get(j).toString() + "\n");
        			}
        		}
        		tempBlockchain.clear();
			}
    	};
    	
    	Runnable reloader2 = new Runnable() {
			@Override
			public void run() {
				peerCount.setText(peerList.size() + "");
        		lightPeerCount.setText(lightPeerList.size() + "");
        		if(SelectPeer.getIndex() == 0)
        			selectedPeer.setText(" First Node selected");
        		else
        			selectedPeer.setText(" Virtual Node: " + SelectPeer.getIndex() + " selected");
			}
    	};
    	
    	Runnable reloader3 = new Runnable() {
			@Override
			public void run() {
				broadcast(VIRTUAL_INFO,null);
        		for(int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
        			model[i].setNumRows(0);
        			int arraySize = virtualPeerInfoList.get(i).size();
        			String data[] = new String[3];
        			for(int j = 0; j < arraySize; j++) {
        				data[0] = virtualPeerInfoList.get(i).get(j).getName();
        				data[1] = virtualPeerInfoList.get(i).get(j).getAddress();
        				data[2] = virtualPeerInfoList.get(i).get(j).getPort() + "";
        				model[i].addRow(data);
        			}
        		}
        		virtualPeerInfoList.clear();
			}
    	};
    	
    	reloadScheduler.scheduleWithFixedDelay(reloader, 0, 5, TimeUnit.SECONDS);
    	reloadScheduler2.scheduleWithFixedDelay(reloader2, 0, 1, TimeUnit.SECONDS);
    	reloadScheduler3.scheduleWithFixedDelay(reloader3, 0, 5, TimeUnit.SECONDS);
    }
    
    private void blockchainAgreement() {
    	//51% 이상의 validchain 확인
    }
    
    private void broadcast(Message.MESSAGE_TYPE type, final Block block) {
    	peerList.forEach(peer -> sendMessage(type, peer.getAddress(), peer.getPort(), block));
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
                    System.out.println(String.format("%d received: %s", PORT, msg.toString()));
                    if (READY == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withType(type)
                                .withReceiver(port)
                                .withSender(PORT)
                                .withBlocks(Arrays.asList(blocks)).build());
                    } else if (RSP_ALL_BLOCKS == msg.type) {
                    	tempBlockchain.add(msg.blocks);
                        break;
                    } else if (VIRTUAL_INFO_RESPONSE == msg.type) {
                    	virtualPeerInfoList.add(msg.peerList);
                    	break;
                    } else if (ALL_BLOCKCHAIN_CLEAR_OK == msg.type) {
                    	break;
                    } else if (GET_BACKUP_CHAIN_OK == msg.type) {
                    	break;
                    } else if (BACKUP_ACTIVITY_OK == msg.type) {
                    	break;
                    }
                }
            }
            peer.close();
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unknown host %s %d", host, port));
        } catch (IOException e) {
            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", PORT, port));
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

class StartPeerThreadPool implements Runnable{
	private static ExecutorService peerThreadPool = Executors.newFixedThreadPool(PeerServer.THREAD_CNT);
	ServerSocket serverSocket;
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(PeerServer.PORT);
			System.out.println("PEER_SERVER_START");
			while(true){
				Socket socket = serverSocket.accept();
				try{
					peerThreadPool.execute(new ConnectionWrap(socket,PeerServer.PORT));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		} catch(IOException e) {
				e.printStackTrace();
		}
	}
}
 
class VirtualPeer implements Runnable{
	private String name;
	private int port;
	private int threadNumber;
	
	
	public VirtualPeer(String name, int port, int threadNumber) {
		this.name = name;
		this.port = port;
		this.threadNumber = threadNumber;
	}
	@Override
	public void run() {
		if(threadNumber == 0) {
			AgentManager agent = new AgentManager();
			agent.fristAddAgent(name, port);
		}
		else {
			AgentManager agent = new AgentManager();
			agent.addAgent(name, port);
		}
	}
}

class SelectPeer implements Runnable{
	private Random randomPeer = new Random();
	private static int index;
	
	public static int getIndex() {
		return index;
	}
	@Override
	public void run() {
		try {
			Thread.sleep(1000);
    	} catch (InterruptedException e1) {
    		e1.printStackTrace();
    	}
		while(true)
		{
			System.out.print("");
			if(PeerServer.peerList.size() != PeerServer.VIRTUAL_NODE_COUNT)
				continue;
			synchronized(PeerServer.peerList) {
				if(PeerServer.peerList.size() == 0)
					continue;
				index = randomPeer.nextInt(PeerServer.peerList.size());
				System.out.println("SELECT: " + (index+1));
				try( final Socket peer = new Socket(PeerServer.peerList.get(index).getAddress(), PeerServer.peerList.get(index).getPort()+1);
						final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());) {
			        	out.writeObject(new Message.MessageBuilder().withType(MAKE_BLOCK).build());
			        	final ObjectInputStream in = new ObjectInputStream(peer.getInputStream());
			        	 Object fromPeer;
			             while ((fromPeer = in.readObject()) != null) {
			                 if (fromPeer instanceof Message) {
			                     final Message msg = (Message) fromPeer;
			                     System.out.println(String.format("%d received: %s", 8000, msg.toString()));
			                     if(END_MINE == msg.type) {
			                    	break;
			                     }
			                 }
			             }
			        	peer.close();
			        	if(PeerServer.stopSelect) break;
			    } catch (IOException | ClassNotFoundException e) {
			        e.printStackTrace();
			    }
			}
			//try {
			//	Thread.sleep(5000);
	    	//} catch (InterruptedException e1) {
	    	//	e1.printStackTrace();
	    	//}
		}
	}
}

class ConnectionWrap implements Runnable{

	private Socket socket = null;
	private int PORT;
	public ConnectionWrap(Socket socket, int port) {
		this.socket = socket;
		this.PORT = port;
	}

	@Override
	public void run() {
		try {
            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Object fromPeer = in.readObject();
            if (fromPeer instanceof Message) {
                final Message msg = (Message) fromPeer;
                System.out.println(String.format("[PeerServer] received: %s", msg.toString()));
                if(DELET_PEER == msg.type) {
                	synchronized(PeerServer.peerList) {
                		for(int i = 0; i < PeerServer.peerList.size(); i++)
                			if(PeerServer.peerList.get(i).getName().equals(msg.peerInfo.getName()))
                				PeerServer.peerList.remove(i);
                	}
                	System.out.println("DELET_PEER: " + msg.peerInfo.getName() + ", " + msg.peerInfo.getAddress() + ", " + msg.peerInfo.getPort());
                } else if(ADD_PEER == msg.type){
                	synchronized(PeerServer.peerList) {
                		PeerServer.peerList.add(msg.peerInfo);
                    }
                	System.out.println("ADD_PEER: " + msg.peerInfo.getName() + ", " + msg.peerInfo.getAddress() + ", " + msg.peerInfo.getPort());
                } else if(ADD_LIGHTPEER == msg.type) {
                	synchronized(PeerServer.lightPeerList) {
                		PeerServer.lightPeerList.add(msg.peerInfo);
                		int min = PeerServer.lightNodeCountList[0];
                		int minIndex = 0;
                		for(int i = 0; i < PeerServer.VIRTUAL_NODE_COUNT; i++)
                		{
                			if(min > PeerServer.lightNodeCountList[i])
                			{
                				min = PeerServer.lightNodeCountList[i];
                				minIndex = i;
                			}
                		}
                		PeerServer.lightNodeCountList[minIndex]++;
                		final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                		out.writeObject(new Message.MessageBuilder()
                				 .withType(VIRTUAL_PORT)
                				 .withPeerInfo(new PeerInfo(PeerServer.virtualNodeName[minIndex],PeerServer.peerServerIP,PeerServer.virtualNodePort[minIndex]))
                				 .build());
                	}
                	System.out.println("ADD_LIGHTPEER: " + msg.peerInfo.getName() + ", " + msg.peerInfo.getAddress() + ", " + msg.peerInfo.getPort());
                } 
            }
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} finally {
			try {
				broadcast(PeerServer.peerList,PeerServer.lightPeerList);
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("PEER COUNT: " + PeerServer.peerList.size() + "");
		System.out.println("LIGHT PEER COUNT: " + PeerServer.lightPeerList.size() + "");
	}
	
	private void broadcast(List<PeerInfo> peerList, List<PeerInfo> lightPeerList) {
		peerList.forEach(peer -> sendMessage(peer.getAddress(),peer.getPort(),peerList,lightPeerList));
    }
    
    private void sendMessage(String host, int port, List<PeerInfo> peerList, List<PeerInfo> lightPeerList) {
        try (
        		 final Socket peer = new Socket(host, port+1);
        		final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());) {
        	out.writeObject(new Message.MessageBuilder()
        			.withType(PEER_LIST_AND_LIGHTPEER_LIST)
        			.withSender(PORT)
        			.withReceiver(port)
        			.withPeerList(peerList).withLightPeerList(lightPeerList).build());
        	peer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
