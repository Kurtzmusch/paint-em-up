
package game.thing.server;

import java.util.LinkedList;

import game.MapThing;
import game.NetworkProtocol;
import game.ServerGame;
import game.TickerImplementor;
import game.interfaces.Ticker;
import game.network.Client;
import game.network.SClient;
import game.network.Server;

public class SSSpawner extends MapThing implements Ticker {
	
	public int team;
	public SPlayer player;
	public PlayerOwner playerOwner;
	public int respawn;
	public int score, deaths, kills;
	
	public boolean clientNotified;
	
	public PlayerOwner humanOwner;
	public PlayerOwner botOwner;
	
	public SClient client;
	
	public SSMap map;
	public LinkedList<SSAINode> path;
	
	public boolean active;
	
	private TickerImplementor ticker;
	
	public final int respawnTime = 60*5;
	
	public static LinkedList<SSSpawner> instanceList = new LinkedList<SSSpawner>();
	
	public SSSpawner( int x, int y, int i, SSMap aMap ) {
		super( x, y );
		this.team = i;
		
		clientNotified = false;
		
		map = aMap;
		respawn = -1;
		score = 0;
		kills = 0;
		deaths = 0;

		
		ticker = new TickerImplementor( 1 ) {
			@Override
			public void tick() {
				SSSpawner.this.tick();
			}
		};
		
		
		
		instanceList.add(this);
		
	}
	
	public void activate() {
		map.match.server.serverGame.enableTicker(ticker);
	}
	
	public void deactivate() {
		map.match.server.serverGame.disableTicker(ticker);
	}
	
	
	public void feedClientInput( byte[] data ) {
		if( playerOwner == humanOwner ) {
			if( playerOwner.player != null ) {
				player.fed = true;
				player.VK_UP = data[1] > 0;
				player.VK_LEFT = data[2] > 0;
				player.VK_DOWN = data[3] > 0;
				player.VK_RIGHT = data[4] > 0;
				float aimX = data[6]*32f + (0b00000000_00000000_00000000_11111111 & data[7])/8f;
				player.aimx = aimX;
				float aimY = data[8]*32f + (0b00000000_00000000_00000000_11111111 & data[9])/8f;
				player.aimy = aimY;
				if( data[5] == 1) {
					// DONT OVERWRITE IF PLAYER HAS SHOT THIS TICK
					player.weapon.shoot();
				}
			}
		}
	}
	
	public void createPath() {
		
		int attempt = 0;
		while( path==null ) {
			
			path = map.createPath( this );
			System.out.println("SERVER: path attempt "+ attempt );
			attempt ++;
			// TODO infinite attempts
			
		}
		System.out.println("SERVER: path created");
		
	}
	
	
	public void notifyClientLeave() {
		humanOwner = null;
		playerOwner = null;
		if( player  != null ) {
			player.destroy();
			player = null;
		}
		playerOwner = botOwner;
		botOwner.spawner = this;
		botOwner.activate();
		respawn = respawnTime;
		System.err.println("SERVER:  respawn" + respawn); 
	}
	
	public void notifyPlayerKill( SPlayer killedPlayer ) {
		if( map.match.teamTyped ) {
			if( killedPlayer.team == team ) {
				kills--;
				map.match.teamKills[team]--;
			}
			else {
				kills++;
				map.match.teamKills[team]++;	
			}
		}
		else {
			kills ++;
		}
	}
	
	/***
	 * Kills a player without increasing the kill count and reseting the respawn clock
	 */
	public void happyKill() {
		score++;
		playerOwner.player.destroy();
		playerOwner.player = null;
	}
	
	public void notifyPlayerDeath() {
		playerOwner.player = null;
		player = null;
		deaths ++;
		if( map.match.teamTyped ) {
			map.match.teamDeaths[team]++;
		}
		if( !map.match.type.contains("Elimination") ) {
			respawn = respawnTime;
		}
		if( playerOwner == botOwner ) {
			if( humanOwner != null ) {
				botOwner.deactivate();
				playerOwner = humanOwner;
				playerOwner.spawner = this;
				clientNotified = false;
				deaths = 0;
				kills = 0;
			}
		}

	}
	
	public void tick() {
		//System.err.println("SERVER: respwn" + respawn ); 
		if( humanOwner != null) {
			if( respawn%60 == 0 ) {
				byte[] data = { NetworkProtocol.PLAYER_RESPAWN_COUNTDOWN, (byte)(respawn/60) };
				map.match.server.send( client, data );
				System.out.println("respawinging in"+(byte)(respawn/60));
			}
		}
		else {
			if( player == null ) {
				SClient tempClient = map.match.team[team].getOrfan();
				if( tempClient != null ) {
					client = tempClient;
					humanOwner = client.playerOwner;
					playerOwner = humanOwner;
					playerOwner.spawner = this;
					client.spawner = this;
					respawn = respawnTime;
					clientNotified = false;
					
				}
			}
		}
		if( respawn == 0 ) {
			//System.err.println("SERVER: respwn" + respawn );
			if( player == null ) {
				player = new SPlayer( this, x+16, y+16, playerOwner, team, playerOwner.weaponType );
				//System.err.println("SERVER: player created at " + x +" | "+ y + "| weapon: " + playerOwner.weaponType + "team" + team );
			}
		}
		if (respawn >= 0 ) {
			respawn --;
		}

		if( !clientNotified && client != null && playerOwner == humanOwner ) {
			byte[] data = { NetworkProtocol.PLAYER_RESPAWN_CONFIRM, (byte) instanceList.indexOf(this) };
			map.match.server.send( client, data );
		}
		
	}
	
	public void destroy() {
		instanceList.remove(this);
		if(player != null ) player.destroy();
	}
	
}
