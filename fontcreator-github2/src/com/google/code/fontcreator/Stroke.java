package com.google.code.fontcreator;

import java.util.LinkedList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class Stroke {

	private Point start, end, control;
	
	private Path path;
	
	private boolean isComponentWise;

	private LinkedList<Point> segments;
	
	public Point getStart() {
		return start;
	}

	public Point getEnd() {
		return end;
	}
	
	public Point getControl() {
		return control;
	}
	
	public LinkedList<Point> getSegments() {
		return segments;
	}

	public Stroke(Point start, Point control, Point end, Paint paint) {
		this.start = new Point(start);
		this.end = new Point(end);
		this.control = new Point(control);
		this.paint = paint;
		
		path = new Path();
		path.moveTo(start.x, start.y);
		path.quadTo(control.x, control.y, end.x, end.y);
		isComponentWise = false;
	}
	
	public Stroke(Paint paint) {
		this.paint = paint;
		this.path = new Path();
		isComponentWise = true;
		segments = new LinkedList<Point>();
	}
	
	public void start(Point p) {
		if (isComponentWise) {
			Point temp = new Point (p);
			segments.add(temp);
			start = temp;
			path.moveTo(temp.x, temp.y);
		}
	}
	
	public void addQuad(Point control, Point end) {
		if (isComponentWise) {
			Point tempC = new Point(control), tempE = new Point(end);
			segments.add(tempC);
			segments.add(tempE);
			path.quadTo(control.x, control.y, end.x, end.y);
		}
	}
	
	public void end(Point control, Point end) {
		if (isComponentWise) {
			Point tempC = new Point(control), tempE = new Point(end);
			segments.add(tempC);
			segments.add(tempE);
			path.quadTo(control.x, control.y, end.x, end.y);
			this.end = tempE;
		}
	}
	
	public void close() {
		if (isComponentWise) {
			segments.add(new Point(start));
			segments.add(new Point(start));
			this.end = new Point(start);
			path.quadTo(start.x, start.y, start.x, start.y);
			path.close();
		}
	}

	public Path getPath() {
		return path;
	}

	public Paint getPaint() {
		return paint;
	}
	
	public boolean isComponentWisePath() {
		return isComponentWise;
	}
	
	private Paint paint;
}
