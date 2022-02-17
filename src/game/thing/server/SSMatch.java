
package game.thing.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import game.Game;
import game.MapDefinition;
import game.NetworkProtocol;
import game.network.Server;

public class SSMatch {

	public SSMap map;
	public String type;
	public boolean allowTeamChoice;
	public DatagramSocket socket;
	public boolean teamTyped;
	public Server server;
	
	private short teamScore[] = { 0, 0, 0 };
	
	short teamKills[] = { 0, 0, 0 };
	short teamDeaths[] = { 0, 0, 0 };
	private int statusClock;
	private byte maxPlayers;
	
	private boolean ready, pathReady;
	
	public SSTeam[] team;
	
	public static String[] types = { "DeathMatch", "Team DeathMatch", "?",
			"Capture The Flag", "Elimination", "Team Elimination" };
	
	public SSMatch( Server aServer, String type, boolean forceBalance, String file, DatagramSocket aSocket ) {
		
		team = new SSTeam[3];
		
		server = aServer;
		
		statusClock = 30;
		
		socket = aSocket;
		
		allowTeamChoice = !forceBalance;
		this.type = type;
		
		int typePosition = 0;
		for( String s : types ) {
			if( s.equals( type ) ) {
				break;
			}
			typePosition ++;
		}
		if( typePosition%2 == 0 ) {
			teamTyped = false;
		}
		else {
			teamTyped = true;
		}
		
		
		ObjectInputStream ois;
		FileInputStream fis;
		
		boolean loaded = false;
		try {
			String mapsFolder = Game.applicationRoot.jarFileLocation + "/peumaps/";
			fis = new FileInputStream( mapsFolder + file + ".map" );
		    ois = new ObjectInputStream(fis);
		    MapDefinition mapDefinition = (MapDefinition) ois.readObject();
		    fis.close();
		    ois.close();
		    map = new SSMap( mapDefinition, this );
		    loaded = true;
		} catch (Exception e) {
		    e.printStackTrace();    
		}
		if( !loaded ) {
			System.out.println("Loading from default");
			InputStream mapStream = Game.class.getResourceAsStream("/dm/" + file + ".map");
			try {
				if( mapStream == null ) { return;  }
				ois = new ObjectInputStream(mapStream);
				MapDefinition mapDefinition = (MapDefinition) ois.readObject();
				ois.close();
				mapStream.close();
			    map = new SSMap( mapDefinition, this );
			    loaded = true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		maxPlayers = 0;
		int[] teamSize = new int[3];
		for( SSSpawner s : SSSpawner.instanceList ) {
			teamSize[s.team]++;
		}
		
		team[0] = new SSTeam( 0, teamSize[0] );
		team[1] = new SSTeam( 1, teamSize[1] );
		team[2] = new SSTeam( 2, teamSize[2] );
		
		
		for( SSSpawner s : SSSpawner.instanceList  ) {
			if( teamTyped ) {
				if( s.team != 0 ) {
					s.active = true;
					maxPlayers ++;
					System.out.println("spawner activated");
				}
			}
			else {
				if( s.team == 0 ) {
					s.active = true;
					maxPlayers ++;
					System.out.println("team 0 spawner activated");
				}
			}
		}
		
		pathReady = false; // its ready when all ai paths gets created
		ready = false; // ready after bots get created
		Thread pathCreator = new Thread("Server PathCreator") {
			public void run() {
				int totalPaths = 0;
				int currentPath = 1;
				for( SSSpawner s : SSSpawner.instanceList ) {
					if(s.active ) {
						totalPaths++;
					}
				}
				for( SSSpawner s : SSSpawner.instanceList ) {
					if(s.active ) {
						
						String pathnumberstring = "Generating AI paths " + currentPath + "/" + totalPaths;
						byte[] stringBytes = pathnumberstring.getBytes(StandardCharsets.US_ASCII);
						byte[] sendData = new byte[pathnumberstring.length()+2];
						sendData[0] = NetworkProtocol.MESSAGE;
						sendData[1] = (byte) stringBytes.length;
						for(int i = 0; i < stringBytes.length; i ++ ) {
							sendData[i+2] = stringBytes[i];
						}
						server.sendAllClients(sendData);
						s.createPath();
						currentPath++;
					}			
				}
				setPathReady();
			}
		};
		pathCreator.start();
		
	}
	public synchronized void setPathReady() {
		pathReady = true;
	}
	
	public synchronized boolean getPathReady() {
		return pathReady;
	}

	
	public void tick() {
		if( !getPathReady() ) {
			return;
		}
		else {
			if( !ready ) {
			for( SSSpawner s : SSSpawner.instanceList ) {
				if(s.active ) {
					s.botOwner = new SBot();
					s.activate();// for tick
					s.notifyClientLeave();
					
				}			
			}ready = true;}
		}
		if( type.contains("DeathMatch") ) {
			updateScore();
		}
		if( type.contains("Elimination") ) {
			checkPlayerCount();
		}
		
		if( statusClock -- == 0 ) {
			statusClock = 60;
			sendMatchStatus();
		}
		
	}
	
	/***
	 * Counts players for each team and spawner, reset spawn clock, update team score
	 */
	public void checkPlayerCount() {
		
		switch( type ) {
			case "Elimination":
				int playerNumber = 0;
				SPlayer playerAlive = null;
				for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
					if( activeSpawner.active ) {
						if( activeSpawner.player != null ) {
							playerNumber ++;
							playerAlive = activeSpawner.player;
						}
					}
				}
				if( playerNumber <= 1 ) {
					if( playerNumber == 1) {
						playerAlive.happyDestroy();
						//teamScore[playerAlive.team] ++;
					}
					else {
						for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
							if( activeSpawner.active ) {
								if( activeSpawner.respawn == -1 ) { // make sure spawner is not counting down
									activeSpawner.respawn = activeSpawner.respawnTime;
								}
							}
						}
					}
				}
				break;
			case "Team Elimination":
				int[] teamPlayerCount = { 0, 0, 0 };
				for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
					if( activeSpawner.active ) {
						if( activeSpawner.player != null ) {
							teamPlayerCount[activeSpawner.team]++;
						}
					}
				}
				if( (teamPlayerCount[1] == 0) || (teamPlayerCount[2] == 0) ) {
					if( teamPlayerCount[1] == 0 ) {
						for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
							if( activeSpawner.active ) {
								if( activeSpawner.team == 2 ) {
									if( activeSpawner.player != null ) {
										activeSpawner.player.happyDestroy();
									}
								}
							}
						}
						teamScore[2]++;
					}
					if( teamPlayerCount[2] == 0 ) {
						for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
							if( activeSpawner.active ) {
								if( activeSpawner.team == 1 ) {
									if( activeSpawner.player != null ) {
										activeSpawner.player.happyDestroy();
									}
								}
							}
						}
						teamScore[1]++;
					}
					if( (teamPlayerCount[1] == 0) && (teamPlayerCount[2] == 0) ) {
						for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
							if( activeSpawner.active ) {
								if( activeSpawner.respawn == -1 ) { // make sure spawner is not counting down
									activeSpawner.respawn = activeSpawner.respawnTime;
								}
							}
						}
					}
					
				}
				
				break;
		}
		
	}
	
	public void updateScore() {
		switch( type ) {
		case "DeathMatch":
			for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
				if( activeSpawner.active ) {
					activeSpawner.score = (activeSpawner.kills + (activeSpawner.kills-activeSpawner.deaths) ); 
				}
			}
			break;
		case "Team DeathMatch":
			
			teamScore[1] = (short) (teamKills[1]+ (teamKills[1]-teamDeaths[1]));
			teamScore[2] = (short) (teamKills[2] + (teamKills[2]-teamDeaths[2]));
			for( SSSpawner activeSpawner : SSSpawner.instanceList ) {
				if( activeSpawner.active ) {
					activeSpawner.score = (activeSpawner.kills + (activeSpawner.kills-activeSpawner.deaths) ); 
				}
			}
			break;
		}
	}

	

	private void sendMatchStatus() {
		byte[] data = new byte[(maxPlayers*3)+(maxPlayers*17)+3];
		
		data[0] = NetworkProtocol.MATCH_BOARD;
		int i = 1;
		for( SSSpawner s: SSSpawner.instanceList ) {
			if( s.active ) {
				byte[] stringData = (s.playerOwner.nickname).getBytes(StandardCharsets.US_ASCII);
				
				data[i] = (byte) s.score;
				i++;
				data[i] = (byte) s.kills;
				i++;
				data[i] = (byte) s.deaths;
				i++;
				data[i] = (byte) stringData.length; i++;
				for( int ii = 0; ii < stringData.length; ii++ ) {
					data[i] = stringData[ii];
					i++;
				}
			}
		}
		data[i] = (byte) teamScore[1];
		data[i+1] = (byte) teamScore[2];
		
		server.sendAllClients( data );
	}
	
	public void destroy() {
		// TODO implement own tick without rely on server calling it and clean it here
		map.destroy();
	}
	
}
