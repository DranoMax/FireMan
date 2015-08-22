package com.alex.model;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.hatstick.fireman.items.Axe;
import com.hatstick.fireman.items.Grappler;
import com.hatstick.fireman.items.WaterGun;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;
import com.hatstick.fireman.physics.Box2DPhysicsWorld.ModelType;

/** Our main character: 
 * 
 * Push ability is tied to health.
 * As health goes down, screen maybe
 * acts weird/grows dim/ect until 
 * game over.
 * 
 * Can use push ability to move people
 * away from his objective?  Uses less 
 * health than being next to them.
 * 
 * **/


public class FireMan extends Entity {

	public Vector2 shootVector = new Vector2(-3,0);

	/** This variable keeps track of health **/
	private int mentalHealth = 100;
	private int MAX_HEALTH = 100;
	private boolean closeEnemies = false;
	private Vector2 sprayTarget = new Vector2();
	private boolean isSpraying = false;
	
	/** Tool Objects **/
	private Grappler grappler = new Grappler();
	private WaterGun waterGun = new WaterGun();
	private Axe axe = new Axe();

	
	/** Tool list **/
	private Tools currentTool = Tools.AXE;
	
	public enum Tools {
		WATERGUN, GRAPPLER, AXE
	}

	public FireMan(Vector2 position) {
		super(position);
		setSpeed(4f);
		setSize(0.5f);
		setJumpVelocity(new Vector2(0,5));
	}
	
	public Grappler getGrappler() {
		return grappler;
	}
	
	public WaterGun getWaterGun() {
		return waterGun;
	}
	
	public Axe getAxe() {
		return axe;
	}
	
	public void setTool(Tools tool) {
		currentTool = tool;
	}
	
	public Tools getCurrentTool() {
		return currentTool;
	}

	public boolean areCloseEnemies() {
		return closeEnemies;
	}

	public void setCloseEnemies(boolean close) {
		closeEnemies = close;
	}

	public void update(float delta) {
		super.update(delta);
	}

	public void damageHealth(int damage) {
		mentalHealth -= damage;
		if (mentalHealth < 0) mentalHealth = 0;
	}

	public void heal(int heal) {
		mentalHealth += heal;
		if (mentalHealth > MAX_HEALTH) mentalHealth = MAX_HEALTH;
	}

	public void setHealth(int health) {
		mentalHealth = health;
	}

	public int getHealth() {
		return mentalHealth;
	}
	
	public void setMouseTarget(int x, int y) {
		sprayTarget.x = x;
		sprayTarget.y = y;
	}
	
	public Vector2 getMouseTarget() {
		return sprayTarget;
	}
	
	public boolean isSpraying() {
		return isSpraying;
	}
	
	public void setIsSpraying(boolean bool) {
		isSpraying = bool;
	}
}
