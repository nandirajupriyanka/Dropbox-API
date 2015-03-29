package com.example.creativepics;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DBApplication extends Activity {
	private static final String TAG = "MainActivity";

	private static final String APP_KEY = "8laski5dpxb9aeq";
	private static final String APP_SECRET = "rycvm9y7jlqzlpd";
	private static final String ACCOUNT_PREFS_NAME = "prefs";
	private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private static final boolean USE_OAUTH1 = false;
	private static final int NEW_PIC = 1;
	private static final int TEXT_CODE = 100;
	static final String TEXT_CONTENT = "value";
	static final String TEXT_NAME = "fileName";
	private final String PHOTO_DIR = "/Photos/";
	private final String FILE_DIR = "/Notes/";

	// Declarations Section

	private DropboxAPI<AndroidAuthSession> myApi;

	private LinearLayout displayLayout;
	private Button uSubmit;
	private ImageButton uCamera;
	private Button viewAll;
	private ImageView uImageView;
	private LinearLayout scrollList;
	private ImageButton uRecord;
	private ImageButton compose;
	private boolean uLoggedIn;
	
	private String picName;

	// OnCreate function

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Passing values to DropboxAPI Object
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		myApi = new DropboxAPI<AndroidAuthSession>(session);
		loadAuthr(session);

		setContentView(R.layout.activity_main);
		checkKeySetup();

		displayLayout = (LinearLayout) findViewById(R.id.display);
		uImageView = (ImageView) findViewById(R.id.image);
		uSubmit = (Button) findViewById(R.id.link_button);
		uSubmit.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (uLoggedIn) {
					logOut();
				} else {
					if (USE_OAUTH1) {
						myApi.getSession().startAuthentication(
								DBApplication.this);
					} else {
						myApi.getSession().startOAuth2Authentication(
								DBApplication.this);
					}
				}
			}

		});

		// To Display Camera and Upload Image
		uCamera = (ImageButton) findViewById(R.id.camera);
		uCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

				Date date = new Date();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss",
						Locale.US);

				String newPicFile = df.format(date) + ".jpg";
				String outPath = new File(Environment
						.getExternalStorageDirectory(), newPicFile).getPath();
				File outFile = new File(outPath);

				picName = outFile.toString();
				Uri outuri = Uri.fromFile(outFile);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
				Log.i(TAG, "Importing New Picture: " + picName);
				try {
					startActivityForResult(intent, NEW_PIC);
				} catch (ActivityNotFoundException e) {
					// Toast.makeText(this, "This is not camera",
					// Toast.LENGTH_LONG);

				}
			}
		});

		// To View All Uploaded Files

		viewAll = (Button) findViewById(R.id.viewAll);
		scrollList = (LinearLayout) findViewById(R.id.scrollList);
		viewAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewPhoto lists = new ViewPhoto(myApi, PHOTO_DIR, handler);
				lists.execute();
			}
		});

		// To Record
		uRecord = (ImageButton) findViewById(R.id.mic);
		uRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
			
			});
		
		// For Text
					compose = (ImageButton) findViewById(R.id.compose);
					compose.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(DBApplication.this, TextEditor.class);
							startActivityForResult(intent, TEXT_CODE);
							
						}
					});
						
						

				
		setLoggedIn(myApi.getSession().isLinked());
	}

	/*
	 * These Methods are related to Authentication and LogIn
	 */

	private void checkKeySetup() {
		// Check if key valid key is added
		if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
			Toast.makeText(this, "Add APP Key and Secret ", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}
		// Check if Manifest.xml is correct
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			Toast.makeText(this, "Add AuthActivity to your app Manifest file",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void logOut() {

		myApi.getSession().unlink();
		clearStoredKeys();
		setLoggedIn(false);

	}

	private void clearStoredKeys() {
		// TODO Auto-generated method stub
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private void loadAuthr(AndroidAuthSession session) {
		// TODO Auto-generated method stub

		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String keyName = prefs.getString(ACCESS_KEY_NAME, null);
		String secretName = prefs.getString(ACCESS_SECRET_NAME, null);

		if (keyName == null || secretName == null || keyName.length() == 0
				|| secretName.length() == 0)
			return;

		if (keyName.equals("oauth2:")) {
			// Token is from oauth2
			session.setOAuth2AccessToken(secretName);
		} else {
			// Token is from oauth1
			session.setAccessTokenPair(new AccessTokenPair(keyName, secretName));
		}

	}

	private void storeAuth(AndroidAuthSession session) {
		// TODO Auto-generated method stub
		// Store oauth2 or oauth1 token
		String oauth2AccessToken = session.getOAuth2AccessToken();
		if (oauth2AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME,
					0);
			Editor edit = prefs.edit();
			edit.putString(ACCESS_KEY_NAME, "oauth2:");
			edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
			edit.commit();
			return;
		}

		AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
		if (oauth1AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME,
					0);
			Editor edit = prefs.edit();
			edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
			edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
			edit.commit();
			return;
		}
	}

	private void setLoggedIn(boolean loggedIn) {
		// TODO Auto-generated method stub
		uLoggedIn = loggedIn;
		if (loggedIn) {
			uSubmit.setText("Unlink from Dropbox");
			displayLayout.setVisibility(View.VISIBLE);
		} else {
			uSubmit.setText("Link with Dropbox");
			displayLayout.setVisibility(View.GONE);

		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AndroidAuthSession session = myApi.getSession();
		if (session.authenticationSuccessful()) {
			try {
				session.finishAuthentication();
				storeAuth(session);
				setLoggedIn(true);

			} catch (IllegalStateException e) {
				Log.i("DbAuthLog", "Error authenticating", e);
			}
		}
	}

	/*
	 * These Methods are related to Photo Uploads
	 */

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString("Photo Name", picName);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NEW_PIC) {
			// return from file upload
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = null;
				if (data != null) {
					uri = data.getData();
				}
				if (uri == null && picName != null) {
					uri = Uri.fromFile(new File(picName));
				}
				File file = new File(picName);

				if (uri != null) {
					UploadPhoto uploadPhoto = new UploadPhoto(this, myApi,
							PHOTO_DIR, file);
					uploadPhoto.execute();
				}
			} else {
				Log.w(TAG, "Unknown Activity Result from mediaImport: "
						+ resultCode);
			}
		}
		if (requestCode == TEXT_CODE){
			if(resultCode == RESULT_OK){
				String value = data.getExtras().getString(TEXT_CONTENT);
				String fileName = data.getExtras().getString(TEXT_NAME);
					
				Log.d("text", "Received" + value);
				UploadText uploadText = new UploadText(DBApplication.this, myApi,
						FILE_DIR,value,fileName);
				uploadText.execute();
			}
		}
	}

	/*
	 * These methods are related to Viewing all Uploaded Photos
	 */

	private final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			final ArrayList<String> result = msg.getData().getStringArrayList(
					"data");

			for (final String fileName : result) {

				Log.i("ListFiles", fileName);
				TextView tv = new TextView(DBApplication.this);

				tv.setText(fileName);

				scrollList.addView(tv);
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.d("msg", "You Clicked");
						Log.i("ListFiles", fileName);

						DownloadPhoto download = new DownloadPhoto(
								DBApplication.this, myApi, PHOTO_DIR,
								uImageView, result.indexOf(fileName));
						download.execute();

					}
				});

			}
			viewAll.setVisibility(View.GONE);
		}
	};


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
