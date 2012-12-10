package com.google.code.fontcreator;

public class AlphabetIterator{
	private String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z","0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", ",", ";", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")"};
	private int current;
	public AlphabetIterator(){
		current = 0;
	}
	
	public String[] getList() {
		return alphabet;
	}

	public String next(){
		if(current<alphabet.length-1)
			current++;
		else if(current == alphabet.length-1)
			current = 0;
		return alphabet[current];
	}

	public String prev(){
		if(current>0)
			current--;
		else if(current == 0)
			current = alphabet.length-1;
		return alphabet[current];
	}

	public String getCurrent(){
		return alphabet[current];
	}

	public boolean setCurrent(String character){
		for(int i=0; i<alphabet.length; i++)
		{
			if(alphabet[i].equals(character))
			{
				current = i;
				return true;
			}
		}
		return false;
	}

}
