package com.google.code.fontcreator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ExportActivity extends Activity {

	private Button sendButton;
	private EditText emailField;
	private Spinner exportSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exportactivitylayout);
		sendButton = (Button) findViewById(R.id.exportsendbutton);
		emailField = (EditText) findViewById(R.id.exportemailaddressbox);
		exportSpinner = (Spinner) findViewById(R.id.exportspinner);
		final String fontFiles[] = FontUtils.getFonts(this);

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, fontFiles);
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
																							// drop
																							// down
																							// view
		exportSpinner.setAdapter(spinnerArrayAdapter);

		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Selection of the spinner
				// Application of the Array to the Spinner

				String emailTo = "";
				emailTo = emailField.getText().toString();

				String emailCC = "";
				String subject = "Your Created Font!";
				String emailText = "In the attachment you will find your TTF file. Love, the FontMaker Team.";

				String attachment = "";
				attachment = exportSpinner.getSelectedItem().toString();

				String attachmentPath = "";
				attachmentPath = "file://"+(FontUtils.getFont(attachment,
						view.getContext())).getAbsolutePath();

				email(view.getContext(), emailTo, emailCC, subject, emailText,
						attachmentPath);

			}

		});

	}

	public static void email(Context context, String emailTo, String emailCC,
			String subject, String emailText, String strFile) {

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("application/octet-stream");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { emailTo });
		emailIntent.putExtra(android.content.Intent.EXTRA_CC,
				new String[] { emailCC });
		// emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, new
		// String[]{subject});
		// emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, new
		// String[]{emailText});

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailText);

		emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,
				Uri.parse(/* "file://" + */strFile));

		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));

		/*
		 * Intent intent = new Intent(Intent.ACTION_SEND);
		 * intent.setType("plain/text"); intent.putExtra(Intent.EXTRA_EMAIL, new
		 * String[] { "some@email.address" });
		 * intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
		 * intent.putExtra(Intent.EXTRA_TEXT, "mail body");
		 * startActivity(Intent.createChooser(intent, ""));
		 */

		// emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailText);
		/*
		 * //has to be an ArrayList ArrayList<Uri> uris = new ArrayList<Uri>();
		 * //convert from paths to Android friendly Parcelable Uri's for (String
		 * file : filePaths) { File fileIn = new File(file); Uri u =
		 * Uri.fromFile(fileIn); uris.add(u); }
		 */
		// emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

	}
}