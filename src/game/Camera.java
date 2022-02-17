
package game;

import org.lwjgl.glfw.GLFW;

import engine.util.Matrix4x4f;

public class Camera {

	public float x, y, z;
	public float xrot, yrot, zrot;
	public float width, height;
	public float currentWidth, currentHeight;
	public float projectionSmoothness;

	public Matrix4x4f viewMatrix;
	public Matrix4x4f viewRotationMatrix;
	public Matrix4x4f viewTranslationMatrix;
	public Matrix4x4f projectionMatrix;
	public Matrix4x4f guiProjectionMatrix;
	
	
	public Camera() {
		x = 0; y = 0; z = 0;
		xrot = 0; yrot = 0; zrot = 0;
	
		width = 1280;
		height = 720;
		currentWidth = width;
		currentHeight = height;
		projectionSmoothness = 0.2f;
		
		viewMatrix = new Matrix4x4f();
		viewMatrix.identity();
		viewTranslationMatrix = new Matrix4x4f();
		viewTranslationMatrix.identity();
		viewRotationMatrix = new Matrix4x4f();
		viewRotationMatrix.identity();
		projectionMatrix = new Matrix4x4f();
		guiProjectionMatrix = new Matrix4x4f();
		
		projectionMatrix.orthographic(-width/2f, width/2f, height/2f, -height/2f, -32f, 32f);
		guiProjectionMatrix.orthographic(-width/2f, width/2f, height/2f, -height/2f, -32f, 32f);
		
		updateScale();
		
	}
	
	public void updateScale() {

		guiProjectionMatrix.orthographic(-Game.mainWindow.width/2f, Game.mainWindow.width/2f, Game.mainWindow.height/2f, -Game.mainWindow.height/2f, -32f, 32f );
		//projectionMatrix.orthographic(-Game.mainWindow.width/2f, Game.mainWindow.width/2f, Game.mainWindow.height/2f, -Game.mainWindow.height/2f, -32f, 32f );
		
	}
	
	public void updateProjectionMatrix() {
		float deltaWidth = width - currentWidth;
		currentWidth += projectionSmoothness*deltaWidth;
		float deltaHeight = height-currentHeight;
		currentHeight += projectionSmoothness*deltaHeight;
		projectionMatrix.orthographic(-currentWidth/2f, currentWidth/2f, currentHeight/2f, -currentHeight/2f, -32f, 32f);
	}
	
	public void updateViewMatrix(){
		Matrix4x4f tempMatrix = new Matrix4x4f();
		/*
		tempMatrix.rotatation( -zrot, 0f, 0f, 1f );
		viewRotationMatrix.multiply( tempMatrix );
		tempMatrix.rotatation( -xrot, 1f, 0f, 0f );
		viewRotationMatrix.multiply( tempMatrix );
		*/
		
		//viewTranslationMatrix.translation( (float)((int)(-x)), (float)((int)(-y)), (float)((int)(-z)) ); 
		
		viewMatrix.translation( -x,-y,-z );
		//viewMatrix.identity();
		
		tempMatrix.rotatation( -zrot, 0f, 0f, 1f );
		viewMatrix.multiply( tempMatrix );
		tempMatrix.rotatation( -xrot, 1f, 0f, 0f );
		viewMatrix.multiply( tempMatrix );
	}

	public void tick() {
		updateProjectionMatrix();
		updateViewMatrix();
		if (Game.mainKeyboard.keyPress[GLFW.GLFW_KEY_C]) {
			System.out.println("x: " + x + "y: " + y);
		}
	}
	
}
