
package game.thing.client;

import java.util.HashMap;
import java.util.LinkedList;

import engine.Program;
import engine.Texture;
import engine.VertexData;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.EuclideanMath;
import game.Game;
import game.MapThing;
import game.Sprite;

public class CSWall extends MapThing {
	
	
	public static DynamicDrawerImplementor classDynamicDrawer;
	private static Sprite[] mapThemeIndexSprite = new Sprite[5];
	static {
		mapThemeIndexSprite[0] = Game.spriteList.get("spr_wall1");
		mapThemeIndexSprite[1] = Game.spriteList.get("spr_wall2");
		mapThemeIndexSprite[2] = Game.spriteList.get("spr_wall1");
		mapThemeIndexSprite[3] = Game.spriteList.get("spr_wall4");
		mapThemeIndexSprite[4] = Game.spriteList.get("spr_wall4");
		
		classDynamicDrawer = new DynamicDrawerImplementor( 8 ) {
			@Override
			public void dynamicDraw(Program program) {
				CSWall.batcheDynamicDraw(program);
			}
		};
		
		Game.enableDynamicDrawer( classDynamicDrawer );
		
	}
	
	public static LinkedList<CSWall> instanceList = new LinkedList<CSWall>();
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	private DynamicDrawerImplementor dynamicDrawer;
	private int theme;
	
	private static boolean listChanged = false;

	public static Texture texture;// = Game.spriteList.get("spr_wall1").texture;
	public static VertexData vertexData;
	public static Matrix4x4f staticTransoformMatrix = new Matrix4x4f();
	static { staticTransoformMatrix.identity(); }
	
	public static void batcheDynamicDraw( Program program ) {
		if( instanceList.isEmpty() ) { return; }
		Sprite s = instanceList.getFirst().sprite;
		int t = instanceList.getFirst().theme;
		vertexData.enable();
		texture.enable();
		program.loadMatrix( "transformMatrix", staticTransoformMatrix );
		vertexData.draw();
	}
	
	public CSWall( int x, int y, int aTheme ) {
		super(x, y);
		theme = aTheme;
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 7);
		
		sprite = mapThemeIndexSprite[theme];
		texture = sprite.texture;
		instanceList.add(this); listChanged = true;
		System.out.println("cswall - " + "x. " + x + "y." + y );
		/*
		dynamicDrawer = new DynamicDrawerImplementor( -7 ) {
			@Override
			public void dynamicDraw( Program program ) {
				CSWall.this.dynamicDraw( program );
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
		for( CSWall wall : instanceList ) {
			pos[i] = wall.x + 0f;
			pos[i+1] = wall.y + 32f;
			pos[i+2] = 0f;
			
			pos[i+3] = wall.x + 32f;
			pos[i+4] = wall.y + 32f;
			pos[i+5] = 0f;
			
			pos[i+6] = wall.x + 0f;
			pos[i+7] = wall.y +0f;
			pos[i+8] = 0f;
			
			pos[i+9] = wall.x + 32f;
			pos[i+10] = wall.y + 32f;
			pos[i+11] = 0f;
			
			pos[i+12] = wall.x + 0f;
			pos[i+13] = wall.y + 0f;
			pos[i+14] = 0f;
			
			pos[i+15] = wall.x + 32f;
			pos[i+16] = wall.y + 0f;
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
	
	/***
	 * return a list of CollisionLine, which is location of the intersection and direction of normal
	 * 
	 * @param lineX1
	 * @param lineY1
	 * @param lineX2
	 * @param lineY2
	 * @return
	 */
	public LinkedList<CollisionLine> collisionLine( float lineX1, float lineY1, float lineX2, float lineY2  ) {
		boolean collided = false;
		float size = 32;
		float leftEdge = x; float rightEdge = x+size;
		float bottomEdge = y; float upperEdge = y+size;
		LinkedList<CollisionLine> collisionList = new LinkedList<CollisionLine>();
		
		// only calculate nodes that are within line range
		if( (leftEdge < EuclideanMath.bigger( lineX1, lineX2 ))
		&&(rightEdge > EuclideanMath.smaller( lineX1, lineX2 ))) {
			if( (bottomEdge < EuclideanMath.bigger( lineY1, lineY2 ))
			&&(upperEdge > EuclideanMath.smaller( lineY1, lineY2 ))) {
				float leftEdgeY = EuclideanMath.lineIntersectVerticalYComponent( lineX1, lineY1, lineX2, lineY2, leftEdge );
				if( (leftEdgeY > bottomEdge)&&(leftEdgeY < upperEdge ) ) {
					collisionList.add( new CollisionLine( leftEdge, leftEdgeY, (float) Math.PI ) );
				}
				float rightEdgeY = 	EuclideanMath.lineIntersectVerticalYComponent(lineX1, lineY1, lineX2, lineY2, rightEdge );
				if( (rightEdgeY > bottomEdge)&&(rightEdgeY < upperEdge ) ) {
					collisionList.add( new CollisionLine( rightEdge, rightEdgeY, (float) 0 ) );
				}
				float bottomEdgeX = EuclideanMath.lineIntersectHorizontalXComponent(lineX1, lineY1, lineX2, lineY2, bottomEdge );;
				if( (bottomEdgeX > leftEdge)&&(bottomEdgeX < rightEdge) ) {
					collisionList.add( new CollisionLine( bottomEdgeX, bottomEdge, (float) (Math.PI/2d*3d) ) );
				}
				float upperEdgeX = EuclideanMath.lineIntersectHorizontalXComponent(lineX1, lineY1, lineX2, lineY2, upperEdge );;
				if( (upperEdgeX > leftEdge)&&(upperEdgeX < rightEdge) ) {
					collisionList.add( new CollisionLine( upperEdgeX, upperEdge, (float) (Math.PI/2d) ) );
				}
			}
		}
		return collisionList;
	}
	
	public static void drawTiles( Program program ) {
		if( instanceList.isEmpty() ) {return; }
		Sprite s = instanceList.getFirst().sprite;
		int t = instanceList.getFirst().theme;
		vertexData.enable();
		texture.enable();
		program.loadMatrix( "transformMatrix", staticTransoformMatrix );
		vertexData.draw();
	}
	
	public void destroy() {
		//Game.disableDynamicDrawer(dynamicDrawer);
		listChanged = true;
		instanceList.remove(this);
	}
	
	public void dynamicDraw( Program program ) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}

}
