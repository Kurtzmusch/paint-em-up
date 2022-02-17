package game;

import java.util.HashMap;
import java.util.LinkedList;

import engine.Program;
import engine.util.ShaderData;

public class GUIProgram extends Program {

	public GUIProgram() {
		
		ShaderData vs = new ShaderData();
		ShaderData fs = new ShaderData();
		
		vs.load("/game/shader/textVertexShader");
		fs.load("/game/shader/textFragmentShader");
		
		HashMap<String,String> shaderTypeSource = new HashMap<String,String>();
		HashMap<Integer,String> attributeLocationName = new HashMap<Integer,String>();
		LinkedList<String> uniformName = new LinkedList<String>();
		
		shaderTypeSource.put("vertex", vs.source);
		shaderTypeSource.put("fragment", fs.source);
		
		attributeLocationName.put(new Integer(0), "pos");
		attributeLocationName.put(new Integer(1), "txc");
		//attributeLocationName.put(new Integer(2), "nrm");
		
		uniformName.add("projectionMatrix");
		//uniformName.add("viewMatrix");
		uniformName.add("transformMatrix");
		uniformName.add("whiteness");
		uniformName.add("color");
		uniformName.add("sample");
		//uniformName.add("playerX");
		//uniformName.add("playerY");
		
		super.sendGPU(shaderTypeSource, attributeLocationName, uniformName);
	}
	
}
