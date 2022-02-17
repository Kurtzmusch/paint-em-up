package game.thing.server;

import java.util.LinkedList;

import game.MapThing;

public class SSGround extends MapThing {
	
	public static LinkedList<SSGround> instanceList = new LinkedList<SSGround>();
	
	public SSGround( int x, int y ) {
		super(x, y);
		instanceList.add(this);		
	}
	
	public void destroy() {
		// TODO
		instanceList.remove(this);
	}
}
