
package game;

import java.util.HashMap;

import engine.LineVertexData;

public class D2Line {

	public float x1,y1,x2,y2;
	private LineVertexData lineVertexData;
	
	public D2Line( float ax1, float ay1, float ax2, float ay2 ) {
		
		x1 = ax1; y1 = ay1;
		x2 = ax2; y2 = ay2;
		
		float[] pos = { x1, y1, 0f, 	x2, y2, 0f };
		float[] col = { 0f, 0f, 0f, 		0f, 0f, 0f };
		
		HashMap<Integer, float[]> locationDataMap = new HashMap<Integer, float[]>();
		
		locationDataMap.put( new Integer(0), pos );
		locationDataMap.put(new Integer(1), col );
		
		lineVertexData = new LineVertexData( locationDataMap );
		lineVertexData.sendGPU();
		
	}
	
	public void destroy() {
		lineVertexData.removeGPU();
	}
	
	public void draw() {
		lineVertexData.singleDraw();
	}
	
	
}
