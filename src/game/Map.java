
package game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import game.thing.AINode;
import game.interfaces.Themable;
import game.thing.Flag;
import game.thing.Ground;
import game.thing.Spawner;
import game.thing.Wall;
import game.thing.server.SSSpawner;

public class Map implements Serializable {
	
	private static final long serialVersionUID = 3913498568026110276L;
	
	MapThing[][] layer1 = new MapThing[64][64];
	MapThing[][] layer2 = new MapThing[64][64];
	
	public MapDefinition mapDefinition;
	
	public Flag flagTeam1;
	public Flag flagTeam2;
	
	private LinkedList<Themable> themableList = new LinkedList<Themable>();
	
	public void changeColor( int team ) {
			for( Flag f : Flag.instanceList  ) {
				if( f.team == team ) {
					f.nextSprite();
				}
			}
			
			for( Spawner s : Spawner.instanceList ) {
				if( s.team == team ) {
					mapDefinition.teamColorIndex[team] = s.nextSprite();
				}
			}
	}
	
	public void destroy() {
	
		for( int i = 0; i < 64; i ++ ) {
			for( int ii = 0; ii < 64; ii ++ ) {
				if( layer2[i][ii] != null ) {
					layer2[i][ii].destroy();
				}
				if( layer1[i][ii] != null ) {
					layer1[i][ii].destroy();
				}
			}
		}
	}
	
	public Map() {
		mapDefinition = new MapDefinition();
		
	}
	
	public Map( MapDefinition mDefinition ) {
		mapDefinition = mDefinition;
		
		for( int x = 0; x < 64; x ++ ) {
			for( int y = 0; y < 64; y ++ ) {
				
				byte b = mapDefinition.byteLayer1[x][y];
				
				add( x, y, b, mapDefinition.theme);
			} 
		}
		for( int x = 0; x < 64; x ++ ) {
			for( int y = 0; y < 64; y ++ ) {
				
				byte b = mapDefinition.byteLayer2[x][y];
				
				add( x, y, b, mapDefinition.theme);
			} 
		}
	}
	
	public void changeTheme( int themeIndex ) {
		mapDefinition.theme = themeIndex;
		for( Themable thing : themableList ) {
			thing.changeTheme( themeIndex );
		}
	}
	
	public void add( int x, int y, int placing, int theme ) {
			switch (placing) {
				case 1:
					if( layer2[x][y] == null ) {
						Wall instance = new Wall( x*32, y*32, theme );
						layer2[x][y] = instance;
						mapDefinition.byteLayer2[x][y] = 1;
						themableList.add(instance);
					}
					break;
				case 2:
					if( layer1[x][y] == null ) {
						Ground instance = new Ground( x*32, y*32, theme );
						layer1[x][y] = instance;
						mapDefinition.byteLayer1[x][y] = 2;
						themableList.add(instance);
					}
					break;
				case 3:
					if( layer2[x][y] == null ) {
						layer2[x][y] = new Spawner( x*32, y*32, 0, 0 );
						mapDefinition.byteLayer2[x][y] = 3;
					}
					break;
				case 4:
					if( layer2[x][y] == null ) {
						layer2[x][y] = new Spawner( x*32, y*32, 1, mapDefinition.teamColorIndex[1] );
						mapDefinition.byteLayer2[x][y] = 4;
					}
					break;
				case 5:
					if( layer2[x][y] == null ) {
						layer2[x][y] = new Spawner( x*32, y*32, 2, mapDefinition.teamColorIndex[2] );
						mapDefinition.byteLayer2[x][y] = 5;
					}
					break;
				case 6:
					if( layer2[x][y] == null ) {
						flagTeam1 = new Flag( x*32, y*32, 1,mapDefinition.teamColorIndex[1] );
						layer2[x][y] = flagTeam1;
						mapDefinition.byteLayer2[x][y] = 6;
					}
					break;
				case 7:
					if( layer2[x][y] == null ) {
						flagTeam2 = new Flag( x*32, y*32, 2, mapDefinition.teamColorIndex[2] );
						layer2[x][y] = flagTeam2;
						mapDefinition.byteLayer2[x][y] = 7;
					}
					break;
			}
	}

	public void remove(int placerX, int placerY) {
		if( layer1[placerX][placerY] != null ) {
			themableList.remove(layer1[placerX][placerY]);
			layer1[placerX][placerY].destroy();
			layer1[placerX][placerY] = null;
			mapDefinition.byteLayer1[placerX][placerY] = 0;
		
		}
		if( layer2[placerX][placerY] != null ) {
			themableList.remove(layer2[placerX][placerY]);
			layer2[placerX][placerY].destroy();
			layer2[placerX][placerY] = null;
			mapDefinition.byteLayer2[placerX][placerY] = 0;
		
		}
		
	}
	
	public LinkedList<AINode> createPath( Spawner spawner ) {
		
		LinkedList<AINode> generatedList = generateNodes();
		
		// puts a node in the spawner position
		AINode spawnerNode = new AINode( spawner.x, spawner.y );
		generatedList.add( spawnerNode );
		generatedList.add( new AINode(flagTeam1.x, flagTeam1.y) );
		generatedList.add( new AINode(flagTeam2.x, flagTeam2.y) );
		
		boolean connectsWithFlag1 = false;
		boolean connectsWithFlag2 = false;
		boolean connectsWithSpawner = false;
		
		// wall radius used for crap path collision
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
				for( AINode node : generatedList ) {
					boolean collided = false;
					for( Wall wall : Wall.instanceList ) {
						
						if( EuclideanMath.distanceLinePoint( flagTeam1.x+16, flagTeam1.y+16, node.x+16, node.y+16,
						wall.x+16, wall.y+16) < diag ) {
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
			if( connectsWithFlag1 == false ) {
				System.out.println("no node connects with flag 1");
				for( AINode generatedNode : generatedList ) {
					generatedNode.destroy();
				}
				return null;
			}
			
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
					if( leafNode == spawnerNode ) { System.out.println("path to flag 1 connects with spawner"); }
					// check if connects to flag2
					boolean collided = false;
					for( Wall wall : Wall.instanceList ) {
						
						if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, leafNode.x+16, leafNode.y+16,
						wall.x+16, wall.y+16) < diag ) {
							collided = true;
							break;
						}
					}
					if( !collided ) {
						if( (leafNode.x == flagTeam2.x) || (leafNode.y == flagTeam2.y) || ( (Math.abs(leafNode.x-flagTeam2.x))==(Math.abs(leafNode.y-flagTeam2.y)) )  ) {
							Game.lineList.add( new D2Line( leafNode.x+16, leafNode.y+16, flagTeam2.x+16, flagTeam2.y+16 ) );
							connectsWithFlag2 = true;
							leafNode.connectsToFlag = true;
						}
					}
					// check if leaf connect to other nodes
					for( AINode generatedNode : generatedList ) {
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
							for( Wall wall : Wall.instanceList ) {	
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
									//Game.lineList.add( new D2Line( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16 ) );
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
				for( AINode generatedNode : generatedList ) {
					generatedNode.destroy();
				}
				return null; 
			}
			
			// navigate nodes from team2 to team1 (path points to team 2)
			
			// clear node types
			for( AINode node : generatedList ) {
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
					for( AINode node : generatedList ) {
						boolean collided = false;
						for( Wall wall : Wall.instanceList ) {
							
							if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, node.x+16, node.y+16,
							wall.x+16, wall.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							// check if its aligned with one of 8 directions
							if( (node.x == flagTeam2.x) || (node.y == flagTeam2.y) || ( (Math.abs(node.x-flagTeam2.x))==(Math.abs(node.y-flagTeam2.y)) )  ) {
								Game.lineList.add( new D2Line( node.x+16, node.y+16, flagTeam2.x+16, flagTeam2.y+16 ) );
								node.connectsToFlag = true;
								sproutList.add(node);
								connectsWithFlag2 = true;
							}
						}
					}
				}
				if( connectsWithFlag2 == false ) {
					System.out.println("no node connects with flag 2");
					for( AINode generatedNode : generatedList ) {
						generatedNode.destroy();
					}
					return null;
				}
				
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
						if( leafNode == spawnerNode ) { System.out.println("path to flag 2 connects with spawner"); }
						// check if connects to flag1
						boolean collided = false;
						for( Wall wall : Wall.instanceList ) {
							
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
						for( AINode generatedNode : generatedList ) {
							
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
								for( Wall wall : Wall.instanceList ) {	
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
										Game.lineList.add( new D2Line( generatedNode.x+16, generatedNode.y+16, leafNode.x+16, leafNode.y+16 ) );
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
					for( AINode generatedNode : generatedList ) {
						generatedNode.destroy();
					}
					return null;
				}
			
		if( ( spawnerNode.connectedNodesCloserToFlag1.size() > 0 || ( spawnerNode.connectsToFlag ) )
		&& ( spawnerNode.connectedNodesCloserToFlag2.size() > 0 || ( spawnerNode.connectsToFlag) ) ){
			connectsWithSpawner = true;
		}
			
		
		// discard unused nodes
		Iterator<AINode> it = generatedList.iterator();
		while(it.hasNext()) {
			AINode instance = it.next();
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
			for( AINode generatedNode : generatedList ) {
				generatedNode.destroy();
			}
			return null;
		}
		
	}
	/*
	public boolean generateNodes2() {
		boolean connectsWith1 = false;
		boolean connectsWith2 = false;
		
		float diag = (float)(Math.sqrt(2))*16 + 1;
		LinkedList<AINode> nodeCurrentList = new LinkedList<AINode>();
		LinkedList<AINode> nodeNextList = new LinkedList<AINode>();
		int level = 0;
		if( flagTeam1 != null ) {
			for( AINode node : AINode.instanceList ) {
				boolean collided = false;
				for( Wall w : Wall.instanceList ) {
					
					if( EuclideanMath.distanceLinePoint( flagTeam1.x+16, flagTeam1.y+16, node.x+16, node.y+16,
					w.x+16, w.y+16) < diag ) {
						collided = true;
						break;
					}
				}
				if( !collided ) {
					if( (node.x == flagTeam1.x) || (node.y == flagTeam1.y) || ( (Math.abs(node.x-flagTeam1.x))==(Math.abs(node.y-flagTeam1.y)) )  ) {
						node.distanceToTeam1 = level;
						nodeNextList.add(node);
						connectsWith1 = true;
						Game.lineList.add( new D2Line( flagTeam1.x+16, flagTeam1.y+16, node.x+16, node.y+16 ) );
					}
				}
			}
		}
		System.err.println("level " + level +" found " + nodeNextList.size() + "nodes" );
		while( !nodeNextList.isEmpty() ) {
			System.err.println("level " + level +" found " + nodeNextList.size() + "nodes" );
			level += 1;
			if(level == 20) {break;} 
			nodeCurrentList = nodeNextList;
			nodeNextList = new LinkedList<AINode>();
			for( AINode node : nodeCurrentList ) {
				
				boolean collided1 = false;
				for( Wall w : Wall.instanceList ) {
					
					if( EuclideanMath.distanceLinePoint( flagTeam2.x+16, flagTeam2.y+16, node.x+16, node.y+16,
					w.x+16, w.y+16) < diag ) {
						collided1 = true;
						break;
					}
				}
				if( !collided1 ) {
					if( (node.x == flagTeam2.x) || (node.y == flagTeam2.y) || ( (Math.abs(node.x-flagTeam2.x))==(Math.abs(node.y-flagTeam2.y)) )  ) {
						node.distanceToTeam1 = level;
						connectsWith2 = true;
						Game.lineList.add( new D2Line( flagTeam2.x+16, flagTeam2.y+16, node.x+16, node.y+16 ) );
					}
				}
				
				for(AINode nodeAll : AINode.instanceList ) {
					if( ( !nodeAll.connectedNodes.contains(node) ) && (!node.connectedNodes.contains(nodeAll) )  ) {
						// if other node isnt connected to current, check for collisions
						boolean collided = false;
						for( Wall w : Wall.instanceList ) {	
							if( EuclideanMath.distanceLinePoint( nodeAll.x+16, nodeAll.y+16, node.x+16, node.y+16,
							w.x+16, w.y+16) < diag ) {
								collided = true;
								break;
							}
						}
						if( !collided ) {
							// check if node is at allowed angles
							if( (node.x == nodeAll.x) || (node.y == nodeAll.y) || ( (Math.abs(node.x-nodeAll.x))==(Math.abs(node.y-nodeAll.y)) )  ) {
								node.distanceToTeam1 = level;
								node.connectedNodes.add(nodeAll);
								nodeNextList.add(nodeAll);
								Game.lineList.add( new D2Line( nodeAll.x+16, nodeAll.y+16, node.x+16, node.y+16 ) );
							}
						}
					}
				}
			}
		}
		
		if( connectsWith1 && connectsWith2 ) {
			return true;
		}
		else {
			for( D2Line line : Game.lineList ) {
				line.destroy();
			}
			Game.lineList.clear();
			LinkedList<AINode> nodesList = new LinkedList<AINode>();
			for( AINode n : AINode.instanceList ) {
				nodesList.add(n);
			}
			for( AINode n : nodesList ) {
				n.destroy();
			}
			return false;
		}
		
	}
	*/
	public LinkedList<AINode> generateNodes() {
		
		LinkedList<AINode> returnList = new LinkedList<AINode>();
		
		// create some random nodes over ground instances
		
		for( Ground g : Ground.instanceList ) {
			if ( Math.random() > 0.3d ) {
				returnList.add( new AINode( g.x, g.y ) ) ;
			}
		}
		
		System.out.println(returnList.size());
		{
			// remove half of them uniformly
			
			Iterator<AINode> it = returnList.iterator();
			boolean skip = true;
			while(it.hasNext()) {
				AINode instance = it.next();
				if( !skip ) { it.remove(); instance.destroy(); }
				skip = !skip;
			}
		}
		System.out.println(returnList.size());
		{
			// remove half of them uniformly
			
			Iterator<AINode> it = returnList.iterator();
			boolean skip = true;
			while(it.hasNext()) {
				AINode instance = it.next();
				if( !skip ) { it.remove(); instance.destroy(); }
				skip = !skip;
			}
		}
		System.out.println(returnList.size());
		return returnList;
	}
		
}
