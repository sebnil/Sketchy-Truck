package se.treplex.sketchytruck;

import java.util.ArrayList;

public abstract class Panel extends UIElement {
	
	public Panel(int containerWidth, int containerHeight) {
		super(containerWidth, containerHeight);
		elements = new ArrayList<UIElement>();
	}

	ArrayList<UIElement> elements;
	
	public int cameraWidth, cameraHeight; 
		
	public abstract void ArrangeElements();
	
	public void AddElement(UIElement element){
		elements.add(element);
		ArrangeElements();
	}
}
