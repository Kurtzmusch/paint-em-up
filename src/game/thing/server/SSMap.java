
package game.thing.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;

import game.EuclideanMath;
import game.MapDefinition;
import game.NetworkProtocol;

public class SSMap {

	public MapDefinition mapDefinition;
	
	public LinkedList<SSFlag> flagList;
	public LinkedList<SSSpawner> spawnerList;
	public LinkedList<SSWall> wallList;
	public LinkedList<SSGround> groundList;
	
	public SSFlag flagTeam1, flagTeam2;
	public SSFlag flagTeam[] = new SSFlag[3];
	
	public SSMatch match;
	
	/***
	 * generate ai nodes placed randomly on this maps grounds
	 * @return
	 */
	public LinkedList<SSAINode> generateNodes() {
		
		LinkedList<SSAINode> returnList = new LinkedList<SSAINode>();
		
		// create some random nodes over ground instances
		
		for( SSGround g : SSGround.instanceList ) {
			// make sure theres no wall here
			if( mapDefinition.byteLayer2[g.x/32][g.y/32] != 1 ) {
				if ( Math.random() > 0.3d ) {
					returnList.add( new SSAINode( g.x, g.y ) ) ;
				}
			}
			else {
				//mapDefinition.byteLayer2[g.x/32][g.y/32] = 0; to see ground under
				//System.err.println("ground under wall");
			}
		}
		
		System.out.println(returnList.size());
		{
			// remove half of them uniformly
			
			Iterator<SSAINode> it = returnList.iterator();
			boolean skip = true;
			while(it.hasNext()) {
				SSAINode instance = it.next();
				if( !skip ) { it.remove(); instance.destroy(); }
				skip = !skip;
			}
		}
		System.out.println(returnList.size());
		{
			// remove half of them uniformly
			
			Iterator<SSAINode> it = returnList.iterator();
			boolean skip = true;
			while(it.hasNext()) {
				SSAINode instance = it.next();
				if( !skip ) { it.remove(); instance.destroy(); }
				skip = !skip;
			}
		}
		System.out.println(returnList.size());
		return returnList;
	}
	
	/***
	 * calls generateNodes and creates the connections between them trying to reach both flags and the desired spawner.
	 * If the path doesnt connect with both flags and the given spawner, this method returns null
	 * @param spawner
	 * @return
	 */
	public LinkedList<SSAINode> createPath( SSSpawner spawner ) {
		
		LinkedList<SSAINode> generatedList = generateNodes();
		
		// puts a node in the spawner position
		SSAINode spawnerNode = new SSAINode( spawner.x, spawner.y );
		generatedList.add( spawnerNode );
		
		boolean connectsWithFlag1 = false;
		boolean connectsWithFlag2 = false;
		boolean connectsWithSpawner = false;
		
		// wall radius used for crap path collision
		float diag = (float)(Math.sqrt(2))*16 + 1;
		
		// nodes can be 
		// leaf - looking for nodes that point to them)
		// trunk - they were leaf once, now they are are able to connect but will not change into leaf after connection
		// sprout - those connected to a leaf and will be transformed in leaf in the next iteration
		LinkedList<SSAINode> sproutList = new LinkedList<SSAINode>();
		LinkedList<SSAINode> trunkList = new LinkedList<SSAINode>();
		LinkedList<SSAINode> leafList = new LinkedList<SSAINode>();
		
		// navigate nodes from team1 to team2 (path points to team 1)
		
			// connect nodes to flag1
			int distanceToFlag1 = 0;
			if( flagTeam1 != null ) {
				for( SSAINode node : generatedList ) {
					boolean collided = false;
					for( SSWall wall : SSWall.instanceList ) {
						
						if( EuclideanMath.distanceLinePoint( flagTeam1.x+16, flagTeam1.y+16, node.x+16, node.y+16,
						wall.x+16, wall.y+16) < diag ) {
							collided = true;
							break;
						}
					}
					if( !collided ) {
						// check if its aligned with one of 8 directions
						if( (node.x == flagTeam1.x) || (node.y == flagTeam1.y) || ( (Math.abs(node.x-flagTeam1.x))==(Math.abs(node.y-flagTeam1.y)) )  ) {
							node.connectsToFlag = true;
							sproutList.add(node);
							connectsWithFlag1 = true;
						}
					}
				}
			}
			if( connectsWithFlag1 == false ) {
				System.out.println("no node connects with flag 1");
				for( SSAINode generatedNode : generatedList ) {
					generatedNode.destroy();
				}
				return null;
			}
			
			// connect nodes to nodes
			while( !sproutList.isEmpty() ) {
				
				distanceToFlag1 += 1;
				// sprouts turn into leafs and leafs into trunks
				for( SSAINode node : sproutList ) {
					node.type = "leaf";
					node.distanceToFlag1 = distanceToFlag1;
				}
				for( SSAINode node : leafList ) {
					node.type = "trunk";
				}
				leafList = sproutList;
				sproutList = new LinkedList<SSAINode>();
				for( SSAINode leafNode : leafList ) {
					if( leafNode == spawnerNode ) { System.out.println("path to flag 1 connects with spawner"); }
					// check if connects to flag2
					boolean collided = false;
					for( SSWall wall : SSWall.instanceList ) {
						
						if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, leafNode.x+16, leafNode.y+16,
						wall.x+16, wall.y+16) < diag ) {
							collided = true;
							break;
						}
					}
					if( !collided ) {
						if( (leafNode.x == flagTeam2.x) || (leafNode.y == flagTeam2.y) || ( (Math.abs(leafNode.x-flagTeam2.x))==(Math.abs(leafNode.y-flagTeam2.y)) )  ) {
							connectsWithFlag2 = true;
							leafNode.connectsToFlag = true;
						}
					}
					// check if leaf connect to other nodes
					for( SSAINode generatedNode : generatedList ) {
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
							for( SSWall wall : SSWall.instanceList ) {	
								if( EuclideanMath.distanceLinePoint( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16,
								wall.x+16, wall.y+16) < diag ) {
									leafAndGeneratedCollided = true;
									break;
								}
							}
							if( !leafAndGeneratedCollided ) {
								// check if node is at allowed angles
								if( (leafNode.x == generatedNode.x) || (leafNode.y == generatedNode.y) || ( (Math.abs(leafNode.x-generatedNode.x))==(Math.abs(leafNode.y-generatedNode.y)) )  ) {
									// cant change distance if connecting to a trunk								
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
			if( connectsWithFlag2 == false ) {
				System.out.println("path doesnt connect with flag 2");
				for( SSAINode generatedNode : generatedList ) {
					generatedNode.destroy();
				}
				return null; 
			}
			
			// navigate nodes from team2 to team1 (path points to team 2)
			
			// clear node types
			for( SSAINode node : generatedList ) {
				node.type = "none";
				node.origin = null;
			}
			// clear lists
			sproutList = new LinkedList<SSAINode>();
			trunkList = new LinkedList<SSAINode>();
			leafList = new LinkedList<SSAINode>();
			
				// connect nodes to flag2
				int distanceToFlag2 = 0;
				if( flagTeam2 != null ) {
					for( SSAINode node : generatedList ) {
						boolean collided = false;
						for( SSWall wall : SSWall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, node.x+16, node.y+16,
							wall.x+16, wall.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							// check if its aligned with one of 8 directions
							if( (node.x == flagTeam2.x) || (node.y == flagTeam2.y) || ( (Math.abs(node.x-flagTeam2.x))==(Math.abs(node.y-flagTeam2.y)) )  ) {
								node.connectsToFlag = true;
								sproutList.add(node);
								connectsWithFlag2 = true;
							}
						}
					}
				}
				if( connectsWithFlag2 == false ) {
					System.out.println("no node connects with flag 2");
					for( SSAINode generatedNode : generatedList ) {
						generatedNode.destroy();
					}
					return null;
				}
				
				// connect nodes to nodes
				while( !sproutList.isEmpty() ) {
					
					distanceToFlag2 += 1;
					// sprouts turn into leafs and leafs into trunks
					for( SSAINode node : sproutList ) {
						node.type = "leaf";
						node.distanceToFlag2 = distanceToFlag2;
					}
					for( SSAINode node : leafList ) {
						node.type = "trunk";
					}
					leafList = sproutList;
					sproutList = new LinkedList<SSAINode>();
					for( SSAINode leafNode : leafList ) {
						if( leafNode == spawnerNode ) { System.out.println("path to flag 2 connects with spawner"); }
						// check if connects to flag1
						boolean collided = false;
						for( SSWall wall : SSWall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam1.x+16, flagTeam1.y+16, leafNode.x+16, leafNode.y+16,
							wall.x+16, wall.y+16) < diag ) {
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
						for( SSAINode generatedNode : generatedList ) {
							
							boolean skip=false;
							//if( generatedNode.type.equals("trunk") || generatedNode.type.equals("leaf") ) { skip = true;}
							
							// ignore origin trunk and leaves with same origin
							if( (generatedNode.type.equals("leaf") ) && (leafNode.origin == generatedNode.origin) ) {
								skip=true;;
							}
							if( generatedNode == leafNode.origin ) {
								skip=true;;
							}
							if(!skip) {
								boolean leafAndGeneratedCollided = false;
								for( SSWall wall : SSWall.instanceList ) {	
									if( EuclideanMath.distanceLinePoint( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16,
									wall.x+16, wall.y+16) < diag ) {
										leafAndGeneratedCollided = true;
										break;
									}
								}
								if( !leafAndGeneratedCollided ) {
									// check if node is at allowed angles
									if( (leafNode.x == generatedNode.x) || (leafNode.y == generatedNode.y) || ( (Math.abs(leafNode.x-generatedNode.x))==(Math.abs(leafNode.y-generatedNode.y)) )  ) {
										// cant change distance if connecting to a trunk								
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
				if( connectsWithFlag1 == false ) {
					System.out.println("path doesnt connect with flag 1");
					for( SSAINode generatedNode : generatedList ) {
						generatedNode.destroy();
					}
					return null;
				}
			
		if( ( (spawnerNode.connectedNodesCloserToFlag1.size() > 0) || ( spawnerNode.connectsToFlag) )
		&& ( (spawnerNode.connectedNodesCloserToFlag2.size() > 0) || ( spawnerNode.connectsToFlag) ) ){
			connectsWithSpawner = true;
		}
			
		
		// discard unused nodes
		Iterator<SSAINode> it = generatedList.iterator();
		while(it.hasNext()) {
			SSAINode instance = it.next();
			if( (instance.connectedNodesCloserToFlag1.size() == 0)
			&&	(instance.connectedNodesCloserToFlag2.size() == 0)	
			&& (instance.connectsToFlag == false) ) {
				it.remove();
				instance.destroy();
			}
		}
		
		if( connectsWithFlag1 && connectsWithFlag2 && connectsWithSpawner ) {
			System.out.println( "Path created" );
			return generatedList;
		}
		else {
			System.out.println( "Path creation failed" );
			System.out.println( spawnerNode.distanceToFlag1 );
			System.out.println( spawnerNode.distanceToFlag2 );
			for( SSAINode generatedNode : generatedList ) {
				generatedNode.destroy();
			}
			return null;
		}
		
	}
	
	public void destroy() {
		// TODO ground list and add all instances on add()
		for( SSFlag f : flagList ) {
			f.destroy();
		}
		for( SSSpawner s : spawnerList ) {
			s.destroy();
		}
		for( SSWall w : wallList ) {
			w.destroy();
		}
		
	}
	
	public void sendPath( byte[] data, DatagramSocket socket, InetAddress ip, int port ) {
		
		if( data[0] == NetworkProtocol.MAP_PATH_HEADER) {
			byte totalPath = 0;
			for( SSSpawner s : SSSpawner.instanceList ) {
				if( s.active ) {
					totalPath ++;
				}
			}
			try {
				socket.send( new DatagramPacket( new byte[] { NetworkProtocol.MAP_PATH_HEADER, totalPath }, 2, ip, port ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			if( data[0] == NetworkProtocol.MAP_PATH_DATA ) {
				byte pathNumber = data[1];
				byte currentPath = 0;
				for( SSSpawner s : SSSpawner.instanceList ) {
					if( s.active ) {
						
						if( currentPath == pathNumber ) {
							byte[] sendData = new byte[s.path.size()*2+3];
							sendData[0] = NetworkProtocol.MAP_PATH_DATA;
							sendData[1] = currentPath;
							sendData[2] = (byte) s.path.size();
							int i = 3;
							for( SSAINode node : s.path ) {
								sendData[i] = (byte) (node.x/32f);
								sendData[i+1] = (byte) (node.y/32f);
								i += 2;
							}
							try {
								socket.send( new DatagramPacket( sendData, sendData.length, ip, port ) );
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}else {
							currentPath ++;
						}
						
					}
				}
			}
		}
		
	}
	
	public void sendPathHeader( DatagramSocket socket, InetAddress ip, int port  ) {
		byte totalPath = 0;
		for( SSSpawner s : SSSpawner.instanceList ) {
			if( s.active ) {
				totalPath ++;
			}
		}
		try {
			socket.send( new DatagramPacket( new byte[] { NetworkProtocol.MAP_PATH_HEADER, totalPath }, 2, ip, port ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***
	 * sends the header of this map to the specified client
	 * the header consist of the theme, team1 color and team2 color in this order
	 */
	public void sendHeader( DatagramSocket socket, InetAddress ip, int port ) {
		
		byte theme = (byte) mapDefinition.theme;
		byte t1colorIndex = (byte)  mapDefinition.teamColorIndex[1];
		byte t2colorIndex = (byte)  mapDefinition.teamColorIndex[2];
		
		byte[] sendData = new byte[4];
		
		sendData[0] = NetworkProtocol.MAP_HEADER; sendData[1] = theme; sendData[2] = t1colorIndex; sendData[3] = t2colorIndex;
		
		try {
			socket.send( new DatagramPacket( sendData, sendData.length, ip, port ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//TODO
	public void send( byte layer, byte row, DatagramSocket socket, InetAddress ip, int port ) {
		
		if ( (layer == 1) || (layer == 2) ) {
			if( (row >= 0) && (row < 64) ) {
				byte[] mapRow;
				if( layer == 1) {
					mapRow = mapDefinition.byteLayer1[row];
				}
				else {
					mapRow = mapDefinition.byteLayer2[row];
				}
				byte[] sendData = new byte[mapRow.length + 3];
				sendData[0] = NetworkProtocol.MAP_GRID;
				sendData[1] = layer;
				sendData[2] = row;
				for( int i = 3; i < sendData.length; i ++ ) {
					sendData[i] = mapRow[i-3];
				}
				try {
					socket.send( new DatagramPacket( sendData, sendData.length, ip, port ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else {
			if ( layer == 3 ) {
				try {
					socket.send( new DatagramPacket( new byte[] { 4, 3, (byte) mapDefinition.theme }, 3, ip, port ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public SSMap( MapDefinition mDefinition, SSMatch aMatch ) {
		spawnerList = new LinkedList<SSSpawner>();
		flagList = new LinkedList<SSFlag>();
		groundList = new LinkedList<SSGround>();
		wallList = new LinkedList<SSWall>();
		mapDefinition = mDefinition;
		match = aMatch;
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
					wallList.add( new SSWall( x*32, y*32 ) );
					break;
				case 2:
					groundList.add( new SSGround( x*32, y*32 ) );
					break;
				case 3:
					spawnerList.add( new SSSpawner( x*32, y*32, 0, this ) );					
					break;
				case 4:
					spawnerList.add( new SSSpawner( x*32, y*32, 1, this ) );					
					break;
				case 5:
					spawnerList.add( new SSSpawner( x*32, y*32, 2, this ) );
					break;
				case 6:
					flagTeam[1] = new SSFlag( x*32, y*32, 1 );
					flagTeam1 = flagTeam[1];
					flagList.add(flagTeam1);
					break;
				case 7:
					flagTeam[2] = new SSFlag( x*32, y*32, 2 );
					flagTeam2 = flagTeam[2];
					flagList.add(flagTeam2);
					break;
			}
	}
	
}
