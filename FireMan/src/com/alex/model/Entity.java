package com.alex.model;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class Entity {

	private float SPEED = 1.5f; // units per second
	private int jumpSpeed = 6;
	private Vector2 JUMP_VECTOR = new Vector2(0,jumpSpeed);
	private static float SIZE = 0.5f; // half a unit

	private Vector2 position = new Vector2();
	private Vector2 acceleration = new Vector2();
	private Vector2 velocity = new Vector2();
	private Rectangle bounds = new Rectangle();
	private float stateTime = 0;
	
	private boolean canJump = false;
	private int numFootContacts = 0;
	private int numRightSideContacts = 0;
	private int numLeftSideContacts = 0;
	
	private State state = State.IDLE;

	boolean facingLeft = true;
	
	private Circle boundingCircle = new Circle();

	Body entityBody;
	Fixture entityFixture;

	public enum State {
		IDLE, WALKING, JUMPING, DYING, ATTACKING
	}
	
	public Entity(Vector2 position) {
		this.position = position;
		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = getSize();
		bounds.width = getSize();
		velocity.x = SPEED;
		boundingCircle.x = position.x + getSize()/2;
		boundingCircle.y = position.y;
		boundingCircle.radius = 50;
	}
	
	public void update(float delta) {
		setStateTime(getStateTime()+delta);
		setPosition(getBody().getPosition());
		setBounds(getPosition());
		boundingCircle.x = position.x + getSize()/2;
		boundingCircle.y = position.y;
	}
	
	// Begin massive list of setters/getters ***********************************
	public int getJumpBaseSpeed() {
		return jumpSpeed;
	}
	
	public void setNumFootContacts(int num) {
		numFootContacts += num;
	}
	
	public int getNumFootContacts() {
		return numFootContacts;
	}
	
	public void setNumLeftSideContacts(int num) {
		numLeftSideContacts += num;
	}
	
	public int getNumLeftSideContacts() {
		return numLeftSideContacts;
	}
	
	public void setNumRightSideContacts(int num) {
		numRightSideContacts += num;
	}
	
	public int getNumRightSideContacts() {
		return numRightSideContacts;
	}
	
	public void canJump(boolean bool) {
		canJump = bool;
	}
	public boolean canJump() {
		return canJump;
	}
	
	public void setBoundingCircle(float x, float y) {
		boundingCircle.x = x;
		boundingCircle.y = y;
	}
	
	public Circle getBoundingCircle() {
		return boundingCircle;
	}
	
	public void setBounds(Vector2 position) {
		bounds.x = position.x;
		bounds.y = position.y;
	}
	
	public void setBoundsWidth(float width) {
		bounds.width = width;
	}
	
	public void setBoundsHeight(float height) {
		bounds.height = height;
	}
	
	public Rectangle getBounds() {
		return bounds;
	}

	public Vector2 getPosition() {
		return position;
	}
	
	public void setFixture(Fixture fixture) {
		entityFixture = fixture;
		setBody(fixture.getBody());
	}
	
	public Fixture getFixture() {
		return entityFixture;
	}

	public void setBody(Body body) {
		entityBody = body;
	}

	public Body getBody() {
		return entityBody;
	}
	
	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public Vector2 getAcceleration() {
		return acceleration;
	}

	public void setSpeed(float speed) {
		SPEED = speed;
	}
	
	public float getSpeed() {
		return SPEED;
	}

	public void setFacingLeft(boolean left) {
		facingLeft = left;
	}

	public boolean isFacingLeft() {
		return facingLeft;
	}

	public void setStateTime(float time) {
		stateTime = time;
	}
	
	public float getStateTime() {
		return stateTime;
	}

	public void setPosition(Vector2 position2) {
		position = position2;
	}
	
	public void setSize(float size) {
		SIZE = size;
	}

	public static float getSize() {
		return SIZE;
	}
	
	public void setJumpVelocity(Vector2 velocity) {
		JUMP_VECTOR = velocity;
	}
	
	public Vector2 getJumpVelocity() {
		return JUMP_VECTOR;
	}
	
	public void setState(State newState) {
		this.state = newState;
	}

	public State getState() {
		return state;
	}
}
