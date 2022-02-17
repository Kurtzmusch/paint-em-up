package game.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import game.Game;
import game.NetworkProtocol;
import game.ServerGame;
import game.TickerImplementor;
import game.interfaces.Destroyable;
import game.interfaces.Ticker;
import game.thing.server.PlayerOwner;
import game.thing.server.SBot;
import game.thing.server.SSMatch;
import game.thing.server.SSSpawner;
import game.thing.server.SSTeam;

public class Server implements Destroyable, Ticker {

	public boolean simulatePacketLoss = false;
	public float packetLossChance = 0.2f;
	public boolean simulatePacketChunking = false;
	public float packetChunkingAmount = 4;
	public int chunking;
	
	public TickerImplementor postTicker;
	public String map;
	public int port;
	public String type;
	public DatagramSocket socket;
	public Thread listener;
	volatile boolean listening;
	public int packetIndex;
	
	public LinkedList<PlayerOwner> humanOwners;
	public LinkedList<PlayerOwner> botOwners;
	public SSMatch match;
	
	private HashMap<String, SClient> clientList;
	private LinkedList<DatagramPacket> packetQueue;
	
	DatagramPacket sendPacket;
	
	ByteBuffer longBuffer;
	ByteBuffer intBuffer;
	
	public ServerGame serverGame;
	
	public Server( ServerGame aServerGame, String matchType, String map, int port ) {
		
		serverGame = aServerGame;
		chunking = 0;
		packetIndex = 0;
		
		packetQueue = new LinkedList<DatagramPacket>();
		clientList = new HashMap<String, SClient>();
		
		//map = Game.config.hostMap;
		//type = Game.config.hostType;		
		
		try {
			socket = new DatagramSocket(port);
			match = new SSMatch( this, matchType, false, map, socket );
			humanOwners = new LinkedList<PlayerOwner>();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		int totalPaths = 0;
		for( SSSpawner s : SSSpawner.instanceList ) {
			if(s.active ) {
				totalPaths++;
			}
		}
		for( SSSpawner s : SSSpawner.instanceList ) {
			if(s.active ) {
				
				s.createPath();
				s.botOwner = new SBot();
				s.notifyClientLeave();
				
			}			
		}*/
		
		postTicker = new TickerImplementor( 20 ) {
			public void tick() {
				Server.this.postTick();
			}
		};
		
		serverGame.enableTicker( postTicker );
		
		listen();
		
	}
	
	public SClient findClient( InetAddress aclientIP, int aclientPort ) {
		
		String fullAddress = aclientIP.toString() + ":" + aclientPort;
		
		return clientList.get(fullAddress);
	}
	
	public boolean checkClientPlaying( SClient aclient ) {
		
		for( SClient playingClient : match.team[0].clientList ) {
			if( aclient == playingClient ) {
				return true;
			}
		}
		for( SClient playingClient : match.team[1].clientList ) {
			if( aclient == playingClient ) {
				return true;
			}
		}
		for( SClient playingClient : match.team[2].clientList ) {
			if( aclient == playingClient ) {
				return true;
			}
		}
		
		return false;
	}
	
	public int checkFreePositions( int ateamID ) {
		if( ( ateamID == 0 ) && ( match.teamTyped ) ) {
			// return sum of team1 and team2 free positions
			return 
				( match.team[1].maximumSize - match.team[1].clientList.size() )+
				( match.team[2].maximumSize - match.team[2].clientList.size() );
		}
		
		return match.team[ateamID].maximumSize - match.team[ateamID].clientList.size();
	}
	
	public SSTeam getSmallerTeam() {
		if( match.team[2].clientList.size() >= match.team[1].clientList.size() ) {
			return match.team[1];
		}
		else {
			return match.team[2];
		}
	}
	
	public void handleJoinRequest( InetAddress clientIP, int clientPort, byte[] data ) {
		
		byte[] sendData = {};
		
		SClient client = findClient( clientIP, clientPort );
		if( client == null ) { return; }
		boolean clientAlreadyPlaying = false;
		if( checkClientPlaying( client ) ) {
			clientAlreadyPlaying = true;
			sendData = new byte[] { NetworkProtocol.PLAYER_JOIN, NetworkProtocol.PLAYER_JOIN_ALREADY_JOINED };
		}
		boolean sessionFull = false;
		if( !(checkFreePositions(0) > 0) ) {
			sessionFull = true;
			sendData = new byte[] { NetworkProtocol.PLAYER_JOIN, NetworkProtocol.PLAYER_JOIN_SESSION_FULL };
		}
		int desiredTeam = data[1];
		int selectedTeam = desiredTeam;
		if( !(checkFreePositions(desiredTeam) > 0) ) { selectedTeam = 0; }
		if( (!match.teamTyped) || (!match.allowTeamChoice) ) { selectedTeam = 0; } 
		
		if( ( !sessionFull ) && ( !clientAlreadyPlaying ) ) {
			if( match.teamTyped ) {
				if( selectedTeam == 0 ) {
					SSTeam smallerTeam;
					smallerTeam = getSmallerTeam();
					smallerTeam.addClient( client );
					selectedTeam = smallerTeam.id;
				}
				else {
					match.team[selectedTeam].addClient( client );
				}
			}
			else {
				match.team[0].addClient( client );
			}
			sendData = new byte[] { NetworkProtocol.PLAYER_JOIN, NetworkProtocol.PLAYER_JOIN_ACCEPTED };
			String msg;
			if( selectedTeam == 0 ) { msg = client.nickname + " joined match"; }
			else { msg = client.nickname + "joined team #" + selectedTeam; }
			
			byte[] stringData = (msg).getBytes(StandardCharsets.US_ASCII);
			byte[] messageData = new byte[stringData.length + 2];
			
			messageData[0] = NetworkProtocol.MESSAGE;
			messageData[1] = (byte) (stringData.length);
			for( int i = 0; i < stringData.length; i ++ ) {
				messageData[i+2] = stringData[i];
			}
			sendAllClients( messageData );
			
		}
		
		try {
			socket.send( new DatagramPacket( sendData, sendData.length , clientIP, clientPort ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private synchronized void addClient( DatagramPacket packet ) {
		byte[] data = packet.getData();
		//DatagramPacket sendPacket;
		InetAddress clientIP;
		int clientPort;
		Set<Entry<String, SClient>> es = clientList.entrySet();
		clientIP = packet.getAddress();
		clientPort = packet.getPort();
		String fullAddress = packet.getAddress().toString() + ":" + packet.getPort();
		if( !clientList.containsKey(fullAddress) ){
			String nickname = new String(data, StandardCharsets.US_ASCII);
			nickname = nickname.substring( 2, 2+data[1] );
			clientList.put(fullAddress, new SClient( clientIP, clientPort, nickname ) );
			// only send chat message if this is really a connection, and not just confirming
			
			System.err.println(nickname + " connected");
			byte[] stringData = (nickname+" connected" ).getBytes(StandardCharsets.US_ASCII);
			byte[] sendData = new byte[stringData.length + 2];
			
			sendData[0] = NetworkProtocol.MESSAGE;
			sendData[1] = (byte) (stringData.length);
			for( int i = 0; i < stringData.length; i ++ ) {
				sendData[i+2] = stringData[i];
			}
			
			for( Entry<String, SClient> entry : es  ) {
				clientIP = entry.getValue().ip;
				clientPort = entry.getValue().port;
				sendPacket = new DatagramPacket( sendData, sendData.length , clientIP, clientPort );
				try {
					socket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		// confirm connection
		sendPacket = new DatagramPacket( new byte[] { NetworkProtocol.CONNECTION_ACCEPTED }, 1, clientIP, clientPort );
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void process( DatagramPacket packet ) {
		
		byte[] data = packet.getData();
		//DatagramPacket sendPacket;
		InetAddress clientIP;
		int clientPort;
		Set<Entry<String, SClient>> es = clientList.entrySet();
		
		switch (data[0]) {
			case NetworkProtocol.CLIENT_INPUT:
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				String fAddress = packet.getAddress().toString() + ":" + packet.getPort();
				if( clientList.containsKey(fAddress) ) {
					SClient client = clientList.get(fAddress);
					client.spawner.feedClientInput( data );
				}
				break;
			case NetworkProtocol.PLAYER_SELECT_GUN:
				SClient client = findClient( packet.getAddress(), packet.getPort() );
				if( client != null ){
					client.changeWeaponType( data[1] );
					send(client, new byte[] { NetworkProtocol.PLAYER_SELECT_GUN, data[1] });
				}
				// TODO send back received type to avoid confirming wrong type
				// and also a deny if client not connected
				break;
			case NetworkProtocol.PLAYER_RESPAWN_CONFIRM:
				SSSpawner.instanceList.get( data[1] ).clientNotified = true;
				System.out.println("spawner " + SSSpawner.instanceList.get( data[1] ) + "received confirm");
				break;
			case NetworkProtocol.PLAYER_JOIN:
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				
				handleJoinRequest( clientIP, clientPort, data );
				System.out.println("server - player wants to join");
				break;
			case NetworkProtocol.MAP_HEADER:
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				match.map.sendHeader( socket, clientIP, clientPort );
				break;
			case NetworkProtocol.MAP_PATH_HEADER:
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				match.map.sendPath( data, socket, clientIP, clientPort );
				System.out.println("SERVER: client requested path_total");
				break;
			
			case NetworkProtocol.MAP_PATH_DATA:
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				match.map.sendPath( data, socket, clientIP, clientPort );
				System.out.println("SERVER: client requested path_data: " + data[1]);
				break;
			case NetworkProtocol.CONNECTION_REQUEST:
				addClient(packet);
				
				
				break;
			case NetworkProtocol.PING:// ping
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				sendPacket = new DatagramPacket( packet.getData(), packet.getData().length, clientIP, clientPort );
				try {
					socket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case NetworkProtocol.MAP_GRID:
				if( clientList.containsKey( packet.getAddress()+":"+packet.getPort() ) ) {
					clientIP = packet.getAddress();
					clientPort = packet.getPort();
					match.map.send( data[1], data[2], socket, clientIP, clientPort );
				}else {
					// TODO remove this after connection implementation
					clientIP = packet.getAddress();
					clientPort = packet.getPort();
					match.map.send( data[1], data[2], socket, clientIP, clientPort );
				}
				break;
			case NetworkProtocol.MATCH_META: // match request
				clientIP = packet.getAddress();
				clientPort = packet.getPort();
				byte mType = 0;
					for(byte i = 0; i < SSMatch.types.length ; i ++ ) {
						if ( match.type.equals(SSMatch.types[i]) ){
							mType = i;
							break;
						}
					}
					byte allow =  ((byte) (match.allowTeamChoice ? 1 : 0 ));
					System.err.println(match.type);
				
				sendPacket = new DatagramPacket( new byte[] { NetworkProtocol.MATCH_META, mType, allow }, 3 , clientIP, clientPort );
				try {
					socket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case NetworkProtocol.MESSAGE:
				System.err.println( new String(data, StandardCharsets.US_ASCII).substring(2) );
				String sender = clientList.get( packet.getAddress().toString() + ":" + packet.getPort() ).nickname;
				StringBuilder sb = new StringBuilder();
				sb.append(sender + " - ");
				sb.append(new String(data, StandardCharsets.US_ASCII).substring(2, 2+data[1]) );
				byte[] stringData = sb.toString().getBytes(StandardCharsets.US_ASCII);
				byte[] sendData = new byte[stringData.length+2];
				sendData[0] = NetworkProtocol.MESSAGE;
				sendData[1] = (byte) (data[1]+(sender + " - ").length());
				for( int c = 2; c < sendData.length; c++ ) {
					sendData[c] = stringData[c-2];
				}
				for( Entry<String, SClient> entry : es  ) {
					clientIP = entry.getValue().ip;
					clientPort = entry.getValue().port;
					sendPacket = new DatagramPacket( sendData, sendData.length , clientIP, clientPort );
					try {
						socket.send(sendPacket);
					} catch (IOException e) {
						// TODO Auto-generated catch blonglock
						e.printStackTrace();
					}
				}
				break;
				
		}
		
	}
	
	public void listen() {
		listening = true;
		listener = new Thread("Server Listener") {
			public void run() {
				while( listening ) { 
					DatagramPacket p = new DatagramPacket(new byte[256], 256);
					try {
						socket.receive(p);
						feedQueue(p);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		listener.start();
	}
	/*
	public void sendState() {
		
		byte[] data = new byte[256];
		DatagramPacket p;
		
		for( SClient client : clientList  ) {
						
			p = client.packet;
			p.setData(data);
			
			new Thread() {
				public void run() {
					try {
						socket.send(p);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};			
		}
		
	}*/

	@Override
	public void destroy() {
		Game.disableTicker( postTicker );
		match.destroy();
		listening = false;
		socket.close();
	}
	
	private synchronized void feedQueue( DatagramPacket packet ) {
		packetQueue.add(packet);
	
	}
	private synchronized DatagramPacket consumeQueue() {
		return packetQueue.pollFirst();
	}
	
	public byte[] longToBytes(long x) {
	    longBuffer = ByteBuffer.allocate(Long.BYTES);
	    longBuffer.putLong(x);
	    return longBuffer.array();
	}
	
	public byte[] intToBytes(int x) {
	    intBuffer = ByteBuffer.allocate(Integer.BYTES);
	    intBuffer.putInt(x);
	    return intBuffer.array();
	}

	public long bytesToLong(byte[] bytes) {
	    longBuffer = ByteBuffer.allocate(Long.BYTES);
	    longBuffer.put(bytes);
	    longBuffer.flip();//need flip 
	    return longBuffer.getLong();
	}
	public long bytesToInt(byte[] bytes) {
	    intBuffer = ByteBuffer.allocate(Integer.BYTES);
	    intBuffer.put(bytes);
	    intBuffer.flip();//need flip 
	    return intBuffer.getInt();
	}

	@Override
	public void tick() {
		if( simulatePacketChunking ) {
			if( chunking++ >= packetChunkingAmount ) {
				chunking = 0;
				while( true ) {	
					DatagramPacket p = consumeQueue();
					if (p == null) { break; }
					else { process(p); }
				}
			}
		}
		else {
			while( true ) {	
				DatagramPacket p = consumeQueue();
				if (p == null) { break; }
				else { process(p); }
			}
		}
		match.tick();
	}
	
	public void postTick() {
		sendGameState();
	}
	
	
	
	public void send( SClient aClient, byte[] aData ) {
		sendPacket = new DatagramPacket( aData, aData.length, aClient.ip, aClient.port );
		
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/***
	 * synchronized should garantee that im sending data to all clients without adding a client to list or
	 * removing from it 
	 */
	public synchronized void sendAllClients( byte[] data ) {
		InetAddress clientIP;
		int clientPort;
		Set<Entry<String, SClient>> es = clientList.entrySet();
		for( Entry<String, SClient> entry : es  ) {
			clientIP = entry.getValue().ip;
			clientPort = entry.getValue().port;
			sendPacket = new DatagramPacket( data, data.length , clientIP, clientPort );
			try {
				socket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	/***
	 * sends players states(positions, aim, and shot) for all clients
	 */
	public void sendGameState() {
		
		if( simulatePacketLoss ) {
			if( Math.random() < packetLossChance ) {
				packetIndex++;
				return;
			}
		}
		
		InetAddress clientIP;
		int clientPort;
		byte[] data = new byte[(16*9)+(2*4)+5];
		data[0] = NetworkProtocol.PLAYER_UPDATE;
		packetIndex++;
		if( packetIndex == Integer.MAX_VALUE ) { packetIndex = 0;}
		byte[] packetIndexArray = intToBytes( packetIndex );
		data[1] = packetIndexArray[0];
		data[2] = packetIndexArray[1];
		data[3] = packetIndexArray[2];
		data[4] = packetIndexArray[3];
		int i = 5;
		data[5] = (byte) (match.map.flagTeam1.x/32f);
		data[6] = (byte) ( (int)((match.map.flagTeam1.x%32f)*8f) );
		data[7] = (byte) (match.map.flagTeam1.y/32f);
		data[8] = (byte) ( (int)((match.map.flagTeam1.y%32f)*8f) );
		data[9] = (byte) (match.map.flagTeam2.x/32f);
		data[10] = (byte) ( (int)((match.map.flagTeam2.x%32f)*8f) );
		data[11] = (byte) (match.map.flagTeam2.y/32f);
		data[12] = (byte) ( (int)((match.map.flagTeam2.y%32f)*8f) );
		i = 13;
		for( SSSpawner s : SSSpawner.instanceList ) {
			if( s.active ) {
				if(s.player != null) {

					byte
					x1 = (byte) (s.player.x/32f) ,x2 = (byte)( (int) ((s.player.x%32f)*8f) ),
					y1 = (byte) (s.player.y/32f), y2 = (byte)( (int) ((s.player.y%32f)*8f) ),
					tx1 = (byte) (s.player.aimx/32f), tx2 = (byte) ((s.player.aimx%32f)*4),
					ty1 = (byte) (s.player.aimy/32f), ty2 = (byte) ((s.player.aimy%32f)*4);
					byte exploded = s.player.exploded;
					byte shot = s.player.weapon.shot;
					byte alive = 1;
					byte flagged = s.player.flagged;
					byte gunType = (byte) s.player.weapon.type;
					byte team = (byte) (s.player.team-1);
					if(team < 0) { team = 0;}
					// aset-type
					
					int finalByte= 0;
					
					finalByte = finalByte | (alive << 7);
					finalByte = finalByte | (shot << 6);
					finalByte = finalByte | (flagged << 5);
					finalByte = finalByte | (team << 4);
					finalByte = finalByte | gunType;
					data[i] = (byte)(finalByte);
					
					data[i+1] = x1;
					data[i+2] = x2;
					
					data[i+3] = y1;
					data[i+4] = y2;
					
					data[i+5] = tx1;
					data[i+6] = tx2;
					
					data[i+7] = ty1;
					data[i+8] = ty2;;
				
				}
				i += 9;
			}		
		}
		Set<Entry<String, SClient>> es = clientList.entrySet();
		for( Entry<String, SClient> entry : es  ) {
			clientIP = entry.getValue().ip;
			clientPort = entry.getValue().port;
			sendPacket = new DatagramPacket( data, data.length , clientIP, clientPort );
			try {
				socket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
