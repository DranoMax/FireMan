package com.tutorial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alex.model.FireMan;
import com.alex.model.FireMan.Tools;
import com.alex.model.Entity.State;
import com.alex.model.Level;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.hatstick.fireman.WorldRenderer;
import com.hatstick.fireman.items.Axe;
import com.hatstick.fireman.items.Grappler;
import com.hatstick.fireman.items.WaterGun;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;
import com.hatstick.fireman.physics.Box2DPhysicsWorld.ModelType;
import com.hatstick.fireman.physics.Box2DPhysicsWorld.UserData;

public class FireManController {

	public enum Keys {
		LEFT, RIGHT, JUMP, FIRE, INTERACT, UP, DOWN
	}

	private static final float ACCELERATION     = 1.5f;
	private static final float MAX_JUMP_SPEED   = 6f;

	private FireMan     bob;
	private boolean jumpingPressed;
	private Box2DPhysicsWorld physicsWorld;
	private Level level;
	private WorldRenderer renderer;
	public Body hitBody;
	private ModelType hitBodyType = null;

	// This int is used to ensure damage/healing happens only once
	// per second
	private float previousHealthCheckTime = 0;

	// This int is used for timing damage from using the push ability
	private float previousSprayCheckTime = 0;

	static Map<Keys, Boolean> keys = new HashMap<FireManController.Keys, Boolean>();
	static {
		keys.put(Keys.LEFT, false);
		keys.put(Keys.RIGHT, false);
		keys.put(Keys.UP, false);
		keys.put(Keys.DOWN, false);
		keys.put(Keys.JUMP, false);
		keys.put(Keys.FIRE, false);
		keys.put(Keys.INTERACT, false);
	};

	public FireManController(Level level, Box2DPhysicsWorld physicsWorld, WorldRenderer renderer) {
		this.level = level;
		this.bob = level.getBob();
		this.physicsWorld = physicsWorld;
		this.renderer = renderer;
	}

	/** The main update method **/
	public void update(float delta) {
		processInput();
		bob.update(delta);		
		bob.setBoundingCircle((bob.getPosition().x+bob.getSize()/2)*renderer.getPpuX()+228-renderer.getCamera().position.x*renderer.getPpuX(), 
				bob.getPosition().y*renderer.getPpuY());

		// Use push ability if active (and take damage to health)
		if (bob.getStateTime()-previousSprayCheckTime > .5) {
			if (bob.isSpraying()) {
				bob.getWaterGun().sprayWater(level, renderer, bob.getMouseTarget().x, bob.getMouseTarget().y);
			}
			previousSprayCheckTime = bob.getStateTime();
		}
	}

	private void handleNotGrappling() {
		bob.setJumpVelocity(new Vector2(0,6)); // Fix our jump in case we've been running up walls
		if (keys.get(Keys.JUMP) && jumpingPressed == false && bob.getNumFootContacts() > 0) {
			jumpingPressed = true;
			bob.setState(State.JUMPING);
			physicsWorld.jumpBob(bob.getJumpVelocity()); 

		}	
		if (bob.getState() == State.JUMPING && bob.getNumFootContacts() > 0) {
			bob.setState(State.IDLE);
		}

		if (keys.get(Keys.LEFT)) {
			// left is pressed
			bob.setFacingLeft(true);
			if (!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.WALKING);
			}
			//bob.getAcceleration().x = -ACCELERATION;
			physicsWorld.moveBob(new Vector2(-ACCELERATION,0));
		} else if (keys.get(Keys.RIGHT)) {
			// left is pressed
			bob.setFacingLeft(false);
			if (!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.WALKING);
			}
			//bob.getAcceleration().x = ACCELERATION;
			physicsWorld.moveBob(new Vector2(ACCELERATION,0));
		} else {
			if (!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.IDLE);
			}
			bob.getAcceleration().x = 0;
		}
	}

	/** Change Bob's state and parameters based on input controls **/
	private boolean processInput() {
		/** Resolving Jumping **/
		/** First, see if he's grappling (this will use velocity.x instead of velocity.y **/
		if(bob.getGrappler().isGrappling() && bob.getNumFootContacts() <= 0) { // Bob not touching ground) {
			bob.getGrappler().handleGrappling(bob, physicsWorld, keys, jumpingPressed);
		}
		else {
			handleNotGrappling();
		}
		return false;
	}

	// ** Key presses and touches **************** //
	public void leftPressed() {
		keys.get(keys.put(Keys.LEFT, true));
	}

	public void rightPressed() {
		keys.get(keys.put(Keys.RIGHT, true));
	}

	public void upPressed() {
		keys.get(keys.put(Keys.UP, true));
	}

	public void downPressed() {
		keys.get(keys.put(Keys.DOWN, true));
	}

	public void jumpPressed() {
		keys.get(keys.put(Keys.JUMP, true));
	}

	// Tools ************************
	public void selectAxe() {
		changeTool();
		bob.setTool(Tools.AXE);
	}

	public void selectWater() {
		changeTool();
		bob.setTool(Tools.WATERGUN);
	}

	public void selectGrappler() {
		changeTool();
		bob.setTool(Tools.GRAPPLER);
	}

	/** Called when changing tools to reset all tools that have persistant effects **/
	public void changeTool() {
		bob.getGrappler().setIsGrappling(false);
	}
	// End Tools ********************

	public void firePressed() {
		keys.get(keys.put(Keys.FIRE, true));
	}

	public void interactPressed() {
		if (physicsWorld.getMouseJoint() != null) {
			physicsWorld.setdestroyMouseJoint();
		}
		//	keys.get(keys.put(Keys.INTERACT, true));
	}

	/** Left click = primary weapon fire **/
	public void primaryPressed(int x, int y) {
		bob.setMouseTarget(x, (int)(renderer.getScreenHeight()-y));
		closestBodyMouse(bob.getMouseTarget());
		if(bob.getCurrentTool() == Tools.WATERGUN) {
			bob.setIsSpraying(true);
		}
		else if(bob.getCurrentTool() == Tools.GRAPPLER) {
			if(hitBody != null && hitBodyType == ModelType.WALL) {
				bob.getGrappler().setIsGrappling(true);
			}
			else bob.getGrappler().setIsGrappling(false);
		}
		else if(bob.getCurrentTool() == Tools.AXE) {
			if(hitBody != null && hitBodyType == ModelType.WALL) 
				bob.getAxe().swingAxe(level, physicsWorld, hitBody, x, y);
		}
	}

	public void primaryMoved(int x, int y) {
		bob.setMouseTarget(x, (int)(renderer.getScreenHeight()-y));
	}

	public void leftReleased() {
		keys.get(keys.put(Keys.LEFT, false));
	}

	public void rightReleased() {
		keys.get(keys.put(Keys.RIGHT, false));
	}

	public void upReleased() {
		keys.get(keys.put(Keys.UP, false));
	}

	public void downReleased() {
		keys.get(keys.put(Keys.DOWN, false));
	}

	public void jumpReleased() {
		keys.get(keys.put(Keys.JUMP, false));
		jumpingPressed = false;
	}

	public void fireReleased() {
		keys.get(keys.put(Keys.FIRE, false));
	}

	public void sprayReleased() {
		bob.setIsSpraying(false);
	}

	/** Gets the closest body between the fireman and the mouse click.
	 *  Useful for things such as grappling/shooting water ect.
	 **/
	private void closestBodyMouse(Vector2 target) {
		target.x = (target.x-240)/renderer.getPpuX()+renderer.getCamera().position.x;
		target.y = target.y/renderer.getPpuY();
		physicsWorld.getWorld().rayCast(callback, bob.getBody().getPosition(), target);
		if(hitBody != null) {
			//		System.out.println(hitBody.getUserData());
		}
	}

	// ** End key presses and touches **************** //
	
	RayCastCallback callback = new RayCastCallback() {
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			UserData data = (UserData) fixture.getUserData();
			/** If we are using grappler, we'll also need to set the location of nearest hit **/
			hitBodyType = null;
			if(bob.getCurrentTool() == Tools.GRAPPLER && data.modelType == ModelType.WALL) {
				bob.getGrappler().setGrapplePoint(point);
				hitBodyType = data.modelType;
			}
			hitBody = fixture.getBody();
			return 0;
		}
	};
}
