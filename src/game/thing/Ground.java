
package game.thing;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.interfaces.DynamicDrawer;
import game.interfaces.Themable;

public class Ground extends MapThing implements Themable {
	
	private static Sprite[] mapThemeIndexSprite = new Sprite[5];
	
	static {
		mapThemeIndexSprite[0] = Game.spriteList.get("spr_ground1");
		mapThemeIndexSprite[1] = Game.spriteList.get("spr_ground1");
		mapThemeIndexSprite[2] = Game.spriteList.get("spr_ground2");
		mapThemeIndexSprite[3] = Game.spriteList.get("spr_ground7");
		mapThemeIndexSprite[4] = Game.spriteList.get("spr_ground0");
	}
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	
	public DynamicDrawerImplementor dynamicDrawer;
	
	public static LinkedList<Ground> instanceList = new LinkedList<Ground>();
	
	public Ground( int x, int y, int theme ) {
		super( x, y );
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 8);
		
		sprite = mapThemeIndexSprite[theme];
		
		instanceList.add(this);
		
		dynamicDrawer = new DynamicDrawerImplementor( -8 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Ground.this.dynamicDraw( program );
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void destroy() {
		Game.disableDynamicDrawer( dynamicDrawer );
		instanceList.remove(this);
		System.out.println("ground destroyed");
	}
	
	public void dynamicDraw( Program program ) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}

	@Override
	public void changeTheme( int spriteIndex ) {
		sprite = mapThemeIndexSprite[spriteIndex];
	}
	
}
