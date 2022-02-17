package game.thing;

import game.Game;
import game.Sprite;

public class Weapon {
	
	public final static int MAX_TYPE = 3;
	
	public static final int TYPE_DEAGLE = 0;
	public static final int TYPE_SHOT = 1;
	public static final int TYPE_ZOOKA = 2;
	
	public static final Sprite[][] weaponSprites = new Sprite[3][16];
	
	// organize sprites
	static {
		for( int i = 0; i < 16; i ++ ) {
			weaponSprites[TYPE_DEAGLE][i] = Game.spriteList.get( "spr_deagle0" );
			weaponSprites[TYPE_SHOT][i] = Game.spriteList.get( "spr_deagle0");
			weaponSprites[TYPE_ZOOKA][i] = Game.spriteList.get( "spr_deagle0");
		}
	}
	
	public static void init() 
	{}
	
	public static int getIDFromName ( String weaponName ) {
		switch( weaponName ) {
		case "Popper":
			return 0;
			
		case "Shotter":
			return 1;
			
		case "Zooker":
			return 2;
			
		}
		throw new IllegalArgumentException("Invalid Weapon Name");
		
	}
	
}
