package engine;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import engine.util.Matrix4x4f;

public class Program {

	public HashMap<String,Integer> uniformNameLocation;
	public LinkedList<Integer> shaderID;
	
	private int programID;
	
	public void enable(){
		GL20.glUseProgram(programID);
	}
	
	public void disable(){
		GL20.glUseProgram(0);
	}
	
	public void loadVec4f( String name, float[] vec4 ) {
		GL20.glUniform4fv( uniformNameLocation.get(name).intValue(), vec4 );
	}
	public void loadMatrix(String name, Matrix4x4f m){
		FloatBuffer mbuffer = m.toFloatBuffer();
		
		GL20.glUniformMatrix4fv(uniformNameLocation.get(name).intValue(), false, mbuffer);
	}
	protected int getUniformLocation( String varName ){
		return GL20.glGetUniformLocation(programID, varName);
	}

	public void loadFloat( String name, float f ){
		GL20.glUniform1f(uniformNameLocation.get(name).intValue(), f);
	}

	public void prepareShader( String source, int type ){
				
		shaderID.add( new Integer(GL20.glCreateShader(type)) );
		GL20.glShaderSource(shaderID.getLast(), source);
		GL20.glCompileShader(shaderID.getLast());
		if(GL20.glGetShaderi(shaderID.getLast(),GL20.GL_COMPILE_STATUS)==GL11.GL_FALSE){
			System.out.println(GL20.glGetShaderInfoLog(shaderID.getLast(), 500));
			System.out.println("Could not compile shader!" + source);
			System.exit(-1);
		}

	}
	
	
	public void sendGPU( HashMap<String, String> shaderTypeSource,
						  HashMap<Integer, String> attributeLocationName,
						  LinkedList<String> uniformName ){
		
		uniformNameLocation = new HashMap<String,Integer>();
		shaderID = new LinkedList<Integer>();
		
		for( Entry<String,String> e : shaderTypeSource.entrySet() ){
			if( e.getKey().equals("vertex") ){
				prepareShader( e.getValue(),GL20.GL_VERTEX_SHADER );
			}
			else{
				if( e.getKey().equals("fragment") ){
					prepareShader( e.getValue(),GL20.GL_FRAGMENT_SHADER );
				}
			}
		}
		
		programID = GL20.glCreateProgram();
		
		for( Integer i : shaderID ){
			GL20.glAttachShader(programID, i.intValue());
		}
		
		for( Entry<Integer,String> e : attributeLocationName.entrySet() ){
			GL20.glBindAttribLocation( programID, e.getKey().intValue(), e.getValue() );
		}
		
		GL20.glLinkProgram( programID );
		GL20.glValidateProgram( programID );
		
		for( String name : uniformName ){
			uniformNameLocation.put(name, GL20.glGetUniformLocation(programID, name));
			
		}
		
	}

}