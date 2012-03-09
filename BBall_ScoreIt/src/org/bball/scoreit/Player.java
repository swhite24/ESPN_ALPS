package org.bball.scoreit;

import org.json.JSONObject;

import android.util.Log;

public class Player {

	private static final String TAG = "BBALL_SCOREIT::PLAYER";
	private String first_name, middle_name, last_name;
	private String id;
	private int jersey_number;
	private boolean is_team_player;
	
	/**
	 * Player constructor which extracts player information from json_string.
	 * @param json_string - JSON formatted string containing player information
	 */
	public Player(String json_string){
		try{
			JSONObject player_obj = new JSONObject(json_string);
			JSONObject player_name = new JSONObject(player_obj.get("name").toString());
			this.jersey_number = player_obj.getInt("jerseyNumber");
			this.is_team_player = player_obj.getBoolean("isTeamPlayer");
			this.id = player_obj.get("playerId").toString();
			this.first_name = player_name.get("firstName").toString();
			this.middle_name = player_name.get("middleName").toString();
			this.last_name = player_name.get("lastName").toString();
		} catch (Exception e){
			Log.e(TAG, "Couldn't construct player");
		}
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getMiddle_name() {
		return middle_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public String getId() {
		return id;
	}

	public int getJersey_number() {
		return jersey_number;
	}

	public boolean isIs_team_player() {
		return is_team_player;
	}
	
	
}
