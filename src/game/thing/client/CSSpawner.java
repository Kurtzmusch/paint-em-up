
package game.thing.client;

import java.util.LinkedList;

import engine.Program;
import engine.SoundSource;
import engine.util.Matrix4x4f;
import game.DynamicDrawerImplementor;
import game.Game;
import game.MapThing;
import game.Sprite;
import game.Text;

public class CSSpawner extends MapThing {
	
	
	public SoundSource splatSoundSource;
	public SoundSource shootSoundSource;
	public int team;
	
	private Matrix4x4f transformMatrix;
	private Sprite sprite;
	
	public CPlayer player;
	public int score, deaths, kills;
	public String name;
	public CSMap map;
	
	public static int maxIndex = Game.colorList.size();;
	public int spriteIndex;
	public static Sprite[] spriteList= new Sprite[maxIndex];
	static {
		for( int c = 0; c < maxIndex; c ++ ) {
			spriteList[c] = Game.spriteList.get("spr_spawner"+c);
		}	
	}
	public static Sprite dmSprite = Game.spriteList.get("spr_spawner_dm");
	
	public Text nameText, killsText, scoreText, deathsText;
	
	private DynamicDrawerImplementor dynamicDrawer;
	
	public static LinkedList<CSSpawner> instanceList = new LinkedList<CSSpawner>();
	
	public CSSpawner( int x, int y, int i, int aSpriteIndex, CSMap aMap ) {
		super( x, y );
		
		map = aMap;
		name = "unknown";
		score = deaths = kills = 0;
		this.team = i;	
		spriteIndex = aSpriteIndex;
		if( team != 0 ) {	sprite = spriteList[spriteIndex]; }
		else { sprite = dmSprite; }
		
		nameText = new Text( name, Game.courierBitmap, 1 );
		scoreText = new Text( String.valueOf( score ), Game.courierBitmap, 1 );
		killsText = new Text( String.valueOf( kills ), Game.courierBitmap, 1 );
		deathsText = new Text( String.valueOf( deaths), Game.courierBitmap, 1 );
		
		shootSoundSource = new SoundSource();
		splatSoundSource = new SoundSource();
		
		
		
		transformMatrix = new Matrix4x4f();
		transformMatrix.translation( x, y, 8 );

		
		dynamicDrawer = new DynamicDrawerImplementor( 8 ) {
			@Override
			public void dynamicDraw(Program program) {
				CSSpawner.this.dynamicDraw(program);
			}
		};
		
		//Game.enableDynamicDrawer( dynamicDrawer );
		
		instanceList.add(this);
		
	}
	
	public void updateTexts() {
		nameText.destroy();
		nameText = new Text( name, Game.courierBitmap, 1 );
		scoreText.destroy();
		scoreText = new Text( String.valueOf( score ), Game.courierBitmap, 1 );
		killsText.destroy();
		killsText = new Text( String.valueOf( kills ), Game.courierBitmap, 1 );
		deathsText.destroy();
		deathsText = new Text( String.valueOf( deaths), Game.courierBitmap, 1 );
	}
	
	protected void dynamicDraw(Program program) {
		program.loadMatrix( "transformMatrix", transformMatrix );
		sprite.draw();
	}

	public void destroy() {
		nameText.destroy(); killsText.destroy(); scoreText.destroy(); deathsText.destroy();
		if( player != null ) {
			player.destroy();
		}
		instanceList.remove(this);
		Game.disableDynamicDrawer( dynamicDrawer );
	}
	
}
