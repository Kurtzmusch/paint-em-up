
package game.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import engine.util.Matrix4x4f;
import game.D2Line;
import game.DynamicDrawerImplementor;
import game.GUIDrawerImplementor;
import game.Game;
import game.Map;
import game.MapDefinition;
import game.SingleMessage;
import game.Sprite;
import game.Text;
import game.TickerImplementor;
import game.interfaces.Controller;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.menu.Changer;
import game.menu.Menu;
import game.menu.MenuController;
import game.menu.MenuItem;
import game.menu.Navigator;
import game.menu.Performer;
import game.menu.Typer;
import game.thing.AINode;
import game.thing.Spawner;

public class Mapper implements Ticker, GUIDrawer, MenuController, Controller {

	public static Mapper mapper;
	
	public D2Line testLine;
	
	private Sprite placer;
	private Sprite aim;
	private Matrix4x4f[] textTransformMatrix;

	private Matrix4x4f placerTransformMatrix;
	private Matrix4x4f aimTransformMatrix;
	
	private Matrix4x4f testerTransformMatrix;
	
	private Matrix4x4f placingTransformMatrix;
	private Matrix4x4f placingXTransformMatrix;
	private Matrix4x4f placingYTransformMatrix;
	private Matrix4x4f themeTextTransformMatrix;
	private int placerX;
	private int placerY;
	private float mouseLastX;
	private float mouseLastY;
	private float testerX;
	private float testerY;
	private float aimX;
	private float aimY;
	private float mouseSpeed;
	private float cameraFriction;
	private Map map;
	private byte placing;
	private byte theme;
	private Text[] staticText;
	private Text placingXText, placingYText, themeText, placingText;
	private SingleMessage singleMessage;
	private int textScale;
	private float textX, textY;
	
	public Menu currentMenu;
	public Menu mapperMenu, mapperSaveMenu, mapperLoadMenu;
	
	public DynamicDrawerImplementor dynamicDrawer;
	public GUIDrawerImplementor guiDrawer;
	public TickerImplementor ticker;
	
	
	private int dynamicDepth;
	
	private boolean dynamicDrawEnabled;
	private boolean tickEnabled;
	
	public static String[] themeStringMap = new String[5];
	
	static {
		
		themeStringMap[0] = "Swamp";
		themeStringMap[1] = "Temple";
		themeStringMap[2] = "Field";
		themeStringMap[3] = "Snow";
		themeStringMap[4] = "Moon";
	
	}
	
	public static String[] thingIDStringMap = new String[8];
	
	static {
		
		thingIDStringMap[1] = "Wall";
		thingIDStringMap[2] = "Ground";
		thingIDStringMap[3] = "Spawner DM";
		thingIDStringMap[4] = "Spawner Team #1";
		thingIDStringMap[5] = "Spawner Team #2";
		thingIDStringMap[6] = "Flag Team #1";
		thingIDStringMap[7] = "Flag Team #2";
	
	}
	
	public Mapper() {
		
		staticText = new Text[4];
		staticText[0] = new Text( "Placing ", Game.courierBitmap, 1 );
		staticText[1] = new Text( "at x: ", Game.courierBitmap, 1  );
		staticText[2] = new Text( "y: ", Game.courierBitmap, 1  );
		staticText[3] = new Text( "Theme: ", Game.courierBitmap, 1  );

		placingText = new Text( thingIDStringMap[1], Game.courierBitmap, 1 );
		placingXText = new Text( "0", Game.courierBitmap, 1 );
		placingYText = new Text( "0", Game.courierBitmap, 1 );
		themeText = new Text( themeStringMap[0], Game.courierBitmap, 1  );
		
		updateTextScale();
		
		//new ChatReceiver( Game.courierBitmap, 16, 8 );
		singleMessage = new SingleMessage( Game.courierBitmap, 32 );
		mapperMenu = new Menu();
		mapperSaveMenu = new Menu();
		mapperLoadMenu = new Menu();
		
		fillMapperMenu();
		fillMapperSaveMenu();
		fillMapperLoadMenu();
		
		
		
		currentMenu = mapperMenu;
		
		placer = Game.spriteList.get("spr_mapper_placer");
		aim = Game.spriteList.get("spr_aim");
		
		testerTransformMatrix = new Matrix4x4f();
		
		placerTransformMatrix = new Matrix4x4f();
		aimTransformMatrix = new Matrix4x4f();
		placerTransformMatrix.identity();
		placerTransformMatrix.applyTranslation( 0f, 0f, -6f);
		aimTransformMatrix.identity();
		aimTransformMatrix.translation( 0f, 0f, -8f);
		
		dynamicDepth = -6;
		
		mouseSpeed = Float.parseFloat( Game.config.mouseSpeed );
		cameraFriction = 0.125f/2f;
		
		placerX = 31;
		placerY = 31;
		
		aimX = 31*32 + 16;
		aimY = 31*32 + 16;
		
		testerX = 31*32;
		testerY = 31*32;
		
		Game.camera.x = aimX;
		Game.camera.y = aimY;
		
		map = new Map();
		
		placing = 1;
		
		mouseLastX = 640;
		mouseLastY = 360;
		
		Game.mainMouse.set(640, 360, Game.mainWindow);
		
		mapper = this;
		
		
		ticker = new TickerImplementor( 0 )  {
			@Override
			public void tick() {
				Mapper.this.tick();
			}
		};

		Game.enableTicker( ticker );
		
		dynamicDrawer = new DynamicDrawerImplementor( 6 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Mapper.this.dynamicDraw( program );
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw( Program program ) {
				Mapper.this.guiDraw( program );
			}
			
		};
		
		Game.enableGUIDrawer( guiDrawer );
		
	}
	
	public void updateTextScale() {
		textScale = Game.mainWindow.width/640;
		
		updateTextPosition();
	}
	
	public void updateTextPosition() {
		textX = -( Game.mainWindow.width/2 );
		textY = ( Game.mainWindow.height/2 )-textScale*Game.courierBitmap.cellH;
		
		for( Text t : staticText ) {	t.transformMatrix.scale( textScale, textScale, 1 ); }
		
		placingText.transformMatrix.scale( textScale, textScale, 1 );
		placingXText.transformMatrix.scale( textScale, textScale, 1 );
		placingYText.transformMatrix.scale( textScale, textScale, 1 );
		themeText.transformMatrix.scale( textScale, textScale, 1 );
		
		staticText[0].transformMatrix.applyTranslation(textX, textY, 0);
		staticText[1].transformMatrix.applyTranslation(textX+(Game.courierBitmap.cellW*textScale)*("placing: ".length()+16), textY, 0);
		staticText[2].transformMatrix.applyTranslation(textX+(Game.courierBitmap.cellW*textScale)*("placing: ".length()+16+"at x: ".length()+3), textY, 0);
		staticText[3].transformMatrix.applyTranslation(textX+(Game.courierBitmap.cellW*textScale)*("placing: ".length()+16+"at x: ".length()+3+"y: ".length()+3), textY, 0);
		
		placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*("placing: ".length()), textY, 0);
		placingXText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*("placing: ".length()+16+"at x: ".length()), textY, 0);
		placingYText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*("placing: ".length()+16+"at x: ".length()+3+"y: ".length()), textY, 0);
		themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*("placing: ".length()+16+"at x: ".length()+3+"y: ".length()+3+"theme: ".length()), textY, 0);
		
	}
	
	private void fillMapperLoadMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();
		menuItemList.add( new Typer( this, "Map Name: ", Game.courierBitmap, 1, 3, "" ) {
			@Override
			public void save() {
				Game.config.mapperLoad = this.typedString;
			}
		});
		menuItemList.add( new Performer( "Load", Game.courierBitmap, 1, 2  ) {
			public void perform() {
				Mapper.this.loadMap(Game.config.mapperLoad);
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, mapperMenu ) );
		mapperLoadMenu.fill( menuItemList );
		
	}

	private void fillMapperSaveMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();
		menuItemList.add( new Typer( this, "Save as: ", Game.courierBitmap, 1, 3, "" ) {
			@Override
			public void save() {
				Game.config.mapperSave = this.typedString;
			}
		});
		menuItemList.add( new Performer( "Save", Game.courierBitmap, 1,2  ) {
			public void perform() {
				Mapper.this.saveMap(Game.config.mapperSave);
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap,1, 1, mapperMenu ) );
		mapperSaveMenu.fill( menuItemList );
	}

	private void fillMapperMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Performer( "New Map", Game.courierBitmap, 1, 5 ) {
			public void perform() {
				Mapper.this.newMap();
			}
		});
		menuItemList.add( new Navigator( this, "Save Map", Game.courierBitmap, 1, 4, mapperSaveMenu ) );
		menuItemList.add( new Navigator( this, "Load Map", Game.courierBitmap, 1, 3, mapperLoadMenu ) );
		menuItemList.add( new Changer("Main Menu", Game.courierBitmap, 1, 2, 0 ) );
		menuItemList.add( new Performer( "Hide", Game.courierBitmap, 1, 1  ) {
			public void perform() {
				Mapper.this.hideMenu();
			}
		});
		mapperMenu.fill( menuItemList );
		
	}

	protected void hideMenu() {
		currentMenu.hide();
	}

	public void input() {
		
		float mx = Game.mainMouse.x;
		float my = Game.mainMouse.y;
		float dx = mx - mouseLastX;
		float dy = my - mouseLastY;
		
		aimX += dx*mouseSpeed/200;
		aimY -= dy*mouseSpeed/200;
		
		Game.mainMouse.set(640/4, 360/4, Game.mainWindow);
		
		mouseLastX = 640/4;
		mouseLastY = 360/4;
		
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_N] && Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_LEFT_SHIFT] ) {
			generateAINodes();			
		}
		

		
		if( Game.mainMouse.mbLeftDown ) {
			map.add( placerX, placerY, placing, theme );
		}
		if( Game.mainMouse.mbRightDown ) {
			map.remove( placerX, placerY );
		}
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_EQUAL] ) {
			Game.camera.width /= 2;
			Game.camera.height /= 2;
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_MINUS] ) {
			Game.camera.width *= 2;
			Game.camera.height *= 2;
		}
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_F1] ) {
			theme = 0;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1 );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_F2] ) {
			theme = 1;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1 );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);

		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_F3] ) {
			theme = 2;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1  );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);

		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_F4] ) {
			theme = 3;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1 );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);

		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_F5] ) {
			theme = 4;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1 );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);

		}
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_1] ) {
			placing = 1;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_2] ) {
			placing = 2;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_3] ) {
			placing = 3;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_4] ) {
			placing = 4;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_5] ) {
			placing = 5;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_6] ) {
			placing = 6;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_7] ) {
			placing = 7;
			placingText.destroy();
			placingText = new Text( thingIDStringMap[placing], Game.courierBitmap, 1 );
			placingText.transformMatrix.scale( textScale, textScale, 1 );
			placingText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(8), textY, 0);
		}
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_P] ) {
			System.out.println("| placer  || " + placerX + ":" + placerY);
			System.out.println("| aim     || " + aimX + ":" + aimY);
			System.out.println("| placing || " + thingIDStringMap[placing] );
			System.out.println("| theme   || " + themeStringMap[theme] );
		}
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_9] ) {
			map.changeColor( 1 );
		}
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_0] ) {
			map.changeColor( 2 );
		}
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_ESCAPE] ) {
			currentMenu.hidden = false;
		}
		
		
	}
	
	private void generateAINodes() {
		//map.createPath( Spawner.instanceList.getFirst() );
		
		while(true) {
			LinkedList<AINode> path = map.createPath( Spawner.instanceList.getFirst() );
			if (path != null){ break; }
		}
		
		
	}
	

	@Override
	public void tick() {
		
		input();
		
		// TODO clamp aim to gui sizes
		aimX = (aimX > Game.camera.x+640/4) ? Game.camera.x+640/4 : aimX;
		aimX = (aimX < Game.camera.x-640/4) ? Game.camera.x-640/4 : aimX;
		aimY = (aimY > Game.camera.y+360/4) ? Game.camera.y+360/4 : aimY;
		aimY = (aimY < Game.camera.y-360/4) ? Game.camera.y-360/4 : aimY;
		
		placerX = ( (int)(aimX) - ((int)(aimX)%32) )/32;
		placerY = ( (int)(aimY) - ((int)(aimY)%32) )/32;
		
		placerX = (placerX > 63) ? 63 : placerX;
		placerX = (placerX <  0) ?  0 : placerX;
		placerY = (placerY > 63) ? 63 : placerY;
		placerY = (placerY <  0) ?  0 : placerY;
		
		testerTransformMatrix.translation( testerX, testerY , 12f );
		
		placerTransformMatrix.identity();
		placerTransformMatrix.applyTranslation( placerX*32, placerY*32, -9f);
		aimTransformMatrix.translation( aimX, aimY, 12f);
		
		// camera target
		float ctx = placerX*32+16;
		float cty = placerY*32+16;
		
		// camera delta
		float cdx = ctx - Game.camera.x;
		float cdy = cty - Game.camera.y;
		
		Game.camera.x += cdx*cameraFriction;
		Game.camera.y += cdy*cameraFriction;
		
		placingXText.destroy();
		placingXText = new Text( Integer.toString(placerX), Game.courierBitmap, 1 );
		placingYText.destroy();
		placingYText = new Text( Integer.toString(placerY), Game.courierBitmap, 1 );
		
		if( Game.mainWindow.resized ) {
			mapperMenu.updateScale();
			mapperSaveMenu.updateScale();
			mapperLoadMenu.updateScale();
			Game.camera.updateScale();
		}
		
		updateTextScale();
		
		currentMenu.tick();
	}
	
	public void newMap() {
		map.destroy();
		map = new Map();
		singleMessage.add( "New map created");
	}
	
	public void loadMap( String file ) {
		
		ObjectInputStream ois;
		FileInputStream fis;
		
		try {
			String mapsFolder = Game.applicationRoot.jarFileLocation + "/peumaps/";
			fis = new FileInputStream( mapsFolder + file + ".map" );
		    ois = new ObjectInputStream(fis);
		    MapDefinition mapDefinition = (MapDefinition) ois.readObject();
		    fis.close();
		    ois.close();
		    map.destroy();
		    map = new Map( mapDefinition );
		    theme = (byte) mapDefinition.theme;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1 );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);
		    singleMessage.add( "Map " + file + " loaded" );
		    System.out.println("map loaded:::" + mapDefinition.teamColorIndex[1] + ", " + mapDefinition.teamColorIndex[2] );
		    
		    return;
		} catch (Exception e) {
		    e.printStackTrace();
		    singleMessage.add( "Could not find map" );
		}
		InputStream mapStream = Class.class.getResourceAsStream("/dm/" + file + ".map");
		try {
			if( mapStream == null ) { return;  }
			ois = new ObjectInputStream(mapStream);
			MapDefinition mapDefinition = (MapDefinition) ois.readObject();
			ois.close();
			mapStream.close();
		    map.destroy();
		    map = new Map( mapDefinition );
		    theme = (byte) mapDefinition.theme;
			map.changeTheme(theme);
			themeText.destroy();
			themeText = new Text( themeStringMap[theme], Game.courierBitmap, 1 );
			themeText.transformMatrix.scale( textScale, textScale, 1 );
			themeText.transformMatrix.applyTranslation(textX+Game.courierBitmap.cellW*textScale*(16+9+2+2+6), textY, 0);
		    singleMessage.add( "Map " + file + " loaded" );
		    System.out.println("map loaded:::" + mapDefinition.teamColorIndex[1] + ", " + mapDefinition.teamColorIndex[2] );
		    return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		 
		
	}
	
	public void saveMap( String file ) {
		
		String mapsFolder = Game.applicationRoot.jarFileLocation + "/peumaps";
		if( ! new File(mapsFolder).exists() ) {
			new File(mapsFolder).mkdir();
		}
		FileOutputStream fos;
		ObjectOutputStream oos;
		try{
			File saveFile = new File( mapsFolder +"/"+ file + ".map" );
			if( !saveFile.exists() ) {
			    fos = new FileOutputStream( saveFile );
			    oos = new ObjectOutputStream(fos);
			    oos.writeObject(map.mapDefinition);
			    singleMessage.add( "Map saved as " + file );
			    System.out.println("map saved:::" + map.mapDefinition.teamColorIndex[1] + ", " + map.mapDefinition.teamColorIndex[2] );
			    oos.close();
			    fos.close();
			}
			else {
				singleMessage.add( "Map with this name already exists" );
			}
		} catch (Exception e) {
		    e.printStackTrace();
		    singleMessage.add( "Could not save Map" );
		}
		
	}

	public void dynamicDraw( Program program ) {
		
		program.loadMatrix( "transformMatrix", testerTransformMatrix );
		aim.draw();
		
		program.loadMatrix("transformMatrix", placerTransformMatrix);
		placer.draw();
		
		// TODO aim into gui
		program.loadMatrix("transformMatrix", aimTransformMatrix);
		aim.draw();
	
	}

	@Override
	public void guiDraw( Program program ) {
		
		currentMenu.guiDraw( program );
	
		for( int i = 0; i < staticText.length; i ++ ) {
			staticText[i].draw( program );
		}
		placingText.draw( program );
		placingXText.draw( program );
		placingYText.draw( program );
		themeText.draw( program );
	}
	
	@Override
	public void changeMenu(Menu targetMenu) {
		
		currentMenu.reset();
		currentMenu = targetMenu;
		currentMenu.reset();
	
	}

	@Override
	public void destroy() {
	

		for( int i = 0; i < 4; i ++ ) {
			staticText[i].destroy();
		}
		placingText.destroy();
		placingXText.destroy();
		placingYText.destroy();
		themeText.destroy();
		
		
		map.destroy();
		singleMessage.destroy();
		mapperMenu.destroy();
		mapperLoadMenu.destroy();
		mapperSaveMenu.destroy();
		Game.disableTicker( ticker );
		Game.disableGUIDrawer( guiDrawer );
		Game.disableDynamicDrawer( dynamicDrawer );
		
	}

	@Override
	public Menu getCurrentMenu() {
		
		return currentMenu;
	
	}

}
