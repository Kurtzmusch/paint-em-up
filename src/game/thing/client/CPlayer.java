package game.thing.client;

import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.EuclideanMath;
import game.Game;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.DynamicDrawer;
import game.interfaces.Ticker;

public class CPlayer implements DynamicDrawer, Ticker {

	
	public static LinkedList<CPlayer> instanceList = new LinkedList<CPlayer>();
	
	
	public float circleDividedBy8 = (float) (Math.PI/4d);
	public float x,y, aimx, aimy;
	public float lastX, lastY;
	public Sprite sprite;
	public Sprite aimSpr;
	public int team, gunType;
	public CWeapon weapon;
	public CSSpawner spawner;
	private float lastAngle, feetAngle;
	
	public byte flagged;
	
	public int lastDir = 10;

	public float lastEmulatedX1 = 0;
	public float lastEmulatedY1 = 0;
	public float lastEmulatedX2 = 0;
	public float lastEmulatedY2 = 0;

	
	public float lastCertainX1 = 0;
	public float lastCertainY1 = 0;
	public float lastCertainX2 = 0;
	public float lastCertainY2 = 0;
	
	public float lastCertainAngle = 0;
	public float lastEmulatedAngle = 0;
	public float lastCertainSize = 0;
	public float lastEmulatedSize = 0;
	
	public float receivedX = 0, receivedY = 0;
	public float lastReceivedX = 0, lastReceivedY = 0;
	public float lastEmulatedX = 0, lastEmulatedY = 0;
	
	
	public boolean fed, shouldDie;
	
	private Matrix4x4f transformMatrix;
	
	private TickerImplementor ticker, postTicker;
	private DynamicDrawerImplementor dynamicDrawer;
	
	public CPlayer( byte aGunType, byte aTeam, CSSpawner aSpawner) {
		
		fed = false;
		flagged = 0;
		spawner = aSpawner;
		gunType = aGunType;
		team = aTeam;
		
		weapon = CWeapon.newWeapon( this, gunType, spawner );
		
		transformMatrix = new Matrix4x4f();
		
		aimSpr = Game.spriteList.get( "spr_aim" );
		sprite = Game.spriteList.get("spr_feet_forward_0");
		
		ticker = new TickerImplementor( 10 ) {
			@Override
			public void tick() {
				CPlayer.this.tick();
			}
		};
		
		
		postTicker = new TickerImplementor( 20 ) {
			@Override
			public void tick() {
				CPlayer.this.postTick();
			}
		};
		
		Game.enableTicker(ticker);
		Game.enableTicker(postTicker);
		
		dynamicDrawer = new DynamicDrawerImplementor( 12 ) {
			 @Override
			public void dynamicDraw(Program program) {
				CPlayer.this.dynamicDraw(program);
			}
		};
		
		Game.enableDynamicDrawer( dynamicDrawer );
		
		instanceList.add( this );
		
	}
	
	public void destroy() {
		Game.disableTicker( ticker );
		Game.disableDynamicDrawer( dynamicDrawer );
		instanceList.remove( this );
		weapon.destroy();
	}
	
	public void postTick() {
		if( shouldDie ) {
			destroy();
		}
	}

	@Override
	public void tick() {
		boolean VK_UP = false, VK_DOWN = false, VK_RIGHT = false, VK_LEFT = false;
		
		if( fed ) {
			
			lastCertainAngle = EuclideanMath.D2LineToAngle(lastReceivedX, lastReceivedY, receivedX, receivedY );
			lastEmulatedAngle = EuclideanMath.D2LineToAngle(lastReceivedX, lastReceivedY, lastEmulatedX, lastEmulatedY );
			
			lastCertainSize = EuclideanMath.distancePointPoint(lastReceivedX, lastReceivedY, receivedX, receivedY);
			lastEmulatedSize = EuclideanMath.distancePointPoint(lastReceivedX, lastReceivedY, lastEmulatedX, lastEmulatedY );
						
			
			// keep last emulated coords if received coords are confirming emulation
			// and its vector is shorter
			boolean keep = true; boolean sameDir = false;
			if( Math.abs( lastCertainAngle - lastEmulatedAngle ) < 1f/100f ) {
				sameDir = true;
				if( lastCertainSize <= lastEmulatedSize ) {
					
					keep = false;
					fed = false;
				}
			}
			
			if( keep ) {
				//fed = true;
				if( ( sameDir = false) ||
				( Math.abs( EuclideanMath.distancePointPoint( x, y, receivedX, receivedY) ) > 0.65f ) ){
					x = receivedX;
					lastX = lastReceivedX;
					y = receivedY;
					lastY = lastReceivedY;
					fed = true;
				}
				else {
					fed = false;
				}
				
				//lastX = lastReceivedX;
				//lastY = lastReceivedY;
				
			}
			lastReceivedX = receivedX; lastReceivedY = receivedY;
			
		}
		if( !fed ) { // dont change to else
			
			int direction;
			float velocity = 0.5f;
			//x += x-lastX;
			//y += y-lastY;
			
			if( x > lastX ) { VK_RIGHT = true; }
			if( x < lastX ) { VK_LEFT = true; }
			if( y > lastY ) { VK_UP = true; }
			if( y < lastY ) { VK_DOWN = true; }
			
			
			direction = 10;
			if( VK_UP ) {
				direction = 2;
				velocity = 0.48f;
				if( VK_RIGHT ) {
					direction = 1;
					velocity = 0.48f;
				}
				if( VK_LEFT ) {
					direction = 3;
					velocity = 0.51f;
				}
			}
			if( VK_DOWN ) {
				direction = 6;
				velocity = 0.51f;
				if( VK_RIGHT ) {
					direction = 7;
					velocity = 0.5f;
				}
				if( VK_LEFT ) {
					direction = 5;
					velocity = 0.48f;
				}
			}
			if( direction == 10 ) {
				if( VK_RIGHT ) {
					direction = 0;
					velocity = 0.5f;
				}
				if( VK_LEFT ) {
					direction = 4;
					velocity = 0.48f;
				}
			}
			velocity = 0.7f;
			if( direction == 10 ) {
				velocity = 0f;
			}
			lastDir = direction;
			lastX = x;
			lastY = y;
			x += EuclideanMath.rotatedXComponent(velocity, 0, (direction * circleDividedBy8) );
			y += EuclideanMath.rotatedYComponent(velocity, 0, (direction * circleDividedBy8) );
			lastEmulatedX = x;
			lastEmulatedY = y;
		}
		fed = false;
		feetAngle = EuclideanMath.D2LineToAngle(lastX, lastY, x, y);
		double angleRemainder = feetAngle%(Math.PI/4);
		feetAngle = (float) (feetAngle - angleRemainder);
		if( angleRemainder > Math.PI/8 ) { feetAngle += (Math.PI/4) ; }
		if( Float.isNaN(feetAngle) ) { feetAngle = lastAngle; }
		transformMatrix.translation( -sprite.texture.width/2, -sprite.texture.height/2, 10 );
		transformMatrix.applyRotation( feetAngle, 0f, 0f, 1f );
		transformMatrix.applyTranslation( x, y, 0 );

		lastAngle = feetAngle;
		
		if( flagged == 1 ) {
			int otherTeam;
			otherTeam = (team == 1) ? 2 : 1;
			this.spawner.map.flagTeam[otherTeam].x = (int) x-16;
			this.spawner.map.flagTeam[otherTeam].y = (int) y-16;
		}
	}

	@Override
	public void dynamicDraw(Program program) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
		Matrix4x4f m = new Matrix4x4f();
		m.translation(aimx, aimy, 10);
		program.loadMatrix( "transformMatrix", m );
		aimSpr.draw();
	}
	
}
