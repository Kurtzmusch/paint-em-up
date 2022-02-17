
package game;

public class EuclideanMath {
	
	public static final double circleDividedBy8 = Math.PI/4d;
	public static final double circleDividedBy4 = Math.PI/2d;
	public static final double circleDividedBy2 = Math.PI;
	
	
	public static float D2LineToAngle( float ax1, float ay1, float ax2, float ay2 ) {
		float lineDeltaX = ax2 - ax1;
		float lineDeltaY = ay2 - ay1;
		float a = lineDeltaY/lineDeltaX; // slope
		float angle = (float)( Math.atan(a) );
		
		// if B is to the left of A
		if( ax2 < ax1 ) {
			angle += Math.PI;
		}
		else {
			if (angle < 0) {
				angle += 2*Math.PI;
			}
		}
		
		return angle;
	}
	
	
	public static float lineIntersectCircleY( float lineX1, float lineY1, float lineX2, float lineY2, 
	float circleX, float circleY, float circleRadius ) {
	
		float distanceCircleToOrigin = distanceInfiniteLinePoint(lineX1, lineY1, lineX2, lineY2, circleX, circleY);
		float hipotenuse = circleRadius;
		float angleCircleToOrigin = (float) (D2LineToAngle(lineX1, lineY1, lineX2, lineY2) - Math.PI/2);
		float angleOriginToIntersection = (float) (D2LineToAngle(lineX1, lineY1, lineX2, lineY2) - Math.PI);
		if( angleCircleToOrigin < 0 ) { angleCircleToOrigin = (float) (Math.PI*2 + angleCircleToOrigin); }
		if( angleOriginToIntersection < 0 ) { angleOriginToIntersection = (float) (Math.PI*2 + angleOriginToIntersection); }
		
		float a, b; // y = ax + b
		
		float lineDeltaX = lineX2 - lineX1;
		float lineDeltaY = lineY2 - lineY1;
		
		a = lineDeltaY/lineDeltaX;
		b = lineY1 - (a*lineX1);
		
		// aPointy = pA*aPointx + pB
		float perpendicularA = -(1/a);
		float perpendicularB = circleY- ( perpendicularA*circleX );
	
		// a*x + b = pA*x + pB
		// (a-pA)*x + b = pB
		// (a-Pa)*x = pb-b
		// x = 
		
		float intersectionX = (perpendicularB-b)/(a-perpendicularA);
		float intersectionY = (intersectionX*perpendicularA) + perpendicularB;
		
		float originX = intersectionX;
		float originY = intersectionY;
		
		float distanceOriginToIntersection = (float) Math.sqrt( (Math.pow( hipotenuse, 2 ) - Math.pow(distanceCircleToOrigin, 2)) );
		
		
		return originY + rotatedYComponent(distanceOriginToIntersection, 0, angleOriginToIntersection);
	}
	
	public static float lineIntersectCircleX( float lineX1, float lineY1, float lineX2, float lineY2, 
	float circleX, float circleY, float circleRadius ) {
	
		float distanceCircleToOrigin = distanceInfiniteLinePoint(lineX1, lineY1, lineX2, lineY2, circleX, circleY);
		float hipotenuse = circleRadius;
		
		float angleCircleToOrigin = (float) (D2LineToAngle(lineX1, lineY1, lineX2, lineY2) - Math.PI/2);
		// not always -90dgrees so i should calculate intersection
		
		float a, b; // y = ax + b
		
		float lineDeltaX = lineX2 - lineX1;
		float lineDeltaY = lineY2 - lineY1;
		
		a = lineDeltaY/lineDeltaX;
		b = lineY1 - (a*lineX1);
		
		// aPointy = pA*aPointx + pB
		float perpendicularA = -(1/a);
		float perpendicularB = circleY- ( perpendicularA*circleX );
	
		// a*x + b = pA*x + pB
		// (a-pA)*x + b = pB
		// (a-Pa)*x = pb-b
		// x = 
		
		float intersectionX = (perpendicularB-b)/(a-perpendicularA);
		float intersectionY = (intersectionX*perpendicularA) + perpendicularB;
		
		
		float angleOriginToIntersection = (float) (D2LineToAngle(lineX1, lineY1, lineX2, lineY2) - Math.PI);
		if( angleCircleToOrigin < 0 ) { angleCircleToOrigin = (float) (Math.PI*2 + angleCircleToOrigin); }
		if( angleOriginToIntersection < 0 ) { angleOriginToIntersection = (float) (Math.PI*2 + angleOriginToIntersection); }
		
		float originX = intersectionX;
		float originY = intersectionY;
		
		float distanceOriginToIntersection = (float) Math.sqrt( (Math.pow( hipotenuse, 2 ) - Math.pow(distanceCircleToOrigin, 2)) );
		
		
		return originX + rotatedXComponent(distanceOriginToIntersection, 0, angleOriginToIntersection);
	}
	
	public static float rotatedXComponent( float aX, float aY, double aAngleInRadians ) {
		float x = (float) (aX * Math.cos( aAngleInRadians ) - aY * Math.sin( aAngleInRadians ));
		return x;
	}
	public static float rotatedYComponent( float aX, float aY, double aAngleInRadians ) {
		float y = (float) (aX * Math.sin( aAngleInRadians ) + aY * Math.cos( aAngleInRadians ));
		return y;
	}
	
	/**
	 * calculates the y component of the intersection between line defined by ax1, ay1, ax2, ay2
	 * with the vertical line at ax3
	 * @return float y component
	 */
	public static float lineIntersectVerticalYComponent( float ax1, float ay1, float ax2, float ay2, float ax3 ) {
		
		float a, b; // y = ax + b
		float lineDeltaX = ax2 - ax1;
		float lineDeltaY = ay2 - ay1;
		a = lineDeltaY/lineDeltaX; // slope
		b = ay1 - (a*ax1);
		
		return a*ax3 + b;
	}
	
	public static float lineIntersectHorizontalXComponent( float ax1, float ay1, float ax2, float ay2, float ay3 ) {
		
		float a, b; // y = ax + b
		float lineDeltaX = ax2 - ax1;
		float lineDeltaY = ay2 - ay1;
		a = lineDeltaY/lineDeltaX; // slope
		b = ay1 - (a*ax1);
		
		return (ay3-b)/a;
	}
	
	public static float distanceInfiniteLinePoint( float aLinex1, float aLiney1, float aLinex2,
	float aLiney2, float aPointx, float aPointy ) {
		
		float a, b; // y = ax + b
		
		float lineDeltaX = aLinex2 - aLinex1;
		float lineDeltaY = aLiney2 - aLiney1;
		if( lineDeltaX == 0 ) {
			// line is vertical just compare x
			float intersectionY = aPointy;
			float intersectionX = aLinex1;

			return distancePointPoint( intersectionX, intersectionY, aPointx, aPointy );		}
		else {
			if( lineDeltaY == 0 ) {
				// line is horizontal
				float intersectionY = aLiney1;
				float intersectionX = aPointx;
				
				return distancePointPoint( intersectionX, intersectionY, aPointx, aPointy );

			}
		}
		a = lineDeltaY/lineDeltaX;
		b = aLiney1 - (a*aLinex1);
		
		// aPointy = pA*aPointx + pB
		float perpendicularA = -(1/a);
		float perpendicularB = aPointy - ( perpendicularA*aPointx );
	
		// a*x + b = pA*x + pB
		// (a-pA)*x + b = pB
		// (a-Pa)*x = pb-b
		// x = 
		
		float intersectionX = (perpendicularB-b)/(a-perpendicularA);
		float intersectionY = (intersectionX*perpendicularA) + perpendicularB;
		
		// DONT check if intersection is part of the line
		return distancePointPoint( intersectionX, intersectionY, aPointx, aPointy );
	}
	
	public static float distanceLinePoint( float aLinex1, float aLiney1, float aLinex2,
	float aLiney2, float aPointx, float aPointy ) {
		
		float a, b; // y = ax + b
		
		float lineDeltaX = aLinex2 - aLinex1;
		float lineDeltaY = aLiney2 - aLiney1;
		if( lineDeltaX == 0 ) {
			// line is vertical just compare x
			float intersectionY = aPointy;
			float intersectionX = aLinex1;
			
			if( intersectionY < bigger(aLiney1, aLiney2) ) {
				if( intersectionY > smaller(aLiney1, aLiney2) ) {
					return distancePointPoint( intersectionX, intersectionY, aPointx, aPointy );
				}
			}
			return smaller( distancePointPoint(aPointx, aPointy, aLinex1, aLiney1), distancePointPoint(aPointx, aPointy, aLinex2, aLiney2) ); 
		}
		else {
			if( lineDeltaY == 0 ) {
				// line is horizontal
				float intersectionY = aLiney1;
				float intersectionX = aPointx;
				
				if( intersectionX < bigger(aLinex1, aLinex2) ) {
					if( intersectionX > smaller(aLinex1, aLinex2) ) {
						return distancePointPoint( intersectionX, intersectionY, aPointx, aPointy );
					}
				}
				return smaller( distancePointPoint(aPointx, aPointy, aLinex1, aLiney1), distancePointPoint(aPointx, aPointy, aLinex2, aLiney2) ); 

			}
		}
		a = lineDeltaY/lineDeltaX;
		b = aLiney1 - (a*aLinex1);
		
		// aPointy = pA*aPointx + pB
		float perpendicularA = -(1/a);
		float perpendicularB = aPointy - ( perpendicularA*aPointx );
	
		// a*x + b = pA*x + pB
		// (a-pA)*x + b = pB
		// (a-Pa)*x = pb-b
		// x = 
		
		float intersectionX = (perpendicularB-b)/(a-perpendicularA);
		float intersectionY = (intersectionX*perpendicularA) + perpendicularB;
		
		// check if intersection is part of the line
		
		if ( intersectionX > smaller( aLinex1, aLinex2 ) ) {
			if( intersectionX < bigger( aLinex1, aLinex2 ) ) {
				if( intersectionY > smaller( aLiney1, aLiney2 ) ) {
					if( intersectionY < bigger( aLiney1, aLiney2 ) ) {
						return distancePointPoint( intersectionX, intersectionY, aPointx, aPointy );
					}
				}
			}
		}
		
		return smaller( distancePointPoint(aPointx, aPointy, aLinex1, aLiney1), distancePointPoint(aPointx, aPointy, aLinex2, aLiney2) );
	
	}
	
	public static float smaller( float a1, float a2 ) {
		if( a1<a2 ) return a1;
		if( a2<a1 ) return a2;
		return a1;
	}
	public static float bigger( float a1, float a2 ) {
		if( a1>a2 ) return a1;
		if( a2>a1 ) return a2;
		return a1;
	}
	
	public static float distancePointPoint( float ax1, float ay1, float ax2, float ay2 ) {
		
		float deltaX = ax2 - ax1;
		float deltaY = ay2 - ay1;
		
		return (float) ( Math.sqrt( (double)(Math.pow(deltaX, 2)+Math.pow(deltaY, 2)) ) );
		
	}
	
}
