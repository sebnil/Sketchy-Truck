package org.anddev.andengine.extension.physics.box2d.util;

import java.util.ArrayList;
import java.util.Collections;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.collision.ShapeCollisionChecker;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.util.GLHelper;
import org.anddev.andengine.opengl.vertex.VertexBuffer;

public class Polyline extends Shape {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final float LINEWIDTH_DEFAULT = 1.0f;
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	protected ArrayList<Float> mX;
	protected ArrayList<Float> mY;
	
	private int vertexCount = 0;

	private float mLineWidth;

	private final PolylineVertexBuffer mPolylineVertexBuffer;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	public Polyline(final ArrayList<Float> pX, final ArrayList<Float> pY) {
		this(pX, pY, LINEWIDTH_DEFAULT);
	}

	public Polyline(final ArrayList<Float> pX, final ArrayList<Float> pY, final float pLineWidth) {
		super(pX.get(0), pY.get(0));

		if (pX.size() != pY.size())
			throw new IllegalArgumentException("pX and pY arrays must by of equal size!");
		
		vertexCount = pX.size();
		
		mX = new ArrayList<Float>(pX);
		mY = new ArrayList<Float>(pY);
		
		this.mLineWidth = pLineWidth;

		this.mPolylineVertexBuffer = new PolylineVertexBuffer(vertexCount, GL11.GL_STATIC_DRAW);
		BufferObjectManager.getActiveInstance().loadBufferObject(this.mPolylineVertexBuffer);
		this.updateVertexBuffer();

		final float width = this.getWidth();
		final float height = this.getHeight();

		this.mRotationCenterX = width * 0.5f;
		this.mRotationCenterY = height * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public float getLineWidth() {
		return this.mLineWidth;
	}

	public void setLineWidth(final float pLineWidth) {
		this.mLineWidth = pLineWidth;
	}
	
	@Override
	public float getWidth() {
		ArrayList<Float> tempX = new ArrayList<Float>(mX);
		Collections.sort(tempX);
		return Math.abs(tempX.get(0) - tempX.get(tempX.size() - 1));
	}

	@Override
	public float getHeight() {
		ArrayList<Float> tempY = new ArrayList<Float>(mY);		
		Collections.sort(tempY);
		return Math.abs(tempY.get(0) - tempY.get(tempY.size() - 1));
	}

	@Override
	public float getBaseWidth() {
		return getWidth(); //TODO
	}

	@Override
	public float getBaseHeight() {
		return getHeight(); //TODO
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	protected boolean isCulled(Camera pCamera) {
		return false; //TODO
	}
	
	@Override
	protected void onInitDraw(final GL10 pGL) {
		super.onInitDraw(pGL);
		GLHelper.disableTextures(pGL);
		GLHelper.disableTexCoordArray(pGL);
		GLHelper.lineWidth(pGL, this.mLineWidth);
	}
	
	@Override
	protected VertexBuffer getVertexBuffer() {
		return mPolylineVertexBuffer;
	}
	
	@Override
	protected void onUpdateVertexBuffer() {
		this.mPolylineVertexBuffer.update(mX, mY);
	}
	
	@Override
	protected void drawVertices(GL10 pGL, Camera pCamera) {
		pGL.glDrawArrays(GL10.GL_LINE_LOOP, 0, vertexCount);
	}
	
	/*
	@Override
	public float[] getSceneCenterCoordinates() {
		return ShapeCollisionChecker.convertLocalToSceneCoordinates(this, getWidth() * 0.5f, getHeight() * 0.5f);
	}*/

	@Override
	public void reset() {
		super.reset();

		final float baseWidth = this.getBaseWidth();
		final float baseHeight = this.getBaseHeight();

		this.mRotationCenterX = baseWidth * 0.5f;
		this.mRotationCenterY = baseHeight * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;
	}
	
	/////////
	// STUBS
	/////////
	
	@Override
	public boolean collidesWith(IShape pOtherShape) {
		return false; //TODO
	}

	@Override
	public boolean contains(float pX, float pY) {
		return false; //TODO
	}

	@Override
	public float[] convertSceneToLocalCoordinates(float pX, float pY) {
		return null; //TODO
	}

	@Override
	public float[] convertLocalToSceneCoordinates(float pX, float pY) {
		return null; //TODO
	}
}
