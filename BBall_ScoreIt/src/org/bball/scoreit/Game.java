package org.bball.scoreit;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Game {

	private static final String TAG = "BBALL_SCOREIT::GAME";
	private String id;
	private Team home_team, away_team;
	private String time;
	private String location;
	
	public Game(String json_string){
		try {
			JSONObject game_obj = new JSONObject(json_string);
			this.id = game_obj.get("gameId").toString();
			this.home_team = new Team(game_obj.get("homeTeam").toString());
			this.away_team = new Team(game_obj.get("awayTeam").toString());
			this.time = game_obj.get("time").toString();
			this.location = game_obj.get("venue").toString();
		} catch (JSONException e) {
			Log.e(TAG, "Unable to initialize game data");
		}
	}
}
