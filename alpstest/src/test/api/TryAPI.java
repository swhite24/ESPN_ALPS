package test.api;


public class TryAPI {

	public static void main(String args[]) {
		HTTPRequest login = new HTTPRequest();
		
		login.login("testuser", "secretPassword");
	}
}
