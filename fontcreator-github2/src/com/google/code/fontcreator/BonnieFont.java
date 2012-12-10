package com.google.code.fontcreator;

import java.util.ArrayList;

public class BonnieFont {

	private ArrayList<Character> chars;
	private ArrayList<FontCharacter> charImages;

	public BonnieFont(){
		//TODO: 
		//SOMETHING
		chars = new ArrayList<Character>();
		charImages = new ArrayList<FontCharacter>();
		loadCharacters();
	}
	
	public void loadCharacters(){
		//A little bit of scratch work to get ideas down
		/*
			fontDB.open; open the database
			charImages = fontDB.getImagesForFont(this);
			//This should load an array list gleaned from the DB
		 */
		
	}

	public FontCharacter getImageForChar(char c){
		int index = chars.indexOf(c);
		return charImages.get(index);
	}

	public void setImageForChar(char c, FontCharacter fc){
		int index = chars.indexOf(c);
		charImages.set(index, fc);
	}

	public char getFontChar(FontCharacter fc){
		int index = charImages.indexOf(fc);
		return chars.get(index);
	}

	public ArrayList<Character> getChars() {
		return chars;
	}
	public void setChars(ArrayList<Character> chars) {
		this.chars = chars;
	}
	public ArrayList<FontCharacter> getCharImages() {
		return charImages;
	}
	public void setCharImages(ArrayList<FontCharacter> charImages) {
		this.charImages = charImages;
	}



}
