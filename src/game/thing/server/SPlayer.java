package game.thing.server;

import java.util.LinkedList;

import game.EuclideanMath;
import game.TickerImplementor;
import game.interfaces.Ticker;

public class SPlayer extends PlayerOwner implements Ticker {

	public static LinkedList<SPlayer> instanceList = new LinkedList<SPlayer>();
	
	public float x, y, xspd, yspd, aimx, aimy, lastX, lastY;
	public int team;
	public PlayerOwner playerOwner;
	public boolean VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT;
	public byte direction;
	public float circleDividedBy8 = (float) (Math.PI/4d);
	public float velocity;
	public SWeapon weapon;
	public byte exploded, shot, flagged;
	
	public int starvingFrames;
	
	public boolean fed = false; // a fed player means it got network input this tick
	
	public TickerImplementor ticker;
	
	public SPlayer( SSSpawner aSpawner, float ax, float ay, PlayerOwner aPlayerOwner, int aTeam, int weaponType ) {
		
		spawner = aSpawner;
		team = aTeam;
		x = ax;
		y = ay;
		aimx = 32*32;
		aimy = 32*32;
		playerOwner = aPlayerOwner;
		playerOwner.player = this;
		flagged = 0;
		
		weapon = SWeapon.newWeapon( this, weaponType, playerOwner.spawner );
		
		starvingFrames = 0;
		
		ticker = new TickerImplementor( 4 ) {
			@Override
			public void tick() {
				SPlayer.this.tick();
			}
		};
		
		instanceList.add( this );
		
		spawner.map.match.server.serverGame.enableTicker(ticker);
	}
	
	public void happyDestroy() {
		playerOwner.spawner.happyKill();
		spawner.map.match.server.serverGame.disableTicker( ticker );
		instanceList.remove( this );
	}
	
	public void destroy() {
		playerOwner.spawner.notifyPlayerDeath();
		spawner.map.match.server.serverGame.disableTicker( ticker );
		weapon.destroy();
		instanceList.remove( this );
		int otherTeam;
		otherTeam = (team == 1) ? 2 : 1;
		if( playerOwner.spawner.map.flagTeam[otherTeam].carrier == this ) {
			playerOwner.spawner.map.flagTeam[otherTeam].carrier = null;
			System.out.println( "player dropped flag. flag.carrier=" + playerOwner.spawner.map.flagTeam[otherTeam].carrier );
		}
		
	}

	@Override
	public void tick() {
		
		direction = 10;
		if( VK_UP ) {
			direction = 2;
			velocity = 0.5f;
			if( VK_RIGHT ) {
				direction = 1;
				velocity = 0.5f;
			}
			if( VK_LEFT ) {
				direction = 3;
				velocity = 0.5f;
			}
		}
		if( VK_DOWN ) {
			direction = 6;
			velocity = 0.5f;
			if( VK_RIGHT ) {
				direction = 7;
				velocity = 0.5f;
			}
			if( VK_LEFT ) {
				direction = 5;
				velocity = 0.5f;
			}
		}
		if( direction == 10 ) {
			if( VK_RIGHT ) {
				direction = 0;
				velocity = 0.5f;
			}
			if( VK_LEFT ) {
				direction = 4;
				velocity = 0.5f;
			}
		}
		velocity = 0.7f;
		if( direction == 10 ) {
			velocity = 0f;
		}
		
		lastX = x; lastY= y;
		
		x += EuclideanMath.rotatedXComponent(velocity, 0, (direction * circleDividedBy8) );
		for( SSWall wall : SSWall.instanceList ) {
			if( wall.collisionCircle(x, y, 3f) ) {
				x = lastX;
				VK_RIGHT = false; VK_LEFT = false;
				break;
			}
		}
		y += EuclideanMath.rotatedYComponent(velocity, 0, (direction * circleDividedBy8) );
		for( SSWall wall : SSWall.instanceList ) {
			if( wall.collisionCircle(x, y, 3f) ) {
				VK_DOWN = false; VK_UP = false;
				break;
			}
		}
		direction = 10;
		if( VK_UP ) {
			direction = 2;
			velocity = 0.5f;
			if( VK_RIGHT ) {
				direction = 1;
				velocity = 0.5f;
			}
			if( VK_LEFT ) {
				direction = 3;
				velocity = 0.5f;
			}
		}
		if( VK_DOWN ) {
			direction = 6;
			velocity = 0.5f;
			if( VK_RIGHT ) {
				direction = 7;
				velocity = 0.5f;
			}
			if( VK_LEFT ) {
				direction = 5;
				velocity = 0.5f;
			}
		}
		if( direction == 10 ) {
			if( VK_RIGHT ) {
				direction = 0;
				velocity = 0.5f;
			}
			if( VK_LEFT ) {
				direction = 4;
				velocity = 0.5f;
			}
		}
		velocity = 0.7f;
		if( direction == 10 ) {
			velocity = 0f;
		} x = lastX; y = lastY;
		x += EuclideanMath.rotatedXComponent(velocity, 0, (direction * circleDividedBy8) );
		y += EuclideanMath.rotatedYComponent(velocity, 0, (direction * circleDividedBy8) );
		
		if( !fed ) {
			starvingFrames++;
		}
		else {
			starvingFrames = 0;
		}
		
		if( starvingFrames > 6 ) {
			resetKeys();
		}
		fed = false;
		
		if( playerOwner.spawner.map.match.type.equals("Capture The Flag") ) {
			int otherTeam;
			otherTeam = (team == 1) ? 2 : 1;
			if( flagged == 0 ) {
				if( EuclideanMath.distancePointPoint(x, y, playerOwner.spawner.map.flagTeam[otherTeam].x+16, playerOwner.spawner.map.flagTeam[otherTeam].y+16 ) < 7 ) {
					if( playerOwner.spawner.map.flagTeam[otherTeam].carrier == null ) {
						playerOwner.spawner.map.flagTeam[otherTeam].carrier = this;
						flagged = 1;
						System.out.println("Player pickedup flag");
					}
				}
				if( EuclideanMath.distancePointPoint(x, y, playerOwner.spawner.map.flagTeam[team].x+16, playerOwner.spawner.map.flagTeam[team].y+16 ) < 7 ) {
					if( playerOwner.spawner.map.flagTeam[team].carrier == null ) {
						playerOwner.spawner.map.flagTeam[team].restore();
					}
				}
			}
			else {
				playerOwner.spawner.map.flagTeam[otherTeam].x = (int) x-16;
				playerOwner.spawner.map.flagTeam[otherTeam].y = (int) y-16;
				if( EuclideanMath.distancePointPoint(x, y, playerOwner.spawner.map.flagTeam[team].x+16, playerOwner.spawner.map.flagTeam[team].y+16 ) < 7 ) {
					if( playerOwner.spawner.map.flagTeam[team].atHolder() ) {
						playerOwner.spawner.map.flagTeam[otherTeam].restore();
						playerOwner.spawner.map.flagTeam[otherTeam].carrier = null;
						flagged = 0;
					}
				}
			}
		}
	}

	private void resetKeys() {
		VK_UP = false;
		VK_DOWN = false;
		VK_LEFT = false;
		VK_RIGHT = false;
	}
	
}
