package com.example.creativepics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextEditor extends Activity {
	EditText uCompose;
	EditText uFileName;
	Button uSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_editor);

		uCompose = (EditText) findViewById(R.id.compose);
		uFileName = (EditText) findViewById(R.id.editSave);
		
		uSave = (Button) findViewById(R.id.save);
		uSave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String value = uCompose.getText().toString();
				String fileName = uFileName.getText().toString();
				if ((value == null || value.length() == 0) && ((fileName == null || fileName.length() == 0))) {
					setResult(RESULT_CANCELED);
					Toast.makeText(TextEditor.this, "FileName or Notes is empty", Toast.LENGTH_LONG).show();
				} else {
					Log.d("text", value);
					Intent intent = new Intent();
					intent.putExtra(DBApplication.TEXT_CONTENT, value);
					intent.putExtra(DBApplication.TEXT_NAME, fileName);
					setResult(RESULT_OK, intent);
				}
				finish();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.text_editor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
