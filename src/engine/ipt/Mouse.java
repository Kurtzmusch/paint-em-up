package engine.ipt;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import engine.Engine;
import engine.opt.Window;

public class Mouse {

	private GLFWMouseButtonCallback button;
	private GLFWCursorPosCallback position;
	private GLFWScrollCallback scroll;
	
	public float x;
	public float y;
	
	public boolean mbLeftDown = false;
	public boolean mbRightDown = false;
	public boolean mbLeftPressed = false;
	public boolean mbRightPressed = false;
	public boolean scrollUp = false;
	public boolean scrollDown = false;
	
	public void clear(){
		
		mbLeftPressed = false;
		mbRightPressed = false;
		scrollUp = false;
		scrollDown = false;
		
	}
	
	public void attachToWindow( Window window ){
		
		window.mouseList.add( this );
		glfwSetMouseButtonCallback( window.id, button );
		glfwSetCursorPosCallback( window.id, position );
		glfwSetScrollCallback( window.id, scroll );
		
	}
	
	public Mouse() {
		if( Engine.glfwStarted ){
			scroll = new GLFWScrollCallback() {
				@Override
				public void invoke( long window, double xoffset, double yoffset ) {
					if ( yoffset > 0 ) {
						scrollUp = true;
					}
					else {
						if ( yoffset < 0 ) {
							scrollDown = true;
						}
					}
				}
			};
			button = new GLFWMouseButtonCallback() {
				@Override
				public void invoke(long window, int button, int action, int mods) {
					if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT){ 
						if( action == GLFW.GLFW_PRESS ){
							mbLeftPressed = true;
							mbLeftDown = true;
						}
						if( action == GLFW.GLFW_RELEASE ){
							mbLeftPressed = false;
							mbLeftDown = false;
						}
					}
					if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT){ 
						
						if( action == GLFW.GLFW_PRESS ){
							mbRightPressed = true;
							mbRightDown = true;
						}
						if( action == GLFW.GLFW_RELEASE ){
							mbRightPressed = false;
							mbRightDown = false;
						}
					}
					
				}
			};
			position = new GLFWCursorPosCallback() {
				@Override
				public void invoke(long window, double xpos, double ypos) {
					
					x = (float) xpos;
					y = (float) ypos;
					
					
				}
			};
		}
		else{
			System.err.println("Gotta start that engine before creating a mouse.");
		}
	}
	
	public void set( int x, int y, Window window ){
		
		glfwSetCursorPos(window.id, x, y);
		
		this.x = x;
		this.y = y;
		
	}
	
}
