package com.hatstick.fireman.items;

import java.util.Map;

import com.alex.model.Entity.State;
import com.alex.model.FireMan;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;
import com.tutorial.controller.FireManController.Keys;

public class Grappler {

	private Vector2 grapplePoint = new Vector2(0,0);
	private boolean isGrappling = false;

	public void setGrapplePoint(Vector2 point) {
		grapplePoint = point;
	}

	public Vector2 getGrapplePoint() {
		return grapplePoint;
	}

	public void setIsGrappling(boolean set) {
		isGrappling = set;
	}

	public boolean isGrappling() {
		return isGrappling;
	}

	public void handleGrappling(FireMan bob, Box2DPhysicsWorld physicsWorld, Map<Keys, Boolean> keys, boolean jumpingPressed) {
		/** Grapple mechanics **/

		/** Make sure that if above grapple point, gravity will come into effect */
		if (bob.getPosition().y < bob.getGrappler().getGrapplePoint().y + bob.getSize()) {
			/** Need to set the gravity so that Bob 'sticks' to the wall depending on position **/

			/** Bob is now grappling against a wall, we need to flip gravity so that he sticks to wall**/
			if (bob.getPosition().x < bob.getGrappler().getGrapplePoint().x) {
				bob.setJumpVelocity(new Vector2(-bob.getJumpBaseSpeed()/8f,0));
				physicsWorld.moveBob(new Vector2(-physicsWorld.getWorld().getGravity().y,1.5f));
			}
			else if (bob.getPosition().x > bob.getGrappler().getGrapplePoint().x){
				bob.setJumpVelocity(new Vector2(bob.getJumpBaseSpeed()/8f,0));
				physicsWorld.moveBob(new Vector2(physicsWorld.getWorld().getGravity().y,1.5f));
			}

			/** Handle key presses **/
			if (keys.get(Keys.JUMP) && jumpingPressed == false) {
				jumpingPressed = true;
				bob.setState(State.JUMPING);
				System.out.println(bob.getJumpVelocity());
				physicsWorld.jumpBob(bob.getJumpVelocity()); 
			}
			if (keys.get(Keys.UP)) {
				if (bob.getPosition().y <= bob.getGrappler().getGrapplePoint().y) {
					// Up is pressed
					bob.setFacingLeft(true);
					if (!bob.getState().equals(State.JUMPING)) {
						bob.setState(State.WALKING);
					}
					physicsWorld.moveBob(new Vector2(0,bob.getJumpVelocity().y+(-physicsWorld.getWorld().getGravity().y)));
				}
			} else if (keys.get(Keys.DOWN)) {
				// Down is pressed
				bob.setFacingLeft(false);
				if (!bob.getState().equals(State.JUMPING)) {
					bob.setState(State.WALKING);
				}
				physicsWorld.moveBob(new Vector2(new Vector2(0,-bob.getJumpVelocity().y+physicsWorld.getWorld().getGravity().y)));
			} 
		}
	}
}