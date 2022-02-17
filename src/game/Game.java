
package game;



import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFlush;
import static org.lwjgl.opengl.GL11.glFinish;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLX11;
import org.lwjgl.opengl.GLX12;
import org.lwjgl.opengl.GLX14;

import engine.Engine;
import engine.LineVertexData;
import engine.RawSound;
import engine.Texture;
import engine.VertexData;
import engine.ipt.Keyboard;
import engine.ipt.Mouse;
import engine.opt.Window;
import engine.util.Matrix4x4f;
import game.controller.Guest;
import game.controller.Hoster;
import game.controller.MainMenu;
import game.controller.Mapper;
import game.controller.ParticleGroup;
import game.interfaces.Controller;
import game.thing.client.CSGround;
import game.thing.client.CSWall;
import game.thing.client.SplatterParticle1;
import game.thing.client.SplatterParticle2;

public class Game {
	
	static long dynamicProgramAcumulator;
	static long guiProgramAcumulator;
	static long swapAcumulator;
	static long dpTime;
	static long guTime;
	static long swTime;

	public static LinkedList<D2Line> lineList;
	
	public static Window mainWindow;
	public static Keyboard mainKeyboard;
	public static Mouse mainMouse;
	
	public static Matrix4x4f viewMatrix;
	public static Matrix4x4f transformMatrix;
	public static Matrix4x4f guiProjectionMatrix;
	
	public static DynamicProgram dynamicProgram;
	public static GUIProgram guiProgram;
	public static LinesDynamicProgram linesProgram;
	public static Textureless2DParticlesProgram textureless2DParticlesProgram;
	
	public static BitmapFont courierBitmap;
	
	public static Config config;
	public static Camera camera;
	
	public static HashMap<String, Sprite> spriteList;
	public static HashMap<String, RawSound> soundList;
	public static LinkedList<Color> colorList;
	
	private static ListIterator<TickerImplementor> tickIterator;
	private static LinkedList<TickerImplementor> tickList;
	private static LinkedList<TickerImplementor> tickRemoveList;
	private static LinkedList<TickerImplementor> tickAddList;
	
	private static ListIterator<GUIDrawerImplementor> guiDrawIterator;
	private static LinkedList<GUIDrawerImplementor> guiDrawList;
	private static LinkedList<GUIDrawerImplementor> guiDrawRemoveList;
	private static LinkedList<GUIDrawerImplementor> guiDrawAddList;
	
	private static ListIterator<DynamicDrawerImplementor> dynamicDrawIterator;
	private static LinkedList<DynamicDrawerImplementor> dynamicDrawList;
	private static LinkedList<DynamicDrawerImplementor> dynamicDrawRemoveList;
	private static LinkedList<DynamicDrawerImplementor> dynamicDrawAddList;
	
	public static ApplicationRoot applicationRoot;
	
	public static Controller currentController;
	
	private static Debugger debugger;
	
	public static ParticleGroup textureless2DParticle;

	public static void main (String[] args ) {
		
		applicationRoot = new ApplicationRoot();
		
		setupEngine();
		setupGame();
		gameLoop();
		
	}

	public static void changeState( int state ) {
		currentController.destroy();
		switch( state ) {
			case 0:
				currentController = new MainMenu();
				break;
			case 1:
				currentController = new Mapper();
				break;
			case 2: currentController = new Hoster();
				break;
			case 3: currentController = new Guest();
				break;
		}
	}
	
	public static void enableTicker( TickerImplementor thing ) {
		tickAddList.add( thing );
	}
	public static void disableTicker( TickerImplementor thing ) {
		tickRemoveList.add( thing );
	}
	
	public static void enableDynamicDrawer( DynamicDrawerImplementor thing ) {
		dynamicDrawAddList.add( thing );
	}
	public static void disableDynamicDrawer( DynamicDrawerImplementor thing ) {
		dynamicDrawRemoveList.add( thing );
	}

	public static void enableGUIDrawer( GUIDrawerImplementor thing ) {
		guiDrawAddList.add( thing );
	}
	public static void disableGUIDrawer( GUIDrawerImplementor thing ) {
		guiDrawRemoveList.add( thing );
	}
		
	private static void preLogic() {
		// --------	
		
		
		for(  TickerImplementor implementor : tickAddList ) {
			implementor.enabled = true;
		}
		
		for( TickerImplementor implementor : tickRemoveList ) {
			implementor.enabled = false;
		}		
		tickRemoveList.clear();
		
		// --------	
			
		
		
		for( GUIDrawerImplementor implementor : guiDrawAddList ) {
			implementor.enabled = true;
		}
		
		for( GUIDrawerImplementor implementor : guiDrawRemoveList ) {
			implementor.enabled = false;
		}
		guiDrawRemoveList.clear();
		
		
		// --------	
		
		for( DynamicDrawerImplementor implementor : dynamicDrawAddList ) {
			implementor.enabled = true;
		}
		
		for( DynamicDrawerImplementor implementor : dynamicDrawRemoveList ) {
			implementor.enabled = false;
		}
		dynamicDrawRemoveList.clear();
		
		// --------	
		int lastPriority;
		int currentPriority;
		
		// --------
		
		lastPriority = Integer.MIN_VALUE;
		currentPriority = Integer.MIN_VALUE;
		Collections.sort(tickAddList);
		
		tickIterator = tickList.listIterator();
		
		TickerImplementor ticker;

		while( tickIterator.hasNext() ) {
			
			ticker = tickIterator.next();
			
			currentPriority = ticker.priority;
			if( currentPriority > lastPriority ) {
				tickIterator.previous();
				while(true) {
					if( !tickAddList.isEmpty() ) {
						if( tickAddList.getFirst().priority <= currentPriority ) {
							tickIterator.add(tickAddList.pop());
						}
						else {
							break;
						}
					}
					else {
						break;
					}
				}
			}
			
			lastPriority = currentPriority;
		}
		
		while( !tickAddList.isEmpty() ) {
			tickList.add( tickAddList.pop() );
		}
		
		// --------
		
		Collections.sort(dynamicDrawAddList);
		
		dynamicDrawIterator = dynamicDrawList.listIterator();
		
		DynamicDrawerImplementor dynamicDrawer;
		lastPriority = Integer.MIN_VALUE;
		currentPriority = Integer.MIN_VALUE;

		while( dynamicDrawIterator.hasNext() ) {
			
			dynamicDrawer = dynamicDrawIterator.next();
			
			currentPriority = dynamicDrawer.priority;
			if( currentPriority > lastPriority ) {
				dynamicDrawIterator.previous();
				while(true) {
					if( !dynamicDrawAddList.isEmpty() ) {
						if( dynamicDrawAddList.getFirst().priority <= currentPriority ) {
							dynamicDrawIterator.add(dynamicDrawAddList.pop());
						}
						else {
							break;
						}
					}
					else {
						break;
					}
				}
			}
			
			lastPriority = currentPriority;
		}

		
		while( !dynamicDrawAddList.isEmpty() ) {
			dynamicDrawList.add( dynamicDrawAddList.pop() );
		}
		
		// --------
		
		// --------
		
		Collections.sort(guiDrawAddList);
		
		guiDrawIterator = guiDrawList.listIterator();
		
		GUIDrawerImplementor guiDrawer;
		lastPriority = Integer.MIN_VALUE;
		currentPriority = Integer.MIN_VALUE;

		while( guiDrawIterator.hasNext() ) {
			
			guiDrawer = guiDrawIterator.next();
			
			currentPriority = guiDrawer.priority;
			if( currentPriority > lastPriority ) {
				guiDrawIterator.previous();
				while(true) {
					if( !guiDrawAddList.isEmpty() ) {
						if( guiDrawAddList.getFirst().priority <= currentPriority ) {
							guiDrawIterator.add(guiDrawAddList.pop());
						}
						else {
							break;
						}
					}
					else {
						break;
					}
				}
			}
			
			lastPriority = currentPriority;
		}
		
		while( !guiDrawAddList.isEmpty() ) {
			guiDrawList.add( guiDrawAddList.pop() );
		}
	}
	
	private static void logic() {

		preLogic();
		
		camera.tick();
		
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_F11] ) {
			if( Game.mainWindow.fs ) {
				Game.mainWindow.togleFullScreen("OFF");
			}	
			else {
				Game.mainWindow.togleFullScreen("ON");
			}		
		}
		
		tickIterator = tickList.listIterator();
		
		while( tickIterator.hasNext() ) {
			
			TickerImplementor ticker = tickIterator.next();
			
			if( ticker.enabled ) {
				ticker.tick();
			}
			else {
				tickIterator.remove();
			}
			
		}
		
		CSWall.tick();
		CSGround.tick();
		SplatterParticle1.staticTick();
		SplatterParticle2.staticTick();
		
	}
	
	private static void render() {
		
		long dynamicProgramInitial = System.currentTimeMillis(); 
		
		dynamicProgram.enable();
		
		dynamicProgram.loadMatrix("projectionMatrix", camera.projectionMatrix);
		dynamicProgram.loadMatrix("viewMatrix", camera.viewMatrix);
		
		
		CSGround.drawTiles( dynamicProgram );
		
		dynamicDrawIterator = dynamicDrawList.listIterator();
		
		while( dynamicDrawIterator.hasNext() ) {
			
			DynamicDrawerImplementor dynamicDrawer = dynamicDrawIterator.next();
			
			if( dynamicDrawer.enabled ) {
				dynamicDrawer.dynamicDraw( dynamicProgram );
			}
			else {
				dynamicDrawIterator.remove();
			}
			
		}
		//CSWall.drawTiles( dynamicProgram );
		//dynamicProgram.disable();
		
		

		dynamicProgramAcumulator +=  System.currentTimeMillis()-dynamicProgramInitial;
		dpTime = System.currentTimeMillis()-dynamicProgramInitial;
		
		
		linesProgram.enable();
		
		linesProgram.loadMatrix( "projectionMatrix", camera.projectionMatrix );
		linesProgram.loadMatrix( "viewMatrix", camera.viewMatrix);
		
		for( D2Line l : lineList ) {
			l.draw();
		}
		
		linesProgram.disable();
		
		
		long guiProgramInitial = System.currentTimeMillis();
		guiProgram.enable();
		
		guiProgram.loadMatrix("projectionMatrix", camera.guiProjectionMatrix);
		guiProgram.loadVec4f("color", new float[] { 1f, 1f, 1f, 1f });
		guiProgram.loadFloat("sample", 1f );
		
		guiDrawIterator = guiDrawList.listIterator();
		
		while( guiDrawIterator.hasNext() ) {
			
			GUIDrawerImplementor guiDrawer = guiDrawIterator.next();
			
			if( guiDrawer.enabled ) {
				guiDrawer.guiDraw( guiProgram );
			}
			else {
				guiDrawIterator.remove();
			}
			
		}
		
		
		//guiProgram.disable();
		
		
		guiProgramAcumulator += System.currentTimeMillis()-guiProgramInitial;
		guTime = System.currentTimeMillis()-guiProgramInitial;
		
		
		//mainWindow.update();
				
		glFlush();
		//glFinish(); seems to cripple widnows performance with vsync
		//swapAcumulator += System.currentTimeMillis()-swapInitial;
		
		
	}
	
	private static void gameLoop() {
		
		double lastSwap = 0;
		double nextSwap = 0;
		
		long acumulatedLogicTime = 0;
		long acumulatedRenderTime = 0;
		float frameTimeAcumulator = 0;
		
		int counter = 0;
		int maxCounter = 3;
		int MAXTIME = 16;
		
		double overLooped = 0d;
		
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidMode = glfwGetVideoMode(monitor);
		
		double MAXTIME2 = 0.0166666666666666666666666666666666666666666666666666666666666666d; 
		double MAXTIME3 = 1d/60d;
		MAXTIME2 = MAXTIME3;
		System.out.println( MAXTIME2 );
		System.out.println( MAXTIME3 );
		//System.out.println( 1000d/vidMode.refreshRate()/1000d );
		//0.0166638893517747042d;
		//0.0166666666666666666d;
		//16,663889352
		double frameTime = 0;
		long swapTime = 0;
		long sleepTime = 0;
		long renderTime = 0;
		long logicTime = 0;
		
		long accumulator = 0;
		long timeSlept = 0;
		
		boolean gc = true;
		while( !mainWindow.shouldClose ) {
		
			
			
			if( maxCounter == 3) { MAXTIME = 16; }
			if( maxCounter-- == 0 ) { MAXTIME = 16; maxCounter = 3;}
			
			
			
			frameTime = GLFW.glfwGetTime();
			
			counter ++;
			
			if( counter == 30 ) {
				//System.out.println("pparticles: " + SplatterParticle1.numberOfParticles);
				gc = !gc;
				if(gc) {Runtime.getRuntime().gc();}
				//System.out.println("dynamic" + dynamicProgramAcumulator/600f );
				//System.out.println("gui" + guiProgramAcumulator/600f );
				//System.out.println("swap" + swapAcumulator/600f );
				dynamicProgramAcumulator = 0;guiProgramAcumulator = 0;swapAcumulator = 0;
				float avgLogic = (float) (acumulatedLogicTime)/30f;
				//System.out.println("Avarage Logic Time: " + avgLogic + "ms");
				float avgRender = (float) (acumulatedRenderTime)/30f;
				//System.out.println("VRAM: " + (VertexData.vRAM + Texture.vRAM)/1024 + "KiB" );
				debugger.vramString = (VertexData.vRAM + Texture.vRAM + LineVertexData.vRAM)/1024 + " KiB";
				debugger.tickInstanceCountString = String.valueOf(tickList.size());
				debugger.dynamicDrawInstanceCountString = String.valueOf(dynamicDrawList.size());
				debugger.guiDrawInstanceCountString = String.valueOf(guiDrawList.size());
				debugger.avgDrawString = (String.valueOf(avgRender)+"    ").substring(0, 4);
				debugger.avgTickString = (String.valueOf(avgLogic)+"    ").substring(0, 4);
				debugger.fpsString = String.valueOf( (1000f / (frameTimeAcumulator/30f))/1000f ).substring(0, 4);
				debugger.ramString =  String.valueOf( ((Runtime.getRuntime().totalMemory()
						-Runtime.getRuntime().freeMemory())/1024) ).substring(0, 4) + "KiB";
				debugger.particlesString = String.valueOf(SplatterParticle1.imortalParticleList.size());
				//System.out.println("RAM: "+ (Runtime.getRuntime().totalMemory()
				//-Runtime.getRuntime().freeMemory())/1024/1024 + "MiB" );
				//Runtime.getRuntime().gc();
				frameTimeAcumulator = 0;
				acumulatedLogicTime = 0;
				acumulatedRenderTime = 0;
				counter = 0;
				//System.out.println("tickList: " + tickList.size());
				//System.out.println("dynamicDrawList: " + dynamicDrawList.size());
				//System.out.println("guiDrawList: " + guiDrawList.size());
				
				debugger.updateStrings();
			}
			
			
				
			Engine.poll();
			
				logicTime = System.currentTimeMillis();
				logic();
				logicTime = System.currentTimeMillis() - logicTime;
				acumulatedLogicTime+=logicTime;
				
			mainWindow.resetInput();
			glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
				
				renderTime = System.currentTimeMillis();
				render();
				renderTime = System.currentTimeMillis() - renderTime;
				acumulatedRenderTime+=renderTime;
			
			
				
			//sleepTime = MAXTIME-frameTime-accumulator-3;
			/*
			if( sleepTime > 0){
				
				timeSlept = System.currentTimeMillis();
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				timeSlept = System.currentTimeMillis() - timeSlept;
				
				//System.err.println(timeSlept + "/" + sleepTime);
				if(timeSlept > sleepTime){
					if( timeSlept - sleepTime > 3 ) {
						//System.err.println("Slept " +(timeSlept-sleepTime)+"ms longer");
					}
					//accumulator += timeSlept-sleepTime;
					//System.err.println("Next frame penalty: " +accumulator+ "");
				}
			
			}
			else {
				
				if( sleepTime < -1 ) {
					//accumulator += (-(sleepTime));
					System.err.println("Next frame penalty: " +accumulator+ "");
					System.err.println("Logic: " + logicTime);
					System.err.println("Render: " + renderTime);
					System.err.println("swap: " + swTime);
					System.err.println("dyna: " + dpTime);
					System.err.println("gui: " + guTime);
				}
				
			}*/
			
			//Thread.yield();
				
			double loopTime = GLFW.glfwGetTime();
			
			if( !config.vSync.equals("ON") ) {	while( GLFW.glfwGetTime() < nextSwap ) {} } // sync myself
			overLooped = GLFW.glfwGetTime() - nextSwap;
			//if( overLooped < 0 ) {System.out.println("overlooped glitch");}
			loopTime = GLFW.glfwGetTime() - loopTime;
			
			long swapInitial = System.currentTimeMillis();
			lastSwap = GLFW.glfwGetTime();
			nextSwap = lastSwap + MAXTIME3;
			mainWindow.swap();
			swTime = System.currentTimeMillis()-swapInitial;
			
			frameTime = GLFW.glfwGetTime() - frameTime;
			frameTimeAcumulator += frameTime;
			nextSwap -= overLooped;
			/*
			if( overLooped > 1d/1000d ) {
				System.out.println("overlooped");
				overLooped = 0d;
				
			}*/
			/*
			if( frameTime >= 17f/1000f ) {
				System.out.println(frameTime);
				//if(sleepTime > 0) {	System.err.println("slept: " + (timeSlept-sleepTime) ); }
				System.err.println("loop: " + loopTime*1000f);
				System.err.println("Render: " + renderTime);
				System.err.println("Logic: " + logicTime);
				//System.err.println("shader1: " + dpTime);
				//System.err.println("shader3: " + guTime);
				System.err.println("swap: " + swTime);

				System.err.println("--------------------------------------------------");
			}*/
			

		} // mainWindow.shouldCLose
		endGame();
	}

	private static void endGame() {
		Config.save(config);
		mainWindow.destroy();
		for( TickerImplementor ti : tickList ) {
			ti.destroy();
		}
		
	}
	
	private static void setupGame() {
		
		Window loadWindow = new Window( 4*64, 64, false, false );
		
		Engine.setThreadGraphicsRenderTarget( loadWindow );
		
		GUIProgram loadGUIProgram = new GUIProgram();
		
		
		glClearColor( 0.2f,0.2f,0.2f,1f );
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//glEnable(GL_DEPTH_TEST);
		//glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		InputStream loadc33in = Game.class.getResourceAsStream("/fnt/basis33Bitmap.png");
		
		BitmapFont loadCourierBitmap = new BitmapFont(loadc33in);
		loadCourierBitmap.bitmapTexture.sendGPU();
		
		Text loadingText = new Text( "Loading ", loadCourierBitmap, 2 );
		loadGUIProgram.enable();
		loadGUIProgram.loadFloat( "whiteness", 1 );
		loadGUIProgram.loadVec4f("color", new float[] { 1f, 1f, 1f, 1f });
		loadGUIProgram.loadFloat("sample", 1f );
		Matrix4x4f loadMatrix = new Matrix4x4f();
		loadMatrix.orthographic(-4*64/2f, 4*64/2f, 64/2f, -64/2f, -32f, 32f);
		loadGUIProgram.loadMatrix("projectionMatrix", loadMatrix );
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		loadingText.draw(loadGUIProgram);
		glFinish();
		loadWindow.show();
		loadWindow.swap();
		
		mainWindow = new Window( 1280, 720, true, false );
		mainKeyboard = new Keyboard();
		mainKeyboard.attachToWindow(mainWindow);
		mainMouse = new Mouse();
		mainMouse.attachToWindow(mainWindow);
		
		Engine.setThreadGraphicsRenderTarget( mainWindow );
		
		loadResources();
		
		config = Config.load();
		//System.out.println(config.nickname);
		if( config == null ) {
			config = new Config();
			System.out.print("first timer");
		}
		
		System.out.println(Game.config.maximumParticles.substring(1));
		
		
		dynamicProgram = new DynamicProgram();
		guiProgram = new GUIProgram();
		linesProgram = new LinesDynamicProgram();
		textureless2DParticlesProgram = new Textureless2DParticlesProgram();
		
		guiProgram.loadFloat( "whiteness", 1 );		
		
		InputStream c33in = Game.class.getResourceAsStream("/fnt/basis33Bitmap.png");
		
		courierBitmap = new BitmapFont(c33in);
		courierBitmap.bitmapTexture.sendGPU();
		
		
		
		
		
		lineList = new LinkedList<D2Line>();
		tickList = new LinkedList<TickerImplementor>();
		tickAddList = new LinkedList<TickerImplementor>();
		tickRemoveList = new LinkedList<TickerImplementor>();
		
		guiDrawList = new LinkedList<GUIDrawerImplementor>();
		guiDrawAddList = new LinkedList<GUIDrawerImplementor>();
		guiDrawRemoveList = new LinkedList<GUIDrawerImplementor>();
		
		dynamicDrawList = new LinkedList<DynamicDrawerImplementor>();
		dynamicDrawAddList = new LinkedList<DynamicDrawerImplementor>();
		dynamicDrawRemoveList = new LinkedList<DynamicDrawerImplementor>();
		
		SplatterParticle1.initialize();
		SplatterParticle2.initialize();
		
		
		textureless2DParticle = new ParticleGroup();
		textureless2DParticle.addVAO( SplatterParticle1.vertexData );
		textureless2DParticle.addVAO( SplatterParticle2.vertexData );
		textureless2DParticle.program = textureless2DParticlesProgram;
		
		System.err.println(colorList.size() + " colors");
		loadGUIProgram.disable();
		glClearColor( 0.2f,0.2f,0.2f,0.01f );
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//glEnable(GL_DEPTH_TEST);
		//glEnable(GL_CULL_FACE);
		//glCullFace(GL_BACK);
		
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		glFinish();
		loadWindow.destroy();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mainWindow.show();
		mainWindow.swap(); // cant swap before showing = (
		mainWindow.togleFullScreen(config.fullScreen); // cant FS while invisible = (
		// on windows changing to fullscreen will
		// set the sync to its default so togleFS must be always called first
		// and followed by toggleVsync
		// also moving a window will sometimes move a fullscreen window
		mainWindow.togleVSync(config.vSync);
		
		
		camera = new Camera();
		
		currentController = new MainMenu();
		
		debugger = new Debugger();

		
	}

	private static void loadSounds() {
		
		soundList = new HashMap<String, RawSound>();
		InputStream soundListList = Game.class.getResourceAsStream("/snd/sounds.txt");
		
		BufferedReader soundBR = new BufferedReader(new InputStreamReader( soundListList ));
		String s;
		while( true ){
			try {
				s = soundBR.readLine();
				if( s != null ) {
					System.out.println(s);
					RawSound sound = new RawSound( "/snd/" + s );
					System.err.println("sound " + s + "loaded" );
					soundList.put( s.substring(0, s.length()-4), sound );
					sound.sendToAPU();
				}
				else {
				soundBR.close();
				break;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private static void loadSprites() {
		spriteList = new HashMap<String, Sprite>();
		// load sprites
		String s;
		InputStream spriteListList = Game.class.getResourceAsStream("/spr/sprites.txt");
		System.out.println( spriteListList.toString() );
		InputStream spriteIS;
		BufferedReader spriteBR = new BufferedReader(new InputStreamReader( spriteListList ));
		while( true ){
			try {
				s = spriteBR.readLine();
				if( s != null ) {
					//System.out.println(s);
					spriteIS = Game.class.getResourceAsStream("/spr/" + s);
					Sprite sprite = new Sprite(spriteIS);
					spriteList.put(s.substring(0, s.length()-4), sprite);
				}
				else {
					spriteBR.close();
					break;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	/***
	 * generates colored sprites for each sprite on spr/nocolor
	 * sprites are generated shifting hue and value based on pallete image pallete.png
	 * for testing purposes the sprites will be outputed to workspace/genspr
	 */
	private static void generateSprites() {
		
		
		InputStream palleteInputStream = Game.class.getResourceAsStream("/img/pallete.png");
		InputStream nocolorTXTList = Game.class.getResourceAsStream("/spr/nocolor/nocolor2.txt");
		InputStream originNocolorSpriteInputStream;
		BufferedImage palleteBimage;
		BufferedImage originNocolorSprite;
		BufferedReader nocolorTXTListReader = new BufferedReader( new InputStreamReader(nocolorTXTList) );
		BufferedImage outputImage;
		String originNocolorSpriteString;
		try {
			
			palleteBimage = ImageIO.read(palleteInputStream);
			
			originNocolorSpriteString = nocolorTXTListReader.readLine();
			
			colorList = new LinkedList<Color>();
			for( int i = 0; i < palleteBimage.getWidth(); i ++ ) {
				colorList.add( new Color(palleteBimage.getRGB(i, 0 )) );
				System.out.println( "g"+colorList.get(i).getGreen()+".r"+ colorList.get(i).getRed() );
			}
			
			while( originNocolorSpriteString != null ) { 
				// load a nocolor sprite
				originNocolorSpriteInputStream = Game.class.getResourceAsStream("/spr/nocolor/" + originNocolorSpriteString );
				originNocolorSprite = ImageIO.read(originNocolorSpriteInputStream);
				outputImage = new BufferedImage( originNocolorSprite.getWidth(), 
						originNocolorSprite.getHeight(), 
						originNocolorSprite.getType());
				
				// for each color on the pallete
					for( int i = 0; i < palleteBimage.getWidth(); i ++ ) {
						
					// color the outputImage
						for( int h = 0; h < originNocolorSprite.getHeight(); h ++ ) {
						for( int w = 0; w < originNocolorSprite.getWidth(); w ++ ) {
							Color originColor = new Color( originNocolorSprite.getRGB( w,  h ), true );
							Color newColor;
							Color palleteColor = new Color(palleteBimage.getRGB(i, 0));
							int r,g,b,a;
							a = originColor.getAlpha();
							r = palleteColor.getRed();
							g = palleteColor.getGreen();
							b = palleteColor.getBlue();
							if( originColor.getRed() == 255 && originColor.getBlue() == 255 && originColor.getGreen() == 255 ) {
								newColor = new Color( r, g, b, a );
							}
							else {
								newColor = originColor;
							}
							outputImage.setRGB( w, h, newColor.getRGB() );
						}
						}
						System.out.println( originNocolorSpriteString.substring(0, originNocolorSpriteString.length()-("_nocolor.png".length()) )+i );
					Sprite sprite = new Sprite( outputImage );
					spriteList.put( originNocolorSpriteString.substring( 0, originNocolorSpriteString.length()-("_nocolor.png".length()) )+i , sprite );
					//File optFile = new File( originNocolorSpriteString + i + ".png");
					//ImageIO.write( outputImage, "PNG", optFile );
				}
				originNocolorSpriteString = nocolorTXTListReader.readLine();
			}
					
		} catch (IOException e) {
			System.err.println("-- Could not load pallete --");
			e.printStackTrace();
		}
		
	}
	
	private static void loadResources() {
		
		loadSounds();
		loadSprites();
		generateSprites();

		/*
		// generate colored sprites from origin sprite
		int counter = 0;
		InputStream hueList = Class.class.getResourceAsStream("/spr/defaultHUE");
		BufferedReader hueListBR = new BufferedReader(new InputStreamReader( hueList ));
		InputStream originIS = Class.class.getResourceAsStream("/spr/spr_weapon_deagle_loaded.png");
		try {
			BufferedImage spr_gun_deagleImage = ImageIO.read(originIS);
			
			while( true ){
				try {
					s = hueListBR.readLine();
					if( s != null ) {
						System.out.println(counter);
						try {
							BufferedImage bImage = new BufferedImage(spr_gun_deagleImage.getWidth(),spr_gun_deagleImage.getHeight(),spr_gun_deagleImage.getType());
							for( int i = 0; i < bImage.getHeight(); i ++ ) {
								for( int ii = 0; ii < bImage.getWidth(); ii ++ ) {
									int oldColor = spr_gun_deagleImage.getRGB(ii, i);
									int newColor;
									Color c = new Color( oldColor, true );
									float[] hsb= Color.RGBtoHSB( c.getRed(), c.getGreen(), c.getBlue(), null );
									newColor = Color.HSBtoRGB( 360f/Integer.parseInt(s), hsb[1], hsb[2] );
									int alpha = c.getAlpha() << 24;
									alpha = alpha | 0b0000_0000__1111_1111__1111_1111__1111_1111;
									newColor = newColor & alpha;
									bImage.setRGB(ii, i,newColor);
								}
							}
							File optFile = new File("spr_test_deagle_hue" + counter + ".png");
							ImageIO.write( bImage, "PNG", optFile );
							counter ++;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						break;
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		/*
		
		//File file = new File(new File(".").getCanonicalFile()+ "/sprites/");
		File file = new File( applicationRoot.jarFileLocation, "/res/spr/" );
		
		File of = new File("./sprites.txt");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(of, false) );
			File[] fileArray = file.listFiles();
			for( File pngFile : fileArray ) {
				Sprite sprite = new Sprite(pngFile);
				spriteList.put(sprite.getName(), sprite);
				bw.write(pngFile.getName());
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
		
		
	}

	private static void setupEngine() {
		
		Engine.start();
		
		
		
		//glViewport( 0, 0, 1920, 1080 );
		
	}

}
