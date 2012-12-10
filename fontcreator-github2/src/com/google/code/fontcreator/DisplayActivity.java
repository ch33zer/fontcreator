package com.google.code.fontcreator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplayActivity extends Activity{
	private TextView fontDisplayTextView, fontNameDisplay;
	private Button mainMenuButton;
	private String fontName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayfont);
		if (getIntent().getExtras().containsKey(FontDefaults.FILENAMEKEY))
			fontName = getIntent().getExtras().getString(FontDefaults.FILENAMEKEY);
		else 
			fontName = FontDefaults.DEFAULTFILENAME;
		initFontDisplay();
		initButtons();
	}
	
	private void initFontDisplay(){
		fontNameDisplay = (TextView) findViewById(R.id.fontNameTextView);
		fontNameDisplay.setText(fontName);
		fontDisplayTextView = (TextView) findViewById(R.id.fontDisplayTextView);
		StringBuilder sb = new StringBuilder();
		for (String s : new AlphabetIterator().getList()) {
			sb.append(s).append(" ");
		}
		fontDisplayTextView.setText(sb.toString());
		fontDisplayTextView.setTypeface(FontUtils.getTypeface(this, fontName));
	}
	
	private void initButtons() {
		// create the mainMenuButton
		mainMenuButton = (Button) findViewById(R.id.menuButton);

		// listener for the button
		mainMenuButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(DisplayActivity.this, MainMenuActivity.class);
	        	startActivity(intent);
			}

		});

		
	}
}
