package com.alex.behavior;

import com.alex.model.FireMan;
import com.alex.model.Civilian;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;
import com.hatstick.fireman.physics.Box2DPhysicsWorld.ModelType;

public class Behaviors {

	private Body hitBody;

	/** model will move along ground until bumping something, then will reverse direction **/
	public void wander(Box2DPhysicsWorld physicsWorld, Civilian enemy) {
		if (enemy.getBody().getLinearVelocity().x == 0) {
			enemy.getVelocity().scl(-1);
		}
		physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity());
	}
	
	/** Model will move towards player character #IF there is line of sight **/
	public void chase(Box2DPhysicsWorld physicsWorld, Civilian enemy, FireMan bob) {

		physicsWorld.getWorld().rayCast(callback, enemy.getBody().getPosition(), bob.getPosition());
		if (hitBody != null && hitBody.getUserData() == ModelType.PLAYER) {
			if (enemy.getPosition().x < bob.getPosition().x) {
				physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity());
			}
			else if (enemy.getPosition().x > bob.getPosition().x) {
				physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity().cpy().scl(-1));
			}
		}
	}
	
	/** As method 'chase', but will also attempt to jump over objects to reach player **/
	public void smartChase(Box2DPhysicsWorld physicsWorld, Civilian enemy, FireMan bob) {

		physicsWorld.getWorld().rayCast(callback, enemy.getBody().getPosition(), bob.getPosition());
			if (enemy.getPosition().x < bob.getPosition().x) {
				
				if (hitBody != null && hitBody.getUserData() == ModelType.WALL && enemy.getBody().getLinearVelocity().y == 0) {
					physicsWorld.jumpBody(enemy.getBody(), 4);
					physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity());
				}
				else {
				physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity());
				}
			}
			else if (enemy.getPosition().x > bob.getPosition().x) {
				if (hitBody != null && hitBody.getUserData() == ModelType.WALL && enemy.getBody().getLinearVelocity().y == 0) {
					physicsWorld.jumpBody(enemy.getBody(), 4);
					physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity());
				}
				else
				physicsWorld.moveBody(enemy.getBody(), enemy.getVelocity().cpy().scl(-1));
			}
	//	}
	}

	RayCastCallback callback = new RayCastCallback() {
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			hitBody = fixture.getBody();
			/*
			if(fixture.getBody().getUserData() == ModelType.PLAYER) {
				return 0;
			}*/
			return fraction;
		}
	};
}
