package game.thing.client;

import engine.Program;
import engine.RawSound;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.EuclideanMath;
import game.Game;
import game.Sprite;
import game.TickerImplementor;
import game.interfaces.DynamicDrawer;
import game.interfaces.Ticker;
import game.thing.Weapon;
import game.thing.server.SDeagle;
import game.thing.server.SPlayer;
import game.thing.server.SShot;
import game.thing.server.SWeapon;
import game.thing.server.SZooka;

public class CWeapon implements Ticker, DynamicDrawer {

	public CPlayer player;
	
	public float x,y;
	public Matrix4x4f transformMatrix;
	public TickerImplementor ticker;
	public DynamicDrawerImplementor dynamicDrawer;
	public Sprite sprite;
	
	public CSSpawner spawner;
	
	public int team;
	
	public RawSound shootSound;
	
	public static final int TYPE_DEAGLE = 0;
	public static final int TYPE_SHOT = 1;
	public static final int TYPE_ZOOKA = 2;
	public byte type;
	public int color;
	public float direction;
	
	public int reloadCD;
	public int reloading;
	
	public byte shot;
	
	public static final Sprite[][][] weaponSprites = new Sprite[3][2][16];
	
	// organize sprites
	static {
		for( int i = 0; i < Game.colorList.size(); i ++ ) {
			weaponSprites[TYPE_DEAGLE][0][i] = Game.spriteList.get( "spr_weapon_deagle_loaded" + String.valueOf(i) );
			weaponSprites[TYPE_SHOT][0][i] = Game.spriteList.get( "spr_weapon_shot_loaded" + String.valueOf(i) );
			weaponSprites[TYPE_ZOOKA][0][i] = Game.spriteList.get( "spr_weapon_zooka_loaded" + String.valueOf(i) );
			
			weaponSprites[TYPE_DEAGLE][1][i] = Game.spriteList.get( "spr_weapon_deagle_reloading" + String.valueOf(i) );
			weaponSprites[TYPE_SHOT][1][i] = Game.spriteList.get( "spr_weapon_shot_reloading" + String.valueOf(i) );
			weaponSprites[TYPE_ZOOKA][1][i] = Game.spriteList.get( "spr_weapon_zooka_reloading" + String.valueOf(i) );
		}
	}
	
	public void burstParticles( float x, float y, float normal, float direction, int aColor ) {}
	
	protected CWeapon( CPlayer aPlayer, int aType, CSSpawner aSpawner ) {
		spawner = aSpawner;
		
		
		player = aPlayer;
		
		team = player.team;
		type = (byte) aType;
		
		reloading = 0;
		shot = 0;
		
		color = spawner.spriteIndex;
		if( team == 0 ) {
			color = (int) (Math.random()*Game.colorList.size());
		}
		sprite = CWeapon.weaponSprites[type][0][color];
		
		transformMatrix = new Matrix4x4f();
		
		ticker = new TickerImplementor( 1 ) {		
			@Override		
			public void tick() {
				CWeapon.this.tick();
			}
		};
		
		dynamicDrawer = new DynamicDrawerImplementor( 13 ) {
			@Override
			public void dynamicDraw(Program program) {
				// TODO Auto-generated method stub
				CWeapon.this.dynamicDraw(program);
			}
		};
		
		Game.enableTicker( ticker );
		Game.enableDynamicDrawer( dynamicDrawer );
		
	}
	
	public void destroy() {
		Game.disableTicker( ticker );
		Game.disableDynamicDrawer( dynamicDrawer );
	}
	
	public static CWeapon newWeapon( CPlayer aPlayer, int type, CSSpawner aSpawner ) {
		switch( type ) {
			case Weapon.TYPE_DEAGLE:
				return new CDeagle( aPlayer, type, aSpawner );
			case Weapon.TYPE_SHOT:
				return new CShot( aPlayer, type, aSpawner );
			case Weapon.TYPE_ZOOKA:
				return new CZooka( aPlayer, type, aSpawner );
		}
		return null;
	}
	
	@Override
	public void dynamicDraw(Program program) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}

	@Override
	public void tick() {
		
		
		
		float playerAngle = (float)(EuclideanMath.D2LineToAngle(player.x, player.y, player.aimx, player.aimy) - Math.PI/2);
		x = player.x + EuclideanMath.rotatedXComponent(4, 0, playerAngle);
		y = player.y + EuclideanMath.rotatedYComponent(4, 0, playerAngle);
		
		direction = EuclideanMath.D2LineToAngle(x, y, player.aimx, player.aimy);
		
		transformMatrix.translation( -sprite.texture.width/2, -sprite.texture.height/2, 12 );
		transformMatrix.applyRotation( direction, 0f, 0f, 1f );
		transformMatrix.applyTranslation( x, y, 0 );
		
		if( reloading == reloadCD ) {
			shot = 0;
		}
		if( reloading > 0 ) {
			reloading --;
		}
		if( reloading == 0 ) {
			sprite = CWeapon.weaponSprites[type][0][color];
		}
		
		if( shot == 1 ) {
			reloading = reloadCD;
			shoot();
			if( team == 0 ) {
				color = (int) (Math.random()*Game.colorList.size());
			}
			sprite = CWeapon.weaponSprites[type][1][color];
			spawner.shootSoundSource.play();
			
		}
		
	}
	public void shoot() {}
	
	
	
}
