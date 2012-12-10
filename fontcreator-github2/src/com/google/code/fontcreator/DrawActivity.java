package com.google.code.fontcreator;

import java.io.File;
import java.io.IOException;
import java.sql.Savepoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class DrawActivity extends Activity implements OnClickListener {

	private ImageButton straightLineToolButton, freeDrawToolButton,
			curvedLineToolButton, clearToolButton, undoButton, redoButton;
	private Button currentLetterDisplayButton, prevButton, saveButton,
			nextButton;

	private FontManager fontManager;

	private DrawPanel drawPanel;

	// private FontManager fontManager;

	private AlphabetIterator ai;

	private String fontName;

	public enum DrawingTools {
		straightLine, freeDraw, curvedLine
	}

	private enum Direction {
		forwards, stay, backwards, callerHandled
	}

	private DrawingTools currentTool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawactivitylayout);
		straightLineToolButton = (ImageButton) findViewById(R.id.straightLineToolButton);
		freeDrawToolButton = (ImageButton) findViewById(R.id.freeDrawToolButton);
		curvedLineToolButton = (ImageButton) findViewById(R.id.curvedLineToolButton);
		clearToolButton = (ImageButton) findViewById(R.id.clearToolButton);
		undoButton = (ImageButton) findViewById(R.id.undoButton);
		redoButton = (ImageButton) findViewById(R.id.redoButton);
		currentLetterDisplayButton = (Button) findViewById(R.id.currentLetterDisplayButton);
		prevButton = (Button) findViewById(R.id.prevButton);
		saveButton = (Button) findViewById(R.id.saveButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		drawPanel = (DrawPanel) findViewById(R.id.drawPanel);
		straightLineToolButton.setOnClickListener(this);
		freeDrawToolButton.setOnClickListener(this);
		curvedLineToolButton.setOnClickListener(this);
		clearToolButton.setOnClickListener(this);
		undoButton.setOnClickListener(this);
		redoButton.setOnClickListener(this);
		currentLetterDisplayButton.setOnClickListener(this);
		prevButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		currentTool = DrawingTools.straightLine;
		drawPanel.setCurrentTool(DrawingTools.straightLine);
		// fontManager = new FontManager();
		ai = new AlphabetIterator();
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(FontDefaults.FILENAMEKEY))
			fontName = extras.getString(FontDefaults.FILENAMEKEY);
		else
			fontName = FontDefaults.DEFAULTFILENAME;
		if (extras != null && extras.containsKey(FontDefaults.EDITFILENAMEKEY)) {
			fontName = extras
					.getString(FontDefaults.EDITFILENAMEKEY);
			fontManager = new FontManager(this, fontName);
		} else {
			fontManager = new FontManager(this);
		}
		updateToolHighlight();
		ViewTreeObserver vto = drawPanel.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				drawPanel.loadGlyph(ai.getCurrent(), fontManager);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.drawactivitymenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.usefontdrawscreenmenuitem:
			saveWithProgressIndicator(new ProgressIndicatorCallback() {

				@Override
				public void onComplete() {
					Intent intent;
					intent = new Intent(DrawActivity.this, UseActivity.class);
					intent.putExtra(FontDefaults.FILENAMEKEY, fontName);
					startActivity(intent);
				}
			});
			return true;
		case R.id.displayfontdrawscreenmenuitem:
			saveWithProgressIndicator(new ProgressIndicatorCallback() {

				@Override
				public void onComplete() {
					Intent intent;
					intent = new Intent(DrawActivity.this, DisplayActivity.class);
					intent.putExtra(FontDefaults.FILENAMEKEY, fontName);
					startActivity(intent);
				}
			});
			return true;
		case R.id.mainmenudrawscreenmenuitem:
			saveWithProgressIndicator(new ProgressIndicatorCallback() {

				@Override
				public void onComplete() {
					Intent intent;
					intent = new Intent(DrawActivity.this, MainMenuActivity.class);
					startActivity(intent);
				}
			});
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.straightLineToolButton:
			currentTool = DrawingTools.straightLine;
			updateToolHighlight();
			drawPanel.setCurrentTool(DrawingTools.straightLine);
			break;
		case R.id.freeDrawToolButton:
			currentTool = DrawingTools.freeDraw;
			updateToolHighlight();
			drawPanel.setCurrentTool(DrawingTools.freeDraw);

			break;
		case R.id.curvedLineToolButton:
			currentTool = DrawingTools.curvedLine;
			updateToolHighlight();
			drawPanel.setCurrentTool(DrawingTools.curvedLine);
			break;
		case R.id.clearToolButton:
			drawPanel.checkClear();
			break;
		case R.id.undoButton:
			drawPanel.undo();
			break;
		case R.id.redoButton:
			drawPanel.redo();
			break;
		case R.id.currentLetterDisplayButton:
			final AlertDialog viewDialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(
					v.getContext());

			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View dialogView = li.inflate(R.layout.letter_selector_popup, null);

			builder.setView(dialogView);
			viewDialog = builder.create();

			final EditText letterselect = (EditText) dialogView
					.findViewById(R.id.select_letter_edit);
			Button selectButton = (Button) dialogView
					.findViewById(R.id.select_letter_button);
			selectButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					String abc = letterselect.getText().toString().trim();
					drawPanel.clear();
					drawPanel.loadGlyph(abc, fontManager);
					currentLetterDisplayButton.setText(abc);
					ai.setCurrent(abc);
					viewDialog.dismiss();
				}
			});
			Button cancelButton = (Button) dialogView
					.findViewById(R.id.cancel_select_letter_button);
			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					viewDialog.dismiss();
				}
			});
			viewDialog.show();
			break;
		case R.id.prevButton:

			if (drawPanel.needSave()) {
				saveGlyphDialog(Direction.backwards);
			} else {
				drawPanel.clear();
				ai.prev();
				drawPanel.loadGlyph(ai.getCurrent(), fontManager);
				currentLetterDisplayButton.setText(ai.getCurrent());
			}
			break;
		case R.id.saveButton:
			saveGlyphDialog(Direction.stay);
			break;
		case R.id.nextButton:

			if (drawPanel.needSave())
				saveGlyphDialog(Direction.forwards);
			else {
				drawPanel.clear();
				ai.next();
				drawPanel.loadGlyph(ai.getCurrent(), fontManager);
				currentLetterDisplayButton.setText(ai.getCurrent());
			}
			break;
		}
	}

	/*
	 * @Override protected void onPostResume() { super.onResume();
	 * drawPanel.loadGlyph(ai.getCurrent(), fontManager); }
	 */

	private void updateToolHighlight() {
		straightLineToolButton.setBackgroundResource(R.color.transparent);
		freeDrawToolButton.setBackgroundResource(R.color.transparent);
		curvedLineToolButton.setBackgroundResource(R.color.transparent);
		switch (currentTool) {
		case straightLine:
			straightLineToolButton
					.setBackgroundResource(R.color.transparentbuttonselected);
			break;
		case freeDraw:
			freeDrawToolButton
					.setBackgroundResource(R.color.transparentbuttonselected);
			break;
		case curvedLine:
			curvedLineToolButton
					.setBackgroundResource(R.color.transparentbuttonselected);
			break;
		}
	}

	private void saveGlyphDialog(final Direction direction) {
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setMessage(getString(R.string.do_you_want_to_save_text));
		ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				saveWithProgressIndicator(null);
			}
		});
		ad.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (direction == Direction.forwards) {
					drawPanel.clear();
					ai.next();
					drawPanel.loadGlyph(ai.getCurrent(), fontManager);
				} else if (direction == Direction.backwards) {
					drawPanel.clear();
					ai.prev();
					drawPanel.loadGlyph(ai.getCurrent(), fontManager);
				} else if (direction == Direction.callerHandled) {
					;
				} else if (direction == Direction.stay) {
					;
				}

			}
		});
		ad.create();
		ad.show();

	}
	
	private void saveWithProgressIndicator(final ProgressIndicatorCallback callback) {
		final ProgressDialog pd = ProgressDialog.show(DrawActivity.this,"Saving Font","This can take several seconds",true,false,null);
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				if (drawPanel.needSave() || !((new File(getFilesDir(), fontName)).exists())) {
					try {
						drawPanel.save(ai.getCurrent(), fontManager, fontName);
						fontManager = new FontManager(DrawActivity.this,
								fontName);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				pd.dismiss();
				if (callback != null) 
					callback.onComplete();
			}
		}.execute();
	}
	
	private interface ProgressIndicatorCallback {
		void onComplete();
	}
}
