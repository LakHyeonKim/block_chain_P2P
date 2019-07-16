package kr.ac.kumoh.s20120499.management;

import java.util.List;

public class AgentManager {

	private Agent agent;
    private Block root;


    public Agent fristAddAgent(String name, int port) {
    	try {
    		root = new Block(0, "ROOT_HASH", "ROOT");
    	}catch(Exception e) {
    		System.out.println("CREATE BLOCK ERROR");
    	}
    	agent = new Agent(name, "localhost", port, root);
    	agent.startPeerServerHost();
    	agent.startHost();
        return agent;
    }
    
    public Agent addAgent(String name, int port) {
    	agent = new Agent(name, "localhost", port);
    	agent.startPeerServerHost();
    	agent.startHost(); 
        return agent;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public List<PeerInfo> getAllAgents() {
        return this.agent.getPeerList();
    }

    public void deleteAgent() {
        final Agent agent = getAgent();
        if (agent != null) 
        	agent.stopHost();
    }

    public List<Block> getAgentBlockchain() {
        final Agent agent = getAgent();
        if (agent != null)
            return agent.getBlockchain();
        return null;
    }

    public Block createBlock() {
        final Agent agent = getAgent();
        if (agent != null)
            return agent.createBlock();
        return null;
    }
}
