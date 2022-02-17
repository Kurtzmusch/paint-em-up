package game.thing.client;

import game.Game;
import game.thing.server.SPlayer;
import game.thing.server.SWeapon;

public class CDeagle extends CWeapon {
	
	protected CDeagle( CPlayer aPlayer, int type, CSSpawner aSpawner ) {
		super(aPlayer, type, aSpawner );
		reloadCD = 10;
		reloading = 0;
		
		shootSound = Game.soundList.get("deagle_pop");
		spawner.shootSoundSource.setRawSound(shootSound);
		
	}
	
	public void shoot() {
		
		new CBullet( spawner, 6, direction, x, y, this );
		
	}
	
	public void burstParticles( float x, float y, float normal, float direction, int aColor ) {
		
		float bounceAngle;
		float invertedDirection = (float) (direction - Math.PI);
		
		float normDif = (float) (Math.PI - normal);
		float newNormal = (float) (Math.PI);
		
		float newInvertedDirection = invertedDirection + normDif;
		
		if( newInvertedDirection > Math.PI*2 ) { newInvertedDirection -= Math.PI*2; }
				
		if( newInvertedDirection < 0 ) { newInvertedDirection = (float) (Math.PI*2 + newInvertedDirection); }
		
		float angleDifference = (newInvertedDirection-newNormal);
		
		bounceAngle = normal - angleDifference;
		
		SplatterParticle1.burstParticles( aColor , 8, x, y, bounceAngle-(10*(float)(Math.PI/180f)), bounceAngle+(10*(float)(Math.PI/180f)), 1f/3f*2f, 2f/3f*2f, -0.5f/2f );
		SplatterParticle1.burstParticles( aColor , 10, x, y, bounceAngle-(45*(float)(Math.PI/180f)), bounceAngle+(45*(float)(Math.PI/180f)), 2f/3f*2f, 4f/3f*2f, -0.75f/2f );
		SplatterParticle1.burstParticles( aColor , 10, x, y, bounceAngle-(2*(float)(Math.PI/180f)), bounceAngle+(2*(float)(Math.PI/180f)), 4f/3f*2f, 6.5f/3f*2f, -1.25f/2f );
	
		SplatterParticle2.burstParticles( aColor , x, y, normal );
	}
	
}
