package com.tutorial.controller;

import java.util.HashMap;
import java.util.Map;

import com.alex.behavior.Behaviors;
import com.alex.model.FireMan;
import com.alex.model.Civilian;
import com.alex.model.Civilian.Behavior;
import com.alex.model.Level;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.hatstick.fireman.WorldRenderer;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;

/** Takes all the enemies in our Level and gives them behaviors **/
public class CivilianController extends Behaviors {

	enum Keys {
		LEFT, RIGHT, JUMP, FIRE
	}

	private static final float ACCELERATION     = 1.5f;
	private static final float MAX_JUMP_SPEED   = 6f;

	private Civilian     enemy;
	private boolean jumpingPressed;
	private Box2DPhysicsWorld physicsWorld;
	private Level level;
	private WorldRenderer renderer;

	public CivilianController(Level level, Box2DPhysicsWorld physicsWorld, WorldRenderer renderer) {
		//	this.enemy = world.getEnemies();
		this.level = level;
		this.physicsWorld = physicsWorld;
		this.renderer = renderer;
	}

	/** The main update method **/
	public void update(float delta) {

		for( Civilian enemy : level.getEnemies().keySet()) {
			enemy.update(delta);
			
			enemy.setBoundingCircle((enemy.getPosition().x+enemy.getSize()/2)*renderer.getPpuX()+228-renderer.getCamera().position.x*renderer.getPpuX(), 
					enemy.getPosition().y*renderer.getPpuY());
			
			if (enemy.getBehavior() == Behavior.WANDER)
				wander(physicsWorld, enemy);
			else if (enemy.getBehavior() == Behavior.WANDER) {
				chase(physicsWorld, enemy, level.getBob());
			}
			else smartChase(physicsWorld, enemy, level.getBob());
		}
	}

	/** Change Bob's state and parameters based on input controls **/
	private boolean processInput() {
		return true;
	}
}
