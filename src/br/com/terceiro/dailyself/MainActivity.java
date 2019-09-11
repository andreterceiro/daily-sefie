package br.com.terceiro.dailyself;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {

	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final long TWO_MINUTES = 2 * 60 * 1000L;
	static final int THUMB_WIDTH = 100;
	static final String IMAGE_PATH_KEY = "imagePath";
	static final String JPEG_FILE_PREFIX = "DS_IMG_";
	static final String JPEG_FILE_SUFFIX = ".jpg";	
	static final String THUMB_FILE_PREFIX = "THUMB_";
	static final String STATE_IMAGE_PATH = "stateImagePath";
	static final String IMAGE_PATH_SUFIX =  "/DCIM/DailySelfie/";
	
	private AlarmManager mAlarmManager;
	private Intent mNotificationReceiverIntent;
	private PendingIntent mNotificationReceiverPendingIntent;
	
	private String mCurrentPhotoPath;	
	
	private ListView list;
	private ArrayList<File> thumbsPaths; 
	private ImagesFinder imagesFinder;
	private ImageAndPathAdapter listAdapter;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		// Avoid crash because orientation change when getting photo
		if (savedInstanceState != null) {
	        mCurrentPhotoPath = savedInstanceState.getString(STATE_IMAGE_PATH);
		} 
		
		this.imagesFinder = new ImagesFinder();
		
		try {
			this.thumbsPaths = this.imagesFinder.listImages(this.getAlbumDir(), THUMB_FILE_PREFIX);
			
			listAdapter = new ImageAndPathAdapter(this, R.layout.list_item, this.thumbsPaths);
			
			list = (ListView) findViewById(R.id.photos_list);
		
			list.setAdapter(listAdapter);
			
		    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
		        @Override
		        public void onItemClick(AdapterView<?> parent, View view,
		                                int position1, long id) {
		        	
		        	Intent intent = new Intent(MainActivity.this, ShowImageActivity.class);
		        	intent.putExtra(
		           			IMAGE_PATH_KEY, 
		        			MainActivity.this.extractLargeImagePath(
		    					MainActivity.this.listAdapter.getImagePathAtPosition(position1)
							)
					);
		        	
		        	startActivity(intent);
		         }
		    });		
		
		    this.configureNotificationAlarm();
		} catch (IOException e) {
			showErrorMessage("An IO error occurred. Please check if the external " +
					"media is inserted, is not out of space and try again.");
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_camera) {
			this.dispatchTakePictureIntent();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			File f = null;
			
			try {				
				f = setUpPhotoFile();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);				
			} catch (IOException e) {
				this.showErrorMessage("Sorry. An error occurred when creating the selfie file. Please try again.");
				f = null;
				mCurrentPhotoPath = null;
			}
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_IMAGE_CAPTURE == requestCode && resultCode == Activity.RESULT_OK) {	
			String thumbPath = this.createThumbnail();
			this.pushImageToList(thumbPath);
		}
	}
	
	private void pushImageToList(String path) {
		this.listAdapter.add(
				new File(
						path
				)
		);		
	}
	
	
	private void configureNotificationAlarm() {
		mNotificationReceiverIntent = new Intent(MainActivity.this,
				AlarmNotificationBroadcastReceiver.class);
						
		mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
				MainActivity.this, 0, mNotificationReceiverIntent, 0);
				
		this.mAlarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		this.mAlarmManager.setRepeating(
			AlarmManager.ELAPSED_REALTIME_WAKEUP,
			SystemClock.elapsedRealtime() + MainActivity.TWO_MINUTES,
			MainActivity.TWO_MINUTES,
			mNotificationReceiverPendingIntent
		);
	}
	
	private String extractLargeImagePath(File thumb) {
		String absolutePath = thumb.getAbsolutePath();
		String directoryPath = absolutePath.substring(
				0, 
				absolutePath.length() - thumb.getName().length()
		);
		
		String imageName = thumb.getName().substring(THUMB_FILE_PREFIX.length()); 
		
		return directoryPath  + imageName;
	}	
	
	private File getAlbumDir() throws IOException{
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = new File (Environment.getExternalStorageDirectory()
					+ IMAGE_PATH_SUFIX); 
			
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						throw new IOException("An error occurred when creating the album directory");
						//Log.d("CameraSample", "failed to create directory");
						//return null;
					}
				}
			}
			
		} else {
			//this.showErrorMessage("Sorry. External storage is not mounted READ/WRITE.");
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
			throw new IOException("External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}
	
	/**
	 * Based on Dr. Adam setPic() suggestion
	 * 
	 * Part of this method and ShowImageActivity.getReducedPic() could
	 * be one method in a helper class. 
	 */
	private String createThumbnail()
	{
	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    int photoW = bmOptions.outWidth;

	    // Determine how much to scale down the image
	    int scaleFactor = photoW  / THUMB_WIDTH;

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;

	    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    
		FileOutputStream out = null;
		String originalFilename = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1);
		String originalPath = mCurrentPhotoPath.substring(0, mCurrentPhotoPath.lastIndexOf("/") + 1);			
		String thumbPath = originalPath + THUMB_FILE_PREFIX + originalFilename;
		
		try {			
			out = new FileOutputStream(thumbPath);
		    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
			this.showErrorMessage("Sorry. An error occurred when creating the thumbnail.");
			thumbPath = null;
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
	    return thumbPath;
	}
	
	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}		
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user's current game state
	    savedInstanceState.putString(STATE_IMAGE_PATH, mCurrentPhotoPath);
	    
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	private void showErrorMessage(String message) {		
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
