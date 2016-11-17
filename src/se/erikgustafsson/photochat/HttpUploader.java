package se.erikgustafsson.photochat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

/*
* This class is used to upload files over http
*/
public class HttpUploader extends AsyncTask<String, Void, byte[]> {
	private String target_url;
	private String file_path;
	private HttpHandler response;

	// Constructor
	// String target_url - the url to upload to
	// String file_path - the file to upload
	// HttpHandler response - The callback handler used when finished
	public HttpUploader(String target_url, String file_path,
			HttpHandler response) {
		// Set initial values
		this.target_url = target_url;
		this.file_path = file_path;
		this.response = response;
		this.execute("");
	}

	// This is what to do in background
	protected byte[] doInBackground(String... params) {
		// Post the file and return the result
		return postfile(target_url, file_path);
	}

	// Read a file, and return the content as a byte array
	public byte[] readfile(String path) {
		// Tutorials on how to read bytes:
		// http://stackoverflow.com/questions/858980/file-to-byte-in-java
		try {
			// Try to read all bytes in the file specifed in path
			RandomAccessFile f = new RandomAccessFile(path, "r");
			byte[] b = new byte[(int) f.length()];
			f.read(b);
			f.close();
			return b;
		} catch (Exception e) {
			// If failed, just return a byte array with a terminating zero
			return new byte[] { 0 };
		}
	}

	// Post a file to a specific url
	// String target_url - url to post to
	// String path - path to the file to post
	public byte[] postfile(String target_url, String path) {
		// This file upload method is made by the help of the tutorial here:
		// http://stackoverflow.com/questions/11766878/sending-files-using-post-with-httpurlconnection
			
		// Set the parameter name
		String attachmentName = "file";
		
		// Set the name of the file
		File f = new File(path);
		String attachmentFileName = f.getName();

		// String constants used in the http request
		final String crlf = "\r\n";
		final String twoHyphens = "--";
		final String boundary = "*****";

		try {
			URL url = new URL(target_url);
			HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
			// Disable caching
			httpUrlConnection.setUseCaches(false);
			
			// Configure the http request header for file upload
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");
			httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
			httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
			httpUrlConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			// Create a stream for sending data
			DataOutputStream request = new DataOutputStream(
					httpUrlConnection.getOutputStream());
			
			// Send header information for posting a file
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\""
					+ attachmentName + "\";filename=\"" + attachmentFileName
					+ "\"" + crlf);
			request.writeBytes(crlf);
			
			// Read the file to be sent
			byte[] filedata = readfile(path);
			
			// Send the file
			request.write(filedata);

			// Send end of file information for http
			request.writeBytes(crlf);
			request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

			request.flush();
			request.close();

			// Get the response
			// http://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-in-java
			InputStream is = httpUrlConnection.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			// Read 10000 byte at the time and store them in buffer
			int nRead;
			byte[] data = new byte[10000];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			httpUrlConnection.disconnect();
			// Return the buffer as a byte array
			return buffer.toByteArray();
		} catch (Exception e) {
			// If we fail, we just return an empty byte array
			byte[] b = new byte[] {};
			return b;
		}
	}

	// When posting file is finished we come to here
	protected void onPostExecute(byte[] result) {
		// Send the result of the http request to the HttpHandler callback interface response
		response.receive(result);
	}
}