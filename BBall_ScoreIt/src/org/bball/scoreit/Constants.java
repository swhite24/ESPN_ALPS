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

}
