package org.bball.scoreit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class ScoreGameActivity extends Activity {

	private static final String TAG = "BBALL_SCOREIT::SCOREGAMEACTIVITY";
	private static final int LOAD_GAMES_PROGRESS = 0;
	private static final int SUBMIT_GAME_DATA = 1;
	private static final int SELECT_AWAY_STARTERS = 2;
	private static final int SELECT_HOME_STARTERS = 3;
	private static final int SUBMIT_ACTION = 4;
	private static final int SUBSTITUTION = 7;
	private static final int PLAYER_ACTION_DIALOG = 8;
	private static final int TEAM_ACTION_SELECT = 40;
	private static final int OFFICIAL_ACTION_SELECT = 50;
	private BallOverlay ball_overlay;
	private GenericReceiver generic_receiver;
	private AwayPlayerListener away_player_click;
	private HomePlayerListener home_player_click;
	private TeamActionListener team_action_click;
	private ProgressDialog progress_dialog;
	private Dialog action_dialog;
	private API_Calls api_calls;
	private Game game;
	public static Team home_team, away_team;
	private Team current_team;
	private TextView away1, away2, away3, away4, away5;
	private TextView home1, home2, home3, home4, home5;
	private TextView away_tv, home_tv, current;
	private TextView home_score, away_score, period_indicator;
	private ScrollView right;
	private Button away_click, home_click;
	private ImageView court;
	private ArrayList<String> current_players;
	private String[] away_starters, home_starters, player_actions;
	private String[] sub_action;
	private String current_player;
	private int period;
	private boolean fast_break, goaltending;
	private boolean[] checked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_game);
		Log.d(TAG, "ScoreGameActivity OnCreate");
		// setup click listener
		away_player_click = new AwayPlayerListener();
		home_player_click = new HomePlayerListener();
		team_action_click = new TeamActionListener();

		away_click = (Button) findViewById(R.id.score_game_away_action);
		home_click = (Button) findViewById(R.id.score_game_home_action);

		away_click.setOnClickListener(team_action_click);
		home_click.setOnClickListener(team_action_click);

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

		sub_action = new String[1];
		sub_action[0] = "Substitution";

		home_score = (TextView) findViewById(R.id.score_game_home_score);
		away_score = (TextView) findViewById(R.id.score_game_away_score);

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

		period_indicator = (TextView) findViewById(R.id.score_game_period_indicator);
		period_indicator.setText("Pregame");
		period = 1;

		api_calls = new API_Calls(this);

		// create game from intent extra
		game = new Game(getIntent().getStringExtra(Constants.GAME_DATA));

		// show progress dialog that game data is being loaded
		showDialog(LOAD_GAMES_PROGRESS);
		// load game data
		api_calls.getGameData(game.getId());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.score_game_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		remove_dialogs();
		switch (item.getItemId()) {
		case R.id.menu_official_action:
			showDialog(OFFICIAL_ACTION_SELECT);
			break;
		case R.id.menu_show_stats:
			startActivity(new Intent(this, ShowStatsActivity.class));
			break;
		}
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (ball_overlay.get_width() == -1) {
			ball_overlay.setWidth(court.getWidth());
			ball_overlay.setHeight(court.getHeight());
			ball_overlay.setX(court.getWidth() / 2.0f);
			ball_overlay.setY(court.getHeight() / 2.0f);
			ball_overlay.invalidate();
		}
		super.onWindowFocusChanged(hasFocus);
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
		case SUBMIT_ACTION:
			progress_dialog = new ProgressDialog(this,
					ProgressDialog.STYLE_SPINNER);
			progress_dialog.setMessage("Submitting action...");
			progress_dialog.setCancelable(false);
			return progress_dialog;
		case OFFICIAL_ACTION_SELECT:
			action_dialog = new Dialog(this);
			action_dialog.setContentView(R.layout.official_action);
			final ListView official_actions = (ListView) action_dialog
					.findViewById(R.id.official_actions);
			ActionAdapter off_ad = new ActionAdapter(this,
					R.layout.action_item,
					Arrays.asList(Constants.OFFICIAL_ACTIONS));
			right = (ScrollView) action_dialog
					.findViewById(R.id.official_right);
			official_actions.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					right.removeAllViews();
					for (View v : official_actions.getTouchables()) {
						v.setBackgroundColor(Color.BLACK);
					}
					((LinearLayout) arg1).setBackgroundColor(Color.RED);
					switch (arg2) {
					case 0:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.jump_ball, right);
						final Spinner away_j = (Spinner) right
								.findViewById(R.id.jump_away_jumper);
						populate_on_court(away_team);
						add_team_player(away_team);
						ArrayAdapter<String> jump_ad = new ArrayAdapter<String>(
								ScoreGameActivity.this,
								android.R.layout.simple_spinner_item,
								current_players);
						jump_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						away_j.setAdapter(jump_ad);
						final Spinner home_j = (Spinner) right
								.findViewById(R.id.jump_home_jumper);
						populate_on_court(home_team);
						add_team_player(home_team);
						jump_ad = new ArrayAdapter<String>(
								ScoreGameActivity.this,
								android.R.layout.simple_spinner_item,
								current_players);
						jump_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						home_j.setAdapter(jump_ad);

						final Spinner winner_j = (Spinner) right
								.findViewById(R.id.jump_winner);
						away_j.setOnItemSelectedListener(new OnItemSelectedListener() {
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								ArrayList<String> winner_list = new ArrayList<String>();
								winner_list.add(away_j.getSelectedItem()
										.toString());
								winner_list.add(home_j.getSelectedItem()
										.toString());
								ArrayAdapter<String> winner_ad = new ArrayAdapter<String>(
										ScoreGameActivity.this,
										android.R.layout.simple_spinner_item,
										winner_list);
								winner_ad
										.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								winner_j.setAdapter(winner_ad);
							}

							public void onNothingSelected(AdapterView<?> arg0) {
							}
						});
						home_j.setOnItemSelectedListener(new OnItemSelectedListener() {
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								ArrayList<String> winner_list = new ArrayList<String>();
								winner_list.add(away_j.getSelectedItem()
										.toString());
								winner_list.add(home_j.getSelectedItem()
										.toString());
								ArrayAdapter<String> winner_ad = new ArrayAdapter<String>(
										ScoreGameActivity.this,
										android.R.layout.simple_spinner_item,
										winner_list);
								winner_ad
										.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								winner_j.setAdapter(winner_ad);
							}

							public void onNothingSelected(AdapterView<?> arg0) {
							}
						});
						Button jump_submit_btn = (Button) right
								.findViewById(R.id.jump_submit_btn);
						jump_submit_btn
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										String winner = winner_j
												.getSelectedItem().toString();
										String winner_id = null;
										String away_id = null;
										String home_id = null;
										if (winner.equals(away_team.getName())) {
											winner_id = away_team
													.get_team_player().getId();
											away_id = winner_id;
										} else if (winner.equals(home_team
												.getName())) {
											winner_id = home_team
													.get_team_player().getId();
											home_id = winner_id;
										} else {
											String winner_jn = winner.substring(winner
													.indexOf(" - ") + 3);
											if (winner_j
													.getSelectedItemPosition() == 0) {
												winner_id = away_team
														.get_player_with_jersey(
																Integer.parseInt(winner_jn))
														.getId();
												away_id = winner_id;
											} else {
												winner_id = home_team
														.get_player_with_jersey(
																Integer.parseInt(winner_jn))
														.getId();
												home_id = winner_id;
											}
										}
										if (away_id == null) {
											String away_jumper = winner_j
													.getItemAtPosition(0)
													.toString();
											if (away_jumper.equals(away_team
													.getName())) {
												away_id = away_team
														.get_team_player()
														.getId();
											} else {
												String away_jn = away_jumper.substring(away_jumper
														.indexOf(" - ") + 3);
												away_id = away_team
														.get_player_with_jersey(
																Integer.parseInt(away_jn))
														.getId();
											}
										}
										if (home_id == null) {
											String home_jumper = winner_j
													.getItemAtPosition(0)
													.toString();
											if (home_jumper.equals(away_team
													.getName())) {
												home_id = home_team
														.get_team_player()
														.getId();
											} else {
												String home_jn = home_jumper.substring(home_jumper
														.indexOf(" - ") + 3);
												home_id = home_team
														.get_player_with_jersey(
																Integer.parseInt(home_jn))
														.getId();
											}
										}
										showDialog(SUBMIT_ACTION);
										api_calls.send_jumpball(home_id,
												away_id, winner_id,
												ball_overlay
														.get_court_location(),
												api_calls.make_context(
														home_team.getScore(),
														away_team.getScore()));
										dismissDialog(OFFICIAL_ACTION_SELECT);
									}
								});
						break;
					case 1:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.simple_submit, right);
						Button ps_btn = (Button) right
								.findViewById(R.id.simple_submit_btn);
						ps_btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								showDialog(SUBMIT_ACTION);
								if (period < 3) {
									period_indicator.setText("Start " + period);
									showDialog(SUBMIT_ACTION);
									api_calls.send_period_start(period,
											api_calls.make_context(
													home_team.getScore(),
													away_team.getScore()));
								}
								dismissDialog(OFFICIAL_ACTION_SELECT);
							}
						});
						break;
					case 2:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.simple_submit, right);
						Button pe_btn = (Button) right
								.findViewById(R.id.simple_submit_btn);
						pe_btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								if (period < 3) {
									period_indicator.setText("End " + period);
									showDialog(SUBMIT_ACTION);
									api_calls.send_period_end(period, api_calls
											.make_context(home_team.getScore(),
													away_team.getScore()));
									period++;
								}
								dismissDialog(OFFICIAL_ACTION_SELECT);
							}
						});
						break;
					case 3:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.simple_submit, right);
						Button to_btn = (Button) right
								.findViewById(R.id.simple_submit_btn);
						to_btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								showDialog(SUBMIT_ACTION);
								api_calls.send_timeout(
										null,
										"official",
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
								dismissDialog(OFFICIAL_ACTION_SELECT);
							}
						});
						break;
					case 4:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.simple_submit, right);
						Button med_btn = (Button) right
								.findViewById(R.id.simple_submit_btn);
						med_btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								showDialog(SUBMIT_ACTION);
								api_calls.send_timeout(
										null,
										"media",
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
								dismissDialog(OFFICIAL_ACTION_SELECT);
							}
						});
						break;
					}
				}
			});
			official_actions.setAdapter(off_ad);
			action_dialog.setTitle("Official Action");
			return action_dialog;
		case TEAM_ACTION_SELECT:
			action_dialog = new Dialog(this);
			action_dialog.setContentView(R.layout.team_action);
			final ListView team_actions = (ListView) action_dialog
					.findViewById(R.id.team_action_actions);
			ActionAdapter team_ad = new ActionAdapter(this,
					R.layout.action_item, Arrays.asList(Constants.TEAM_ACTIONS));
			right = (ScrollView) action_dialog
					.findViewById(R.id.team_actions_right);
			team_actions.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					right.removeAllViews();
					for (View v : team_actions.getTouchables()) {
						v.setBackgroundColor(Color.BLACK);
					}
					((LinearLayout) arg1).setBackgroundColor(Color.RED);
					switch (arg2) {
					case 0:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.simple_submit, right);
						Button tech_btn = (Button) right
								.findViewById(R.id.simple_submit_btn);
						tech_btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								showDialog(SUBMIT_ACTION);
								api_calls.send_foul(current_team
										.get_team_player().getId(), null,
										"technical", false, ball_overlay
												.get_court_location(),
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
								dismissDialog(TEAM_ACTION_SELECT);
							}
						});
						break;
					case 1:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.rebound, right);
						final Spinner team_reb_type = (Spinner) right
								.findViewById(R.id.rebound_type);
						Button team_reb_btn = (Button) right
								.findViewById(R.id.rebound_submit_btn);
						team_reb_btn
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										String team_reb_id = current_team
												.get_team_player().getId();
										showDialog(SUBMIT_ACTION);
										api_calls.send_rebound(team_reb_id,
												team_reb_type.getSelectedItem()
														.toString(),
												ball_overlay
														.get_court_location(),
												api_calls.make_context(
														home_team.getScore(),
														away_team.getScore()));
										dismissDialog(TEAM_ACTION_SELECT);
									}
								});
						break;
					case 2:
						ViewGroup.inflate(ScoreGameActivity.this,
								R.layout.simple_submit, right);
						Button timeout_btn = (Button) right
								.findViewById(R.id.simple_submit_btn);
						timeout_btn
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										showDialog(SUBMIT_ACTION);
										api_calls.send_timeout(current_team
												.getId(), "team", api_calls
												.make_context(
														home_team.getScore(),
														away_team.getScore()));
										dismissDialog(TEAM_ACTION_SELECT);
									}
								});
						break;
					}
				}
			});
			team_actions.setAdapter(team_ad);
			action_dialog.setTitle("Action for " + current_team.getName());
			return action_dialog;
		case SELECT_AWAY_STARTERS:
			action_dialog = new Dialog(this);
			action_dialog.setContentView(R.layout.starter_select);
			right = (ScrollView) action_dialog
					.findViewById(R.id.starter_select_right);
			ViewGroup.inflate(this, R.layout.preview_starters, right);
			final Button starter_submit = (Button) right
					.findViewById(R.id.preview_starters_submit_btn);
			starter_submit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					populate_away();
					dismissDialog(SELECT_AWAY_STARTERS);
				}
			});
			starter_submit.setClickable(false);
			starter_submit.setEnabled(false);
			final ListView avail_players = (ListView) action_dialog
					.findViewById(R.id.starter_select_players);
			populate_players(away_team);
			ActionAdapter away_ad = new ActionAdapter(this,
					R.layout.action_item, current_players);
			avail_players.setAdapter(away_ad);
			avail_players.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					checked[arg2] = !checked[arg2];
					Log.d(TAG, "currentpos: " + arg2);
					Log.d(TAG, "checked: " + checked[arg2]);

					ArrayList<View> vs = avail_players.getTouchables();
					String[] starters = new String[5];
					int count = 0;
					for (int i = 0; i < checked.length; i++) {
						String item = avail_players.getItemAtPosition(i)
								.toString();
						if (checked[i]) {
							if (count < 5) {
								starters[count] = item.substring(0,
										item.indexOf(" - "));
							}
							count++;
						}
					}
					for (int i = 0; i < vs.size(); i++) {
						String text = avail_players.getItemAtPosition(i)
								.toString();
						String name_text = text.substring(0,
								text.indexOf(" - "));
						if (Arrays.asList(starters).contains(name_text)) {
							vs.get(i).setBackgroundColor(Color.RED);
						} else {
							vs.get(i).setBackgroundColor(Color.BLACK);
						}
					}
					TextView starter1 = (TextView) right
							.findViewById(R.id.preview_starters_1);
					TextView starter2 = (TextView) right
							.findViewById(R.id.preview_starters_2);
					TextView starter3 = (TextView) right
							.findViewById(R.id.preview_starters_3);
					TextView starter4 = (TextView) right
							.findViewById(R.id.preview_starters_4);
					TextView starter5 = (TextView) right
							.findViewById(R.id.preview_starters_5);

					starter1.setText((starters[0] == null ? "Starter 1: "
							: "Starter 1: " + starters[0]));
					starter2.setText((starters[1] == null ? "Starter 2: "
							: "Starter 2: " + starters[1]));
					starter3.setText((starters[2] == null ? "Starter 3: "
							: "Starter 3: " + starters[2]));
					starter4.setText((starters[3] == null ? "Starter 4: "
							: "Starter 4: " + starters[3]));
					starter5.setText((starters[4] == null ? "Starter 5: "
							: "Starter 5: " + starters[4]));

					Log.d(TAG, "count:" + count);
					if (count == 5) {
						starter_submit.setEnabled(true);
						starter_submit.setClickable(true);
					} else {
						starter_submit.setClickable(false);
						starter_submit.setEnabled(false);
					}
				}
			});
			action_dialog
					.setTitle("Select Starters for " + away_team.getName());
			return action_dialog;
			// alert_dialog = new AlertDialog.Builder(this)
			// .setTitle("5 more starters for " + away_team.getName())
			// .setMultiChoiceItems(players, checked,
			// new Starter_Select(away_team.getName()))
			// .setPositiveButton("Finished", new OnClickListener() {
			// public void onClick(DialogInterface dialog, int which) {
			// populate_away();
			// }
			// }).create();
			// return alert_dialog;
			// Dialog allowing user to select starters for home team
		case SELECT_HOME_STARTERS:
			action_dialog = new Dialog(this);
			action_dialog.setContentView(R.layout.starter_select);
			right = (ScrollView) action_dialog
					.findViewById(R.id.starter_select_right);
			ViewGroup.inflate(this, R.layout.preview_starters, right);
			final Button h_starter_submit = (Button) right
					.findViewById(R.id.preview_starters_submit_btn);
			h_starter_submit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					populate_home();
					dismissDialog(SELECT_HOME_STARTERS);
				}
			});
			h_starter_submit.setClickable(false);
			h_starter_submit.setEnabled(false);
			final ListView h_avail_players = (ListView) action_dialog
					.findViewById(R.id.starter_select_players);
			populate_players(home_team);
			ActionAdapter home_ad = new ActionAdapter(this,
					R.layout.action_item, current_players);
			h_avail_players.setAdapter(home_ad);
			h_avail_players.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					checked[arg2] = !checked[arg2];

					ArrayList<View> vs = h_avail_players.getTouchables();
					String[] starters = new String[5];
					int count = 0;
					for (int i = 0; i < checked.length; i++) {
						String item = h_avail_players.getItemAtPosition(i)
								.toString();
						if (checked[i]) {
							if (count < 5) {
								starters[count] = item.substring(0,
										item.indexOf(" - "));
							}
							count++;
						}
					}
					for (int i = 0; i < vs.size(); i++) {
						String text = h_avail_players.getItemAtPosition(i)
								.toString();
						String name_text = text.substring(0,
								text.indexOf(" - "));
						if (Arrays.asList(starters).contains(name_text)) {
							vs.get(i).setBackgroundColor(Color.RED);
						} else {
							vs.get(i).setBackgroundColor(Color.BLACK);
						}
					}
					TextView starter1 = (TextView) right
							.findViewById(R.id.preview_starters_1);
					TextView starter2 = (TextView) right
							.findViewById(R.id.preview_starters_2);
					TextView starter3 = (TextView) right
							.findViewById(R.id.preview_starters_3);
					TextView starter4 = (TextView) right
							.findViewById(R.id.preview_starters_4);
					TextView starter5 = (TextView) right
							.findViewById(R.id.preview_starters_5);

					starter1.setText((starters[0] == null ? "Starter 1: "
							: "Starter 1: " + starters[0]));
					starter2.setText((starters[1] == null ? "Starter 2: "
							: "Starter 2: " + starters[1]));
					starter3.setText((starters[2] == null ? "Starter 3: "
							: "Starter 3: " + starters[2]));
					starter4.setText((starters[3] == null ? "Starter 4: "
							: "Starter 4: " + starters[3]));
					starter5.setText((starters[4] == null ? "Starter 5: "
							: "Starter 5: " + starters[4]));

					if (count == 5) {
						h_starter_submit.setEnabled(true);
						h_starter_submit.setClickable(true);
					} else {
						h_starter_submit.setClickable(false);
						h_starter_submit.setEnabled(false);
					}
				}
			});
			action_dialog
					.setTitle("Select Starters for " + away_team.getName());
			return action_dialog;
			// alert_dialog = new AlertDialog.Builder(this)
			// .setTitle("5 more starters for " + home_team.getName())
			// .setMultiChoiceItems(players, checked,
			// new Starter_Select(home_team.getName()))
			// .setPositiveButton("Finished", new OnClickListener() {
			// public void onClick(DialogInterface dialog, int which) {
			// switch (which) {
			// case DialogInterface.BUTTON_POSITIVE:
			// populate_home();
			// break;
			// }
			// }
			// }).create();
			// return alert_dialog;
			// Dialog for player action
		case PLAYER_ACTION_DIALOG:
			action_dialog = new Dialog(this);
			action_dialog.setContentView(R.layout.action_dialog);
			right = (ScrollView) action_dialog.findViewById(R.id.shot_d_right);
			final ListView actions = (ListView) action_dialog
					.findViewById(R.id.shot_d_lv_left);
			ActionAdapter ad = new ActionAdapter(this, R.layout.action_item,
					Arrays.asList(player_actions));
			actions.setOnItemClickListener(new PlayerActionClick(actions));
			actions.setAdapter(ad);
			action_dialog.setTitle("Action for "
					+ current_team.get_player_with_id(current_player)
							.getLast_name());
			return action_dialog;
		case SUBSTITUTION:
			String name = "!";
			action_dialog = new Dialog(this);
			action_dialog.setContentView(R.layout.empty_sub);
			final ListView sub = (ListView) action_dialog
					.findViewById(R.id.empty_sub_lv);
			ActionAdapter sub_ad = new ActionAdapter(this,
					R.layout.action_item, Arrays.asList(sub_action));
			sub.setAdapter(sub_ad);
			right = (ScrollView) action_dialog
					.findViewById(R.id.empty_sub_right);
			ViewGroup.inflate(this, R.layout.substitution, right);
			final Spinner emp_sub_sp = (Spinner) right
					.findViewById(R.id.sub_players);
			populate_off_court(current_team);
			ArrayAdapter<String> emp_ad = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, current_players);
			emp_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			emp_sub_sp.setAdapter(emp_ad);
			Button emp_submit = (Button) right
					.findViewById(R.id.sub_submit_btn);
			emp_submit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String emp_info = emp_sub_sp.getSelectedItem().toString();
					String emp_trunc_name = emp_info.substring(0, 5);

					String emp_jnum = emp_info.substring(emp_info
							.indexOf(" - ") + 3);
					if (current_team.getName().equals(away_team.getName())) {
						String temp_id = away_team.get_player_with_jersey(
								Integer.parseInt(emp_jnum)).getId();
						away_team.get_player_with_id(temp_id).setOn_court(true);
					} else {
						String temp_id = home_team.get_player_with_jersey(
								Integer.parseInt(emp_jnum)).getId();
						home_team.get_player_with_id(temp_id).setOn_court(true);
					}
					current.setText(emp_trunc_name + "\n" + emp_jnum);
					dismissDialog(SUBSTITUTION);
				}
			});
			action_dialog.setTitle("Substitution for " + name);
			return action_dialog;
		default:
			return null;
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

			if (game_obj.has("gameSetupData")) {
				Log.d(TAG, "Previous game data present.");
			} else {
				Log.d(TAG, "No game events");
			}

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
		for (int i = 0; i < current_players.size(); i++) {
			if (count == 5)
				break;
			if (checked[i]) {
				String player_info = current_players.get(i);
				String trunc_name = player_info.substring(0, 5);
				String jersey_num = player_info.substring(player_info
						.indexOf(" - ") + 3);
				away_team.get_player_with_jersey(Integer.parseInt(jersey_num))
						.setOn_court(true);
				starters[count] = trunc_name + "\n" + jersey_num;
				away_starters[count] = away_team.get_player(i + 1).getId();
				count++;
				checked[i] = false;
			}
		}
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
		for (int i = 0; i < current_players.size(); i++) {
			if (count == 5)
				break;
			if (checked[i]) {
				String player_info = current_players.get(i);
				String trunc_name = player_info.substring(0, 5);
				String jersey_num = player_info.substring(player_info
						.indexOf(" - ") + 3);
				home_team.get_player_with_jersey(Integer.parseInt(jersey_num))
						.setOn_court(true);
				starters[count] = trunc_name + "\n" + jersey_num;
				home_starters[count] = home_team.get_player(i + 1).getId();
				count++;
				checked[i] = false;
			}
		}
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
		current_team = away_team;
		showDialog(SELECT_AWAY_STARTERS);
	}

	/**
	 * Initializes a selection array of CharSequence's and a selected array of
	 * booleans. Shows dialog to select starters for home team.
	 */
	private void select_home_starters() {
		populate_players(home_team);
		current_team = home_team;
		showDialog(SELECT_HOME_STARTERS);

	}

	/**
	 * Fills players array with all players from team
	 * 
	 * @param team
	 *            - Team to collect players from
	 */
	private void populate_players(Team team) {
		ArrayList<Player> players_list = (ArrayList<Player>) team.getPlayers();
		checked = new boolean[players_list.size() - 1];
		current_players = new ArrayList<String>();
		for (int i = 0; i < players_list.size() - 1; i++) {
			String s = players_list.get(i + 1).getLast_name() + " - "
					+ players_list.get(i + 1).getJersey_number();
			current_players.add(s);
			checked[i] = false;
		}
	}

	/**
	 * Fills players array with all players from team currently on the court
	 * 
	 * @param team
	 *            - Team to collect players from
	 */
	private void populate_on_court(Team team) {
		ArrayList<Player> players_list = (ArrayList<Player>) team.getPlayers();
		ArrayList<Player> on_court = new ArrayList<Player>();

		for (int i = 1; i < players_list.size(); i++) {
			Player temp = players_list.get(i);
			if (temp.isOn_court() && !temp.getId().equals(current_player))
				on_court.add(players_list.get(i));
		}
		current_players = new ArrayList<String>();
		checked = null;
		for (int i = 0; i < on_court.size(); i++) {
			String s = on_court.get(i).getLast_name() + " - "
					+ on_court.get(i).getJersey_number();
			current_players.add(s);
		}
	}

	/**
	 * Fills players array with all players from team currently off court
	 * 
	 * @param team
	 *            - Team to collect players from
	 */
	private void populate_off_court(Team team) {
		ArrayList<Player> players_list = (ArrayList<Player>) team.getPlayers();
		ArrayList<Player> off_court = new ArrayList<Player>();

		for (int i = 1; i < players_list.size(); i++) {
			Player temp = players_list.get(i);
			if (!temp.isOn_court() && temp.isEligible()) {
				off_court.add(players_list.get(i));
			}
		}
		current_players = new ArrayList<String>();
		checked = null;
		for (int i = 0; i < off_court.size(); i++) {
			String s = off_court.get(i).getLast_name() + " - "
					+ off_court.get(i).getJersey_number();
			current_players.add(s);
		}
	}

	private void add_team_player(Team team) {
		ArrayList<String> temp_list = new ArrayList<String>();
		temp_list.add(team.getName());
		for (int i = 0; i < current_players.size(); i++) {
			temp_list.add(current_players.get(i));
		}
		current_players = temp_list;
	}

	/**
	 * Wipes references to all existing dialogs
	 */
	private void remove_dialogs() {
		removeDialog(LOAD_GAMES_PROGRESS);
		removeDialog(OFFICIAL_ACTION_SELECT);
		removeDialog(PLAYER_ACTION_DIALOG);
		removeDialog(SELECT_AWAY_STARTERS);
		removeDialog(SELECT_HOME_STARTERS);
		removeDialog(SUBMIT_ACTION);
		removeDialog(SUBMIT_GAME_DATA);
		removeDialog(SUBSTITUTION);
		removeDialog(TEAM_ACTION_SELECT);
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
			case 2:
				dismissDialog(SUBMIT_ACTION);
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
			remove_dialogs();
			TextView temp = (TextView) v;
			if (!temp.getText().equals("!")) {
				String text = temp.getText().toString();
				String jersey = text.substring(text.indexOf("\n") + 1);
				current_player = away_team.get_player_with_jersey(
						Integer.parseInt(jersey)).getId();
				current_team = away_team;
				current = temp;
				Log.d(TAG, "jersey/currentplayer: "
						+ jersey
						+ "/"
						+ current_team.get_player_with_id(current_player)
								.getLast_name());
				showDialog(PLAYER_ACTION_DIALOG);
			} else {
				current_player = null;
				current_team = away_team;
				current = temp;
				populate_off_court(away_team);
				showDialog(SUBSTITUTION);
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
			remove_dialogs();
			TextView temp = (TextView) v;
			if (!temp.getText().equals("!")) {
				String text = temp.getText().toString();
				String jersey = text.substring(text.indexOf("\n") + 1);
				current_player = home_team.get_player_with_jersey(
						Integer.parseInt(jersey)).getId();
				current_team = home_team;
				current = temp;
				Log.d(TAG, "jersey/currentplayer: "
						+ jersey
						+ "/"
						+ current_team.get_player_with_id(current_player)
								.getLast_name());
				showDialog(PLAYER_ACTION_DIALOG);
			} else {
				current_player = null;
				current_team = home_team;
				current = temp;
				populate_off_court(home_team);
				showDialog(SUBSTITUTION);
			}
		}
	}

	/**
	 * OnClickListener for each team's Action button
	 * 
	 * @author Steve
	 * 
	 */
	private class TeamActionListener implements
			android.view.View.OnClickListener {

		public void onClick(View v) {
			remove_dialogs();
			switch (v.getId()) {
			case R.id.score_game_away_action:
				current_team = away_team;
				showDialog(TEAM_ACTION_SELECT);
				break;
			case R.id.score_game_home_action:
				current_team = home_team;
				showDialog(TEAM_ACTION_SELECT);
				break;
			}
		}
	}

	/**
	 * ArrayAdapter to display list of player actions on side panel
	 * 
	 * @author Steve
	 * 
	 */
	private class ActionAdapter extends ArrayAdapter<String> {

		private int resource;

		public ActionAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			resource = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout action_view;

			// initialize rowView
			if (convertView == null) {
				action_view = new LinearLayout(getContext());
				String inflate = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater li = (LayoutInflater) getContext()
						.getSystemService(inflate);
				li.inflate(resource, action_view, true);
			} else {
				action_view = (LinearLayout) convertView;
			}

			TextView text = (TextView) action_view
					.findViewById(R.id.action_item_text);
			text.setTextColor(Color.WHITE);
			text.setText(getItem(position));

			if (checked != null) {
				if (checked[position]) {
					action_view.setBackgroundColor(Color.RED);
				} else {
					action_view.setBackgroundColor(Color.BLACK);
				}
			}

			return action_view;
		}
	}

	/**
	 * OnItemClickListener for list of player actions on left panel. Colors
	 * selected item, inflates corresponding view, and conducts all interactions
	 * with api_calls. Meat of activity.
	 * 
	 * @author Steve
	 * 
	 */
	private class PlayerActionClick implements OnItemClickListener {
		private ListView actions;

		public PlayerActionClick(ListView actions) {
			this.actions = actions;
		}

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			right.removeAllViews();
			for (View v : actions.getTouchables()) {
				v.setBackgroundColor(Color.BLACK);
			}
			((LinearLayout) arg1).setBackgroundColor(Color.RED);
			switch (arg2) {
			// REBOUND
			case 0:
				ViewGroup.inflate(ScoreGameActivity.this, R.layout.rebound,
						right);
				Button reb_submit = (Button) right
						.findViewById(R.id.rebound_submit_btn);
				final Spinner reb_type = (Spinner) right
						.findViewById(R.id.rebound_type);
				reb_submit.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (reb_type.getSelectedItemId() == 0) {
							current_team.get_player_with_id(current_player)
									.off_rb();
						} else {
							current_team.get_player_with_id(current_player)
									.def_rb();
						}

						showDialog(SUBMIT_ACTION);
						api_calls.send_rebound(current_player, reb_type
								.getSelectedItem().toString(), ball_overlay
								.get_court_location(), api_calls.make_context(
								home_team.getScore(), away_team.getScore()));
						dismissDialog(PLAYER_ACTION_DIALOG);
					}
				});
				break;
			// MADE SHOT
			case 1:
				ViewGroup.inflate(ScoreGameActivity.this, R.layout.made_shot,
						right);
				final Spinner assisting = (Spinner) right
						.findViewById(R.id.made_shot_assisted_spin);
				final CheckBox assisted = (CheckBox) right
						.findViewById(R.id.made_shot_assisted);
				Button submit = (Button) right
						.findViewById(R.id.made_shot_submit_btn);
				final CheckBox goal_tending = (CheckBox) right
						.findViewById(R.id.made_shot_gt);
				final CheckBox fastbreak = (CheckBox) right
						.findViewById(R.id.made_shot_fb);
				final Spinner shot_type = (Spinner) right
						.findViewById(R.id.made_shot_type);
				final TextView asst_prompt = (TextView) right
						.findViewById(R.id.made_shot_assisted_tv);
				populate_on_court(current_team);
				ArrayAdapter<String> ast_ad = new ArrayAdapter<String>(
						ScoreGameActivity.this,
						android.R.layout.simple_spinner_item, current_players);
				ast_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				assisting.setAdapter(ast_ad);
				assisted.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (assisted.isChecked()) {
							assisting.setVisibility(View.VISIBLE);
							asst_prompt.setVisibility(View.VISIBLE);
						} else {
							assisting.setVisibility(View.GONE);
							asst_prompt.setVisibility(View.GONE);
						}
					}
				});
				submit.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						int points = 0;
						String sel_shot = null;
						fast_break = fastbreak.isChecked();
						goaltending = goal_tending.isChecked();
						if (shot_type.getSelectedItemPosition() == 0) {
							sel_shot = "jump-shot";
							points = 3;
						} else if (shot_type.getSelectedItemPosition() == 1) {
							points = 2;
							sel_shot = "jump-shot";
						} else if (shot_type.getSelectedItemPosition() == 5) {
							points = 1;
							sel_shot = "free-throw";
							fast_break = false;
							goaltending = false;
						} else {
							sel_shot = Constants.SHOT_TYPES[shot_type
									.getSelectedItemPosition()];
							points = 2;
						}
						current_team.get_player_with_id(current_player)
								.made_shot(points);
						current_team.incrementScore(points);
						if (current_team.getName().equals(away_team.getName())) {
							away_score.setText("" + away_team.getScore());
						} else {
							home_score.setText("" + home_team.getScore());
						}
						String assist_id = null;
						if (assisted.isChecked()) {
							String player_info = current_players.get(assisting
									.getSelectedItemPosition());
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							assist_id = current_team.get_player_with_jersey(
									Integer.parseInt(jersey_num)).getId();
							current_team.get_player_with_id(assist_id).assist();
						}
						showDialog(SUBMIT_ACTION);
						api_calls.send_made_shot(current_player, (assisted
								.isChecked() ? assist_id : null), sel_shot,
								points, fast_break, goaltending, ball_overlay
										.get_court_location(), api_calls
										.make_context(home_team.getScore(),
												away_team.getScore()));
						dismissDialog(PLAYER_ACTION_DIALOG);
					}
				});
				break;
			// MISSED SHOT
			case 2:
				ViewGroup.inflate(ScoreGameActivity.this, R.layout.missed_shot,
						right);
				final Spinner blocker = (Spinner) right
						.findViewById(R.id.missed_shot_blocked_spin);
				final Spinner miss_type = (Spinner) right
						.findViewById(R.id.missed_shot_type);
				final CheckBox blocked = (CheckBox) right
						.findViewById(R.id.missed_shot_blocked);
				final CheckBox miss_fastbreak = (CheckBox) right
						.findViewById(R.id.missed_shot_fb);
				Button miss_submit = (Button) right
						.findViewById(R.id.missed_shot_submit_btn);
				final TextView bloc_prompt = (TextView) right
						.findViewById(R.id.missed_shot_blocked_tv);
				populate_on_court((current_team.getName().equals(
						away_team.getName()) ? home_team : away_team));
				ArrayAdapter<String> block_ad = new ArrayAdapter<String>(
						ScoreGameActivity.this,
						android.R.layout.simple_spinner_item, current_players);
				block_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				blocker.setAdapter(block_ad);
				blocked.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (blocked.isChecked()) {
							bloc_prompt.setVisibility(View.VISIBLE);
							blocker.setVisibility(View.VISIBLE);
						} else {
							bloc_prompt.setVisibility(View.GONE);
							blocker.setVisibility(View.GONE);
						}
					}
				});
				miss_submit.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						String blocker_id = null;
						if (blocked.isChecked()) {
							String b_player = blocker.getSelectedItem()
									.toString();
							String b_jnum = b_player.substring(b_player
									.indexOf(" - ") + 3);
							if (current_team.getName().equals(
									away_team.getName())) {
								blocker_id = home_team.get_player_with_jersey(
										Integer.parseInt(b_jnum)).getId();
								home_team.get_player_with_id(blocker_id)
										.block();
							} else {
								blocker_id = away_team.get_player_with_jersey(
										Integer.parseInt(b_jnum)).getId();
								away_team.get_player_with_id(blocker_id)
										.block();
							}
						}
						String m_type = null;
						int m_pts = 0;
						boolean m_fb = miss_fastbreak.isChecked();
						if (miss_type.getSelectedItemPosition() == 0) {
							m_type = "jump-shot";
							m_pts = 3;
							current_team.get_player_with_id(current_player)
									.missed_shot();
						} else if (miss_type.getSelectedItemPosition() == 1) {
							m_type = "jump-shot";
							m_pts = 2;
							current_team.get_player_with_id(current_player)
									.missed_shot();
						} else if (miss_type.getSelectedItemPosition() == 5) {
							m_type = "free-throw";
							m_pts = 1;
							m_fb = false;
							current_team.get_player_with_id(current_player)
									.missed_ft();
						} else {
							m_type = Constants.SHOT_TYPES[miss_type
									.getSelectedItemPosition()];
							m_pts = 2;
							current_team.get_player_with_id(current_player)
									.missed_shot();
						}
						showDialog(SUBMIT_ACTION);
						api_calls.send_missed_shot(current_player, blocker_id,
								m_type, m_pts, m_fb, ball_overlay
										.get_court_location(), api_calls
										.make_context(home_team.getScore(),
												away_team.getScore()));
						dismissDialog(PLAYER_ACTION_DIALOG);
					}
				});
				break;
			// TURNOVER
			case 3:
				ViewGroup.inflate(ScoreGameActivity.this, R.layout.turnover,
						right);
				final Spinner turn_type = (Spinner) right
						.findViewById(R.id.turnover_type);
				final CheckBox forced = (CheckBox) right
						.findViewById(R.id.turnover_forced);
				final Spinner forcer = (Spinner) right
						.findViewById(R.id.turnover_forcer);
				final TextView force_prompt = (TextView) right
						.findViewById(R.id.turnover_forced_tv);
				Button turn_submit = (Button) right
						.findViewById(R.id.turnover_submit_btn);

				populate_on_court((current_team.getName().equals(
						away_team.getName()) ? home_team : away_team));
				ArrayAdapter<String> forced_ad = new ArrayAdapter<String>(
						ScoreGameActivity.this,
						android.R.layout.simple_spinner_item, current_players);
				forced_ad
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				forcer.setAdapter(forced_ad);
				forced.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (forced.isChecked()) {
							force_prompt.setVisibility(View.VISIBLE);
							forcer.setVisibility(View.VISIBLE);
						} else {
							force_prompt.setVisibility(View.GONE);
							forcer.setVisibility(View.GONE);
						}
					}
				});

				turn_submit.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						String forcer_id = null;
						if (forced.isChecked()) {
							String f_player = forcer.getSelectedItem()
									.toString();
							String f_jnum = f_player.substring(f_player
									.indexOf(" - ") + 3);
							if (current_team.getName().equals(
									away_team.getName())) {
								forcer_id = home_team.get_player_with_jersey(
										Integer.parseInt(f_jnum)).getId();
							} else {
								forcer_id = away_team.get_player_with_jersey(
										Integer.parseInt(f_jnum)).getId();
							}
						}
						current_team.get_player_with_id(current_player)
								.turnover();
						showDialog(SUBMIT_ACTION);
						api_calls.send_turnover(current_player, forcer_id,
								turn_type.getSelectedItem().toString(),
								ball_overlay.get_court_location(), api_calls
										.make_context(home_team.getScore(),
												away_team.getScore()));
						dismissDialog(PLAYER_ACTION_DIALOG);
					}
				});
				break;
			// FOUL
			case 4:
				ViewGroup.inflate(ScoreGameActivity.this, R.layout.foul, right);
				final Spinner foul_type_sp = (Spinner) right
						.findViewById(R.id.foul_type);
				final TextView drawn_prompt = (TextView) right
						.findViewById(R.id.foul_drawn_tv);
				final Spinner drawn_by = (Spinner) right
						.findViewById(R.id.foul_drawn_by);
				populate_on_court((current_team.getName().equals(
						away_team.getName()) ? home_team : away_team));
				ArrayAdapter<String> drawn_ad = new ArrayAdapter<String>(
						ScoreGameActivity.this,
						android.R.layout.simple_spinner_item, current_players);
				drawn_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				drawn_by.setAdapter(drawn_ad);
				final CheckBox ejected = (CheckBox) right
						.findViewById(R.id.foul_ejected);
				final CheckBox drawn = (CheckBox) right
						.findViewById(R.id.foul_drawn);
				drawn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (drawn.isChecked()) {
							drawn_prompt.setVisibility(View.VISIBLE);
							drawn_by.setVisibility(View.VISIBLE);
						} else {
							drawn_prompt.setVisibility(View.GONE);
							drawn_by.setVisibility(View.GONE);
						}
					}
				});
				Button foul_submit = (Button) right
						.findViewById(R.id.foul_submit_btn);
				foul_submit.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						String drawn_id = null;
						if (drawn.isChecked()) {
							String d_player = drawn_by.getSelectedItem()
									.toString();
							String d_jnum = d_player.substring(d_player
									.indexOf(" - ") + 3);
							if (current_team.getName().equals(
									away_team.getName())) {
								drawn_id = home_team.get_player_with_jersey(
										Integer.parseInt(d_jnum)).getId();
							} else {
								drawn_id = away_team.get_player_with_jersey(
										Integer.parseInt(d_jnum)).getId();
							}
						}
						current_team.get_player_with_id(current_player).foul();
						showDialog(SUBMIT_ACTION);
						api_calls.send_foul(current_player, drawn_id,
								foul_type_sp.getSelectedItem().toString(),
								ejected.isChecked(), ball_overlay
										.get_court_location(), api_calls
										.make_context(home_team.getScore(),
												away_team.getScore()));
						dismissDialog(PLAYER_ACTION_DIALOG);
					}
				});
				break;
			// SUBSTITUTION
			case 5:
				ViewGroup.inflate(ScoreGameActivity.this,
						R.layout.substitution, right);
				final Spinner in_players = (Spinner) right
						.findViewById(R.id.sub_players);
				populate_off_court(current_team);
				ArrayAdapter<String> sub_adapter = new ArrayAdapter<String>(
						ScoreGameActivity.this,
						android.R.layout.simple_spinner_item, current_players);
				sub_adapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				in_players.setAdapter(sub_adapter);
				Button sub_submit = (Button) right
						.findViewById(R.id.sub_submit_btn);
				sub_submit.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						String in_id = null;
						String s_player = in_players.getSelectedItem()
								.toString();
						String trunc_name = s_player.substring(0, 5);
						String s_jnum = s_player.substring(s_player
								.indexOf(" - ") + 3);
						if (current_team.getName().equals(away_team.getName())) {
							in_id = away_team.get_player_with_jersey(
									Integer.parseInt(s_jnum)).getId();
							away_team.get_player_with_id(in_id).setOn_court(
									true);
							away_team.get_player_with_id(current_player)
									.setOn_court(false);
						} else {
							in_id = home_team.get_player_with_jersey(
									Integer.parseInt(s_jnum)).getId();
							home_team.get_player_with_id(in_id).setOn_court(
									true);
							home_team.get_player_with_id(current_player)
									.setOn_court(false);
						}
						current.setText(trunc_name + "\n" + s_jnum);
						showDialog(SUBMIT_ACTION);
						api_calls.send_substitution(in_id, current_player,
								api_calls.make_context(home_team.getScore(),
										away_team.getScore()));
						dismissDialog(PLAYER_ACTION_DIALOG);
					}
				});
				break;
			}

		}
	}
}
