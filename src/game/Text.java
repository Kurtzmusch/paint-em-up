
package game;

import java.util.HashMap;

import engine.Program;
import engine.Texture;
import engine.VertexData;
import engine.util.Matrix4x4f;

public class Text {

	public VertexData vertexData;
	public Texture bitmapFontTexture;
	public Matrix4x4f transformMatrix;
	public BitmapFont font;
	public int lenght;
	
	public Text( String s, BitmapFont f, int size ) {
		if( s == null ) { s = "?"; }
		if( s.length() == 0 ) { s = " "; }
		font = f;
		lenght = s.length();
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.scale( 1, 1, 0 );
		
		float[] pos = new float[s.length()*6*3];
		float[] txc = new float[s.length()*6*2];
		
		for(int i = 0; i < s.length(); i ++ ) {
			
			pos[0+(18*i)] = 0f + ( (f.cellW* size) * i );
			pos[1+(18*i)] = (f.cellH * size);
			pos[2+(18*i)] = 0f;
			
			pos[3+(18*i)] = (f.cellW*size) + ( (f.cellW*size) * i );
			pos[4+(18*i)] = (f.cellH*size);
			pos[5+(18*i)] = 0f;
			
			pos[6+(18*i)] = 0f+( (f.cellW*size) * i );
			pos[7+(18*i)] = 0f;
			pos[8+(18*i)] = 0f;
			
			pos[9+(18*i)] = (f.cellW*size) + ( (f.cellW*size) * i );
			pos[10+(18*i)] = (f.cellH*size);
			pos[11+(18*i)] = 0f;
			
			pos[12+(18*i)] = 0f+( (f.cellW*size) * i );
			pos[13+(18*i)] = 0f;
			pos[14+(18*i)] = 0f;
			
			pos[15+(18*i)] = (f.cellW*size) + ( (f.cellW*size)  * i );
			pos[16+(18*i)] = 0f;
			pos[17+(18*i)] = 0f;
			
			int code = s.codePointAt(i);
			
			if( code == 32 ) {
				code += 12*8;
			}
			
			code -= 33;
			
			txc[0+(i*12)] = f.u[code*4];
			txc[1+(i*12)] = f.v[code*4];
		
			txc[2+(i*12)] = f.u[code*4+1];
			txc[3+(i*12)] = f.v[code*4+1];
			
			txc[4+(i*12)] = f.u[code*4+2];
			txc[5+(i*12)] = f.v[code*4+2];

			txc[6+(i*12)] = f.u[code*4+1];
			txc[7+(i*12)] = f.v[code*4+1];
			
			txc[8+(i*12)] = f.u[code*4+2];
			txc[9+(i*12)] = f.v[code*4+2];
			
			txc[10+(i*12)] = f.u[code*4+3];
			txc[11+(i*12)] = f.v[code*4+3];
			
		}
		
		HashMap<Integer, float[]> mapLocationData = new HashMap<Integer, float[]>();
		
		mapLocationData.put(new Integer(0), pos);
		mapLocationData.put(new Integer(1), txc);
		
		
		vertexData = new VertexData( mapLocationData );
		bitmapFontTexture = f.bitmapTexture;
		
		//bitmapFontTexture.sendGPU();
		vertexData.sendGPU();
	
	}
	
	public void destroy() {
		vertexData.removeGPU();
	}
	
	public void alignCenter() {
		float scale = transformMatrix.value[0];
		transformMatrix.applyTranslation( (-lenght/2)*font.cellW*scale, 0f, 0f );
	}
	
	public void alignLeft() {
		float scale = transformMatrix.value[0];
		transformMatrix.applyTranslation( lenght*font.cellW*scale, 0f, 0f );
	}
	
	public void draw( Program p ) {
		
		bitmapFontTexture.enable();
		p.loadMatrix("transformMatrix", transformMatrix );
		vertexData.singleDraw();
	}
		
	
}
