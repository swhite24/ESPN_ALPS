package org.bball.scoreit;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Game {

	private static final String TAG = "BBALL_SCOREIT::GAME";
	private String id;
	private String time;
	private String location;
	
	/**
	 * Game constructor which extracts game information from json_string
	 * @param json_string - JSON formatted string containing game information
	 */
	public Game(String json_string){
		Log.d(TAG, "Game constructor");
		try {
			JSONObject game_obj = new JSONObject(json_string);
			this.id = game_obj.get("gameId").toString();
			this.time = game_obj.get("time").toString();
			this.location = game_obj.get("venue").toString();
		} catch (JSONException e) {
			Log.e(TAG, "Unable to initialize game data");
		}
	}

	public String getId() {
		return id;
	}

	public String getTime() {
		return time;
	}

	public String getLocation() {
		return location;
	}
	
}
