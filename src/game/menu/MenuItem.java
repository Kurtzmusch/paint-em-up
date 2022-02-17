
package game.menu;

import engine.Program;
import game.BitmapFont;
import game.Game;
import game.interfaces.Destroyable;

public class MenuItem implements Destroyable {

	public int textScale;
	
	public boolean selected;
	
	public boolean enabled;
	
	public int x, y;
	
	public  float xoffset, yoffset, charWidth, charHeight;
	
	public MenuItem( BitmapFont font, int xo, int yo ) {
		charWidth = font.cellW;
		charHeight = font.cellH;
		xoffset = xo;
		yoffset = yo;
		enabled = true;
	}
	
	public void enable() { enabled = true;}
	public void disable() { enabled = false;}
	
	public void perform() {}
	
	public void draw(Program p) {
		
		if( !enabled) {
			p.loadFloat( "whiteness", 0.5f );
		}
		
	}
	
	public void update() {};
	
	public boolean checkResize() {
		if(Game.mainWindow.resized) {
			updateTextScale();
			return true;
		}
		else {
			return false;
		}
	}
	
	public void updateTextScale() {
		textScale = Game.mainWindow.width/640;
		updateTextPosition();
	}
	
	public void updateTextPosition() {
		x = ( Game.mainWindow.width/2 ) - (int) ((xoffset*charWidth*textScale)*32);
		y = -( Game.mainWindow.height/2 ) + ( (int) ((yoffset*charHeight*textScale)) );
		
	}
	
	public void select() { selected = true;  }
	
	public void unselect() { selected = false; }

	public  void destroy() {}
	
}
