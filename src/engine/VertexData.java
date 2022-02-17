
package engine;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

public class VertexData {
	
	public static int vRAM = 0;
	
	private HashMap<Integer, float[]> locationDataMap;
	private LinkedList<Integer> attributeBufferID;
	private LinkedList<Integer> attributeID;
	
	private int vaoID;
	private int pNumber;
	
	public boolean empty;
	
	public int instanceCount; // holds the current instances to be drawn on instanced mode
	
	public VertexData() {
		empty = true;
	}
	
	public VertexData( HashMap<Integer, float[]> ldm ) {
		
		locationDataMap = ldm;
		empty = false;
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
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, pNumber);
	}
	
	public void instancedDraw( int aInstanceCount ) {
		GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, pNumber, aInstanceCount );
		//GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, instanceCount, 6 );
	}
	
	public void instancedDraw() {
		GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, pNumber, instanceCount );
		//GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, instanceCount, 6 );
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
	
	public void updateVBO( int vboPosition, float[] data ) {
		
		int vboID = attributeBufferID.get( vboPosition );
		FloatBuffer buffer = DirectBuffer.createFloats( data, data.length );
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vboID );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, data.length, GL15.GL_DYNAMIC_DRAW ); // buffer orphaning
		GL15.glBufferSubData( GL15.GL_ARRAY_BUFFER, 0, buffer );
		GL20.glVertexAttribPointer( vboPosition, 3, GL11.GL_FLOAT, false, 0, 0 );
		GL33.glVertexAttribDivisor( vboID, 1 );
		
	}
	
	public void updateVBO( int vboPosition, byte[] data ) {
		
		int vboID = attributeBufferID.get( vboPosition );
		ByteBuffer buffer = DirectBuffer.createBytes( data, data.length );
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vboID );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, data.length, GL15.GL_DYNAMIC_DRAW ); // buffer orphaning
		GL15.glBufferSubData( GL15.GL_ARRAY_BUFFER, 0, buffer );
		GL20.glVertexAttribPointer( vboPosition, 4, GL11.GL_BYTE, true, 0, 0 );
		GL33.glVertexAttribDivisor( vboID, 1 );
	}
	
	
	public void sendGPU(){
		
		pNumber = (locationDataMap.get(new Integer(0)).length)/3;
		
		//System.out.println("vertex count: " + pNumber);
		
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
			GL20.glVertexAttribPointer( attributeID.getLast().intValue(), e.getValue().length/pNumber, GL11.GL_FLOAT, false, 0, 0 );
			GL33.glVertexAttribDivisor( attributeID.getLast().intValue(), 0 );
		}
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		
		GL30.glBindVertexArray(0);
		
	}
	
}
