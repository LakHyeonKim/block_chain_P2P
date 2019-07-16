package kr.ac.kumoh.s20120499.management;

import java.util.Scanner;

public class LightMain3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LightAgentManager la = new LightAgentManager();
		la.addAgent("rlafkrgus3", 1011);
		String message;
        Scanner scan = new Scanner(System.in);
        int count = 0;
        while(true) {
        	System.out.println("--------------------------------------------------------------------------------");
        	System.out.print(">> ");
        	message = scan.nextLine();
        	if(message.equals("tx"))
        		la.makeTransaction(new BlockData("rlafkrgus3"
        				, new PeerInfo(la.getLightAgent().getName(),la.getLightAgent().getAddress(),la.getLightAgent().getPort())
        				, "������"));
        	if(message.equals("ce"))
        		la.sendCertificationTransaction("������");
        	if(message.equals("de"))
        		la.deletLightAgent();
        	if(message.equals("in"))
        		la.addAgent("rlafkrgus3", 1011);
        	//System.out.println("--------------------------------------------------------------------------------");
        	//System.out.print(">> ");
        	//message = scan.nextLine();
        	/*while(true)
        	{
        		try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
        		if(la.check()) {System.out.println(">> ���� ����"); break;}
        		else System.out.println(">> ���� ����");
        	}*/
        }
	}

}
