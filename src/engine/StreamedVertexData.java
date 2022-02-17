package engine;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;

public class StreamedVertexData {

	public static int vRAM;
	
	public int poligonCount, instanceCount;
	
	HashMap<Integer, Integer> attributeBufferMap;
	
	LinkedList<VertexBufferData> vboList;
	
	public int vaoID;
	
	public StreamedVertexData() {

		vaoID = GL30.glGenVertexArrays();
		
		attributeBufferMap = new HashMap<>();
	}
	
	public void draw() {
		GL30.glBindVertexArray( vaoID );
		
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL11.glDrawArrays( GL11.GL_TRIANGLES, 0, 4 );
	}
	public void instancedDraw() {
		GL30.glBindVertexArray( vaoID );
		
		
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
		GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, 6, instanceCount );
		
		//GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, instanceCount, 6 );
	}
	public void instancedDraw( int aInstanceCount, int poligonCount ) {
		GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, poligonCount, aInstanceCount );
		//GL31.glDrawArraysInstanced( GL11.GL_TRIANGLES, 0, instanceCount, 6 );
	}
	
	
	
	public void updateVBO( int attributeLocation, byte[] data, int size,
	int dataPerVertexOrInstance, boolean stream, boolean normalized, int instancesPerDataSet ) {
		
		int drawType;
		if( stream ) { drawType = GL15.GL_STREAM_DRAW; }
		else { drawType = GL15.GL_STATIC_DRAW; }
		
		int vboID = attributeBufferMap.get( attributeLocation );
		ByteBuffer buffer = DirectBuffer.createBytes( data, size );
		
		GL30.glBindVertexArray(vaoID); // probably unnecessary
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vboID );
		//GL15.glBufferData( GL15.GL_ARRAY_BUFFER, data.length, drawType ); // buffer orphaning
		GL15.glBufferSubData( GL15.GL_ARRAY_BUFFER, 0, buffer );
		GL20.glVertexAttribPointer( attributeLocation, dataPerVertexOrInstance, GL11.GL_UNSIGNED_BYTE, normalized, 0, 0 );
		GL33.glVertexAttribDivisor( attributeLocation, instancesPerDataSet );
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL30.glBindVertexArray(0);
		
	}
	
	public void updateVBO( int attributeLocation, float[] data, int size,
	int dataPerVertexOrInstance, boolean stream, boolean normalized, int instancesPerDataSet ) {
		
		int drawType;
		if( stream ) { drawType = GL15.GL_STREAM_DRAW; }
		else { drawType = GL15.GL_STATIC_DRAW; }
		
		int vboID = attributeBufferMap.get( attributeLocation );
		FloatBuffer buffer = DirectBuffer.createFloats( data, size );
		
		GL30.glBindVertexArray(vaoID);
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vboID );
		//GL15.glBufferData( GL15.GL_ARRAY_BUFFER, data, drawType ); // buffer orphaning
		GL15.glBufferSubData( GL15.GL_ARRAY_BUFFER, 0, buffer );
		GL20.glVertexAttribPointer( attributeLocation, dataPerVertexOrInstance, GL11.GL_FLOAT, normalized, 0, 0 );
		GL33.glVertexAttribDivisor( attributeLocation, instancesPerDataSet );
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL30.glBindVertexArray(0);
		
	}
	
	public void addVBO( int attributeLocation, float[] data,
	int dataPerVertexOrInstance, boolean stream, boolean normalized, int instancesPerDataSet ) {
		
		if( attributeBufferMap.containsKey(new Integer(attributeLocation)) ) {
			System.err.println("A buffer already uses this location");
			return;
		}
		
		int drawType;
		if( stream ) { drawType = GL15.GL_STREAM_DRAW; }
		else { drawType = GL15.GL_STATIC_DRAW; }
		
		vRAM += 4*data.length;
		
		FloatBuffer buffer = DirectBuffer.createFloats( data, data.length );
		
		int vboID = GL15.glGenBuffers();
		
		attributeBufferMap.put(new Integer(attributeLocation), new Integer(vboID) );
		
		GL30.glBindVertexArray(vaoID);
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vboID );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, buffer, drawType );
		GL20.glVertexAttribPointer( attributeLocation, dataPerVertexOrInstance, GL11.GL_FLOAT, normalized, 0, 0 );
		GL33.glVertexAttribDivisor( attributeLocation, instancesPerDataSet );
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL30.glBindVertexArray(0);
		
	}
	
	
	
	public void addVBO( int attributeLocation, byte[] data,
	int dataPerVertexOrInstance, boolean stream, boolean normalized, int instancesPerDataSet ) {
		
		if( attributeBufferMap.containsKey(new Integer(attributeLocation)) ) {
			System.err.println("A buffer already uses this location");
			return;
		}
		
		int drawType;
		if( stream ) { drawType = GL15.GL_STREAM_DRAW; }
		else { drawType = GL15.GL_STATIC_DRAW; }
		
		
		ByteBuffer buffer = DirectBuffer.createBytes( data, data.length );
		
		int vboID = GL15.glGenBuffers();
		
		attributeBufferMap.put(new Integer(attributeLocation), new Integer(vboID) );
		
		GL30.glBindVertexArray(vaoID);
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vboID );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, buffer, drawType );
		GL20.glVertexAttribPointer( attributeLocation, dataPerVertexOrInstance, GL11.GL_BYTE, normalized, 0, 0 );
		GL33.glVertexAttribDivisor( attributeLocation, instancesPerDataSet );
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL30.glBindVertexArray(0);
		
	}
	
}
