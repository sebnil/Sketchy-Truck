package se.treplex.sketchytruck;

import org.anddev.andengine.entity.sprite.Sprite;

public class UISprite extends UIElement {

	Sprite sprite;
	public float sx = 1, sy = 1;
	
	public UISprite(int containerWidth, int containerHeight, Sprite sprite) {
		super(containerWidth, containerHeight);
 
		this.sprite = sprite;		
		this.sprite.setPosition(actualX, actualY);
		SetScale();
	}
	
	@Override
	public void SetProperties(){		
		super.SetProperties();
		SetScale();
	}

	public void SetScale(){
		sprite.setScaleCenter(0, 0);
				
		if(width != -1){
			sx = actualWidth / sprite.getWidth();			
		}
		
		if(height != -1){
			sy = actualHeight / sprite.getHeight();			
		}		
		
		sprite.setScale(sx, sy);
	}

	@Override
	public void SetPosition(float x, float y) {
		sprite.setPosition(x, y);		
	}
}
