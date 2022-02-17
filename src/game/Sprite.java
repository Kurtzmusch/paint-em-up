package game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import engine.Texture;
import engine.VertexData;
import engine.util.ImageData;

public class Sprite {

	public Texture texture;
	public VertexData vertexData;
	private String name;
	
	
	public Sprite( BufferedImage bufferedImage ) {
		
		texture = new Texture( ImageData.load( bufferedImage ) );
		
		float[] pos = { 0f, texture.height, 0f,		texture.width, texture.height,0f,	0f, 0f, 0f,
						texture.width, texture.height,0f,	0f, 0f, 0f,		texture.width, 0f, 0f
				
		};
		float[] txc = { 0f, 0f, 	1f, 0f,		0f, 1f,
						1f, 0f,		0f, 1f,		1f, 1f
		};
		
		HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
		mapLocationData.put(new Integer(0), pos);
		mapLocationData.put(new Integer(1), txc);
		
		vertexData = new VertexData(mapLocationData);
		
		vertexData.sendGPU();
		texture.sendGPU();
		
	}
	
	public Sprite( InputStream is ) {
		
		texture = new Texture( ImageData.load( is ) );
		
		float[] pos = { 0f, texture.height, 0f,		texture.width, texture.height,0f,	0f, 0f, 0f,
						texture.width, texture.height,0f,	0f, 0f, 0f,		texture.width, 0f, 0f
				
		};
		float[] txc = { 0f, 0f, 	1f, 0f,		0f, 1f,
						1f, 0f,		0f, 1f,		1f, 1f
		};
		
		HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
		mapLocationData.put(new Integer(0), pos);
		mapLocationData.put(new Integer(1), txc);
		
		vertexData = new VertexData(mapLocationData);
		
		vertexData.sendGPU();
		texture.sendGPU();
		
	}
	
	public Sprite( File file ) {
		
		name = file.getName().substring(0, file.getName().length()-4);
		
		texture = new Texture( ImageData.load(file.getPath()) );
		
		float[] pos = { 0f, texture.height, 0f,		texture.width, texture.height,0f,	0f, 0f, 0f,
						texture.width, texture.height,0f,	0f, 0f, 0f,		texture.width, 0f, 0f
				
		};
		float[] txc = { 0f, 0f, 	1f, 0f,		0f, 1f,
						1f, 0f,		0f, 1f,		1f, 1f
		};
		
		HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
		mapLocationData.put(new Integer(0), pos);
		mapLocationData.put(new Integer(1), txc);
		
		vertexData = new VertexData(mapLocationData);
		
		vertexData.sendGPU();
		texture.sendGPU();
		
	}
	
	public String getName() {
		return name;
	}
	
	public void destroy() {
		vertexData.removeGPU();
	}
	
	public void draw() {
		texture.enable();
		vertexData.singleDraw();
	}
	
}
