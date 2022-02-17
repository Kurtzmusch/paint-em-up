package game;

import java.util.HashMap;
import java.util.LinkedList;

import engine.Program;
import engine.util.ShaderData;

public class LinesDynamicProgram extends Program {

	public LinesDynamicProgram(){
		
		ShaderData vs = new ShaderData();
		ShaderData fs = new ShaderData();
		
		vs.load("/game/shader/linesDynamicVertexShader");
		fs.load("/game/shader/linesDynamicFragmentShader");
		
		HashMap<String,String> shaderTypeSource = new HashMap<String,String>();
		HashMap<Integer,String> attributeLocationName = new HashMap<Integer,String>();
		LinkedList<String> uniformName = new LinkedList<String>();
		
		shaderTypeSource.put("vertex", vs.source);
		shaderTypeSource.put("fragment", fs.source);
		
		attributeLocationName.put(new Integer(0), "pos");
		attributeLocationName.put(new Integer(1), "col");
		
		uniformName.add("projectionMatrix");
		uniformName.add("viewMatrix");
		
		super.sendGPU(shaderTypeSource, attributeLocationName, uniformName);
		
	}
	
}