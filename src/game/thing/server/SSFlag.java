
package game.thing.server;

import java.util.LinkedList;

import game.Game;
import game.MapThing;
import game.interfaces.Ticker;

public class SSFlag extends MapThing implements Ticker {
	
	public int team;
	public SPlayer carrier;
	public SSFlagHolder flagHolder;
	
	
	public static LinkedList<SSFlag> instanceList = new LinkedList<SSFlag>();
	
	public SSFlag( int x, int y, int i ) {
		super( x, y );
		flagHolder = new SSFlagHolder( x, y );
		this.team = i;		
	}
	
	public void destroy() {
		instanceList.remove(this);
		flagHolder.destroy();
	}

	public void restore() {
		x = flagHolder.x;
		y = flagHolder.y;
	}
	public boolean atHolder() {
		return( x==flagHolder.x && y == flagHolder.y );
	}
	@Override
	public void tick() {
		// TODO Auto-generated method stub	
	}
}