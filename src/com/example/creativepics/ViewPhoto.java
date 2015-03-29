package com.example.creativepics;

import java.util.ArrayList;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ViewPhoto extends AsyncTask<Void, Void, ArrayList<String>>{

	private DropboxAPI<AndroidAuthSession> myApi;
	private String uPath;
	private Handler handler;
	
	
	public ViewPhoto(DropboxAPI<AndroidAuthSession> myApi, String uPath,
			Handler handler) {
		super();
		this.myApi = myApi;
		this.uPath = uPath;
		this.handler = handler;
	}


	@Override
	protected ArrayList<String> doInBackground(Void... params) {
		// TODO Auto-generated method stub
		ArrayList<String> files = new ArrayList<String>();
		try {
			Entry directory = myApi.metadata(uPath, 1000, null, true, null);
			for (Entry entry : directory.contents) {
				files.add(entry.fileName());
			}
		} catch (DropboxException e) {
			e.printStackTrace();
		}

		return files;
	}


	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(ArrayList<String> result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Message msgObj = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putStringArrayList("data", result);
		msgObj.setData(b);
		handler.sendMessage(msgObj);
	}

	

}
