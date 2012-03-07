package test.api;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Testing various api methods
 * @author Steve
 *
 */

public class TryAPI {

	public static void main(String args[]) {
		HTTPRequest request = new HTTPRequest();
		
		String token = request.login(Constants.USERNAME, Constants.PASSWORD);
		
		System.out.println(token);
		
		
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
		Date now = new Date();
		Date tomorrow = new Date();
		tomorrow.setDate(tomorrow.getDate() + 30);
		
		String now_string = null, end_string = null;
		try {
			now_string = df.format(now).toString();
			end_string = df.format(tomorrow).toString();			
		} catch (Exception e) {
			System.out.println("Failed to create date strings.");
		}
		
		String games_data = request.get_games(token, now_string, end_string);
		System.out.println(games_data);
	}
}
