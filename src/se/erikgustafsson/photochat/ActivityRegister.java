package se.erikgustafsson.photochat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * The activity for registering new users
 */
public class ActivityRegister extends Activity implements HttpHandler,OnClickListener{
	// References to UI views
	private EditText edit_register_username;
	private EditText edit_register_password;
	private Button button_register_user;
	private TextView register_out;
	private TextView register_debug;
	// Reference to application state
	private AppState appstate;
	// Backend-wrapper. Makes calls to the backend easier
	private BackendWrapper backend = new BackendWrapper();
	
	// On the creation of registration activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        // Get the appstate object
        appstate = (AppState)getApplication();
    }

	// Registration activity starts
	protected void onStart() {
		// Get all the views
		register_debug=(TextView)findViewById(R.id.register_debug);
		register_out=(TextView)findViewById(R.id.register_out);
		edit_register_username=(EditText)findViewById(R.id.edit_register_username);
		edit_register_password=(EditText)findViewById(R.id.edit_register_password);
		button_register_user=(Button)findViewById(R.id.button_register_user);
		// Register this as an onclick listener for the register button
		button_register_user.setOnClickListener(this);
		super.onStart();
	}
	
	// Reciever method for Http requests. Use when trying to register user
	public void receive(byte[] raw_answer) {
		String answer = new String(raw_answer);
		if(AppState.getDebugMode()) { 
			// If we are in debug mode we display incoming http data
			register_debug.setText(answer);
		}
		try {
			// Parse incoming http data as JSON
			JSONObject result = new JSONObject(answer);
			int registersuccess=result.getInt("registersuccess");
			// Display message from the backend
			register_out.setText(result.getString("message"));
			
			// If we successfully registered
			if(registersuccess!=0) {
				// Set appstates login state
				appstate.setLoggedIn(edit_register_username.getText().toString());
				// Close this activity
				ActivityRegister.this.finish();
				// Start the contacts activity
				startActivity(new Intent(ActivityRegister.this, ActivityContacts.class));
			}
		} catch (Exception e) {
			
		}
	}

	// Click handler for register button
	public void onClick(View arg0) {
		backend.call("registeruser",edit_register_username.getText().toString(),edit_register_password.getText().toString(), this);
	}	
}
