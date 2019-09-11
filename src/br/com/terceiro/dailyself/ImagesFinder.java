package br.com.terceiro.dailyself;

import java.io.File;
import java.util.ArrayList;

public class ImagesFinder {
	public ArrayList<File> listImages (File directory, String prefixFilter) {
		ArrayList<File> imagesList = new ArrayList<File>();
		
		if (directory.exists() && directory.isDirectory()) {
	        File[] files = directory.listFiles();
	        for (int i = 0; i < files.length; ++i) {
	            File file = files[i];
	            if (! file.isDirectory()) {
	            	String filePrefix = file.getName().substring(0, prefixFilter.length());

            		if (prefixFilter != null && (! filePrefix.equals(prefixFilter)) ) {
            			continue;
            		}
            		
            		imagesList.add(file);
	            }
            }
	    }
	    
	    return imagesList ;
	}
	
	public ArrayList<File> listImages (File directory) {
		return this.listImages(directory, null);
	}
}
