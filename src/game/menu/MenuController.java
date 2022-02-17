package game.menu;

import game.interfaces.Destroyable;

public interface MenuController extends Destroyable {

	public void changeMenu( Menu targetMenu );

	//public void destroy();

	public Menu getCurrentMenu();
	
}
