package game;

import engine.Program;
import engine.util.Matrix4x4f;
import game.interfaces.Destroyable;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;

public class SingleMessage implements GUIDrawer, Destroyable {

	private Text text;

	private GUIDrawerImplementor guiDrawer;
	
	Matrix4x4f transformMatrix;
	BitmapFont font;
	int maxChars;
	int originY = 0;
	int lifespan;
	float textScale;
	
	public SingleMessage( BitmapFont f, int maxChars ) {
		font = f;
		this.maxChars = maxChars;
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation(0, 0, 0);
		text = new Text( " ", font, 1 );
		add("teste1");
		
		updateTextScale();

		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw( Program program ) {
				SingleMessage.this.guiDraw( program );
			}
		};
		
		Game.enableGUIDrawer( guiDrawer );
	}
	public void updateTextScale() {
		textScale = Game.mainWindow.width/640;
	}
	
	public void add( String string ) {
		text.destroy();
		text = new Text( string, font, 1 );
		text.transformMatrix.scale( textScale, textScale, 1 );
		text.alignCenter();
		lifespan = 8*string.length();
	
	}

	@Override
	public void guiDraw(Program program) {
		if( lifespan-- > 0 ) {
			
			text.draw( program );
		}
		
	}

	@Override
	public void destroy() {
		text.destroy();
		Game.disableGUIDrawer( guiDrawer );
		
	}

}
