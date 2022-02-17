package game.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import engine.util.Matrix4x4f;
import game.ChatReceiver;
import game.DynamicDrawerImplementor;
import game.GUIDrawerImplementor;
import game.Game;
import game.NetworkProtocol;
import game.SingleMessage;
import game.Sprite;
import game.Text;
import game.TickerImplementor;
import game.controller.Guest;
import game.controller.Hoster;
import game.interfaces.Controller;
import game.interfaces.Destroyable;
import game.interfaces.DynamicDrawer;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;
import game.thing.client.CPlayer;
import game.thing.client.CSMatch;
import game.thing.client.CSSpawner;
import game.thing.client.ChatSender;

public class Client implements Destroyable, Ticker, GUIDrawer, DynamicDrawer, NetworkReceiver {

	private TickerImplementor ticker;
	private GUIDrawerImplementor guiDrawer;
	private DynamicDrawerImplementor dynamicDrawer;
	  
	public boolean simulatePacketLoss = false;
	public float packetLossChance = 0.0f;
	
	public SingleMessage singleMessage;
	public String map;
	public int port;
	public String type;
	public DatagramSocket socket;
	public Thread listener;
	volatile boolean listening;
	private HashMap<InetAddress, SClient> clientList;
	private LinkedList<DatagramPacket> packetQueue;
	public int serverPort;
	public InetAddress serverIP;
	private Text pingText;
	private Text packetLossText;
	private Matrix4x4f transformMatrix;
	public float aimX, aimY;
	public Controller owner;
	private boolean connected;
	private int connectClock;
	public Request request;
	boolean playing;
	public float cameraTargetX, cameraTargetY;
	public int lostPackets;
	
	public Matrix4x4f aimTransformMatrix;
	
	public CSSpawner spawner;
	
	public int currentIndex;

	public int lastRequestReceived = 0;
	
	private boolean spectating;
	
	public ChatReceiver chatReceiver;
	public CSMatch match;
	
	DatagramPacket sendPacket;
	private ChatSender chatSender;
	private float mouseSpeed, mouseLastX, mouseLastY;
	// clocks to resend lost packets
	private int pingClock;
	private int mapClock;
	private int matchClock;
	private int updatesClock;
	
	public int playerRepeatShot;
	
	public boolean playersUpdated = false;
	
	private Sprite aimSprite;
	
	ByteBuffer longBuffer;
	
	public Client( SingleMessage sm, ChatReceiver aChatReceiver, Controller owner ) {
		this.owner = owner;
		chatReceiver = aChatReceiver;
		transformMatrix = new Matrix4x4f();
		transformMatrix.identity();
		transformMatrix.translation( 0, 360-Game.courierBitmap.cellH, -10 );
		
		aimTransformMatrix = new Matrix4x4f();
		
		singleMessage = sm;
		chatSender = new ChatSender( this );
		
		lostPackets = 0;
		connectClock = 0;
		mapClock = 0;
		pingClock = 0;
		pingText = new Text( "Ping: ?" ,Game.courierBitmap, 1 );
		packetLossText = new Text( "PL: ?" ,Game.courierBitmap, 2 );
		
		aimSprite = Game.spriteList.get("spr_aim");

		mouseSpeed = Float.parseFloat( Game.config.mouseSpeed )/200f;
		
		packetQueue = new LinkedList<DatagramPacket>();
		
		match = new CSMatch( singleMessage, this );
		
		spectating = true;
		currentIndex = 0;
		
		dynamicDrawer = new DynamicDrawerImplementor( 20 ) {
			@Override
			public void dynamicDraw( Program program ){
				Client.this.dynamicDraw( program );
			}
		};
		
		playerRepeatShot = 0;
		
		try {
			if( owner instanceof Hoster) {
				serverIP = InetAddress.getByName("127.0.0.1");
			}else {
				serverIP = InetAddress.getByName(Game.config.joinIP);
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch blockFloat.parseFloat( Game.config.mouseSpeed );
			e1.printStackTrace();
		}
		if( owner instanceof Hoster) {
			serverPort = Integer.parseInt( Game.config.hostPort );
		}else {
			serverPort = Integer.parseInt( Game.config.joinPort );
		}
		
		try {
			socket = new DatagramSocket();
			listen();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		aimX = 31*32 + 16;
		aimY = 31*32 + 16;
		
		Game.camera.x = aimX;
		Game.camera.y = aimY;
		
		initiateRequests();
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void dynamicDraw( Program program ) {
		aimTransformMatrix.translation( aimX, aimY , 0 );
		program.loadMatrix( "transformMatrix", aimTransformMatrix );
		aimSprite.draw();
	}
	
	private void initiateRequests() {
				
		// connection
		byte[] nickname = Game.config.nickname.getBytes(StandardCharsets.US_ASCII);
		byte[] sendData = new byte[nickname.length + 2];
		
		sendData[0] = NetworkProtocol.CONNECTION_REQUEST;
		sendData[1] = (byte) (nickname.length);
		for( int i = 0; i < nickname.length; i ++ ) {
			sendData[i+2] = nickname[i];
		}
		DatagramPacket packet = new DatagramPacket( sendData, sendData.length, serverIP, serverPort );
		request = new Request( socket, packet, 1 );
		
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

	public long bytesToLong(byte[] bytes) {
	    longBuffer = ByteBuffer.allocate(Long.BYTES);
	    longBuffer.put(bytes);
	    longBuffer.flip();//need flip 
	    return longBuffer.getLong();
	}
	public int bytesToInt(byte[] bytes) {
	    longBuffer = ByteBuffer.allocate(Integer.BYTES);
	    longBuffer.put(bytes);
	    longBuffer.flip();//need flip 
	    return longBuffer.getInt();
	}
	
	private void listen() {
		listening = true;
		
		listener = new Thread("Client Listener") {
			public void run() {
				while( listening ) { 
					DatagramPacket p = new DatagramPacket(new byte[512], 512);
					try {
						socket.receive(p);
						feedQueue(p);
					}
					catch (SocketTimeoutException ste) {
						System.out.println(socket.isClosed());
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		};
		listener.start();
		
	}
	
	private void process( DatagramPacket packet ) {
		
		// TODO
		byte[] data = packet.getData();
		
		switch (data[0]) {
			case NetworkProtocol.PLAYER_RESPAWN_CONFIRM:
				byte spawnerID = data[1];
				spawner = CSSpawner.instanceList.get(spawnerID);
				byte[] sData = { NetworkProtocol.PLAYER_RESPAWN_CONFIRM, spawnerID };
				singleMessage.add("received spawner: " + spawnerID);
				playing = true;
				spectating = false;
				send(sData);
				break;
			case NetworkProtocol.PLAYER_RESPAWN_COUNTDOWN:
				singleMessage.add("Respawning in " + data[1] + " seconds");
				break;
			case NetworkProtocol.PLAYER_SELECT_GUN:
				if( request.id == 601 ) {
					request = new Request( socket, new DatagramPacket( new byte[0], 0 ), 10000  );
				}
				break;
			case NetworkProtocol.PLAYER_JOIN:
				if( request.id == 600 ) {
					request = new Request( socket, new DatagramPacket( new byte[0], 0 ), 10000  );
				}
				else {
					break;
				}
				switch( data[1] ) {
					
					case NetworkProtocol.PLAYER_JOIN_ACCEPTED:

						singleMessage.add("You have been placed on the queue, waiting...");
						break;
					case NetworkProtocol.PLAYER_JOIN_SESSION_FULL:
						singleMessage.add("The session is full");
						break;
					case NetworkProtocol.PLAYER_JOIN_ALREADY_JOINED:
						singleMessage.add("You have already joined");
						break;
				}
			case NetworkProtocol.CONNECTION_ACCEPTED:
				if( request.id == 1 ) {
					connected = true;
					byte[] requestData = { NetworkProtocol.MAP_HEADER };
					DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, serverIP, serverPort );
					request = new Request( socket, requestPacket, 2 );
					System.out.println("Client - r: connection accepted");
				}
				
				break;
			case NetworkProtocol.MAP_HEADER:
				if( request.id == 2 ) {
					match.map.feedHeader( data );
					System.out.println("Client - r: map header");
				}
				break;
			case NetworkProtocol.MAP_GRID:
				match.map.feedGrid( data );
				System.out.println("Client - r: map grid" + data[1] +" | "+ data[2] );
				break;
			case NetworkProtocol.MAP_PATH_HEADER:
				match.map.feedPathHeader( data ); //totalPaths = data[1];
				System.out.println( "Client - r: path header" );
				break;
			case NetworkProtocol.MAP_PATH_DATA:
				match.map.feedPathData( data );
				System.out.println( "Client - r: path data" );
				break;
			case NetworkProtocol.MATCH_META:
				match.feedMeta( data );
				if (owner instanceof Hoster) {
					((Hoster) owner).updateMenus(match);
				}
				if (owner instanceof Guest ) {
					((Guest ) owner).updateGuestMenu(match);
				}
				System.out.println("Client: - r: match meta");
				break;
				
			// unrequested packets
			case NetworkProtocol.PING:
				byte[] longBytes = new byte[Long.BYTES];
				for( int i = 1; i < Long.BYTES+1; i ++ ) {
					longBytes[i-1] = data[i];
				}
				long lastTime = bytesToLong(longBytes);
				pingText.destroy();
				pingText = new Text("ping: "+(System.currentTimeMillis() - lastTime)+"ms", Game.courierBitmap, 1 );
				break;
			case NetworkProtocol.MATCH_BOARD:
				match.feedBoard(data);
				break;
			case NetworkProtocol.PLAYER_UPDATE:
				int pID = 0;
				if(!match.ready) { break; }
				int packetIndex;
				byte[] packetIndexArray = new byte[4];
				packetIndexArray[0] = data[1];
				packetIndexArray[1] = data[2];
				packetIndexArray[2] = data[3];
				packetIndexArray[3] = data[4];
				packetIndex = bytesToInt(packetIndexArray);
				if( packetIndex != 0 ) {
					
					if( packetIndex <= currentIndex ) {
						break;
					}
					else {
						if( packetIndex > (currentIndex+1) ) {
							lostPackets += packetIndex - (currentIndex+1);
						}
					}
				}
				currentIndex = packetIndex;
				match.map.flagTeam1.x = (int) (data[5]*32f + (0b00000000_00000000_00000000_11111111 & data[6])/8f);
				match.map.flagTeam1.y = (int) (data[7]*32f + (0b00000000_00000000_00000000_11111111 & data[8])/8f);
				match.map.flagTeam2.x = (int) (data[9]*32f + (0b00000000_00000000_00000000_11111111 & data[10])/8f);
				match.map.flagTeam2.y = (int) (data[11]*32f + (0b00000000_00000000_00000000_11111111 & data[12])/8f);
				for( int i = 13; i < 13+(9*match.spawnerList.length); i += 9 ) {
					byte gunType =  (byte) ( (data[i] & 0b0000_1111) );
					byte alive = (byte) ( (data[i] & 0b1000_0000)>>>7);
					byte shot = (byte)( (data[i] & 0b0100_0000 ) >>> 6 );
					byte flagged = (byte)( (data[i] & 0b0010_0000 ) >>> 5 );
					byte team = (byte) ( (data[i] & 0b0001_0000) >>>4 );
					
					
					if( alive == 1 ) { 
						if(  match.spawnerList[pID].player == null ) {
							if( match.teamBasedType ) { team ++; }
							match.spawnerList[pID].player = new CPlayer( gunType, team, match.spawnerList[pID] );
							//System.out.println("CLIENT: player created with gun: " + gunType);
						
						}
						byte
						x1 = data[i+1],x2 = data[i+2],
						y1 = data[i+3], y2 = data[i+4],
						tx1 = data[i+5], tx2 = data[i+6],
						ty1 = data[i+7], ty2 = data[i+8];
						match.spawnerList[pID].player.flagged = flagged;
						match.spawnerList[pID].player.fed = true;
						
						match.spawnerList[pID].player.receivedX = x1*32f + (0b00000000_00000000_00000000_11111111 & x2)/8f;
						match.spawnerList[pID].player.receivedY = y1*32f + (0b00000000_00000000_00000000_11111111 & y2)/8f;
						// binary notation to extract bytes without considering the sign
						// aim doesnt need it because its divided bt 4 which is always < 128
						match.spawnerList[pID].player.aimx = tx1*32 + tx2/4;
						match.spawnerList[pID].player.aimy = ty1*32 + ty2/4;

						if( shot == 1 ) {	match.spawnerList[pID].player.weapon.shot = shot; }
					
					}
					else {
						if( match.spawnerList[pID].player != null ) {
							match.spawnerList[pID].player.shouldDie = true;
							match.spawnerList[pID].player = null;
						}
					}
					pID ++;
				}
				break;
			case NetworkProtocol.MESSAGE:
				
				String message = new String(data, StandardCharsets.US_ASCII);
				message = message.substring( 2, 2+data[1] );
				System.out.println("got message");
				if (owner instanceof Hoster) {
					((Hoster) owner).chatReceiver.add(message);
				}
				if (owner instanceof Guest) {
					((Guest) owner).chatReceiver.add(message);
				}
				break;
		}
	}

	public void send( byte[] data ) {
		try {
			socket.send( new DatagramPacket( data, data.length, serverIP, serverPort) );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void enableChat() {
		chatSender.enable();
	}
	
	@Override
	public void destroy() {
		
		socket.close();
		
		match.destroy();

		singleMessage.destroy();
		chatReceiver.destroy();
		//chatSender.destroy(); // TODO its ticked and gui draw by the clinet, maybe change this?
		//Game.disableGUIDrawer(guiDrawer);
		pingText.destroy();
		packetLossText.destroy();
		
		Game.disableDynamicDrawer(dynamicDrawer);
		
		listening = false;
		
		match.destroy();
	}
	
	public void input() {
		if( spectating || ! playing ) {
			if( Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_W] ) {
				//Game.camera.y += Game.camera.width/256;
				Game.camera.y += 4;
			}
			if( Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_S] ) {
				//Game.camera.y -= Game.camera.width/256;
				Game.camera.y -= 4;
			}
			Game.camera.y = Math.round(Game.camera.y);
			if( Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_A] ) {
				//Game.camera.x -= Game.camera.width/256;
				Game.camera.x -= 4;
			}
			if( Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_D] ) {
				//Game.camera.x += Game.camera.width/256;
				Game.camera.x += 4;
			}
			Game.camera.x = Math.round(Game.camera.x);
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_EQUAL] ) {
				Game.camera.width /= 2;
				Game.camera.height /= 2;
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_MINUS] ) {
				Game.camera.width *= 2;
				Game.camera.height *= 2;
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_0] ) {
				match.map.togglePath( 0 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_1] ) {
				match.map.togglePath( 1 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_2] ) {
				match.map.togglePath( 2 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_3] ) {
				match.map.togglePath( 3 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_4] ) {
				match.map.togglePath( 4 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_5] ) {
				match.map.togglePath( 5 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_6] ) {
				match.map.togglePath( 6 );
			}
			if( Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_7] ) {
				match.map.togglePath( 7 );
			}
		}
		else { // !spectating
			if( playing && (spawner.player != null) ) {
				
				float mx = Game.mainMouse.x;
				float my = Game.mainMouse.y;
				float dx = mx - mouseLastX;
				float dy = my - mouseLastY;
				
				aimX += dx*mouseSpeed;
				aimY -= dy*mouseSpeed;
				
				aimX = (aimX > (+Game.camera.width/2+Game.camera.x-1)) ? (+Game.camera.width/2+Game.camera.x)-1 : aimX;
				aimX = (aimX < (-Game.camera.width/2+Game.camera.x-1)) ? (-Game.camera.width/2+Game.camera.x)-1 : aimX;
				

				aimY = (aimY > (+Game.camera.height/2+Game.camera.y-1)) ? (+Game.camera.height/2+Game.camera.y)-1 : aimY;
				aimY = (aimY < (-Game.camera.height/2+Game.camera.y-1)) ? (-Game.camera.height/2+Game.camera.y)-1 : aimY;
				
				aimX += (spawner.player.x-spawner.player.lastX)/2f;
				aimY += (spawner.player.y-spawner.player.lastY)/2f;
				
				Game.mainMouse.set(640/4, 360/4, Game.mainWindow);
				
				
				byte playerShot = 0;
				if( spawner.player != null ) {
					spawner.player.aimx = aimX;
					spawner.player.aimy = aimY;
					if( Game.mainMouse.mbLeftPressed ) {
						playerShot = 1;
						playerRepeatShot = 7;
						//spawner.player.weapon.shot = 1;
					}
				}
				
				playerRepeatShot--;
				if( playerRepeatShot > 0 ) {
					playerShot = 1;
				}
				
				
				mouseLastX = 640/4;
				mouseLastY = 360/4;
				byte tx1, tx2, ty1, ty2;
				byte keyW = Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_W] ? (byte)(1) : (byte)(0);
				byte keyA = Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_A] ? (byte)(1) : (byte)(0);
				byte keyS = Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_S] ? (byte)(1) : (byte)(0);
				byte keyD = Game.mainKeyboard.keyDown[GLFW.GLFW_KEY_D] ? (byte)(1) : (byte)(0);
				tx1 = (byte) (spawner.player.aimx/32f); tx2 = (byte) ((spawner.player.aimx%32f)*8);
				ty1 = (byte) (spawner.player.aimy/32f); ty2 = (byte) ((spawner.player.aimy%32f)*8);
				byte[] sendData = { NetworkProtocol.CLIENT_INPUT, keyW, keyA, keyS, keyD, playerShot, tx1, tx2, ty1, ty2 };
				
				if( simulatePacketLoss ) {
				if( Math.random() > packetLossChance ) {
					send(sendData);
				}}
				else {
					send(sendData);
				}
			}
		}
	}

	
	public void ping() {
		if( pingClock++ == 30) {
			
			// use ping clock to count packets
			packetLossText = new Text( "PL: " + (String.valueOf( (lostPackets/30f)*100f ).substring(0, 2)) + "%" ,Game.courierBitmap, 2 );
			lostPackets = 0;
			
			pingClock = 0;
			byte[] longBytes = longToBytes( System.currentTimeMillis() );
			byte[] sendBytes = new byte[9];
			sendBytes[0] = NetworkProtocol.PING;
			sendBytes[1] = longBytes[0];
			sendBytes[2] = longBytes[1];
			sendBytes[3] = longBytes[2];
			sendBytes[4] = longBytes[3];
			sendBytes[5] = longBytes[4];
			sendBytes[6] = longBytes[5];
			sendBytes[7] = longBytes[6];
			sendBytes[8] = longBytes[7];
			
			try {
				socket.send( new DatagramPacket(sendBytes, sendBytes.length, serverIP, serverPort) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	public void requestMap() {
		if( mapClock++ == 1 ) {
			mapClock = 0;
			match.map.request( socket, serverIP, serverPort );
		}
	}
	public void requestHeader() {
			match.map.requestHeader( socket, serverIP, serverPort );
	}
	public void requestMatch() {
		if( matchClock++ == 30) {
			matchClock = 0;
			match.request( socket, serverIP, serverPort );
		}
	}
	public void requestPath() {
		match.map.requestPath(socket, serverIP, serverPort );
	}
			processNetworkInput();
	public void requestConnection() {
		// attempt connection every half second
		if( connectClock++ == 30 ) {
			connectClock = 0;
			try {
				byte[] nickname = Game.config.nickname.getBytes(StandardCharsets.US_ASCII);
				byte[] sendData = new byte[nickname.length + 2];
				
				sendData[0] = NetworkProtocol.CONNECTION_REQUEST;
				sendData[1] = (byte) (nickname.length);
				for( int i = 0; i < nickname.length; i ++ ) {
					sendData[i+2] = nickname[i];
				}
				socket.send( new DatagramPacket( sendData, sendData.length, serverIP, serverPort  ) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	*/
	@Override
	public void tick() {
	
		ping();
		
		if( request.id != 10000) {
			request.send();
		}
		
		processNetworkInput();
		input();

		chatSender.tick();
		
		if( !spectating && playing && spawner.player != null ) {
			Game.camera.width = 320;
			Game.camera.height = 180;
			float targetRange = 32f*6f;
			
			float camRangeX = Game.camera.width/2f;
			float maxCamX = targetRange - camRangeX;
			float xFraction = maxCamX/targetRange;
			float camRangeY = Game.camera.height/2f;
			float maxCamY = targetRange - camRangeY;
			float yFraction = maxCamY/targetRange;
			
			float camTY = spawner.player.y + ( (spawner.player.aimy - spawner.player.y)*yFraction );
			float camTX = spawner.player.x + ( (spawner.player.aimx - spawner.player.x)*xFraction );
			
			float camDX = camTX - Game.camera.x;
			float camDY = camTY - Game.camera.y;
			float camFriction = 1f/8f;
			
			Game.camera.x += camFriction*camDX;
			Game.camera.y += camFriction*camDY;
			
		}
		
	}
	
	public void processNetworkInput() {
		int i = 0;
		while( true ) {
			DatagramPacket p = consumeQueue();
			
			if (p == null) { break; }
			else { process(p); i ++; }
		}
		//if( i > 2 ) {System.out.println(i +" packets on a tick");}
	}


	@Override
	public void guiDraw( Program program ) {
		float textScale = Game.mainWindow.width/640;
		packetLossText.transformMatrix.translation( -Game.mainWindow.width/2f + "PING: xxxxx".length()*Game.courierBitmap.cellW*textScale, (Game.mainWindow.height/2f)-(Game.courierBitmap.cellH*2), 0 );
		pingText.transformMatrix.scale( textScale, textScale, 1 );
		pingText.transformMatrix.applyTranslation( -Game.mainWindow.width/2f, (Game.mainWindow.height/2f)-(Game.courierBitmap.cellH*textScale) , 0f );
		pingText.draw( program );
		packetLossText.draw( program );
		chatSender.guiDraw( program );
	}

	@Override
	public boolean getReceived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void request() {
		// TODO Auto-generated method stub
		
	}

}
