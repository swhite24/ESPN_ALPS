package org.bball.scoreit;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ScoreGameActivity extends Activity {

	private static final String TAG = "BBALL_SCOREIT::SCOREGAMEACTIVITY";
	private static final int LOAD_GAMES = 0;
	private GenericReceiver generic_receiver;
	private ProgressDialog load_games_dialog;
	private API_Calls api_calls;
	private Game game;
	private Team home_team, away_team;
	private TextView away1, away2, away3, away4, away5;
	private TextView home1, home2, home3, home4, home5;
	private TextView team_text;

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

		team_text = (TextView) findViewById(R.id.score_game_team_txt);

		api_calls = new API_Calls(this);

		// create game from intent extra
		game = new Game(getIntent().getStringExtra(Constants.GAME_DATA));

		// show progress dialog that game data is being loaded
		showDialog(LOAD_GAMES);
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
		case LOAD_GAMES:
			load_games_dialog = new ProgressDialog(this,
					ProgressDialog.STYLE_SPINNER);
			load_games_dialog.setMessage("Loading game data...");
			load_games_dialog.setCancelable(false);
			return load_games_dialog;
		default:
			return null;
		}
	}
	/**
	 * Extracts team info from gameData String and constructs
	 * home_team and away_team. Each team populates its respective
	 * list of players.  Once teams are constructed fills TextViews of
	 * layout with corresponding values. 
	 * @param gameData - JSON formatted string containing game information.
	 */
	private void populateFields(String gameData) {
		try {
			JSONObject response_obj = new JSONObject(gameData);
			JSONObject game_obj = new JSONObject(response_obj.get("response")
					.toString());

			home_team = new Team(game_obj.get("homeTeam").toString());
			away_team = new Team(game_obj.get("awayTeam").toString());
			
			team_text.setText(away_team.getName() + " at "
					+ home_team.getName());

			away1.setText("" + away_team.get_player_at(1).getJersey_number());
			away2.setText("" + away_team.get_player_at(2).getJersey_number());
			away3.setText("" + away_team.get_player_at(3).getJersey_number());
			away4.setText("" + away_team.get_player_at(4).getJersey_number());
			away5.setText("" + away_team.get_player_at(5).getJersey_number());

			home1.setText("" + home_team.get_player_at(1).getJersey_number());
			home2.setText("" + home_team.get_player_at(2).getJersey_number());
			home3.setText("" + home_team.get_player_at(3).getJersey_number());
			home4.setText("" + home_team.get_player_at(4).getJersey_number());
			home5.setText("" + home_team.get_player_at(5).getJersey_number());

		} catch (Exception e) {
			Log.e(TAG, "Failed to populate game fields");
		}
	}

	/**
	 * BroadcastReceiver which receives response string HTTPRequest.
	 * Populates views of layout.
	 * @author Steve
	 *
	 */
	private class GenericReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			dismissDialog(LOAD_GAMES);
			Log.d(TAG,
					"Received result length: "
							+ intent.getStringExtra("result").length());
			populateFields(intent.getStringExtra("result"));
		}

	}
}
