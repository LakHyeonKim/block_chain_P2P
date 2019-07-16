package kr.ac.kumoh.s20120499.management;

public class DummyLightNode {
	private static final int dummyLightNodeCount = 50;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LightAgentManager la[] = new LightAgentManager[dummyLightNodeCount];
		for(int i = 0; i < dummyLightNodeCount; i++) {
			la[i] = new LightAgentManager();
			la[i].addAgent("dummy_" + i, 9000 + i);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}
