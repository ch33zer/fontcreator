package com.google.code.fontcreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

	public static final String FILENAMEKEY = "FILENAME.KEY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		final Button newFontButton = (Button) findViewById(R.id.new_font_button);
		newFontButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final AlertDialog viewDialog;
				AlertDialog.Builder builder = new AlertDialog.Builder(v
						.getContext());

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View dialogView = li.inflate(R.layout.new_font_name_dialog,
						null);

				builder.setView(dialogView);
				viewDialog = builder.create();

				final EditText editText = (EditText) dialogView
						.findViewById(R.id.name_new_font_editText);

				final Button okButton = (Button) dialogView
						.findViewById(R.id.name_ok_button);
				okButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {

						String fontName = editText.getText().toString();
						if (!fontName.toLowerCase().endsWith(".ttf"))
							;
						fontName += ".ttf";
						Toast t = Toast.makeText(getApplicationContext(),
								"Name is " + fontName, Toast.LENGTH_LONG);
						t.show();
						Intent myIntent = new Intent(view.getContext(),
								DrawActivity.class);
						myIntent.putExtra(FILENAMEKEY, fontName);
						viewDialog.dismiss();
						startActivity(myIntent);
					}
				});
				final Button cancelButton = (Button) dialogView
						.findViewById(R.id.name_cancel_button);
				cancelButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(editText.getWindowToken(),
								0);
						viewDialog.dismiss();
					}
				});

				editText.requestFocus();
				viewDialog.setOnShowListener(new OnShowListener() {

					@Override
					public void onShow(DialogInterface dialog) {
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
								.showSoftInput(editText,
										InputMethodManager.SHOW_IMPLICIT);
					}
				});
				viewDialog.show();
			}
		});
		final Button existingFontButton = (Button) findViewById(R.id.existing_font_button);
		existingFontButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String fontFiles[] = FontUtils.getFonts(v.getContext());
				if (fontFiles.length > 0) {
					final AlertDialog viewDialog;
					AlertDialog.Builder builder = new AlertDialog.Builder(v
							.getContext());

					LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View dialogView = li.inflate(R.layout.existingfontdialogue,
							null);

					builder.setView(dialogView);
					viewDialog = builder.create();

					final Spinner spinner = (Spinner) dialogView
							.findViewById(R.id.chooseExistingFontSpinner);
					/*
					 * ArrayAdapter<CharSequence> adapter =
					 * ArrayAdapter.createFromResource(v.getContext(),
					 * R.array.fonts, android.R.layout.simple_spinner_item);
					 * adapter.setDropDownViewResource(android.R.layout.
					 * simple_spinner_dropdown_item);
					 * spinner.setAdapter(adapter);
					 */

					ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
							v.getContext(),
							android.R.layout.simple_spinner_item, fontFiles);
					spinnerArrayAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
																										// drop
																										// down
																										// view
					spinner.setAdapter(spinnerArrayAdapter);

					final Button okButton = (Button) dialogView
							.findViewById(R.id.ok_button);
					okButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (spinner.getSelectedItem() != null) {
								String filename = spinner.getSelectedItem()
										.toString();
								Intent myIntent = new Intent(v.getContext(),
										ManageFontActivity.class);
								myIntent.putExtra(FontDefaults.FILENAMEKEY,
										filename);
								viewDialog.dismiss();
								startActivity(myIntent);
								// Will put code for specific font?
							}
						}
					});
					final Button cancelButton = (Button) dialogView
							.findViewById(R.id.cancel_button);
					cancelButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							viewDialog.dismiss();
						}
					});

					viewDialog.show();
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
					builder.setMessage("No fonts found");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
						}
					});
					builder.show();
				}
			}
		});
		final Button helpButton = (Button) findViewById(R.id.help_button);
		helpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(),
						DrawscreenHelpActivity.class);
				startActivity(myIntent);
			}
		});
	}

}
