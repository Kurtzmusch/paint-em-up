package game;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import org.lwjgl.glfw.GLFW;

import engine.Engine;
import engine.Texture;
import engine.VertexData;
import game.network.Server;

public class ServerGame implements Runnable {

	private ListIterator<TickerImplementor> tickIterator;
	private LinkedList<TickerImplementor> tickList;
	private LinkedList<TickerImplementor> tickRemoveList;
	private LinkedList<TickerImplementor> tickAddList;
	
	public Server server;
	
	private volatile boolean shouldStop;
	
	private String matchType, map;
	private int port;
	
	public ServerGame ( String aMatchType, String aMap, int aPort ) {
		matchType = aMatchType;
		map = aMap;
		port = aPort;
		new Thread( this, "ServerGame" ).start();
		
	}
	
	public void enableTicker( TickerImplementor thing ) {
		tickAddList.add( thing );
	}
	public void disableTicker( TickerImplementor thing ) {
		tickRemoveList.add( thing );
	}
	@Override
	public void run() { // this does the same as main()
		setupServer();
		serverGameLoop();
	}
	
	public void setupServer() {
		tickList = new LinkedList<TickerImplementor>();
		tickAddList = new LinkedList<TickerImplementor>();
		tickRemoveList = new LinkedList<TickerImplementor>();
		server = new Server( this, matchType, map, port );
	}
	

	private void preLogic() {
		// --------	
		
		
		for(  TickerImplementor implementor : tickAddList ) {
			implementor.enabled = true;
		}
		
		for( TickerImplementor implementor : tickRemoveList ) {
			implementor.enabled = false;
		}		
		tickRemoveList.clear();
		
		// --------	
		
		// --------	
		int lastPriority;
		int currentPriority;
		
		// --------
		
		lastPriority = Integer.MIN_VALUE;
		currentPriority = Integer.MIN_VALUE;
		Collections.sort(tickAddList);
		
		tickIterator = tickList.listIterator();
		
		TickerImplementor ticker;

		while( tickIterator.hasNext() ) {
			
			ticker = tickIterator.next();
			
			currentPriority = ticker.priority;
			if( currentPriority > lastPriority ) {
				tickIterator.previous();
				while(true) {
					if( !tickAddList.isEmpty() ) {
						if( tickAddList.getFirst().priority <= currentPriority ) {
							tickIterator.add(tickAddList.pop());
						}
						else {
							break;
						}
					}
					else {
						break;
					}
				}
			}
			
			lastPriority = currentPriority;
		}
		
		while( !tickAddList.isEmpty() ) {
			tickList.add( tickAddList.pop() );
		}
		
		// --------
		
	}
	
	private void logic() {
		
		preLogic();
		
		server.tick();
		
		tickIterator = tickList.listIterator();
		
		while( tickIterator.hasNext() ) {
			
			TickerImplementor ticker = tickIterator.next();
			
			if( ticker.enabled ) {
				ticker.tick();
			}
			else {
				tickIterator.remove();
			}
			
		}
		
	}
	
	private void serverGameLoop() {
		long acumulatedLogicTime = 0;
		long acumulatedRenderTime = 0;
		int counter = 0;
		
		double MAXTIME3 = 1d/60d;
		double frameTime = 0;
		double nextSwap = 0;
		double overLooped = 0;
	
		while( !shouldStop ) {
		
			frameTime = GLFW.glfwGetTime();
			
			
			long sleepTime = 0;
			long logicTime = 0;
			
			long accumulator = 0;
			long timeSlept = 0;
			
			counter ++;
			
			if( counter == 600 ) {
				float avgLogic = (float) (acumulatedLogicTime)/600f;
				//System.out.println("SERVER: Avarage Logic Time: " + avgLogic + "ms");
				//Runtime.getRuntime().gc();
				acumulatedLogicTime = 0;
				acumulatedRenderTime = 0;
				counter = 0;
				//System.out.println("SERVER: tickList: " + tickList.size());
			}
			
				logicTime = System.currentTimeMillis();
				logic();
				logicTime = System.currentTimeMillis() - logicTime;
				acumulatedLogicTime+=logicTime;
				
				//Thread.yield();
			while( GLFW.glfwGetTime() < nextSwap ) {} // sync myself
			overLooped = GLFW.glfwGetTime() - nextSwap;
			nextSwap = GLFW.glfwGetTime() + MAXTIME3;
			nextSwap -= overLooped;
			frameTime = GLFW.glfwGetTime() - frameTime;
			
			//sleepTime = 16-frameTime-accumulator;
			/*
			if( sleepTime > 0 ){
				
				timeSlept = System.currentTimeMillis();
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				timeSlept = System.currentTimeMillis() - timeSlept;
				//System.err.println(timeSlept + "/" + sleepTime);
				if(timeSlept > sleepTime){
					//System.err.println("Slept " +(timeSlept-sleepTime)+"ms longer");
					accumulator += timeSlept-sleepTime;
					//System.err.println("Next frame penalty: " +accumulator+ "");
				}
			
			}
			else {
				accumulator += (-(sleepTime));
				//System.err.println("Next frame penalty: " +accumulator+ "");
			}*/

		}
		
		endServer();
		
	}
	
	public void stopServerGame() {
		shouldStop = true;
		// TODO halt in a while loop to garantee all threads have stoped
	}
	
	public void endServer() {
		// TODO Auto-generated method stub
		server.destroy();
	}
	
}
