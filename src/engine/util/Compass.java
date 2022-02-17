package engine.util;

import java.util.HashMap;

import engine.VertexData;

public class Compass {
	
	public static VertexData vertexData;

	public Compass(){
		
		if( vertexData == null ) {
		
			float[] pos = { 	0f, 1f, 0f, 0f, -1f, 0f, 8f, 0f, 0f,
								0f, 1f, 0f, -2f, 0f, 0f, 0f, -1f, 0f,
								
								0f, 0f, -1f, 0f, 0f, 1f, 0f, 8f, 0f,
								0f, 0f, -1f, 0f, -2f, 0f, 0f, 0f, 1f,
								
								-1f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 8f,
								1f, 0f, 0f, 0f, 0f, -2f, -1f, 0f, 0f };
	
			float[] col = { 	1f, 0f, 0f, 1f, 0f, 0, 1f, 0f, 0f,
								1f, 0f, 0f, 1f, 0f, 0, 1f, 0f, 0f,
							
								0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,
								0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,
								
								0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f,
								0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f };
			
			HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
			
			mapLocationData.put(new Integer(0), pos);
			mapLocationData.put(new Integer(1), col);
			
			vertexData = new VertexData( mapLocationData );
			
			vertexData.sendGPU();
		
		}
	
	}
	
	public void draw() {
		vertexData.singleDraw();
	}
}
