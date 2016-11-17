package se.erikgustafsson.photochat;

/*
 * This class describes a message
 * It does not have any methods and its meant to be used like a C struct by other classes
 */
public class Message {
	private int id;  // The id of the message. The same as in the database
	private String sender=""; // The username of the sender
	private String receiver=""; // The username of the receiver
	private String type=""; // Which type of message. It could be "text" or "image". More alternatives are plannaed in the fututre
	private String content=""; // The content of the message. For text this is the text, for image this is the media id of the image
	private boolean pending=false; // If the message is already sent or not. If its not sent its waiting to be sent
	
	// Getter for id attribute
	public int getId() {
		return id;
	}

	// Setter for id attribute
	public void setId(int id) {
		this.id = id;
	}

	// Getter for sender attribute
	public String getSender() {
		return sender;
	}

	// Setter for sender attribute
	public void setSender(String sender) {
		this.sender = sender;
	}

	// Getter for receiver attribute
	public String getReceiver() {
		return receiver;
	}

	// Setter for receiver attribute
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	// Getter for type attribute
	public String getType() {
		return type;
	}

	// Setter for type attribute
	public void setType(String type) {
		this.type = type;
	}

	// Getter for content attribute
	public String getContent() {
		return content;
	}

	// Setter for content attribute
	public void setContent(String content) {
		this.content = content;
	}

	// Getter for pending attribute
	public boolean getPending() {
		return pending;
	}

	// Setter for pending attribute
	public void setPending(boolean pending) {
		this.pending = pending;
	}
	
	// Empty constructor as we want to be able to start a message without parameters
	public Message() {
	}
	
	// Constructur that with parameters for all the initial values
	public Message(int id,String sender,String receiver,String type,String content,boolean pending) {
		this.id=id;
		this.sender=sender;
		this.receiver=receiver;
		this.type=type;
		this.content=content;
		this.pending=pending;
	}
}
