package kr.ac.kumoh.s20120499.management;

import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.END_MINE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.MAKE_BLOCK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.PEER_LIST_AND_LIGHTPEER_LIST;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.READY;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AgentPeerServerThread extends Thread {
	private Socket client;
	private Agent agent;

    AgentPeerServerThread(final Socket client, Agent agent) {
        this.client = client;
        this.agent = agent;
    }
    
    @Override
    public void run() {
        try (
                final ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
        	Object fromPeer = in.readObject();
            if (fromPeer instanceof Message) {
                final Message msg = (Message) fromPeer;
                if(PEER_LIST_AND_LIGHTPEER_LIST == msg.type)
                {
                	Agent.peerList = msg.peerList;
                	Agent.lightPeerList = msg.lightPeerList;
                }
                if(MAKE_BLOCK == msg.type)
                {
                	agent.createBlock();
                	while(true)
                	{
                		if(agent.getCount() == PeerServer.VIRTUAL_NODE_COUNT ) {
                			final ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                			Message message = new Message
                					.MessageBuilder()
                					.withSender(agent.getPort())
                					.withType(END_MINE)
                					.build();
                            out.writeObject(message);
                            agent.setCount(0);
                			break;
                		}
                	}
                }
            }
            //for(int i = 0; i < Agent.peerList.size(); i++) {
            	//PeerInfo peerinfo = Agent.peerList.get(i);
        		//System.out.println(String.format("%d received list: %s, PEER SIZE: %d", agent.getPort(), peerinfo.getName(), Agent.peerList.size()));
            //}
            client.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
