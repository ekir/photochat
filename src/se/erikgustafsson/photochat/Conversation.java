package se.erikgustafsson.photochat;

import java.util.ArrayList;

/*
 * Stores a conversation
 * A conversation is the interaction between two users
 * Normally the apps logged in user and another user the logged in user is chatting with
 */
public class Conversation {
	// Array of messages in the conversation
	private ArrayList<Message> message_array = new ArrayList<Message>();
	
	// Clears the conversation
	public void clear() {
		message_array = new ArrayList<Message>();
	}
	
	// Add a new message to the conversation
	public void add(Message m) {
		message_array.add(m);
	}
	
	// Get the messages in the conversation
	public ArrayList<Message> getMessageArray(){
		return message_array;
	}
}
