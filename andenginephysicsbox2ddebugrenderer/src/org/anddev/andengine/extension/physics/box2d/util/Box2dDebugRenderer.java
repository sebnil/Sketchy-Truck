package org.anddev.andengine.extension.physics.box2d.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;

import android.graphics.Color;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Box2dDebugRenderer extends Entity {

	private PhysicsWorld world;
	
	private Queue<Body> toRemove = new LinkedList<Body>();
	private HashMap<Body, ArrayList<Shape>> bodies = new HashMap<Body, ArrayList<Shape>>();
	private HashMap<Body, Boolean> isBodyActiveMap = new HashMap<Body, Boolean>();
    private HashMap<Shape, Boolean> isSensorMap = new HashMap<Shape, Boolean>();
	
	private float colorAlpha = 0.35f;
	private int activeBodyColor = Color.rgb(255, 0, 0);
	private int sensorBodyColor = Color.rgb(55, 55, 255);
	private int inactiveBodyColor = Color.rgb(180, 180, 180);
	
	public Box2dDebugRenderer(PhysicsWorld world) {
		this.world = world;
	}
	
	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		float ptm = PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT;
		
        for (Body body : bodies.keySet()) {
            isBodyActiveMap.put(body, false);
        }
        
		Iterator<Body> iterator = world.getBodies();
		while(iterator.hasNext()) {
			Body body = iterator.next();
			if(!bodies.containsKey(body)) addBodyToShapeCollection(body);
            isBodyActiveMap.put(body, true);
			
            // update shapes
			ArrayList<Shape> shapes = bodies.get(body);
			int shapesSize = shapes.size();
			for(int i = 0; i < shapesSize; i++) {
				Shape shape = shapes.get(i);
				
				shape.setAlpha(colorAlpha);
				if(body.isAwake()) shape.setColor(Color.red(activeBodyColor), Color.green(activeBodyColor), Color.blue(activeBodyColor));
				if(isSensorMap.containsKey(shape)) shape.setColor(Color.red(sensorBodyColor), Color.green(sensorBodyColor), Color.blue(sensorBodyColor));
				else shape.setColor(Color.red(inactiveBodyColor), Color.green(inactiveBodyColor), Color.blue(inactiveBodyColor));
				
				shape.setRotationCenter(body.getMassData().center.x * ptm, body.getMassData().center.y * ptm);
				shape.setRotation((float) (body.getAngle() * (180 / Math.PI)));
				shape.setPosition(body.getPosition().x * ptm, body.getPosition().y * ptm);
			}
		}

		// remove all shapes of bodies that are no longer in the world
        for (Body body : bodies.keySet()) {
            if(isBodyActiveMap.get(body) == null || isBodyActiveMap.get(body) == false) {
                ArrayList<Shape> shapes = bodies.get(body);
                for(Shape shape : shapes) {
                    this.detachChild(shape);
                }
                toRemove.add(body);
            }
        }
        
        // remove all bodies that are no longer in the world
        for(Body body : toRemove) {
            bodies.remove(body);
        }
        toRemove.clear();
	}
	
	private void addBodyToShapeCollection(Body body) {
		bodies.put(body, new ArrayList<Shape>());
		isBodyActiveMap.put(body, false);
		
		for(Fixture fixture : body.getFixtureList()) {
			Shape debugShape = null;
			if(fixture.getShape() instanceof PolygonShape) {
    			debugShape = createPolygonShape(fixture);
			} else if(fixture.getShape() instanceof CircleShape){
				debugShape = createCircleShape(fixture);
			}
			
			if(debugShape != null) {
    			bodies.get(body).add(debugShape);
    			if(fixture.isSensor()) isSensorMap.put(debugShape, true);
    			this.attachChild(debugShape);
			}
		}
	}

    private Shape createCircleShape(Fixture fixture) {
    	CircleShape fixtureShape = (CircleShape) fixture.getShape();
    	Vector2 position = fixtureShape.getPosition();
    	Ellipse ellipse = new Ellipse(position.x, position.y, fixtureShape.getRadius()*PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT);
    	return ellipse;
    }
	
	private Shape createPolygonShape(Fixture fixture) {
		PolygonShape fixtureShape = (PolygonShape) fixture.getShape();
		if(fixtureShape == null) return null;
		ArrayList<Float> xPoints = new ArrayList<Float>();
		ArrayList<Float> yPoints = new ArrayList<Float>();
		Vector2 vertex = new Vector2();
		
		for(int i = 0; i < fixtureShape.getVertexCount(); i++) {
			fixtureShape.getVertex(i, vertex);
			xPoints.add(vertex.x * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT);
			yPoints.add(vertex.y * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT);
		}
		
		return new Polyline(xPoints, yPoints);
	}

	public void setActiveBodyColor(int red, int green, int blue) {
		activeBodyColor = Color.rgb(red, green, blue);
	}
	public void setSensorBodyColor(int red, int green, int blue) {
		sensorBodyColor = Color.rgb(red, green, blue);
	}
	public void setInactiveBodyColor(int red, int green, int blue) {
		inactiveBodyColor = Color.rgb(red, green, blue);
	}

	public int getActiveBodyColor() {
		return activeBodyColor;
	}

	public int getSensorBodyColor() {
		return sensorBodyColor;
	}

	public int getInactiveBodyColor() {
		return inactiveBodyColor;
	}
	
}
