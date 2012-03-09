package org.bball.scoreit;

import java.text.SimpleDateFormat;

/**
 * Interface containing commonly used constants.
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
	
	// Intents
	public static final String LOGIN_ACTIVITY = "org.bball.scoreit.LoginActivity";
	public static final String GETGAMES_ACTIVITY = "org.bball.scoreit.GetGamesActivity";
	
	// Payload Keys
	public static final String PAYLOAD = "PAYLOAD";
	public static final String URL = "URL";
	public static final String API_CALL = "API_CALL";
	public static final String TYPE = "TYPE";
	
	
	// account info
	public static final String USERNAME = "swhite24";
	public static final String PASSWORD = "!WVUalps";

	
	
}
