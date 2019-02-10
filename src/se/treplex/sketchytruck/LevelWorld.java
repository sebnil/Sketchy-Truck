/**
 * @author Sebastian Nilsson
 * project url: http://sebastiannilsson.com/projekt/sketchy-truck/behover-hjalp-2d-bilspel-i-android/
 */

package se.treplex.sketchytruck;

import java.util.ArrayList;
import java.util.LinkedList;

import org.anddev.andengine.entity.shape.Shape;

public class LevelWorld {
	public String frontBackground;
	public ArrayList<String> backBackground = new ArrayList<String>();
	public int width;
	public int height;
	public int index;
	public int carX = 0;
	public int carY = 0;
	Shape finish;
	public LinkedList<MixedDataContainer> entities = new LinkedList<MixedDataContainer>();
	LinkedList<Shape> deadAreas = new LinkedList<Shape>();
}
