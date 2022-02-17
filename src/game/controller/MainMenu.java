
package game.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import game.GUIDrawerImplementor;
import game.Game;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.Controller;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.menu.Changer;
import game.menu.Chooser;
import game.menu.Menu;
import game.menu.MenuController;
import game.menu.MenuItem;
import game.menu.Navigator;
import game.menu.Quitter;
import game.menu.Typer;
import game.thing.client.SplatterParticle1;
import game.thing.client.SplatterParticle2;

public class MainMenu implements Ticker, MenuController, Controller, GUIDrawer {

	private TickerImplementor ticker;
	private GUIDrawerImplementor guiDrawer;
	
	public Menu currentMenu;
	public Menu rootMenu, joinMenu, joinIPMenu, joinPortMenu, hostMenu, hostTypeMenu, hostMapMenu, hostPortMenu, optionsMenu, quitMenu, optionsNicknameMenu, optionsAimSpeedMenu,
				optionsMasterVolumeMenu, optionsParticleLifeMenu, optionsFullScreenMenu;
	
	public String[] maps; 
	
	public MainMenu() {
		
		maps = loadMaps();
		
		
		rootMenu = new Menu();
		joinMenu = new Menu();
			//joinIPMenu = new Menu();
			//joinPortMenu = new Menu();
		hostMenu = new Menu();
			//hostTypeMenu = new Menu();
			//hostMapMenu = new Menu();
			//hostPortMenu = new Menu();
		optionsMenu = new Menu();
			//optionsNicknameMenu = new Menu();
			//optionsFullScreenMenu = new Menu();
		quitMenu = new Menu();
		
		fillRootMenu();
		fillJoinMenu();
			//fillJoinIPMenu();
			//fillJoinPortMenu();
		fillHostMenu();
			//fillHostTypeMenu();
			//fillHostMapMenu();
			//fillHostPortMenu();
		fillOptionsMenu();
			//fillOptionsNicknameMenu();
			//fillOptionsFullScreenMenu();
		fillQuitMenu();
		
		currentMenu = rootMenu;
		
		ticker = new TickerImplementor( 0 ) {
			@Override
			public void tick() {
				MainMenu.this.tick();
			
			}
		};
		
		Game.enableTicker( ticker );
		
		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw( Program program ) {
				MainMenu.this.guiDraw( program );
			}
		};
		
		Game.enableGUIDrawer( guiDrawer );
		
	}
	

	private String[] loadMaps() {
		// load created maps
		File file;
		LinkedList<String> fileList = new LinkedList<String>();
		String mapsFolder = Game.applicationRoot.jarFileLocation + "/peumaps/";
		file = new File( mapsFolder );
		if (file.exists() ){
			File[] fileArray = file.listFiles();
			for( File f : fileArray ) {
				if( f.getName().endsWith(".map") ) {
					fileList.add(f.getName().substring(0, f.getName().length()-4));
				}
			}			
		}
		// load default maps
		
		InputStream mapsListList = Game.class.getResourceAsStream("/dm/maps.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader( mapsListList ));
		String s;
		while( true ){
			try {
				s = br.readLine();
				if( s != null ) {
					fileList.add(s.substring(0, s.length()-4));
				}
				else {
					br.close();
					break;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		int listSize = fileList.size();
		// make it an array and add random to the end
		String[] array = new String[fileList.size()+1];
		for( int i = 0; fileList.size() > 0; i ++ ) {
			array[i] = fileList.pop();
		}
		array[listSize] = "random";
		return array;
		
	}
	

	
	private void fillOptionsFullScreenMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Chooser( this, "Fullscreen: ", Game.courierBitmap, 1, 2, new String[] { "ON", "OFF" }, Game.config.fullScreen ) {
			@Override
			public void save() {
				Game.config.fullScreen = this.choosenString;
				//Game.mainWindow.togleFullScreen(this.choosenString);
				MainMenu.this.updateScale();
				Game.camera.updateScale();
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, optionsMenu ) );
		optionsFullScreenMenu.fill( menuItemList );
		
	}

	protected void updateScale() {
		
		rootMenu.updateScale();
		joinMenu.updateScale();
		hostMenu.updateScale();
		optionsMenu.updateScale();
		quitMenu.updateScale();
		
	}


	private void fillHostMapMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		
		menuItemList.add( new Chooser( this, "Map: ", Game.courierBitmap, 1, 2, maps, Game.config.hostMap ) {
			@Override
			public void save() {
				Game.config.hostMap = this.choosenString;
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, hostMenu ) );
		hostMapMenu.fill( menuItemList );
		
	}

	private void fillHostPortMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Typer( this, "Port: ", Game.courierBitmap, 1, 2, Game.config.hostPort ) {
			@Override
			public void save() {
				Game.config.hostPort = this.typedString;
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, hostMenu ) );
		hostPortMenu.fill( menuItemList );
		
	}

	private void fillJoinPortMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Typer( this, "Port: ", Game.courierBitmap, 1, 2, Game.config.joinPort ) {
			@Override
			public void save() {
				Game.config.joinPort = this.typedString;
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, joinMenu ) );
		joinPortMenu.fill( menuItemList );
	}
	
	private void fillJoinIPMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Typer( this, "IP: ", Game.courierBitmap, 1, 2, Game.config.joinIP ) {
			@Override
			public void save() {
				Game.config.joinIP = this.typedString;
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, joinMenu ) );
		joinIPMenu.fill( menuItemList );
	}

	private void fillHostTypeMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Chooser( this, "Match Type: ", Game.courierBitmap, 1, 2, new String[] {"Capture the Flag", "Team DeathMatch", "DeathMatch"}, Game.config.hostType ) {
			@Override
			public void save() {
				Game.config.hostType = this.choosenString;
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, hostMenu ) );
		hostTypeMenu.fill( menuItemList );
		
	}

	private void fillOptionsNicknameMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Typer( this, "Nickname: ", Game.courierBitmap, 1, 2, Game.config.nickname ) {
			@Override
			public void save() {
				Game.config.nickname = this.typedString;
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, optionsMenu ) );
		optionsNicknameMenu.fill( menuItemList );
	}

	
	
	private void fillJoinMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Changer( "Join Match", Game.courierBitmap, 1, 4, 3 ) );
		menuItemList.add( new Typer( this, "Server IP: ", Game.courierBitmap, 1, 3, Game.config.joinIP ) {
			@Override
			public void save() {
				Game.config.joinIP = this.typedString;
			}
		});
		//menuItemList.add( new Navigator( this, "Server IP", Game.courierBitmap, 1, 3, joinIPMenu ) );
		menuItemList.add( new Typer( this, "Server Port: ", Game.courierBitmap, 1, 2, Game.config.joinPort ) {
			@Override
			public void save() {
				Game.config.joinPort = this.typedString;
			}
		});
		//menuItemList.add( new Navigator( this, "Server Port", Game.courierBitmap, 1, 2, joinPortMenu ) );
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, rootMenu ) );
		joinMenu.fill( menuItemList );
	}
	
	private void fillRootMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Navigator( this, "Join a Match", Game.courierBitmap, 1, 5, joinMenu ) );
		menuItemList.add( new Navigator( this, "Host a Match", Game.courierBitmap, 1, 4, hostMenu ) );
		menuItemList.add( new Changer( "Create a Map", Game.courierBitmap, 1, 3, 1 ) );
		menuItemList.add( new Navigator( this, "Options", Game.courierBitmap, 1, 2, optionsMenu ) );
		menuItemList.add( new Navigator( this, "Quit", Game.courierBitmap, 1, 1, quitMenu ) );
		rootMenu.fill( menuItemList );
	}
	
	private void fillHostMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Changer( "Start Match", Game.courierBitmap, 1, 5, 2 ) );
		menuItemList.add( new Chooser( this, "Match Type: ", Game.courierBitmap, 1, 4, new String[] {"Capture The Flag", "Team DeathMatch", "DeathMatch", "Team Elimination", "Elimination"}, Game.config.hostType ) {
			@Override
			public void save() {
				Game.config.hostType = this.choosenString;
			}
		});
		//menuItemList.add( new Navigator( this, "Match Type", Game.courierBitmap, 1, 4, hostTypeMenu ) );
		menuItemList.add( new Chooser( this, "Map: ", Game.courierBitmap, 1, 3, maps, Game.config.hostMap ) {
			@Override
			public void save() {
				Game.config.hostMap = this.choosenString;
			}
		});
		//menuItemList.add( new Navigator( this, "Match Map", Game.courierBitmap, 1, 3, hostMapMenu ) );
		menuItemList.add( new Typer( this, "Port: ", Game.courierBitmap, 1, 2, Game.config.hostPort ) {
			@Override
			public void save() {
				Game.config.hostPort = this.typedString;
			}
		});
		//menuItemList.add( new Navigator( this, "Server Port", Game.courierBitmap, 1, 2, hostPortMenu ) );
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, rootMenu ) );
		hostMenu.fill( menuItemList );
	}
	private void fillOptionsMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Typer( this, "Nickname: ", Game.courierBitmap, 1, 9, Game.config.nickname ) {
			@Override
			public void save() {
				Game.config.nickname = this.typedString;
			}
		});
		//menuItemList.add( new Navigator( this, "Nickname", Game.courierBitmap, 1, 7, optionsNicknameMenu) );
		menuItemList.add( new Chooser( this, "Mouse Speed: ", Game.courierBitmap, 1, 8, new String[] {"50", "75", "100", "150" ,"200" }, Game.config.mouseSpeed ) {
			@Override
			public void save() {
				Game.config.mouseSpeed = this.choosenString;
			}
		});
		//menuItemList.add( new Navigator( this, "Aim Speed", Game.courierBitmap, 1, 6, optionsMenu) );
		menuItemList.add( new Chooser( this, "Master Volume: ", Game.courierBitmap, 1, 7, new String[] {"0", "10", "20", "30" ,"40", "50", "60", "70", "80", "90", "100" }, Game.config.masterVolume ) {
			@Override
			public void save() {
				Game.config.masterVolume = this.choosenString;
			}
		});
		menuItemList.add( new Chooser( this, "Effects Volume: ", Game.courierBitmap, 1, 6, new String[] {"0", "10", "20", "30" ,"40", "50", "60", "70", "80", "90", "100" }, Game.config.effectsVolume ) {
			@Override
			public void save() {
				Game.config.effectsVolume = this.choosenString;
			}
		});
		menuItemList.add( new Chooser( this, "Music Volume: ", Game.courierBitmap, 1, 5, new String[] {"0", "10", "20", "30" ,"40", "50", "60", "70", "80", "90", "100" }, Game.config.musicVolume ) {
			@Override
			public void save() {
				Game.config.musicVolume = this.choosenString;
			}
		});
		
		//menuItemList.add( new Navigator( this, "Master Volume", Game.courierBitmap, 1, 5, optionsMenu) );
		menuItemList.add( new Chooser( this, "Maximum Particles: ", Game.courierBitmap, 1, 4, new String[] {"x1", "x2", "x4" ,"x8", "x16" }, Game.config.maximumParticles ) {
			@Override
			public void save() {
				Game.config.maximumParticles = this.choosenString;
				SplatterParticle1.updateMaximum();
				SplatterParticle2.updateMaximum();
			}
		});
		//menuItemList.add( new Navigator( this, "Particle Life", Game.courierBitmap, 1, 4, optionsMenu) );
		menuItemList.add( new Chooser( this, "Fullscreen: ", Game.courierBitmap, 1, 3, new String[] { "ON", "OFF" }, Game.config.fullScreen ) {
			@Override
			public void save() {
				Game.config.fullScreen = this.choosenString;
				Game.mainWindow.togleFullScreen(this.choosenString);
				MainMenu.this.updateScale();
				Game.camera.updateScale();
			}
		});
		//menuItemList.add( new Navigator( this, "Fullscreen", Game.courierBitmap, 1, 3, optionsFullScreenMenu) );
		menuItemList.add( new Chooser( this, "V-Sync: ", Game.courierBitmap, 1, 2, new String[] { "ON", "OFF" }, Game.config.vSync ) {
			@Override
			public void save() {
				Game.config.vSync = this.choosenString;
				Game.mainWindow.togleVSync(this.choosenString);			}
		});
		//menuItemList.add( new Navigator( this, "V-Sync", Game.courierBitmap, 1, 2, optionsMenu) );
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, rootMenu) );
		optionsMenu.fill( menuItemList );
	}
	
	private void fillQuitMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>(); 
		menuItemList.add( new Quitter( "Quit", Game.courierBitmap, 1, 2 ) );
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, rootMenu) );		
		quitMenu.fill( menuItemList );
	}

	@Override
	public void tick() {
		
		if( Game.mainWindow.resized ) {
			updateScale();
			Game.camera.updateScale();
		}
		currentMenu.tick();
		
	}

	@Override
	public void changeMenu( Menu targetMenu ) {
		
		currentMenu.reset();
		currentMenu = targetMenu;
		currentMenu.reset();
	
	}

	@Override
	public void destroy() {
		
		rootMenu.destroy(); joinMenu.destroy();
		hostMenu.destroy(); optionsMenu.destroy();
		quitMenu.destroy();
		Game.disableGUIDrawer( guiDrawer );
		Game.disableTicker( ticker );
	
	}

	@Override
	public Menu getCurrentMenu() {
		
		return currentMenu;
	
	}

	@Override
	public void guiDraw( Program program ) {
		
		currentMenu.guiDraw( program );
	
	}

}
