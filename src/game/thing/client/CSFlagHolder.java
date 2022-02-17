package game.thing.client;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.TickerImplementor;

public class CSFlagHolder extends MapThing {

	public Sprite sprite;
	public Matrix4x4f transformMatrix;
	public int spriteIndex;
	public DynamicDrawerImplementor dynamicDrawer;
	
	private static Sprite[] mapThemeIndexSprite = new Sprite[5];
	static {
		mapThemeIndexSprite[0] = Game.spriteList.get("spr_flag_holder1");
		mapThemeIndexSprite[1] = Game.spriteList.get("spr_flag_holder3");
		mapThemeIndexSprite[2] = Game.spriteList.get("spr_flag_holder1");
		mapThemeIndexSprite[3] = Game.spriteList.get("spr_flag_holder4");
		mapThemeIndexSprite[4] = Game.spriteList.get("spr_flag_holder4");
	}
	
	public CSFlagHolder( int aX, int aY, int aTheme ) {
		super( aX, aY );
		transformMatrix = new Matrix4x4f();
		transformMatrix.translation( x, y, 8 );
		
		spriteIndex = 0;
		sprite = mapThemeIndexSprite[aTheme];
		
		
		dynamicDrawer = new DynamicDrawerImplementor( 8 ) {
			@Override
			public void dynamicDraw(Program program) {
				CSFlagHolder.this.dynamicDraw( program );
			}
		};
		//Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void enableDynamicDraw() {
		Game.enableDynamicDrawer( dynamicDrawer );
	}
	
	public void dynamicDraw( Program p ) {
		p.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}
	
	public void destroy() {
		Game.disableDynamicDrawer(dynamicDrawer);
	}
	
}
