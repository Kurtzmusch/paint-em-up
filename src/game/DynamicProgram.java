package game;

import java.util.HashMap;
import java.util.LinkedList;

import engine.Program;
import engine.util.ShaderData;

public class DynamicProgram extends Program {

	public DynamicProgram(){
		
		ShaderData vs = new ShaderData();
		ShaderData fs = new ShaderData();
		
		vs.load("/game/shader/dynamicVertexShader");
		fs.load("/game/shader/dynamicFragmentShader");
		
		HashMap<String,String> shaderTypeSource = new HashMap<String,String>();
		HashMap<Integer,String> attributeLocationName = new HashMap<Integer,String>();
		LinkedList<String> uniformName = new LinkedList<String>();
		
		shaderTypeSource.put("vertex", vs.source);
		shaderTypeSource.put("fragment", fs.source);
		
		attributeLocationName.put(new Integer(0), "pos");
		attributeLocationName.put(new Integer(1), "txc");
		
		uniformName.add("projectionMatrix");
		uniformName.add("viewMatrix");
		uniformName.add("transformMatrix");
		
		super.sendGPU(shaderTypeSource, attributeLocationName, uniformName);
		
	}
	
}