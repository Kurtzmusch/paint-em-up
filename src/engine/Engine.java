
package engine;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;

import engine.ipt.Keyboard;
import engine.ipt.Mouse;
import engine.opt.Window;

public class Engine {

	public static boolean glfwStarted;
	
	public static Window window;
	public static Keyboard keyboard;
	public static Mouse mouse;
	
	private static GLFWErrorCallback callBack;
	
	public static void start(){
		
		callBack = GLFWErrorCallback.createPrint(System.err);
		callBack.set();
		glfwStarted = glfwInit();
		
		long device = alcOpenDevice( (String) null );
		long context = alcCreateContext( device, (int[]) null );
		
		setThreadAudioRenderTarget( context, device );
		
		
	}
	
	public static void setThreadGraphicsRenderTarget( Window window ) {
		
		glfwMakeContextCurrent( window.oglContext );
		GL.createCapabilities();
		
	}
	
	public static void setThreadAudioRenderTarget( long context, long device ) {
		
		alcMakeContextCurrent(context);
		
		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
		
	}
	
	public static void poll(){
		
		glfwPollEvents();
	
	}
	
	
}
