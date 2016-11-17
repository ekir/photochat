package se.erikgustafsson.photochat;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * The activity for adding new contacts
 */
public class ActivityAddContact extends Activity implements OnClickListener {
	// References to UI views
	private TextView addcontact_out;
	private EditText edit_add_contact;
	private Button button_add_contact;
	// Backend-wrapper. Makes calls to the backend easier
	private BackendWrapper backend = new BackendWrapper();

	// On the creation of add contact activity
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addcontact_activity);
	}

	// Add contact activity starts
	public void onStart() {
		super.onStart();
		// Get all the views
		addcontact_out = (TextView) findViewById(R.id.addcontact_out);
		edit_add_contact = (EditText) findViewById(R.id.edit_add_contact);
		button_add_contact = (Button) findViewById(R.id.button_add_contact);
		// Add this as action listner to the add contact button
		button_add_contact.setOnClickListener(this);
	}

	// Event handler when add contact is clicked
	public void onClick(View view) {
		// Get the username we want to add
		String contact_username = edit_add_contact.getText().toString();
		
		// Call the backend about the username we want to add
		backend.call("addcontact", contact_username, new HttpHandler() {

			// HttpHandler callback when done
			public void receive(byte[] raw_answer) {
				String answer = new String(raw_answer);
				
				// Parse result
				try {
					JSONObject result = new JSONObject(answer);
					String message = result.getString("message");
					int success = result.getInt("success");
					// If we succeded go back to the activity we came from
					if (success == 1) {
						finish();
					} else {
						// Otherwise dsplay a message what happened
						addcontact_out.setText(message);
					}
				} catch (Exception e) {

				}
			}

		});

	}
}
