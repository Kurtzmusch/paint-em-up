
package game.thing;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.interfaces.DynamicDrawer;
import game.interfaces.Ticker;

public class Flag extends MapThing implements Ticker {
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	private boolean tickEnabled;
	public int team;
	public Player carrier;
	public static int maxIndex = Game.colorList.size();
	public int spriteIndex;
	public static Sprite[] spriteList= new Sprite[maxIndex];
	static {
		for( int c = 0; c < maxIndex; c ++ ) {
			spriteList[c] = Game.spriteList.get("spr_flag"+c);
		}	
	}
	public DynamicDrawerImplementor dynamicDrawer;
	
	public static LinkedList<Flag> instanceList = new LinkedList<Flag>();
	private static Sprite[] mapTeamIndexSprite = new Sprite[3];
	static {
		mapTeamIndexSprite[0] = Game.spriteList.get("spr_flag1");
		mapTeamIndexSprite[1] = Game.spriteList.get("spr_flag1");
		mapTeamIndexSprite[2] = Game.spriteList.get("spr_flag2");
	}
	
	public int nextSprite() {
		spriteIndex++;
		if( spriteIndex == maxIndex ) { spriteIndex = 0; }
		sprite = spriteList[spriteIndex];
		return spriteIndex;
	}
	
	public Flag( int x, int y, int i, int aSpriteIndex ) {
		super( x, y );
		
		this.team = i;
		
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 5);
		
		spriteIndex = aSpriteIndex;
		sprite = spriteList[spriteIndex];
		
		instanceList.add(this);
		
		dynamicDrawer = new DynamicDrawerImplementor( -5 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Flag.this.dynamicDraw( program );
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

	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

	
}
