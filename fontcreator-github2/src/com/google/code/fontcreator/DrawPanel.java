package com.google.code.fontcreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;

public class DrawPanel extends SurfaceView implements SurfaceHolder.Callback {
	private TutorialThread drawingThread;
	private boolean needSave = false;
	private List<Stroke> pathList, redoHistory, contourList,
			contourRedoHistory;
	private boolean initialDrawPress = false, editingControlPoint = false,
			draggingControlPoint = false, drawingContinuous = false,
			inContour = false;
	private Point startPoint = null, controlPointHandle = null,
			endPoint = null, lastContourEnd = null, contourStart = null;
	private Stroke currentPath;
	private Paint blackPaint = null, drawPathPaint = null, continuousPaint = null, contourPaint = null;
	private int lastDownX, lastDownY;
	private DrawActivity.DrawingTools currentTool;
	
	private float scaleFactor;

	public DrawPanel(Context context, AttributeSet attribs) {
		super(context, attribs);
		pathList = Collections.synchronizedList(new ArrayList<Stroke>());
		contourList = Collections.synchronizedList(new ArrayList<Stroke>());
		redoHistory = new ArrayList<Stroke>();
		contourRedoHistory = new ArrayList<Stroke>();
		blackPaint = new Paint();
		blackPaint.setColor(Color.BLACK);
		blackPaint.setStyle(Paint.Style.STROKE);
		blackPaint.setStrokeWidth(1.0f);
		blackPaint.setStrokeMiter(1.0f);
		blackPaint.setStrokeJoin(Paint.Join.MITER);
		blackPaint.setStrokeCap(Cap.SQUARE);
		blackPaint.setAntiAlias(true);
		drawPathPaint = new Paint();
		drawPathPaint.setColor(Color.BLUE);
		drawPathPaint.setStyle(Paint.Style.STROKE);
		drawPathPaint.setStrokeWidth(1.0f);
		drawPathPaint.setStrokeMiter(1.0f);
		drawPathPaint.setStrokeJoin(Paint.Join.MITER);
		drawPathPaint.setStrokeCap(Cap.SQUARE);
		drawPathPaint.setAntiAlias(true);
		continuousPaint = new Paint();
		continuousPaint.setColor(Color.BLUE);
		continuousPaint.setStyle(Paint.Style.STROKE);
		continuousPaint.setStrokeWidth(1.0f);
		continuousPaint.setStrokeMiter(1.0f);
		continuousPaint.setStrokeJoin(Paint.Join.ROUND);
		continuousPaint.setStrokeCap(Cap.ROUND);
		continuousPaint.setAntiAlias(true);
		contourPaint = new Paint();
		contourPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		contourPaint.setStrokeWidth(1.0f);
		contourPaint.setStrokeMiter(1.0f);
		contourPaint.setStrokeJoin(Paint.Join.ROUND);
		contourPaint.setStrokeCap(Cap.ROUND);
		contourPaint.setAntiAlias(true);
		contourPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), Color.BLACK, Color.BLACK, Shader.TileMode.REPEAT));
		getHolder().addCallback(this);
		drawingThread = new TutorialThread(getHolder(), this);
		setFocusable(true);
		setZOrderOnTop(true);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		setNeedSave(true);
		synchronized (drawingThread.getSurfaceHolder()) {
			switch (currentTool) {
			case straightLine:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					clearRedoHistory();
					if (!inContour) {
						startPoint = new Point((int) event.getX(),
								(int) event.getY());
						lastDownX = (int) event.getX();
						lastDownY = (int) event.getY();
						initialDrawPress = true;
						contourStart = startPoint;
						inContour = true;
					} else {
						startPoint = lastContourEnd;
						lastDownX = (int) event.getX();
						lastDownY = (int) event.getY();
						initialDrawPress = true;
					}
				} else if (initialDrawPress
						&& event.getAction() == MotionEvent.ACTION_UP) {
					Point end = new Point((int) event.getX(),
							(int) event.getY());
					lastContourEnd = end;
					Path out = new Path();
					out.moveTo(startPoint.x, startPoint.y);
					out.quadTo((end.x - startPoint.x) / 2 + startPoint.x,
							(end.y - startPoint.y) / 2 + startPoint.y, end.x,
							end.y);
					Stroke stroke = new Stroke(startPoint, end, end,
							drawPathPaint);
					synchronized (pathList) {
						pathList.add(stroke);
					}
					redoHistory.clear();
					startPoint = null;
					initialDrawPress = false;
					checkClosePath();
				} else if (initialDrawPress
						&& event.getAction() == MotionEvent.ACTION_MOVE) {
					lastDownX = (int) event.getX();
					lastDownY = (int) event.getY();
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					initialDrawPress = false;
					startPoint = null;
				}
				return true;
			case curvedLine:
				if (!editingControlPoint
						&& event.getAction() == MotionEvent.ACTION_DOWN) {
					clearRedoHistory();
					if (!inContour) {
						startPoint = new Point((int) event.getX(),
								(int) event.getY());
						lastDownX = (int) event.getX();
						lastDownY = (int) event.getY();
						initialDrawPress = true;
						contourStart = startPoint;
						inContour = true;
					}
					else {
						startPoint = lastContourEnd;
						lastDownX = (int) event.getX();
						lastDownY = (int) event.getY();
						initialDrawPress = true;
					}
				} else if (editingControlPoint
						&& event.getAction() == MotionEvent.ACTION_DOWN) {
					Point curr = new Point((int) event.getX(),
							(int) event.getY());
					if (distBetween(curr, controlPointHandle) > 50) {
						Path out = new Path();
						out.moveTo(startPoint.x, startPoint.y);
						out.quadTo(controlPointHandle.x, controlPointHandle.y,
								endPoint.x, endPoint.y);
						Stroke stroke = new Stroke(startPoint,
								controlPointHandle, endPoint, drawPathPaint);
						synchronized (pathList) {
							pathList.add(stroke);
						}
						editingControlPoint = false;
						controlPointHandle = null;
						redoHistory.clear();
						startPoint = null;
						initialDrawPress = false;
						endPoint = null;
						checkClosePath();
					} else {
						draggingControlPoint = true;
						lastDownX = (int) event.getX();
						lastDownY = (int) event.getY();
					}
				} else if (initialDrawPress
						&& event.getAction() == MotionEvent.ACTION_UP) {
					endPoint = new Point((int) event.getX(), (int) event.getY());
					controlPointHandle = new Point((endPoint.x - startPoint.x)
							/ 2 + startPoint.x, (endPoint.y - startPoint.y) / 2
							+ startPoint.y);
					editingControlPoint = true;
					initialDrawPress = false;
					lastContourEnd = endPoint;
				} else if (draggingControlPoint
						&& event.getAction() == MotionEvent.ACTION_UP) {
					draggingControlPoint = false;
				} else if (!draggingControlPoint
						&& event.getAction() == MotionEvent.ACTION_MOVE) {
					lastDownX = (int) event.getX();
					lastDownY = (int) event.getY();
				} else if (draggingControlPoint
						&& event.getAction() == MotionEvent.ACTION_MOVE) {
					lastDownX = (int) event.getX();
					lastDownY = (int) event.getY();
					controlPointHandle.set(lastDownX, lastDownY);
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					initialDrawPress = false;
					startPoint = null;
					controlPointHandle = null;
					draggingControlPoint = false;
					initialDrawPress = false;
					endPoint = null;
				}
				return true;
			case freeDraw:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					clearRedoHistory();
					if (!inContour) {
						startPoint = new Point((int) event.getX(),
								(int) event.getY());
						currentPath = new Stroke(continuousPaint);
						currentPath.start(startPoint);
						drawingContinuous = true;
						contourStart = startPoint;
						inContour = true;
					}
					else {
						startPoint = lastContourEnd;
						currentPath = new Stroke(continuousPaint);
						currentPath.start(startPoint);
						drawingContinuous = true;
					}
				} else if (drawingContinuous
						&& event.getAction() == MotionEvent.ACTION_UP) {
					Point end = new Point((int) event.getX(),
							(int) event.getY());
					currentPath.end(end, end);
					synchronized (pathList) {
						pathList.add(currentPath);
					}
					currentPath = null;
					redoHistory.clear();
					drawingContinuous = false;
					startPoint = null;
					lastContourEnd = end;
					checkClosePath();
				} else if (drawingContinuous
						&& event.getAction() == MotionEvent.ACTION_MOVE) {
					Point curr = new Point((int) event.getX(), (int) event.getY());
					currentPath.addQuad(curr, curr);
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					currentPath = null;
					drawingContinuous = false;
				}
				return true;
			}
			return false;
		}
	}

	private void checkClosePath() {
		if (distBetween(contourStart, lastContourEnd) < 30) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage("Close contour?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finalizeContour();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									;
								}
							}).show();
		}
	}

	private void finalizeContour() {
		/*Path contour = new Path();
		contour.moveTo(contourStart.x, contourStart.y);
		Point start, mid, end;*/
		Stroke contour = new Stroke(contourPaint);
		synchronized (pathList) {
			LinkedList<Point> segments;
			Point start = pathList.get(0).getStart();
			Iterator<Point> iter;
			contour.start(start);
			for (Stroke s : pathList) {
				if (s.isComponentWisePath()) {
					segments = s.getSegments();
					iter = segments.iterator();
					iter.next(); //get rid of start
					while (iter.hasNext()) {
						contour.addQuad(iter.next(), iter.next());
					}
				}
				else {
					contour.addQuad(s.getControl(), s.getEnd());
				}
			}
		}
		contour.close();

		synchronized (contourList) {
			contourList.add(contour);
		}
		synchronized (pathList) {
			pathList.clear();
		}
		inContour = false;
		contourStart = null;
		lastContourEnd = null;
		clearRedoHistory();

	}
	
	public void loadGlyph(String newGlyph, FontManager fm) {
		needSave = false;
		Stroke currContour = new Stroke(continuousPaint);
		SimpleGlyph glyph = (SimpleGlyph) fm.getGlyph(newGlyph);
		int width = getWidth();
		int height = getHeight();
		int baselineHeight = (int)(height * 3f/4f);
		int baselineWidth = (int)(width * 1f/5f);
		float xScale = (width - baselineWidth)/((float)glyph.xMax() +100);
		float yScale = (height -(height - baselineHeight))/((float)glyph.yMax()+100);
		scaleFactor = xScale < yScale ? xScale : yScale;
	    Point inferredOnCurve = null, control = new Point(), current = new Point(), start = new Point();
	    for (int i = 0; i < glyph.numberOfContours(); i++) {
	    	for (int j = 0; j < glyph.numberOfPoints(i); j++) {
	    		int xScaled = (int)(scaleFactor * glyph.xCoordinate(i, j)) + baselineWidth;
	    		int yScaled = -1 * (int)(scaleFactor * glyph.yCoordinate(i, j)) + baselineHeight;
	    		current.set(xScaled, yScaled);
	    		if (glyph.onCurve(i, j)) {
	    			if (j == 0) {
	    				start.set(current.x, current.y);
	    				currContour.start(current);
	    				control = null;
	    			}
	    			else if (control == null) {
	    				currContour.addQuad(current, current);
	    			}
	    			else {
	    				currContour.addQuad(control, current);
	    				control = null;
	    			}
	    		}
	    		else {
	    			if (control == null) {
	    				control = new Point(xScaled, yScaled);
	    			} 
	    			else {
	    				inferredOnCurve = new Point((control.x + current.x) /2,(control.y + current.y)/2);
	    				currContour.addQuad(control, inferredOnCurve);
	    				control.set(xScaled, yScaled);
	    			}
	    		}
	    		if (j == glyph.numberOfPoints(i)-1) {
	    			if (control != null) {
	    				currContour.addQuad(control, start);
	    			}
	    		}
	    	}
	    	currContour.close();
	    	contourList.add(currContour);
	    	currContour = new Stroke(contourPaint);
	    }
	}
	
	public void checkClear() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage("Clear all contours? This cannot be undone.")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								clear();
								setNeedSave(true);
							}
						})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								;
							}
						}).show();
	}

	private static int distBetween(Point p1, Point p2) {
		int dx = p1.x - p2.x, dy = p1.y - p2.y;
		return (int)Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		synchronized (contourList) {
			Path p = new Path();
			for (Stroke stroke : contourList) {
				p.addPath(stroke.getPath());
			}
			p.close();
			canvas.drawPath(p, contourPaint);
		}
		synchronized (pathList) {
			for (Stroke stroke : pathList) {
				canvas.drawPath(stroke.getPath(), stroke.getPaint());
			}
		}
		if (initialDrawPress) {
			Point end = new Point(lastDownX, lastDownY);
			Path out = new Path();
			out.moveTo(startPoint.x, startPoint.y);
			out.quadTo(end.x, end.y, end.x, end.y);
			canvas.drawPath(out, drawPathPaint);
		} else if (editingControlPoint) {
			Path out = new Path();
			out.moveTo(startPoint.x, startPoint.y);
			out.quadTo(controlPointHandle.x, controlPointHandle.y, endPoint.x,
					endPoint.y);
			canvas.drawPath(out, drawPathPaint);
			canvas.drawCircle(controlPointHandle.x, controlPointHandle.y, 50,
					drawPathPaint);
		} else if (drawingContinuous) {
			canvas.drawPath(currentPath.getPath(), continuousPaint);
		}
		float height = getHeight();
		float width = getWidth();
		canvas.drawLine(0.0f, height * 3f/4f, width, height * 3f/4f, blackPaint);
		canvas.drawLine(width * 1.0f/5.0f, 0f, width * 1.0f/5.0f, height, blackPaint);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		try {
			setFocusable(true);
			setZOrderOnTop(true);
			drawingThread.setRunning(true);
			drawingThread.start();
		} catch (Exception ex) {
			drawingThread = new TutorialThread(getHolder(), this);
			drawingThread.setRunning(true);
			setFocusable(true);
			setZOrderOnTop(true);
			drawingThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// simply copied from sample application LunarLander:
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		drawingThread.setRunning(false);
		while (retry) {
			try {
				drawingThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	public void undo() {
		finalizeDanglingControlPoints();
		boolean undoPath = false;
		synchronized (pathList) {
			if (pathList.size() > 0){
				Stroke s = pathList.remove(pathList.size() - 1);
				if (pathList.size() != 0) {
					setNeedSave(true);
					lastContourEnd = s.getStart();
				}
				else {
					inContour = false;
				}
				redoHistory.add(s);
			}
			else{
				undoPath = true;
			}
		}
		if (undoPath) {
			synchronized (contourList) {
				if (contourList.size() > 0) {
					setNeedSave(true);
					contourRedoHistory.add(contourList.remove(contourList.size()-1));
				}
			}
		}
	}

	public void redo() {
		finalizeDanglingControlPoints();
		boolean undoPath = false;
		synchronized (contourList) {
			if (contourRedoHistory.size() > 0) 
				contourList.add(contourRedoHistory.remove(contourRedoHistory.size()-1));
			else {
				undoPath = true;
			}
		}
		if (undoPath) {
			synchronized (pathList) {
				if (redoHistory.size() > 0){
					Stroke s = redoHistory.remove(redoHistory.size() - 1);
					lastContourEnd = s.getEnd();
					inContour = true;
					pathList.add(s);
				}
			}
		}

	}
	
	private void clearRedoHistory() {
		contourRedoHistory.clear();
		redoHistory.clear();
	}

	private void finalizeDanglingControlPoints() {
		if (editingControlPoint) {
			editingControlPoint = false;
			synchronized (pathList) {
				Path out = new Path();
				out.moveTo(startPoint.x, startPoint.y);
				out.quadTo(controlPointHandle.x, controlPointHandle.y,
						endPoint.x, endPoint.y);
				pathList.add(new Stroke(startPoint, controlPointHandle,
						endPoint, drawPathPaint));
				lastContourEnd = endPoint;
			}
			checkClosePath();
		}
	}
	
	public FontManager save(String letter, FontManager fm, String fontName) throws IOException {
		
		Glyph f = fm .makeGlyph(fm.getGlyph(letter), contourList, (int)(getHeight() * 3f/4f),(int)( getWidth() * 1f/5f), getWidth(), 1/scaleFactor);
		needSave = false;
		return fm.changeGlyph(letter, f, fontName);
		

	}
	
	
	
	public boolean needSave()
	{
		return needSave;
	}
	
	public void setNeedSave(boolean ns)
	{
		//Log.v("NEEDSAVE", "Need save set " + ns);
		needSave = ns;
	}

	public DrawActivity.DrawingTools getCurrentTool() {
		return currentTool;
	}

	public void setCurrentTool(DrawActivity.DrawingTools currentTool) {
		finalizeDanglingControlPoints();
		this.currentTool = currentTool;
	}

	public void clear() {
		synchronized (pathList) {
			pathList.clear();
		}
		synchronized (contourList) {
			contourList.clear();
		}
		clearRedoHistory();
		draggingControlPoint = false;
		editingControlPoint = false;
		initialDrawPress = false;
		startPoint = null;
		endPoint = null;
		controlPointHandle = null;
		inContour = false;
	}

	private class TutorialThread extends Thread {
		private SurfaceHolder _surfaceHolder;
		private DrawPanel _panel;
		private boolean _run = false;

		public TutorialThread(SurfaceHolder surfaceHolder, DrawPanel panel) {
			_surfaceHolder = surfaceHolder;
			_panel = panel;
		}

		public void setRunning(boolean run) {
			_run = run;
		}

		public SurfaceHolder getSurfaceHolder() {
			return _surfaceHolder;
		}

		@Override
		public void run() {
			Canvas c;
			while (_run) {
				c = null;
				try {
					c = _surfaceHolder.lockCanvas(null);
					synchronized (_surfaceHolder) {
						_panel.onDraw(c);
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						_surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

	}

}
