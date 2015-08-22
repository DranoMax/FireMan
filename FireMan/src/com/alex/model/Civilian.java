package com.alex.model;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Civilian extends Entity {

	public enum Behavior {
		WANDER, CHASE, SMARTCHASE
	}

	private Behavior behavior = Behavior.WANDER;

	public Civilian(Vector2 position, Behavior behavior) {
		super(position);
		this.behavior = behavior;
		setSpeed(2f);
		setSize(0.5f);
		setBoundsHeight(getSize());
		setBoundsWidth(getSize());
		setJumpVelocity(new Vector2(0,1));
	}

	public void update(float delta) {
		super.update(delta);
		if (getBody().getLinearVelocity().x != 0) {
			if(getBody().getLinearVelocity().x > 0) facingLeft = false;
			else facingLeft = true;
			setState(State.WALKING);
		}
		else {
			setState(State.IDLE);
		}
	}

	public void setBehavior(Behavior behavior) {
		this.behavior = behavior;
	}

	public Behavior getBehavior() {
		return behavior;
	}
}