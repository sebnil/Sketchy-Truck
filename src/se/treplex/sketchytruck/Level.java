package se.treplex.sketchytruck;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.view.Display;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class Level {

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f);
	private static final FixtureDef FIXTURE_DEF2 = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f);

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

	public LevelWorld actualWorld;
	public LinkedList<MixedDataContainer> entities;
	public PhysicsWorld levelWorldPhysics;

	// shapes
	private PhysicsEditorShapeLibrary worldPhysicsEditorShapeLibrary;
	private PhysicsEditorShapeLibrary gamePhysicsEditorShapeLibrary;

	public HashMap<String, TextureRegion> worldTextureRegionHashMap = new HashMap<String, TextureRegion>();
	public HashMap<String, TextureRegion> gameTextureRegionHashMap = new HashMap<String, TextureRegion>();
	public RevoluteJointDef motor1;
	public RevoluteJoint mMotor1;
	public RevoluteJoint mMotor2;
	public PrismaticJointDef spring1;
	public PrismaticJointDef spring2;
	public PrismaticJoint mSpring1;
	public PrismaticJoint mSpring2;
	public Body cart;
	public Shape cartShape;
	public Body wheel1Body;
	public Body wheel2Body;
	public Body axle1Body;
	public Body axle2Body;
	public Line connectionLine1;
	public Line connectionLine2;
	public List<MixedDataContainer> oEntities = new LinkedList<MixedDataContainer>();
	public LinkedList<Shape> areas = new LinkedList<Shape>();
	public Shape finish;
	// car parameters
	public MixedDataContainer oCar = new MixedDataContainer();
	public MixedDataContainer pCar = new MixedDataContainer();
	public MixedDataContainer pWheel1 = new MixedDataContainer();
	public MixedDataContainer pWheel2 = new MixedDataContainer();
	public MixedDataContainer pSpring1 = new MixedDataContainer();
	public MixedDataContainer pSpring2 = new MixedDataContainer();
	public Sprite wheel1Face;
	public Sprite wheel2Face;
	public Sprite axle1Face;
	public Sprite axle2Face;
	private LinkedList<IEntity> level;

	public void LoadInfo(InputStream stream) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			dbf.setValidating(false);
			dbf.setIgnoringComments(false);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setNamespaceAware(true);

			DocumentBuilder db = null;
			db = dbf.newDocumentBuilder();

			Document document = db.parse(stream);
			Element root = document.getDocumentElement();

			actualWorld.frontBackground = root.getAttribute("background-front");

			actualWorld.width = Integer.parseInt(root.getAttribute("width"));

			actualWorld.height = Integer.parseInt(root.getAttribute("height"));

			NodeList carNodeList = root.getElementsByTagName("car");
			if (carNodeList.getLength() == 1) {
				Element carElement = (Element) carNodeList.item(0);
				this.oCar.x = Integer.parseInt(carElement.getAttribute("x"));
				this.oCar.y = Integer.parseInt(carElement.getAttribute("y"));
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

			NodeList deadNodeList = root.getElementsByTagName("deadareas");
			if (deadNodeList.getLength() == 1) {
				Element carElement = (Element) deadNodeList.item(0);
				int l = Integer.parseInt(carElement.getAttribute("length"));
				if (l > 0) {
					NodeList entitiesItemsNodList = carElement
							.getElementsByTagName("deadarea");
					int numEntities = entitiesItemsNodList.getLength();
					for (int i = 0; i < numEntities; i++) {
						Element entityElement = (Element) entitiesItemsNodList
								.item(i);

						x = Integer.parseInt(entityElement
								.getAttribute("x"));
						y = Integer.parseInt(entityElement
								.getAttribute("y"));
						width = Integer.parseInt(entityElement
								.getAttribute("width"));
						height = Integer.parseInt(entityElement
								.getAttribute("height"));

						areas.add(new Rectangle(x, y, width, height));
					}
				}
			}

			NodeList entitiesNodeList = root.getElementsByTagName("entities");
			if (entitiesNodeList.getLength() == 1) {
				Element entitiesElement = (Element) entitiesNodeList.item(0);
				NodeList entitiesItemsNodList = entitiesElement
						.getElementsByTagName("entity");
				int numEntities = entitiesItemsNodList.getLength();
				for (int i = 0; i < numEntities; i++) {
					Element entityElement = (Element) entitiesItemsNodList
							.item(i);
					
					MixedDataContainer entity = new MixedDataContainer();
					entity.x = Integer
							.parseInt(entityElement.getAttribute("x"));
					entity.y = Integer
							.parseInt(entityElement.getAttribute("y"));
					entity.sprite = entityElement.getAttribute("sprite");
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

	private void generateLevelWord(LevelWorld w, IEntity mScene, GameActivity context, BaseGameActivity mEngine, BoundCamera cameraBound) {
		actualWorld = w;
		this.worldPhysicsEditorShapeLibrary = new PhysicsEditorShapeLibrary();
		this.worldPhysicsEditorShapeLibrary.open(context, "levels/level" + w.index
				+ "/shapes.xml");

		worldTextureRegionHashMap = new HashMap<String, TextureRegion>();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("levels/level"
				+ w.index + "/");
		BitmapTextureAtlasEx mTextureAtlasEx = new BitmapTextureAtlasEx(4096,
				2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		for (int i = 0; i < this.oEntities.size(); i++) {
			MixedDataContainer entity = this.oEntities.get(i);
			TextureRegion mTextureRegion = mTextureAtlasEx.appendTextureAsset(
					context, "gfx/" + entity.sprite);
			worldTextureRegionHashMap.put(entity.sprite, mTextureRegion);
		}
		mEngine.getTextureManager().loadTexture(mTextureAtlasEx);

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
			level.add(sprite);
			mScene.attachChild(sprite);
			levelWorldPhysics.registerPhysicsConnector(new PhysicsConnector(
					sprite, body, true, true));
		}

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("levels/level"
				+ w.index + "/gfx/");

		BitmapTextureAtlas tempTexture = new BitmapTextureAtlas(4096, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion tempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(tempTexture, context, w.frontBackground, 0, 0);
		mEngine.getTextureManager().loadTexture(tempTexture);

		Sprite background = new Sprite(0, 0, tempTextureRegion);

		level.add(background);
		mScene.attachChild(background);

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

		mScene.attachChild(ground);
		mScene.attachChild(roof);
		mScene.attachChild(left);
		mScene.attachChild(right);
		
		level.add(ground);
		level.add(roof);
		level.add(left);
		level.add(right);
	}
}
