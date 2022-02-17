package game.menu;

import engine.Program;
import engine.util.Matrix4x4f;
import game.BitmapFont;
import game.Game;
import game.Text;

public class Navigator extends MenuItem {

	public String string;
	public Text text;
	public Matrix4x4f transformMatrix;
	public Menu targetMenu;
	public MenuController owner;
	//public float charWidth, charHeight; 
	
	public Navigator( MenuController owner,  String s, BitmapFont f, int xoffset, int yoffset, Menu menu ){
		super( f, xoffset, yoffset );
		super.updateTextScale();
		text = new Text(s, f, 1);
		selected = false;
		targetMenu = menu;
		this.owner = owner;
		
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
		owner.changeMenu(targetMenu);
	}
	
	public void select() { selected = true;
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation(x+charWidth*textScale*4, y, 0);
	}
	
	public void unselect() { selected = false;
		text.transformMatrix.scale(textScale, textScale, 0);
		text.transformMatrix.applyTranslation(x, y, 0);
	}
	
	public void draw( Program p ) {
		//super.draw(p);
		if(!enabled) { p.loadFloat( "whiteness", 0.7f ); }
		text.draw( p );
		p.loadFloat( "whiteness", 1f );
	}
	
}
