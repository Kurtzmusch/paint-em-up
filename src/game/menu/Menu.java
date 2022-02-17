
package game.menu;

import java.util.LinkedList;

import engine.Program;

import static org.lwjgl.glfw.GLFW.*;

import game.Game;
import game.interfaces.Destroyable;
import game.interfaces.GUIDrawer;
import game.interfaces.Ticker;

public class Menu implements GUIDrawer, Ticker, Destroyable {

	public MenuItem[] options;
	public MenuItem selectedItem;
	public int current = 0;
	public boolean lock = false;
	public boolean hidden;
	
	public Menu() {
		options = new MenuItem[0];
	}
	
	public void updateScale() {
		for(MenuItem m : options ) {
			m.updateTextScale();
		}
	}
	public void hide() {
		hidden = true;
	}
	
	public void show() {
		hidden = false;
	}
	
	public void replace( MenuItem newItem, int position ) {
		selectedItem = null;
		options[position].destroy(); // TODO check for resource leak
		options[position] = newItem;
		reset();
	}
	
	public void fill( LinkedList<MenuItem> itemList  ) {
		options = new MenuItem[itemList.size()];
		for(int i = 0; i < itemList.size(); i ++ ) {
			options[i] = itemList.get(i);
		}
		options[0].select();
		selectedItem = options[0];
	}
	
	public Menu( LinkedList<MenuItem> itemList ) {
		
		options = new MenuItem[itemList.size()];
		for(int i = 0; i < itemList.size(); i ++ ) {
			options[i] = itemList.get(i);
		}
		options[0].select();
		selectedItem = options[0];
		
	}
	
	public void reset() {
		for( MenuItem mi : options ) {
			mi.unselect();
		}
		options[0].select();
		selectedItem = options[0];
		current = 0;
		onReset();
	}
	
	public void onReset() {}

	@Override
	public void guiDraw( Program program ) {
		if (hidden) {return;}
		for( MenuItem mi : options ) {
			mi.draw( program );
		}
	}
	
	public void lock() {
		lock = true;
	}
	
	public void unlock() {
		lock = false;
	}
	
	@Override
	public void tick() {
		if (hidden) {return;}
		if ( !lock ) {
			if ( (Game.mainKeyboard.keyPress[GLFW_KEY_UP]) || (Game.mainKeyboard.keyPress[GLFW_KEY_W]) ) {
				selectedItem.unselect();current = warp(--current);
				selectedItem = options[current];
				selectedItem.select();
			}
			if ( (Game.mainKeyboard.keyPress[GLFW_KEY_DOWN]) || (Game.mainKeyboard.keyPress[GLFW_KEY_S]) ) {
				selectedItem.unselect();current = warp(++current);
				selectedItem = options[current];
				selectedItem.select();
			}	
		}
		if ( (Game.mainKeyboard.keyPress[GLFW_KEY_SPACE]) || (Game.mainKeyboard.keyPress[GLFW_KEY_ENTER]) ) {
			if( selectedItem.enabled ) {
				selectedItem.perform();
			}
		}
		for( MenuItem mi : options ) {
			if ( mi instanceof Ticker ) {
				
				((Ticker) mi).tick();
			}
		}

		
		
	}
	
	public int warp( int c ) {
		
		if (c < 0) {
			c = options.length-1;
			return c;
		}
		
		if (c == options.length) {
			c = 0;
			return c;
		}
		return c;
		
	}

	@Override
	public void destroy() {
		for( MenuItem mi : options ) {
			mi.destroy();
		}
	}
	
}