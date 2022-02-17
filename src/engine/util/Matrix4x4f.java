
package engine.util;

import static java.lang.Math.*;

import java.nio.FloatBuffer;


import engine.DirectBuffer;


public class Matrix4x4f {

	public float[] value;
	
	public Matrix4x4f(){
		
		value = new float[4 * 4];
		
		for( int i = 0; i < 16; i ++ ){
			value[i] = 0.0f;
		}
		
	}
	
	public void scale( float x, float y, float z) {
		
		this.identity();
		
		value[ 0 + 0*4 ] *= x;
		value[ 1 + 1*4 ] *= y;
		value[ 2 + 2*4 ] *= z;
	}
	
	public void identity(){

		for( int i = 0; i < 16; i ++ ){
			value[i] = 0.0f;
		}
		
		value[ 0 + 0*4 ] = 1.0f;
		value[ 1 + 1*4 ] = 1.0f;
		value[ 2 + 2*4 ] = 1.0f;
		value[ 3 + 3*4 ] = 1.0f;
		
	}
	
	public void multiply( Matrix4x4f m ){
		
		Matrix4x4f tempMatrix = new Matrix4x4f();
		
		for( int y = 0; y < 4; y ++ ){
			for( int x = 0; x < 4; x ++ ){
				float sum = 0.0f;
				for( int e = 0 ; e < 4; e ++ ){
					sum += this.value[e + y*4] * m.value[x + e*4];
				}
				tempMatrix.value[ x + y*4 ] = sum;
			}
		}
		
		for( int i = 0; i < 16; i ++ ){
			this.value[i] = tempMatrix.value[i];
		}
		
	}
	
	public void applyRotation( float angle, float x, float y, float z ){
		
		Matrix4x4f tempMatrix = new Matrix4x4f();
		tempMatrix.rotatation(angle, x, y, z);
		
		this.multiply(tempMatrix);
		
	}
	
	public void applyTranslation( float x, float y, float z ){
		
		Matrix4x4f tempMatrix = new Matrix4x4f();
		tempMatrix.identity();
		tempMatrix.translation(x, y, z);
		
		this.multiply(tempMatrix);
	
	}
	
	public void applyScale(  float x, float y, float z ) {
		
		Matrix4x4f tempMatrix = new Matrix4x4f();
		tempMatrix.identity();
		tempMatrix.scale(x, y, z);
		
		this.multiply(tempMatrix);
		
	}
	
	public void translation( float x, float y, float z ){
		this.identity();
		
		this.value[0 + 3*4] = x;
		this.value[1 + 3*4] = y;
		this.value[2 + 3*4] = z;
	}
	
	public void rotatation( float angle, float x, float y, float z ){
		
		this.identity();
		
		float cos = (float) cos(angle);
		float sin = (float) sin(angle);
		float dif = 1.0f - cos;
		
		this.value[0 + 0*4] = x * dif + cos;
		this.value[1 + 0*4] = y * x * dif + z * sin;
		this.value[2 + 0*4] = x * z * dif - y * sin;
		
		this.value[0 + 1*4] = x * y * dif - z * sin;
		this.value[1 + 1*4] = y * dif + cos;
		this.value[2 + 1*4] = y * z * dif + x * sin;
		
		this.value[0 + 2*4] = x * z * dif + y * sin;
		this.value[1 + 2*4] = y * z * dif - x * sin;
		this.value[2 + 2*4] = z * dif + cos;
	}
	
	public void orthographic( float left, float right, float top, float bottom, float near, float far ){
		
		this.identity();
		
		this.value[0 + 0*4] = 2.0f / (right - left);
		this.value[1 + 1*4] = 2.0f / (top - bottom);
		this.value[2 + 2*4] = 2.0f / (far - near);
		
		//this.value[0 + 3*4] = (left + right) / (left - right);
		//this.value[1 + 3*4] = (bottom + top) / (bottom - top);
		this.value[2 + 3*4] = -((near + far) / (far - near));
		
	}
	
	public FloatBuffer toFloatBuffer(){
		return DirectBuffer.createFloats(value, value.length);
	}
	
}