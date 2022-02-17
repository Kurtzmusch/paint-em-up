package game.thing.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;


import game.D2Line;
import game.EuclideanMath;
import game.Game;
import game.MapDefinition;
import game.MapThing;
import game.NetworkProtocol;
import game.SingleMessage;
import game.network.Client;
import game.network.Request;
import game.thing.AINode;
import game.thing.server.SSSpawner;

public class CSMap {

	public MapDefinition mapDefinition;
	
	public LinkedList<MapThing> mapThingList;
	public LinkedList<SSSpawner> spawnerList;
	public LinkedList<CSWall> CSWallList;
	
	public CSFlag flagTeam1, flagTeam2;
	public CSFlag[] flagTeam = new CSFlag[3];

	public boolean linesGenerated;
	
	public boolean ready;
	public boolean gridReady, headerReady;
	private byte currentRowLayer1;
	private byte currentRowLayer2;
	public byte totalPaths;
	public boolean pathReady;
	public byte currentPath = 0;
	public Client client;
	public int currentRequestID;
	
	public LinkedList<LinkedList<AINode>> pathList = new LinkedList<LinkedList<AINode>>(); 
	public LinkedList<LinkedList<D2Line>> pathLineList = new LinkedList<LinkedList<D2Line>>();
	
	public SingleMessage singleMessage;
	
	public void togglePath( int path ) {
		LinkedList<AINode> nodeList = pathList.get( path );
		LinkedList<D2Line> lineList = pathLineList.get( path );
		for( AINode n : nodeList ) {
			n.toggleDynamicDraw();
		}
		for( D2Line l : lineList ) {
			if(Game.lineList.remove(l)) {}
			else {
				Game.lineList.add(l);
			}
		}
		
	}
	
	public CSMap( SingleMessage sm, Client aClient ) {
		mapThingList = new LinkedList<MapThing>();
		singleMessage = sm;
		mapDefinition = new MapDefinition();
		ready = false;
		client = aClient;
		currentRowLayer1 = 0;
		currentRowLayer2 = 0;
		currentRequestID = 10;
	}
	
	public void generateLines() {
		int c = 0;
		for(LinkedList<AINode> path : pathList ) {
			
			System.out.println("generating lines for path - " + c);
			c++;
			LinkedList<D2Line> lineList = new LinkedList<D2Line>();
			pathLineList.add( lineList );
			D2Line currentLine;
			boolean connectsWithFlag2 = false,connectsWithFlag1 = false;
			
			// CSWall radius used for crap path collision
			float diag = (float)(Math.sqrt(2))*16 + 1;
			
			// nodes can be 
			// leaf - looking for nodes that point to them)
			// trunk - they were leaf once, now they are are able to connect but will not change into leaf after connection
			// sprout - those connected to a leaf and will be transformed in leaf in the next iteration
			LinkedList<AINode> sproutList = new LinkedList<AINode>();
			LinkedList<AINode> trunkList = new LinkedList<AINode>();
			LinkedList<AINode> leafList = new LinkedList<AINode>();
			
			// navigate nodes from team1 to team2 (path points to team 1)
			
				// connect nodes to flag1
				int distanceToFlag1 = 0;
				if( flagTeam1 != null ) {
					for( AINode node : path ) {
						boolean collided = false;
						for( CSWall CSWall : CSWall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam1.x+16, flagTeam1.y+16, node.x+16, node.y+16,
							CSWall.x+16, CSWall.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							// check if its aligned with one of 8 directions
							if( (node.x == flagTeam1.x) || (node.y == flagTeam1.y) || ( (Math.abs(node.x-flagTeam1.x))==(Math.abs(node.y-flagTeam1.y)) )  ) {
								//Game.lineList.add( new D2Line( node.x+16, node.y+16, flagTeam1.x+16, flagTeam1.y+16 ) );
								node.connectsToFlag = true;
								sproutList.add(node);
								connectsWithFlag1 = true;
							}
						}
					}
				}
				if( connectsWithFlag1 == false ) { System.out.println("no node connects with flag 1"); return; }
				
				// connect nodes to nodes
				while( !sproutList.isEmpty() ) {
					distanceToFlag1 += 1;
					// sprouts turn into leafs and leafs into trunks
					for( AINode node : sproutList ) {
						node.type = "leaf";
						node.distanceToFlag1 = distanceToFlag1;
					}
					for( AINode node : leafList ) {
						node.type = "trunk";
					}
					leafList = sproutList;
					sproutList = new LinkedList<AINode>();
					for( AINode leafNode : leafList ) {
						//if( leafNode == spawnerNode ) { System.out.println("path to flag 1 connects with spawner"); }
						// check if connects to flag2
						boolean collided = false;
						for( CSWall CSWall : CSWall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, leafNode.x+16, leafNode.y+16,
							CSWall.x+16, CSWall.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							if( (leafNode.x == flagTeam2.x) || (leafNode.y == flagTeam2.y) || ( (Math.abs(leafNode.x-flagTeam2.x))==(Math.abs(leafNode.y-flagTeam2.y)) )  ) {
								currentLine = new D2Line( leafNode.x+16, leafNode.y+16, flagTeam2.x+16, flagTeam2.y+16 );
								lineList.add(currentLine);
								//Game.lineList.add( currentLine );
								connectsWithFlag2 = true;
								leafNode.connectsToFlag = true;
							}
						}
						// check if leaf connect to other nodes
						for( AINode generatedNode : path ) {
							boolean skip = false;
							//if( generatedNode.type.equals("trunk") || generatedNode.type.equals("leaf") ) { skip = true;}
							
							// ignore origin trunk and leaves with same origin
							if( (generatedNode.type.equals("leaf") ) && (leafNode.origin == generatedNode.origin) ) {
								skip=true;
							}
							if( generatedNode == leafNode.origin ) {
								skip=true;
							}
							if( !skip ) {
								boolean leafAndGeneratedCollided = false;
								for( CSWall CSWall : CSWall.instanceList ) {	
									if( EuclideanMath.distanceLinePoint( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16,
									CSWall.x+16, CSWall.y+16) < diag ) {
										leafAndGeneratedCollided = true;
										break;
									}
								}
								if( !leafAndGeneratedCollided ) {
									// check if node is at allowed angles
									if( (leafNode.x == generatedNode.x) || (leafNode.y == generatedNode.y) || ( (Math.abs(leafNode.x-generatedNode.x))==(Math.abs(leafNode.y-generatedNode.y)) )  ) {
										// cant change distance if connecting to a trunk
										currentLine = new D2Line( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16 );
										lineList.add(currentLine);
										//Game.lineList.add( currentLine );
										
										generatedNode.connectedNodesCloserToFlag1.add( leafNode );
										if( (generatedNode.type.equals("trunk")) || (generatedNode.type.equals("leaf") ) ) {
											skip = true;;
										}
										if(!skip) {
										generatedNode.origin = leafNode;
										sproutList.add( generatedNode );}
									}
								}
							}
						}
					}
				}
				if( connectsWithFlag2 == false ) { System.out.println("path doesnt connect with flag 2"); return;  }
				
				// navigate nodes from team2 to team1 (path points to team 2)
				
				// clear node types
				for( AINode node : path ) {
					node.type = "none";
					node.origin = null;
				}
				// clear lists
				sproutList = new LinkedList<AINode>();
				trunkList = new LinkedList<AINode>();
				leafList = new LinkedList<AINode>();
				
				// connect nodes to flag2
				int distanceToFlag2 = 0;
				if( flagTeam2 != null ) {
					for( AINode node : path ) {
						boolean collided = false;
						for( CSWall CSWall : CSWall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, node.x+16, node.y+16,
							CSWall.x+16, CSWall.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							// check if its aligned with one of 8 directions
							if( (node.x == flagTeam2.x) || (node.y == flagTeam2.y) || ( (Math.abs(node.x-flagTeam2.x))==(Math.abs(node.y-flagTeam2.y)) )  ) {
								currentLine = new D2Line( node.x+16, node.y+16, flagTeam2.x+16, flagTeam2.y+16 );
								lineList.add(currentLine);
								//Game.lineList.add( currentLine );
								node.connectsToFlag = true;
								sproutList.add(node);
								connectsWithFlag2 = true;
							}
						}
					}
				}
				if( connectsWithFlag2 == false ) { System.out.println("no node connects with flag 2"); return; }
				
				// connect nodes to nodes
				while( !sproutList.isEmpty() ) {
					System.out.println("sprouts: " + sproutList.size());
					distanceToFlag2 += 1;
					// sprouts turn into leafs and leafs into trunks
					for( AINode node : sproutList ) {
						node.type = "leaf";
						node.distanceToFlag2 = distanceToFlag2;
					}
					for( AINode node : leafList ) {
						node.type = "trunk";
					}
					leafList = sproutList;
					sproutList = new LinkedList<AINode>();
					for( AINode leafNode : leafList ) {
						//if( leafNode == spawnerNode ) { System.out.println("path to flag 2 connects with spawner"); }
						// check if connects to flag1
						boolean collided = false;
						for( CSWall CSWall : CSWall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam1.x+16, flagTeam1.y+16, leafNode.x+16, leafNode.y+16,
							CSWall.x+16, CSWall.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							if( (leafNode.x == flagTeam1.x) || (leafNode.y == flagTeam1.y) || ( (Math.abs(leafNode.x-flagTeam1.x))==(Math.abs(leafNode.y-flagTeam1.y)) )  ) {
								connectsWithFlag1 = true;
								leafNode.connectsToFlag = true;
							}
						}
						// check if leaf connect to other nodes
						for( AINode generatedNode : path ) {
							
							boolean skip=false;
							//if( generatedNode.type.equals("trunk") || generatedNode.type.equals("leaf") ) { skip = true;}
							
							// ignore origin trunk and leaves with same origin
							if( (generatedNode.type.equals("leaf") ) && (leafNode.origin == generatedNode.origin) ) {
								skip=true;
							}
							if( generatedNode == leafNode.origin ) {
								skip=true;
							}
							if(!skip) {
								boolean leafAndGeneratedCollided = false;
								for( CSWall CSWall : CSWall.instanceList ) {	
									if( EuclideanMath.distanceLinePoint( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16,
									CSWall.x+16, CSWall.y+16) < diag ) {
										leafAndGeneratedCollided = true;
										break;
									}
								}
								if( !leafAndGeneratedCollided ) {
									// check if node is at allowed angles
									if( (leafNode.x == generatedNode.x) || (leafNode.y == generatedNode.y) || ( (Math.abs(leafNode.x-generatedNode.x))==(Math.abs(leafNode.y-generatedNode.y)) )  ) {
										// cant change distance if connecting to a trunk	
										currentLine = new D2Line( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16 );
										lineList.add(currentLine);
										//Game.lineList.add( currentLine );
										generatedNode.connectedNodesCloserToFlag2.add( leafNode );
										if( (generatedNode.type.equals("trunk")) || (generatedNode.type.equals("leaf") ) ) {
											skip=true;;
										}
										if(!skip) {
										generatedNode.origin = leafNode;
										sproutList.add( generatedNode );}
									}
								}
							}
						}
					}
				}
				if( connectsWithFlag1 == false ) { System.out.println("path doesnt connect with flag 1"); return; }
		}
	}
	
	public void destroy() {
		
		for( MapThing t : mapThingList ) {
			t.destroy();
		}
		mapThingList.clear();
		for( LinkedList<D2Line> list : pathLineList ) {
			for( D2Line line : list ) {
				line.destroy();
			}
		}
		for( LinkedList<AINode> list : pathList ) {
			for( AINode node : list ) {
				node.destroy();
			}
		}
	}
	
	public void feedHeader( byte[] data ) {
		
		mapDefinition.theme = data[1];
		mapDefinition.teamColorIndex[1] = data[2];
		mapDefinition.teamColorIndex[2] = data[3];
		
		byte[] requestData = { NetworkProtocol.MAP_GRID, 1, 0 };
		DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
		client.request = new Request( client.socket, requestPacket, 10 );
		
	}
	
	public void feedGrid( byte[] data ) {
		switch( data[1] ) { // received layer
			case 1:
				currentRowLayer1 = data[2];
				// was the row received the expected?
				if( client.request.id == (10 + currentRowLayer1 + 0) ) {
					// feed row
					for( int i = 0; i < 64; i ++ ) {
						mapDefinition.byteLayer1[data[2]][i] = data[i+3];
					}
					singleMessage.add( "Downloading Map: " + (int)( (data[2]/128f)*100f ) + "%" );
					// request next row
					if( currentRowLayer1 == 63 ) {
						byte[] requestData = { NetworkProtocol.MAP_GRID, 2, 0 };
						DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
						client.request = new Request( client.socket, requestPacket, 10+64 );
					}
					else {
						byte[] requestData = { NetworkProtocol.MAP_GRID, 1, (byte) (currentRowLayer1+1) };
						DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
						client.request = new Request( client.socket, requestPacket, 10+currentRowLayer1+1 );
					}
					
				}
				break;
			case 2:
				currentRowLayer2 =  data[2];
				// was the row received the expected?
				if( client.request.id == (10 + 64 + currentRowLayer2 ) ) {
					// feed row
					for( int i = 0; i < 64; i ++ ) {
						mapDefinition.byteLayer2[data[2]][i] = data[i+3];
					}
					singleMessage.add( "Downloading Map: " + (int)( (data[2]/128f)*100f + 50f ) + "%" );
					if( client.request.id == 10+127 ) {
						ready = true;
						singleMessage.add( "Map downloaded successfully !");
						System.out.println("map download completed");
						
						createFromDefinition();
						//requestPathHeader();
						requestMatchMeta();
						
					}
					else {
						// request next row
						byte[] requestData = { NetworkProtocol.MAP_GRID, 2, (byte) (currentRowLayer2+1) };
						DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
						client.request = new Request( client.socket, requestPacket, 10+64+currentRowLayer2+1 );
					}
				}
				break;
		}
	}
	/*
	public void feed(byte[] data) {
		
		if( (data[2] >= 0) && (data[2] < 64) ) { // rows
			switch (data[1]) { // layer
				case 1:
					if( data[2] == currentRowLayer1 ) {
						for( int i = 0; i < 64; i ++ ) {
							mapDefinition.byteLayer1[data[2]][i] = data[i+3];
						}
						singleMessage.add( "Downloading Map: " + (int)( (data[2]/128f)*100f ) + "%" );
						currentRowLayer1 ++;
					}
					break;
				case 2:
					if( data[2] == currentRowLayer2 ) { // if received row is a needed one
						for( int i = 0; i < 64; i ++ ) {
							mapDefinition.byteLayer2[data[2]][i] = data[i+3];
						}
						singleMessage.add( "Downloading Map: " + (int)( (data[2]/128f)*100f + 50f ) + "%" );
						currentRowLayer2 ++;
					}
					break;
				case 3:
					gridReady = true;
					singleMessage.add( "Map downloaded successfully ! - "+mapDefinition.theme );
					System.out.println("map download completed");
					//createFromDefinition();
					break;
			}
		}
	}
	 */
	
	public void feedPathHeader( byte[] data ) {
		if( client.request.id == 10+128 ) {
			totalPaths = data[1];
			
			//requestMatchMeta(); // skip paths
			requestPathData( 0 );
			
		}
	}
	
	public void requestMatchMeta() {
		byte[] requestData = { NetworkProtocol.MATCH_META, (byte) 1 };
		DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
		client.request = new Request( client.socket, requestPacket, 200 );
	}
	
	public void feedPathData( byte[] data ) {
		
		byte receivedPath = data[1];
		// was the path received expected?
		if( ( 139 + receivedPath ) == client.request.id ) { // this was 139 - receivedPath... why ?
			byte totalNodes = data[2];
			LinkedList<AINode> nodeList = new LinkedList<AINode>();
			pathList.add( nodeList );
			for( int i = 3; i < totalNodes*2+3; i+=2 ) {
				nodeList.add( new AINode( data[i]*32, data[i+1]*32 ) );
			}
			if( (receivedPath+1) == totalPaths ) {
				System.out.println("Received all " + totalPaths + " paths");
				generateLines();
				requestMatchMeta();
				
			}
			else {
				requestPathData( receivedPath+1 );
			}
		}
	}
	
	public void requestPathHeader() {
		byte[] requestData = { NetworkProtocol.MAP_PATH_HEADER };
		DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
		client.request = new Request( client.socket, requestPacket, 10+128 );
	}
	
	public void requestPathData( int pathID ) {
		
		byte[] requestData = { NetworkProtocol.MAP_PATH_DATA, (byte) pathID };
		DatagramPacket requestPacket = new DatagramPacket( requestData, requestData.length, client.serverIP, client.serverPort );		
		client.request = new Request( client.socket, requestPacket, 10+129+pathID );
		
	}
	
	public void requestHeader( DatagramSocket socket, InetAddress ip, int port ) {
		try {
			socket.send( new DatagramPacket( new byte[] { NetworkProtocol.MAP_HEADER }, 1, ip, port  ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	public void request( DatagramSocket socket, InetAddress ip, int port ) {
		if( currentRowLayer1 < 64 ) {
			try {
				socket.send( new DatagramPacket( new byte[] { 4, 1, currentRowLayer1 }, 3, ip, port  ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			if( currentRowLayer2 < 64 ) {
				try {
					socket.send( new DatagramPacket( new byte[] { 4, 2, currentRowLayer2 }, 3, ip, port  ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else { // request theme
				try {
					socket.send( new DatagramPacket( new byte[] { 4, 3 }, 2, ip, port  ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
	}
	*/
	public void createFromDefinition() {
	
		for( int x = 0; x < 64; x ++ ) {
			for( int y = 0; y < 64; y ++ ) {
				
				byte b = mapDefinition.byteLayer1[x][y];
				
				add( x, y, b );
			} 
		}
		for( int x = 0; x < 64; x ++ ) {
			for( int y = 0; y < 64; y ++ ) {
				
				byte b = mapDefinition.byteLayer2[x][y];
				
				add( x, y, b );
			} 
		}
	}

	public void add( int x, int y, int placing ) {
			switch (placing) {
				case 1:
					mapThingList.add( new CSWall( x*32, y*32, mapDefinition.theme ) );
					break;
				case 2:
					mapThingList.add( new CSGround( x*32, y*32, mapDefinition.theme ) );
					break;
				case 3:
					mapThingList.add( new CSSpawner( x*32, y*32, 0, 0, this ) );
					break;
				case 4:
					mapThingList.add( new CSSpawner( x*32, y*32, 1, mapDefinition.teamColorIndex[1], this ) );					
					break;
				case 5:
					mapThingList.add( new CSSpawner( x*32, y*32, 2, mapDefinition.teamColorIndex[2], this ) );
					break;
				case 6:
					mapThingList.add( flagTeam1 = new CSFlag( x*32, y*32, 1, mapDefinition.teamColorIndex[1], mapDefinition.theme ) );
					flagTeam[1] = flagTeam1;
					break;
				case 7:
					mapThingList.add( flagTeam2 = new CSFlag( x*32, y*32, 2, mapDefinition.teamColorIndex[2], mapDefinition.theme ) );
					flagTeam[2] = flagTeam2;
					break;
			}
	}


}
