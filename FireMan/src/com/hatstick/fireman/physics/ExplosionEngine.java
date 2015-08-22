package com.hatstick.fireman.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.hatstick.fireman.physics.Box2DPhysicsWorld.UserData;

/** Big thank you to http://www.emanueleferonato.com/2012/03/05/breaking-objects-with-box2d-the-realistic-way/
 *  for this one!
 *  **/

public class ExplosionEngine {

	private Box2DPhysicsWorld world;
	private Collections collections;
	private float explosionX = 0;
	private float explosionY = 0;
	private ArrayList<Body> explodingBodies = new ArrayList<Body>();
	private int explosionCuts = 5;
	private int numEnterPoints = 0;
	private ArrayList<Vector2> enterPointsVec = new ArrayList<Vector2>();
	private int worldScale = 32;

	// function to create an explosion
	/** Simply pass the point of impact and the body involved to make it explode **/
	public void boom(Vector2 explosion, Body hitBody, Box2DPhysicsWorld world) {
		this.world = world;
		for (int i = 0; i < world.getNumEnterPoints(); i++) {
			enterPointsVec.add(null);
		}
		double cutAngle = 0;
		explosionX = explosion.x;
		explosionY = explosion.y;
		// storing the exploding bodies in a vector. I need to do it since I do not want other bodies
		// to be affected by the raycast and explode
		explodingBodies.add(hitBody);
		// the explosion begins!
		for (int i = 1; i < explosionCuts; i++) {
			// choosing a random angle
			cutAngle = (Math.random()*Math.PI*2);
			// creating the two points to be used for the raycast, according to the random angle and mouse position
			// also notice how I need to add a little offset (i/10) or Box2D will crash. Probably it's not able to
			// determine raycast on objects whose area is very very close to zero (or zero)
			Vector2 p1 = new Vector2((float)(explosionX+i/10-2000*Math.cos(cutAngle))/worldScale,
					(float)(explosionY-2000*Math.sin(cutAngle))/worldScale);
			Vector2 p2 = new Vector2((float)(explosionX+2000*Math.cos(cutAngle))/worldScale,
					(float)(explosionY+2000*Math.sin(cutAngle))/worldScale);
			world.getWorld().rayCast(intersection, p1, p2);
			world.getWorld().rayCast(intersection, p2, p1);
		}
	}
	RayCastCallback intersection = new RayCastCallback() {
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
	
			if (explodingBodies.indexOf(fixture.getBody()) != -1) {
				UserData spr = (UserData) fixture.getUserData();
				// Throughout this whole code I use only one global vector, and that is enterPointsVec. Why do I need it you ask?
				// Well, the problem is that the world.RayCast() method calls this function only when it sees that a given line gets into the body - it doesnt see when the line gets out of it.
				// I must have 2 intersection points with a body so that it can be sliced, thats why I use world.RayCast() again, but this time from B to A - that way the point, at which BA enters the body is the point at which AB leaves it!
				// For that reason, I use a vector enterPointsVec, where I store the points, at which AB enters the body. And later on, if I see that BA enters a body, which has been entered already by AB, I fire the splitObj() function!
				// I need a unique ID for each body, in order to know where its corresponding enter point is - I store that id in the userData of each body.
					if (enterPointsVec.get(spr.id) != null) {
						// If this body has already had an intersection point, then it now has two intersection points, thus it must be split in two - thats where the splitObj() method comes in.
						splitObject(fixture.getBody(), enterPointsVec.get(spr.id), point.cpy());
					}
					else {
						enterPointsVec.set(spr.id, point);
					}
				}
			
			return 1;
		}
	};

	private void splitObject(Body sliceBody, Vector2 a, Vector2 b) {
		ArrayList<Fixture> origFixture = sliceBody.getFixtureList();
		PolygonShape poly = (PolygonShape) origFixture.get(0).getShape();
		int numVertices = poly.getVertexCount();
		ArrayList<Vector2> verticesVec = new ArrayList<Vector2>();
		Vector2 vertex = new Vector2();
		for (int i = 0; i < numVertices; i++) {
			poly.getVertex(i, vertex);
			verticesVec.add(vertex);
		}
		ArrayList<Vector2> shape1Vertices = new ArrayList<Vector2>();
		ArrayList<Vector2> shape2Vertices = new ArrayList<Vector2>();
		//var origUserData:userData=sliceBody.GetUserData().textureData,origUserDataId:int=origUserData.id,d:Number;
		PolygonShape polyShape = new PolygonShape();
		Body body;
		// First, I destroy the original body and remove its Sprite representation from the childlist.
		
		world.deleteBody(sliceBody);
		//	removeChild(origuserData);
		// The world.RayCast() method returns points in world coordinates, so I use the b2Body.GetLocalPoint() to convert them to local coordinates.;
		a = sliceBody.getLocalPoint(a);
		b = sliceBody.getLocalPoint(b);
		// I use shape1Vertices and shape2Vertices to store the vertices of the two new shapes that are about to be created.
		// Since both point A and B are vertices of the two new shapes, I add them to both vectors.
		shape1Vertices.add(a);
		shape1Vertices.add(b);
		shape2Vertices.add(a);
		shape2Vertices.add(b);
		// I iterate over all vertices of the original body. ;
		// I use the function det() ("det" stands for "determinant") to see on which side of AB each point is standing on. The parameters it needs are the coordinates of 3 points:
		// - if it returns a value >0, then the three points are in clockwise order (the point is under AB)
		// - if it returns a value =0, then the three points lie on the same line (the point is on AB)
		// - if it returns a value <0, then the three points are in counter-clockwise order (the point is above AB).
		float d = 0;
		for (int i = 0; i < numVertices; i++) {
			d = det(a.x, a.y, b.x, b.y, verticesVec.get(i).x, verticesVec.get(i).y);
			if (d>0) {
				shape1Vertices.add(verticesVec.get(i));
			}
			else {
				shape2Vertices.add(verticesVec.get(i));
			}
		}
		// In order to be able to create the two new shapes, I need to have the vertices arranged in clockwise order.
		// I call my custom method, arrangeClockwise(), which takes as a parameter a vector, representing the coordinates of the shape's vertices and returns a new vector, with the same points arranged clockwise.
		shape1Vertices = arrangeClockwise(shape1Vertices);
		shape2Vertices = arrangeClockwise(shape2Vertices);
		// setting the properties of the two newly created shapes
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = sliceBody.getPosition().x;
		bodyDef.position.y = sliceBody.getPosition().y;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = origFixture.get(0).getDensity();
		fixtureDef.friction = origFixture.get(0).getFriction();
		fixtureDef.restitution = origFixture.get(0).getRestitution();
		// creating the first shape, if big enough
		if (getArea(shape1Vertices, shape1Vertices.size()) >= 0.05) {
			Vector2[] face = shape1Vertices.toArray(new Vector2[shape1Vertices.size()]);
			polyShape.set((Vector2[])(shape1Vertices).toArray());
			fixtureDef.shape = polyShape;
			//bodyDef.userData={assetName:"debris",textureData:new userData(origUserDataId,shape1Vertices,origUserData.texture)};
			//addChild(bodyDef.userData.textureData);
			//enterPointsVec[origUserDataId]=null;
			body = world.getWorld().createBody(bodyDef);
			body.setAngularVelocity(sliceBody.getAngle());
			body.createFixture(fixtureDef);
		}
		// creating the second shape, if big enough;
		if (getArea(shape2Vertices, shape2Vertices.size()) >= 0.05) {
			polyShape.set((Vector2[])(shape2Vertices).toArray());
			fixtureDef.shape = polyShape;
			//bodyDef.userData={assetName:"debris",textureData:new userData(origUserDataId,shape2Vertices,origUserData.texture)};
			//addChild(bodyDef.userData.textureData);
			//enterPointsVec[origUserDataId]=null;
			body = world.getWorld().createBody(bodyDef);
			body.setAngularVelocity(sliceBody.getAngle());
			body.createFixture(fixtureDef);
		}
	}

	private ArrayList<Vector2> arrangeClockwise(ArrayList<Vector2> orig) {
		// The algorithm is simple:
		// First, it arranges all given points in ascending order, according to their x-coordinate.
		// Secondly, it takes the leftmost and rightmost points (lets call them C and D), and creates tempVec, where the points arranged in clockwise order will be stored.
		// Then, it iterates over the vertices vector, and uses the det() method I talked about earlier. It starts putting the points above CD from the beginning of the vector, and the points below CD from the end of the vector.
		// That was it!
		Vector2[] vec = new Vector2[orig.size()];
		vec = (Vector2[]) orig.toArray();
		System.out.println(vec.length);
		int n = vec.length;
		float d;
		int i1 = 1;
		int i2 = n-1;
		Vector2[] tempVec = new Vector2[n];
		Vector2 C;
		Vector2 D;
		quickSort(vec, 0, vec.length);
		tempVec[0] = vec[0];
		C = vec[0];
		D = vec[n-1];
		for (int i = 1; i < n-1; i++) {
			d = det(C.x, C.y, D.x, D.y, vec[i].x, vec[i].y);
			if (d<0) {
				tempVec[i1++] = vec[i];
			}
			else {
				tempVec[i2--] = vec[i];
			}
		}
		tempVec[i1] = vec[n-1];
		orig = (ArrayList<Vector2>) Arrays.asList(tempVec);
		return orig;
	}

	private static void swap(Vector2[] array, int x, int y) {
		Vector2 temp = array[x];
		array[x] = array[y];
		array[y] = temp;
	}

	private static int partition(Vector2[] arr, int left, int right) {

		int i = left, j = right;
		int pivot = (int) arr[(left + right) / 2].x;

		while (i <= j) {
			while (arr[i].x < pivot)
				i++;
			while (arr[j].x > pivot)
				j--;
			if (i <= j) {
				swap(arr, i, j);
				i++;
				j--;
			}
		};
		return i;
	}

	private static void quickSort(Vector2[] arr, int left, int right) {

		int index = partition(arr, left, right);
		if (left < index - 1)
			quickSort(arr, left, index - 1);
		if (right > index)
			quickSort(arr, index, right);
	}

	private float det(float x1, float y1, float x2, float y2, float x3, float y3) {
		// This is a function which finds the determinant of a 3x3 matrix.
		// If you studied matrices, you'd know that it returns a positive number if three given points are in clockwise order, negative if they are in anti-clockwise order and zero if they lie on the same line.
		// Another useful thing about determinants is that their absolute value is two times the face of the triangle, formed by the three given points.
		return x1*y2+x2*y3+x3*y1-y1*x2-y2*x3-y3*x1;
	}

	// function to get the area of a shape. I will remove tiny shape to increase performance
	private float getArea(ArrayList<Vector2> vs, int count) {
		float area = 0;
		float p1X = 0;
		float p1Y = 0;
		float inv3 = (float) (1.0/3.0);

		for (int i = 0; i < count; ++i) {
			Vector2 p2 = vs.get(i);
			Vector2 p3 = i + 1 < count?vs.get(i+1):vs.get(0);
			float e1X = p2.x-p1X;
			float e1Y = p2.y-p1Y;
			float e2X = p3.x-p1X;
			float e2Y = p3.y-p1Y;
			float D = (e1X*e2Y - e1Y*e2X);
			float triangleArea = (float) (0.5*D);
			area += triangleArea;
		}
		return area;
	}
}