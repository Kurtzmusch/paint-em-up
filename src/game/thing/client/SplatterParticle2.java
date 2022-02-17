
package game.thing.client;

import java.awt.Color;
import java.util.LinkedList;
import java.util.ListIterator;

import engine.StreamedVertexData;
import game.EuclideanMath;
import game.Game;

public class SplatterParticle2 {
	
	public static int trueMaximumParticles = 32200/(27/3);
	public static int maxParticles = 
	(Integer.parseInt( Game.config.maximumParticles.substring(1) )*2000) / (27/3);
	
	public static StreamedVertexData vertexData = new StreamedVertexData();
	
	public static LinkedList<SplatterParticle2> aliveParticleList = new LinkedList<SplatterParticle2>();
	public static LinkedList<SplatterParticle2> deadParticleList = new LinkedList<SplatterParticle2>();
	public static LinkedList<SplatterParticle2> imortalParticleList = new LinkedList<SplatterParticle2>();
	
	public static float[] vertexPosition = { -.5f, .5f,		.5f, .5f,		-.5f, -.5f,
			.5f, .5f,	-.5f, -.5f,		.5f, -.5f };
	public static float[] position;
	public static byte[] color;
	
	public static int atribArrayPositionLength;
	public static int atribArrayColorLength;
	public static int numberOfParticles;
	
	static {
		for( int i = 0; i < trueMaximumParticles; i ++ ) {
			deadParticleList.add( new SplatterParticle2() );
		}
		
	}
	
	public static void updateMaximum() {
		maxParticles = (Integer.parseInt( Game.config.maximumParticles.substring(1) )*2000)/(27/3);
	}
	
	public static void initialize() {
		
		
		
		position = new float[2*(trueMaximumParticles)];
		color = new byte[3*(trueMaximumParticles)];
		vertexData.addVBO( 0, vertexPosition, 2, false, false, 0 );
		vertexData.addVBO( 1, position, 2, true, false, 1 );
		vertexData.addVBO( 2, color, 3, true, true, 1 );
		
	}
	
	Color pcolor;
	int life, colorID;
	float x, y, velocity, friction, direction, acceleration;
	byte[] instanceColor;
	
	public static void burstParticles( int aColorID, float x, float y, float aDirection )  {
		if(true)return;
		SplatterParticle2 p;
		
		p = deadParticleList.poll();
		if( p == null ) { // pick one from immortals
			p = imortalParticleList.pop();
		}
		imortalParticleList.add(p);
		p.x = x + EuclideanMath.rotatedXComponent(0.5f, 0f, aDirection+Math.PI);
		p.y = y + EuclideanMath.rotatedYComponent(0.5f, 0f, aDirection+Math.PI);
		p.pcolor = Game.colorList.get( aColorID );
		
			p = deadParticleList.poll();
			if( p == null ) { // pick one from immortals
				p = imortalParticleList.pop();
			}
			imortalParticleList.add(p);
			p.x = x + EuclideanMath.rotatedXComponent(0.5f, 0f, aDirection);
			p.y = y + EuclideanMath.rotatedYComponent(0.5f, 0f, aDirection);
			p.pcolor = Game.colorList.get( aColorID );
			
			p = deadParticleList.poll();
			if( p == null ) { // pick one from immortals
				p = imortalParticleList.pop();
			}
			imortalParticleList.add(p);
			p.x = x + EuclideanMath.rotatedXComponent(0.5f, 0f, aDirection);
			p.y = y + EuclideanMath.rotatedYComponent(0.5f, 0f, aDirection);
			p.x += EuclideanMath.rotatedXComponent(1f, 0f, aDirection-Math.PI/2f);
			p.y += EuclideanMath.rotatedYComponent(1f, 0f, aDirection-Math.PI/2f);
			
			p.pcolor = Game.colorList.get( aColorID );
			
			p = deadParticleList.poll();
			if( p == null ) { // pick one from immortals
				p = imortalParticleList.pop();
			}
			imortalParticleList.add(p);
			p.x = x + EuclideanMath.rotatedXComponent(0.5f, 0f, aDirection);
			p.y = y + EuclideanMath.rotatedYComponent(0.5f, 0f, aDirection);
			p.x += EuclideanMath.rotatedXComponent(1f, 0f, aDirection+Math.PI/2f);
			p.y += EuclideanMath.rotatedYComponent(1f, 0f, aDirection+Math.PI/2f);
			
			p.pcolor = Game.colorList.get( aColorID );
		
		
		while( imortalParticleList.size() > maxParticles ) {
			p = imortalParticleList.pop();
			p.life = (int) (Math.random()*60)+60;
			aliveParticleList.add( p );
		}
	}
	
	private SplatterParticle2() {
		
	}
	
	public void tick() {
		if( life > 0 ) { life --; }
	}
	
	public static void staticTick() {
		if(true)return;
		// update this particle-type physics, lists and VAO
		SplatterParticle2 particle;
		ListIterator<SplatterParticle2> imortalIterator;
		ListIterator<SplatterParticle2> aliveIterator;
		imortalIterator = imortalParticleList.listIterator();
		aliveIterator = aliveParticleList.listIterator();
		int pos = 0;
		int col = 0;
		numberOfParticles = 0;
		while( imortalIterator.hasNext() ) {
			particle = imortalIterator.next();
			particle.tick();
			// update attribute array
			position[pos] = particle.x;
			position[pos+1] = particle.y;
			color[col] = (byte) particle.pcolor.getRed(); 
			color[col+1] = (byte) particle.pcolor.getGreen();
			color[col+2] = (byte) particle.pcolor.getBlue();
			pos+=2; col+=3;numberOfParticles++;
		}
		while( aliveIterator.hasNext() ) {
			particle = aliveIterator.next();
			particle.tick();
			// update attribute array
			if( particle.life <= 0 ) {
				aliveIterator.remove();
				deadParticleList.add( particle );
				continue;
			}
			position[pos] = particle.x;
			position[pos+1] = particle.y;
			color[col] = (byte) particle.pcolor.getRed(); 
			color[col+1] = (byte) particle.pcolor.getGreen();
			color[col+2] = (byte) particle.pcolor.getBlue();
			pos+=2; col+=3;numberOfParticles++;
		}
		atribArrayPositionLength = pos;
		atribArrayColorLength = col;
		
		// performance test mode uploads all the array every frame
		vertexData.updateVBO( 1, position, position.length, 2, true, false, 1 );
		vertexData.updateVBO( 2, color, color.length, 3, true, true, 1 );
		//doesnt seem to matter
		/*
		vertexData.updateVBO( 1, position, pos, 2, true, false, 1 );
		vertexData.updateVBO( 2, color, col, 3, true, true, 1 );
		*/
		vertexData.instanceCount = numberOfParticles;
		vertexData.poligonCount = 2;
		
	}
	
}