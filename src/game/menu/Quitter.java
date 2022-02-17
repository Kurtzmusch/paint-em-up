package game.menu;

import engine.Program;
import engine.util.Matrix4x4f;
import game.BitmapFont;
import game.Game;
import game.Text;

public class Quitter extends MenuItem {

	public String string;
	public Text text;
	public Matrix4x4f transformMatrix;
	public Menu targetMenu;
	
	public Quitter( String s, BitmapFont f, int x, int y ){
		super( f, x, y );
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
		Game.mainWindow.shouldClose = true;
		
	}
	
	public void update() {
		
	}
	
	public void draw( Program p ) {
		if (selected) {
			text.transformMatrix.scale(textScale, textScale, 0);
			text.transformMatrix.applyTranslation(x + Game.courierBitmap.cellW*textScale*4, y, 0);
		}
		else {
			text.transformMatrix.scale(textScale, textScale, 0);
			text.transformMatrix.applyTranslation(x, y, 0);
		}
		text.draw( p );
	}
}
