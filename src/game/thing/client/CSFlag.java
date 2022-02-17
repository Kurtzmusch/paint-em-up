
package game.thing.client;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.DynamicDrawer;
import game.thing.server.SSPlayer;

public class CSFlag extends MapThing implements DynamicDrawer {
	
	private TickerImplementor ticker;
	private DynamicDrawerImplementor dynamicDrawer;
	
	private Matrix4x4f transformMatrix;
	private Sprite sprite;
	
	public int team;
	public SSPlayer carrier;
	public CSFlagHolder flagHolder;
	
	public static int maxIndex = Game.colorList.size();
	public int spriteIndex;
	public static Sprite[] spriteList= new Sprite[maxIndex];
	static {
		for( int c = 0; c < maxIndex; c ++ ) {
			spriteList[c] = Game.spriteList.get("spr_flag"+c);
		}	
	}
	
	public static LinkedList<CSFlag> instanceList = new LinkedList<CSFlag>();
	
	public CSFlag( int x, int y, int i, int aSpriteIndex, int aTheme ) {
		super( x, y );
		
		this.team = i;
		
		flagHolder = new CSFlagHolder( x, y, aTheme );
	
		transformMatrix = new Matrix4x4f();
		transformMatrix.translation( x, y, 8 );
		
		spriteIndex = aSpriteIndex;
		sprite = spriteList[spriteIndex];
		
		ticker = new TickerImplementor( 0 ) {
			@Override
			public void tick() {
				CSFlag.this.tick();
			}	
		};
		Game.enableTicker( ticker );
		
		dynamicDrawer = new DynamicDrawerImplementor( 8 ) {
			@Override
			public void dynamicDraw(Program program) {
				CSFlag.this.dynamicDraw(program);
			}
		};
		//Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void destroy() {
		flagHolder.destroy();
		instanceList.remove(this);
		Game.disableTicker( ticker );
		Game.disableDynamicDrawer( dynamicDrawer );
	}

	public void tick() {
		transformMatrix.translation( x, y, 8 );
	}
	
	public void enableDynamicDraw() {
		Game.enableDynamicDrawer( dynamicDrawer );
		flagHolder.enableDynamicDraw();
	}

	@Override
	public void dynamicDraw(Program program) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}
	
}