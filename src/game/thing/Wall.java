
package game.thing;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.interfaces.Themable;

public class Wall extends MapThing implements Themable {
	
	private static Sprite[] mapThemeIndexSprite = new Sprite[5];
	
	static {
		mapThemeIndexSprite[0] = Game.spriteList.get("spr_wall1");
		mapThemeIndexSprite[1] = Game.spriteList.get("spr_wall2");
		mapThemeIndexSprite[2] = Game.spriteList.get("spr_wall1");
		mapThemeIndexSprite[3] = Game.spriteList.get("spr_wall4");
		mapThemeIndexSprite[4] = Game.spriteList.get("spr_wall4");
	}
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	private DynamicDrawerImplementor dynamicDrawer;
	
	public static LinkedList<Wall> instanceList = new LinkedList<Wall>();
	
	public Wall( int x, int y, int theme ) {
		super(x, y);
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 7);
		
		sprite = mapThemeIndexSprite[theme];
		
		instanceList.add(this);
		
		dynamicDrawer = new DynamicDrawerImplementor( -7 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Wall.this.dynamicDraw( program );
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void destroy() {
		Game.disableDynamicDrawer(dynamicDrawer);
		instanceList.remove(this);
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
