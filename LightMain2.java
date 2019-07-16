package kr.ac.kumoh.s20120499.management;

import java.util.Scanner;

public class LightMain2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LightAgentManager la = new LightAgentManager();
		 
		la.addAgent("rlafkrgus2", 1007);
		String message;
        Scanner scan = new Scanner(System.in);
        int count = 0;
        while(true) {
        	System.out.println("--------------------------------------------------------------------------------");
        	System.out.print(">> ");
        	message = scan.nextLine();
        	if(message.equals("tx"))
        		la.makeTransaction(new BlockData("rlafkrgus2"
        				, new PeerInfo(la.getLightAgent().getName(),la.getLightAgent().getAddress(),la.getLightAgent().getPort())
        				, "문용성"));
        	if(message.equals("ce"))
        		la.sendCertificationTransaction("문용성");
        	if(message.equals("de"))
        		la.deletLightAgent();
        	if(message.equals("in"))
        		la.addAgent("rlafkrgus2", 1007);
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
        		if(la.check()) {System.out.println(">> 컨펌 성공"); break;}
        		else System.out.println(">> 컨펌 실패");
        	}*/
        }
	}

}
