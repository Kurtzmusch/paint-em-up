package game.menu;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;

import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import engine.util.Matrix4x4f;
import game.BitmapFont;
import game.Game;
import game.Sprite;
import game.Text;
import game.interfaces.Ticker;

public class Typer extends MenuItem implements Ticker {

	public String string;
	public String typedString;
	public Text text;
	public Text typedText;
	public Matrix4x4f transformMatrix;
	public BitmapFont font;
	public boolean typing;
	public MenuController owner;
	public Sprite cursor;
	private boolean drawCursor;
	private int cursorClock;
	public float xoffset, yoffset, charWidth, charHeight;
	
	private float cursorXPosition;
	public Matrix4x4f cursorTransformMatrix;
	
	public Typer( MenuController owner, String s, BitmapFont f, int xoffset, int yoffset, String configValue ){
		super( f, xoffset, yoffset );
		cursor = Game.spriteList.get("spr_cursor2");
		this.owner = owner;
		string = s;
		
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		this.charHeight = f.cellH;
		this.charWidth = f.cellW;
		
		super.updateTextScale();
		text = new Text(s, f, 1);
		
	
		
		selected = false;
		
		cursorTransformMatrix = new Matrix4x4f();
		
		cursorClock = 0;
		
		font = f;
		typing = false;
		typedString = configValue;
		typedText = new Text( s + typedString, font, 1 );
		
		typedText.transformMatrix.scale( textScale, textScale, 0 );
		typedText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0f);
		
		//cursorXPosition = x + ( ( typedString.length() + s.length() )*f.cellW*textScale ) + (f.cellW*textScale*4);
		drawCursor = false;
		cursorTransformMatrix.scale( textScale, textScale, 0 );
		cursorTransformMatrix.applyTranslation( cursorXPosition, y, 0f );
		
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation(x, y, 0);
	
	}
	
	public void destroy() {
		text.destroy();
		typedText.destroy();
	}
	
	public void updateTextScale() {
		super.updateTextScale();
		if(selected) {
			select();
		}
		else {
			unselect();
		}
	}
	
	public void updateTextPosition() {
		x = ( Game.mainWindow.width/2 ) - (int) ((xoffset*charWidth*textScale)*32);
		y = -( Game.mainWindow.height/2 ) + ( (int) ((yoffset*charHeight*textScale)) );
	}
	
	public void save() {} // to be overridden in instantiation
	
	public void perform() {
		
		
		if (typing) {
			if(Game.mainKeyboard.keyPress[GLFW_KEY_ENTER]) {
				typing = false;
				drawCursor = false;
				save();
				typedText.destroy();
				typedText = new Text( string + typedString, font, 1 );
				
				typedText.transformMatrix.scale( textScale, textScale, 0 );
				typedText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0f);
				
				owner.getCurrentMenu().unlock();
			}
		}
		else {
			Game.mainKeyboard.lastString = typedString;
			typing = true;
			drawCursor = true;
			typedText.destroy();
			typedText = new Text( "Type: " + typedString, font, 1 );
			
			typedText.transformMatrix.scale( textScale, textScale, 1 );
			typedText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0f);
			
			cursorXPosition = x + ( ( typedString.length() + "Type: ".length() )*font.cellW*textScale ) + (font.cellW*textScale*4);
			cursorTransformMatrix.scale( textScale, textScale, 0 );
			cursorTransformMatrix.applyTranslation( cursorXPosition, y, 0f );
			owner.getCurrentMenu().lock();
		}
		
	}
	
	public void select() { selected = true; 
		typedText.transformMatrix.scale( textScale, textScale, 0 );
		typedText.transformMatrix.applyTranslation(x+charWidth*4*textScale, y, 0f);
	}
	
	public void unselect() { selected = false;
		typedText.transformMatrix.scale( textScale,  textScale,  0 );
		typedText.transformMatrix.applyTranslation(x, y, 0f);
	}
	
	@Override
	public void tick() {

		if( typing ) {
			if ( cursorClock++  == 20) {
				cursorClock = 0;
				drawCursor = !drawCursor;
			}
			if( !Game.mainKeyboard.lastString.equals(typedString) ) {				
				typedString = Game.mainKeyboard.lastString;
				typedText.destroy();
				typedText = new Text( "Type: " + typedString, font, 1 );
				
				typedText.transformMatrix.scale( textScale, textScale, 0 );
				typedText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0f);
				
				cursorXPosition = x + ( ( typedString.length() + "Type: ".length() )*font.cellW*textScale ) + (font.cellW*textScale*4);
				cursorTransformMatrix.scale( textScale, textScale, 0 );
				cursorTransformMatrix.applyTranslation( cursorXPosition, y, 0f );
			}					
		}
		
	}
	
	public void draw( Program p ) {
		
		typedText.draw( p );
		
		if( drawCursor ) {
			p.loadMatrix( "transformMatrix", cursorTransformMatrix );
			cursor.draw();
		}
	}
	
}
