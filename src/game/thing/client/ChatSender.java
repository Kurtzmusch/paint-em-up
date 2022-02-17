package game.thing.client;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.nio.charset.StandardCharsets;

import engine.Program;
import engine.util.Matrix4x4f;
import game.BitmapFont;
import game.Game;
import game.NetworkProtocol;
import game.Sprite;
import game.Text;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.network.Client;

public class ChatSender implements Ticker, GUIDrawer {

	private boolean typing;
	private boolean enabled;
	private boolean drawCursor;
	private String typedString;
	private Text typedText;
	private BitmapFont font;
	private Sprite cursor;
	private Matrix4x4f cursorTransformMatrix;
	private float x, y;
	private float textScale;
	private float cursorXPosition;
	private int cursorClock;
	private Client client;
	
	public ChatSender( Client aClient ) {
		
		client = aClient;
		cursor = Game.spriteList.get("spr_cursor2");
		cursorTransformMatrix = new Matrix4x4f();
		font = Game.courierBitmap;
		typing = false;
		enabled = false;
		typedString = "";
		typedText = new Text( "Press [Enter] to chat", font, 1 );
		
		updateTextScale();
		typedText.transformMatrix.scale( textScale, textScale, 1 );
		typedText.transformMatrix.applyTranslation( x, y, 0 );
		
	}
	
	public void updateTextScale() {
		textScale = Game.mainWindow.width/640;
		updateTextPosition();
	}
	
	public void updateTextPosition() {
		x = -Game.mainWindow.width/2;
		y = -Game.mainWindow.height/2;
		cursorXPosition = x + ( typedString.length()*font.cellW*textScale );
	}
	
	@Override
	public void guiDraw( Program program ) {
		if( enabled ) {
			typedText.draw( program );
		}
		if( drawCursor && typing ) {
			program.loadMatrix( "transformMatrix", cursorTransformMatrix );
			cursor.draw();
		}
	}
	
	public void enable() { enabled = true; }

	@Override
	public void tick() {
		
		if( Game.mainWindow.resized ) {
			updateTextScale();
		}
		
		if( Game.mainKeyboard.keyPress[GLFW_KEY_ESCAPE] ) {
			enabled = false;
			typing = false;
		}
		
		if( enabled ) {
			if( typing ) {
				if ( cursorClock++  == 20) {
					cursorClock = 0;
					drawCursor = !drawCursor;
				}
				if( !Game.mainKeyboard.lastString.equals(typedString) ) {				
					typedString = Game.mainKeyboard.lastString;
					typedText.destroy();
					typedText = new Text( typedString, font, 1 );
					
					typedText.transformMatrix.scale( textScale, textScale, 0 );
					typedText.transformMatrix.applyTranslation(x, y, 0f);
					
					cursorXPosition = x + ( typedString.length()*font.cellW*textScale );
					cursorTransformMatrix.scale( textScale, textScale, 0 );
					cursorTransformMatrix.applyTranslation( cursorXPosition, y, 0f );
				}					
			}
			if(Game.mainKeyboard.keyPress[GLFW_KEY_ENTER]) {
				if (typing) {
				
					typing = false;
					
					typedText.destroy();
					typedText = new Text( "Press [Enter] to chat", font, 1 );
					typedText.transformMatrix.scale( textScale, textScale, 0 );
					typedText.transformMatrix.applyTranslation(x, y, 0f);
					
					if( typedString.length() > 128 ) { typedString = typedString.substring( 0, 127);}
					byte[] preData = typedString.getBytes(StandardCharsets.US_ASCII);
					
					byte[] data = new byte[preData.length+2];
					data[0] = NetworkProtocol.MESSAGE;
					data[1] = (byte) (preData.length);
					for( int i = 0; i < preData.length; i++ ) {
						data[i+2] = preData[i];
					}
					client.send(data);
					
				}
				else {
					typedString = new String();
					Game.mainKeyboard.lastString = new String();
					typing = true;
					typedText.destroy();
					typedText = new Text( typedString, font, 1 );
					
					cursorXPosition = x;
					cursorTransformMatrix.scale( textScale, textScale, 0 );
					cursorTransformMatrix.applyTranslation( cursorXPosition, y, 0f );
				}
			}
		}
	}

	
}
