
package game.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Request {

	DatagramPacket packet;
	DatagramSocket socket;

	public int id;
	
	boolean received = false;
	
	public Request( DatagramSocket aSocket, DatagramPacket aPacket, int aID ) {
		
		socket = aSocket;
		packet = aPacket;
		id = aID;
		
	}
	
	public void send() {
		try {
			socket.send( packet );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
