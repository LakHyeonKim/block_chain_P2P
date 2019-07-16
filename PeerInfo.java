package kr.ac.kumoh.s20120499.management;

import java.io.Serializable;

public class PeerInfo implements Serializable{
	private static final long serialVersionUID = 111L;
	 private String name;
	 private String address;
	 private int port;
	 
	 PeerInfo(String name, String address, int port){
		 this.name = name;
		 this.address = address;
		 this.port = port;
	 }
	 
	 public String getName() {
		 return name;
	 }

	 public String getAddress() {
	     return address;
	 }

	 public int getPort() {
	     return port;
	 }
	 @Override
	 public String toString() {
	     return "PeerInfo{" +
	             "name=" + name +
	             ", address=" + address +
	             ", port=" + port +
	             '}';
	 }

}
