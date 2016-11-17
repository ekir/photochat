package se.erikgustafsson.photochat;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * The activity displaying the contacts
 */
public class ActivityContacts extends Activity implements OnItemClickListener {
	// References to UI views
	private View welcome_message;
	private TextView contacts_log;
	private ListView contacts_list;
	// Reference to application state
	private AppState appstate;
	// Backend-wrapper. Makes calls to the backend easier
	private BackendWrapper backend = new BackendWrapper();
	// Store reference to applications list of contacts
	private ArrayList<String> contacts;
	
	// Adapter used for the contact list
	private ArrayAdapter<String> adapter;

	// On the creation of contacts activity
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_activity);
        // Get the appstate object
		appstate = (AppState) getApplication();
		// Get contacts from appstate
		contacts = appstate.getContacts();
	}

	// contacts activity starts
	public void onStart() {
		super.onStart();
		// Get all the views
		welcome_message = findViewById(R.id.welcome_message);
		contacts_log = (TextView) findViewById(R.id.login_debug);
		contacts_list = (ListView) findViewById(R.id.contacts_list);
		
		// Make an array adapter for the contact list based on the contacts array
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, contacts);
		contacts_list.setAdapter(adapter);
		
		// Set onclick-listener for the contact list
		contacts_list.setOnItemClickListener(this);

		// Register context menu for contact list
		registerForContextMenu(contacts_list);

		// Make the UI log visible if we are in debug mode
		if (AppState.getDebugMode()) {
			contacts_log.setVisibility(View.VISIBLE);
		}

		// Load the contacts from the backend
		backend.call("getcontacts", new HttpHandler() {

			// Receive callback when contacts are received
			public void receive(byte[] raw_answer) {
				String answer = new String(raw_answer);
				// Parse the retrived contacts
				try {
					JSONArray array = new JSONArray(answer);
					// Build up the contact arrau from the data we received
					contacts.clear();
					for (int i = 0; i < array.length(); i++) {
						String contact_username = array.getString(i);
						contacts.add(contact_username);
					}
					// If we dont have any contacts show the welcome message
					// The welcome message explains how to add contacts
					if (array.length() == 0) {
						welcome_message.setVisibility(View.VISIBLE);
					} else {
						welcome_message.setVisibility(View.GONE);
					}
				} catch (Exception e) {

				}
				// Update the contact adapter
				adapter.notifyDataSetChanged();
				// Set message in the debuglog. This is only visible in debugmode
				contacts_log.setText(answer);
			}

		});
		// If we are in debugmode download the session state and display
		// This is very useful to see that the server has registered our login and user is correct
		// Only for debuging purposes
		if (AppState.getDebugMode()) {
			backend.call("session", new HttpHandler() {
				@Override
				public void receive(byte[] raw_answer) {
					messagebox(new String(raw_answer));
				}
			});
		}
	}
	
	// Event handler when action from the menu is clicked
	public boolean onOptionsItemSelected(MenuItem Item) {
		// Choose action depending on the action id that is clicked
		switch (Item.getItemId()) {
		// Add new contact
		case R.id.menuitem_add_contact:
			// Show the dialog for adding new contact
			startActivity(new Intent(ActivityContacts.this,
					ActivityAddContact.class));
			return true;
		// Logout
		case R.id.menuitem_logout:
			// Set the login state to null. We are no longer logged in
			appstate.setLoggedIn(null);
			// Send the user back to the login screen
			startActivity(new Intent(ActivityContacts.this, ActivityLogin.class));
			finish();
			return true;
		}
		return super.onOptionsItemSelected(Item);
	}

	// Create context menu. This context menu is used for the contact list
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mif = getMenuInflater();
		// Construct the menu from the resource menu.contact_popup_menu
		mif.inflate(R.menu.contact_popup_menu, menu);
		return;
	}

	// Event handler when something is selected in the context menu
	// The only thing that can currently be done on the context menu is removing users from contacts
	public boolean onContextItemSelected(MenuItem item) {
		// This method is build with help of this tutorial
		// http://stackoverflow.com/questions/2321332/detecting-which-selected-item-in-a-listview-spawned-the-contextmenu-android
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		
		// Display a message about user being removed
		messagebox("Removing contact  " + contacts.get((int) info.id));
		String contact_username = contacts.get((int) info.id);
		// Call the backend to remove the user from the contact list
		backend.call("delcontact", contact_username, new HttpHandler() {

			@Override
			public void receive(byte[] raw_answer) {
				// When contact has been removed, recreate the contacts activity
				ActivityContacts.this.recreate();

			}
		});
		super.onContextItemSelected(item);
		return true;
	}

	// Create the options menu
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mif = getMenuInflater();
		// Create options menu from the resource menu.contacts_menu
		mif.inflate(R.menu.contacts_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	// onClick handler for contact list
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Find username contact that was clicked
		String username = contacts.get(arg2);
		// Tell the appstate we currently have a convesation with that user
		appstate.setOpenChat(username);
		// Start the chat activity
		startActivity(new Intent(ActivityContacts.this, ActivityChat.class));
		messagebox("Chatting with " + contacts.get(arg2));
	}

	// Messagebox. A simplified wrapper for showing a message in a toast
	private void messagebox(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
