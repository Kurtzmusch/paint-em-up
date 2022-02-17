package game;

import engine.Program;
import game.interfaces.GUIDrawer;

public class GUIDrawerImplementor implements GUIDrawer, Comparable<GUIDrawerImplementor> {

	public boolean enabled;
	public int priority;
	
	public GUIDrawerImplementor( int priority ) {
		this.priority = priority;
	}
	
	/*** to be overridden ***/
	@Override
	public void guiDraw( Program program ) {}
	
	@Override
	public int compareTo(GUIDrawerImplementor GUIDImplementor) {
        
		if( this.priority >= GUIDImplementor.priority ) {
			if( this.priority > GUIDImplementor.priority ) {
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
