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

public class Chooser extends MenuItem implements Ticker {

	public String string;
	public String choosenString;
	public Text text;
	public Text displayText;
	public Matrix4x4f transformMatrix;
	public Matrix4x4f leftChooserTransformMatrix;
	public Matrix4x4f rightChooserTransformMatrix;
	public BitmapFont font;
	public boolean choosing;
	public MenuController owner;
	public Sprite rightChooser;
	public Sprite leftChooser;
	private boolean drawChoosers;
	private int choosenIndex;
	public String[] optionsString;
	
	private int choosersClock;
	private float chooserRightXPosition;
	private float chooserLeftXPosition;
	public Matrix4x4f cursorTransformMatrix;
	
	public Chooser( MenuController owner, String s, BitmapFont f, int xoffset, int yoffset, String[] optionsString, String stringInitial ){
		super(f, xoffset, yoffset);
		this.optionsString = optionsString;
		leftChooser = Game.spriteList.get("spr_leftChooser2");
		rightChooser = Game.spriteList.get("spr_rightChooser2");
		this.owner = owner;
		string = s;
		super.updateTextScale();
		text = new Text(s, f, 1);
		selected = false;
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(x, y, 0);
		leftChooserTransformMatrix = new Matrix4x4f();
		leftChooserTransformMatrix.identity();
		rightChooserTransformMatrix = new Matrix4x4f();
		rightChooserTransformMatrix.identity();
		font = f;
		choosing = false;
		choosenIndex = 0;
		for( int i = 0; i <  optionsString.length; i ++ ) {
			if( optionsString[i].equals(stringInitial) ) {
				choosenIndex = i;
				break;
			}
		}
		
		choosenString = optionsString[choosenIndex];
		displayText = new Text( s + choosenString, font, 1 );
		displayText.transformMatrix.scale(textScale, textScale, 0);
		displayText.transformMatrix.applyTranslation(x, y, 0);

		
		drawChoosers = false;
		choosersClock = 0;
	
	
	}
	
	public void destroy() {
		text.destroy();
		displayText.destroy();
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
	
	public void save() {} // to be overridden in instantiation
	
	public void perform() {
		
		
		if (choosing) {
			choosing = false;
			drawChoosers = false;
			choosenString = optionsString[choosenIndex];
			displayText.destroy();
			displayText = new Text( string + choosenString, font, 1 );
			displayText.transformMatrix.scale(textScale, textScale, 0);
			displayText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0);
			save();
			owner.getCurrentMenu().unlock();
		}
		else {
			choosing = true;
			drawChoosers = true;
			choosenString = optionsString[choosenIndex];
			displayText.destroy();
			displayText = new Text( "Choose:   " + choosenString, font, 1 );
			displayText.transformMatrix.scale(textScale, textScale, 0);
			displayText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0);
			chooserRightXPosition = x + ( ( choosenString.length() + "Choose:   ".length() )*font.cellW*textScale ) + (font.cellW*textScale*(4+1));
			rightChooserTransformMatrix.scale( textScale, textScale, 0 );
			rightChooserTransformMatrix.applyTranslation( chooserRightXPosition, y, 0f );
			chooserLeftXPosition = x + ( ( "Choose: ".length() )*font.cellW*textScale ) + (font.cellW*textScale*(4));
			leftChooserTransformMatrix.scale( textScale, textScale, 0 );
			leftChooserTransformMatrix.applyTranslation( chooserLeftXPosition, y, 0f );
			
			owner.getCurrentMenu().lock();
		}
		
	}
	
	public void select() { selected = true;
		displayText.transformMatrix.scale(textScale, textScale, 0);
		displayText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0);
	}
	
	public void unselect() { selected = false; 
		displayText.transformMatrix.scale(textScale, textScale, 0);
		displayText.transformMatrix.applyTranslation(x, y, 0f);
	}
	
	@Override
	public void tick() {

		if( choosing ) {
			if ( choosersClock++  == 20) {
				choosersClock = 0;
				drawChoosers = !drawChoosers;
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_RIGHT] || Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_D] ) {
				choosenIndex ++;
			}
			else {
				if(Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_LEFT] || Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_A]) {
					choosenIndex --;
				}
			}
			choosenIndex = (choosenIndex > optionsString.length-1) ? 0 : choosenIndex;
			choosenIndex = (choosenIndex < 0) ? optionsString.length-1 : choosenIndex;
		
			choosenString = optionsString[choosenIndex];
			displayText.destroy();
			displayText = new Text( "Choose:   " + choosenString, font, 1 );
			displayText.transformMatrix.scale(textScale, textScale, 0);
			displayText.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0);
			
			chooserRightXPosition = x + ( ( choosenString.length() + "Choose:   ".length() )*font.cellW*textScale ) + (font.cellW*textScale*(4+1));
			rightChooserTransformMatrix.scale( textScale, textScale, 0 );
			rightChooserTransformMatrix.applyTranslation( chooserRightXPosition, y, 0f );
			chooserLeftXPosition = x + ( ( "Choose: ".length() )*font.cellW*textScale ) + (font.cellW*textScale*(4));
			leftChooserTransformMatrix.scale( textScale, textScale, 0 );
			leftChooserTransformMatrix.applyTranslation( chooserLeftXPosition, y, 0f );
		}
		
	}
	
	public void draw( Program p ) {
		displayText.draw( p );
		if( drawChoosers ) {
			p.loadMatrix( "transformMatrix", leftChooserTransformMatrix );
			leftChooser.draw();
			p.loadMatrix( "transformMatrix", rightChooserTransformMatrix );
			rightChooser.draw();
		}
	}
	
}
