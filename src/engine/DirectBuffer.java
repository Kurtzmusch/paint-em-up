package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class DirectBuffer {
	
	private DirectBuffer() {}

	public static FloatBuffer createFloats( float[] array, int length ){
		FloatBuffer buffer = ByteBuffer.allocateDirect(array.length<<2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer.put(array, 0, length);
		buffer.flip();
		return buffer;
	}
	
	public static IntBuffer createInts( int[] array, int length ){
		IntBuffer buffer = ByteBuffer.allocateDirect(array.length<<2).order(ByteOrder.nativeOrder()).asIntBuffer();
		buffer.put(array, 0, length);
		buffer.flip();
		return buffer;
	}
	
	public static ByteBuffer createBytes( byte[] array, int length ){
		ByteBuffer buffer = ByteBuffer.allocateDirect(array.length).order(ByteOrder.nativeOrder());
		buffer.put(array, 0, length);
		buffer.flip();
		return buffer;
	}

}
