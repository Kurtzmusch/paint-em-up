package game.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import game.ChatReceiver;
import game.DynamicDrawerImplementor;
import game.GUIDrawerImplementor;
import game.Game;
import game.NetworkProtocol;
import game.SingleMessage;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.Controller;
import game.interfaces.DynamicDrawer;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.menu.Changer;
import game.menu.Menu;
import game.menu.MenuController;
import game.menu.MenuItem;
import game.menu.Navigator;
import game.menu.Performer;
import game.menu.Typer;
import game.network.Client;
import game.network.Request;
import game.network.Server;
import game.thing.client.CSMatch;

public class Guest implements Controller, MenuController, GUIDrawer, DynamicDrawer, Ticker {
	
	public static Guest guest;
	
	public DynamicDrawerImplementor dynamicDrawer;
	public GUIDrawerImplementor guiDrawer;
	public TickerImplementor ticker;
	
	public Sprite aim;
	
	public Client client;
	
	public ChatReceiver chatReceiver;
	public SingleMessage singleMessage;
	
	public Menu guestMenu;
	public Menu guestSaveMenu;
	public Menu guestJoinMenu;
	
	public Menu currentMenu;
	
	public boolean tickEnabled = false;
	
	public Guest() {
		singleMessage = new SingleMessage( Game.courierBitmap, 32 );
		chatReceiver = new ChatReceiver( Game.courierBitmap, 16, 8 );
		
		aim = Game.spriteList.get("spr_aim");

		client = new Client( singleMessage, chatReceiver, this );
		
		guestMenu = new Menu();
		guestJoinMenu = new Menu();
		guestSaveMenu = new Menu();
		
		fillGuestMenu();
		
		currentMenu = guestMenu;
		
		guest = this;
		
		Game.mainMouse.set(640, 360, Game.mainWindow);
		
		ticker = new TickerImplementor( 0 ) {
			@Override
			public void tick() {
				Guest.this.tick();
			}
			public void destroy() {
				Guest.this.destroy();
			}
		};

		Game.enableTicker( ticker );
		
		dynamicDrawer = new DynamicDrawerImplementor( 6 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Guest.this.dynamicDraw( program );
			}
		};

		Game.enableDynamicDrawer( dynamicDrawer );
	
		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw( Program program ) {
				Guest.this.guiDraw( program );
			}
		};

		Game.enableGUIDrawer( guiDrawer );
	}
	
	/*
	private void fillGuestSaveMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();
		menuItemList.add( new Typer( this, "Save as: ", Game.courierBitmap, 1, 3, "" ) {
			@Override
			public void save() {
				Game.config.hosterSave = this.typedString;
			}
		});
		menuItemList.add( new Performer( "Save", Game.courierBitmap, 1, 2  ) {
			public void perform() {
				Guest.this.saveMap(Game.config.hosterSave);
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, hosterMenu ) );
		hosterSaveMenu.fill( menuItemList );
	}*/

	private void fillGuestMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();		
		MenuItem joinM = new Navigator( this, "Join", Game.courierBitmap, 1, 4, guestJoinMenu );
		joinM.disable();
		menuItemList.add( joinM );
		MenuItem saveM = new Navigator( this, "Save Map", Game.courierBitmap, 1, 3, guestSaveMenu );
		saveM.disable();
		menuItemList.add( saveM );
		menuItemList.add( new Changer( "Disconnect", Game.courierBitmap, 1, 2, 0 ) );
		menuItemList.add( new Performer( "Hide", Game.courierBitmap, 1, 1  ) {
			public void perform() {
				Guest.this.hideMenu();
				Guest.this.client.enableChat();
			}
		});
		
		guestMenu.fill(menuItemList );
	}
	
	public void updateGuestMenu( CSMatch match ) {
		System.out.println( match.type + "|" + match.allowTeamChoice );
		if (match.teamBasedType ) {
			fillGuestJoinMenu(match);
			guestMenu.options[0].enable();
			guestMenu.options[1].enable();
		}
		else {
			Performer performer = new Performer( "Join", Game.courierBitmap, 1, 4  ) {
				public void perform() {
					Guest.this.requestJoin( 2 );
				}
			};
			guestMenu.replace( performer , 0 );
			
			guestMenu.options[1].enable();
		}
		fillGuestSave();
	}
	
	private void fillGuestSave() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();
		menuItemList.add( new Typer( this, "Save as: ", Game.courierBitmap, 1, 3, "" ) {
			@Override
			public void save() {
				Game.config.guestSave = this.typedString;
			}
		});
		menuItemList.add( new Performer( "Save", Game.courierBitmap, 1,2  ) {
			public void perform() {
				Guest.this.saveMap(Game.config.guestSave);
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap,1, 1, guestMenu ) );
		guestSaveMenu.fill( menuItemList );
	}
	
	private void fillGuestJoinMenu( CSMatch match ) {
		// TODO Auto-generated method stub
		
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();		
		if( match.allowTeamChoice ) {
			menuItemList.add( new Performer( "Join Team #1", Game.courierBitmap, 1, 4 ) {
				public void perform() {
					Guest.this.requestJoin( 1 );
				}
			});
			menuItemList.add( new Performer( "Join Team #2", Game.courierBitmap, 1, 3 ) {
				public void perform() {
					Guest.this.requestJoin( 2 );
				}
			});
		}
		menuItemList.add( new Performer( "Auto-Balance", Game.courierBitmap, 1, 2 ) {
			public void perform() {
				Guest.this.requestJoin( 0 );
			}
		});
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, guestMenu ) );		
		guestJoinMenu.fill( menuItemList );
	}
		
	protected void requestJoin( int team ) {
		if( client.request.id == 10000 ) {
			byte[] requestData = { NetworkProtocol.PLAYER_JOIN, (byte)(team) };
			DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
			client.request = new Request( client.socket, requestPacket, 600 );
		}
		
	}

	protected void hideMenu() {
		currentMenu.hide();
	}
	
	protected void saveMap(String guestSave) {
		
		String mapsFolder = Game.applicationRoot.jarFileLocation + "/peumaps";
		if( ! new File(mapsFolder).exists() ) {
			new File(mapsFolder).mkdir();
		}
		FileOutputStream fos;
		ObjectOutputStream oos;
		try{
			File saveFile = new File( mapsFolder +"/"+ guestSave + ".map" );
			if( !saveFile.exists() ) {
			    fos = new FileOutputStream( saveFile );
			    oos = new ObjectOutputStream(fos);
			    oos.writeObject(client.match.map.mapDefinition);
			    singleMessage.add( "Map saved as " + guestSave );
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

	@Override
	public void destroy() {
		client.destroy();
		guestMenu.destroy();
		guestSaveMenu.destroy();
		guestJoinMenu.destroy();
		chatReceiver.destroy();
		singleMessage.destroy();
		Game.disableTicker( ticker );
		Game.disableDynamicDrawer( dynamicDrawer );
		Game.disableGUIDrawer( guiDrawer );
	}
	
	public void input() {
		if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_ESCAPE] ) {
			currentMenu.hidden = false;
		}
	}

	@Override
	public void tick() {
		input();
		currentMenu.tick();
		client.tick();
	}

	@Override
	public void dynamicDraw(Program program) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void guiDraw(Program program) {
		currentMenu.guiDraw( program );
		client.guiDraw( program );
	}

	@Override
	public void changeMenu(Menu targetMenu) {
		currentMenu.reset();
		currentMenu = targetMenu;
		currentMenu.reset();
	}

	@Override
	public Menu getCurrentMenu() {
		return currentMenu;
	}
	
}
