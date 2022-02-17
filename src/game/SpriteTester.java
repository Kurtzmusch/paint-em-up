package game;

import java.util.HashMap;

import engine.Texture;
import engine.VertexData;
import engine.util.ImageData;

public class SpriteTester {

	public VertexData vertexData;
	public Texture texture;
	
	public SpriteTester() {
		
	
		HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
		
		float[] pos = 	{ 	0f, 32f, 0f,		14f, 32f, 0f, 	0f, 0f, 0f,
							14f, 32f, 0f,		0f, 0f, 0f, 	14f, 0f, 0f,
						};
		float[] txc =	{
							33f/64f, 0f,			(33+14)/64f, 0f,		33f/64f, 32f/64f,
							(33+14)/64f, 0f,		33f/64f, 32f/64f,		(33+14)/64f, 32f/64f
						};
		
		mapLocationData.put(new Integer(0), pos);
		mapLocationData.put(new Integer(1), txc);
		
		
		vertexData = new VertexData( mapLocationData );
		texture = new Texture( ImageData.load("spirteTest.png") );
		
		texture.sendGPU();
		vertexData.sendGPU();

	}

	public void draw() {
		texture.enable();
		vertexData.singleDraw();
	}
	
}
