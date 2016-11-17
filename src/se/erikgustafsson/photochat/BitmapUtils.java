package se.erikgustafsson.photochat;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

/*
 * A set of different methods to work with graphics
 */
public class BitmapUtils {
	// Used to calculate the size of ImageViews in imageLayoutParams
	private static int display_width = 0;
	
	// This function is used to initially store the display size for the screen
	// The size of the screen is used to calculate the size of the imageViews
	public static void setDisplaySize(Point size) {
		BitmapUtils.display_width=size.x;
	}

	// Calculate the height/width ratio for an image
	static float BitmapRatio(Bitmap bitmap) {
		return ((float) bitmap.getHeight()) / ((float) bitmap.getWidth());
	}
	
	// Gets the max side of an image file in pixels
	// The max side is the longest side, the biggest one of width and height
	public static int imagefile_maxside(String image_path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// Dont load, just get information
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image_path, options);
		if(options.outWidth>options.outHeight) {
			// If width is bigger, return width
			return options.outWidth;
		} else {
			// If height is bigger, return hight
			return options.outHeight;
		}
	}
	
	// Load an image as Bitmap, and scale it so that biggest side(width or hight) fits maxside
	public static Bitmap load_scaled(String bigimage_path,int maxside) {
		// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		// Calculate samplesize depending on the imagefiles pixelsize
		// We want the smallest possible image, without going below maxsize
		int samplesize=(int)Math.floor(imagefile_maxside(bigimage_path)/maxside);
		options.inSampleSize=samplesize;
		
		// Decode the image file
		Bitmap big = BitmapFactory.decodeFile(bigimage_path,options);
		
		// Scale the image to maxsize
		float ratio = BitmapRatio(big);
		if(ratio < 1) {
			// If height/width < 1 
			big = Bitmap.createScaledBitmap(big, maxside,
					(int) (maxside * ratio), false);
		} else {
			// If height/width >= 1 
			big = Bitmap.createScaledBitmap(big, (int) (maxside/ratio),
					maxside, false);		
		}
		return big;
	}

	// Rotates an image correctly depending on its original Exif orientation
	// Bitmap bitmap - the bitmap to correctly rotate
	// int orientation - the orientation parameter in Exif
	public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
		// Here I read about Exif orientations
		// http://sylvana.net/jpegcrop/exif_orientation.html
		// I found this function and took most of the code from this rotation-function from here
		// http://stackoverflow.com/questions/20478765/how-to-get-the-correct-orientation-of-the-image-selected-from-the-default-image
		try {
			Matrix matrix = new Matrix();
			switch (orientation) {
			case ExifInterface.ORIENTATION_NORMAL:
				// Dont do anything
				return bitmap;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				// Flip horizontially
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				// Rotate 180 degrees
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				// Flip verticaly
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				// Transpose orientation
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:
				// Transverse orientation
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				// Rotate 270 degrees
				matrix.setRotate(-90);
				break;
			default:
				// If none of them, just return the original bitmap
				return bitmap;
			}
			// Create a new bitmap based on the orientation
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
			return bitmap;
		} catch (Exception e) {
			// If we failed with the orientation for some reason, just return the original picture
			return bitmap;
		}
	}

	// This calculates the size of an imageView depending on the size of the screen
	// And the size of the image to show
	// Bitmap bmp - the bitmap to show
	public static LayoutParams imageLayoutParams(Bitmap bmp) {
		// Make it horzintally match_parent and verticly wrap_content
		LinearLayout.LayoutParams imageLayout = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		// We want the picture to take up 80% of the screen width
		float part_of_screen = (float) 0.8;
		imageLayout.width = (int) (display_width);
		if (bmp != null) {
			// If the image we are displying is loaded we calculate the width/height ratio
			float ratio = BitmapUtils.BitmapRatio(bmp);
			// Then calculate height from width and ratio
			imageLayout.height = (int) (part_of_screen * display_width * ratio);
		} else {
			// If the image we are displying is not loaded we just assume that it is of form width=height
			// Then set our height to be the same as width
			imageLayout.height = (int) (part_of_screen * display_width * 1);
		}
		// Return the layoutParams for the image
		return imageLayout;
	}
}
