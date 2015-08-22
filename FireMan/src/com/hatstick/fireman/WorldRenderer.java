package com.hatstick.fireman;

import java.util.Map;
import java.util.Random;

import com.alex.model.Block;
import com.alex.model.FireMan;
import com.alex.model.Civilian;
import com.alex.model.Entity.State;
import com.alex.model.Level;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class WorldRenderer {

	private OrthographicCamera cam;
	private static final float CAMERA_WIDTH = 10f;
	private static final float CAMERA_HEIGHT = 7f;
	private static final float RUNNING_FRAME_DURATION = 0.06f;

	private static int SCREEN_WIDTH = 0;
	private static int SCREEN_HEIGHT = 0;

	private Level world;

	/** for debug rendering **/
	ShapeRenderer debugRenderer = new ShapeRenderer();

	private TextureRegion bobIdleLeft;
	private TextureRegion bobIdleRight;
	private TextureRegion blockTexture;
	private TextureRegion bobFrame;
	private TextureRegion enemyFrame;

	private BitmapFont font;

	/** Falling textures **/
	private TextureRegion bobJumpLeft;
	private TextureRegion bobFallLeft;
	private TextureRegion bobJumpRight;
	private TextureRegion bobFallRight;

	/** Animations **/
	private Animation walkLeftAnimation;
	private Animation walkRightAnimation;

	private FPSLogger fpsLogger;

	private SpriteBatch spriteBatch;
	private boolean debug = false;
	private int width;
	private int height;
	private float ppuX;	// pixels per unit on the X axis
	private float ppuY;	// pixels per unit on the Y axis

	// For camera shaking
	private float shakeRadius = 50;
	private Vector2 shakeOffset = new Vector2(shakeRadius,0);
	private float previousTime = 0;
	private boolean shakeRight = false;

	public void setSize (int w, int h) {
		SCREEN_WIDTH = w;
		SCREEN_HEIGHT = h;
		this.width = w;
		this.height = h;
		ppuX = (float)width / CAMERA_WIDTH;
		ppuY = (float)height / CAMERA_HEIGHT;
	}

	public WorldRenderer(Level world, boolean debug) {
		this.world = world;
		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
		this.cam.update();
		this.debug = debug;
		spriteBatch = new SpriteBatch();
		loadTextures();
		fpsLogger = new FPSLogger();
	}

	private void loadWalkingAnimation(TextureAtlas atlas) {
		bobIdleLeft = atlas.findRegion("bob-01");
		bobIdleRight = new TextureRegion(bobIdleLeft);
		bobIdleRight.flip(true, false);
		blockTexture = atlas.findRegion("block");
		TextureRegion[] walkLeftFrames = new TextureRegion[5];
		for (int i = 0; i < 5; i++) {
			walkLeftFrames[i] = atlas.findRegion("bob-0" + (i + 2));
		}
		walkLeftAnimation = new Animation(RUNNING_FRAME_DURATION, walkLeftFrames);

		TextureRegion[] walkRightFrames = new TextureRegion[5];

		for (int i = 0; i < 5; i++) {
			walkRightFrames[i] = new TextureRegion(walkLeftFrames[i]);
			walkRightFrames[i].flip(true, false);
		}
		walkRightAnimation = new Animation(RUNNING_FRAME_DURATION, walkRightFrames);
	}

	private void loadTextures() {
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("images/textures/textures.pack"));
		loadWalkingAnimation(atlas);

		/** Jumping/Falling Down textures **/
		bobJumpLeft = atlas.findRegion("bob-up");
		bobJumpRight = new TextureRegion(bobJumpLeft);
		bobJumpRight.flip(true, false);
		bobFallLeft = atlas.findRegion("bob-down");
		bobFallRight = new TextureRegion(bobFallLeft);
		bobFallRight.flip(true, false);

		// Prepare our font
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
	}

	private void drawDebugBoundingCircle(Circle circle) {
		debugRenderer.setColor(1, 0, 0, 1);
		debugRenderer.begin(ShapeType.Line);
		debugRenderer.circle(circle.x, circle.y, circle.radius);
		debugRenderer.end();
	}

	private void drawBob() {
		FireMan bob = world.getBob();
		bobFrame = bob.isFacingLeft() ? bobIdleLeft : bobIdleRight;
		if(bob.getState().equals(State.WALKING)) {
			bobFrame = bob.isFacingLeft() ? walkLeftAnimation.getKeyFrame(bob.getStateTime(), true) : 
				walkRightAnimation.getKeyFrame(bob.getStateTime(), true);
		}else if (bob.getState().equals(State.JUMPING)) {
			if (bob.getAcceleration().y > 0) {
				bobFrame = bob.isFacingLeft() ? bobJumpLeft : bobJumpRight;
			} else {
				bobFrame = bob.isFacingLeft() ? bobFallLeft : bobFallRight;
			}
		}
		/** TODO: Figure out the correct translation for the sprite Bob from physics Bob instead
		 *        of using this ductape method...
		 **/
		spriteBatch.draw(bobFrame, (bob.getPosition().x*ppuX+SCREEN_WIDTH/2-bob.getSize()/2*ppuX-cam.position.x*ppuX), 
				(bob.getPosition().y-bob.getSize()/2)* ppuY, FireMan.getSize() * ppuX, FireMan.getSize() * ppuY);

		drawDebugBoundingCircle(bob.getBoundingCircle());
	}

	private void drawEnemies() {
		Color undo = spriteBatch.getColor();

		// Change the enemy sprite colors
		spriteBatch.setColor(1, .5f, 0, 1);
		for (Civilian enemy : world.getEnemies().keySet()) {
			/** TODO: Figure out the correct translation for the sprite Bob from physics Bob instead
			 *        of using this ductape method...
			 **/

			enemyFrame = enemy.isFacingLeft() ? bobIdleLeft : bobIdleRight;

			if(enemy.getState() == State.WALKING) {
				enemyFrame = enemy.isFacingLeft() ? walkLeftAnimation.getKeyFrame(enemy.getStateTime(), true) : 
					walkRightAnimation.getKeyFrame(enemy.getStateTime(), true);
			}else if (enemy.getState().equals(State.JUMPING)) {
				if (enemy.getAcceleration().y > 0) {
					enemyFrame = enemy.isFacingLeft() ? bobJumpLeft : bobJumpRight;
				} else {
					enemyFrame = enemy.isFacingLeft() ? bobFallLeft : bobFallRight;
				}
			}

			drawDebugBoundingCircle(enemy.getBoundingCircle());

			spriteBatch.draw(enemyFrame, (enemy.getPosition().x*ppuX+228-cam.position.x*ppuX), 
					(enemy.getPosition().y-enemy.getSize()/2)* ppuY, enemy.getSize() * ppuX, enemy.getSize() * ppuY);
		}
		spriteBatch.setColor(undo);
	}

	/** Increase max number of particles by 10 **/
	public void increaseMaxParticles() {
		for (ParticleEffect fire : world.getFires().keySet()) {
			fire.getEmitters().get(0).setMaxParticleCount(fire.getEmitters().get(0).getMaxParticleCount()+10);
		}
	}

	/** Reduce max number of particles by 10 **/
	public void decreaseMaxParticles() {
		for (ParticleEffect fire : world.getFires().keySet()) {
			fire.getEmitters().get(0).setMaxParticleCount(fire.getEmitters().get(0).getMaxParticleCount()-10);
		}
	}

	public void drawFires() {
		for (Map.Entry<ParticleEffect, Vector2> fire : world.getFires().entrySet()) {
			fire.getKey().setPosition(fire.getValue().x*ppuX+228-cam.position.x*ppuX, fire.getValue().y* ppuY);
			fire.getKey().draw(spriteBatch, Gdx.graphics.getDeltaTime()/2);
		}
	}

	public void cameraShake() {
		cam.position.x = world.getBob().getPosition().x + shakeOffset.x/ppuX; // set center of viewport
		if(shakeOffset.x < -shakeRadius/(world.getBob().getHealth()+1)) {
			shakeRight = true;
		}
		else if(shakeOffset.x > shakeRadius/(world.getBob().getHealth()+1)){
			shakeRight = false;
		}

		if(shakeRight == true) {
			shakeOffset.x += 0.8f+shakeRadius/(world.getBob().getHealth()+1);
		}
		else shakeOffset.x -= 0.8f+shakeRadius/(world.getBob().getHealth()+1);
	}

	public void render() {

		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if(world.getBob().areCloseEnemies()) {
			if(world.getBob().getStateTime()-previousTime > 0.01) {
				cameraShake();
				previousTime = world.getBob().getStateTime();
			}
		}
		else { 
			cam.position.x = world.getBob().getPosition().x;
		}
		cam.update();
		spriteBatch.begin();
		// drawBlocks();
		drawFires();
		drawBob();
		drawEnemies();
		if(world.getBob().getHealth() > 50) font.setColor(Color.BLUE);
		else font.setColor(Color.RED);	
		//font.draw(spriteBatch, "Health: " + world.getBob().getHealth(), 0, 20);
		font.draw(spriteBatch, "FPS: " +  Gdx.graphics.getFramesPerSecond(), 0, 20);
		font.draw(spriteBatch, world.getBob().getCurrentTool().toString(), SCREEN_WIDTH-100, SCREEN_HEIGHT-10);
		spriteBatch.end();

		if(world.getBob().isSpraying()) {
			debugRenderer.begin(ShapeType.Filled);
			debugRenderer.circle(world.getBob().getMouseTarget().x, 
					world.getBob().getMouseTarget().y, 5);

			for (Vector2 fire : world.getFires().values()) {	
				debugRenderer.rect((fire.x*ppuX+228-cam.position.x*ppuX)-25, (fire.y*ppuY)-25, 50, 50);
			}		
			debugRenderer.end();
		}
		if (world.getBob().getGrappler().isGrappling()) {
			debugRenderer.begin(ShapeType.Line);
			debugRenderer.line(world.getBob().getPosition().x*ppuX+228-cam.position.x*ppuX, world.getBob().getPosition().y*ppuY, 
					world.getBob().getGrappler().getGrapplePoint().x*ppuX+228-cam.position.x*ppuX, world.getBob().getGrappler().getGrapplePoint().y*ppuY);
			debugRenderer.end();
		}
		if (debug)
			drawDebug();
	}

	private void drawBlocks() {
		for (Block block : world.getBlocks()) {
			spriteBatch.draw(blockTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, Block.SIZE * ppuX, Block.SIZE * ppuY);
		}
	}

	private void drawDebug() {
		// render blocks
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.Line);
		for (Block block : world.getBlocks()) {
			Rectangle rect = block.getBounds();
			float x1 = block.getPosition().x + rect.x;
			float y1 = block.getPosition().y + rect.y;
			debugRenderer.setColor(new Color(1, 0, 0, 1));
			debugRenderer.rect(x1, y1, rect.width, rect.height);
		}
		// render Bob
		FireMan bob = world.getBob();
		Rectangle rect = bob.getBounds();
		float x1 = bob.getPosition().x + rect.x;
		float y1 = bob.getPosition().y + rect.y;
		debugRenderer.setColor(new Color(0, 1, 0, 1));
		debugRenderer.rect(x1, y1, rect.width, rect.height);
		debugRenderer.end();
	}

	public float getPpuX() {
		return ppuX;
	}

	public float getPpuY() {
		return ppuY;
	}

	public void setDebug() {
		debug = !debug;
	}

	public OrthographicCamera getCamera() {
		return cam;
	}

	public float getScreenWidth() {
		return CAMERA_WIDTH*ppuX;
	}

	public float getScreenHeight() {
		return CAMERA_HEIGHT*ppuY;
	}

	public void dispose () {
		spriteBatch.dispose();
	}

	/** The magic formula that translates from physics world to the screen **/
	public Vector2 worldToScreen(Vector2 world) {
		Vector2 screen = world;
		return screen;
	}
}
