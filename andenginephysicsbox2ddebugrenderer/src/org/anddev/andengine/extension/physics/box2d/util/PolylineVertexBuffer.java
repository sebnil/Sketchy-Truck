package org.anddev.andengine.extension.physics.box2d.util;

import java.util.ArrayList;

import org.anddev.andengine.opengl.util.FastFloatBuffer;
import org.anddev.andengine.opengl.vertex.VertexBuffer;

public class PolylineVertexBuffer extends VertexBuffer {
	public PolylineVertexBuffer(final int vertexCount, final int pDrawType) {
		super(2 * vertexCount, pDrawType, true);
	}

	public synchronized void update(final ArrayList<Float> pX, final ArrayList<Float> pY) {
		final int[] bufferData = this.mBufferData;

		for(int i=0; i<bufferData.length/2; i++) {
			bufferData[2*i] = Float.floatToRawIntBits(pX.get(i));
			bufferData[2*i+1] = Float.floatToRawIntBits(pY.get(i));
		}
		
		final FastFloatBuffer buffer = this.getFloatBuffer();
		buffer.position(0);
		buffer.put(bufferData);
		buffer.position(0);

		super.setHardwareBufferNeedsUpdate();
	}
}
