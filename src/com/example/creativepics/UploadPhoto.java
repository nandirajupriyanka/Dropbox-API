package com.example.creativepics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

public class UploadPhoto extends AsyncTask<Void, Long, Boolean> {

	private DropboxAPI<AndroidAuthSession> myApi;
	private String uPath;
	private File uFile;

	private long uFileLen;
	private UploadRequest uRequest;
	private Context mContext;
	private final ProgressDialog pd;

	private String errorMsg;

	public UploadPhoto(Context context, DropboxAPI<AndroidAuthSession> api, String dropboxPath,
			File file) {
		// We set the context this way so we don't accidentally leak activities
		mContext = context.getApplicationContext();

		uFileLen = file.length();
		myApi = api;
		uPath = dropboxPath;
		uFile = file;
		
		pd = new ProgressDialog(context);
		pd.setMax(100);
		pd.setMessage("Uploading " + file.getName());
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setProgress(0);
		pd.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel",
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// This will cancel the putFile operation
						uRequest.abort();
					}
				});
		pd.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
		try {

			FileInputStream fis = new FileInputStream(uFile);
			String path = uPath + uFile.getName();
			uRequest = myApi.putFileOverwriteRequest(path, fis, uFile.length(),
					new ProgressListener() {
						@Override
						public long progressInterval() {
							// Update the progress bar every half-second or so
							return 500;
						}

						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes);
						}
					});

			if (uRequest != null) {
				uRequest.upload();
				return true;
			}

		} catch (DropboxUnlinkedException e) {
			errorMsg = "This app wasn't authenticated properly.";
		} catch (DropboxFileSizeException e) {
			errorMsg = "This file is too big to upload";
		} catch (DropboxPartialFileException e) {
			errorMsg = "Upload canceled";
		} catch (DropboxServerException e) {
			if (e.error == DropboxServerException._401_UNAUTHORIZED) {
			} else if (e.error == DropboxServerException._403_FORBIDDEN) {
			} else if (e.error == DropboxServerException._404_NOT_FOUND) {
			} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
			} else {
				// Something else
			}
			// Translate Error
			errorMsg = e.body.userError;
			if (errorMsg == null) {
				errorMsg = e.body.error;
			}
		} catch (DropboxIOException e) {
			errorMsg = "Network error.  Try again.";
		} catch (DropboxParseException e) {
			errorMsg = "Dropbox error.  Try again.";
		} catch (DropboxException e) {
			errorMsg = "Unknown error.  Try again.";
		} catch (FileNotFoundException e) {
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		pd.dismiss();
		if (result) {
			showToast("Image successfully uploaded");
		} else {
			showToast(errorMsg);
		}
	}

	@Override
	protected void onProgressUpdate(Long... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		  int percent = (int)(100.0*(double)values[0]/uFileLen + 0.5);
	        pd.setProgress(percent);
	}

	private void showToast(String s) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(mContext, s, Toast.LENGTH_LONG);
		toast.show();
	}
}
