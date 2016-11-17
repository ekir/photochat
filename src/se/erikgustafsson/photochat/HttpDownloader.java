package se.erikgustafsson.photochat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

/*
 * This class is used to download data over http
 * When the data is downloaded its return to the callback interface of type HttpHandler
 */
public class HttpDownloader extends AsyncTask<String, Void, byte[]> {
	private HttpHandler receiver;
	private String url;
	private int delay=0;
	// Normal constructor
	// String turl - the url to download
	// HttpHandler treceiver - callback interface. Called when download is finished
	public HttpDownloader(String turl,HttpHandler treceiver) {
		receiver=treceiver;
		url=turl;
		this.execute("");
	}
	// Constructor with extra delay parameter
	// String turl - the url to download
	// HttpHandler treceiver - callback interface. Called when download finished
	// int delay - how long to wait before starting this download. Can be used to schedule a download
	public HttpDownloader(String turl,HttpHandler treceiver,int delay) {
		receiver=treceiver;
		url=turl;
		this.delay=delay;
		this.execute("");
	}
	
    // The background execution of this AsyncTask. This is runned in a background thread
    protected byte[] doInBackground(String... params) {
    	// If delay is not zero, then do the delay that is set
    	if(delay!=0) {
	    	try {
	    		Thread.sleep(delay);
	    	} catch(Exception e) {
	    		
	    	}
    	}
    	// Do the http download and return the result as the result for this AsyncTask
    	return geturl(url);
    }


    // When exection is finished
    protected void onPostExecute(byte[] result) {
    	// Use the callback interface(reciever) and send with the downloaded data
    	receiver.receive(result);
    }
    
    // Get a file from a URL over HTTP
    // String turl - the url to get
    public byte[] geturl(String turl) {
		try {
		    // Make a urlconnection and to turl
		    URL url = new URL(turl);
		    URLConnection conn = url.openConnection();
		    
		    // Get the response
		    //http://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-in-java
		    InputStream is = conn.getInputStream();
		    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		    
		    int nRead;
		    byte[] data = new byte[10000];
		    // Read the response 10000 bytes at the time from the input stream to a buffer array
		    while ((nRead = is.read(data, 0, data.length)) != -1) {
		      buffer.write(data, 0, nRead);
		    }

		    // Return the buffer array
		    return buffer.toByteArray();
		} catch (Exception e) {
			// If something goes wrong just send a an array with only an ending zero in it
			byte[] b = new byte[]{0};
			return b;
		}
    }
}
