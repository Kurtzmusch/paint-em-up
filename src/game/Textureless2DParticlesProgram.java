package game;

import java.util.HashMap;
import java.util.LinkedList;

import engine.Program;
import engine.util.ShaderData;

public class Textureless2DParticlesProgram extends Program {

	public Textureless2DParticlesProgram() {
		
		ShaderData vs = new ShaderData();
		ShaderData fs = new ShaderData();
		
		vs.load("/game/shader/textureless2DParticlesVertexShader");
		fs.load("/game/shader/textureless2DParticlesFragmentShader");
		
		HashMap<String,String> shaderTypeSource = new HashMap<String,String>();
		HashMap<Integer,String> attributeLocationName = new HashMap<Integer,String>();
		LinkedList<String> uniformName = new LinkedList<String>();
		
		shaderTypeSource.put("vertex", vs.source);
		shaderTypeSource.put("fragment", fs.source);
		
		attributeLocationName.put(new Integer(0), "vPos");
		attributeLocationName.put(new Integer(1), "pPos");
		attributeLocationName.put(new Integer(2), "col");
		
		uniformName.add("projectionMatrix");
		uniformName.add("viewMatrix");
		//uniformName.add("transformMatrix");
		//uniformName.add("whiteness");
		//uniformName.add("color");
		//uniformName.add("sample");
		//uniformName.add("playerX");
		//uniformName.add("playerY");
		
		super.sendGPU(shaderTypeSource, attributeLocationName, uniformName);
	}
	
}
