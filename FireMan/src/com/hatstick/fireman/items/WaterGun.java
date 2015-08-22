package com.hatstick.fireman.items;

import java.util.Map.Entry;

import com.alex.model.Level;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hatstick.fireman.WorldRenderer;

public class WaterGun {
	
	/** Sprays water outward in direction standing **/
	public void sprayWater(Level level, WorldRenderer renderer, float x, float y) {
		Rectangle bounds = new Rectangle();
		for (Entry<ParticleEffect, Vector2> fire : level.getFires().entrySet()) {
			bounds.set(fire.getValue().x*renderer.getPpuX()+228-renderer.getCamera().position.x*renderer.getPpuX()-25, 
					(fire.getValue().y*renderer.getPpuY())-25, 50, 50);
			// If we're hitting the fire, make it shrink
			if(bounds.contains(x, y)) {
				if(fire.getKey().getEmitters().get(0).getMaxParticleCount() > 20) {
					fire.getKey().getEmitters().get(0).setMaxParticleCount(fire.getKey().getEmitters().get(0).getMaxParticleCount()-20);
				}
			}
		}
	}
}
