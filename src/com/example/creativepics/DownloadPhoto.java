package com.example.creativepics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

public class DownloadPhoto extends AsyncTask<Void, Long, Boolean> {

	private Context mContext;
	private final ProgressDialog pd;
	private DropboxAPI<?> myApi;
	private String uPath;
	private ImageView uView;
	private Drawable uDrawable;
	private int uPic;

	private FileOutputStream uFos;

	private boolean uCanceled;
	private Long uFileLen;
	private String errorMsg;
	private final static String IMAGE_FILE_NAME = "pia.png";
	

	  public DownloadPhoto(Context context, DropboxAPI<?> api,
	            String dropboxPath, ImageView view, Integer fileName) {

	        // We set the context this way so we don't accidentally leak activities
	        mContext = context.getApplicationContext();

	        myApi = api;
	        uPath = dropboxPath;
	        uView = view;
	        uPic = fileName;

	        pd = new ProgressDialog(context);
	        pd.setMessage("Downloading Image");
	        pd.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                uCanceled = true;
	                errorMsg = "Canceled";

	                // This will cancel the getThumbnail operation by closing
	                // its stream
	                if (uFos != null) {
	                    try {
	                        uFos.close();
	                    } catch (IOException e) {
	                    }
	                }
	            }
	        });

	        pd.show();
	  }
	// Note that, since we use a single file name
	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
		  try {
	            if (uCanceled) {
	                return false;
	            }
	            //MetaData
	            Entry dirent = myApi.metadata(uPath, 1000, null, true, null);

	            if (!dirent.isDir || dirent.contents == null) {
	                errorMsg = "File or empty directory";
	                return false;
	            }
	            //List of Thumbnails
	            ArrayList<Entry> thumbs = new ArrayList<Entry>();
	            for (Entry ent: dirent.contents) {
	                if (ent.thumbExists) {
	                    thumbs.add(ent);
	                }
	            }

	            if (uCanceled) {
	                return false;
	            }

	            if (thumbs.size() == 0) {
	                errorMsg = "No pictures in that directory";
	                return false;
	            }

	            // Now pick a random one
	           
	            /*int index = (int)(Math.random() * thumbs.size());
	            Entry ent = thumbs.get(index);
	            String path = ent.path;
	            uFileLen = ent.bytes;*/

	           //int index = thumbs.indexOf(uPic);
	            
	            Entry ent = thumbs.get(uPic);
	            String path = ent.path;
	            uFileLen = ent.bytes;
	            
	            

	            String cachePath = mContext.getCacheDir().getAbsolutePath() + "/" + IMAGE_FILE_NAME;
	            try {
	                uFos = new FileOutputStream(cachePath);
	            } catch (FileNotFoundException e) {
	                errorMsg = "Couldn't create a local file to store the image";
	                return false;
	            }
	            myApi.getThumbnail(path, uFos, ThumbSize.BESTFIT_960x640,
	                    ThumbFormat.JPEG, null);
	            if (uCanceled) {
	                return false;
	            }

	            uDrawable = Drawable.createFromPath(cachePath);
	            // We must have a legitimate picture
	            return true;

	        } catch (DropboxUnlinkedException e) {
	        } catch (DropboxPartialFileException e) {
	                  errorMsg = "Download canceled";
	        } catch (DropboxServerException e) {
	            
	            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
	               
	            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
	               
	            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
	               
	            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
	              
	            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
	               
	            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
	                
	            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
	               
	            } else {
	                // Something else
	            }
	            
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
	        }
	        return false;
	}
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		 pd.dismiss();
	        if (result) {
	            // Set the image now that we have it
	            uView.setImageDrawable(uDrawable);
	        } else {
	            // Couldn't download it, so show an error
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

	private void showToast(String errorMsg) {
		// TODO Auto-generated method stub
		 Toast error = Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG);
	        error.show();
	}
}
