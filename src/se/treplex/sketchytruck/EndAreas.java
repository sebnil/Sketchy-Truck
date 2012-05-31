package se.treplex.sketchytruck;

public class EndAreas {
	public float x;
	public float y;
	public float width;
	public float height;
	
	public boolean isInside(float x, float y){
		if(x > this.x && x < this.x + width)
			if(y > this.y && y < this.y + height)
				return true;
		return false;
	}
}
