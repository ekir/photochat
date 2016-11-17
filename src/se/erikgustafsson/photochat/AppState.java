package se.erikgustafsson.photochat;


import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.widget.ImageView;

/* This class is for data shared between activities
* Inspired by this tutorial
* http://www.jameselsey.co.uk/blogs/techblog/android-implementing-global-state-share-data-between-activities-and-across-your-application/
*/
public class AppState extends Application{
	// Documentation relevant for HashMap and SpareArray used in this class
	//http://java67.blogspot.se/2013/02/10-examples-of-hashmap-in-java-programming-tutorial.html
	//http://developer.android.com/reference/android/util/SparseArray.html#SparseArray%28%29
	
	// Contains the username of logged in user. Null means noone is logged in
	private String loggedin=null;
	
	// Store list of contacts
	private ArrayList<String> contacts = new ArrayList<String>();
	
	// Stores all the conversations in a hashmap mapped with strings. Every string is the user name for the corresponding conversation
	private HashMap<String, Conversation> conversation_map = new HashMap<String, Conversation>();
	
	// Contains the username for the currently opened chat. Null means no currently open chat
	private String openchat=null;
	
	// Stores the images in a cache, so we dont have to download them again all the time
	private SparseArray<Bitmap> image_cache = new SparseArray<Bitmap>();
	
	// The debug mode. Turn this on to give more debug information
	private static boolean debug_mode = false;
	
	// Constructor for our AttState application class
	public AppState() {
		// Sets cookie handler, so that we wouldnt lose our HTTP cookie-state between backend calls
    	CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
	}

	/*
	 * This class loads images in the background and them apply them to a ImageView and store them in cache
	 */
	class ImageLoader implements HttpHandler {
		// the id of the image in the backend
		private int mediaid;
		
		// A reference to the image cache
		// This reference should be pointed to the shared image cache
		private SparseArray<Bitmap> image_cache;
		// The image vire we want to load the image into
		private ImageView imageview;
		
		// Constructor. Initializes the image loading
		public ImageLoader(SparseArray<Bitmap> image_cache, Integer mediaid,ImageView imageview) {
			// Set values for image cache reference, mediaid and imageView
			this.image_cache=image_cache;
			this.mediaid=mediaid;
			this.imageview = imageview;
			// Start the download and set this as a callback (see receive method)
			new HttpDownloader("http://cchat.eglab.cc/?action=getmedia&parm1="+mediaid.toString(),this);
		}

		// Executed when the download is finished
		public void receive(byte[] raw_answer) {
			try {
			// Decode the downloaded bitmap
	    	Bitmap big = BitmapFactory.decodeByteArray(raw_answer, 0, raw_answer.length);
	    	// Put the bitmap in the cache
	    	image_cache.put(mediaid, big);
	    	// If we have the imageview set, then set the image in it
	    	// ImageLoader can also be used to load images without displaying them
	    	// That is if imageview is set to null
	    	if(this.imageview!=null) {
	    		imageview.setImageBitmap(big);
	    		imageview.setLayoutParams(BitmapUtils.imageLayoutParams(big));
	    	}
			} catch (Exception e) {
				// If we get some decode problem, we dont do anything
				// The image wouldnt be loaded
			}
		}
	}
	
	// Get an image from the cache and put it in an imageview
	// If the image is not in cache, load it first from the server with ImageLoader, then display it
	Bitmap get_image(Integer mediaid, ImageView view) {
		// If no image found in cache
		if(image_cache.get(mediaid)==null) {
			// Load the image and display it in the imageview
			new ImageLoader(image_cache,mediaid,view);
	    	return null;
		}
		// If it was found set the image in the imageview 
		view.setImageBitmap(image_cache.get(mediaid));
		// Return reference to the image in cache
		return image_cache.get(mediaid);
	}
	
	// Get a specific conversation
	// String username - the username to get conversation with
	Conversation get_conversation(String username) {
		// If the conversation does not already exist. Create it
		if(conversation_map.get(username)==null) {
			conversation_map.put(username, new Conversation());
		}
		// Return the conversation
		return conversation_map.get(username);
	}

	// Getter for debug mode
	public static boolean getDebugMode() {
		return debug_mode;
	}
	
	// Getter for contact list
	public ArrayList<String> getContacts() {
		return contacts;
	}

	// Getter for logged in
	public String getLoggedIn() {
		return loggedin;
	}

	// Setter for logged in
	public void setLoggedIn(String loggedin) {
		this.loggedin = loggedin;
	}

	// Getter for open chat
	public String getOpenChat() {
		return openchat;
	}

	// Setter for open chat
	public void setOpenChat(String openchat) {
		this.openchat = openchat;
	}
}
