package org.bball.scoreit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

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
	public static void setToken(String _token){
		token = _token;
	}
	
	// map of api_calls used to correctly initialize broadcast receivers
	public static HashMap<Integer, String> api_map = init_map();	
	private static HashMap<Integer, String> init_map(){
		HashMap<Integer, String> temp = new HashMap<Integer, String>();
		temp.put(0, "LOGIN");
		temp.put(1, "GETGAMES");		
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
	 * Logs into system, starting service to retrieve valid token. Token viable for 
	 * 5 minutes and is used in all subsequent API calls.
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
	 * @param token
	 *            - token generated from login method
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
