package org.bball.scoreit;

import java.text.SimpleDateFormat;

/**
 * Interface containing commonly used constants.
 * 
 * @author Steve
 * 
 */
public interface Constants {
	// application specific values
	public static final String ACCESS_KEY = "96a55a98a49c7f4f795eee184d20414eb8a95178";
	public static final String SHARED_SECRET = "oR72b4paDj3AJmZGMEqswvQMk61FYbsOZSHEICn0";

	// ALPS date format
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ";
	public static final SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

	// URLS for specific methods
	public static final String LOGIN = "https://api.espnalps.com/login";
	public static final String GAMES = "http://api.espnalps.com/v0/cbb/games";
	public static final String GET_GAME_DATA = "http://api.espnalps.com/v0/cbb/getGameData";
	public static final String SET_GAME_DATA = "http://api.espnalps.com/v0/cbb/setGameData";
	public static final String SUB_URL = "http://api.espnalps.com/v0/cbb/substitution";
	public static final String REBOUND_URL = "http://api.espnalps.com/v0/cbb/rebound";
	public static final String MADESHOT_URL = "http://api.espnalps.com/v0/cbb/madeShot";
	public static final String MISSEDSHOT_URL = "http://api.espnalps.com/v0/cbb/missedShot";
	public static final String TURNOVER_URL = "http://api.espnalps.com/v0/cbb/turnover";
	public static final String FOUL_URL = "http://api.espnalps.com/v0/cbb/foul";
	public static final String TIMEOUT_URL = "http://api.espnalps.com/v0/cbb/timeout";
	public static final String JUMPBALL_URL = "http://api.espnalps.com/v0/cbb/jumpBall";
	public static final String START_PERIOD_URL = "http://api.espnalps.com/v0/cbb/periodStart";
	public static final String END_PERIOD_URL = "http://api.espnalps.com/v0/cbb/periodEnd";

	// Intents
	public static final String LOGIN_ACTIVITY = "org.bball.scoreit.LoginActivity";
	public static final String GETGAMES_ACTIVITY = "org.bball.scoreit.GetGamesActivity";

	// Payload Keys
	public static final String PAYLOAD = "PAYLOAD";
	public static final String URL = "URL";
	public static final String API_CALL = "API_CALL";
	public static final String TYPE = "TYPE";
	public static final String GAME_DATA = "GAME_DATA";
	public static final String METHOD_ID = "METHOD_ID";

	// account info
	public static final String USERNAME = "swhite24";
	public static final String PASSWORD = "!WVUalps";

	// options for various actions
	public static final CharSequence[] REBOUND_OPTIONS = { "defensive",
			"offensive" };
	public static final String[] REB_OPTIONS = { "defensive", "offensive" };
	public static final CharSequence[] SHOT_OPTIONS = { "3pt jump-shot",
			"2pt jump-shot", "layup", "dunk", "tip-in", "free-throw" };
	public static final String[] SHOT_TYPES = { "3pt jump-shot",
		"2pt jump-shot", "layup", "dunk", "tip-in", "free-throw" };
	public static final CharSequence[] FOUL_OPTIONS = { "blocking", "charging",
			"shooting", "offensive", "personal", "technical", "flagrant" };
	public static final CharSequence[] TURNOVER_OPTIONS = { "traveling",
			"lost-ball", "offensive-foul", "out-of-bounds", "violation",
			"offensive-goaltending", "thrown-away" };
	public static final CharSequence[] TIMEOUT_OPTIONS = { "team", "official",
			"media" };
	public static final CharSequence[] TEAM_ACTION_OPTIONS = {
			"Team Technical Foul", "Team Rebound", "Timeout" };
	public static final CharSequence[] OFFICIAL_OPTIONS = { "Jump Ball",
			"Period Start", "Period End", "Official Timeout", "Media Timeout" };

}
