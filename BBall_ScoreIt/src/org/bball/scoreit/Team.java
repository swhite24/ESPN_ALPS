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
	private int score = 0, TOs;
	private List<Player> players;
	
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
	public Player get_player(int loc){
		return players.get(loc);
	}
	
	public Player get_player_with_jersey(int jersey){
		for (int i = 0; i < players.size(); i++){
			if (get_player(i).getJersey_number() == jersey)
				return get_player(i);
		}
		return null;
	}
	
	public Player get_player_with_name(String name){
		for (int i = 0; i < players.size(); i++){
			if (get_player(i).getLast_name().equals(name))
				return get_player(i);
		}
		return null;
	}
	
	public Player get_player_with_id(String id){
		for (int i = 0; i < players.size(); i++){
			if (get_player(i).getId().equals(id))
				return get_player(i);
		}
		return null;
	}
	
	public Player get_team_player(){
		for (int i = 0; i < players.size(); i++){
			if (get_player(i).isIs_team_player())
				return get_player(i);
		}
		return null;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	
	public void incrementScore(int amount){
		score += amount;
	}
	
	public void useTO(){
		TOs--;
	}
}
