package test.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

public class HTTPRequest {
	// base url
	private static final String BASE_URL = "https://api.espnalps.com/prod/login";

	// info on bball_scoreit
	private static final String ACCESS_KEY = "96a55a98a49c7f4f795eee184d20414eb8a95178";
	private static final String SHARED_SECRET = "oR72b4paDj3AJmZGMEqswvQMk61FYbsOZSHEICn0";

	private MessageDigest md = null;

	public HTTPRequest() {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	// Generate md5 hash of access key + secret + current time
	private String getSignature() {
		int time = (int) (System.currentTimeMillis() / 1000L);

		String concat = ACCESS_KEY + SHARED_SECRET + time;

		byte[] concat_bytes = concat.getBytes();

		if (md != null) {
			md.update(concat_bytes);
		} else {
			return null;
		}
		BigInteger hash_int = new BigInteger(1, md.digest());
		String result = String.format("%1$032X", hash_int);
		System.out.println(result + "\n" + result.length());
		return result;
	}

	public void login(String userName, String password) {
		JSONObject data = new JSONObject();
		try {
			data.put("username", userName);
			data.put("password", password);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String data_string = data.toString();
		System.out.println(data_string);
		// InputStream to get response

		// Complete login URL
		String loginURL = BASE_URL + "?signature=" + getSignature() + "&key="
				+ ACCESS_KEY;
		System.out.println(loginURL);
		
		
		// Retrieve response
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(loginURL);
		InputStream response_inStream = null;
		try {
			StringEntity se = new StringEntity(data_string);
			se.setContentType(new BasicHeader("Content-Type",
					"application/json"));
			httppost.setEntity(se);
//			httppost.setHeader("Content-Type", "application/json");

			HttpResponse httpresponse = httpclient.execute(httppost);
			HttpEntity entity = httpresponse.getEntity();
			response_inStream = entity.getContent();
		} catch (Exception e) {
			System.out.println("Error retrieving result.");
		}
		
		// Read response
		StringBuilder response_builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response_inStream));
			String response_line = null;
			while ((response_line = reader.readLine()) != null) {
				response_builder.append(response_line);
			}
		} catch (Exception e) {
			System.out.println("Error reading result.");
		}

		System.out.println(response_builder.toString());
	}

}
