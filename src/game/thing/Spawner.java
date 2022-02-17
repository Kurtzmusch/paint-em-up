
package game.thing;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;

public class Spawner extends MapThing {
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	
	public DynamicDrawerImplementor dynamicDrawer;
	
	public int team;
	public static int maxIndex = Game.colorList.size();
	public int spriteIndex;
	public static Sprite[] spriteList= new Sprite[maxIndex];
	static {
		for( int c = 0; c < maxIndex; c ++ ) {
			spriteList[c] = Game.spriteList.get("spr_spawner"+c);
		}	
	}
	public static Sprite dmSprite = Game.spriteList.get("spr_spawner_dm");
	
	public static LinkedList<Spawner> instanceList = new LinkedList<Spawner>();
	
	public Spawner( int x, int y, int i, int aSpriteindex ) {
		super( x, y );
		
		this.team = i;
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 6);
		
		spriteIndex = aSpriteindex;
		if( team != 0 ) {	sprite = spriteList[spriteIndex]; }
		else { sprite = dmSprite; }
		
		instanceList.add(this);
		
		dynamicDrawer = new DynamicDrawerImplementor( -6 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Spawner.this.dynamicDraw( program );
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public int nextSprite() {
		if( team == 0 ) { return spriteIndex; }
		spriteIndex++;
		if( spriteIndex == maxIndex ) { spriteIndex = 0; }
		sprite = spriteList[spriteIndex];
		return spriteIndex;
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
