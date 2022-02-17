package game;

import org.lwjgl.glfw.GLFW;

import engine.Program;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;



public class Debugger implements GUIDrawer, Ticker  {

	GUIDrawerImplementor guiDrawer;
	TickerImplementor ticker;
	boolean show;
	
	public String fpsString, vramString, tickInstanceCountString,
	dynamicDrawInstanceCountString, guiDrawInstanceCountString,
	avgDrawString, avgTickString, ramString, particlesString;

	Text fpsText, vramText, avgDrawText, avgTickText, tickInstanceCountText,
	dynamicDrawInstanceCountText, guiDrawInstanceCountText, ramText, particlesText;
	Text fpsDescriptionText, vramDescriptionText, avgDrawDescriptionText, 
	avgTickDescriptionText, dynamicDrawInstanceCountDescriptionText,
	guiDrawInstanceCountDescriptionText, tickInstanceCountDescriptionText,
	ramDescriptionText, particlesDescriptionText;
	
	public Debugger() {
		show = false;
		
		fpsDescriptionText = new Text( "FPS: ", Game.courierBitmap, 2 );
		ramDescriptionText = new Text( "RAM: ", Game.courierBitmap, 2 );
		vramDescriptionText = new Text( "VRAM: ", Game.courierBitmap, 2 );
		avgDrawDescriptionText = new Text( "AVG DRAW: ", Game.courierBitmap, 2 );
		avgTickDescriptionText = new Text( "AVG TICK: ", Game.courierBitmap, 2 );
		tickInstanceCountDescriptionText = new Text( "T INSTs: ", Game.courierBitmap, 2 );
		dynamicDrawInstanceCountDescriptionText = new Text( "DD INSTs: ", Game.courierBitmap, 2 );
		guiDrawInstanceCountDescriptionText = new Text( "GD INSTs: ", Game.courierBitmap, 2 );
		particlesDescriptionText = new Text( "PARTs: ", Game.courierBitmap, 2 );
		
		fpsDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*2, 0 );
		ramDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*3, 0 );
		vramDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*4, 0 );
		avgDrawDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*5, 0 );
		avgTickDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*6, 0 );
		tickInstanceCountDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*7, 0 );
		dynamicDrawInstanceCountDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*8, 0 );
		guiDrawInstanceCountDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*9, 0 );
		particlesDescriptionText.transformMatrix.translation( -Game.mainWindow.width/2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*10, 0 );

		
		fpsText = new Text( fpsString, Game.courierBitmap, 2 );
		fpsText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"FPS: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*2, 0 );

		ramText = new Text( ramString, Game.courierBitmap, 2 );
		ramText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"RAM: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*3, 0 );
		
		vramText = new Text( vramString, Game.courierBitmap, 2 );
		vramText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"VRAM: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*4, 0 );

		avgDrawText = new Text( avgDrawString, Game.courierBitmap, 2 );
		avgDrawText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"AVG DRAW: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*5, 0 );

		avgTickText = new Text( avgTickString, Game.courierBitmap, 2 );
		avgTickText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"AVG TICK: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*6, 0 );

		tickInstanceCountText = new Text( tickInstanceCountString, Game.courierBitmap, 2 );
		tickInstanceCountText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"T INSTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*7, 0 );
		
		dynamicDrawInstanceCountText = new Text( dynamicDrawInstanceCountString, Game.courierBitmap, 2 );
		dynamicDrawInstanceCountText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"DD INSTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*8, 0 );
		
		guiDrawInstanceCountText  = new Text( guiDrawInstanceCountString, Game.courierBitmap, 2 );
		guiDrawInstanceCountText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"GD INSTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*9, 0 );
		
		particlesText  = new Text( particlesString, Game.courierBitmap, 2 );
		particlesText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"PARTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*10, 0 );
		
		
		guiDrawer = new GUIDrawerImplementor( 10 ) {
			public void guiDraw( Program p ) {
				Debugger.this.guiDraw( p );
			}
		};
		
		ticker = new TickerImplementor( 10 ) {
			public void tick() {
				Debugger.this.tick();
			}
		};
		
		Game.enableTicker( ticker );
		
		toggle();
	}
	
	public void tick() {
		if( Game.mainKeyboard.keyPress[39] ) {
			toggle();
		}
	}

	public void toggle() {
		if( show ) {
			Game.disableGUIDrawer( guiDrawer );
			show = false;
		}
		else {
			Game.enableGUIDrawer( guiDrawer );
			show = true;
		}
	}
	
	public void updateStrings() {
		fpsText.destroy();
		fpsText = new Text( fpsString, Game.courierBitmap, 2 );
		fpsText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"FPS: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*2, 0 );
		
		ramText.destroy();
		ramText = new Text( ramString, Game.courierBitmap, 2 );
		ramText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"RAM: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*3, 0 );
		
		vramText.destroy();
		vramText = new Text( vramString, Game.courierBitmap, 2 );
		vramText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"VRAM: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*4, 0 );
		avgDrawText.destroy();
		avgDrawText = new Text( avgDrawString, Game.courierBitmap, 2 );
		avgDrawText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"AVG DRAW: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*5, 0 );
		avgTickText.destroy();
		avgTickText = new Text( avgTickString, Game.courierBitmap, 2 );
		avgTickText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"AVG TICK: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*6, 0 );
		tickInstanceCountText.destroy();
		tickInstanceCountText = new Text( tickInstanceCountString, Game.courierBitmap, 2 );
		tickInstanceCountText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"T INSTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*7, 0 );
		dynamicDrawInstanceCountText.destroy();
		dynamicDrawInstanceCountText = new Text( dynamicDrawInstanceCountString, Game.courierBitmap, 2 );
		dynamicDrawInstanceCountText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"DD INSTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*8, 0 );
		guiDrawInstanceCountText.destroy();
		guiDrawInstanceCountText = new Text( guiDrawInstanceCountString, Game.courierBitmap, 2 );
		guiDrawInstanceCountText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"GD INSTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*9, 0 );
		particlesText.destroy();
		particlesText = new Text( particlesString, Game.courierBitmap, 2 );
		particlesText.transformMatrix.translation( -Game.mainWindow.width/2 + Game.courierBitmap.cellW*"PARTs: ".length()*2, Game.mainWindow.height/2 - Game.courierBitmap.cellH*2*10, 0 );
		
	}
	
	@Override
	public void guiDraw(Program program) {
		
		fpsDescriptionText.draw(program);
		ramDescriptionText.draw(program);
		vramDescriptionText.draw(program);
		avgDrawDescriptionText.draw(program);
		avgTickDescriptionText.draw(program);
		tickInstanceCountDescriptionText.draw(program);
		dynamicDrawInstanceCountDescriptionText.draw(program);
		guiDrawInstanceCountDescriptionText.draw(program);
		particlesDescriptionText.draw(program);
		
		fpsText.draw(program);
		ramText.draw(program);
		vramText.draw(program);
		avgDrawText.draw(program);
		avgTickText.draw(program);
		tickInstanceCountText.draw(program);
		dynamicDrawInstanceCountText.draw(program);
		guiDrawInstanceCountText.draw(program);
		particlesText.draw(program);
	}
	
}