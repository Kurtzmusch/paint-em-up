
package engine;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Texture {

	public static int vRAM = 0;
	
	private int textureID;
	private int[] pixels;
	public int width;
	public int height;
	
	public Texture( LinkedList<int[]> list ) {
	
		pixels = list.pollFirst();
		int[] dimensions = list.pollLast();
		width = dimensions[0];
		height = dimensions[1];
		
	}
	
	public void enable() {
		
		GL13.glActiveTexture( GL13.GL_TEXTURE0 );
		glBindTexture( GL_TEXTURE_2D, textureID );
		
	}
	
	public void removeGPU() {
		
		GL11.glDeleteTextures( textureID );
		vRAM -= 4*pixels.length;
		
	}
	
	public void sendGPU() {
		
		vRAM += 4*pixels.length;
		
		textureID = glGenTextures();
		
		glBindTexture( GL_TEXTURE_2D, textureID );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
		glTexImage2D( GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, DirectBuffer.createInts(pixels, pixels.length) );
		glBindTexture( GL_TEXTURE_2D, 0 );
		
	}
	
}
