/**
 * @author Sebastian Nilsson
 * project url: http://sebastiannilsson.com/projekt/sketchy-truck/behover-hjalp-2d-bilspel-i-android/
 */

package se.treplex.sketchytruck;

public class StackPanel extends Panel {
	
	public float elementsMargin = 0.03f;
	
	public StackPanel(int containerWidth, int containerHeight) {
		super(containerWidth, containerHeight);
	}

	@Override
	public void ArrangeElements() {
		float total = 0;
		float maxWidth = 0;
		for (UIElement element : elements) {		
			total += (element.actualHeight+ containerHeight * elementsMargin);
			maxWidth = Math.max(maxWidth, element.actualWidth);
		}
		total -= (containerHeight * elementsMargin);
		
		actualWidth = maxWidth;
		actualHeight = total;
		SetHorizontalAligmentCenter();
		SetVerticalAligmentCenter();
		total = actualY;
		
		for (UIElement element : elements) {
			element.SetPosition(actualX, total);
			total += (element.actualHeight+ containerHeight * elementsMargin);
		}
	}

	@Override
	public void SetPosition(float x, float y) {
		actualX = x;
		actualY = y;
	}
}
