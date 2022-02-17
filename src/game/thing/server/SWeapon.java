package game.thing.server;

import java.util.LinkedList;

import engine.util.Matrix4x4f;
import game.EuclideanMath;
import game.Game;
import game.ServerGame;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.Ticker;
import game.thing.Weapon;

public class SWeapon implements Ticker {
	
	public float x, y;
	public float direction;
	public SPlayer player;
	public int team;

	public byte shot;
	public int reloading;
	public int reloadCD;
	public int type;
	public SSSpawner spawner;
	
	public TickerImplementor ticker;
	
	protected SWeapon( SPlayer aPlayer, int aType, SSSpawner aSpawner ) {
		spawner = aSpawner;
		player = aPlayer;
		x = player.x;
		y = player.y;
		team = player.team;
		type = aType;
		
		ticker = new TickerImplementor( 2 ) { // should tick after SPlayer
			@Override
			public void tick() {
				SWeapon.this.tick();
			}
		};
		
		spawner.map.match.server.serverGame.enableTicker(ticker);
		
	}
	
	public void destroy() {
		spawner.map.match.server.serverGame.disableTicker(ticker);
	}
	
	public void shoot() {
		
		if( reloading == 0) {
			shot = 1;
		}
		
	}
	
	public void tick() {
		
		float playerAngle = (float)(EuclideanMath.D2LineToAngle(player.x, player.y, player.aimx, player.aimy) - Math.PI/2);
		x = player.x + EuclideanMath.rotatedXComponent(4, 0, playerAngle);
		y = player.y + EuclideanMath.rotatedYComponent(4, 0, playerAngle);
		
		direction = EuclideanMath.D2LineToAngle(x, y, player.aimx, player.aimy);
		
		if( reloading == reloadCD ) {
			shot = 0;
		}
		if( reloading > 0 ) {
			reloading --;
		}
		if( shot == 1 ) {
			reloading = reloadCD;
			kidShoot();
		}
	}
	
	public void kidShoot() {};
	
	public static SWeapon newWeapon( SPlayer aPlayer, int type, SSSpawner aSpawner ) {
		switch( type ) {	
			case Weapon.TYPE_DEAGLE:
				return new SDeagle( aPlayer, type, aSpawner );
			case Weapon.TYPE_SHOT:
				return new SShot( aPlayer, type, aSpawner );
			case Weapon.TYPE_ZOOKA:
				return new SZooka( aPlayer, type, aSpawner );
		}
		return null;
	}
	
}
