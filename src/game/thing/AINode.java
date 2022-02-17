package game.thing;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.Sprite;

public class AINode {
	
	private Sprite sprite;
	private Matrix4x4f transformMatrix;
	private boolean dynamicDrawEnabled;
	
	public int distanceToFlag1;
	public int distanceToFlag2;
	
	public String type = "none";
	
	public AINode origin = null;
	public boolean connectsToFlag = false;
	
	public LinkedList<AINode> connectedNodesCloserToFlag1 = new LinkedList<>();
	public LinkedList<AINode> connectedNodesCloserToFlag2= new LinkedList<>();
	public LinkedList<AINode> connectedNodes= new LinkedList<>();
	
	public DynamicDrawerImplementor dynamicDrawer;
	
	public float x,y;
	
	public static LinkedList<AINode> instanceList = new LinkedList<AINode>();
	
	public AINode( float x, float y ) {
		this.x = x;
		this.y = y;
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 0);
		
		sprite = Game.spriteList.get("spr_ainode");
		
		connectedNodes = new LinkedList<AINode>();
		
		instanceList.add(this);
		
		dynamicDrawer = new DynamicDrawerImplementor( 0 ) {
			@Override
			public void dynamicDraw( Program program ) {
				AINode.this.dynamicDraw( program );
			}
		};
		//toggleDynamicDraw();
	}
	
	public void toggleDynamicDraw() {
		if( dynamicDrawEnabled ) {
			Game.disableDynamicDrawer( dynamicDrawer );
			dynamicDrawEnabled = false;
		}else {
			Game.enableDynamicDrawer( dynamicDrawer );
			dynamicDrawEnabled = true;
		}
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
