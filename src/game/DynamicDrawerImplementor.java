
package game;

import engine.Program;
import game.interfaces.DynamicDrawer;

public class DynamicDrawerImplementor implements DynamicDrawer, Comparable<DynamicDrawerImplementor> {

	public boolean enabled;
	public int priority;
	
	
	/**
	 * Creates a dynamic drawer implementor. 	 * @param priority: sets the priority of this implementor:
	 *
	 *
	 *	The game loop will call dynamicDraw() for all enabled implementors in ascending order acording to its priority value
	 * This means that low priority values will be drawn first
	 */
	public DynamicDrawerImplementor( int priority ) {
		this.priority = priority;
	}
	
	@Override
	public void dynamicDraw( Program program ) {}
	
	@Override
	public int compareTo(DynamicDrawerImplementor DDImplementor) {
        
		if( this.priority >= DDImplementor.priority ) {
			if( this.priority > DDImplementor.priority ) {
				return 1;
			}
			else {
				return 0;
			}
		} else {
			return -1;
		}
		
    }
	
}
