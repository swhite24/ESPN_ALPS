package org.bball.scoreit;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ScoreGameActivity extends Activity {

	private static final String TAG = "BBALL_SCOREIT::SCOREGAMEACTIVITY";
	private static final int LOAD_GAMES_PROGRESS = 0;
	private static final int SUBMIT_GAME_DATA = 1;
	private static final int SELECT_AWAY_STARTERS = 2;
	private static final int SELECT_HOME_STARTERS = 3;
	private GenericReceiver generic_receiver;
	private ProgressDialog progress_dialog;
	private AlertDialog alert_dialog;
	private API_Calls api_calls;
	private Game game;
	private Team home_team, away_team;
	private TextView away1, away2, away3, away4, away5;
	private TextView home1, home2, home3, home4, home5;
	private TextView away_tv, home_tv;
	private CharSequence[] away_players, home_players;
	private String[] away_starters, home_starters;
	private boolean[] away_checked, home_checked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_game);

		// initialize all textviews in layout
		away1 = (TextView) findViewById(R.id.score_game_away_1);
		away2 = (TextView) findViewById(R.id.score_game_away_2);
		away3 = (TextView) findViewById(R.id.score_game_away_3);
		away4 = (TextView) findViewById(R.id.score_game_away_4);
		away5 = (TextView) findViewById(R.id.score_game_away_5);

		home1 = (TextView) findViewById(R.id.score_game_home_1);
		home2 = (TextView) findViewById(R.id.score_game_home_2);
		home3 = (TextView) findViewById(R.id.score_game_home_3);
		home4 = (TextView) findViewById(R.id.score_game_home_4);
		home5 = (TextView) findViewById(R.id.score_game_home_5);

		away_tv = (TextView) findViewById(R.id.score_game_away_team_tv);
		home_tv = (TextView) findViewById(R.id.score_game_home_team_tv);

		api_calls = new API_Calls(this);

		// create game from intent extra
		game = new Game(getIntent().getStringExtra(Constants.GAME_DATA));

		// show progress dialog that game data is being loaded
		showDialog(LOAD_GAMES_PROGRESS);
		// load game data
		api_calls.getGameData(game.getId());
	}

	@Override
	protected void onPause() {
		// unregister receiver when in background
		unregisterReceiver(generic_receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// create & register receiver when in foreground
		IntentFilter filter = new IntentFilter(
				(String) API_Calls.api_map.get(2));
		generic_receiver = new GenericReceiver();
		registerReceiver(generic_receiver, filter);
		super.onResume();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		// Progress dialog indicating that game data is loading
		case LOAD_GAMES_PROGRESS:
			progress_dialog = new ProgressDialog(this,
					ProgressDialog.STYLE_SPINNER);
			progress_dialog.setMessage("Loading game data...");
			progress_dialog.setCancelable(false);
			return progress_dialog;
		case SUBMIT_GAME_DATA:
			progress_dialog = new ProgressDialog(this,
					ProgressDialog.STYLE_SPINNER);
			progress_dialog.setMessage("Submitting starters...");
			progress_dialog.setCancelable(false);
			return progress_dialog;
			// Dialog allowing users to select starters for away team
		case SELECT_AWAY_STARTERS:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Starters for " + away_team.getName())
					.setMultiChoiceItems(away_players, away_checked,
							new selection_click_handler())
					.setPositiveButton("Finished", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								populate_away();
								break;
							}
						}
					}).create();
			return alert_dialog;
			// Dialog allowing user to select starters for home team
		case SELECT_HOME_STARTERS:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Starters for " + home_team.getName())
					.setMultiChoiceItems(home_players, home_checked,
							new selection_click_handler())
					.setPositiveButton("Finished", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								populate_home();
								break;
							}
						}
					}).create();
			return alert_dialog;
		default:
			return null;
		}
	}

	private class selection_click_handler implements
			DialogInterface.OnMultiChoiceClickListener {
		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			// nothing
		}
	}

	/**
	 * Extracts team info from gameData String and constructs home_team and
	 * away_team. Each team populates its respective list of players. Prompts
	 * user to select starters for away team, followed by home team.
	 * 
	 * @param gameData
	 *            - JSON formatted string containing game information.
	 */
	private void populateFields(String gameData) {
		away_starters = new String[5];
		home_starters = new String[5];
		try {
			JSONObject response_obj = new JSONObject(gameData);
			JSONObject game_obj = new JSONObject(response_obj.get("response")
					.toString());

			home_team = new Team(game_obj.get("homeTeam").toString());
			away_team = new Team(game_obj.get("awayTeam").toString());

			away_tv.setText("" + away_team.getName());

			home_tv.setText("" + home_team.getName());
			select_away_starters();
		} catch (Exception e) {
			Log.e(TAG, "Failed to populate game fields");
		}
	}

	/**
	 * Populates away player TextViews from the selected players to start for
	 * the away team.
	 */
	private void populate_away() {
		int count = 0;
		String[] starters = new String[5];
		for (int i = 0; i < away_players.length; i++) {
			if (count == 5)
				break;
			if (away_checked[i]) {
				String player_info = away_players[i].toString();
				starters[count] = player_info.substring(0, 5) + "\n"
						+ player_info.substring(player_info.indexOf(" - ") + 3);
				away_starters[count] = away_team.get_player_at(i + 1).getId();
				count++;
			}
		}
		away_players = null;
		away_checked = null;

		away1.setText(starters[0]);
		away2.setText(starters[1]);
		away3.setText(starters[2]);
		away4.setText(starters[3]);
		away5.setText(starters[4]);
		select_home_starters();
	}

	/**
	 * Populates home player TextViews from the selected players to start for
	 * the home team.
	 */
	private void populate_home() {
		int count = 0;
		String[] starters = new String[5];
		for (int i = 0; i < home_players.length; i++) {
			if (count == 5)
				break;
			if (home_checked[i]) {
				String player_info = home_players[i].toString();
				starters[count] = player_info.substring(0, 5) + "\n"
						+ player_info.substring(player_info.indexOf(" - ") + 3);
				home_starters[count] = home_team.get_player_at(i + 1).getId();
				count++;
			}
		}
		home_players = null;
		home_checked = null;

		home1.setText(starters[0]);
		home2.setText(starters[1]);
		home3.setText(starters[2]);
		home4.setText(starters[3]);
		home5.setText(starters[4]);

		showDialog(SUBMIT_GAME_DATA);
		api_calls.setGameData(away_starters, home_starters);
	}

	/**
	 * Initializes a selection array of CharSequence's and a selected array of
	 * booleans. Shows dialog to select starters for away team.
	 */
	private void select_away_starters() {
		ArrayList<Player> away_players_list = (ArrayList<Player>) away_team
				.getPlayers();
		away_players = new CharSequence[away_players_list.size() - 1];
		away_checked = new boolean[away_players.length];
		for (int i = 0; i < away_players_list.size() - 1; i++) {
			away_players[i] = away_players_list.get(i + 1).getLast_name()
					+ " - " + away_players_list.get(i + 1).getJersey_number();
		}
		showDialog(SELECT_AWAY_STARTERS);
	}

	/**
	 * Initializes a selection array of CharSequence's and a selected array of
	 * booleans. Shows dialog to select starters for home team.
	 */
	private void select_home_starters() {
		ArrayList<Player> home_players_list = (ArrayList<Player>) home_team
				.getPlayers();
		home_players = new CharSequence[home_players_list.size() - 1];
		home_checked = new boolean[home_players.length];
		for (int i = 0; i < home_players_list.size() - 1; i++) {
			home_players[i] = home_players_list.get(i + 1).getLast_name()
					+ " - " + home_players_list.get(i + 1).getJersey_number();
		}
		showDialog(SELECT_HOME_STARTERS);

	}

	/**
	 * BroadcastReceiver which receives response string HTTPRequest. Populates
	 * views of layout.
	 * 
	 * @author Steve
	 * 
	 */
	private class GenericReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int method_id = intent.getIntExtra(Constants.METHOD_ID, -1);
			switch (method_id){
			case 0:
				dismissDialog(LOAD_GAMES_PROGRESS);
				Log.d(TAG,
						"Received result length: "
								+ intent.getStringExtra("result").length());
				populateFields(intent.getStringExtra("result"));
				break;
			case 1:
				dismissDialog(SUBMIT_GAME_DATA);
				break;
			}
		}

	}
}
