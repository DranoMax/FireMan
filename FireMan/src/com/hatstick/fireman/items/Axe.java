package com.hatstick.fireman.items;

import java.util.Map.Entry;

import com.alex.model.Level;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.hatstick.fireman.WorldRenderer;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;
import com.hatstick.fireman.physics.ExplosionEngine;

public class Axe {
	ExplosionEngine explosion = new ExplosionEngine();
	/** Hits clicked object (if close enough) **/
	public void swingAxe(Level level, Box2DPhysicsWorld world, Body hitBody, float x, float y) {
		
		/** Blow 'er up **/
		explosion.boom(new Vector2(x,y), hitBody, world);
	}
}
