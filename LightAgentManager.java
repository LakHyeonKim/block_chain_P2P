package kr.ac.kumoh.s20120499.management;

import java.net.Socket;

public class LightAgentManager {
	private LightAgent lightAgent;
	
	public LightAgent addAgent(String name, int port) {
		String address = "";
		try {
			Socket socket = new Socket("www.kumoh.ac.kr",80);
			address = socket.getLocalAddress().getHostAddress();
			System.out.println("PEERSERVER IP ADDRESS: " + address);
			socket.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		lightAgent = new LightAgent(name, address, port);
		lightAgent.startHost(); 
        return lightAgent;
    }
	
	public void makeTransaction(BlockData blockData) {
		lightAgent.sendTransaction(blockData);
	}
	
	public void sendCertificationTransaction(String certificationTransaction) {
		lightAgent.sendCertificationTransaction(certificationTransaction);
	}
	
	public boolean checkTransaction() {
		return lightAgent.checkTransaction();
	}
	
	public boolean checkCertification() {
		return lightAgent.checkCertification();
	}
	
	public LightAgent getLightAgent() {
		return lightAgent;
	}
	
	public void deletLightAgent() {
		lightAgent.stopHost();
	}
}
