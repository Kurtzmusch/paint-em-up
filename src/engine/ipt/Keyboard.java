
package engine.ipt;

import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;

import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import engine.Engine;
import engine.opt.Window;

public class Keyboard {
	
	public String lastString = new String();
	
	public boolean keyDown[] = new boolean[2 << 12];
	public boolean keyPress[] = new boolean[2 << 12];
	public LinkedList<Integer> pressList = new LinkedList<Integer>();
	
	public GLFWKeyCallback keyCall;
	
	public GLFWCharCallback textCall;
	
	public void clear(){
		for( Integer i : pressList ){
			keyPress[i.intValue()] = false;
		}
		pressList.clear();
	}
	
	public void attachToWindow( Window window ){
		
		window.keyboardList.add( this );
		glfwSetKeyCallback( window.id, keyCall );
		glfwSetCharCallback( window.id, textCall );
		
	}
	
	public Keyboard() {
		if( Engine.glfwStarted ){
			keyCall = new GLFWKeyCallback() {
				@Override
				public void invoke(long window, int key, int scancode, int action, int mods) {
					if( action == GLFW.GLFW_PRESS ){
						
						keyDown[key] = true; keyPress[key] = true;
						pressList.add( new Integer(key) );
						if( (key == GLFW.GLFW_KEY_BACKSPACE) ) {
							if( lastString.length() > 0 ) {
							lastString = lastString.substring(0, lastString.length()-1);
							}
						}
						
					}
					
					if( action == GLFW.GLFW_RELEASE ){	keyPress[key] = false; keyDown[key] = false;   }
				}
			};
			textCall = new GLFWCharCallback() {
				@Override
				public void invoke(long window, int key) {
					if( key < 32 || key > 125 ){ key = 63; }
					lastString = lastString.concat(Character.toString((char)key));
				}
			};
			
		}
		else{
			System.err.println("Gotta start that engine before creating a keyboard.");
		}
	}
	
}