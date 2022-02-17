package game.thing.server;

import java.util.LinkedList;

import game.EuclideanMath;
import game.ServerGame;
import game.TickerImplementor;
import game.interfaces.Ticker;
import game.thing.Weapon;


public class SBot extends PlayerOwner implements Ticker {

	public static final byte
	BEHAVIOUR_ROAMER = 0,
	BEHAVIOUR_DEFENDER = 1,
	BEHAVIOUR_ATTACKER = 2;
	
	public static final String[] names = { "Smeakol", "Frotto", "Binder", 
	"Hemmer", "Burt", "Spook", "Rorshuck", "Crapper", "Farter", "Burper", "Bugger",
	"Waffles", "Butcher", "Dangler", "Macho", "Freak" };
	
	public boolean evasive;
	public boolean berserk;
	
	public byte behaviour;
	
	private TickerImplementor ticker;
	private int s = 1;
	private int sCounter = 0;
	private float targetX;
	private float targetY;
	private int twoSeconds;
	private boolean fighting;
	private float targetAimx;
	private float targetAimy;
	private boolean lost;
	private SSAINode targetNode;
	private LinkedList<SSAINode> path;
	private int personalityAimAgility;
	private int personalityAimLeading;
	private int personalityAimAccuracy;
	private int pathDirection;
	private int personalityEvasiveness;
	
	LinkedList<SPlayer> enemiesList;
	LinkedList<SPlayer> visibleList;
	SPlayer targetEnemy;
	
	public SBot() {
		
		personalityEvasiveness = (int) (60f*Math.random())+30;
		personalityAimAgility = (int) (4*Math.random());
		personalityAimLeading = (int) (32*Math.random()) -16;
		personalityAimAccuracy = (int) (6*Math.random()) - 3;
		if(personalityAimAgility== 0 ) { personalityAimAgility= 1; }
		int nameIndex = (int) (Math.random()*15);
		nickname = "[BOT] " + names[nameIndex];
		
		
		
		lost = true;

		twoSeconds = (int) (Math.random()*personalityEvasiveness)-2;
		changeStrategy();
		
		ticker = new TickerImplementor( 1 ) {
			@Override
			public void tick() {
				SBot.this.tick();				
			}
		};
		
	}
	
	public void changeStrategy() {
		behaviour = (byte) (Math.random()*3);
		System.out.println( "bot behaviour:" + behaviour);
		
		behaviour = BEHAVIOUR_ATTACKER;
		if( Math.random() < 0.3 ) {
			berserk = true;
		}
		if( Math.random() < 0.7 ) {
			evasive = true;
		}
		
		float shotChance = 0.33f;
		float zookaChance = 0.4f;
		float deagleChance = 1f;
		
		if( berserk ) {
			shotChance = 0.7f;
		}
		
		double weaponCoeficient = Math.random();	
		if(  weaponCoeficient < deagleChance ) {
			weaponType = Weapon.TYPE_DEAGLE;
		}
		if( weaponCoeficient < zookaChance ) {
			weaponType = Weapon.TYPE_ZOOKA;
		}
		if( weaponCoeficient < shotChance ) {
			weaponType = Weapon.TYPE_SHOT;
		}
		
	}
	
	public void activate() {
		path = spawner.path;
		spawner.map.match.server.serverGame.enableTicker( ticker );
	}
	
	public void deactivate() {
		spawner.map.match.server.serverGame.disableTicker( ticker );
	}
	
	public void tick() {
		
		if( player == null ) { return; }
		

		
		// a bot will be lost if its path node isnt visible
		if( lost ) {
			
			// attempt to find a node on the same path
			targetNode = findNode();
			if( targetNode == null ) { // find a new path
				path = findPath();
				return;
			}
			else {
				lost = false;
				targetX = targetNode.x+16;
				targetY = targetNode.y+16;
			}
			
		}
		else {
			
			// close to target node
			if( EuclideanMath.distancePointPoint(targetNode.x+16, targetNode.y+16, player.x, player.y) < 2 ) {
				
				if( spawner.map.match.type.equals("Capture The Flag") ) {
					switch( behaviour ) {
						case BEHAVIOUR_ROAMER:
							pathDirection = 0;
							if( EuclideanMath.distancePointPoint( this.spawner.map.flagTeam[player.team].x, this.spawner.map.flagTeam[player.team].y, 
							this.spawner.map.flagTeam[player.team].flagHolder.x, this.spawner.map.flagTeam[player.team].flagHolder.y) > 32f ) {
								// TODO cache flag of team
								pathDirection = -1;
							}
							break;
						case BEHAVIOUR_ATTACKER:
							pathDirection = 0;
							break;
						case BEHAVIOUR_DEFENDER:
							pathDirection = (int) (Math.random()*2) + 1;
							if( EuclideanMath.distancePointPoint( this.spawner.map.flagTeam[player.team].x, this.spawner.map.flagTeam[player.team].y, 
							this.spawner.map.flagTeam[player.team].flagHolder.x, this.spawner.map.flagTeam[player.team].flagHolder.y) > 32f ) {
								// TODO cache flag of team
								pathDirection = -1;
							}
							break;
					}
					if( player.flagged == 1 ) {
						pathDirection = -1;
					}
				}
				else {
					if( spawner.map.match.teamTyped ) {
						
						if( targetNode.distanceToFlag1 <= 1 ) {
							pathDirection = 2;// TODO pick a new connectedNodesCloserToFlag1 path
						}
						if( targetNode.distanceToFlag2 <= 1) {
							pathDirection = 1;
						}
						
						if( targetNode.distanceToFlag1 <= 1 ) {
							pathDirection = 2;
						}
						if( targetNode.distanceToFlag2 <= 1) {
							pathDirection = 1;
						}	
					}
					else {
						// TODO pick a random node? pick a random path ?
						pathDirection = (int) (Math.random()*2)+1;
					}
				}
				
				
				switch( pathDirection ) {
					case -1:
						if( player.team == 1 ) {
							pathDirection = 1;
						}
						if( player.team == 2) {
							pathDirection = 2;
						}
						break;
					case 0:
						if( player.team == 1 ) {
							pathDirection = 2;
						}
						if( player.team == 2) {
							pathDirection = 1;
						}
						break;
				}
				// needs another switch cause pathDirection get cached on the switch statement
				switch( pathDirection ) { 
					case 1: // towards flag 1
						if( targetNode.connectedNodesCloserToFlag1.size() > 0 ) {
							targetNode = targetNode.connectedNodesCloserToFlag1.getFirst();
						}
						else {
							if(player.flagged==1);//System.out.println("no nodes found closer to flag 1");
						}
						break;
					case 2:
						if( targetNode.connectedNodesCloserToFlag2.size() > 0 ) {
							targetNode = targetNode.connectedNodesCloserToFlag2.getFirst();
						}
						else {
							if(player.flagged==1);//	System.out.println("no nodes found closer to flag 2");
						}
						break;
				}
				if( pathDirection == -1 || pathDirection == 0 ) {System.out.println("no path");}
				targetX = targetNode.x+16;
				targetY = targetNode.y+16;
			}
		} // !lost
		
		if( twoSeconds++ == personalityEvasiveness ) {
			twoSeconds = 0;
		}
		
		// lost or not bot will look for threats 
		targetEnemy = null;
		enemiesList = findEnemies( 32*6 );
		
		boolean safe = true;
		fighting = false;
		
		if( !enemiesList.isEmpty() ) {
			visibleList = filterVisible( enemiesList );
			if( !visibleList.isEmpty() ) {
				enemiesList = visibleList;
			}
			targetEnemy = findClosest( enemiesList );
		}
		
		boolean recoil = false;
		
		if( targetEnemy != null ) {
			targetAimx = targetEnemy.x + (( targetEnemy.x - targetEnemy.lastX )*(32+personalityAimLeading ));
			targetAimy = targetEnemy.y + (( targetEnemy.y - targetEnemy.lastY )*(32+personalityAimLeading ));
			fighting = true;
			// TODO shoot if dir is close enough, shoot with a chance
			if( !visibleList.isEmpty() ) {
				if( EuclideanMath.distancePointPoint(targetAimx, targetAimy, player.aimx, player.aimy) < 18 ) {
					recoil = true;
					//if( player.team == 1 )
					player.weapon.shoot();
				}
			}
		}
		
		
		

		
		// 
		boolean collided = false;
		for( SSWall wall : SSWall.instanceList ) {
			if( EuclideanMath.distanceLinePoint( targetNode.x+16, targetNode.y+16, player.x, player.y, wall.x+16, wall.y+16 ) < Math.sqrt(2)*16 + 0.2 ) {
				collided = true;
				lost = true;
				return;
			}
		}

		if( safe ) {
			
			if( EuclideanMath.distancePointPoint(targetNode.x+16, targetNode.y+16, player.x, player.y) < 16 ) {
				targetX = targetNode.x+16;
				targetY = targetNode.y+16;
			}else {
			
			//targetX = targetNode.x;
			//targetY = targetNode.y;
			
				if( ( twoSeconds == 0 ) ) {
					targetX = (targetNode.x + 16) + (((int) (Math.random()+.5f)-.5f) * 
							  (((int)(Math.random()+.5f)+1)*8f)  );
					targetY = (targetNode.y + 16) + (((int) (Math.random()+.5f)-.5f) * 
							  (((int)(Math.random()+.5f)+1)*8f)  );
				}
			}
			
		}
		
		if( spawner.map.match.type.equals("Capture The Flag") ) {
			if( player.flagged == 0 ) {
				int otherTeam;
				otherTeam = (player.team == 1) ? 2 : 1; // TODO cache otherTeam
				// get their flag
				if( checkFlagVisible( this.spawner.map.flagTeam[otherTeam] ) ) {
					//System.out.println("found enemy flag");
					// check if angle is one of eight by angle / eghtth and see if its close to an integer
					double angleDiference = (EuclideanMath.D2LineToAngle( player.x, player.y, spawner.map.flagTeam[otherTeam].x+16, spawner.map.flagTeam[otherTeam].y+16)
							/EuclideanMath.circleDividedBy8);
					angleDiference = angleDiference - (int) angleDiference;
					if( (angleDiference < 0.05f) || (angleDiference > 0.95f) ) {
						//System.out.println("aligned with an enemy flag"+angleDiference);
						if( ( this.spawner.map.flagTeam[otherTeam].carrier == null ) ) {
							targetX = this.spawner.map.flagTeam[otherTeam].x+16;
							targetY = this.spawner.map.flagTeam[otherTeam].y+16;
						}
					}
				}
				/*
				if( EuclideanMath.distancePointPoint( player.x, player.y, spawner.map.flagTeam[otherTeam].x, spawner.map.flagTeam[otherTeam].y ) < 128f ) {
					
				}*/
			}
			else {
				if( checkFlagVisible( this.spawner.map.flagTeam[player.team] ) ) {
					// check if angle is one of eight by angle / eghtth and see if its close to an integer
					double angleDiference = (EuclideanMath.D2LineToAngle( player.x, player.y, spawner.map.flagTeam[player.team].x+16, spawner.map.flagTeam[player.team].y+16)
							/EuclideanMath.circleDividedBy8);
					angleDiference = angleDiference - (int) angleDiference;
					if( (angleDiference < 0.05f) || (angleDiference > 0.95f) || (EuclideanMath.distancePointPoint(player.x, player.y, spawner.map.flagTeam[player.team].x+16, spawner.map.flagTeam[player.team].y+16) < 32+16) ) {
						//System.out.println("aligned with my flag"+angleDiference);
						targetX = this.spawner.map.flagTeam[player.team].x+16;
						targetY = this.spawner.map.flagTeam[player.team].y+16;
						
					}
				}
			}
			if( EuclideanMath.distancePointPoint( this.spawner.map.flagTeam[player.team].x, this.spawner.map.flagTeam[player.team].y, 
			this.spawner.map.flagTeam[player.team].flagHolder.x, this.spawner.map.flagTeam[player.team].flagHolder.y) > 32f ) {
			// flag isnt at base
				if( checkFlagVisible( this.spawner.map.flagTeam[player.team] ) ) {
					targetX = this.spawner.map.flagTeam[player.team].x+16;
					targetY = this.spawner.map.flagTeam[player.team].y+16;
				}
				
			}
		}

		if( !fighting ) {
			targetAimx = targetX;
			targetAimy = targetY;
		}
		// aim at walking direction if not fighting
		float deltaAimx = targetAimx - player.aimx;
		float deltaAimy = targetAimy - player.aimy;
		
		
		
		// move aim
		player.aimx += deltaAimx/(16+(2*personalityAimAgility));
		player.aimy += deltaAimy/(16+(2*personalityAimAgility));
		if( deltaAimx < 24 ) {  player.aimx += (deltaAimx/(38/personalityAimAgility)); }
		if( deltaAimy < 24 ) {  player.aimy += (deltaAimy/(32/personalityAimAgility)); } 

		
		
		// move to target whatever it is
		
		if( Math.abs( targetX - player.x ) > 0.3 ) {
			if( targetX < player.x ) {
				player.VK_LEFT = true;
			}
			if( targetX > player.x ) {
				player.VK_RIGHT = true;
			}
		}
		if( Math.abs( targetY - player.y ) > 0.3 ) {
			if( targetY > player.y ) {
				player.VK_UP = true;
			}
			if( targetY < player.y ) {
				player.VK_DOWN = true;
			}
		}
		
		player.starvingFrames = 7;
	
	}
	
	/***
	 * finds the closest SPlayer on a SPlayer list
	 */
	private SPlayer findClosest( LinkedList<SPlayer> aEnemiesList ) {
		SPlayer returnPlayer = null;
		float smallerDistance = Float.MAX_VALUE;
		for( SPlayer enemyPlayer : aEnemiesList ) {
			if( EuclideanMath.distancePointPoint(player.x, player.y, enemyPlayer.x, enemyPlayer.y) < smallerDistance ) {
				returnPlayer = enemyPlayer;
			}
		}
		return returnPlayer;
	}
	
	private boolean checkFlagVisible( SSFlag flag ) {
		for( SSWall w : SSWall.instanceList ) {
			if( w.collisionLine(player.x, player.y, flag.x+16, flag.y+16 ) ) {
				return false;
			}
		}
		return true;
	}
	
	/***
	 * creates a list of visible enemies from a list
	 * 
	 */
	private LinkedList<SPlayer> filterVisible( LinkedList<SPlayer> aEnemiesList ) {
		LinkedList<SPlayer> returnList = new LinkedList<SPlayer>();
		for( SPlayer enemyPlayer : aEnemiesList ) {
			boolean collided = false;
			for( SSWall w : SSWall.instanceList ) {
				if( w.collisionLine(player.x, player.y, enemyPlayer.x, enemyPlayer.y ) ) {
					collided = true;
					break;
				}
			}
			if( !collided ) {
				returnList.add(enemyPlayer);
			}
		}
		return returnList;
	}

	/***
	 * creates a list of enemies within a range
	 * 
	 * loops through all active spawner's non-null players that are considered enemies and are within range, and adds them to the return list
	 */
	private LinkedList<SPlayer> findEnemies( float range ) {
		LinkedList<SPlayer> returnList = new LinkedList<SPlayer>();
		for( SSSpawner s : SSSpawner.instanceList ) {
			if( s.active ) {
				if( s != spawner ) { // ignore calling player
					if(s.player != null ) {
						if( spawner.map.match.teamTyped ) {
							if( s.player.team != player.team ) {
								// if other player is close enough
								if( EuclideanMath.distancePointPoint(player.x, player.y, s.player.x, s.player.y) < range ) {
									returnList.add(s.player);
								}
							}
						}
						else {
							// if other player is close enough
							if( EuclideanMath.distancePointPoint(player.x, player.y, s.player.x, s.player.y) < range ) {
								returnList.add(s.player);
							}
						}
					}
				}
			}
		}
		return returnList;
	}
	
	/***
	 * Finds a visible node for the current path
	 * 
	 */
	private SSAINode findNode() {
		for( SSAINode node : path ) {
			boolean collided = false;
			for( SSWall wall : SSWall.instanceList ) {
				if( EuclideanMath.distanceLinePoint(node.x+16, node.y+16, player.x, player.y, wall.x+16, wall.y+16 ) < Math.sqrt(2)*16+1 ) {
					collided = true;
					break;
				}
			}
			if( !collided ) {
				return node;
			}
		}
		// bots that cant find node are usually stuck in a wall
		//System.out.println("bot " + this + "couldnt find node" );
		return null;
	}

	/***
	 * Finds a path with a visible node
	 * TODO: implement decent collision with wall
	 */
	private LinkedList<SSAINode> findPath() {
		
		for( SSSpawner spawner : SSSpawner.instanceList ) {
			if( spawner.active ) {
				for( SSAINode node : spawner.path ) {
					for( SSWall wall : SSWall.instanceList ) {
						if( EuclideanMath.distanceLinePoint(node.x+16, node.y+16, player.x, player.y, wall.x+16, wall.y+16 ) > Math.sqrt(2)*16+1 ) {
							return spawner.path;
						}
					}
				}
			}
		}
		//System.out.println("bot " + this + "couldnt find path" );
		return null;
	}
	
}
