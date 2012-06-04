/**
 * @author Sebastian Nilsson
 * project url: http://sebastiannilsson.com/projekt/sketchy-truck/behover-hjalp-2d-bilspel-i-android/
 */

package se.treplex.sketchytruck;

import static org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.WakeLockOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.ITexture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.res.AssetManager;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.CursorAdapter;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class GameActivity extends LayoutGameActivity implements
		IOnSceneTouchListener, IAccelerometerListener {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final short CATEGORYBIT_NO = 0;
	public static final short CATEGORYBIT_WALL = 1;
	public static final short CATEGORYBIT_BOX = 2;
	public static final short CATEGORYBIT_CIRCLE = 4;
	public static final short MASKBITS_WALL = CATEGORYBIT_WALL
			+ CATEGORYBIT_BOX + CATEGORYBIT_CIRCLE;
	public static final short MASKBITS_BOX = CATEGORYBIT_WALL + CATEGORYBIT_BOX; // Missing:
	// CATEGORYBIT_CIRCLE
	public static final short MASKBITS_CIRCLE = CATEGORYBIT_WALL
			+ CATEGORYBIT_CIRCLE; // Missing: CATEGORYBIT_BOX
	public static final short MASKBITS_NOTHING = 0; // Missing: all
	public static final short MASKBITS_ONLY_WALL = CATEGORYBIT_WALL; // Missing:
	// all
	// but
	// wall
	public static final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.8f, false, CATEGORYBIT_WALL,
					MASKBITS_WALL, (short) 0);
	public static final FixtureDef BOX_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_BOX,
					MASKBITS_BOX, (short) 0);
	public static final FixtureDef CIRCLE_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_CIRCLE,
					MASKBITS_CIRCLE, (short) 0);
	public static final FixtureDef NO_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_NO,
					MASKBITS_NOTHING, (short) 0);
	public static final FixtureDef ONLY_WALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.2f, 1f, false, CATEGORYBIT_CIRCLE,
					MASKBITS_ONLY_WALL, (short) -1);
	

	public static final int NOSCREEN = 0;
	public static final int MAIN_SCREEN_ID = 1;
	public static final int OPTION_SCREEN_ID = 2;
	public static final int HELP_SCREEN_ID = 3;
	public static final int ABOUT_SCREEN_ID = 4;
	public static final int CHOOSELEVEL_SCREEN_ID = 5;
	public static final int PAUSE_SCREEN_ID = 6;
	public static final int LOADING_SCREEN_ID = 7;
	


	// ===========================================================
	// Physics Constants
	// ===========================================================
	
	private static final float CAR_FORWARDS_TORQUE = 1000;
	private static final float CAR_FORWARDS_SPEED = 20;
	
	private static final float CAR_BRAKE_TORQUE = 2000;
	
	private static final float CAR_REVERSE_TORQUE = 1000;
	private static final float CAR_REVERSE_SPEED = -10;
	

	// ===========================================================
	// Fields
	// ===========================================================
	//Flags to know wich side of the screen is pressed
	boolean pressedLeft = false;
	boolean pressedRight = false;
	//Index of the actual level
	int actualLevelIndex = 0;
	//Value selected by the user in the options
	private float tiltAlpha = 0.5f;
	private Scene mScene;
	//Each one of the different screens of the game
	private ArrayList<Sprite> mainScreen;
	private ArrayList<Sprite> loadingScreen;
	private ArrayList<Sprite> pauseScreen;
	private ArrayList<Sprite> aboutScreen;
	private ArrayList<Sprite> optionScreen;
	private ArrayList<Sprite> helpScreen;
	private ArrayList<ArrayList<Sprite>> chooseLevelScreen;
	//holds the current chosen screen
	private int currentScreenID;
	//Entities loaded in the level, use to release them from the scene when level finished
	private ArrayList<IEntity> levelEntities;
	private StackPanel sp;
	
	//Physics Engine
	private PhysicsWorld levelWorldPhysics;
	private BoundCamera cameraBound;
	//Container of info for the level world to be loaded
	private LevelWorld actualWorld;
	//Main background of the actual level
	private Sprite levelBackground;
	//back backgrounds for parallax
	private ArrayList<Sprite> bbackground;
	//Flag to know if you fall in a dead area
	private boolean rest = false;
	//Flag to know if you reach the end
	private boolean isFinish = false;
	private Sprite line1;
	private Sprite line2;
	// shapes
	private PhysicsEditorShapeLibrary worldPhysicsEditorShapeLibrary;
	private PhysicsEditorShapeLibrary gamePhysicsEditorShapeLibrary;

	private HashMap<String, TextureRegion> worldTextureRegionHashMap = new HashMap<String, TextureRegion>();
	private HashMap<String, TextureRegion> gameTextureRegionHashMap = new HashMap<String, TextureRegion>();
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Physics Entities
	RevoluteJointDef motor1;
	private RevoluteJoint mMotor1;
	private RevoluteJoint mMotor2;
	PrismaticJointDef spring1;
	PrismaticJointDef spring2;
	PrismaticJoint mSpring1;
	PrismaticJoint mSpring2;
	Body cart;
	Shape cartShape;
	Body wheel1Body;
	Body wheel2Body;
	private Body axle1Body;
	private Body axle2Body;
	Line connectionLine1;
	Line connectionLine2;
	List<MixedDataContainer> oEntities = new LinkedList<MixedDataContainer>();
	LinkedList<Shape> areas = new LinkedList<Shape>();
	Shape finish;
	// car parameters
	MixedDataContainer oCar = new MixedDataContainer();
	MixedDataContainer pCar = new MixedDataContainer();
	MixedDataContainer pWheel1 = new MixedDataContainer();
	MixedDataContainer pWheel2 = new MixedDataContainer();
	MixedDataContainer pSpring1 = new MixedDataContainer();
	MixedDataContainer pSpring2 = new MixedDataContainer();
	private Sprite wheel1Face;
	private Sprite wheel2Face;
	private Sprite axle1Face;
	private Sprite axle2Face;	
	////////////////////////////////////////////////////////////////////////////////////////

	// NUMBER of level in the folder levels
	// a folder level has the following format "level[n]" where n is a number
	// i.e level0
	// The levels folder start their numbers from 0
	private int NUMBRE_OF_LEVELS = 3;

	//Sound of the car engine
	private Sound engineSound;
	//Path of this sound
	private String engineSoundPath = "click-many-effect.mp3";
	//Flag to know if the level is fully loaded
	private boolean levelStarted;
	//Updater instance of the scene used to later be unregistered
	private myUpdateHandler upHand = new myUpdateHandler(this);
	//Flag to know if the game is paused
	private boolean isPaused;
	//Application Main Background(Screens)
	private Sprite mainBackground;
	//Textures to be unloaded whe needed to free memory
	private ArrayList<ITexture> textures;
	//Cumul of second waiting for loading
	float preUpdates = 0;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public Engine onLoadEngine() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();
		//Chase Camera creation
		cameraBound = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 0,
				CAMERA_WIDTH, 0, CAMERA_HEIGHT);
		cameraBound.setBoundsEnabled(true);

		//Engine initialization
		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), cameraBound)
				.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		
		//Setting the use of music
		engineOptions.setNeedsMusic(true);
		engineOptions.setNeedsSound(true);
		return new Engine(engineOptions);
	}

	public void onLoadResources() {
		
		//Try to load the music of the game
		try {
			engineSound = SoundFactory.createSoundFromAsset(mEngine
					.getSoundManager(), this, "sfx/" + engineSoundPath);
			engineSound.setLooping(true);
			engineSound.setVolume(0.5f);
		} catch (Exception e) {

		}

		/* car and hud textues */
		BitmapTextureAtlasEx mGameTextureAtlasEx = new BitmapTextureAtlasEx(
				256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		// // car body
		TextureRegion mTextureRegion1 = mGameTextureAtlasEx.appendTextureAsset(
				this, "gfx/car/body_2.png");
		gameTextureRegionHashMap.put("car_body", mTextureRegion1);
		TextureRegion mTextureRegion2 = mGameTextureAtlasEx.appendTextureAsset(
				this, "gfx/car/wheel_2.png");
		gameTextureRegionHashMap.put("car_wheel", mTextureRegion2);
		this.mEngine.getTextureManager().loadTexture(mGameTextureAtlasEx);

		// Create the shape importer and open our shape definition file
		this.gamePhysicsEditorShapeLibrary = new PhysicsEditorShapeLibrary();
		this.gamePhysicsEditorShapeLibrary.open(this, "gfx/car/shapes.xml");
	}

	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(4096, 2048,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		//Main Background loading process note the file name if you need to change it
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/menubackground.png", 0, 0);  
		mEngine.getTextureManager().loadTexture(tempTexture);

		mainBackground = new Sprite(0, 0, tempTextureRegion);
		mainBackground.setScaleCenter(0, 0);
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT= display.getHeight();
		
		mainBackground.setScale(CAMERA_WIDTH / mainBackground.getWidth(), CAMERA_HEIGHT / mainBackground.getHeight());
		//Scene creation
		mScene = new Scene();
		mScene.setBackground(new SpriteBackground(mainBackground));
		mScene.setOnSceneTouchListener(this);

		//Intialization of the different screens
		initializeMainScreen();
		intializeLoadingScreen();
		initializeHelpScreen();
		initializeAboutScreen();
		initializeOptionScreen();
		initializePauseScreen();
		loadLevelsInfo();

		//Enabeling the use of the touch areas
		mScene.setTouchAreaBindingEnabled(true);
		//Load the main screen of the game
		loadScreen(mainScreen);
		currentScreenID = MAIN_SCREEN_ID;
		return mScene;
	}

	public void onLoadComplete() {

	}

	// Control of the taped area of the screen
	public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent) {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();

		//If the level is no started there is no need of do any thing
		if (!levelStarted)
			return false;
		//If the game is not in pause 
		if (!isPaused)
			//Look for the region touched to set flags
			if (pSceneTouchEvent.isActionDown()|| pSceneTouchEvent.isActionMove()) {
				if (pSceneTouchEvent.getMotionEvent().getRawX() > CAMERA_WIDTH
						/ 2 + CAMERA_WIDTH / 4) {

					if (!pressedRight)
						engineSound.play();
					pressedRight = true;
					pressedLeft = false;
				} else if (pSceneTouchEvent.getMotionEvent().getRawX() < CAMERA_WIDTH
						/ 2 - CAMERA_WIDTH / 4) {
					pressedLeft = true;
					pressedRight = false;
					engineSound.stop();
				} else if(!isPaused && pSceneTouchEvent.isActionMove()){
					pressedLeft = pressedRight = false;
					engineSound.stop();
				}
				//Pausing the game
				else if (!isPaused && !pSceneTouchEvent.isActionMove()) {

					isPaused = true;

					pressedLeft = pressedRight = false;
					engineSound.stop();

					mScene.unregisterUpdateHandler(levelWorldPhysics);
					mEngine.unregisterUpdateHandler(upHand);
					
					sp.ArrangeElements();
					//Bringing the controls to the center of the camera
					pauseScreen.get(0).setPosition(
							cameraBound.getMinX() + pauseScreen.get(0).getX(),
							cameraBound.getMinY() + + pauseScreen.get(0).getY());

					pauseScreen.get(1).setPosition(
							cameraBound.getMinX() + pauseScreen.get(1).getX(),
							cameraBound.getMinY()+ pauseScreen.get(1).getY());

					pauseScreen.get(2).setPosition(
							cameraBound.getMinX() + pauseScreen.get(2).getX(),
							cameraBound.getMinY()+ pauseScreen.get(2).getY());

					//Loading pause Screen
					loadScreen(pauseScreen);
					currentScreenID = PAUSE_SCREEN_ID;
					return true;

				}

			} else {
				pressedLeft = pressedRight = false;
				engineSound.stop();
			}

		return true;
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		//finish();
		int a = 10;
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		finish();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private void generateUpdateHandler() {
		upHand = new myUpdateHandler(this);
		this.mEngine.registerUpdateHandler(upHand);
		this.mScene.registerUpdateHandler(levelWorldPhysics);
	}
	
	//Update handler of the Entire application
	// update position and looking if you are in a dead area for restart or if
	// you reached the finish line
	// also here is the checking for the left and right pressed flags
	class myUpdateHandler implements IUpdateHandler {
		
		IAccelerometerListener c;
		public myUpdateHandler(IAccelerometerListener c){
			this.c = c;
		}
		
		public void onUpdate(float pSecondsElapsed) {

			if (preUpdates > 3) {
				preUpdates = -1;
				enableAccelerometerSensor(c);
				levelStarted = true;
				unloadScreen(loadingScreen);
				currentScreenID = NOSCREEN;
				LoadLevelSprites();
			} else if (preUpdates >= 0)
				preUpdates += pSecondsElapsed;

			final Display display = getWindowManager().getDefaultDisplay();
			int CAMERA_WIDTH = display.getWidth();
			
			if (pressedRight) {
				//accelerate forwards
				mMotor1.enableMotor(true);
				mMotor1.setMotorSpeed(CAR_FORWARDS_SPEED);
				mMotor1.setMaxMotorTorque(CAR_FORWARDS_TORQUE);

				mMotor2.enableMotor(true);
				mMotor2.setMotorSpeed(CAR_FORWARDS_SPEED);
				mMotor2.setMaxMotorTorque(CAR_FORWARDS_TORQUE);
				
			} else if (pressedLeft) {
				//brake if wheels going forwards, otherwise reverse
				
				if(mMotor1.getMotorSpeed() > 0){
					//brake
					mMotor1.enableMotor(true);
					mMotor1.setMotorSpeed(0);
					mMotor1.setMaxMotorTorque(CAR_BRAKE_TORQUE);
	
					mMotor2.enableMotor(true);
					mMotor2.setMotorSpeed(0);
					mMotor2.setMaxMotorTorque(CAR_BRAKE_TORQUE);
				}
				else{
					//reverse
					mMotor1.enableMotor(true);
					mMotor1.setMotorSpeed(CAR_REVERSE_SPEED);
					mMotor1.setMaxMotorTorque(CAR_REVERSE_TORQUE);

					mMotor2.enableMotor(true);
					mMotor2.setMotorSpeed(CAR_REVERSE_SPEED);
					mMotor2.setMaxMotorTorque(CAR_REVERSE_TORQUE);
				}
				
			} else {
				//coast (no acceleration/deceleration)
				mMotor1.setMotorSpeed(0);
				mMotor1.setMaxMotorTorque(0);
				mMotor2.setMotorSpeed(0);
				mMotor2.setMaxMotorTorque(0);
			}

			final Vector2 cartCenter = cart.getWorldCenter();
			final Vector2 wheel1Center = wheel1Body.getWorldCenter();
			connectionLine1.setPosition(cartCenter.x
					* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					cartCenter.y
							* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					wheel1Center.x
							* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					wheel1Center.y
							* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);

			final Vector2 wheel2Center = wheel2Body.getWorldCenter();
			connectionLine2.setPosition(cartCenter.x
					* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					cartCenter.y
							* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					wheel2Center.x
							* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					wheel2Center.y
							* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
			// springs
			mSpring1.setMaxMotorForce((float) (40 + Math.abs(400 * Math.pow(
					mSpring1.getJointTranslation(), 2))));
			mSpring1
					.setMotorSpeed((float) ((mSpring1.getMotorSpeed() - 10 * mSpring1
							.getJointTranslation()) * 0.4));

			mSpring2.setMaxMotorForce((float) (40 + Math.abs(400 * Math.pow(
					mSpring2.getJointTranslation(), 2))));
			mSpring2
					.setMotorSpeed((float) ((mSpring2.getMotorSpeed() - 10 * mSpring2
							.getJointTranslation()) * 0.4));
			
			//If you are in a dead area then Restart
			if (rest)
				Restart();
			rest = false;

			//If you reach the end, you will go to the next level in case there is one in other case
			//the main screen will be shown
			if (isFinish)
				if (actualLevelIndex < NUMBRE_OF_LEVELS - 1) {
					actualLevelIndex++;
					CloseLevel();
					preUpdates = 0;
					loadScreen(loadingScreen);
					currentScreenID = LOADING_SCREEN_ID;
					Thread thread = new Thread(new LoadLevelThread());
					thread.start();
					mScene.sortChildren();
				} else {
					CloseLevel();
					preUpdates = 0;
					loadScreen(mainScreen);
					currentScreenID = MAIN_SCREEN_ID;
				}
			isFinish = false;
			
			//Update for the different backs in the parallax
			for (Sprite sp : bbackground) {
				float val = ((levelBackground.getWidthScaled() - sp.getWidthScaled()) / (levelBackground.getWidthScaled() - CAMERA_WIDTH))
						* cameraBound.getMinX();
				sp.setPosition(val, 0);
			}
		}

		public void reset() {
		}
	}

	// Unload the specified screen
	private void unloadScreen(ArrayList<Sprite> screen) {
		for (Sprite sprite : screen) {
			mScene.detachChild(sprite);
			mScene.unregisterTouchArea(sprite);
			mEngine.getTextureManager().unloadTexture(sprite.getTextureRegion().getTexture());
		}
	}

	// Load the specified screen
	private void loadScreen(ArrayList<Sprite> screen) {
		for (Sprite sprite : screen) {
			mScene.attachChild(sprite);
			mScene.registerTouchArea(sprite);
			mEngine.getTextureManager().loadTexture(sprite.getTextureRegion().getTexture());
		}
	}

	public float getCenter(float total, float size) {
		return (total - size) / 2f;
	}

	// Here i create the main user interface
	// The idea is store all the sprites in a list the same for options
	// choose levels and other screens
	// The is use loadScreen and unloadscreen to set the screen
	private void initializeMainScreen() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		mainScreen = new ArrayList<Sprite>();

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/sketchytitle.png", 0,
						0);
		mEngine.getTextureManager().loadTexture(tempTexture);

        Sprite tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
                .getWidth()), getCenter(CAMERA_HEIGHT, 4 * tempTextureRegion
                        .getHeight() + 20), tempTextureRegion);
		UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.5f;
		us.height = 1/5f;
        us.leftMargin = 0.26f;
        us.topMargin = 0.01f;
		us.SetProperties();
		
		mainScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/treplexlogo.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(CAMERA_WIDTH - tempTextureRegion.getWidth()
				- 10, CAMERA_HEIGHT - tempTextureRegion.getHeight() + 5,
				tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(mainScreen);
					loadScreen(aboutScreen);
					currentScreenID = ABOUT_SCREEN_ID;
					return true;
				}
				return false;
			}
		};

		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.2f;
		us.height = 1/9f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		mainScreen.add(us.sprite);
		
		StackPanel sp = new StackPanel(CAMERA_WIDTH, CAMERA_HEIGHT);		

		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
                .createFromAsset(tempTexture, this, "gfx/newgamebutton.png", 0,
						0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 4 * tempTextureRegion
				.getHeight() + 20), tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(mainScreen);

					if (chooseLevelScreen.size() > 0)  {
						loadScreen(chooseLevelScreen.get(0));
						currentScreenID = CHOOSELEVEL_SCREEN_ID;
					}
					return true;
				}
				return false;
            }
        };
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);

		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/optionsbutton.png", 0,
						0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 4 * tempTextureRegion
				.getHeight() + 20)
				+ tempTextureRegion.getHeight() + 10, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(mainScreen);
					loadScreen(optionScreen);
					currentScreenID = OPTION_SCREEN_ID;
					return true;
				}
				return false;
			}
		};
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);

		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/helpbutton.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);
		
		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 4 * tempTextureRegion
				.getHeight() + 20)
				+ 2 * tempTextureRegion.getHeight() + 20, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(mainScreen);
					mScene.setBackground(new ColorBackground(0, 0, 0));
					loadScreen(helpScreen);
					currentScreenID = HELP_SCREEN_ID;
					return true;
				}
				return false;
			}
		};
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);
		
		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/quitbutton.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 4 * tempTextureRegion
				.getHeight() + 20)
				+ 3 * tempTextureRegion.getHeight() + 30, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					finish();
					return true;
				}
				return false;
			}
		};
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);
		
		for (UIElement element : sp.elements) {
			mainScreen.add(((UISprite)element).sprite);
		}
	}

	private void initializePauseScreen() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		pauseScreen = new ArrayList<Sprite>();
		
		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/resume.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		Sprite tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 3 * tempTextureRegion
				.getHeight() + 20), tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(pauseScreen);
					currentScreenID = NOSCREEN;
					generateUpdateHandler();
					isPaused = false;
					return true;
				}
				return false;
			}
		};
		sp = new StackPanel(CAMERA_WIDTH, CAMERA_HEIGHT);
		
		UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);

		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/restart.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 3 * tempTextureRegion
				.getHeight() + 20)
				+ 2 * tempTextureRegion.getHeight() + 20, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(pauseScreen);
					Restart();
					isPaused = false;
					return true;
				}
				return false;
			}
		};

		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);

		tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/quitbutton.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, 3 * tempTextureRegion
				.getHeight() + 20)
				+ tempTextureRegion.getHeight() + 10, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					preUpdates = 0;
					CloseLevel();
					unloadScreen(pauseScreen);
					loadScreen(mainScreen);
					currentScreenID = MAIN_SCREEN_ID;
					isPaused = false;
					return true;
				}
				return false;
			}
		};

		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
        us.width = 0.41f;
        us.height = 1 / 8f;
		us.bottomMargin = 0.0f;
		us.rightMargin = 0.0f;
		us.SetProperties();
		
		sp.AddElement(us);
		
		for (UIElement element : sp.elements) {
			pauseScreen.add(((UISprite)element).sprite);
		}
	}

	private void initializeAboutScreen() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		aboutScreen = new ArrayList<Sprite>();

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/abouttitle.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		Sprite tempSprite = new Sprite(10, 5, tempTextureRegion);
		UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.4f;
		us.height = 0.23f;
		us.leftMargin = 0.05f;
		us.topMargin = 0.05f;		
		us.SetProperties();
		
		aboutScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/aboutcaption.png", 0,
						0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, tempTextureRegion
				.getHeight()), tempTextureRegion);
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.7f;
		us.height = 1/2f;		
		us.SetProperties();
		us.SetHorizontalAligmentCenter();
		us.SetVerticalAligmentCenter();
		
		aboutScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/backarrow.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(5, CAMERA_HEIGHT
				- tempTextureRegion.getHeight(), tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(aboutScreen);
					loadScreen(mainScreen);
					currentScreenID = MAIN_SCREEN_ID;
					return true;
				}
				return false;
			}
		};
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.1f;
		us.height = 0.15f;
		us.leftMargin = 0.01f;
		us.bottomMargin = 0.01f;		
		us.SetProperties();
		
		aboutScreen.add(us.sprite);
	}

	private void initializeHelpScreen() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		helpScreen = new ArrayList<Sprite>();

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/helptitle.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		Sprite tempSprite = new Sprite(10, 5, tempTextureRegion);
		
		UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.4f;
		us.height = 0.23f;
		us.leftMargin = 0.05f;
		us.topMargin = 0.02f;		
		us.SetProperties();
		
		helpScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/control_example.png",
						0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, tempTextureRegion
				.getHeight()), tempTextureRegion);
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.83f;
		us.height = 0.725f;
		us.SetProperties();
		us.SetHorizontalAligmentCenter();
		us.SetVerticalAligmentCenter();
		
		helpScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/backarrow.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(5, CAMERA_HEIGHT
				- tempTextureRegion.getHeight(), tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(helpScreen);
					mScene.setBackground(new SpriteBackground(mainBackground));
					loadScreen(mainScreen);
					currentScreenID = MAIN_SCREEN_ID;
					return true;
				}
				return false;
			}
		};
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.1f;
		us.height = 0.15f;
		us.leftMargin = 0.01f;
		us.bottomMargin = 0.01f;		
		us.SetProperties();
		
		helpScreen.add(us.sprite);
	}

	private void initializeOptionScreen() {
		final Display display = getWindowManager().getDefaultDisplay();
		final int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		optionScreen = new ArrayList<Sprite>();

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/optionstitle.png", 0,
						0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		Sprite tempSprite = new Sprite(10, 5, tempTextureRegion);
		
		UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.4f;
		us.height = 0.23f;
		us.leftMargin = 0.05f;
		us.topMargin = 0.02f;		
		us.SetProperties();
		
		optionScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/optionscontent.png",
						0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);
		final float cx, cy;
		cx = getCenter(CAMERA_WIDTH, tempTextureRegion.getWidth());
		cy = getCenter(CAMERA_HEIGHT, tempTextureRegion.getHeight());
		tempSprite = new Sprite(getCenter(CAMERA_WIDTH, tempTextureRegion
				.getWidth()), getCenter(CAMERA_HEIGHT, tempTextureRegion
				.getHeight()), tempTextureRegion);
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.875f;
		us.height = 0.625f;	
		us.SetProperties();
		us.SetHorizontalAligmentCenter();
		us.SetVerticalAligmentCenter();
		
		optionScreen.add(us.sprite);

		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/backarrow.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(5, CAMERA_HEIGHT
				- tempTextureRegion.getHeight(), tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionDown()) {
					unloadScreen(optionScreen);
					loadScreen(mainScreen);
					currentScreenID = MAIN_SCREEN_ID;
					return true;
				}
				return false;
			}
		};
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.1f;
		us.height = 0.15f;
		us.leftMargin = 0.01f;
		us.bottomMargin = 0.01f;		
		us.SetProperties();
		
		optionScreen.add(us.sprite);
		
		StackPanel sp = new StackPanel(CAMERA_WIDTH, CAMERA_HEIGHT);
		sp.elementsMargin = 0.3f;
		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/line.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(5, CAMERA_HEIGHT
				- tempTextureRegion.getHeight(), tempTextureRegion) ;
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.78f;
		us.height = 0.05f;	
		us.SetProperties();
		
		sp.AddElement(us);
		
		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/line.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(5, CAMERA_HEIGHT
				- tempTextureRegion.getHeight(), tempTextureRegion) ;
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.78f;
		us.height = 0.05f;	
		us.SetProperties();
		sp.AddElement(us);
		for (UIElement element : sp.elements) {
			optionScreen.add(((UISprite)element).sprite);
		}
		
		final float thumbStart = us.sprite.getX();
		final float thumbEnd = us.sprite.getX() + us.actualWidth - CAMERA_WIDTH * 0.07f;
				
		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/lineYellow.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		line1 = new Sprite(thumbStart, ((UISprite)sp.elements.get(0)).sprite.getY(), tempTextureRegion) ;
		line1.setScaleCenter(0, 0);
		line1.setScale(((thumbEnd - thumbStart)/2.0f) / line1.getWidth(), us.sy);
		optionScreen.add(line1);
		
		tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/lineYellow.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		line2 = new Sprite(thumbStart, ((UISprite)sp.elements.get(1)).sprite.getY(), tempTextureRegion) ;
		line2.setScaleCenter(0, 0);
		line2.setScale(((thumbEnd - thumbStart)/2.0f) / line2.getWidth(), us.sy);
		optionScreen.add(line2);
		
		sp = new StackPanel(CAMERA_WIDTH, CAMERA_HEIGHT);
		sp.elementsMargin = 0.28f;

		tempTexture = new BitmapTextureAtlas(32, 32,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/thumb.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(thumbEnd, cy + 54, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionMove() || t.isActionDown()) {
					float xp = t.getX();
					xp = Math.max(thumbStart, xp);
					xp = Math.min(thumbEnd, xp);
					this.setPosition(xp, this.getY());

					engineSound.setVolume((xp - thumbStart)
							/ (thumbEnd - thumbStart));
					line1.setScaleX((xp - thumbStart)/line1.getWidth());
					return true;
				}
				return false;
			}
		};
		
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.07f;
		us.height = 0.07f;	
		us.SetProperties();
		sp.AddElement(us);

		tempTexture = new BitmapTextureAtlas(32, 32,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/thumb.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		tempSprite = new Sprite(thumbEnd, cy + 147, tempTextureRegion) {
			@Override
			public boolean onAreaTouched(final TouchEvent t, final float x,
					final float y) {
				if (t.isActionMove() || t.isActionDown()) {
					float xp = t.getX();
					xp = Math.max(thumbStart, xp);
					xp = Math.min(thumbEnd, xp);
					this.setPosition(xp, this.getY());
					line2.setScaleX((xp - thumbStart) / line2.getWidth() );
					tiltAlpha = (xp - thumbStart) / (thumbEnd - thumbStart);
					return true;
				}
				return false;
			}
		};
		us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.07f;
		us.height = 0.07f;	
		us.SetProperties();
		
		sp.AddElement(us);
		for (UIElement element : sp.elements) {
			optionScreen.add(((UISprite)element).sprite);
		}
	}

	private void intializeLoadingScreen() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		loadingScreen = new ArrayList<Sprite>();

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, "gfx/loading.png", 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		Sprite tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
				tempTextureRegion.getWidth()), getCenter(CAMERA_HEIGHT,
				tempTextureRegion.getHeight()), tempTextureRegion);
		
		UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
		
		us.width = 0.83f;
		us.height = 0.725f;
		us.SetProperties();
		us.SetHorizontalAligmentCenter();
		us.SetVerticalAligmentCenter();
		
		loadingScreen.add(us.sprite);
	}

	// Here i look for in the folder specified by the globla variable
	// NUMBER_OF_LEVELS
	private void loadLevelsInfo() {
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();

		AssetManager assetManager = getAssets();
		InputStream stream = null;

		chooseLevelScreen = new ArrayList<ArrayList<Sprite>>();
		ArrayList<Sprite> tempLevel = new ArrayList<Sprite>();

		try {
			for (int i = 0; i < NUMBRE_OF_LEVELS; i++) {
				final int index = i;

				BitmapTextureAtlas tempTexture = null;
				TextureRegion tempTextureRegion = null;
				Sprite tempSprite = null;

				if (i / 6 != chooseLevelScreen.size()) {

					if (i / 6 != 1) {
						tempTexture = new BitmapTextureAtlas(1024, 512,
								TextureOptions.BILINEAR_PREMULTIPLYALPHA);
						tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
								.createFromAsset(tempTexture, this,
										"gfx/backlevel.png", 0, 0);
						mEngine.getTextureManager().loadTexture(tempTexture);

						tempSprite = new Sprite(5, getCenter(CAMERA_HEIGHT,
								tempTextureRegion.getHeight()),
								tempTextureRegion) {
							@Override
							public boolean onAreaTouched(final TouchEvent t,
									final float x, final float y) {
								if (t.isActionDown()) {
									unloadScreen(chooseLevelScreen
											.get(index / 6 - 1));
									loadScreen(chooseLevelScreen
											.get(index / 6 - 2));
									return true;
								}
								return false;
							}
						};
						UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
						
						us.width = 0.05f;
						us.height = 0.14f;
						us.leftMargin = 0.04f;						
						us.SetProperties();
						us.SetVerticalAligmentCenter();
						tempLevel.add(us.sprite);
					}

					tempTexture = new BitmapTextureAtlas(1024, 512,
							TextureOptions.BILINEAR_PREMULTIPLYALPHA);
					tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
							.createFromAsset(tempTexture, this,
									"gfx/forwardlevel.png", 0, 0);
					mEngine.getTextureManager().loadTexture(tempTexture);

					tempSprite = new Sprite(CAMERA_WIDTH
							- tempTextureRegion.getWidth() - 5, getCenter(
							CAMERA_HEIGHT, tempTextureRegion.getHeight()),
							tempTextureRegion) {
						@Override
						public boolean onAreaTouched(final TouchEvent t,
								final float x, final float y) {
							if (t.isActionDown()) {
								unloadScreen(chooseLevelScreen
										.get(index / 6 - 1));
								loadScreen(chooseLevelScreen.get(index / 6));
								return true;
							}
							return false;
						}
					};
					
					UISprite us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
					
					us.width = 0.05f;
					us.height = 0.14f;
					us.rightMargin = 0.02f;						
					us.SetProperties();
					us.SetVerticalAligmentCenter();
					tempLevel.add(us.sprite);

					tempTexture = new BitmapTextureAtlas(1024, 512,
							TextureOptions.BILINEAR_PREMULTIPLYALPHA);
					tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
							.createFromAsset(tempTexture, this,
									"gfx/leveltitle.png", 0, 0);
					mEngine.getTextureManager().loadTexture(tempTexture);

					tempSprite = new Sprite(10, 5, tempTextureRegion);
					us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
						
					us.width = 0.4f;
					us.height = 0.23f;
					us.leftMargin = 0.05f;
					us.topMargin = 0.02f;		
					us.SetProperties();
					tempLevel.add(us.sprite);

					tempTexture = new BitmapTextureAtlas(1024, 512,
							TextureOptions.BILINEAR_PREMULTIPLYALPHA);
					tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
							.createFromAsset(tempTexture, this,
									"gfx/backarrow.png", 0, 0);
					mEngine.getTextureManager().loadTexture(tempTexture);

					tempSprite = new Sprite(5, CAMERA_HEIGHT
							- tempTextureRegion.getHeight(), tempTextureRegion) {
						@Override
						public boolean onAreaTouched(final TouchEvent t,
								final float x, final float y) {
							if (t.isActionDown()) {
								unloadScreen(chooseLevelScreen
										.get(index / 6 - 1));
								loadScreen(mainScreen);
								return true;
							}
							return false;
						}
					};
					us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
					
					us.width = 0.1f;
					us.height = 0.15f;
					us.leftMargin = 0.01f;
					us.bottomMargin = 0.01f;		
					us.SetProperties();
					
					tempLevel.add(us.sprite);

					chooseLevelScreen.add(tempLevel);
					tempLevel = new ArrayList<Sprite>();
				}

				stream = assetManager.open("levels/level" + i + "/world.xml");

				if (stream != null) {
					try {
						DocumentBuilderFactory dbf = DocumentBuilderFactory
								.newInstance();

						dbf.setValidating(false);
						dbf.setIgnoringComments(false);
						dbf.setIgnoringElementContentWhitespace(true);
						dbf.setNamespaceAware(true);

						DocumentBuilder db = null;
						db = dbf.newDocumentBuilder();

						Document document = db.parse(stream);
						Element root = document.getDocumentElement();

						String levelCaption = root
								.getAttribute("level-caption-image");

						stream.close();

						BitmapTextureAtlasTextureRegionFactory
								.setAssetBasePath("levels/level" + i + "/gfx/");
						tempTexture = new BitmapTextureAtlas(256, 128,
								TextureOptions.BILINEAR_PREMULTIPLYALPHA);
						tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
								.createFromAsset(tempTexture, this,
										levelCaption, 0, 0);
						mEngine.getTextureManager().loadTexture(tempTexture);
						UISprite us = null;
						if (i % 6 == 0){
							tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
									tempTextureRegion.getWidth() * 3),
									getCenter(CAMERA_HEIGHT, tempTextureRegion
											.getHeight() * 2 + 100),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(loadingScreen);
										currentScreenID = LOADING_SCREEN_ID;
										actualLevelIndex = index;
										Thread thread = new Thread(
												new LoadLevelThread());
										thread.start();
										// loadLevel(index);
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							us.width = 0.17f;
							us.height = 0.12f;
							us.leftMargin = 0.1225f;
							us.topMargin = 0.3f;
							us.SetProperties();
						}

						if (i % 6 == 3){
							tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
									tempTextureRegion.getWidth() * 3)
									+ tempTextureRegion.getWidth() * 2,
									getCenter(CAMERA_HEIGHT, tempTextureRegion
											.getHeight() * 2 + 100)
											+ 100
											+ tempTextureRegion.getHeight(),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(loadingScreen);
										currentScreenID = LOADING_SCREEN_ID;
										actualLevelIndex = index;
										Thread thread = new Thread(
												new LoadLevelThread());
										thread.start();
										// loadLevel(index);
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							us.width = 0.17f;
							us.height = 0.12f;
							us.leftMargin = 0.1225f;
							us.topMargin = 0.3f + 0.34f;
							us.SetProperties();
						}
						if (i % 6 == 1){
							tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
									tempTextureRegion.getWidth() * 3)
									+ tempTextureRegion.getWidth() * 2,
									getCenter(CAMERA_HEIGHT, tempTextureRegion
											.getHeight() * 2 + 100),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(loadingScreen);
										currentScreenID = LOADING_SCREEN_ID;
										actualLevelIndex = index;
										Thread thread = new Thread(
												new LoadLevelThread());
										thread.start();
										// loadLevel(index);
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							us.width = 0.17f;
							us.height = 0.12f;
							us.leftMargin = 2* 0.1225f + 0.17f;
							us.topMargin = 0.3f;
							us.SetProperties();
						}
						if (i % 6 == 2){
							tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
									tempTextureRegion.getWidth() * 3),
									getCenter(CAMERA_HEIGHT, tempTextureRegion
											.getHeight() * 2 + 100)
											+ 100
											+ tempTextureRegion.getHeight(),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(loadingScreen);
										currentScreenID = LOADING_SCREEN_ID;
										actualLevelIndex = index;
										Thread thread = new Thread(
												new LoadLevelThread());
										thread.start();
										// loadLevel(index);
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							us.width = 0.17f;
							us.height = 0.12f;
							us.leftMargin = 3*0.1225f + 2*0.17f;
							us.topMargin = 0.3f;
							us.SetProperties();
						}
						if (i % 6 == 4){
							tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
									tempTextureRegion.getWidth() * 3),
									getCenter(CAMERA_HEIGHT, tempTextureRegion
											.getHeight() * 2 + 100)
											+ 100
											+ tempTextureRegion.getHeight(),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(loadingScreen);
										currentScreenID = LOADING_SCREEN_ID;
										actualLevelIndex = index;
										Thread thread = new Thread(
												new LoadLevelThread());
										thread.start();
										// loadLevel(index);
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							us.width = 0.17f;
							us.height = 0.12f;
							us.leftMargin = 2*0.1225f + 0.17f;
							us.topMargin = 0.3f + 0.34f;
							us.SetProperties();
						}
						if (i % 6 == 5){
							tempSprite = new Sprite(getCenter(CAMERA_WIDTH,
									tempTextureRegion.getWidth() * 3),
									getCenter(CAMERA_HEIGHT, tempTextureRegion
											.getHeight() * 2 + 100)
											+ 100
											+ tempTextureRegion.getHeight(),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(loadingScreen);
										currentScreenID = LOADING_SCREEN_ID;
										actualLevelIndex = index;
										Thread thread = new Thread(
												new LoadLevelThread());
										thread.start();
										// loadLevel(index);
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							us.width = 0.17f;
							us.height = 0.12f;
							us.leftMargin = 3*0.1225f + 2*0.17f;
							us.topMargin = 0.3f + 0.34f;
							us.SetProperties();
						}
						tempLevel.add(us.sprite);

						BitmapTextureAtlasTextureRegionFactory
								.setAssetBasePath("");

						if (i + 1 == NUMBRE_OF_LEVELS) {
							if (i / 6 != 0) {
								tempTexture = new BitmapTextureAtlas(
										4096,
										2048,
										TextureOptions.BILINEAR_PREMULTIPLYALPHA);
								tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
										.createFromAsset(tempTexture, this,
												"gfx/backlevel.png", 0, 0);
								mEngine.getTextureManager().loadTexture(
										tempTexture);

								tempSprite = new Sprite(5, getCenter(
										CAMERA_HEIGHT, tempTextureRegion
												.getHeight()),
										tempTextureRegion) {
									@Override
									public boolean onAreaTouched(
											final TouchEvent t, final float x,
											final float y) {
										if (t.isActionDown()) {
											unloadScreen(chooseLevelScreen
													.get(index / 6));
											loadScreen(chooseLevelScreen
													.get(index / 6 - 1));
											return true;
										}
										return false;
									}
								};
								us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
								
								us.width = 0.05f;
								us.height = 0.14f;
								us.leftMargin = 0.04f;						
								us.SetProperties();
								us.SetVerticalAligmentCenter();
								tempLevel.add(us.sprite);
							}

							tempTexture = new BitmapTextureAtlas(1024, 512,
									TextureOptions.BILINEAR_PREMULTIPLYALPHA);
							tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
									.createFromAsset(tempTexture, this,
											"gfx/leveltitle.png", 0, 0);
							mEngine.getTextureManager()
									.loadTexture(tempTexture);

							tempSprite = new Sprite(10, 5, tempTextureRegion);
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							
							us.width = 0.4f;
							us.height = 0.23f;
							us.leftMargin = 0.05f;
							us.topMargin = 0.02f;		
							us.SetProperties();
							tempLevel.add(us.sprite);

							tempTexture = new BitmapTextureAtlas(1024, 512,
									TextureOptions.BILINEAR_PREMULTIPLYALPHA);
							tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
									.createFromAsset(tempTexture, this,
											"gfx/backarrow.png", 0, 0);
							mEngine.getTextureManager()
									.loadTexture(tempTexture);

							tempSprite = new Sprite(5, CAMERA_HEIGHT
									- tempTextureRegion.getHeight(),
									tempTextureRegion) {
								@Override
								public boolean onAreaTouched(
										final TouchEvent t, final float x,
										final float y) {
									if (t.isActionDown()) {
										unloadScreen(chooseLevelScreen
												.get(index / 6));
										loadScreen(mainScreen);
										currentScreenID = MAIN_SCREEN_ID;
										return true;
									}
									return false;
								}
							};
							us = new UISprite(CAMERA_WIDTH, CAMERA_HEIGHT, tempSprite);
							
							us.width = 0.1f;
							us.height = 0.15f;
							us.leftMargin = 0.01f;
							us.bottomMargin = 0.01f;		
							us.SetProperties();
							
							tempLevel.add(us.sprite);

							chooseLevelScreen.add(tempLevel);
						}

					} catch (Exception e) {

					}
				}
			}

		} catch (IOException e) {
			// handle
		}
	}

	class LoadLevelThread extends Thread {
		public void run() {
			loadLevel(actualLevelIndex);
		}
	}
	// Load the specified level
	private void loadLevel(int index) {
		textures = new ArrayList<ITexture>();
		isFinish = false;
		areas = new LinkedList<Shape>();
		oEntities = new LinkedList<MixedDataContainer>();
		actualWorld = new LevelWorld();
		actualLevelIndex = index;
		levelEntities = new ArrayList<IEntity>();

		AssetManager assetManager = getAssets();
		try {
			InputStream stream = assetManager.open("levels/level" + index
					+ "/world.xml");

			if (stream != null) {
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory
							.newInstance();

					dbf.setValidating(false);
					dbf.setIgnoringComments(false);
					dbf.setIgnoringElementContentWhitespace(true);
					dbf.setNamespaceAware(true);

					DocumentBuilder db = null;
					db = dbf.newDocumentBuilder();

					Document document = db.parse(stream);
					Element root = document.getDocumentElement();

					actualWorld.frontBackground = root
							.getAttribute("background-front");

					actualWorld.backBackground = new ArrayList<String>();
					NodeList backsNodeList = root
							.getElementsByTagName("backgrounds");
					if (backsNodeList.getLength() == 1) {
						Element carElement = (Element) backsNodeList.item(0);
						if (Integer.parseInt(carElement.getAttribute("length")) > 0) {
							NodeList entitiesItemsNodList = carElement
									.getElementsByTagName("background");
							int numEntities = entitiesItemsNodList.getLength();
							for (int i = 0; i < numEntities; i++) {
								Element entityElement = (Element) entitiesItemsNodList
										.item(i);
								actualWorld.backBackground.add(entityElement
										.getAttribute("name"));
							}
						}
					}

					actualWorld.width = Integer.parseInt(root
							.getAttribute("width"));

					actualWorld.height = Integer.parseInt(root
							.getAttribute("height"));

					actualWorld.index = index;

					NodeList carNodeList = root.getElementsByTagName("car");
					if (carNodeList.getLength() == 1) {
						Element carElement = (Element) carNodeList.item(0);
						this.oCar.x = Integer.parseInt(carElement
								.getAttribute("x"));
						this.oCar.y = Integer.parseInt(carElement
								.getAttribute("y"));
					}

					float x, y, width, height;
					NodeList finNode = root.getElementsByTagName("finish");
					if (finNode.getLength() == 1) {
						Element carElement = (Element) finNode.item(0);
						x = Float.parseFloat(carElement.getAttribute("x"));
						y = Float.parseFloat(carElement.getAttribute("y"));

						width = Float.parseFloat(carElement
								.getAttribute("width"));
						height = Float.parseFloat(carElement
								.getAttribute("height"));

						finish = new Rectangle(x, y, width, height);
					}

					NodeList deadNodeList = root
							.getElementsByTagName("deadareas");
					if (deadNodeList.getLength() == 1) {
						Element carElement = (Element) deadNodeList.item(0);
						if (Integer.parseInt(carElement.getAttribute("length")) > 0) {
							NodeList entitiesItemsNodList = carElement
									.getElementsByTagName("deadarea");
							int numEntities = entitiesItemsNodList.getLength();
							for (int i = 0; i < numEntities; i++) {
								Element entityElement = (Element) entitiesItemsNodList
										.item(i);

								x = Float.parseFloat(entityElement
										.getAttribute("x"));
								y = Float.parseFloat(entityElement
										.getAttribute("y"));
								width = Float.parseFloat(entityElement
										.getAttribute("width"));
								height = Float.parseFloat(entityElement
										.getAttribute("height"));

								areas.add(new Rectangle(x, y, width, height));
							}
						}
					}

					NodeList entitiesNodeList = root
							.getElementsByTagName("entities");
					if (entitiesNodeList.getLength() == 1) {
						Element entitiesElement = (Element) entitiesNodeList
								.item(0);
						NodeList entitiesItemsNodList = entitiesElement
								.getElementsByTagName("entity");
						int numEntities = entitiesItemsNodList.getLength();
						for (int i = 0; i < numEntities; i++) {
							Element entityElement = (Element) entitiesItemsNodList
									.item(i);
							/*
							 * NamedNodeMap entityAttributes =
							 * entityElement.getAttributes();
							 * Debug.d(entityAttributes.toString());
							 */
							MixedDataContainer entity = new MixedDataContainer();
							entity.x = Integer.parseInt(entityElement
									.getAttribute("x"));
							entity.y = Integer.parseInt(entityElement
									.getAttribute("y"));
							entity.sprite = entityElement
									.getAttribute("sprite");
							entity.shape = entityElement.getAttribute("shape");
							entity.visible = Boolean.getBoolean(entityElement
									.getAttribute("visible"));
							oEntities.add(entity);
						}
					}

					stream.close();

				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}

		generateLevelPhysics();
		generateLevelWord(actualWorld);
		generateSensors();
		generateLevelCar();

		this.enableAccelerometerSensor(this);
		mScene.sortChildren();

		levelWorldPhysics.setContactListener(new ContactListener() {

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}

			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beginContact(Contact contact) {
				if (contact.getFixtureA().isSensor()) {
					if (contact.getFixtureA().getBody().getUserData()
							.toString().equals("dead"))
						rest = true;
				}

				if (contact.getFixtureB().isSensor()) {
					if (contact.getFixtureB().getBody().getUserData()
							.toString().equals("dead"))
						rest = true;
				}
				if (contact.getFixtureA().isSensor()) {
					if (contact.getFixtureA().getBody().getUserData()
							.toString().equals("finish"))
						isFinish = true;
				}

				if (contact.getFixtureB().isSensor()) {
					if (contact.getFixtureB().getBody().getUserData()
							.toString().equals("finish"))
						isFinish = true;
				}
			}
		});

		generateUpdateHandler();
	}

	//Here i load the sprites of the actual level after the game is fully loaded
	private void LoadLevelSprites() {
		for (IEntity item : levelEntities) {
			mScene.attachChild(item);
		}

		cameraBound.setChaseEntity(cartShape);
		levelWorldPhysics.setGravity(new Vector2(0, 15));
		mScene.sortChildren();
	}
	
	//Here the sensors of finish and dead areas are created
	private void generateSensors() {
		FixtureDef d = new FixtureDef();
		d.isSensor = true;
		Body end = PhysicsFactory.createBoxBody(levelWorldPhysics, finish,
				BodyType.StaticBody, d);
		end.setUserData("finish");

		ArrayList<Body> deads = new ArrayList<Body>();

		for (Shape area : this.areas) {
			Body temp = PhysicsFactory.createBoxBody(levelWorldPhysics, area,
					BodyType.StaticBody, d);
			temp.setUserData("dead");
			deads.add(temp);
		}
	}

	// The next methods are pretty similar to the one you write this are the
	// box2d initialization
	private void generateLevelWord(LevelWorld w) {
		actualWorld = w;
		this.worldPhysicsEditorShapeLibrary = new PhysicsEditorShapeLibrary();
		this.worldPhysicsEditorShapeLibrary.open(this, "levels/level" + w.index
				+ "/shapes.xml");
		textures = new ArrayList<ITexture>();
		worldTextureRegionHashMap = new HashMap<String, TextureRegion>();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("levels/level"
				+ w.index + "/");
		BitmapTextureAtlasEx mTextureAtlasEx = new BitmapTextureAtlasEx(4096,
				2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		for (int i = 0; i < this.oEntities.size(); i++) {
			MixedDataContainer entity = this.oEntities.get(i);
			TextureRegion mTextureRegion = mTextureAtlasEx.appendTextureAsset(
					this, "gfx/" + entity.sprite);
			worldTextureRegionHashMap.put(entity.sprite, mTextureRegion);
		}
		textures.add(mTextureAtlasEx);
		this.mEngine.getTextureManager().loadTexture(mTextureAtlasEx);

		cameraBound.setBounds(0, w.width, 0, w.height);
		cameraBound.setBoundsEnabled(true);
		for (int i = 0; i < this.oEntities.size(); i++) {
			MixedDataContainer entity = this.oEntities.get(i);
			TextureRegion textureRegion = this.worldTextureRegionHashMap
					.get(entity.sprite);
			Sprite sprite = new Sprite(entity.x, entity.y, textureRegion);
			// Create physics body
			if (entity.visible == false)
				sprite.setVisible(false);
			Body body = this.worldPhysicsEditorShapeLibrary.createBody(
					entity.shape, sprite, levelWorldPhysics);
			levelEntities.add(sprite);
			// this.mScene.attachChild(sprite);
			levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
					sprite, body, true, true));
		}

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("levels/level"
				+ w.index + "/gfx/");

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(2048, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, this, w.frontBackground, 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		textures.add(tempTexture);

		levelBackground = new Sprite(0, 0, tempTextureRegion);
		
		levelBackground.setScaleCenter(0, 0);	
		float fx = w.width / levelBackground.getWidth();
		float fy = w.height / levelBackground.getHeight();
		levelBackground.setScale(fx, fy);
		bbackground = new ArrayList<Sprite>();
		for (String s : w.backBackground) {
			tempTexture = new BitmapTextureAtlas(2048, 512,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
					.createFromAsset(tempTexture, this, s, 0, 0);
			mEngine.getTextureManager().loadTexture(tempTexture);
			textures.add(tempTexture);
			bbackground.add(new Sprite(0, 0, tempTextureRegion));
		}
		
		levelEntities.add(levelBackground);		

		for (Sprite sp : bbackground) {
			levelEntities.add(sp);
			
			sp.setScaleCenter(0, 0);
			sp.setScale(fx, fy);
		}

		final Shape ground = new Rectangle(0, w.height, w.width * 2, 1);
		final Shape roof = new Rectangle(0, 0, w.width, 1);
		final Shape left = new Rectangle(-1, 0, 1, w.height);
		final Shape right = new Rectangle(w.width, 0, 1, w.height);
		PhysicsFactory.createBoxBody(levelWorldPhysics, ground,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(levelWorldPhysics, roof,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(levelWorldPhysics, left,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(levelWorldPhysics, right,
				BodyType.StaticBody, WALL_FIXTURE_DEF);

		levelEntities.add(ground);
		levelEntities.add(roof);
		levelEntities.add(left);
		levelEntities.add(right);
	}

	//The physics structure of the car is created here
	private void generateLevelCar() {
		// body
		pCar.mass = 5;
		pCar.I = 10;

		// wheels
		pWheel1.relativeX = -80;
		pWheel2.relativeX = 60;
		pWheel1.relativeY = 60;
		pWheel2.relativeY = 60;
		pWheel1.mass = 80;
		pWheel2.I = 5;
		pWheel1.mass = 80;
		pWheel2.I = 5;

		// springs
		pSpring1.relativeX = 0;
		pSpring2.relativeX = 0;
		pSpring1.lowerTranslation = -25.5f;
		pSpring2.lowerTranslation = -25.5f;
		pSpring1.upperTranslation = 15.0f;
		pSpring2.upperTranslation = 15.0f;

		Sprite sprite = new Sprite(0, 0, this.gameTextureRegionHashMap
				.get("car_body"));
		sprite.setPosition(oCar.x, oCar.y);
		cartShape = sprite;
		cartShape.setZIndex(10);

		this.cart = PhysicsFactory.createBoxBody(levelWorldPhysics, cartShape,
				BodyType.DynamicBody, ONLY_WALL_FIXTURE_DEF);
		
		MassData md = new MassData();
		md.I = pCar.I;
		md.mass = pCar.mass;

		this.cart.setMassData(md);
		levelEntities.add(cartShape);
		levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
				cartShape, cart, true, true));

		wheel1Face = new Sprite((cart.getWorldCenter()).x
				* PIXEL_TO_METER_RATIO_DEFAULT + pWheel1.relativeX, cartShape
				.getY()
				+ pWheel1.relativeY, this.gameTextureRegionHashMap
				.get("car_wheel"));
		wheel1Face.setZIndex(11);
		this.wheel1Body = PhysicsFactory.createCircleBody(levelWorldPhysics,
				wheel1Face, BodyType.DynamicBody, ONLY_WALL_FIXTURE_DEF);
		md = new MassData();
		md.I = 1;
		md.mass = 5;
		wheel1Body.setMassData(md);
		levelEntities.add(wheel1Face);

		wheel2Face = new Sprite((cart.getWorldCenter()).x
				* PIXEL_TO_METER_RATIO_DEFAULT + pWheel2.relativeX, cartShape
				.getY()
				+ pWheel2.relativeY, this.gameTextureRegionHashMap
				.get("car_wheel"));
		wheel2Face.setZIndex(11);
		this.wheel2Body = PhysicsFactory.createCircleBody(levelWorldPhysics,
				wheel2Face, BodyType.DynamicBody, ONLY_WALL_FIXTURE_DEF);
		wheel2Body.setLinearDamping(1);
		wheel2Body.setMassData(md);

		levelEntities.add(wheel2Face);

		axle1Face = new Sprite((cart.getWorldCenter()).x
				* PIXEL_TO_METER_RATIO_DEFAULT + pWheel1.relativeX, cartShape
				.getY()
				+ pWheel1.relativeY, this.gameTextureRegionHashMap
				.get("car_wheel"));
		axle1Face.setScale((float) 0.5);
		axle1Face.setVisible(false);
		this.axle1Body = PhysicsFactory.createCircleBody(levelWorldPhysics,
				axle1Face, BodyType.DynamicBody, NO_FIXTURE_DEF);
		levelEntities.add(axle1Face);
		levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
				axle1Face, axle1Body, true, true));
		axle2Face = new Sprite((cart.getWorldCenter()).x
				* PIXEL_TO_METER_RATIO_DEFAULT + pWheel2.relativeX, cartShape
				.getY()
				+ pWheel2.relativeY, this.gameTextureRegionHashMap
				.get("car_wheel"));
		axle2Face.setVisible(false);
		axle2Face.setScale((float) 0.5);

		this.axle2Body = PhysicsFactory.createCircleBody(levelWorldPhysics,
				axle2Face, BodyType.DynamicBody, NO_FIXTURE_DEF);

		levelEntities.add(axle2Face);
		levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
				axle2Face, axle2Body, true, true));
		// linje 1
		connectionLine1 = new Line(10, 10, 11, 11);
		connectionLine1.setColor(0 / 255f, 0f / 255f, 0f / 255f);
		connectionLine1.setLineWidth(4.0f);
		connectionLine1.setZIndex(9);

		levelEntities.add(connectionLine1);
		levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
				wheel1Face, wheel1Body, true, true));

		// linje 2
		connectionLine2 = new Line(10, 10, 11, 11);
		connectionLine2.setColor(0 / 255f, 0f / 255f, 0f / 255f);
		connectionLine2.setLineWidth(4.0f);
		connectionLine2.setZIndex(9);
		levelEntities.add(connectionLine2);

		levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
				wheel2Face, wheel2Body, true, true));

		// spring 1
		spring1 = new PrismaticJointDef();
		Float a1 = (float) (-(Math.cos(Math.PI / 3)));
		Float a2 = (float) ((Math.sin(Math.PI / 3)));
		spring1.initialize(cart, axle1Body, new Vector2(cart.getWorldCenter().x
				+ pSpring1.relativeX / PIXEL_TO_METER_RATIO_DEFAULT, cart
				.getWorldCenter().y), new Vector2(a1, a2));
		spring1.lowerTranslation = (float) pSpring1.lowerTranslation
				/ PIXEL_TO_METER_RATIO_DEFAULT;
		spring1.upperTranslation = (float) pSpring1.upperTranslation
				/ PIXEL_TO_METER_RATIO_DEFAULT;
		
		spring1.enableLimit = true;
		spring1.enableMotor = true;
		spring1.motorSpeed = 10;
		spring1.maxMotorForce = 20;
		mSpring1 = (PrismaticJoint) levelWorldPhysics.createJoint(spring1);

		// spring 2
		spring2 = new PrismaticJointDef();
		a1 = (float) ((Math.cos(Math.PI / 3)));
		a2 = (float) ((Math.sin(Math.PI / 3)));
		spring2.initialize(cart, axle2Body, new Vector2(cart.getWorldCenter().x
				+ pSpring2.relativeX / PIXEL_TO_METER_RATIO_DEFAULT, cart
				.getWorldCenter().y), new Vector2(a1, a2));
		
		spring2.lowerTranslation = (float) pSpring2.lowerTranslation
				/ PIXEL_TO_METER_RATIO_DEFAULT;
		spring2.upperTranslation = (float) pSpring2.upperTranslation
				/ PIXEL_TO_METER_RATIO_DEFAULT;
		spring2.enableLimit = true;
		spring2.enableMotor = true;
		spring2.motorSpeed = 10;
		spring2.maxMotorForce = 20;
		mSpring2 = (PrismaticJoint) levelWorldPhysics.createJoint(spring2);

		// motor 1
		this.motor1 = new RevoluteJointDef();
		motor1.initialize(axle1Body, wheel1Body, wheel1Body.getWorldCenter());
		motor1.enableMotor = true;
		motor1.motorSpeed = 1;
		motor1.maxMotorTorque = 200;
		this.mMotor1 = (RevoluteJoint) levelWorldPhysics.createJoint(motor1);
		// motor 2
		RevoluteJointDef motor2 = new RevoluteJointDef();
		motor2.initialize(axle2Body, wheel2Body, wheel2Body.getWorldCenter());
		motor2.enableMotor = false;
		motor2.motorSpeed = 1;
		motor2.maxMotorTorque = 200;
		this.mMotor2 = (RevoluteJoint) levelWorldPhysics.createJoint(motor2);

	}

	private void generateLevelPhysics() {
		levelWorldPhysics = new FixedStepPhysicsWorld(100, new Vector2(0,
				SensorManager.GRAVITY_DEATH_STAR_I), false, 60, 60);
		levelWorldPhysics.setGravity(new Vector2(0, 0));
	}

	//Steps for close a new level
	private void CloseLevel() {
		//The accelerometer is disable
		disableAccelerometerSensor();
		//The updaters are unregistered 
		mScene.unregisterUpdateHandler(levelWorldPhysics);
		mEngine.unregisterUpdateHandler(upHand);

		
		final Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();
		//The camera is reseted
		cameraBound.setChaseEntity(null);
		cameraBound.updateChaseEntity();
		cameraBound.reset();
		cameraBound.setCenter(CAMERA_WIDTH / 2.0f, CAMERA_HEIGHT / 2.0f);

		//The levelStarted flag is set to false
		levelStarted = false;
		//The sound is stoped
		engineSound.stop();
		//All the level entities are detached from the scene
		//The textures are unloaded
		for (IEntity item : levelEntities) {
			mEngine.getTextureManager().unloadTexture(
					(ITexture) item.getUserData());
			mScene.detachChild(item);
		}

		//The cart joints are destroyed
		levelWorldPhysics.destroyJoint(mMotor1);
		levelWorldPhysics.destroyJoint(mMotor2);
		levelWorldPhysics.destroyJoint(mSpring1);
		levelWorldPhysics.destroyJoint(mSpring2);

		ArrayList<Body> lista = new ArrayList<Body>();
		//Now i query for Body in the physics world
		//and them their are destroyed
		Iterator<Body> bodyIterator = levelWorldPhysics.getBodies();
		while (bodyIterator.hasNext()) {
			lista.add(bodyIterator.next());
		}
		for (Body body : lista) {
			levelWorldPhysics.destroyBody(body);
		}

		//The connectors are also destroyed
		PhysicsConnector[] contents = new PhysicsConnector[levelWorldPhysics
				.getPhysicsConnectorManager().size()];
		levelWorldPhysics.getPhysicsConnectorManager().toArray(contents);

		for (PhysicsConnector physicsConnector : contents) {
			levelWorldPhysics.unregisterPhysicsConnector(physicsConnector);
		}
		pressedLeft = pressedRight = false;

		for (ITexture t : textures) {
			mEngine.getTextureManager().unloadTexture(t);
		}

		//The world is reseted
		levelWorldPhysics.reset();
	}

	private void Restart() {
		//On restart the level is closed
		CloseLevel();
		preUpdates = 0;
		//Loading screen is loaded		
		loadScreen(loadingScreen);
		currentScreenID = LOADING_SCREEN_ID;
		//The world is loaded in another thread
		Thread thread = new Thread(new LoadLevelThread());
		thread.start();
		mScene.sortChildren();
	}

    @Override
    protected int getLayoutID() {
        return R.layout.main;
    }

    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.xmllayoutRenderSurfaceView;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && currentScreenID == NOSCREEN) {
            Log.d("MP", "MENU pressed");
            try {
                GamePause();
            } catch (Exception e) {

                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void GamePause() {
        isPaused = true;

        pressedLeft = pressedRight = false;
        engineSound.stop();

        mScene.unregisterUpdateHandler(levelWorldPhysics);
        mEngine.unregisterUpdateHandler(upHand);

        sp.ArrangeElements();
        // Bringing the controls to the center of the camera
        pauseScreen.get(0).setPosition(
                    cameraBound.getMinX() + pauseScreen.get(0).getX(),
                    cameraBound.getMinY() + +pauseScreen.get(0).getY());

        pauseScreen.get(1).setPosition(
                    cameraBound.getMinX() + pauseScreen.get(1).getX(),
                    cameraBound.getMinY() + pauseScreen.get(1).getY());

        pauseScreen.get(2).setPosition(
                    cameraBound.getMinX() + pauseScreen.get(2).getX(),
                    cameraBound.getMinY() + pauseScreen.get(2).getY());

        // Loading pause Screen
        loadScreen(pauseScreen);
        currentScreenID = PAUSE_SCREEN_ID;
    }
    
    @Override
	public void onBackPressed() {
		   switch (currentScreenID) {
		case OPTION_SCREEN_ID:
			unloadScreen(optionScreen);
			loadScreen(mainScreen);
			currentScreenID = MAIN_SCREEN_ID;
			break;
		case HELP_SCREEN_ID:
			unloadScreen(helpScreen);
			mScene.setBackground(new SpriteBackground(mainBackground));
			loadScreen(mainScreen);
			currentScreenID = MAIN_SCREEN_ID;
			break;
		case ABOUT_SCREEN_ID: 
			unloadScreen(aboutScreen);
			loadScreen(mainScreen);
			currentScreenID = MAIN_SCREEN_ID;
			break;
		//case CHOOSELEVEL_SCREEN_ID:
		case PAUSE_SCREEN_ID:
			unloadScreen(pauseScreen); 
			generateUpdateHandler();
			isPaused = false;
			currentScreenID = NOSCREEN;
			break;
		case MAIN_SCREEN_ID:
			finish();
		case NOSCREEN:
			GamePause();
		default:
			break;
		}
	}

	@Override
	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
		float tilt = pAccelerometerData.getX() / 4;
		if (tilt > 1)
			tilt = 1;
		if (tilt < -1)
			tilt = -1;

		if (cart != null && levelStarted)
			cart.applyTorque(tilt * 900 * tiltAlpha);
	}
}
