package game.menu;

import engine.Program;
import engine.util.Matrix4x4f;
import game.BitmapFont;
import game.Game;
import game.Text;

public class Changer extends MenuItem {

	public String string;
	public Text text;
	public Matrix4x4f transformMatrix;
	public Menu targetMenu;
	public int targetState;
	
	public Changer( String s, BitmapFont f, int xoffset, int yoffset, int targetState ){
		super( f, xoffset, yoffset );
		super.updateTextScale();
		text = new Text(s, f, 1);
		selected = false;
		this.targetState = targetState;
		updateTextPosition();
		
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation(x, y, 0);
	}
	
	public void destroy() {
		text.destroy();
	}
	
	public void perform() {
		Game.changeState( targetState );
	}
	
	public void update() {
		
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
	
	
	public void select() { selected = true;
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation(x+Game.courierBitmap.cellW*4*textScale, y, 0);
	}
	
	public void unselect() { selected = false;
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation( x, y, 0);
	}
	
	public void draw( Program p ) {

		text.draw( p );
	}
	
}
