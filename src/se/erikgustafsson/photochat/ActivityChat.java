package se.erikgustafsson.photochat;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * The activity used for chatting
 */
public class ActivityChat extends Activity implements OnClickListener, HttpHandler {
	// References to UI views
	private TextView chat_debug;
	private LinearLayout chat_layout;
	private Button send_button;
	private EditText send_text;
	
	// Backend-wrapper. Makes calls to the backend easier
	private BackendWrapper backend = new BackendWrapper();
	// Reference to application state
	private AppState appstate;
	// Reference to the current conversation
	private Conversation conversation;

	// This makes sure we stop the chat updates when activity is not active
	private boolean running = false;
	
	// On the creation of chat activity
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_activity);
        // Get the appstate object
		appstate = (AppState) getApplication();
	}
	
	// Chat activity starts
	public void onStart() {
		super.onStart();
		// Get all the views
		chat_debug = (TextView) findViewById(R.id.chat_debug);
		chat_layout = (LinearLayout) findViewById(R.id.chat_layout);
		send_button = (Button) findViewById(R.id.send_button);
		send_text = (EditText) findViewById(R.id.send_text);
		
		// Tell the activity we are running. This effects auto update of chat
		running = true;
		
		// If debug mode is activitated show the debug log
		if (AppState.getDebugMode()) {
			chat_debug.setVisibility(View.VISIBLE);
		}

		// Set the title of the titlebar to the name of the user we are chatting with
		this.setTitle(appstate.getOpenChat());
		// Get conversation data for the current chat
		conversation = appstate.get_conversation(appstate.getOpenChat());

		// Send onClick listener for the send button
		send_button.setOnClickListener(this);
		// Redraw the conversation
		drawchat();
		// Start the auto update loop
		updatechat_startloop();
	}
	
	public void updatechat_startloop() {
		// Make a backend call to get messages
		// When we return from it we will get into the receive callback in this activity
		backend.call("getmessages", appstate.getOpenChat(), "0", this);
	}
	
	// Receive callback. This keeps the updates of the chat running with a 1 sec intervall
	public void receive(byte[] raw_answer) {
		String answer = new String(raw_answer);
		// If we are not currently running this activity we just stop
		if (running == false) {
			return;
		}
		// Handle the incoming messages
		messages_updatehandler(answer);
		// Call for another cycle of get messages. This is delayed 1000 ms=1 second
		// Which means the messages will be auto updated every second
		backend.call_delay("getmessages", appstate.getOpenChat(), "0", this, 1000);
	}
	
	// Create the options menu
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mif = getMenuInflater();
		// Build a menu from menu.chat_menu
		mif.inflate(R.menu.chat_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	// Even handler when item actionbar is clicked
	// It currently only contain the camera action
	public boolean onOptionsItemSelected(MenuItem Item) {
		switch (Item.getItemId()) {
		case R.id.menuitem_sendimage:
			// Launch the camera
			launch_camera();
			return true;
		}
		return super.onOptionsItemSelected(Item);
	}
	
	// Lunch camera activity
	public void launch_camera() {
		// Try to get external storage
		String temppath=try_getexternalstorage();
		if(temppath==null) {
			// The try_getexternalstorage already informs the user if somethings goes wrong
			return;
		}
		// Make a file to receive the image to
		File tempFile = new File(temppath+"/bigphoto.jpg");
		// Create an intent for launching the camera
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
		startActivityForResult(intent, 10);
	}

	// When we return after camera activity we come here
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=RESULT_OK) {
			// If no picture was taken. Dont do anything
			return;
		}
		// Try to get access to external storage
		String temppath=try_getexternalstorage();
		if(temppath==null) {
			// If we couldnt access the memory, dont do anything
			// The try_getexternalstorage already informs the user we couldnt access it
			return;
		}
		// Path for the image directly captured by the camera
		String bigimage_path = temppath
				+ "/bigphoto.jpg";
		
		// Load the camera image, and scale it so the maximum side is 1000 pixels
		Bitmap big = BitmapUtils.load_scaled(bigimage_path,1000);
		
		// Try to get information from the image to rotate it correctly
		try {
			ExifInterface exif = new ExifInterface(bigimage_path);
			// Extract exif orientation
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, 1);
			// Rotate based on exif orientation
			big = BitmapUtils.rotateBitmap(big, orientation);
		} catch (Exception e) {
			messagebox(e.toString());
		}
		
		
		// Save the scaled file
		
		// Path for the scaled file that should be created and sent
		String scaledimage_path = temppath
				+ "/scaledphoto.jpg";

		try {
			// Save the image to a compressed jpg for sending
			FileOutputStream stream_out = new FileOutputStream(scaledimage_path);
			big.compress(Bitmap.CompressFormat.JPEG, 90, stream_out);
			// Close the file
			stream_out.close();
		} catch (Exception e) {
			messagebox(e.getMessage());
			return;
		}
		// Send the scaled file to the receving user
		// This is done in two steps
		// 1. Upload the file to the server with HttpUploader
		// 2. Notify the receiving user with a message of type "image"
		
		// Uploading image file to server
		new HttpUploader("http://cchat.eglab.cc/?action=postmedia",
				scaledimage_path, new HttpHandler() {
			
					// When image file is uploaded we come here
					public void receive(byte[] raw_answer) {
						// Now we send a message to the receving user about it
						String answer = new String(raw_answer);
						try {
							// Extract media id of the uploaded image
							JSONObject mediadata = new JSONObject(answer);
							int mediaid = mediadata.getInt("id");
							// Send a message to the receiving user and include the media id
							sendmessage("image", "" + mediaid);
						} catch (Exception e) {
						}
					}
				});
	}
	
	// Make a dialog with only ok button and text in it
	public void makeOkDialog(String message) {
		// This method is based on the code found here
		//http://stackoverflow.com/questions/5810084/android-alertdialog-single-button
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   // We dont use this right now
		        	   // If we want to add an event for the Ok button we can do it here
		           }
		       });
		AlertDialog alert = builder.create();
		// SHow the dialog
		alert.show();
	}
	
	// Try to get external storage
	// If we fail inform the user about why
	// Return path to external storage, or null if failed
	public String try_getexternalstorage() {
		String storage_state = android.os.Environment.getExternalStorageState();
		if(storage_state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			// This is the only successfull case. The other ones are for different errors
			return this.getExternalCacheDir().getAbsolutePath();
		} else if (storage_state.equals(android.os.Environment.MEDIA_UNMOUNTED)) {
			// Media not mounted
			makeOkDialog("External storage is not mounted");
			return null;
		} else if (storage_state.equals(android.os.Environment.MEDIA_REMOVED)) {
			// Media not found
			makeOkDialog("No external storage found");
			return null;
		} else if (storage_state.equals(android.os.Environment.MEDIA_SHARED)) {
			// Media busy since its shared
			makeOkDialog("External storage currently shared over USB. Please unmount USB first");
			return null;
		} else if (storage_state.equals(android.os.Environment.MEDIA_CHECKING)) {
			// Media is currently being scanned
			makeOkDialog("External storage is currently being scanned. Try again in 10 seconds");
			return null;	
		} else {
			// Something else is wrong. Inform the user
			makeOkDialog("Could not access external storage: "+storage_state);
			return null;
		}
	}

	// This sends a message over the chat
	// String type - the type of message to send "image" or "text"
	// String content - What to send. Either a raw text for "text" or the id of the image
	public void sendmessage(String type, String content) {
		backend.call("sendmessage", appstate.getOpenChat(), type, content,
				new HttpHandler() {
					// This happens when returning after message sent
					public void receive(byte[] answer) {
						// Add a message to the conversation with the pending set to true
						// Pending set to true means it not sent yet
						conversation.add(new Message(0, "asd", appstate.getOpenChat(), "image",
								"message", true));
						// Redraw the chat
						drawchat();
					}

				});
	}

	// Process the data received when updating the chat
	public void messages_updatehandler(String answer) {

		// Show raw data in the debug view
		// This will only be visible if debug is on
		chat_debug.setText("answer: " + answer);
		
		//Construct conversatino based on json data
		conversation.clear();
		try {
			JSONObject result = new JSONObject(answer);
			JSONArray message_array_json = result.getJSONArray("message_array");
			// Loop over all messages
			for (int i = 0; i < message_array_json.length(); i++) {
				// Parse JSON for message data
				JSONObject message = message_array_json.getJSONObject(i);
				int id = message.getInt("id");
				String type = message.getString("type");
				String content = message.getString("content");
				String sender = message.getString("sender");
				
				// Create new message objects for every message
				Message m = new Message();
				m.setId(id);
				m.setType(type);
				m.setContent(content);
				m.setSender(sender);
				// Put the new message object in the conversation
				conversation.add(m);
			}
		} catch (Exception e) {
			messagebox(e.toString());
		}
		// Redraw chat
		drawchat();
	}


	// Draw the messages in the conversation
	public void drawchat() {
		chat_layout.removeAllViews();
		// Get the messages
		ArrayList<Message> message_array = conversation.getMessageArray();
		// Message reference to use when looping
		Message m;
		// Set an optinal limit on how many messages to display to reduce memory consumption
		// This should be configurable in the future
		int display_limit = 30;
		int display_count = 0;
		for (int i = message_array.size() - 1; i >= 0; i--) {
			// This display limit counter could have been integrated into the for-loop
			// But i choosed not to do it, since I plan to change the display limit feature in the future
			display_count++;
			if(display_limit!=0 && display_count>=display_limit) {
				break;
			}
			// Set message reference for the message we are currently working with
			m = message_array.get(i);
			// Handeling of text messages
			if (m.getType().compareTo("text") == 0) {
				if (m.getPending()) { // Pending message
					addchatline("Sending message... ");
				} else { // Normal message
					if (AppState.getDebugMode()) {
						addchatline(m.getId() + " " + m.getSender() + ": " + m.getContent());
					} else {
						addchatline(m.getSender() + ": " + m.getContent());
					}
				} // Handeling of image messages
			} else if (m.getType().compareTo("image") == 0) {
				if (m.getPending()) { // Pending message
					addchatline("Sending image... ");
				} else { // Normal message
					if (AppState.getDebugMode()) {
						addchatline(m.getSender() + " sent a picture " + m.getContent());
					} else {
						addchatline(m.getSender() + " sent a picture");
					}
					// Create an image view for the new image
					ImageView newimage = new ImageView(ActivityChat.this);

					// Get the media id from the message
					Integer mediaid;
					try {
						mediaid = Integer.parseInt(m.getContent());
					} catch (Exception e) {
						if (AppState.getDebugMode()) {
							messagebox("parsing error of " + m.getContent());
						}
						mediaid = 1;
					}
					// Apply the image to the imageview
					Bitmap resultBitmap = appstate.get_image(
							Integer.valueOf(mediaid), newimage);
					// Apply layout to the new imageview
					LayoutParams layoutParams = BitmapUtils
							.imageLayoutParams(resultBitmap);

					// Add the item to the chat
					chat_layout.addView(newimage, layoutParams);
				}
			}
		}
		// If we are in debug mode tell when the chat is refreshed
		if (AppState.getDebugMode()) {
			messagebox("redrawing chat");
		}
	}

	// Ad a line of text to the chat
	// String newline - the line to add
	private void addchatline(String newline) {
		TextView a = new TextView(ActivityChat.this);
		a.setText(newline);
		chat_layout.addView(a);
	}

	// The chat activity stops
	public void onStop() {
		super.onStop();
		// Remember we stopped so we can cancel the update until activity starts again
		running = false;
	}

	// Event handler when user click the send button
	public void onClick(View arg0) {
		// Get message to send
		String message = send_text.getText().toString();
		// Encode message to utf8
		try {
			message = URLEncoder.encode(message, "UTF-8");
		} catch (Exception e) {

		}
		// Clear the send text view
		send_text.setText("");
		// Call the backend about sending the message
		backend.call("sendmessage", appstate.getOpenChat(), "text", message,
				new HttpHandler() {

					// HttpHandler callback when sending is finished
					public void receive(byte[] raw_answer) {
						conversation.add(new Message(0, "asd", appstate.getOpenChat(), "text",
								"message", true));
						// Redraw chat
						drawchat();
					}

				});
	}

	// Messagebox. A simplified wrapper for showing a message in a toast
	private void messagebox(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
