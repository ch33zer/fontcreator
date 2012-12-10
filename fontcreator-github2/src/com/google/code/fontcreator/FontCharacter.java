package com.google.code.fontcreator;

import android.view.SurfaceView;

public class FontCharacter {
	private char character;
	private SurfaceView surfaceView;
	
	public FontCharacter(char c, SurfaceView sv){
		character = c;
		surfaceView = sv;
	}
	
	public char getCharacter() {
		return character;
	}
	public void setCharacter(char character) {
		this.character = character;
	}
	public SurfaceView getSurfaceView() {
		return surfaceView;
	}
	public void setSurfaceView(SurfaceView surfaceView) {
		this.surfaceView = surfaceView;
	}
	
	
}
