package org.bball.scoreit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class API_Calls {
	private static final String TAG = "BBALL_SCOREIT::API_CALLS";

	private MessageDigest md = null;
	private Context context;
	public static String token = null;

	public static void setToken(String _token) {
		token = _token;
	}

	public static String game_id = null;

	public static void setGameId(String gameId) {
		game_id = gameId;
	}

	// map of api_calls used to correctly initialize broadcast receivers
	public static HashMap<Integer, String> api_map = init_map();

	private static HashMap<Integer, String> init_map() {
		HashMap<Integer, String> temp = new HashMap<Integer, String>();
		temp.put(0, "LOGIN");
		temp.put(1, "GETGAMES");
		temp.put(2, "GAMEDATA");
		temp.put(3, "SETGAMEDATA");
		return temp;
	}

	public API_Calls(Context context) {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Unable to instantiate MessageDigest.");
		}
		this.context = context;
	}

	/**
	 * Logs into system, starting service to retrieve valid token. Token viable
	 * for 5 minutes and is used in all subsequent API calls.
	 * 
	 * @param username
	 *            - username registered espnalps.com
	 * @param password
	 *            - password for said username
	 */
	public void login(String username, String password) {
		// construct JSONObject for login payload
		JSONObject un_pw = new JSONObject();
		try {
			un_pw.put("username", username);
			un_pw.put("password", password);
		} catch (Exception e) {
			Log.e(TAG, "Failed to populate JSONObject.");
		}

		// construct url to login
		String loginURL = Constants.LOGIN + "?signature=" + getSignature()
				+ "&key=" + Constants.ACCESS_KEY;

		// create service_intent and add URL + Payload
		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.URL, loginURL);
		service_intent.putExtra(Constants.PAYLOAD, un_pw.toString());
		service_intent.putExtra(Constants.API_CALL, 0);
		service_intent.putExtra(Constants.TYPE, 0);
		context.startService(service_intent);
	}

	/**
	 * Gets list of all games over the time interval start to end.
	 * 
	 * @param start
	 *            - initial Date
	 * @param end
	 *            - final Date
	 */
	public void get_games(String start, String end) {
		// construct JSONObject for games payload
		JSONObject start_end = new JSONObject();
		try {
			start_end.put("start", start);
			start_end.put("end", end);
		} catch (JSONException e) {
			Log.e(TAG, "Failed to populate JSONObject.");
		}

		// construct games URL
		String gamesURL = Constants.GAMES + "?token=" + token + "&signature="
				+ getSignature() + "&key=" + Constants.ACCESS_KEY;

		// create service_intent and add URL + Payload
		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.URL, gamesURL);
		service_intent.putExtra(Constants.PAYLOAD, start_end.toString());
		service_intent.putExtra(Constants.API_CALL, 1);
		service_intent.putExtra(Constants.TYPE, 0);
		context.startService(service_intent);
	}

	/**
	 * Get game data for game gameId
	 * 
	 * @param gameId
	 *            - string containing gameId of game to get data for
	 */
	public void getGameData(String gameId) {
		setGameId(gameId);
		// Construct URL
		String gameDataURL = Constants.GET_GAME_DATA + "/" + gameId + "?token="
				+ token + "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.URL, gameDataURL);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.TYPE, 1);
		service_intent.putExtra(Constants.METHOD_ID, 0);
		context.startService(service_intent);
	}

	/**
	 * Sets up starting rosters for each team once game setup is complete
	 * 
	 * @param away_on_court
	 *            - String array containing playerId's of players on court for
	 *            away team
	 * @param home_on_court
	 *            - String array containing playerId's of players on court for
	 *            home team
	 */
	public void setGameData(String[] away_on_court, String[] home_on_court) {
		JSONObject game_data_payload = new JSONObject();
		try {
			Calendar current = Calendar.getInstance();
			game_data_payload.put("time",
					Constants.df.format(current.getTime()));

			JSONObject away_roster = new JSONObject();
			away_roster.put("onField", away_on_court);

			JSONObject home_roster = new JSONObject();
			home_roster.put("onField", home_on_court);

			game_data_payload.put("homeTeam", home_roster);
			game_data_payload.put("awayTeam", away_roster);
		} catch (Exception e) {
			Log.e(TAG, "Failed to construct setGameData payload.");
		}

		String setDataURL = Constants.SET_GAME_DATA + "/" + game_id + "?token="
				+ token + "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.URL, setDataURL);
		service_intent
				.putExtra(Constants.PAYLOAD, game_data_payload.toString());
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.METHOD_ID, 1);
		context.startService(service_intent);
	}

	/**
	 * Submits substitution to server with given parameters.
	 * 
	 * @param in_player
	 *            - id of player entering game.
	 * @param out_player
	 *            - id of player exiting game.
	 * @param con
	 *            - properly formatted context
	 */
	public void send_substitution(String in_player, String out_player,
			JSONObject con) {
		JSONObject sub_payload = new JSONObject();
		try {
			sub_payload.put("gameId", game_id);
			sub_payload.put("exitingPlayer", out_player);
			sub_payload.put("enteringPlayer", in_player);
			sub_payload.put("context", con);
		} catch (Exception e) {
			Log.e(TAG,
					"Unable to create substitution payload: " + e.getMessage());
		}

		String sub_url = Constants.SUB_URL + "?token=" + token + "&signature="
				+ getSignature() + "&key=" + Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.PAYLOAD, sub_payload.toString());
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.URL, sub_url);
		service_intent.putExtra(Constants.METHOD_ID, 2);
		context.startService(service_intent);
	}

	/**
	 * Submits rebound to server with given parameter.
	 * 
	 * @param playerId
	 *            - ID of player who got rebound, null if team rebound.
	 * @param type
	 *            - Type of rebound; see Constants.REBOUND_OPTIONS
	 * @param location
	 *            - location on court of rebound; see
	 *            BallOverlay.get_court_location()
	 * @param con
	 *            - context document; see API_Calls.make_context()
	 */
	public void send_rebound(String playerId, String type, JSONArray location,
			JSONObject con) {
		JSONObject rebound_payload = new JSONObject();
		try {
			rebound_payload.put("gameId", game_id);
			if (playerId != null)
				rebound_payload.put("rebounder", playerId);
			rebound_payload.put("reboundType", type);
			rebound_payload.put("location", location);
			rebound_payload.put("context", con);
		} catch (Exception e) {
			Log.e(TAG, "Unable to create rebound_payload: " + e.getMessage());
		}
		String rebound_url = Constants.REBOUND_URL + "?token=" + token
				+ "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.PAYLOAD, rebound_payload.toString());
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.URL, rebound_url);
		service_intent.putExtra(Constants.METHOD_ID, 2);
		context.startService(service_intent);
	}

	/**
	 * Send made shot to server.
	 * 
	 * @param shooter
	 *            - id of shooter
	 * @param assister
	 *            - id of assisting player; null if none
	 * @param type
	 *            - type of shot; see Constants.SHOT_OPTIONS
	 * @param pts
	 *            - pt value of shot
	 * @param fb
	 *            - fast-break opportunity
	 * @param goaltending
	 *            - goaltending committed
	 * @param location
	 *            - location of shot on court; see
	 *            BallOverlay.get_court_location()
	 * @param con
	 *            - context document; see API_Calls.make_context()
	 */
	public void send_made_shot(String shooter, String assister, String type,
			int pts, boolean fb, boolean goaltending, JSONArray location,
			JSONObject con) {
		JSONObject shot_payload = new JSONObject();
		try {
			shot_payload.put("gameId", game_id);
			shot_payload.put("shooter", shooter);
			if (assister != null)
				shot_payload.put("assistedBy", assister);
			shot_payload.put("shotType", type);
			shot_payload.put("pointsScored", pts);
			shot_payload.put("fastBreakOpportunity", fb);
			shot_payload.put("goaltending", goaltending);
			shot_payload.put("location", location);
			shot_payload.put("context", con);
		} catch (Exception e) {
			Log.e(TAG, "Unable to create shot_payload: " + e.getMessage());
		}

		String shot_url = Constants.MADESHOT_URL + "?token=" + token
				+ "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.PAYLOAD, shot_payload.toString());
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.URL, shot_url);
		service_intent.putExtra(Constants.METHOD_ID, 2);
		context.startService(service_intent);
	}

	/**
	 * Sends missed shot to server
	 * 
	 * @param shooter
	 *            - id of shooter
	 * @param blocker
	 *            - id of blocking player; null if none
	 * @param type
	 *            - type of shot; see Constants.SHOT_OPTIONS
	 * @param pts
	 *            - point value of shot
	 * @param fb
	 *            - fast break opportunity
	 * @param location
	 *            - location of shot on court; see
	 *            BallOverlay.get_court_location();
	 * @param con
	 *            - context document; see API_Calls.make_context();
	 */
	public void send_missed_shot(String shooter, String blocker, String type,
			int pts, boolean fb, JSONArray location, JSONObject con) {
		JSONObject shot_payload = new JSONObject();
		try {
			shot_payload.put("gameId", game_id);
			shot_payload.put("shooter", shooter);
			if (blocker != null)
				shot_payload.put("blockedBy", blocker);
			shot_payload.put("shotType", type);
			shot_payload.put("pointsAttempted", pts);
			shot_payload.put("fastBreakOpportunity", fb);
			shot_payload.put("location", location);
			shot_payload.put("context", con);
		} catch (Exception e) {
			Log.e(TAG,
					"Unable to create missed shot payload: " + e.getMessage());
		}

		String shot_url = Constants.MISSEDSHOT_URL + "?token=" + token
				+ "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.PAYLOAD, shot_payload.toString());
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.URL, shot_url);
		service_intent.putExtra(Constants.METHOD_ID, 2);
		context.startService(service_intent);
	}

	/**
	 * Sends turnover to server.
	 * 
	 * @param committer
	 *            - id of player who committed turnover
	 * @param forcer
	 *            - id of player who forced turnover; null if none
	 * @param type
	 *            - type of turnover; see Constants.TURNOVER_OPTIONS
	 * @param loc
	 *            - location of turnover; see BallOverlay.get_court_location()
	 * @param con
	 *            - context document; see API_Calls.make_context()
	 */
	public void send_turnover(String committer, String forcer, String type,
			JSONArray loc, JSONObject con) {
		JSONObject turnover_payload = new JSONObject();
		try {
			turnover_payload.put("gameId", game_id);
			turnover_payload.put("committedBy", committer);
			if (forcer != null)
				turnover_payload.put("forcedBy", forcer);
			turnover_payload.put("turnoverType", type);
			turnover_payload.put("location", loc);
			turnover_payload.put("context", con);
		} catch (Exception e) {
			Log.e(TAG, "Unable to create turnover_payload: " + e.getMessage());
		}

		String to_url = Constants.TURNOVER_URL + "?token=" + token
				+ "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.PAYLOAD, turnover_payload.toString());
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.URL, to_url);
		service_intent.putExtra(Constants.METHOD_ID, 2);
		context.startService(service_intent);
	}

	/**
	 * Sends foul document to server
	 * 
	 * @param committer
	 *            - id of player who committed foul
	 * @param drawer
	 *            - id of player who drew foul; null if none
	 * @param type
	 *            - type of foul; see Constants.FOUL_OPTIONS
	 * @param ejected
	 *            - whether player is ejected or not
	 * @param loc
	 *            - location of foul; see BallOverlay.get_court_location()
	 * @param con
	 *            - context document; see API_Calls.make_context()
	 */
	public void send_foul(String committer, String drawer, String type,
			boolean ejected, JSONArray loc, JSONObject con) {
		JSONObject foul_payload = new JSONObject();
		try {
			foul_payload.put("gameId", game_id);
			foul_payload.put("committedBy", committer);
			if (drawer != null)
				foul_payload.put("drewBy", drawer);
			foul_payload.put("foulType", type);
			foul_payload.put("ejected", ejected);
			foul_payload.put("location", loc);
			foul_payload.put("context", con);
		} catch (Exception e) {
			Log.e(TAG, "Unable to construct foul_payload: " + e.getMessage());
		}

		String foul_url = Constants.FOUL_URL + "?token=" + token
				+ "&signature=" + getSignature() + "&key="
				+ Constants.ACCESS_KEY;

		Intent service_intent = new Intent(context, HTTPRequest.class);
		service_intent.putExtra(Constants.PAYLOAD, foul_payload.toString());
		service_intent.putExtra(Constants.TYPE, 0);
		service_intent.putExtra(Constants.API_CALL, 2);
		service_intent.putExtra(Constants.URL, foul_url);
		service_intent.putExtra(Constants.METHOD_ID, 2);
		context.startService(service_intent);
	}

	/**
	 * Creates context document from given scores
	 * 
	 * @param homeScore
	 *            - score of home team
	 * @param awayScore
	 *            - score of away team
	 * @return properly formatted context document to send to server
	 */
	public JSONObject make_context(int homeScore, int awayScore) {
		try {
			JSONObject temp = new JSONObject();
			temp.put("time",
					Constants.df.format(Calendar.getInstance().getTime()));
			temp.put("homeScore", homeScore);
			temp.put("awayScore", awayScore);
			return temp;
		} catch (JSONException e) {
			Log.d(TAG, "Unable to create context: " + e.getMessage());
			return null;
		}

	}

	/**
	 * Generated MD5 hash of application access key, shared secret, and current
	 * unix time. Resulting 'signature' is required to generate token.
	 * 
	 * @return unique signature for application
	 */
	private String getSignature() {
		int time = (int) (System.currentTimeMillis() / 1000L);

		// Access key -> shared secret -> current unix time
		String concat = Constants.ACCESS_KEY + Constants.SHARED_SECRET + time;

		// Get md5 bytes
		if (md != null) {
			md.update(concat.getBytes());
		} else {
			return null;
		}
		byte[] result_bytes = md.digest();

		// Construct md5 string
		StringBuffer result_string = new StringBuffer();
		for (int i = 0; i < result_bytes.length; i++) {
			String hex_string = Integer.toHexString(0xFF & result_bytes[i]);
			// make sure 0's are not lost
			while (hex_string.length() < 2) {
				hex_string = "0" + hex_string;
			}
			result_string.append(hex_string);
		}
		return result_string.toString();
	}

}
