package game.thing.server;

import java.util.LinkedList;

import game.network.SClient;

public class SSTeam {

	public byte id;
	public int maximumSize;
	
	public LinkedList<SClient> clientList;
	
	public SSTeam( int aid, int amaximumSize ) {
		id = (byte) aid;
		maximumSize = amaximumSize;
		clientList = new LinkedList<SClient>();
	}
	
	/***
	 * returns a client without spawner
	 */
	public SClient getOrfan() {
		for( SClient client : clientList ) {
			if( client.spawner == null ) {
				return client;
			}
		}
		return null;
	}
	
	public void addClient( SClient aclient ) {
		if( clientList.size() < maximumSize ) {
			clientList.add(aclient);
		}
		else {
			throw new IllegalArgumentException("Tryed to add to a full list");
		}
	}
	
}
