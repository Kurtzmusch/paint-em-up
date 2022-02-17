package engine.opt;

import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import engine.Engine;
import engine.ipt.Keyboard;
import engine.ipt.Mouse;
import game.Game;

public class Window {

	public long id = 0l;
	public long oglContext;
	
	public int width;
	public int height;
	public boolean resized;
	
	public boolean fs;
	
	public boolean shouldClose = false;
	
	public String title = "	Unnamed";
	
	public LinkedList<Keyboard> keyboardList = new LinkedList<Keyboard>();
	public LinkedList<Mouse> mouseList = new LinkedList<Mouse>();

	public void attachMouse( Mouse mouse ){
		
		if(id != 0l){
			mouse.attachToWindow( this );
		}
		
	}
	
	public void destroy() {
		GLFW.glfwDestroyWindow( id );
	}
	
	public void show() {
		GLFW.glfwShowWindow( id );
	}
	
	public void attachKeyboard( Keyboard keyboard ){
		
		if(id != 0l){
			keyboard.attachToWindow( this );
		}
		
	}
	
	public void swap() {
		glfwSwapBuffers( id );
		
	}
	
	public void resetInput() {
		if( shouldClose == false ) { 
			shouldClose = glfwWindowShouldClose( this.id );
		}
		
		for( Mouse m : mouseList ){
			m.clear();
		}
		
		for( Keyboard k : keyboardList ){
			k.clear();
		}
		if( shouldClose == false ) { 
			shouldClose = glfwWindowShouldClose( this.id );
		}
	}
	
	/**
	 * This method swaps the buffers, clears the backbuffer(color and depth),
	 * polls shouldClose hint and reset the state of input devices attached to this display 
	*/
	public void update(){
		resized = false;
		glfwSwapBuffers( id );
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		
		if( shouldClose == false ) { 
			shouldClose = glfwWindowShouldClose( this.id );
		}
		
		for( Mouse m : mouseList ){
			m.clear();
		}
		
		for( Keyboard k : keyboardList ){
			k.clear();
		}
		
	}
	
	public void togleVSync( String s ) {
		if( s.equals("ON") ) { 
			glfwSwapInterval(1);
		}
		else {
			glfwSwapInterval(0);
		}
	}
	
	public void iconify() {
		GLFW.glfwIconifyWindow( this.id );
	}
	
	public void togleFullScreen( String s ) {
		// toggling vsync is necessary cause togling FS may change vsync to platform defautls
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidMode = glfwGetVideoMode(monitor);
		if( s.equals("ON") ) {
			fs = true;
			width = vidMode.width();
			height = vidMode.height();
			glfwSetWindowMonitor( id, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate() );
			glViewport( 0, 0, vidMode.width(), vidMode.height() );
			resized = true;
			togleVSync(Game.config.vSync);
		}
		else {
			
				fs = false;
				width = 1280;
				height = 720;
				glfwSetWindowMonitor( id, 0l, vidMode.width()/2 - width/2, vidMode.height()/2 - height/2, width, height, vidMode.refreshRate() );
				glViewport( 0, 0, width, height );
				resized = true;
				togleVSync(Game.config.vSync);// this is necessary cause togling FS may change vsync to platform defautls
		}
		
	}
	
	public Window( int width, int height, boolean decorated, boolean visible ) {
		
		this.width = width;
		this.height = height;
		
		if( Engine.glfwStarted ){
			glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3 );
			glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3 );
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			if( visible ) {
				glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
			}
			else {
				glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
			}
			if( decorated ) {
				glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE );
			}
			else {
				glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE );
			}
			glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
			fs = false;
			this.id = glfwCreateWindow( width, height, title, 0l, 0l );
			long monitor = glfwGetPrimaryMonitor();
			GLFWVidMode vidMode = glfwGetVideoMode(monitor);
			GLFW.glfwSetWindowPos( id, vidMode.width()/2-(width/2), vidMode.height()/2-(height/2) );
			this.oglContext = this.id;
			
		}
		else{
			System.err.println("Gotta start that engine before creating a window.");
		}
		
	}
	
}
