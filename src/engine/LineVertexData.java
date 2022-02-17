
package engine;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class LineVertexData {

	private HashMap<Integer, float[]> locationDataMap;
	private LinkedList<Integer> attributeBufferID;
	private LinkedList<Integer> attributeID;
	
	public static int vRAM = 0;
	
	private int vaoID;
	private int verticesNumber;
	
	public LineVertexData( HashMap<Integer, float[]> aLocationDataMap ) {
		
		locationDataMap = aLocationDataMap;
		
	}
	
	public void enable() {
		
		GL30.glBindVertexArray( vaoID );
		
		for( Integer i : attributeID ){
			GL20.glEnableVertexAttribArray(i);
		}
		
	}
	
	public void disable() {
		for( Integer i : attributeID ){
			GL20.glDisableVertexAttribArray(i);
		}
		
		GL30.glBindVertexArray(0);
	}
	
	public void draw() {
		GL11.glDrawArrays( GL11.GL_LINES, 0, verticesNumber );
	}
		
	public void singleDraw() {
		
		enable();
		draw();
		disable();
		
	}
		
	public void removeGPU() {
		for( Integer i : attributeBufferID ){
			GL15.glDeleteBuffers(i.intValue());
		}
		GL30.glDeleteVertexArrays(vaoID);
		
		for( Entry<Integer, float[]> e : locationDataMap.entrySet() ){
			vRAM -= 4*e.getValue().length;
		}
		
	}
	
	public void sendGPU(){
		
		verticesNumber = (locationDataMap.get(new Integer(0)).length)/3;
		
		attributeBufferID = new LinkedList<Integer>();
		attributeID = new LinkedList<Integer>();
		
		vaoID = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(vaoID);
		
		for( Entry<Integer, float[]> e : locationDataMap.entrySet() ){
			
			vRAM += 4*e.getValue().length;
			
			FloatBuffer buffer = DirectBuffer.createFloats(e.getValue(), e.getValue().length);
			attributeBufferID.add( new Integer(GL15.glGenBuffers()) );
			attributeID.add(e.getKey().intValue());
			GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, attributeBufferID.getLast().intValue() );
			GL15.glBufferData( GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW );
			GL20.glVertexAttribPointer( attributeID.getLast().intValue(), e.getValue().length/verticesNumber, GL11.GL_FLOAT, false, 0, 0 );
			
		}
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		
		GL30.glBindVertexArray(0);
		
	}
	
}
