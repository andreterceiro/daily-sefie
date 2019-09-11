package br.com.terceiro.dailyself;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ShowImageActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_show_image);
		this.setImageBitmap(
			getIntent().getExtras().getString(MainActivity.IMAGE_PATH_KEY)
		);
	}
	
	private void setImageBitmap(String imagePath) {
		ImageView largeImage =  (ImageView) this.findViewById(R.id.large_image);
		largeImage.setImageBitmap(
				//BitmapFactory.decodeFile(imagePath)
				getReducedPic(imagePath)
		);
	}
	
	/**
	 * Based on Dr. Adam setPic() suggestion
	 */
	private Bitmap getReducedPic(String imagePath) {
	    // Get the dimensions of the View
	    int maxWidth = 500;
	    int maxHeight = 500;

	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(imagePath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;

	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/maxWidth, photoH/maxHeight);

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;

	    return  BitmapFactory.decodeFile(imagePath, bmOptions);
	}	
}
