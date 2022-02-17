package game.controller;

import java.net.DatagramPacket;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import game.ChatReceiver;
import game.DynamicDrawerImplementor;
import game.GUIDrawerImplementor;
import game.Game;
import game.NetworkProtocol;
import game.ServerGame;
import game.SingleMessage;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.Controller;
import game.interfaces.DynamicDrawer;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.menu.Changer;
import game.menu.Chooser;
import game.menu.Menu;
import game.menu.MenuController;
import game.menu.MenuItem;
import game.menu.Navigator;
import game.menu.Performer;
import game.menu.Typer;
import game.network.Client;
import game.network.Request;
import game.network.Server;
import game.thing.Weapon;
import game.thing.client.CSMatch;

public class Hoster implements Controller, MenuController, GUIDrawer, DynamicDrawer, Ticker {

	//public static Hoster hoster;
	
	public DynamicDrawerImplementor dynamicDrawer;
	public GUIDrawerImplementor guiDrawer;
	public TickerImplementor ticker;
	
	public Sprite aim;
	
	public Server server;
	public Client client;
	
	public ChatReceiver chatReceiver;
	public SingleMessage singleMessage;
	
	public Menu hosterMenu;
	public Menu hosterJoinMenu;
	
	public Menu currentMenu;
	
	public ServerGame serverGame;
	
	public boolean tickEnabled = false;
	
	public byte weapon;
	
	public Hoster() {
		
		singleMessage = new SingleMessage( Game.courierBitmap, 32 );
		chatReceiver = new ChatReceiver( Game.courierBitmap, 16, 8 );
		
		aim = Game.spriteList.get("spr_aim");
		
		serverGame = new ServerGame(Game.config.hostType, Game.config.hostMap, Integer.parseInt(Game.config.hostPort));
		
		client = new Client( singleMessage, chatReceiver, this );
		
		hosterMenu = new Menu();
		hosterJoinMenu = new Menu();
		
		fillHosterMenu();
		
		currentMenu = hosterMenu;
		
		//hoster = this;
		
		Game.mainMouse.set(640, 360, Game.mainWindow);
		
		ticker = new TickerImplementor( 0 ) {
			@Override
			public void tick() {
				Hoster.this.tick();
			}
			public void destroy() {
				Hoster.this.destroy();
			}
		};

		Game.enableTicker( ticker );
		
		dynamicDrawer = new DynamicDrawerImplementor( 6 ) {
			@Override
			public void dynamicDraw( Program program ) {
				Hoster.this.dynamicDraw( program );
			}
		};

		Game.enableDynamicDrawer( dynamicDrawer );
	
		guiDrawer = new GUIDrawerImplementor( 0 ) {
			@Override
			public void guiDraw( Program program ) {
				Hoster.this.guiDraw( program );
			}
		};

		Game.enableGUIDrawer( guiDrawer );
		
	}
	


	private void fillHosterJoinMenu( CSMatch match ) {
		// TODO Auto-generated method stub
		
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();		
		if( match.allowTeamChoice ) {
			menuItemList.add( new Performer( "Join Team #1", Game.courierBitmap, 1, 4 ) {
				public void perform() {
					Hoster.this.requestJoin( 1 );
				}
			});
			menuItemList.add( new Performer( "Join Team #2", Game.courierBitmap, 1, 3 ) {
				public void perform() {
					Hoster.this.requestJoin( 2 );
				}
			});
		}
		menuItemList.add( new Performer( "Auto-Balance", Game.courierBitmap, 1, 2 ) {
			public void perform() {
				Hoster.this.requestJoin( 0 );
			}
		});
		
		menuItemList.add( new Navigator( this, "Back", Game.courierBitmap, 1, 1, hosterMenu ) );		
		hosterJoinMenu.fill( menuItemList );
	}
	
	
	
	protected void requestJoin( int team ) {


		if( client.request.id == NetworkProtocol.NULL_REQUEST ) {
			byte[] requestData = { NetworkProtocol.PLAYER_JOIN, (byte)(team), weapon };
			DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
			client.request = new Request( client.socket, requestPacket, 600 );
		}
		
	}
	
	public void requestWeapon( String weaponName ) {
		if( client.request.id == NetworkProtocol.NULL_REQUEST ) {
			byte[] requestData = { NetworkProtocol.PLAYER_SELECT_GUN, (byte) Weapon.getIDFromName(weaponName) };
			DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
			client.request = new Request( client.socket, requestPacket, 601 );
		}
	}


	public void updateMenus( CSMatch match ) {
		updateHosterJoin( match );
		updateHosterWeapon( match );
	}
	
	public void updateHosterWeapon( CSMatch amatch ) {
		//if( amatch.allowGunChoince )
		hosterMenu.options[1].enable();
	}

	public void updateHosterJoin( CSMatch match ) {
		System.out.println( match.type + "|" + match.allowTeamChoice );
		if (match.teamBasedType ) {
			fillHosterJoinMenu(match);
			hosterMenu.options[0].enable();
		}
		else {
			hosterMenu.replace(new Performer( "Join", Game.courierBitmap, 1, 4  ) {
				public void perform() {
					Hoster.this.requestJoin( 0 );
				}
			}, 0 );
		}
	}

	private void fillHosterMenu() {
		LinkedList<MenuItem> menuItemList = new LinkedList<MenuItem>();		
		MenuItem joinM = new Navigator( this, "Join", Game.courierBitmap, 1, 4, hosterJoinMenu );
		joinM.disable();
		MenuItem weaponM = new Chooser( this, "Weapon: ", Game.courierBitmap, 1, 3,  new String[] { "Popper", "Shotter", "Zooker" }, "Deagle" ) {
			@Override
			public void save() {
				Hoster.this.requestWeapon( this.choosenString );
			}
		};
		weaponM.disable();
		menuItemList.add( joinM );
		menuItemList.add( weaponM );
		menuItemList.add( new Changer( "Drop Server", Game.courierBitmap, 1, 2, 0 ) );
		menuItemList.add( new Performer( "Hide", Game.courierBitmap, 1, 1  ) {
			public void perform() {
				Hoster.this.hideMenu();
				Hoster.this.client.enableChat();
			}
		});
		
		hosterMenu.fill( menuItemList );
	}

	protected void hideMenu() {
		currentMenu.hide();
	}

	@Override
	public void destroy() {
		
		serverGame.stopServerGame();
		client.destroy();
		
		
		singleMessage.destroy();
		chatReceiver.destroy();
		
		hosterMenu.destroy();
		hosterJoinMenu.destroy();
		
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
	public void guiDraw( Program program ) {
		currentMenu.guiDraw( program );
		client.guiDraw( program );
	}

	@Override
	public void changeMenu( Menu targetMenu ) {
		// TODO Auto-generated method stub
		currentMenu.reset();
		currentMenu = targetMenu;
		currentMenu.reset();
	}

	@Override
	public Menu getCurrentMenu() {
		// TODO Auto-generated method stub
		return currentMenu;
	}
	
	@Override
	public void dynamicDraw( Program program ) {
		// TODO Auto-generated method stub
		
	}

}
