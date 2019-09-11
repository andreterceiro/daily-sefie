package br.com.terceiro.dailyself;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAndPathAdapter extends ArrayAdapter<File>{
	private Context context;
	private int layoutResource;
	private ArrayList<File> imagesFiles;
	
	public ImageAndPathAdapter(Context context, int layoutResource, ArrayList<File> imagesFiles) {
		super(context, layoutResource, imagesFiles);
		
		this.context = context;
		this.layoutResource = layoutResource;
		this.imagesFiles = imagesFiles;
	}
	
	@SuppressLint("ViewHolder")
	public View getView(int position, View view , ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(this.layoutResource, viewGroup, false);
		
		// Setting the text value
		TextView imageDescription = (TextView) rowView.findViewById(R.id.thumbDescription);
		imageDescription.setText(
				this.imagesFiles.get(position).getAbsolutePath()
		);
		
		// Setting the image
		ImageView image = (ImageView) rowView.findViewById(R.id.thumb);
		Bitmap imageContent = BitmapFactory.decodeFile(
				this.imagesFiles.get(position).getAbsolutePath()
		);
		image.setImageBitmap(imageContent);

		// Returning the view line
		return rowView;
	}
	
	public File getImagePathAtPosition(int position) {
		return this.imagesFiles.get(position);
	}
	
	public void add(File imageFile) {
		this.imagesFiles.add(imageFile);
		this.notifyDataSetChanged();
	}
}
