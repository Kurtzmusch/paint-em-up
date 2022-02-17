
package game;

import java.io.InputStream;
import java.util.LinkedList;

import engine.Texture;
import engine.util.ImageData;
import game.util.BMPFontData;

public class BitmapFont {

	public Texture bitmapTexture;
	
	public float cellW;
	public float cellH;
	public float lineH;

	public float[] u;
	public float[] v;
	
	public BitmapFont( InputStream is ) {
		
		bitmapTexture = new Texture( ImageData.load( is ) );
		
		cellW = 7f;
		cellH = 12f;
		lineH = cellH*2;
		
		u = new float[8*12*4];
		v = new float[8*12*4];
		
		int i = 0;
		
		for(  int y = 0; y < 12; y ++ ) {
			for(  int x = 0; x < 8; x ++ ) {
				u[i] =  (float)(cellW * x)/64f;
				v[i] = (float)(cellH*y)/256f;
				i ++;
				u[i] = (float) (cellW *( x+1))/64f;
				v[i] = (float)(cellH*y)/256f;
				i ++;
				u[i] =  (float)(cellW * x)/64f;
				v[i] = (float)(cellH*(y+1))/256f;
				i ++;
				u[i] = (float) (cellW * (x+1))/64f;
				v[i] = (float)(cellH*(y+1))/256f;
				i ++;
			}
		}
	}
	
	public BitmapFont( String path ) {
		
		bitmapTexture = new Texture( ImageData.load(path) );
		
		cellW = 7f;
		cellH = 12f;
		lineH = cellH*2;
		
		u = new float[8*12*4];
		v = new float[8*12*4];
		
		int i = 0;
		
		for(  int y = 0; y < 12; y ++ ) {
			for(  int x = 0; x < 8; x ++ ) {
				u[i] =  (float)(cellW * x)/64f;
				v[i] = (float)(cellH*y)/256f;
				i ++;
				u[i] = (float) (cellW *( x+1))/64f;
				v[i] = (float)(cellH*y)/256f;
				i ++;
				u[i] =  (float)(cellW * x)/64f;
				v[i] = (float)(cellH*(y+1))/256f;
				i ++;
				u[i] = (float) (cellW * (x+1))/64f;
				v[i] = (float)(cellH*(y+1))/256f;
				i ++;
			}
		}
		
	}
	
}
