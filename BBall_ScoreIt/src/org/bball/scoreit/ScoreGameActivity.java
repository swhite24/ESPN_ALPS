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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoreGameActivity extends Activity {

	private static final String TAG = "BBALL_SCOREIT::SCOREGAMEACTIVITY";
	private static final int LOAD_GAMES_PROGRESS = 0;
	private static final int SUBMIT_GAME_DATA = 1;
	private static final int SELECT_AWAY_STARTERS = 2;
	private static final int SELECT_HOME_STARTERS = 3;
	private static final int SUBMIT_ACTION = 4;
	private static final int AWAY_PLAYER_SUBSTITUTION = 5;
	private static final int HOME_PLAYER_SUBSTITUTION = 6;
	private static final int AWAY_PLAYER_ACTION = 10;
	private static final int HOME_PLAYER_ACTION = 11;
	private static final int PLAYER_REBOUND_TYPE_SELECT = 12;
	private static final int PLAYER_SHOT_TYPE_SELECT = 13;
	private static final int PLAYER_TURNOVER_TYPE_SELECT = 14;
	private static final int PLAYER_FOUL_TYPE_SELECT = 15;
	private static final int CHECK_ASSISTED = 20;
	private static final int CHECK_FAST_BREAK = 21;
	private static final int CHECK_GOALTENDING = 22;
	private static final int CHECK_BLOCKED = 23;
	private static final int CHECK_FORCED = 24;
	private static final int CHECK_DRAWN = 25;
	private static final int CHECK_EJECTED = 26;
	private static final int CHOOSE_ASSISTER = 30;
	private static final int CHOOSE_BLOCKER = 31;
	private static final int CHOOSE_FORCER = 32;
	private static final int CHOOSE_DRAWER = 33;
	private static final int CHOOSE_AWAY_JUMPER = 34;
	private static final int CHOOSE_HOME_JUMPER = 35;
	private static final int CHOOSE_JUMP_WINNER = 36;
	private static final int TEAM_ACTION_SELECT = 40;
	private static final int OFFICIAL_ACTION_SELECT = 50;
	private BallOverlay ball_overlay;
	private GenericReceiver generic_receiver;
	private AwayPlayerListener away_player_click;
	private HomePlayerListener home_player_click;
	private TeamActionListener team_action_click;
	private ProgressDialog progress_dialog;
	private AlertDialog alert_dialog, away_dialog, home_dialog;
	private API_Calls api_calls;
	private Game game;
	public static Team home_team, away_team;
	private Team current_team;
	private TextView away1, away2, away3, away4, away5;
	private TextView home1, home2, home3, home4, home5;
	private TextView away_tv, home_tv, current;
	private TextView home_score, away_score, period_indicator;
	private Button away_click, home_click;
	private ImageView court;
	private CharSequence[] players;
	private String[] away_starters, home_starters, player_actions;
	private String current_player, sec_player, shot_type, foul_type;
	private String turn_type;
	private int pts, period;
	private boolean fast_break, goaltending, make, ejected, team;
	private boolean[] checked;
	private static final CharSequence[] yes_no = { "Yes", "No" };

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
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Official Action")
					.setItems(Constants.OFFICIAL_OPTIONS,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										populate_on_court(away_team);
										add_team_player();
										current_team = away_team;
										showDialog(CHOOSE_AWAY_JUMPER);
										break;
									case 1:
										if (period < 3) {
											period_indicator.setText("Start "
													+ period);
											showDialog(SUBMIT_ACTION);
											api_calls
													.send_period_start(
															period,
															api_calls
																	.make_context(
																			home_team
																					.getScore(),
																			away_team
																					.getScore()));
										}
										break;
									case 2:
										if (period < 3) {
											period_indicator.setText("End "
													+ period);
											showDialog(SUBMIT_ACTION);
											api_calls
													.send_period_end(
															period,
															api_calls
																	.make_context(
																			home_team
																					.getScore(),
																			away_team
																					.getScore()));
											period++;
										}
										break;
									case 3:
										showDialog(SUBMIT_ACTION);
										api_calls.send_timeout(null,
												"official",
												api_calls.make_context(
														home_team.getScore(),
														away_team.getScore()));
										break;
									case 4:
										showDialog(SUBMIT_ACTION);
										api_calls.send_timeout(null, "media",
												api_calls.make_context(
														home_team.getScore(),
														away_team.getScore()));
									}
								}
							}).create();
			return alert_dialog;
		case TEAM_ACTION_SELECT:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Action for " + current_team.getName())
					.setItems(Constants.TEAM_ACTION_OPTIONS,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										showDialog(SUBMIT_ACTION);
										api_calls.send_foul(current_team
												.get_team_player().getId(),
												null, "technical", false,
												ball_overlay
														.get_court_location(),
												api_calls.make_context(
														home_team.getScore(),
														away_team.getScore()));
										break;
									case 1:
										current_player = current_team
												.get_team_player().getId();
										showDialog(PLAYER_REBOUND_TYPE_SELECT);
										break;
									case 2:
										showDialog(SUBMIT_ACTION);
										api_calls.send_timeout(current_team
												.getId(), "team", api_calls
												.make_context(
														home_team.getScore(),
														away_team.getScore()));
										break;
									}
								}
							}).create();
			return alert_dialog;
			// Dialog allowing users to select starters for away team
		case SELECT_AWAY_STARTERS:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("5 more starters for " + away_team.getName())
					.setMultiChoiceItems(players, checked,
							new Starter_Select(away_team.getName()))
					.setPositiveButton("Finished", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							populate_away();
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
			away_dialog = new AlertDialog.Builder(this)
					.setTitle(
							"Action for "
									+ current_team.get_player_with_id(
											current_player).getLast_name())
					.setItems(player_actions, new AwayPlayerAction()).create();
			return away_dialog;
		case HOME_PLAYER_ACTION:
			home_dialog = new AlertDialog.Builder(this)
					.setTitle(
							"Action for "
									+ current_team.get_player_with_id(
											current_player).getLast_name())
					.setItems(player_actions, new HomePlayerAction()).create();
			return home_dialog;
		case AWAY_PLAYER_SUBSTITUTION:
			String name = null;
			if (current_player == null)
				name = "!";
			else
				name = current_team.get_player_with_id(current_player)
						.getLast_name();
			away_dialog = new AlertDialog.Builder(this)
					.setTitle("Substitution for " + name)
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String trunc_name = player_info.substring(0, 5);
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							away_team.get_player_with_jersey(
									Integer.parseInt(jersey_num)).setOn_court(
									true);
							if (current_player != null) {
								away_team.get_player_with_id(current_player)
										.setOn_court(false);
								showDialog(SUBMIT_ACTION);
								api_calls.send_substitution(
										away_team.get_player_with_jersey(
												Integer.parseInt(jersey_num))
												.getId(), current_player,
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
							}
							String new_player = trunc_name + "\n" + jersey_num;
							current.setText(new_player);
						}
					}).create();
			return away_dialog;
		case HOME_PLAYER_SUBSTITUTION:
			String name1 = null;
			if (current_player == null)
				name1 = "!";
			else
				name1 = current_team.get_player_with_id(current_player)
						.getLast_name();
			home_dialog = new AlertDialog.Builder(this)
					.setTitle("Substitution for " + name1)
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String trunc_name = player_info.substring(0, 5);
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							home_team.get_player_with_jersey(
									Integer.parseInt(jersey_num)).setOn_court(
									true);
							if (current_player != null) {
								home_team.get_player_with_id(current_player)
										.setOn_court(false);
								showDialog(SUBMIT_ACTION);
								api_calls.send_substitution(
										home_team.get_player_with_jersey(
												Integer.parseInt(jersey_num))
												.getId(), current_player,
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
							}
							String new_player = trunc_name + "\n" + jersey_num;
							current.setText(new_player);
						}
					}).create();
			return home_dialog;
		case PLAYER_REBOUND_TYPE_SELECT:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Select Rebound Type")
					.setItems(Constants.REBOUND_OPTIONS, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							String type = null;
							if (which == 0) {
								current_team.get_player_with_id(current_player)
										.def_rb();
								type = team ? "team-offensive" : "offensive";
							} else {
								current_team.get_player_with_id(current_player)
										.off_rb();
								type = team ? "team-defensive" : "defensive";
							}
							showDialog(SUBMIT_ACTION);
							api_calls.send_rebound(current_player, type,
									ball_overlay.get_court_location(),
									api_calls.make_context(
											home_team.getScore(),
											away_team.getScore()));
						}
					}).create();
			return alert_dialog;
		case PLAYER_SHOT_TYPE_SELECT:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Select Shot Type")
					.setItems(Constants.SHOT_OPTIONS, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							shot_type = null;
							pts = 0;
							if (which == 0) {
								shot_type = "jump-shot";
								pts = 3;
							} else if (which == 1) {
								shot_type = "jump-shot";
								pts = 2;
							} else if (which == 5) {
								shot_type = "free-throw";
								pts = 1;
								current_team.get_player_with_id(current_player)
										.made_ft();
								current_team.incrementScore(pts);
								if (current_team.getName().equals(
										away_team.getName()))
									away_score.setText(""
											+ current_team.getScore());
								else
									home_score.setText(""
											+ current_team.getScore());
								showDialog(SUBMIT_ACTION);
								api_calls.send_made_shot(
										current_player,
										null,
										shot_type,
										pts,
										false,
										false,
										ball_overlay.get_court_location(),
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
								return;
							} else {
								shot_type = Constants.SHOT_OPTIONS[which]
										.toString();
								pts = 2;
							}
							if (make)
								showDialog(CHECK_ASSISTED);
							else
								showDialog(CHECK_BLOCKED);
						}
					}).create();
			return alert_dialog;
		case PLAYER_TURNOVER_TYPE_SELECT:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Turnover Type")
					.setItems(Constants.TURNOVER_OPTIONS,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									turn_type = Constants.TURNOVER_OPTIONS[which]
											.toString();
									showDialog(CHECK_FORCED);
								}
							}).create();
			return alert_dialog;
		case PLAYER_FOUL_TYPE_SELECT:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Foul Type")
					.setItems(Constants.FOUL_OPTIONS, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							foul_type = Constants.FOUL_OPTIONS[which]
									.toString();
							showDialog(CHECK_EJECTED);
						}
					}).create();
			return alert_dialog;
		case CHECK_ASSISTED:
			alert_dialog = new AlertDialog.Builder(this).setTitle("Assisted?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								populate_on_court(current_team);
								showDialog(CHOOSE_ASSISTER);
							} else {
								sec_player = null;
								showDialog(CHECK_FAST_BREAK);
							}
						}
					}).create();
			return alert_dialog;
		case CHECK_BLOCKED:
			alert_dialog = new AlertDialog.Builder(this).setTitle("Blocked?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								if (current_team.getName().equals(
										away_team.getName()))
									populate_on_court(home_team);
								else
									populate_on_court(away_team);
								showDialog(CHOOSE_BLOCKER);
							} else {
								sec_player = null;
								showDialog(CHECK_FAST_BREAK);
							}
						}
					}).create();
			return alert_dialog;
		case CHECK_FORCED:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Forced Turnover?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								if (current_team.getName().equals(
										away_team.getName()))
									populate_on_court(home_team);
								else
									populate_on_court(away_team);
								showDialog(CHOOSE_FORCER);
							} else {
								sec_player = null;
								current_team.get_player_with_id(current_player)
										.turnover();
								showDialog(SUBMIT_ACTION);
								api_calls.send_turnover(
										current_player,
										sec_player,
										turn_type,
										ball_overlay.get_court_location(),
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
							}
						}
					}).create();
			return alert_dialog;
		case CHECK_EJECTED:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Player Ejected?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ejected = which == 0;
							showDialog(CHECK_DRAWN);
						}
					}).create();
			return alert_dialog;
		case CHECK_DRAWN:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Drawn by other player?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								if (current_team.getName().equals(
										away_team.getName())) {
									populate_on_court(home_team);
								} else {
									populate_on_court(away_team);
								}
								showDialog(CHOOSE_DRAWER);
							} else {
								sec_player = null;
								current_team.get_player_with_id(current_player)
										.foul();
								showDialog(SUBMIT_ACTION);
								api_calls.send_foul(
										current_player,
										sec_player,
										foul_type,
										ejected,
										ball_overlay.get_court_location(),
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
							}
						}
					}).create();
			return alert_dialog;
		case CHECK_FAST_BREAK:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Fast Break?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							fast_break = which == 0;
							if (make)
								showDialog(CHECK_GOALTENDING);
							else {
								current_team.get_player_with_id(current_player)
										.missed_shot();
								if (sec_player != null)
									if (current_team.getName().equals(
											away_team.getName()))
										home_team
												.get_player_with_id(sec_player)
												.block();
									else
										away_team
												.get_player_with_id(sec_player)
												.block();
								showDialog(SUBMIT_ACTION);
								api_calls.send_missed_shot(
										current_player,
										sec_player,
										shot_type,
										pts,
										fast_break,
										ball_overlay.get_court_location(),
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
							}
						}
					}).create();
			return alert_dialog;
		case CHECK_GOALTENDING:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Goaltending?")
					.setItems(yes_no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							goaltending = which == 0;
							current_team.get_player_with_id(current_player)
									.made_shot(pts);
							current_team.incrementScore(pts);
							if (sec_player != null)
								current_team.get_player_with_id(sec_player)
										.assist();
							if (current_team.getName().equals(
									away_team.getName()))
								away_score.setText("" + current_team.getScore());
							else
								home_score.setText("" + current_team.getScore());
							showDialog(SUBMIT_ACTION);
							if (make) {
								api_calls.send_made_shot(
										current_player,
										sec_player,
										shot_type,
										pts,
										fast_break,
										goaltending,
										ball_overlay.get_court_location(),
										api_calls.make_context(
												home_team.getScore(),
												away_team.getScore()));
							}
						}
					}).create();
			return alert_dialog;
		case CHOOSE_ASSISTER:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Assisting Player")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							sec_player = current_team.get_player_with_jersey(
									Integer.parseInt(jersey_num)).getId();
							showDialog(CHECK_FAST_BREAK);
						}
					}).create();
			return alert_dialog;
		case CHOOSE_AWAY_JUMPER:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Away Jumper")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0)
								current_player = current_team.get_team_player()
										.getId();
							else {
								String player_info = players[which].toString();
								String jersey_num = player_info
										.substring(player_info.indexOf(" - ") + 3);
								current_player = current_team
										.get_player_with_jersey(
												Integer.parseInt(jersey_num))
										.getId();
							}
							current_team = home_team;
							populate_on_court(current_team);
							add_team_player();
							showDialog(CHOOSE_HOME_JUMPER);
						}
					}).create();
			return alert_dialog;
		case CHOOSE_HOME_JUMPER:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Home Jumper")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0)
								sec_player = current_team.get_team_player()
										.getId();
							else {
								String player_info = players[which].toString();
								String jersey_num = player_info
										.substring(player_info.indexOf(" - ") + 3);
								sec_player = current_team
										.get_player_with_jersey(
												Integer.parseInt(jersey_num))
										.getId();
							}
							players = new CharSequence[2];
							if (away_team.get_player_with_id(current_player)
									.isIs_team_player()) {
								players[0] = away_team.getName();
								players[1] = home_team.getName();
							} else {
								players[0] = away_team.get_player_with_id(
										current_player).getLast_name()
										+ " - "
										+ away_team.get_player_with_id(
												current_player)
												.getJersey_number();
								players[1] = home_team.get_player_with_id(
										sec_player).getLast_name()
										+ " - "
										+ home_team.get_player_with_id(
												sec_player).getJersey_number();
							}
							showDialog(CHOOSE_JUMP_WINNER);
						}
					}).create();
			return alert_dialog;
		case CHOOSE_JUMP_WINNER:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Winner")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String winner = which == 0 ? current_player
									: sec_player;
							showDialog(SUBMIT_ACTION);
							api_calls.send_jumpball(current_player, sec_player,
									winner, ball_overlay.get_court_location(),
									api_calls.make_context(
											home_team.getScore(),
											away_team.getScore()));
						}
					}).create();
			return alert_dialog;
		case CHOOSE_BLOCKER:
			alert_dialog = new AlertDialog.Builder(this)
					.setTitle("Choose Blocking Player")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							if (current_team.getName().equals(
									away_team.getName()))
								sec_player = home_team.get_player_with_jersey(
										Integer.parseInt(jersey_num)).getId();
							else
								sec_player = away_team.get_player_with_jersey(
										Integer.parseInt(jersey_num)).getId();
							showDialog(CHECK_FAST_BREAK);
						}
					}).create();
			return alert_dialog;
		case CHOOSE_FORCER:
			alert_dialog = new AlertDialog.Builder(this).setTitle("Forced by")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							if (current_team.getName().equals(
									away_team.getName()))
								sec_player = home_team.get_player_with_jersey(
										Integer.parseInt(jersey_num)).getId();
							else
								sec_player = away_team.get_player_with_jersey(
										Integer.parseInt(jersey_num)).getId();
							current_team.get_player_with_id(current_player)
									.turnover();
							showDialog(SUBMIT_ACTION);
							api_calls.send_turnover(current_player, sec_player,
									turn_type, ball_overlay
											.get_court_location(), api_calls
											.make_context(home_team.getScore(),
													away_team.getScore()));
						}
					}).create();
			return alert_dialog;
		case CHOOSE_DRAWER:
			alert_dialog = new AlertDialog.Builder(this).setTitle("Drawn by")
					.setItems(players, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String player_info = players[which].toString();
							String jersey_num = player_info
									.substring(player_info.indexOf(" - ") + 3);
							if (current_team.getName().equals(
									away_team.getName()))
								sec_player = home_team.get_player_with_jersey(
										Integer.parseInt(jersey_num)).getId();
							else
								sec_player = away_team.get_player_with_jersey(
										Integer.parseInt(jersey_num)).getId();
							current_team.get_player_with_id(current_player)
									.foul();
							showDialog(SUBMIT_ACTION);
							api_calls.send_foul(current_player, sec_player,
									foul_type, ejected, ball_overlay
											.get_court_location(), api_calls
											.make_context(home_team.getScore(),
													away_team.getScore()));
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
			switch (which) {
			case 0:
				showDialog(PLAYER_REBOUND_TYPE_SELECT);
				break;
			case 1:
				make = true;
				showDialog(PLAYER_SHOT_TYPE_SELECT);
				break;
			case 2:
				make = false;
				showDialog(PLAYER_SHOT_TYPE_SELECT);
				break;
			case 3:
				showDialog(PLAYER_TURNOVER_TYPE_SELECT);
				break;
			case 4:
				showDialog(PLAYER_FOUL_TYPE_SELECT);
				break;
			case 5:
				current_team = away_team;
				populate_off_court(away_team);
				showDialog(AWAY_PLAYER_SUBSTITUTION);
				break;
			}
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
			switch (which) {
			case 0:
				showDialog(PLAYER_REBOUND_TYPE_SELECT);
				break;
			case 1:
				make = true;
				showDialog(PLAYER_SHOT_TYPE_SELECT);
				break;
			case 2:
				make = false;
				showDialog(PLAYER_SHOT_TYPE_SELECT);
				break;
			case 3:
				showDialog(PLAYER_TURNOVER_TYPE_SELECT);
				break;
			case 4:
				showDialog(PLAYER_FOUL_TYPE_SELECT);
				break;
			case 5:
				current_team = home_team;
				populate_off_court(home_team);
				showDialog(HOME_PLAYER_SUBSTITUTION);
				break;
			}

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
				away_starters[count] = away_team.get_player(i + 1).getId();
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
				home_starters[count] = home_team.get_player(i + 1).getId();
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

	/**
	 * Fills players array with all players from team
	 * 
	 * @param team
	 *            - Team to collect players from
	 */
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

		players = new CharSequence[on_court.size()];
		checked = new boolean[players.length];
		for (int i = 0; i < on_court.size(); i++) {
			players[i] = on_court.get(i).getLast_name() + " - "
					+ on_court.get(i).getJersey_number();
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

		players = new CharSequence[off_court.size()];
		checked = new boolean[players.length];
		for (int i = 0; i < off_court.size(); i++) {
			players[i] = off_court.get(i).getLast_name() + " - "
					+ off_court.get(i).getJersey_number();
		}
	}

	private void add_team_player() {
		CharSequence[] temp = new CharSequence[players.length + 1];
		temp[0] = "Team";
		for (int i = 0; i < players.length; i++) {
			temp[i + 1] = players[i];
		}

		players = temp;
	}

	/**
	 * Wipes references to all existing dialogs
	 */
	private void remove_dialogs() {
		removeDialog(AWAY_PLAYER_ACTION);
		removeDialog(AWAY_PLAYER_SUBSTITUTION);
		removeDialog(CHECK_ASSISTED);
		removeDialog(CHECK_DRAWN);
		removeDialog(CHECK_EJECTED);
		removeDialog(CHECK_FAST_BREAK);
		removeDialog(CHECK_GOALTENDING);
		removeDialog(CHECK_BLOCKED);
		removeDialog(CHOOSE_AWAY_JUMPER);
		removeDialog(CHOOSE_HOME_JUMPER);
		removeDialog(CHOOSE_JUMP_WINNER);
		removeDialog(CHOOSE_BLOCKER);
		removeDialog(CHOOSE_ASSISTER);
		removeDialog(CHOOSE_DRAWER);
		removeDialog(HOME_PLAYER_ACTION);
		removeDialog(HOME_PLAYER_SUBSTITUTION);
		removeDialog(LOAD_GAMES_PROGRESS);
		removeDialog(OFFICIAL_ACTION_SELECT);
		removeDialog(PLAYER_FOUL_TYPE_SELECT);
		removeDialog(PLAYER_REBOUND_TYPE_SELECT);
		removeDialog(PLAYER_SHOT_TYPE_SELECT);
		removeDialog(SELECT_AWAY_STARTERS);
		removeDialog(SELECT_HOME_STARTERS);
		removeDialog(SUBMIT_ACTION);
		removeDialog(SUBMIT_GAME_DATA);
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
			team = false;
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
				alert_dialog = null;
				away_dialog = null;
				showDialog(AWAY_PLAYER_ACTION);
			} else {
				current_player = null;
				current_team = away_team;
				current = temp;
				populate_off_court(away_team);
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
			remove_dialogs();
			team = false;
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
				showDialog(HOME_PLAYER_ACTION);
			} else {
				current_player = null;
				current_team = home_team;
				current = temp;
				populate_off_court(home_team);
				showDialog(HOME_PLAYER_SUBSTITUTION);
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
			team = true;
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
}
