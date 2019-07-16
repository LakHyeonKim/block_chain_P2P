package kr.ac.kumoh.s20120499.management;

import java.util.Scanner;

public class LightMain1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LightAgentManager la = new LightAgentManager();
		la.addAgent("rlafkrgus1", 1003);
		String message;
		int count = 0;
        Scanner scan = new Scanner(System.in);
        while(true) {
        	System.out.println("--------------------------------------------------------------------------------");
        	System.out.print(">> ");
        	message = scan.nextLine();
        	if(message.equals("tx"))
        		la.makeTransaction(new BlockData("rlafkrgus1"
        				, new PeerInfo(la.getLightAgent().getName(),la.getLightAgent().getAddress(),la.getLightAgent().getPort())
        				, "±è¶ôÇö"));
        	if(message.equals("ce"))
        		la.sendCertificationTransaction("±è¶ôÇö");
        	if(message.equals("de"))
        		la.deletLightAgent();
        	if(message.equals("in"))
        		la.addAgent("rlafkrgus1", 1003);
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
        		if(la.check()) {System.out.println(">> ÄÁÆß ¼º°ø"); break;}
        		else System.out.println(">> ÄÁÆß ½ÇÆÐ");
        	}*/
        	
        }
	}

}
