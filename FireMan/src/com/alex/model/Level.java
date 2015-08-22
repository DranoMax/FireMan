package com.alex.model;

import java.util.HashMap;

import com.alex.model.Civilian.Behavior;
import com.alex.model.Item.Type;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Level {
	
	/** The blocks making up the world **/
	private Array<Block> blocks = new Array<Block>();
	/** Our player controlled hero **/
	private FireMan bob;
	/** Our enemy collection 
	 *  Note: Integer is used to store the id of the enemy for various calculations**/
	private HashMap<Civilian, Integer> enemies = new HashMap<Civilian, Integer>();
	/** Our item collection **/
	private HashMap<Item, Integer> items = new HashMap<Item, Integer>();
	/** Our fire locations **/
	private HashMap<ParticleEffect, Vector2> fires = new HashMap<ParticleEffect, Vector2>();
	
	// Getters ---------
	public Array<Block> getBlocks() {
		return blocks;
	}
	public FireMan getBob() {
		return bob;
	}
	public HashMap<Civilian, Integer> getEnemies() {
		return enemies;
	}
	public HashMap<Item, Integer> getItems() {
		return items;
	}
	public HashMap<ParticleEffect, Vector2> getFires() {
		return fires;
	}
	// -----------------
	
	public Level() {
		createDemoWorld();
	}
	
	public ParticleEffect fireEffect(int x, int y) {
		ParticleEffect effect = new ParticleEffect();
		effect.load(Gdx.files.internal("data/fire_particles.p"), Gdx.files.internal("data"));
		effect.setPosition(x, y);
		// Of course, a ParticleEffect is normally just used, without messing around with its emitters.
	//	effect.getEmitters().clear();
		effect.getEmitters().add(effect.getEmitters().get(0));
		return effect;
	}
	
	private void createDemoWorld() {
		bob = new FireMan(new Vector2(1,4));
//		items.put(new Item(new Vector2(3,2), Type.SMALL), true);
//		enemies.put(new Enemy(new Vector2(3,2), Behavior.SMARTCHASE), true);
		fires.put(fireEffect(6,1),new Vector2(6,1));
		fires.put(fireEffect(8,1),new Vector2(8,1));
	//	enemies.put(new Civilian(new Vector2(8,2), Behavior.WANDER), 1);
		
		for (int i = 0; i < 21; i++) {
			blocks.add(new Block(new Vector2(i,1)));
		}
		
		blocks.add(new Block(new Vector2(4, 3)));
		blocks.add(new Block(new Vector2(5, 3)));
		
		blocks.add(new Block(new Vector2(0, 2)));
		blocks.add(new Block(new Vector2(0, 3)));
		blocks.add(new Block(new Vector2(0, 4)));
		blocks.add(new Block(new Vector2(0, 5)));
		
		blocks.add(new Block(new Vector2(3, 3)));
		blocks.add(new Block(new Vector2(3, 4)));
		blocks.add(new Block(new Vector2(3, 5)));
		
		blocks.add(new Block(new Vector2(20, 2)));
		blocks.add(new Block(new Vector2(20, 3)));
		blocks.add(new Block(new Vector2(20, 4)));
		blocks.add(new Block(new Vector2(20, 5)));
		
		blocks.add(new Block(new Vector2(17, 3)));
		blocks.add(new Block(new Vector2(17, 4)));
		blocks.add(new Block(new Vector2(17, 5)));
		
		//Pillar
		blocks.add(new Block(new Vector2(7, 2)));
		blocks.add(new Block(new Vector2(7, 3)));
		blocks.add(new Block(new Vector2(7, 4)));
		blocks.add(new Block(new Vector2(7, 5)));
		blocks.add(new Block(new Vector2(7, 6)));
	}
}
