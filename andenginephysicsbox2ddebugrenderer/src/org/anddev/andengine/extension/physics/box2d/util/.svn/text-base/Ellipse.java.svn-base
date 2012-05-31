package org.anddev.andengine.extension.physics.box2d.util;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.collision.RectangularShapeCollisionChecker;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.util.GLHelper;

public class Ellipse extends Shape {
	private static final float LINEWIDTH_DEFAULT = 1.0f;
	private static final int SEGMENTS_DEFAULT = 50;
	private final EllipseVertexBuffer vertexBuffer;
	private int filledMode;
	private int segments;
	private float lineWidth;
	private float height;
	private float width;

	private Rectangle collisionRectangle;

	public Ellipse(float pX, float pY, float radius) {
		this(pX, pY, radius, radius);
	}

	public Ellipse(float pX, float pY, float radius, boolean filled) {
		this(pX, pY, radius, LINEWIDTH_DEFAULT, filled, SEGMENTS_DEFAULT);
	}

	public Ellipse(float pX, float pY, float radius, float lineWidth,
			boolean filled) {
		this(pX, pY, radius, radius, lineWidth, filled, SEGMENTS_DEFAULT);
	}

	public Ellipse(float pX, float pY, float radius, float lineWidth,
			boolean filled, int segments) {
		this(pX, pY, radius, radius, lineWidth, filled, segments);
	}

	public Ellipse(float pX, float pY, float radius, int segments) {
		this(pX, pY, radius, LINEWIDTH_DEFAULT, false, segments);
	}

	public Ellipse(float pX, float pY, float width, float height) {
		this(pX, pY, width, height, LINEWIDTH_DEFAULT, false, SEGMENTS_DEFAULT);
	}

	public Ellipse(int pX, int pY, int radius, float lineWidth, int segments) {
		this(pX, pY, radius, lineWidth, false, segments);
	}

	public Ellipse(float pX, float pY, float width, float height,
			float lineWidth, boolean filled, int segments) {
		super(pX, pY);
		this.width = width;
		this.height = height;
		this.filledMode = (filled) ? GL10.GL_TRIANGLE_FAN : GL10.GL_LINE_LOOP;
		this.segments = segments;
		this.lineWidth = lineWidth;

		collisionRectangle = new Rectangle(-width, -height, width * 2, height * 2);
		collisionRectangle.setVisible(false);
		collisionRectangle.setIgnoreUpdate(true);
		attachChild(collisionRectangle);

		vertexBuffer = new EllipseVertexBuffer(segments, GL11.GL_STATIC_DRAW);
		BufferObjectManager.getActiveInstance().loadBufferObject(vertexBuffer);
		this.updateVertexBuffer();
	}

	@Override
	public float[] getSceneCenterCoordinates() {
		return this.convertLocalToSceneCoordinates(this.width * 0.5f, this.height * 0.5f);
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getBaseWidth() {
		return width;
	}

	@Override
	public float getBaseHeight() {
		return height;
	}

	@Override
	public boolean collidesWith(IShape pOtherShape) {
		// unsupported
		return false;
	}

	@Override
	public boolean contains(float pX, float pY) {
		return RectangularShapeCollisionChecker.checkContains(collisionRectangle, pX, pY); // TODO
																						   // Ellipse
																						   // Collision
	}

	@Override
	protected void onUpdateVertexBuffer() {
		vertexBuffer.update(segments, getWidth(), getHeight(), filledMode);
	}

	@Override
	public EllipseVertexBuffer getVertexBuffer() { // was protected
		return vertexBuffer;
	}

	@Override
	protected boolean isCulled(Camera pCamera) {
		return false;
	}

	@Override
	protected void onInitDraw(final GL10 pGL) {
		super.onInitDraw(pGL);
		GLHelper.disableTextures(pGL);
		GLHelper.disableTexCoordArray(pGL);
		// enable for nicer lines, at the expense of a limited linewidth of 1
		// pGL.glEnable(GL10.GL_LINE_SMOOTH);
		GLHelper.lineWidth(pGL, lineWidth);
	}

	@Override
	protected void drawVertices(GL10 gl, Camera pCamera) {
		gl.glDrawArrays(filledMode, 0, segments);
	}

	public float getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}

	public void setHeight(float height) {
		this.height = height;
		this.collisionRectangle.setHeight(height);
		this.updateVertexBuffer();
	}

	public void setWidth(float width) {
		this.width = width;
		this.collisionRectangle.setWidth(width);
		this.updateVertexBuffer();
	}

}