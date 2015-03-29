package com.example.creativepics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class UploadText extends AsyncTask<Void, Long, Boolean> {

	private DropboxAPI<AndroidAuthSession> myApi;
	private String uPath;
	private Context mContext;
	private String uText;
	private String ufileName;
	
	
	public UploadText(
			Context mContext, DropboxAPI<AndroidAuthSession> myApi,String uPath, String uText, String ufileName) {
		super();
		this.myApi = myApi;
		this.uPath = uPath;
		this.mContext = mContext;
		this.uText = uText;
		this.ufileName = ufileName;
		Log.d("text", uText);
	}


	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
		final File tempDir = mContext.getCacheDir();
		File tempFile;
		FileWriter fr;
		try {
			tempFile = File.createTempFile("file", ".txt", tempDir);
			fr = new FileWriter(tempFile);
			fr.write(uText);
			fr.close();

			FileInputStream fileInputStream = new FileInputStream(tempFile);
			myApi.putFile(uPath + ufileName + ".txt", fileInputStream,
					tempFile.length(), null, null);
			tempFile.delete();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DropboxException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (result) {
			Toast.makeText(mContext, "File Uploaded Sucesfully!",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "Failed to upload file", Toast.LENGTH_LONG)
					.show();
		}
	}

}
