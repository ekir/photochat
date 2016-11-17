package se.erikgustafsson.photochat;

/*
 * Backend wrapper. This makes calls to the backend-server easier
 */
public class BackendWrapper {
	private final String baseurl="http://cchat.eglab.cc";
	
	// All calls to the backend are on form action, parameter list
	// action is what action to do, for example "login"
	// depending on the action there are different number of parmeters
	// there can be parm1, parm2 and parm3 or no parameters
	//
	// To make the backend easier to use there exist 4 versions of call
	// They have 0 to 3 parameters
	
	// Call with no inputs specified. parm1,parm2 and parm3 are all ""
	public void call(String action,HttpHandler treceiver) {
		call(action,"","","",treceiver);
	}
	
	// Call with one inputs specified: parm1
	public void call(String action,String parm1,HttpHandler treceiver) {
		call(action,parm1,"","",treceiver);
	}
	
	// Call with two inputs specified: parm1 and parm2
	public void call(String action,String parm1,String parm2,HttpHandler treceiver) {
		call(action,parm1,parm2,"",treceiver);
	}
	
	// Call with three inputs specified: parm1,parm2 and parm3
	public void call(String action,String parm1,String parm2,String parm3,HttpHandler treceiver) {
		new HttpDownloader(baseurl+"/index.php?action="+action+"&parm1="+parm1+"&parm2="+parm2+"&parm3="+parm3,treceiver);
	}
	
	// This is a special case which is a delayed version of call with 3 params. The delay means wait before call
	public void call_delay(String action,String parm1,String parm2,HttpHandler treceiver,int delay) {
		new HttpDownloader(baseurl+"/index.php?action="+action+"&parm1="+parm1+"&parm2="+parm2,treceiver,delay);
	}
}
