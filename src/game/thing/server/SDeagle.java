
package game.thing.server;

import game.thing.client.CBullet;

public class SDeagle extends SWeapon {
	
	
	public SDeagle( SPlayer aPlayer, int type, SSSpawner aSpawner ) {
		super( aPlayer, type, aSpawner );
		reloadCD = 12;
		reloading = 2;
	}
	
	public void kidShoot() {
		new SBullet( spawner, 6, direction, x, y );
	}
	
	public void shoot() {
		super.shoot();
	}

}
