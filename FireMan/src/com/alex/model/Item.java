package com.alex.model;

import com.badlogic.gdx.math.Vector2;

public class Item extends Entity {
	
	enum Type {
		SMALL 
	}

	private Type type = Type.SMALL;

	public Item(Vector2 position, Type type) {
		super(position);
		this.type = type;
		setSpeed(2f);
		setSize(0.3f);
		setJumpVelocity(new Vector2(0,0));
		setBoundsHeight(getSize());
		setBoundsWidth(getSize());
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

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}
}
