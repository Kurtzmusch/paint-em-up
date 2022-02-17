package game.thing.server;

import game.thing.client.CBullet;

public class SShot extends SWeapon {

	protected SShot( SPlayer aPlayer, int type, SSSpawner aSpawner ) {
		super( aPlayer, type, aSpawner );
		reloadCD = 40;
		reloading = 2;
	}
	
	public void kidShoot() {
		new SBullet( spawner, 5, direction-1f/32f, x, y );
		new SBullet( spawner, 5, direction+1f/32f, x, y );

	}
	
	public void shoot() {
		super.shoot();
	}

}
