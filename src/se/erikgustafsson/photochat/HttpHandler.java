package se.erikgustafsson.photochat;

/*
 * This is an interface used to receive data downloaded over http
 * Used by both HttpDownloader and HttpUploader
 */
public interface HttpHandler {
	// A callback method that says http request is finished
	// byte[] raw_answer - a byte array of what was returned from the http request
	public void receive(byte[] raw_answer);
}
