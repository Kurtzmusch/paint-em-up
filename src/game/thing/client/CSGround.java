package game.thing.client;

import java.util.HashMap;
import java.util.LinkedList;

import engine.Program;
import engine.Texture;
import engine.VertexData;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.thing.Ground;

public class CSGround extends MapThing {

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
	private int theme;
	
	public static boolean listChanged = false;

	public static Texture texture;// = Game.spriteList.get("spr_ground7").texture;
	public static VertexData vertexData;
	public static Matrix4x4f staticTransoformMatrix = new Matrix4x4f();
	static { staticTransoformMatrix.identity(); }
	
	public DynamicDrawerImplementor dynamicDrawer;
	
	public static LinkedList<CSGround> instanceList = new LinkedList<CSGround>();
	
	public CSGround( int x, int y, int aTheme ) {
		super( x, y );
		theme = aTheme;
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 8);
		
		sprite = mapThemeIndexSprite[theme];
		texture = sprite.texture;
		instanceList.add(this); listChanged = true;
		
		/*
		dynamicDrawer = new DynamicDrawerImplementor( -8 ) {
			@Override
			public void dynamicDraw( Program program ) {
				CSGround.this.dynamicDraw( program );
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		*/
	}
	
	public static void destroyMesh() {
		if(vertexData != null) {
			vertexData.removeGPU();
		}
	}
	
	/**
	 * creates a mesh for all instances on instancesList, allowing them to be drawn in a single call
	 */
	public static void updateMesh() {
		
		if( instanceList.isEmpty() ) {
			if(vertexData != null) {
				vertexData.removeGPU();
			}
			vertexData = null;
			return;
		}
		
		float[] pos = new float[ instanceList.size()*18 ];
		float[] txc = new float[ instanceList.size()*12 ];
		HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
		
		mapLocationData.put(new Integer(0), pos);
		mapLocationData.put(new Integer(1), txc);
		
		int i = 0; int ii = 0;
		for( CSGround ground: instanceList ) {
			pos[i] = ground.x + 0f;
			pos[i+1] = ground.y + 32f;
			pos[i+2] = 0f;
			
			pos[i+3] = ground.x + 32f;
			pos[i+4] = ground.y + 32f;
			pos[i+5] = 0f;
			
			pos[i+6] = ground.x + 0f;
			pos[i+7] = ground.y +0f;
			pos[i+8] = 0f;
			
			pos[i+9] = ground.x + 32f;
			pos[i+10] = ground.y + 32f;
			pos[i+11] = 0f;
			
			pos[i+12] = ground.x + 0f;
			pos[i+13] = ground.y + 0f;
			pos[i+14] = 0f;
			
			pos[i+15] = ground.x + 32f;
			pos[i+16] = ground.y + 0f;
			pos[i+17] = 0f;
			
			txc[ii] = 0f;
			txc[ii+1] = 0f;
			
			txc[ii+2] = 1f;
			txc[ii+3] = 0f;
			
			txc[ii+4] = 0f;
			txc[ii+5] = 1f;
			
			txc[ii+6] = 1f;
			txc[ii+7] = 0f;
			
			txc[ii+8] = 0f;
			txc[ii+9] = 1f;
			
			txc[ii+10] = 1f;
			txc[ii+11] = 1f;
			
			i += 18;
			ii += 12;
			
		}
		if(vertexData != null) {
			vertexData.removeGPU();
		}
		vertexData = new VertexData(mapLocationData);
		vertexData.sendGPU();
		
	}
	
	public static void tick() {
		
		if( listChanged ) {
			listChanged = false;
			updateMesh();
		}
	}
	
	public static void drawTiles( Program program ) {
		if( instanceList.isEmpty() ) {return; }

		vertexData.enable();
		texture.enable();
		program.loadMatrix( "transformMatrix", staticTransoformMatrix );
		vertexData.draw();
	}
	
	
	public void destroy() {
		//Game.disableDynamicDrawer( dynamicDrawer );
		instanceList.remove(this);
		listChanged = true;
	}
	
	public void dynamicDraw( Program program ) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}

}
