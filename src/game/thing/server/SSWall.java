
package game.thing.server;

import java.util.LinkedList;

import game.EuclideanMath;
import game.MapThing;

public class SSWall extends MapThing {
	
	public static LinkedList<SSWall> instanceList = new LinkedList<SSWall>();
	
	public SSWall( int x, int y ) {
		super(x, y);
		
		instanceList.add(this);
	}
	
	
	public boolean collisionCircle( float aX, float aY, float aRadius ) {
		
		float size = 32;
		float leftEdge = x; float rightEdge = x+size;
		float bottomEdge = y; float upperEdge = y+size;
		
		if( aX > leftEdge && aY > bottomEdge ) {
			if( aX < rightEdge && aY < upperEdge ) {
				return true;
			}
		}
		
		if( (EuclideanMath.distanceLinePoint(x, y, x+size, y, aX, aY) < aRadius )
		||	(EuclideanMath.distanceLinePoint(x, y, x, y+size, aX, aY) < aRadius )
		||	(EuclideanMath.distanceLinePoint(x+size, y+size, x, y+size, aX, aY) < aRadius )
		||	(EuclideanMath.distanceLinePoint(x+size, y+size, x+size, y, aX, aY) < aRadius ) ) {
			return true;
		}
		
		return false;
	}
	
	
	public boolean collisionLine( float lineX1, float lineY1, float lineX2, float lineY2  ) {
		boolean collided = false;
		float size = 32;
		float leftEdge = x; float rightEdge = x+size;
		float bottomEdge = y; float upperEdge = y+size;
		
		// only calculate nodes that are within line range
		if( (leftEdge < EuclideanMath.bigger( lineX1, lineX2 ))
		&&(rightEdge > EuclideanMath.smaller( lineX1, lineX2 ))) {
			if( (bottomEdge < EuclideanMath.bigger( lineY1, lineY2 ))
			&&(upperEdge > EuclideanMath.smaller( lineY1, lineY2 ))) {
				float leftEdgeY = EuclideanMath.lineIntersectVerticalYComponent( lineX1, lineY1, lineX2, lineY2, leftEdge );
				if( (leftEdgeY > bottomEdge)&&(leftEdgeY < upperEdge ) ) {
					return true;
				}
				float rightEdgeY = 	EuclideanMath.lineIntersectVerticalYComponent(lineX1, lineY1, lineX2, lineY2, rightEdge );
				if( (rightEdgeY > bottomEdge)&&(rightEdgeY < upperEdge ) ) {
					return true;
				}
				float bottomEdgeX = EuclideanMath.lineIntersectHorizontalXComponent(lineX1, lineY1, lineX2, lineY2, bottomEdge );;
				if( (bottomEdgeX > leftEdge)&&(bottomEdgeX < rightEdge) ) {
					return true;
				}
				float upperEdgeX = EuclideanMath.lineIntersectHorizontalXComponent(lineX1, lineY1, lineX2, lineY2, upperEdge );;
				if( (upperEdgeX > leftEdge)&&(upperEdgeX < rightEdge) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void destroy() {
		// TODO
		instanceList.remove(this);
	}

}
