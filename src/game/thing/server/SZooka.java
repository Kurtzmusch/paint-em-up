package game.thing.server;

import game.thing.client.CBullet;

public class SZooka extends SWeapon {

	protected SZooka( SPlayer aPlayer, int type, SSSpawner aSpawner ) {
		super( aPlayer, type, aSpawner );
		reloadCD = 120;
		reloading = 0;
	}
	
	public void shoot() {
		
		new SBullet( spawner, 2, direction-1, x, y );
		new SBullet( spawner, 2, direction+1, x, y );
	}

}
