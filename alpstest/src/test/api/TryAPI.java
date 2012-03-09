package test.api;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		String games = null;
		String game_id = null;
		try {
			JSONObject obj = new JSONObject(games_data);
			games = obj.get("games").toString();
			JSONArray arr = new JSONArray(games);
			for (int i = 0; i < arr.length(); i++){
				JSONObject temp = arr.getJSONObject(i);
				game_id = temp.get("gameId").toString();
				System.out.println(temp.toString());
			}
			System.out.println("arr length: " + arr.length());
		} catch (JSONException e) {
			System.out.println("error");
		}
		
		String game_data = request.get_game_data(token, game_id);
		System.out.println(game_data);
	}
}
