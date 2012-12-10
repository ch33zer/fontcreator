package com.google.code.fontcreator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelpActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		Button drawscreenHelpButton = (Button)findViewById(R.id.drawscreen_help_button);
		drawscreenHelpButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), DrawscreenHelpActivity.class);
				startActivity(i);
			}
		});
	}
}
