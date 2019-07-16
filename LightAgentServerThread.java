package kr.ac.kumoh.s20120499.management;

import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CERTIFICATION_FAILURE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CERTIFICATION_SUCCESS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.CONFIRM;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.END_MINE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.INFO_NEW_BLOCK;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.READY;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.REQ_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.RSP_ALL_BLOCKS;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.BLOCKCHAIN_PIECE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.GET_BLOCKCHAIN_PIECE;
import static kr.ac.kumoh.s20120499.management.Message.MESSAGE_TYPE.SEND_BLOCKCHAIN_PIECE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class LightAgentServerThread extends Thread {
    private Socket client;
    private LightAgent lightAgent;

    LightAgentServerThread(LightAgent lightAgent, final Socket client) {
        super(lightAgent.getName() + System.currentTimeMillis());
        this.lightAgent = lightAgent;
        this.client = client;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
            Message message = new Message.MessageBuilder().withSender(lightAgent.getPort()).withType(READY).build();
            out.writeObject(message);
            Object fromClient;
            while ((fromClient = in.readObject()) != null) {
                if (fromClient instanceof Message) {
                    final Message msg = (Message) fromClient;
                    System.out.println(String.format("%d ---received: %s", lightAgent.getPort(), fromClient.toString()));
                    if(CONFIRM == msg.type && LightAgent.data.getPeerInfo().getName().equals(msg.blockData.get(0).getPeerInfo().getName())) {
                    	LightAgent.confirm = true;
                    	break;
                    } else if(END_MINE == msg.type) {
                    	if(LightAgent.data != null)
                    		LightAgent.endMine = true;
                    	break;
                    } else if(CERTIFICATION_SUCCESS == msg.type) {
                    	LightAgent.certificationSuccess = true;
                    	break;
                    } else if(CERTIFICATION_FAILURE == msg.type) {
                    	LightAgent.certificationSuccess = false;
                    	break;
                    } else if(BLOCKCHAIN_PIECE == msg.type) {
                    	lightAgent.setBlockchainPiece(msg.blocks);
                    	break;
                    } else if(GET_BLOCKCHAIN_PIECE == msg.type) {
                    	out.writeObject(new Message.MessageBuilder()
                                .withType(SEND_BLOCKCHAIN_PIECE)
                                .withReceiver(msg.sender)
                                .withSender(lightAgent.getPort())
                                .withBlocks(lightAgent.getBlockchainPiece()).build());
                    	break;
                    }
                }
            }
            client.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
