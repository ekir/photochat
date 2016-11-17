package se.erikgustafsson.photochat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/*
 * The first activity launched. Used for logging in
 */
public class ActivityLogin extends Activity implements OnClickListener {
	// References to UI views
	private TextView login_out;
	private Button login_button;
	private TextView login_debug;
	private TextView text_username;
	private TextView text_password;
	private TextView text_register_here;
	// Reference to application state
	private AppState appstate;
	// Backend-wrapper. Makes calls to the backend easier
	private BackendWrapper backend = new BackendWrapper();;
	
	// On the creation of login activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        // Get the appstate object
    	appstate = (AppState) getApplication(); 
    }
    
	// Login activity starts
    public void onStart() {
    	super.onStart();
		// Get all the views
    	login_debug = (TextView)findViewById(R.id.login_debug);
    	login_button=(Button)findViewById(R.id.login_button);
		text_username=(TextView)findViewById(R.id.text_username);
		text_password=(TextView)findViewById(R.id.text_password);
		text_register_here=(TextView)findViewById(R.id.text_register_here);
		login_out = (TextView)findViewById(R.id.login_out);
		// Set onclick listeners for login button and register button to this activity
    	login_button.setOnClickListener(this);
    	text_register_here.setOnClickListener(this);
    	
    	// If we are in debugmode show the login debug view
    	if(AppState.getDebugMode()){
    		login_debug.setVisibility(View.VISIBLE);
    	}
    	// Get screen information
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		// Use screen information to configure BitmapUtils
		// This will later be used to calculate size of displayed images
		BitmapUtils.setDisplaySize(size);
		
		// If appstates logged in is not null, then open the contacts activity
		if(appstate.getLoggedIn()!=null) {
			// We skip the loggin screen and go directly to the contact activity
			ActivityLogin.this.finish();
			startActivity(new Intent(ActivityLogin.this, ActivityContacts.class));
		}
    }
    
    // onClick handler for login and register buttons
    public void onClick(View view) {
    	// Switch on id of the button clicked
		switch(view.getId()) {
		case R.id.login_button:
			// Try to login
			login();
			break;
		case R.id.text_register_here:
			// Start registration activity
			startActivity(new Intent(ActivityLogin.this, ActivityRegister.class));
			break;
		}
	}
	
    // Try to login
	private void login() {
		// Get inputed text for username and password
		String username=text_username.getText().toString();
		String password=text_password.getText().toString();
		// Inform the user that we try to login by changing text on loggin button
    	login_button.setText("Logging in");
    	
    	// Call the backend and ask to login
    	backend.call("login",username,password,new HttpHandler() {
    		// Event handler when result of login comes
			public void receive(byte[] raw_answer) {
				String answer = new String(raw_answer);
				// Parse the result
    			try {
    				JSONObject result = new JSONObject(answer);
    				int loginsuccess = result.getInt("loginsuccess");
    				// Check if the login was successfull
    				if(loginsuccess!=0) {
    					// If login was successfull close this activity and start ActivityContacts
    					appstate.setLoggedIn(text_username.getText().toString());
    					ActivityLogin.this.finish();
    					startActivity(new Intent(ActivityLogin.this, ActivityContacts.class));
    				} else {
    					// If login was not successful change the login button back to "login"
    			    	login_button.setText("Login");
    			    	// Display the message why login failed to the user
    			    	login_out.setText(result.getString("message"));
    				}
    				
    				
    			} catch(Exception e) {
    				// If the data we got is invalid give error message
        			login_out.setText("error parsing "+answer);
    			}
    			// Show debug information.
    			// This will only be visible in debugmode
    			login_debug.setText(answer);
    		}
    	});
	}
}
