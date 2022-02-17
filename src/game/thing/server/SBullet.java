package game.thing.server;

import game.EuclideanMath;
import game.Game;
import game.ServerGame;
import game.TickerImplementor;
import game.interfaces.Ticker;

public class SBullet implements Ticker {

	public float speed;
	public float direction;
	public float x, y, lastX, lastY;
	
	public SSSpawner spawner;
	
	public TickerImplementor ticker;
	
	public SBullet( SSSpawner aSpawner, float aSpeed,
	float aDirection, float ax, float ay ) {
		spawner = aSpawner;
		speed = aSpeed;
		direction = aDirection;
		x = ax;
		y = ay;
		
		ticker = new TickerImplementor( 12 ) {
			@Override
			public void tick() {
				SBullet.this.tick();
			}
		};
		
		spawner.map.match.server.serverGame.enableTicker( ticker );
		//System.out.println("server bullet created");
	}

	@Override
	public void tick() {
		lastX = x;
		lastY = y;
		x += EuclideanMath.rotatedXComponent( speed,  0,  direction );
		y += EuclideanMath.rotatedYComponent( speed,  0,  direction );
	
		for( SSWall w : SSWall.instanceList ) {
			if( w.collisionLine( lastX, lastY, x, y ) ) {
				destroy();
				break;
			}
		}
		SPlayer collidedPlayer = null;
		for( SPlayer player : SPlayer.instanceList ) {
			if( player != spawner.player ) {
				if( EuclideanMath.distanceLinePoint(lastX, lastY, x, y, player.x, player.y ) < 4f ) {
					destroy();
					collidedPlayer = player;
					spawner.notifyPlayerKill( player );
					
					break;
				}
			}
		}
		if( collidedPlayer != null ) {
			collidedPlayer.destroy();
		}
		
	}
	
	public void destroy() {
		spawner.map.match.server.serverGame.disableTicker( ticker );
	}
}
