package game;

import game.interfaces.Destroyable;
import game.interfaces.Ticker;

public class TickerImplementor implements Ticker, Comparable<TickerImplementor>, Destroyable {

	public boolean enabled;
	public int priority;
	
	/**
	 * Creates a ticker implementor. 	 * @param priority: sets the priority of this implementor:
	 *
	 *
	 *	The game loop will call tick() for all enabled implementors in ascending order acording to its priority value
	 */
	public TickerImplementor( int priority ) {
		this.priority = priority;
	}
	
	/*** to be overridden ***/
	@Override
	public void tick() {}
	
	@Override
	public int compareTo(TickerImplementor TImplementor) {
        
		if( this.priority >= TImplementor.priority ) {
			if( this.priority > TImplementor.priority ) {
				return 1;
			}
			else {
				return 0;
			}
		} else {
			return -1;
		}
		
    }

	@Override
	public void destroy() {}
}
