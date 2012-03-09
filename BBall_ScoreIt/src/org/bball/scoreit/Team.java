package org.bball.scoreit;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Team {

	private static final String TAG = "BBALL_SCOREIT::TEAM";
	private String id;
	private String name;
	List<Player> players;
	
	/**
	 * Team constructor which extracts JSONObject from json_string and
	 * pulls id, name, and list of players.
	 * @param json_string - JSON formatted string containing team data.
	 */
	public Team(String json_string){
		players = new ArrayList<Player>();
		try{
			JSONObject team_obj = new JSONObject(json_string);
			id = team_obj.get("teamId").toString();
			name = team_obj.get("teamName").toString();
			JSONArray player_arr = new JSONArray(team_obj.get("players").toString());
			for (int i = 0; i < player_arr.length(); i++){
				players.add(new Player(player_arr.getJSONObject(i).toString()));
			}
		} catch (Exception e){
			Log.e(TAG, "Failed to construct team");
		}
	}
	
	/**
	 * 
	 * @param loc - location of player in players list
	 * @return Player at location loc
	 */
	public Player get_player_at(int loc){
		return players.get(loc);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	
}
