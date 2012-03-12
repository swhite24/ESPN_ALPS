package org.bball.scoreit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ShowGamesActivity extends ListActivity {

	private static final String TAG = "BBALL_SCOREIT::SHOWGAMESACTIVITY";
	private static final int GOT_GAMES = 0;
	private List<String> games;
	private GamesReceiver games_receiver;
	private ProgressDialog loading_games;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, getClass() + " oncreate");
		setContentView(R.layout.list_games);

		games = new ArrayList<String>();
		loading_games = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		if (loading_games.isShowing())
			dismissDialog(GOT_GAMES);
		showDialog(GOT_GAMES);
		
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String game_data = games.get(position);
		try {
			Intent score_game = new Intent(this, ScoreGameActivity.class);
			// attach JSON string to intent
			score_game.putExtra(Constants.GAME_DATA, game_data);
			startActivity(score_game);
		} catch (Exception e) {
			Log.e(TAG, "Couldn't create game_obj");
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case GOT_GAMES:
			loading_games.setMessage("Loading Games...");
			loading_games.setCancelable(false);
			return loading_games;
		default:
			return null;
		}
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

		api_calls.get_games(Constants.df.format(initial.getTime()),
				Constants.df.format(end.getTime()));
	}

	/**
	 * Fills ListView with game data
	 */
	private void setAdapter() {
		GameAdapter game_adapter = new GameAdapter(this, R.layout.game_item,
				games);
		setListAdapter(game_adapter);
	}

	/**
	 * BroadcastReceiver to receive list of games from Games API method. Calls
	 * setAdapter() on success.
	 * 
	 * @author Steve
	 * 
	 */
	private class GamesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			dismissDialog(GOT_GAMES);
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
