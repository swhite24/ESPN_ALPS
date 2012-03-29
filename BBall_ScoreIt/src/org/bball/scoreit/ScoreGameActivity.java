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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoreGameActivity extends Activity {

	private static final String TAG = "BBALL_SCOREIT::SCOREGAMEACTIVITY";
	private static final int LOAD_GAMES_PROGRESS = 0;
	private static final int SUBMIT_GAME_DATA = 1;
	private static final int SELECT_AWAY_STARTERS = 2;
	private static final int SELECT_HOME_STARTERS = 3;
	private static final int AWAY_PLAYER_ACTION = 10;
	private static final int HOME_PLAYER_ACTION = 11;
	private static final int AWAY_PLAYER_SUBSTITUTION = 20;
	private static final int HOME_PLAYER_SUBSTITUTION = 21;
	private BallOverlay ball_overlay;
	private GenericReceiver generic_receiver;
	private AwayPlayerListener away_player_click;
	private HomePlayerListener home_player_click;
	private ProgressDialog progress_dialog;
	private AlertDialog alert_dialog;
	private API_Calls api_calls;
	private Game game;
	private Team home_team, away_team, current_team;
	private TextView away1, away2, away3, away4, away5;
	private TextView home1, home2, home3, home4, home5;
	private TextView away_tv, home_tv, current;
	private ImageView court;
	private CharSequence[] players;
	private String[] away_starters, home_starters, player_actions;
	private String current_player;
	private boolean[] checked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_game);
		Log.d(TAG, "ScoreGameActivity OnCreate");

		// setup click listener
		away_player_click = new AwayPlayerListener();
		home_player_click = new HomePlayerListener();

		ball_overlay = (BallOverlay) findViewById(R.id.score_game_ball_overlay);
		court = (ImageView) findViewById(R.id.score_game_court);
		court.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				ball_overlay.setX(event.getX());
				ball_overlay.setY(event.getY());
				ball_overlay.invalidate();
				return true;
			}
		});

		// list of limited actions for player
		player_actions = new String[6];
		player_actions[0] = "Rebound";
		player_actions[1] = "Made Shot";
		player_actions[2] = "Missed Shot";
		player_actions[3] = "Turnover";
		player_actions[4] = "Foul";
		player_actions[5] = "Substitution";

		// initialize all textviews in layout
		away1 = (TextView) findViewById(R.id.score_game_away_1);
		away2 = (TextView) findViewById(R.id.score_game_away_2);
		away3 = (TextView) findViewById(R.id.score_game_away_3);
		away4 = (TextView) findViewById(R.id.score_game_away_4);
		away5 = (TextView) findViewById(R.id.score_game_away_5);

		away1.setOnClickListener(away_player_click);
		away2.setOnClickListener(away_player_click);
		away3.setOnClickListener(away_player_click);
		away4.setOnClickListener(away_player_click);
		away5.setOnClickListener(away_player_click);

		home1 = (TextView) findViewById(R.id.score_game_home_1);
		home2 = (TextView) findViewById(R.id.score_game_home_2);
		home3 = (TextView) findViewById(R.id.score_game_home_3);
		home4 = (TextView) findViewById(R.id.score_game_home_4);
		home5 = (TextView) findViewById(R.id.score_game_home_5);

		home1.setOnClickListener(home_player_click);
		home2.setOnClickListener(home_player_click);
		home3.setOnClickListener(home_player_click);
		home4.setOnClickListener(home_player_click);
		home5.setOnClickListener(home_player_click);

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
					.setTitle("5 more starters for " + away_team.getName())
					.setMultiChoiceItems(players, checked,
							new Starter_Select(away_team.getName()))
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
					.setTitle("5 more starters for " + home_team.getName())
					.setMultiChoiceItems(players, checked,
							new Starter_Select(home_team.getName()))
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
			// Dialog for player action
		case AWAY_PLAYER_ACTION:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Action for " + current_player)
					.setItems(player_actions, new AwayPlayerAction()).create();
			return alert_dialog;
		case HOME_PLAYER_ACTION:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Action for " + current_player)
					.setItems(player_actions, new HomePlayerAction()).create();
			return alert_dialog;
		case AWAY_PLAYER_SUBSTITUTION:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Substitution for " + current_player)
					.setItems(players, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String trunc_name = player_info.substring(0, 5);
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							current_team.get_player_with_jersey(
									Integer.parseInt(jersey_num)).setOn_court(
									true);
							String new_player = trunc_name + "\n" + jersey_num;
							current.setText(new_player);
						}
					}).create();
			return alert_dialog;
		case HOME_PLAYER_SUBSTITUTION:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Substitution for " + current_player)
					.setItems(players, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String trunc_name = player_info.substring(0, 5);
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							current_team.get_player_with_jersey(
									Integer.parseInt(jersey_num)).setOn_court(
									true);
							String new_player = trunc_name + "\n" + jersey_num;
							current.setText(new_player);
						}
					}).create();
			return alert_dialog;
		default:
			return null;
		}
	}

	/**
	 * Dialog ClickListener for updating title when selecting team starters.
	 * 
	 * @author Steve
	 * 
	 */
	private class Starter_Select implements
			DialogInterface.OnMultiChoiceClickListener {
		private String team_name;

		public Starter_Select(String team_name) {
			this.team_name = team_name;
		}

		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			alert_dialog.setTitle(get_selected() + " more starters for "
					+ team_name);
		}

		/**
		 * Number of remaining starts to select.
		 * 
		 * @return - number of remaining starters to select. (5 - current
		 *         number)
		 */
		private int get_selected() {
			int count = 0;
			for (int i = 0; i < checked.length; i++) {
				if (checked[i])
					count++;
			}
			return 5 - count;
		}
	}

	/**
	 * Dialog ClickListener for away player action.
	 * 
	 * @author Steve
	 * 
	 */
	private class AwayPlayerAction implements OnClickListener {
		public void onClick(DialogInterface dialog, int which) {

		}
	}

	/**
	 * Dialog ClickListener for away player action.
	 * 
	 * @author Steve
	 * 
	 */
	private class HomePlayerAction implements OnClickListener {

		public void onClick(DialogInterface dialog, int which) {

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
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * Populates away player TextViews from the selected players to start for
	 * the away team.
	 */
	private void populate_away() {
		int count = 0;
		String[] starters = new String[5];
		for (int i = 0; i < players.length; i++) {
			if (count == 5)
				break;
			if (checked[i]) {
				String player_info = players[i].toString();
				String trunc_name = player_info.substring(0, 5);
				String jersey_num = player_info.substring(player_info
						.indexOf(" - ") + 3);
				away_team.get_player_with_jersey(Integer.parseInt(jersey_num))
						.setOn_court(true);
				starters[count] = trunc_name + "\n" + jersey_num;
				away_starters[count] = away_team.get_player_at(i + 1).getId();
				count++;
			}
		}
		players = null;
		checked = null;

		away1.setText(starters[0] == null ? "!" : starters[0]);
		away2.setText(starters[1] == null ? "!" : starters[1]);
		away3.setText(starters[2] == null ? "!" : starters[2]);
		away4.setText(starters[3] == null ? "!" : starters[3]);
		away5.setText(starters[4] == null ? "!" : starters[4]);
		select_home_starters();
	}

	/**
	 * Populates home player TextViews from the selected players to start for
	 * the home team.
	 */
	private void populate_home() {
		int count = 0;
		String[] starters = new String[5];
		for (int i = 0; i < players.length; i++) {
			if (count == 5)
				break;
			if (checked[i]) {
				String player_info = players[i].toString();
				String trunc_name = player_info.substring(0, 5);
				String jersey_num = player_info.substring(player_info
						.indexOf(" - ") + 3);
				home_team.get_player_with_jersey(Integer.parseInt(jersey_num))
						.setOn_court(true);
				starters[count] = trunc_name + "\n" + jersey_num;
				home_starters[count] = home_team.get_player_at(i + 1).getId();
				count++;
			}
		}
		players = null;
		checked = null;

		home1.setText(starters[0] == null ? "!" : starters[0]);
		home2.setText(starters[1] == null ? "!" : starters[1]);
		home3.setText(starters[2] == null ? "!" : starters[2]);
		home4.setText(starters[3] == null ? "!" : starters[3]);
		home5.setText(starters[4] == null ? "!" : starters[4]);

		showDialog(SUBMIT_GAME_DATA);
		api_calls.setGameData(away_starters, home_starters);
	}

	/**
	 * Initializes a selection array of CharSequence's and a selected array of
	 * booleans. Shows dialog to select starters for away team.
	 */
	private void select_away_starters() {
		populate_players(away_team);
		showDialog(SELECT_AWAY_STARTERS);
	}

	/**
	 * Initializes a selection array of CharSequence's and a selected array of
	 * booleans. Shows dialog to select starters for home team.
	 */
	private void select_home_starters() {
		populate_players(home_team);
		showDialog(SELECT_HOME_STARTERS);

	}

	private void populate_players(Team team) {
		ArrayList<Player> players_list = (ArrayList<Player>) team.getPlayers();
		players = new CharSequence[players_list.size() - 1];
		checked = new boolean[players.length];
		for (int i = 0; i < players_list.size() - 1; i++) {
			players[i] = players_list.get(i + 1).getLast_name() + " - "
					+ players_list.get(i + 1).getJersey_number();
		}
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
			Log.d(TAG, "GenericReceiver onReceive");
			int method_id = intent.getIntExtra(Constants.METHOD_ID, -1);
			switch (method_id) {
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

	/**
	 * OnClickListener for five currently on-court players for the away team.
	 * Prompts user to either select an action for player or substitute the
	 * player.
	 * 
	 * @author Steve
	 * 
	 */
	private class AwayPlayerListener implements
			android.view.View.OnClickListener {
		public void onClick(View v) {
			TextView temp = (TextView) v;
			if (!temp.getText().equals("!")) {
				String text = temp.getText().toString();
				String jersey = text.substring(text.indexOf("\n") + 1);
				current_player = away_team.get_player_with_jersey(
						Integer.parseInt(jersey)).getLast_name();
				current_team = away_team;
				current = temp;
				Log.d(TAG, "jersey/currentplayer: " + jersey + "/"
						+ current_player);
				if (alert_dialog != null)
					alert_dialog.setTitle("Action for " + current_player);
				showDialog(AWAY_PLAYER_ACTION);
			} else {
				current_player = "!";
				current_team = away_team;
				current = temp;
				populate_players(away_team);
				alert_dialog = null;
				showDialog(AWAY_PLAYER_SUBSTITUTION);
			}
		}
	}

	/**
	 * OnClickListener for five currently on-court players for the home team.
	 * Prompts user to either select an action for player or substitute the
	 * player.
	 * 
	 * @author Steve
	 * 
	 */
	private class HomePlayerListener implements
			android.view.View.OnClickListener {

		public void onClick(View v) {
			TextView temp = (TextView) v;
			if (!temp.getText().equals("!")) {
				String text = temp.getText().toString();
				String jersey = text.substring(text.indexOf("\n") + 1);
				current_player = home_team.get_player_with_jersey(
						Integer.parseInt(jersey)).getLast_name();
				current_team = home_team;
				current = temp;
				Log.d(TAG, "jersey/currentplayer: " + jersey + "/"
						+ current_player);
				if (alert_dialog != null)
					alert_dialog.setTitle("Action for " + current_player);
				showDialog(HOME_PLAYER_ACTION);
			} else {
				current_player = "!";
				current_team = home_team;
				current = temp;
				populate_players(home_team);
				alert_dialog = null;
				showDialog(HOME_PLAYER_SUBSTITUTION);
			}
		}
	}
}
