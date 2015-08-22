package com.hatstick.fireman.physics;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.util.ArrayList;
import java.util.Map;

import com.alex.model.Block;
import com.alex.model.FireMan;
import com.alex.model.Civilian;
import com.alex.model.Item;
import com.alex.model.Level;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.TimeUtils;

/** Base class for all Box2D Testbed tests, all subclasses must implement the createWorld() method.
 * 
 * @author badlogicgames@gmail.com */
public class Box2DPhysicsWorld implements RayCastCallback {

	/** The Explosion Engine **/
	private ExplosionEngine xEngine;
	
	/** the renderer **/
	private Box2DDebugRenderer renderer;

	/** our box2D world **/
	private World world;

	/** a hit body **/
	private Body hitBody = null;

	/** The level that we need to render **/
	private Level level;

	/** Bob that we need to render physics for **/
	private FireMan bob;

	/** our mouse joint **/
	private MouseJoint mouseJoint = null;
	private boolean destroyMousejoint = false;

	/** Used to keep track of id's **/
	private int numEnterPoints = 0;

	/** These two arrays are required for creating/removing bodies between world steps **/
	private ArrayList<JointDef> jointsToCreate = new ArrayList<JointDef>();
	private ArrayList<JointDef> jointsToDelete = new ArrayList<JointDef>();
	private ArrayList<BodyDef> bodiesToCreate = new ArrayList<BodyDef>();
	private ArrayList<Body> bodiesToDelete = new ArrayList<Body>();

	public enum ModelType {
		PLAYER, WALL, ENEMY, ITEM, FOOT_SENSOR, LEFT_SIDE_SENSOR, RIGHT_SIDE_SENSOR
	}

	public Box2DPhysicsWorld(Level level) {
		this.level = level;
		this.bob = level.getBob();
		createPhysicsWorld();
	}

	public void createPhysicsWorld() {
		
		xEngine = new ExplosionEngine();
		
		// create the debug renderer
		renderer = new Box2DDebugRenderer();

		// create the world
		world = new World(new Vector2(0, -3), true);

		bob.setBody(createPhysicsObject(bob.getBounds(), ModelType.PLAYER, -1).getBody());

		// Create our enemy's physical representations
		for( Map.Entry<Civilian, Integer> civilian : level.getEnemies().entrySet()) {
			civilian.getKey().setFixture(createPhysicsObject(civilian.getKey().getBounds(), ModelType.ENEMY, civilian.getValue()));
		}

		// Create our item's physical representations
		for( Map.Entry<Item, Integer> item : level.getItems().entrySet()) {
			item.getKey().setFixture(createPhysicsObject(item.getKey().getBounds(), ModelType.ITEM, item.getValue()));
		}

		// Create our level out of blocks
		for (Block block : level.getBlocks()) {
			createBoxes(block.getBounds());
		}
		
	//	createDestruction();

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact (Contact contact) {

				// Get our UserData object, and check for contacts
				UserData dataA = (UserData) contact.getFixtureA().getUserData();
				UserData dataB = (UserData) contact.getFixtureA().getUserData();

				/** Used for determining if character can jump or not **/
				if(dataA.modelType == ModelType.FOOT_SENSOR) {
					if(dataA.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumFootContacts(1);
						bob.canJump(true);
					}
				}
				if(dataB.modelType == ModelType.FOOT_SENSOR) {
					if(dataB.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumFootContacts(1);
					}
				}
				
				if(dataA.modelType == ModelType.LEFT_SIDE_SENSOR) {
					if(dataA.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumLeftSideContacts(1);
						bob.canJump(true);
					}
				}
				if(dataB.modelType == ModelType.LEFT_SIDE_SENSOR) {
					if(dataB.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumLeftSideContacts(1);
					}
				}
				
				if(dataA.modelType == ModelType.RIGHT_SIDE_SENSOR) {
					if(dataA.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumRightSideContacts(1);
						bob.canJump(true);
					}
				}
				if(dataB.modelType == ModelType.RIGHT_SIDE_SENSOR) {
					if(dataB.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumRightSideContacts(1);
					}
				}

				/** Touching an item (used for picking up items) **/
				if(dataA.modelType == ModelType.PLAYER&& 
						dataB.modelType == ModelType.ITEM ||
						dataB.modelType == ModelType.ITEM && 
						dataA.modelType == ModelType.PLAYER) {


					MouseJointDef def = new MouseJointDef();
					def.bodyA = contact.getFixtureA().getBody();
					def.bodyB = contact.getFixtureB().getBody();
					dataA = (UserData) def.bodyA.getUserData();
					def.collideConnected = false;
					def.target.set(testPoint.x, testPoint.y);

					if(dataA.modelType == ModelType.PLAYER){ 
						def.target.set(def.bodyA.getWorldCenter());
						def.maxForce = 1000.0f * def.bodyA.getMass();
					}
					else {
						def.target.set(def.bodyB.getWorldCenter());
						def.maxForce = 1000.0f * def.bodyB.getMass();
					}
					jointsToCreate.add(def);
				}
			}

			@Override
			public void endContact (Contact contact) {
				// Get our UserData object, and check for contacts
				UserData dataA = (UserData) contact.getFixtureA().getUserData();
				UserData dataB = (UserData) contact.getFixtureA().getUserData();

				/** Used for determining if character can jump or not **/
				if(dataA.modelType == ModelType.FOOT_SENSOR) {
					if(dataA.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumFootContacts(-1);
					}
				}
				if(dataB.modelType == ModelType.FOOT_SENSOR) {
					if(dataB.parentId == -1)  { // PlayerCharacter's foot
						bob.setNumFootContacts(-1);
					}
				}
			}

			@Override
			public void preSolve (Contact contact, Manifold oldManifold) {}

			@Override
			public void postSolve (Contact contact, ContactImpulse impulse) {}
		});
	}

	public World getWorld() {
		return world;
	}

	public void render(OrthographicCamera camera) {
		// update the world with a fixed time step
		world.step(Gdx.app.getGraphics().getDeltaTime()*10, 8, 3);
		if(mouseJoint != null ) mouseJoint.setTarget(bob.getBody().getWorldCenter());


		// Destroy mouseJoint? (drop item)
		if (destroyMousejoint == true) {
			if( !world.isLocked() ) {
				world.destroyJoint(mouseJoint);
				mouseJoint = null;
				destroyMousejoint = false;
			}
		}

		// Delete any bodies up for deletion
		if( !bodiesToDelete.isEmpty() ) {
			// Make sure it is safe to delete!!
			if( !world.isLocked() ) {
				for( Body body : bodiesToDelete ) {
					world.destroyBody(body);
					body.setUserData(null);
					body = null;
				}
				bodiesToDelete.clear(); // Don't forget to clear the null bodies!
			}
		}

		// Create any bodies up for creation
		if( !bodiesToCreate.isEmpty() ) {
			// Make sure it is safe to create!!
			if( !world.isLocked() ) {
				for( BodyDef body : bodiesToCreate ) {
					world.createBody(body);
				}
				bodiesToCreate.clear(); // Don't forget to clear!
			}
		}

		// Create any joints up for creation
		if( !jointsToCreate.isEmpty() ) {
			// Make sure it is safe to create!!
			if( !world.isLocked() ) {
				for( JointDef body : jointsToCreate ) {
					mouseJoint = (MouseJoint) world.createJoint(body);
				}
				jointsToCreate.clear(); // Don't forget to clear!
			}
		}
		// render the world using the debug renderer
		renderer.render(world, camera.combined);
	}

	private Fixture createPhysicsObject(Rectangle bounds, ModelType type, Integer id) {

		UserData userData = new UserData(numEnterPoints, type, null);
		CircleShape objectPoly = new CircleShape();
		objectPoly.setRadius(bounds.width/2);

		BodyDef enemyBodyDef = new BodyDef();
		enemyBodyDef.type = BodyType.DynamicBody;
		enemyBodyDef.position.x = bounds.x;
		enemyBodyDef.position.y = bounds.y;
		Body objectBody = world.createBody(enemyBodyDef);
		FixtureDef objectFixtureDef = new FixtureDef();
		objectFixtureDef.shape = objectPoly;

		//	objectBody.setUserData(userData);
		objectFixtureDef.restitution = .025f;
		Fixture fixture = objectBody.createFixture(objectFixtureDef);
		fixture.setUserData(userData);
		objectBody.setLinearDamping(2f);
		objectBody.setGravityScale(.4f);
		objectBody.setFixedRotation(true);
		objectPoly.dispose();
		numEnterPoints++;

		//add a sensor on the bottom to check if touching ground (for jumping)
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(bounds.width/3, bounds.height/8, new Vector2(0,-bounds.height/2), 0);
		objectFixtureDef = new FixtureDef();
		objectFixtureDef.shape = polygonShape;
		objectFixtureDef.isSensor = true;
		Fixture footSensorFixture = objectBody.createFixture(objectFixtureDef);
		footSensorFixture.setUserData(new UserData(numEnterPoints, ModelType.FOOT_SENSOR, id));

		//add a sensor on left side to check if touching wall (for grappling)
		PolygonShape polygonShape2 = new PolygonShape();
		polygonShape2.setAsBox(bounds.width/8, bounds.height/3, new Vector2(-bounds.width/2,0), 0);
		objectFixtureDef = new FixtureDef();
		objectFixtureDef.shape = polygonShape2;
		objectFixtureDef.isSensor = true;
		Fixture leftSideSensorFixture = objectBody.createFixture(objectFixtureDef);
		leftSideSensorFixture.setUserData(new UserData(numEnterPoints, ModelType.LEFT_SIDE_SENSOR, id));

		//add a sensor on right side to check if touching wall (for grappling)
		polygonShape2.setAsBox(bounds.width/8, bounds.height/3, new Vector2(bounds.width/2,0), 0);
		objectFixtureDef = new FixtureDef();
		objectFixtureDef.shape = polygonShape2;
		objectFixtureDef.isSensor = true;
		Fixture rightSideSensorFixture = objectBody.createFixture(objectFixtureDef);
		rightSideSensorFixture.setUserData(new UserData(numEnterPoints, ModelType.RIGHT_SIDE_SENSOR, id));

		return fixture;
	}
	
	private void createDestruction() {
		UserData userData = new UserData(numEnterPoints, ModelType.WALL, null);
		PolygonShape brickPoly = new PolygonShape();
		brickPoly.setAsBox(0.5f,0.5f);

		BodyDef brickBodyDef = new BodyDef();
		brickBodyDef.type = BodyType.DynamicBody;
		brickBodyDef.position.x = 2;
		brickBodyDef.position.y = 2;
		Body brickBody = world.createBody(brickBodyDef);

		FixtureDef brickFixtureDef = new FixtureDef();
		brickFixtureDef.shape = brickPoly;
		// For now, it seems the best way to tell our renderer what our 
		// object is through userData...
		//brickBody.setUserData(userData);
		Fixture fixture = brickBody.createFixture(brickFixtureDef);
		fixture.setUserData(userData);
		brickPoly.dispose();
		numEnterPoints++;
		
		xEngine.boom(new Vector2(2.1f,2.1f), brickBody, this);
	}

	private void createBoxes(Rectangle bounds) {

		UserData userData = new UserData(numEnterPoints, ModelType.WALL, null);
		PolygonShape brickPoly = new PolygonShape();
		brickPoly.setAsBox(bounds.width/2, bounds.height/2);

		BodyDef brickBodyDef = new BodyDef();
		brickBodyDef.type = BodyType.StaticBody;
		brickBodyDef.position.x = bounds.x;
		brickBodyDef.position.y = bounds.y;
		Body brickBody = world.createBody(brickBodyDef);

		FixtureDef brickFixtureDef = new FixtureDef();
		brickFixtureDef.shape = brickPoly;
		// For now, it seems the best way to tell our renderer what our 
		// object is through userData...
		//brickBody.setUserData(userData);
		Fixture fixture = brickBody.createFixture(brickFixtureDef);
		fixture.setUserData(userData);
		brickPoly.dispose();
		numEnterPoints++;
	
	}

	// Bob-specific methods ************************************************************
	public void moveBob(Vector2 direction) {
		bob.getBody().applyForceToCenter(direction, true);
	}

	public void moveBody(Body body, Vector2 direction) {
		body.applyForceToCenter(direction, true);
	}

	public void jumpBody(Body body, float height) {
		body.applyLinearImpulse(new Vector2(0,height), body.getWorldCenter(), true);
	}

	public void jumpBob(Vector2 jump) {
		bob.getBody().applyLinearImpulse(jump, bob.getBody().getWorldCenter(), true);
	}

	public MouseJoint getMouseJoint() {
		return mouseJoint;
	}

	public void setdestroyMouseJoint() {
		destroyMousejoint = true;
	}
	/** Use this method to create when not on a time slice **/
	public void addBody(BodyDef body) {
		bodiesToCreate.add(body);
	}
	/** Use this method to delete when not on a time slice **/
	public void deleteBody(Body body) {
		bodiesToDelete.add(body);
	}

	// End Bob-specific methods ********************************************************


	/** we instantiate this vector and the callback here so we don't irritate the GC **/
	Vector3 testPoint = new Vector3();
	QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture (Fixture fixture) {
			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};
	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point,
			Vector2 normal, float fraction) {
		UserData data = (UserData) fixture.getUserData();
		if(data.modelType == ModelType.WALL) {
			
		}
		// TODO Auto-generated method stub
		return 0;
	}

	public void dispose () {
		renderer.dispose();
		world.dispose();
		renderer = null;
		world = null;
		hitBody = null;
	}

	public class UserData {
		public int id;
		public ModelType modelType;
		public Integer parentId;
		public UserData(int id, ModelType modelType, Integer parentId) {
			this.id = id;
			this.modelType = modelType;
			this.parentId = parentId;
		}
	}

	public int getNumEnterPoints() {
		return numEnterPoints;
	}
}
