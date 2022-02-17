package game.thing.client;

import game.Game;

public class CZooka extends CWeapon {

	protected CZooka(CPlayer aPlayer, int type, CSSpawner aSpawner) {
		super(aPlayer, type, aSpawner);
		reloadCD = 118;
		reloading = 2;
		
		shootSound = Game.soundList.get("deagle_shoot");
		spawner.shootSoundSource.setRawSound(shootSound);
		
	}

}
