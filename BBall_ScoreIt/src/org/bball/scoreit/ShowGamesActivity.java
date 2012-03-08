package org.bball.scoreit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class ShowGamesActivity extends ListActivity {

	private List<String> games;
	private String token;
	private GamesReceiver games_receiver;
	private static final String TAG = "BBALL_SCOREIT::SHOWGAMESACTIVITY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, getClass() + " oncreate");
		setContentView(R.layout.list_games);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			token = extras.getString("token");
		}
		games = new ArrayList<String>();
		getGames();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(games_receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter(
				(String) API_Calls.api_map.get(1));
		games_receiver = new GamesReceiver();
		registerReceiver(games_receiver, filter);
		super.onResume();
	}

	/**
	 * Calls Games API method over time interval: last week to next month
	 * Populates listview with game data of all returned games
	 */
	private void getGames() {
		Log.d(TAG, "Getting games");
		API_Calls api_calls = new API_Calls(this);
		
		Calendar initial = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		initial.set(Calendar.DAY_OF_YEAR, initial.get(Calendar.DAY_OF_YEAR) - 7);
		end.set(Calendar.DAY_OF_YEAR, end.get(Calendar.DAY_OF_YEAR) + 30);

		api_calls.get_games(token, Constants.df.format(initial.getTime()),
				Constants.df.format(end.getTime()));
	}
	
	/**
	 * Fills ListView with game data 
	 */
	private void setAdapter(){
		GameAdapter game_adapter = new GameAdapter(this,
				R.layout.game_item, games);
		setListAdapter(game_adapter);
	}

	/**
	 * BroadcastReceiver to receive list of games from Games API method.
	 * Calls setAdapter() on success.
	 * @author Steve
	 *
	 */
	private class GamesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				JSONObject result_obj = new JSONObject(
						intent.getStringExtra("result"));
				JSONObject response_obj = new JSONObject(result_obj.get(
						"response").toString());
				JSONArray game_objs = new JSONArray(response_obj.get("games")
						.toString());
				Log.d(TAG, "Got array of " + game_objs.length());
				// Add string for each JSONObject
				for (int i = 0; i < game_objs.length(); i++) {
					JSONObject temp = game_objs.getJSONObject(i);
					games.add(temp.toString());
				}
				setAdapter();
			} catch (Exception e) {
				Log.d(TAG, "Couldn't extract response.");
			}
		}

	}
}
