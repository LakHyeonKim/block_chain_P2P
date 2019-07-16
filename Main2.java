package kr.ac.kumoh.s20120499.management;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main2 {
	private static ExecutorService contraler = Executors.newFixedThreadPool(1);
	public static int num2 = 0;
	
	public static void main(String[] args) {
		String message;
		Scanner scan = new Scanner(System.in);  
	    contraler.execute(new Activity2());
		System.out.println("--------------------------------------------------------------------------------");
		System.out.print(">> ");
		message = scan.nextLine();
		if(message.equals("ex"))
		{
			num2 = -1;
			System.out.println("Á¾·á");
		}
	}
	
}

class Activity2 implements Runnable{
	
	@Override
	public void run() {
		AgentManager a = new AgentManager();
		a.addAgent("krh2", 1005);
		/*while(true) {
			System.out.print("");
			List<Block> blockchain = a.getAgentBlockchain();
    		for(int i = 0; i < blockchain.size(); i++)
    		{
    			System.out.println(blockchain.get(i).toString());
    		}
    		try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
			if(Main2.num2 == -1) {
				a.deleteAgent();
        		for(int i = 0; i < blockchain.size(); i++)
        		{
        			System.out.println(blockchain.get(i).toString());
        		}
				break;
			}
				
		}*/
	}
	
}