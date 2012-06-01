/**
 * @author Sebastian Nilsson
 * project url: http://sebastiannilsson.com/projekt/sketchy-truck/behover-hjalp-2d-bilspel-i-android/
 */

package se.treplex.sketchytruck;

public abstract class UIElement {
	public float leftMargin = -1;
	public float rightMargin = -1;
	public float topMargin = -1;
	public float bottomMargin = -1;

	public float width = -1;
	public float height = -1;

	public float actualX, actualY;
	public float actualWidth, actualHeight;

	public int containerWidth, containerHeight;

	public UIElement(int containerWidth, int containerHeight) {
		this.containerHeight = containerHeight;
		this.containerWidth = containerWidth;
	}

	public void SetVerticalAligmentCenter() {
		actualY = (containerHeight - actualHeight) / 2f;
		SetPosition(actualX, actualY);
	}

	public void SetHorizontalAligmentCenter() {
		actualX = (containerWidth - actualWidth) / 2f;
		SetPosition(actualX, actualY);
	}

	public void Translate(float x, float y) {

		actualX = actualX + x;
		actualY = actualY + y;
	}
	
	public void SetProperties(){
		SetSize();
		SetPosition();		
	}
	
	public abstract void SetPosition(float x, float y);

	public void SetPosition() {
		float tx = 0, ty = 0;

		if (bottomMargin != -1) {
			ty = containerHeight - bottomMargin * containerHeight - actualHeight;
		}

		if (rightMargin != -1) {
			tx = containerWidth - rightMargin * containerWidth - actualWidth;
		}

		if (topMargin != -1) {
			ty = topMargin * containerHeight;
		}

		if (leftMargin != -1) {
			tx = leftMargin * containerWidth;
		}

		actualX = tx;
		actualY = ty;
		
		SetPosition(tx, ty);
	}

	public void SetSize() {

		actualWidth = containerWidth * width;
		actualHeight = containerHeight * height;

	}
}
