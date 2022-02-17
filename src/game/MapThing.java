
package game;

import java.util.LinkedList;

import engine.Program;
import game.interfaces.Destroyable;

public abstract class MapThing implements Destroyable {

	public int x, y;
	
	public MapThing( int x, int y ) {
		
		this.x = x;
		this.y = y;
		
	}
	
	public void destroy() {}
	
}
