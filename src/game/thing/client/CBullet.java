package game.thing.client;

import java.util.LinkedList;

import engine.Program;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.EuclideanMath;
import game.Game;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.DynamicDrawer;
import game.interfaces.Ticker;

public class CBullet implements Ticker, DynamicDrawer {

	public float speed;
	public float direction;
	public float x, y, lastX, lastY;
	
	public Sprite sprite;
	public Matrix4x4f transformMatrix;
	
	public int color;
	public CSSpawner spawner;
	public CWeapon weapon;
	public TickerImplementor ticker;
	public DynamicDrawerImplementor dynamicDrawer;
	
	public CBullet( CSSpawner aSpawner, float aSpeed, float aDirection, float ax, float ay, CWeapon aWeapon ) {
		
		weapon = aWeapon;

		spawner = aSpawner;
		speed = aSpeed;
		direction = aDirection;
		color = weapon.color;
		x = ax;
		y = ay;
		sprite = Game.spriteList.get("spr_bullet"+color);
		
		transformMatrix = new Matrix4x4f();
		
		ticker = new TickerImplementor( 12 ) { // after player moves
			@Override
			public void tick() {
				CBullet.this.tick();
			}
		};
		
		dynamicDrawer = new DynamicDrawerImplementor( 12 ) {
			@Override
			public void dynamicDraw( Program program ) {
				CBullet.this.dynamicDraw( program );
			}
		};
		
		Game.enableTicker( ticker );
		Game.enableDynamicDrawer( dynamicDrawer );
		tick(); // call 1 tick to make up for lag
	}
	
	public void tick() {
		lastX = x;
		lastY = y;
		x += EuclideanMath.rotatedXComponent( speed, 0, direction );
		y += EuclideanMath.rotatedYComponent( speed, 0, direction );
		

		LinkedList<CollisionLine> collisionList = new LinkedList<CollisionLine>();
		LinkedList<CollisionLine> wallCollisionList;
		for( CSWall w : CSWall.instanceList ) {
			wallCollisionList = w.collisionLine( lastX, lastY, x, y );
			if( !wallCollisionList.isEmpty() ){
				for( CollisionLine cl : wallCollisionList ) {
					collisionList.add(cl);
				}
				
			}
		}
		if( !collisionList.isEmpty() ) {
			float smallerDistance = Float.MAX_VALUE;
			float tempDistance;
			CollisionLine selected = null;
			for( CollisionLine cl : collisionList ) {
				tempDistance = EuclideanMath.distancePointPoint(lastX, lastY, cl.x, cl.y);
				if( tempDistance < smallerDistance ) {
					smallerDistance = tempDistance;
					selected = cl;
				}
			}
			x = selected.x;
			y = selected.y;
			destroy();
			weapon.burstParticles( x, y, selected.direction, direction, color );
		}
		float norm;
		float xIntersects = 0, yIntersects = 0;

		for( CPlayer player : CPlayer.instanceList ) {
			if( player != spawner.player ) {
				if( EuclideanMath.distanceLinePoint(lastX, lastY, x, y, player.x, player.y ) < 4f ) {
					
					xIntersects = EuclideanMath.lineIntersectCircleX(lastX, lastY, x, y, player.x, player.y, 4.1f );
					yIntersects = EuclideanMath.lineIntersectCircleY(lastX, lastY, x, y, player.x, player.y, 4.1f );
					x = xIntersects; y = yIntersects;
					destroy();
					norm = (float) (EuclideanMath.D2LineToAngle( player.x, player.y, x, y )-Math.PI);
					if( norm < 0 ) { norm = (float) (Math.PI*2 + norm); }
					weapon.burstParticles(x,y, norm, direction, color );
					break;
				}
			}
		}
		
		transformMatrix.translation( -8.5f, -1.5f,  12 ); // origin
		transformMatrix.applyScale( 0.5f, 0.5f, 1f );
		
		transformMatrix.applyRotation( direction, 0, 0, 1 );
		transformMatrix.applyTranslation( x, y,  12 );
	}
	
	public void dynamicDraw( Program program ) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}
	
	public void destroy() {
		spawner.splatSoundSource.setRawSound(	Game.soundList.get("splat" + ((int)(Math.random()*2)+1) ) );
		spawner.splatSoundSource.play();
		Game.disableTicker(ticker);
		Game.disableDynamicDrawer(dynamicDrawer);
	}
	
}
