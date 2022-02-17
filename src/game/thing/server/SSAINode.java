package game.thing.server;

import java.util.LinkedList;

import game.thing.AINode;

public class SSAINode {

	public float x, y;
	
	public SSAINode origin;
	
	public int distanceToFlag1;
	public int distanceToFlag2;
	
	public String type = "none";
	
	public boolean connectsToFlag;
	
	public LinkedList<SSAINode> connectedNodesCloserToFlag1;
	public LinkedList<SSAINode> connectedNodesCloserToFlag2;
	
	public SSAINode( float ax, float ay ) {
		x = ax; 
		y = ay;
		
		connectedNodesCloserToFlag1= new LinkedList<SSAINode>();
		connectedNodesCloserToFlag2 = new LinkedList<SSAINode>();
		
	}
	
	public void destroy() {
		// TODO
	}
	
}
