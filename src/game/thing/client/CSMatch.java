
package game.thing.client;

import java.awt.Color;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import engine.VertexData;
import engine.util.Matrix4x4f;
import game.GUIDrawerImplementor;
import game.Game;
import game.SingleMessage;
import game.Text;
import game.TickerImplementor;
import game.interfaces.Destroyable;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.network.Client;
import game.network.Request;

public class CSMatch implements Destroyable, Ticker, GUIDrawer {

	
	public VertexData vertexData;
	public boolean showScore;
	public String type;
	public CSMap map;
	public SingleMessage singleMessage;
	public boolean allowTeamChoice;
	public boolean ready;
	public boolean teamBasedType;
	public TickerImplementor ticker;
	public GUIDrawerImplementor guiDrawer;
	public Matrix4x4f scoreTransformMatrix;
	public float tablex, tabley, tablex1, tablex2;
	public CSSpawner[] spawnerList;
	public Text tableHeaderText;
	public Client client;
	
	static String[] types = { "DeathMatch", "Team DeathMatch", "?",
												"Capture The Flag", "Elimination", "Team Elimination" };
	
	public CSMatch( SingleMessage sm, Client aClient ) {
		client = aClient;
		map = new CSMap( sm, client );
		singleMessage = sm;
		ready = false;
		
		tableHeaderText = new Text( "-Player------------Kills--Deaths--Score-", Game.courierBitmap, 1 );
		
		ticker = new TickerImplementor( 8 ) {
			@Override
			public void tick() {
				CSMatch.this.tick();
			}
		};
		
		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw(Program program) {
				CSMatch.this.guiDraw(program);
			}
		};
		
		
	}
	
	public void feedMeta( byte[] data ) {
		
		// did we ask for meta?
		if( client.request.id != 200 ) { return; }
		
		client.request = new Request( client.socket, new DatagramPacket( new byte[0], 0 ), 10000  );
		
		if( ready ) { return; }
		switch (data[2]) {
			case 1:
				allowTeamChoice = true;
				break;
			case 0:
				allowTeamChoice = false;
				break;
		}
		type = types[data[1]];
		teamBasedType = true;
		if( data[1] % 2 == 0 ) {
			teamBasedType = false;
		}
		System.err.println(allowTeamChoice + " | " + type);
		
		LinkedList<CSSpawner> spawnerL = new LinkedList<CSSpawner>();
		
		for( CSSpawner spawner: CSSpawner.instanceList  ) {
			if( teamBasedType ) {
				if( spawner.team > 0 ) {
					spawnerL.add(spawner);
				}
			}
			else {
				if( spawner.team == 0 ) {
					spawnerL.add(spawner);
				}
			}
		}
		spawnerList = new CSSpawner[spawnerL.size()];
		System.out.println("player slots: " + spawnerL.size());
		int i = 0;
		for( CSSpawner s : spawnerL ) {
			spawnerList[i] = s;
			i++;
		}
					
		ready = true;
		int charScale = Game.mainWindow.width/640;
		int cellwidth = (int) (Game.courierBitmap.cellW*charScale);
		int cellheight = (int) (Game.courierBitmap.cellH*charScale);
		float width = 0, height;;
		width += cellwidth*"-playerxxxxxxxxxx-".length();
		width += cellwidth*"-kills-".length();
		width += cellwidth*"-deaths-".length();
		width += cellwidth*"-score-".length();
		width += (cellwidth/2)*2;
		float teamedHeight = (spawnerL.size()/2+1)*cellheight;
		teamedHeight +=(spawnerL.size()/2+1)*cellheight/2;
		teamedHeight += cellheight/2;
		height = (spawnerL.size()+1)*cellheight;
		height += (spawnerL.size()+1)*cellheight/2;
		height += cellheight/2;
		
		if( !teamBasedType ) {
			
			tablex = -width/2;
			tabley = -height/2;
			float[] pos = { tablex, tabley, 0f, tablex, tabley+height, 0f, tablex+width, tabley+height, 0f, 
					tablex+width, tabley+height, 0f, tablex, tabley, 0f, tablex+width, tabley, 0f };
			HashMap<Integer, float[]> ldm = new HashMap<Integer, float[]>();
			ldm.put( new Integer(0), pos );
			vertexData = new VertexData( ldm );
			vertexData.sendGPU();
		}
		else {
			tablex1 = -width -cellwidth;
			tablex2 = tablex1 + width + cellwidth;
			tabley = -teamedHeight/2;
			float[] pos = { tablex1, tabley, 0f, tablex1, tabley+teamedHeight, 0f, tablex1+width, tabley+teamedHeight, 0f, 
					tablex1+width, tabley+teamedHeight, 0f, tablex1, tabley, 0f, tablex1+width, tabley, 0f,
					tablex2, tabley, 0f, tablex2, tabley+teamedHeight, 0f, tablex2+width, tabley+teamedHeight, 0f, 
					tablex2+width, tabley+teamedHeight, 0f, tablex2, tabley, 0f, tablex2+width, tabley, 0f,
					};
			HashMap<Integer, float[]> ldm = new HashMap<Integer, float[]>();
			ldm.put( new Integer(0), pos );
			vertexData = new VertexData( ldm );
			vertexData.sendGPU();
		}
		
		
		scoreTransformMatrix = new Matrix4x4f();
		scoreTransformMatrix.identity();
		//scoreTransformMatrix.translation(-Game.mainWindow.width/2,-Game.mainWindow.height/2, 1);		
		//TODO fix multiple vertexData creation
		Game.enableTicker( ticker );
		Game.enableGUIDrawer( guiDrawer );
		if( type.equals("Capture The Flag") ) {
			map.flagTeam1.enableDynamicDraw();
			map.flagTeam2.enableDynamicDraw();
		}
		
	}
	/***
	 * feeds the score board and update its Text based on internet input
	 * 
	 */
	public void feedBoard( byte[] data ) {
		if(ready) {
			int c = 1;
			String namesString = new String(data, StandardCharsets.US_ASCII);
			for( CSSpawner activeSpawner : spawnerList ) {
				activeSpawner.score = data[c];c++;
				activeSpawner.kills = data[c];c++;
				activeSpawner.deaths = data[c];c++;
				//extract name String size from a signed byte
				//int nameStringSize = 0b00000000_00000000_00000000_11111111 & data[c];
				int nameStringSize = data[c];
				//System.out.println(data[c]);
				activeSpawner.name = namesString.substring( c+1, c+1+nameStringSize); c+= nameStringSize+1;
				//System.out.println(activeSpawner.name);
				//String.valueOf(data[c]); c += data[c]+1;
						
				activeSpawner.updateTexts();
			}
		}
		
	}
	
	@Override
	public void destroy() {
		Game.disableTicker(ticker);
		Game.disableGUIDrawer(guiDrawer);;
		map.destroy();
	}

	@Override
	public void guiDraw(Program program) {
		// TODO Auto-generated method stub
		if( showScore ) {
			program.loadMatrix( "transformMatrix", scoreTransformMatrix );
			program.loadVec4f( "color", new float[] { 0f, 0f, 0f, 0.65f } );
			program.loadFloat( "sample", 0f );
			vertexData.singleDraw();
			program.loadFloat( "sample", 1f );
			if( !teamBasedType ) {
				int hcount = spawnerList.length;
				tableHeaderText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
				tableHeaderText.transformMatrix.applyTranslation(tablex+(Game.courierBitmap.cellW*Game.mainWindow.width/640)/2, tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
				tableHeaderText.draw(program);
				hcount--;
				for( CSSpawner activeSpawner : spawnerList ) {
					activeSpawner.nameText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
					activeSpawner.nameText.transformMatrix.applyTranslation( tablex+(Game.courierBitmap.cellW*Game.mainWindow.width/640)/2, tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
					activeSpawner.nameText.draw( program );
					activeSpawner.killsText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
					activeSpawner.killsText.transformMatrix.applyTranslation( tablex+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
					activeSpawner.killsText.draw( program );
					activeSpawner.deathsText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
					activeSpawner.deathsText.transformMatrix.applyTranslation( tablex+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3+5+2)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
					activeSpawner.deathsText.draw( program );
					activeSpawner.scoreText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
					activeSpawner.scoreText.transformMatrix.applyTranslation( tablex+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3+5+2+6+2)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
					activeSpawner.scoreText.draw( program );
					hcount--;
				}
			}
			else {
				Color team1Color = Game.colorList.get( map.mapDefinition.teamColorIndex[1] );
				program.loadVec4f( "color", new float[] { team1Color.getRed()/255f, team1Color.getGreen()/255f, team1Color.getBlue()/255f, 1f } );
				program.loadFloat( "sample", 1f );
				int hcount = spawnerList.length/2;
				tableHeaderText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
				tableHeaderText.transformMatrix.applyTranslation(tablex1+(Game.courierBitmap.cellW*Game.mainWindow.width/640)/2, tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
				tableHeaderText.draw(program);
				hcount--;
				
				for( CSSpawner activeSpawner : spawnerList ) {
					if( activeSpawner.team == 1 ) {
						activeSpawner.nameText.transformMatrix.scale( (int)Game.mainWindow.width/640, (int)Game.mainWindow.width/640, 1 );
						activeSpawner.nameText.transformMatrix.applyTranslation( tablex1+(Game.courierBitmap.cellW*Game.mainWindow.width/640)/2, tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.nameText.draw( program );
						activeSpawner.killsText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.killsText.transformMatrix.applyTranslation( tablex1+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.killsText.draw( program );
						activeSpawner.deathsText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.deathsText.transformMatrix.applyTranslation( tablex1+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3+5+2)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.deathsText.draw( program );
						activeSpawner.scoreText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.scoreText.transformMatrix.applyTranslation( tablex1+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3+5+2+6+2)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.scoreText.draw( program );
						hcount--;
					}
				}
				Color team2Color = Game.colorList.get( map.mapDefinition.teamColorIndex[2] );
				program.loadVec4f( "color", new float[] { team2Color.getRed()/255f, team2Color.getGreen()/255f, team2Color.getBlue()/255f, 1f } );
				program.loadFloat( "sample", 1f );
				hcount = spawnerList.length/2;
				tableHeaderText.transformMatrix.scale( (int)Game.mainWindow.width/640, (int)Game.mainWindow.width/640, 1 );
				tableHeaderText.transformMatrix.applyTranslation((int)(tablex2+(Game.courierBitmap.cellW*Game.mainWindow.width/640)/2), (int) ( tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 ), 0 );
				tableHeaderText.draw(program);
				hcount--;
				
				for( CSSpawner activeSpawner : spawnerList ) {
					if( activeSpawner.team == 2 ) {
						activeSpawner.nameText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.nameText.transformMatrix.applyTranslation( tablex2+(Game.courierBitmap.cellW*Game.mainWindow.width/640)/2, tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.nameText.draw( program );
						activeSpawner.killsText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.killsText.transformMatrix.applyTranslation( tablex2+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.killsText.draw( program );
						activeSpawner.deathsText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.deathsText.transformMatrix.applyTranslation( tablex2+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3+5+2)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.deathsText.draw( program );
						activeSpawner.scoreText.transformMatrix.scale( Game.mainWindow.width/640, Game.mainWindow.width/640, 1 );
						activeSpawner.scoreText.transformMatrix.applyTranslation( tablex2+ ((Game.courierBitmap.cellW*Game.mainWindow.width/640)/2) + (Game.courierBitmap.cellW*Game.mainWindow.width/640*(16+3+5+2+6+2)), tabley+hcount*( (Game.courierBitmap.cellH*Game.mainWindow.height/360)+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2 )+(Game.courierBitmap.cellH*Game.mainWindow.height/360)/2, 0 );
						activeSpawner.scoreText.draw( program );
						hcount--;
					}
				}
				program.loadFloat( "sample", 1f );
				program.loadVec4f( "color", new float[] { 1f, 1f, 1f, 1f } );
			}
			
		}
		
	}

	@Override
	public void tick() {
		
		
		if( Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_TAB] ) {
			showScore = true;
		}
		else {
			showScore = false;
		}
		
	}
	
}
