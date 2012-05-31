package se.treplex.sketchytruck;

import java.util.LinkedList;
import java.util.List;

import org.anddev.andengine.entity.text.ChangeableText;

public class MixedDataContainer {
	 public int x;
	    public int y;
	    public int width;
	    public int height;
	    public boolean visible;
	    public String shape;
	    public String sprite;
	    public String backgroundImage;
	    public int mass;
	    public int I;
	    public int relativeX;
	    public int relativeY;
	    public float lowerTranslation = 0;
	    public float upperTranslation = 0;
	    List<ChangeableText> changeableTextList = new LinkedList<ChangeableText>();
	    ChangeableText debugTextFPS;
	    ChangeableText debugTextTilt;
	    ChangeableText debugTextJointTranslation1;
	    ChangeableText debugTextJointTranslation2;
}
