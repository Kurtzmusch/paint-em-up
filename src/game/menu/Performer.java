
package game.menu;

import engine.Program;
import engine.util.Matrix4x4f;
import game.BitmapFont;
import game.Game;
import game.Text;

public class Performer extends MenuItem {

	public String string;
	public Text text;
	
	public Performer( String s, BitmapFont f, int xoffset, int yoffset ){
		super( f, xoffset , yoffset );
		super.updateTextScale();
		text = new Text(s, f, 1);
		selected = false;
		
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation(x, y, 0);
		
	}
	
	public void destroy() {
		text.destroy();
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
	
	public void perform() {
	}
	
	public void update() {
		
	}
	
	
	public void select() { selected = true;
		text.transformMatrix.scale(textScale, textScale, 0f);
		text.transformMatrix.applyTranslation(x+charWidth*textScale*4, y, 0);
	}
	
	public void unselect() {  selected = false; 
		text.transformMatrix.scale(textScale, textScale, 0f);
		text.transformMatrix.applyTranslation( x, y, 0 );
	}
	
	public void draw( Program p ) {
		super.draw(p);
		text.draw( p );
	}
	
}
