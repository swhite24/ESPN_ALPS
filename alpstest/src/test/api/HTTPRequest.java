package test.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generic HTTP Request class.
 * 
 * @author Steve
 * 
 */
public class HTTPRequest {
	// base url

	private MessageDigest md = null;
	private String sig = null;

	public HTTPRequest() {
		sig = getSignature();
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public String login(String username, String password) {
		// construct JSONObject for login payload
		JSONObject un_pw = new JSONObject();
		try {
			un_pw.put("username", Constants.USERNAME);
			un_pw.put("password", Constants.PASSWORD);
		} catch (Exception e) {
			System.out.println("Failed to populate JSONObject.");
		}

		// construct url to login
		String loginURL = Constants.LOGIN + "?signature=" + getSignature()
				+ "&key=" + Constants.ACCESS_KEY;

		// get token
		JSONObject login_response = null;
		String token = null;
		try {
			login_response = new JSONObject(http_post(loginURL, un_pw));

			token = new JSONObject(login_response.get("response").toString())
					.get("token").toString();
		} catch (JSONException e) {
			System.out.println("Malformed login response.");
		}

		return token;
	}

	public String get_games(String token, String start, String end) {
		// construct JSONObject for games payload
		JSONObject start_end = new JSONObject();
		try {
			start_end.put("start", start);
			start_end.put("end", end);
		} catch (JSONException e) {
			System.out.println("Failed to populate JSONObject.");
			return null;
		}

		// construct games URL
		String gamesURL = Constants.GAMES + "?token=" + token + "&signature="
				+ getSignature() + "&key=" + Constants.ACCESS_KEY;

		// Read response
		JSONObject games_response_obj = null;
		String games_response = null;
		try {
			games_response_obj = new JSONObject(http_post(gamesURL, start_end));
			games_response = games_response_obj.get("response").toString();
		} catch (JSONException e) {
			System.out.println("Failed to extract response.");
			return null;
		}

		return games_response;
	}

	public String get_game_data(String token, String gameId) {
		 String dataURL = Constants.GAME_DATA + "/" + gameId + "?token=" +
		 token
		 + "&signature=" + getSignature() + "&key="
		 + Constants.ACCESS_KEY;
	//	String dataURL = "http://api.espnalps.com/v0/cbb/getGameData/4f46c113e4b079ad546850ae?token=281cd2a897ef62da175a3cb59597f800&signature=bd3442195eb3b763b2496c3943d93b09&key=96a55a98a49c7f4f795eee184d20414eb8a95178";
		System.out.println(dataURL);
		JSONObject response_obj = null;
		String response = null;

		try {
			response_obj = new JSONObject(http_get(dataURL, null));
			response = response_obj.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}
	
	private String http_get(String URL, JSONObject obj){
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);
		InputStream response_inStream = null;


		// Retrieve response
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			response_inStream = entity.getContent();
		} catch (Exception e) {
			System.out.println("Failed to retrieve response.");
			return null;
		}

		// Read response
		StringBuilder response_builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response_inStream));
			String response_line = null;
			while ((response_line = reader.readLine()) != null) {
				response_builder.append(response_line + "\n");
			}
		} catch (Exception e) {
			System.out.println("Failed to read response.");
			return null;
		}
		return response_builder.toString();
	}



	private String http_post(String URL, JSONObject obj) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(URL);
		InputStream response_inStream = null;

		// setup StringEntity containing JSONObject data
		StringEntity se = null;
		if (obj != null) {
			try {
				se = new StringEntity(obj.toString());
				se.setContentType(new BasicHeader("Content-Type",
						"application/json"));
				post.setEntity(se);
			} catch (UnsupportedEncodingException e) {
				System.out.println("Failed to build StringEntity.");
			}
		}

		// Retrieve response
		try {
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			response_inStream = entity.getContent();
		} catch (Exception e) {
			System.out.println("Failed to retrieve response.");
			return null;
		}

		// Read response
		StringBuilder response_builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response_inStream));
			String response_line = null;
			while ((response_line = reader.readLine()) != null) {
				response_builder.append(response_line + "\n");
			}
		} catch (Exception e) {
			System.out.println("Failed to read response.");
			return null;
		}
		return response_builder.toString();
	}

	// Generate md5 hash of access key + secret + current time
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
