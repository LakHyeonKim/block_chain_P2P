package kr.ac.kumoh.s20120499.management;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main1 {
	private static ExecutorService contraler = Executors.newFixedThreadPool(1);
	public static int num = 0;
	
	public static void main(String[] args) {
		String message;
		Scanner scan = new Scanner(System.in);  
	    contraler.execute(new Activity());
		System.out.println("--------------------------------------------------------------------------------");
		System.out.print(">> ");
		message = scan.nextLine();
		if(message.equals("ex"))
		{
			num = -1;
			System.out.println("Á¾·á");
		}
	}
	
}

class Activity implements Runnable{
	
	@Override
	public void run() {
		AgentManager a = new AgentManager();
		a.fristAddAgent("krh1", 1001);
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
			if(Main1.num == -1) {
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
