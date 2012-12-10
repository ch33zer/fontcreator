package com.google.code.fontcreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ManageFontActivity extends Activity {
	
	private String fontName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.managefont);
		
		if (getIntent().getExtras().containsKey(FontDefaults.FILENAMEKEY))
			fontName = getIntent().getExtras().getString(FontDefaults.FILENAMEKEY);
		else 
			fontName = FontDefaults.DEFAULTFILENAME;
		
		((TextView) findViewById(R.id.font_to_be_edited)).setText(fontName);
		final Button editButton = (Button)findViewById(R.id.edit_button);
		editButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), DrawActivity.class);
				myIntent.putExtra(FontDefaults.EDITFILENAMEKEY, fontName);
                startActivity(myIntent);				
			}
		});
		
		final Button exportButton = (Button)findViewById(R.id.export_button);
		exportButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), ExportActivity.class);
				myIntent.putExtra(FontDefaults.FILENAMEKEY, fontName);
                startActivity(myIntent);
			}
		});
		
		final Button displayButton = (Button)findViewById(R.id.display_button);
		displayButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), DisplayActivity.class);
				myIntent.putExtra(FontDefaults.FILENAMEKEY, fontName);
                startActivity(myIntent);				
			}
		});
		
		final Button useButton = (Button)findViewById(R.id.use_button);
		useButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), UseActivity.class);
				myIntent.putExtra(FontDefaults.FILENAMEKEY, fontName);
                startActivity(myIntent);				
			}
		});
		
		final Button deleteButton = (Button) findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
				adb.setMessage("Really delete font \""+ fontName +"\"?");
				adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ManageFontActivity.this.deleteFile(fontName);
						ManageFontActivity.this.finish();
					}
				});
				adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						;
					}
				});
				adb.show();
			}
		});
	}
}
