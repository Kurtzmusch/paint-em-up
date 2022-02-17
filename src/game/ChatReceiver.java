
package game;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.interfaces.Destroyable;
import game.interfaces.GUIDrawer;

public class ChatReceiver implements GUIDrawer, Destroyable {

	LinkedList<Text> textList = new LinkedList<Text>();

	private GUIDrawerImplementor guiDrawer;
	
	Matrix4x4f transformMatrix;
	BitmapFont font;
	int rows;
	int maxChars;
	int originY = -360;
	int textScale = 1;
	int x,y;
	float charWidth;
	float charHeight;
	
	public ChatReceiver( BitmapFont f, int maxChars, int rows ) {
		font = f;
		this.maxChars = maxChars;
		this.rows = rows;
		charWidth = font.cellW;
		charHeight = font.cellH;
		transformMatrix = new Matrix4x4f();
		
		updateTextScale();
		
		
		add("Options");add("teste2");add("teste2");add("teste2");
		add("teste2");
		add("teste2");add("teste2");add("Options");
		add("teste2");add("teste22");add("Options");

		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw( Program program ) {
				ChatReceiver.this.guiDraw( program );
			}
		};
		
		Game.enableGUIDrawer( guiDrawer );
	}
	
	public void updateTextScale() {
		textScale = Game.mainWindow.width/640;
		updateTextPosition();
	}
	
	public void updateTextPosition() {
		x = -( Game.mainWindow.width/2 );
		y = -( Game.mainWindow.height/2 ) + ( (int) (((rows+1)*charHeight*textScale)) );
		
	}
	
	
	public void add( String string ) {
		
		textList.addLast( new Text( string, font, 1 ) );
		if( textList.size() > rows ) {
			textList.pop().destroy();
		}
	
	}

	@Override
	public void guiDraw(Program program) {
		updateTextScale();
		int yoffset = 0;
		for( Text t : textList ) {
			yoffset += charHeight*textScale;
			t.transformMatrix.scale(textScale, textScale, 1);
			t.transformMatrix.applyTranslation( x, y-yoffset, 0);
			t.draw( program );
		}
		
	}

	@Override
	public void destroy() {
		for( Text t : textList ) {
			t.destroy();
		}
		Game.disableGUIDrawer( guiDrawer );
		
	}
	
	
	
}
