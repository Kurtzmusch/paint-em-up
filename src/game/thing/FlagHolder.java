
package game.thing;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.interfaces.DynamicDrawer;

public class FlagHolder extends MapThing {
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	private byte theme;
	
	public DynamicDrawerImplementor dynamicDrawer;
	
	public static LinkedList<FlagHolder> instanceList = new LinkedList<FlagHolder>();
	
	public FlagHolder( int x, int y, byte theme ) {
		super( x, y );
		
		this.theme = theme;
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 7);
		
		sprite = Game.spriteList.get( "spr_flagHolder" + theme );
		
		instanceList.add(this);
		
		dynamicDrawer = new DynamicDrawerImplementor( -7 ) {
			@Override
			public void dynamicDraw( Program program ) {
				FlagHolder.this.dynamicDraw( program );
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void destroy() {
		Game.disableDynamicDrawer( dynamicDrawer );
		instanceList.remove(this);
	}
	
	public void dynamicDraw( Program program ) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}
	
}
