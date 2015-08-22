package com.tutorial.screen;

import com.alex.model.Level;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.tutorial.controller.FireManController;
import com.tutorial.controller.CivilianController;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.hatstick.fireman.WorldRenderer;
import com.hatstick.fireman.physics.Box2DPhysicsWorld;

public class GameScreen implements Screen, InputProcessor {

	private Level 			level;
	private WorldRenderer 	renderer;
	private FireManController	controller;
	private CivilianController enemyController;
	private Box2DPhysicsWorld physicsWorld;

	private int width, height;

	@Override
	public void show() {
		level = new Level();
		physicsWorld = new Box2DPhysicsWorld(level);
		renderer = new WorldRenderer(level, false);
		controller = new FireManController(level, physicsWorld, renderer);
		enemyController = new CivilianController(level, physicsWorld, renderer);
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		physicsWorld.render(renderer.getCamera());
		controller.update(delta);
		enemyController.update(delta);
		renderer.render();
	}

	@Override
	public void resize(int width, int height) {
		renderer.setSize(width, height);
		this.width = width;
		this.height = height;
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		Gdx.input.setInputProcessor(null);
		physicsWorld.dispose();
		renderer.dispose();
	}

	// * InputProcessor methods ***************************//

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.A)
			controller.leftPressed();
		if (keycode == Keys.D)
			controller.rightPressed();
		if (keycode == Keys.UP)
			renderer.increaseMaxParticles();
		if (keycode == Keys.DOWN)
			renderer.decreaseMaxParticles();
		if (keycode == Keys.W)
			controller.upPressed();
		if (keycode == Keys.S)
			controller.downPressed();
		if (keycode == Keys.SPACE)
			controller.jumpPressed();
		if (keycode == Keys.E)
			controller.interactPressed();
		if (keycode == Keys.Z)
			renderer.setDebug();
		
		// Tool selection ********************************//
		if (keycode == Keys.NUM_1)
			controller.selectAxe();
		if (keycode == Keys.NUM_2)
			controller.selectWater();
		if (keycode == Keys.NUM_3)
			controller.selectGrappler();
		
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.A)
			controller.leftReleased();
		if (keycode == Keys.D)
			controller.rightReleased();
		if (keycode == Keys.W)
			controller.upReleased();
		if (keycode == Keys.S)
			controller.downReleased();
		if (keycode == Keys.SPACE)
			controller.jumpReleased();
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		controller.primaryPressed(x, y);
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		controller.sprayReleased();
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		controller.primaryMoved(x,y);
		return false;
	}

	
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
}
