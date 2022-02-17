package game.controller;

import java.util.LinkedList;

import engine.Program;
import engine.StreamedVertexData;
import engine.VertexData;
import game.DynamicDrawerImplementor;
import game.Game;

public class ParticleGroup {

	public DynamicDrawerImplementor dynamicDrawer;
	
	public LinkedList<StreamedVertexData> vaoList = new LinkedList<StreamedVertexData>();
	
	public Program program;
	
	public ParticleGroup() {
		dynamicDrawer = new DynamicDrawerImplementor( 7 ) {
			public void dynamicDraw( Program program ) {
				ParticleGroup.this.dynamicDraw( program );
			}
		};
		Game.enableDynamicDrawer( dynamicDrawer );
	}
	
	public ParticleGroup( LinkedList<StreamedVertexData> aVAOList, Program aProgram ) {
		
		program = aProgram;
		vaoList = aVAOList;
		
		dynamicDrawer = new DynamicDrawerImplementor( 7 ) {
			public void dynamicDraw( Program program ) {
				ParticleGroup.this.dynamicDraw( program );
			}
		};
		
	}
	
	public void addVAO( StreamedVertexData aVAO ) {
		vaoList.add( aVAO );
	}

	public void dynamicDraw( Program aProgram ) {
		
		program.enable();
		
		program.loadMatrix("projectionMatrix", Game.camera.projectionMatrix);
		program.loadMatrix("viewMatrix", Game.camera.viewMatrix);
		
		for( StreamedVertexData vao : vaoList ) {
			vao.instancedDraw();
			//vao.draw();
		}
		
		aProgram.enable();
		
		program.loadMatrix("projectionMatrix", Game.camera.projectionMatrix);
		program.loadMatrix("viewMatrix", Game.camera.viewMatrix);
		
	}
	
	
	
}
