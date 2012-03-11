package org.bball.scoreit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class HTTPRequest extends Service {
	private Get_Response get_response;
	private String URL;
	private String payload;
	private int api_call, type, method_id;
	private static final String TAG = "BBALL_SCOREIT::HTTPREQUEST";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// if ASyncTask not running
		if (get_response == null
				|| get_response.getStatus().equals(AsyncTask.Status.FINISHED)) {

			// Extract URL and Payload, then execute
			URL = intent.getStringExtra(Constants.URL);
			api_call = intent.getIntExtra(Constants.API_CALL, -1);
			type = intent.getIntExtra(Constants.TYPE, -1);
			method_id = intent.getIntExtra(Constants.METHOD_ID, -1);
			if (type == 0) {
				payload = intent.getStringExtra(Constants.PAYLOAD);
			}
			get_response = new Get_Response();
			get_response.execute((Void[]) null);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	// on completion, announce results to calling activity
	private void announce_results(String result) {
		Log.d(TAG, "Broadcasting result to: " + API_Calls.api_map.get(api_call));
		Intent intent = new Intent((String) API_Calls.api_map.get(api_call));
		intent.putExtra("result", result);
		intent.putExtra(Constants.METHOD_ID, method_id);
		sendBroadcast(intent);
		stopSelf();
	}

	/**
	 * ASyncTask which performs the API call specified by URL string extra. If
	 * the call requires a JSON string payload, it must be formatted prior to
	 * being executed. On completion, results are broadcast to specific
	 * activity.
	 * 
	 * @author Steve
	 * 
	 */
	private class Get_Response extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			switch (type) {
			case 0:
				return http_post();
			case 1:
				return http_get();
			default:
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// announce results
			announce_results(result);
		}
		/**
		 * Executes an HttpPost with given URL and payload
		 * @return - String containing response from server
		 */
		private String http_post() {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(URL);
			InputStream response_inStream = null;

			// setup StringEntity containing JSONObject data
			StringEntity se = null;
			if (payload != null) {
				try {
					se = new StringEntity(payload);
					se.setContentType(new BasicHeader("Content-Type",
							"application/json"));
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "Failed to build StringEntity.");
				}
			}

			// Retrieve response
			try {
				post.setEntity(se);
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				response_inStream = entity.getContent();
			} catch (Exception e) {
				Log.e(TAG, "Failed to retrieve response.");
				return null;
			}

			// Read response
			StringBuilder response_builder = new StringBuilder();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response_inStream));
				String response_line = null;
				while ((response_line = reader.readLine()) != null) {
					response_builder.append(response_line + "\n");
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to read response.");
				return null;
			}
			Log.d(TAG, "Response length: " + response_builder.toString());
			return response_builder.toString();
		}
		/**
		 * Executes an HttpGet at given URL
		 * @return - String containing response from server
		 */
		private String http_get() {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(URL);
			InputStream response_inStream = null;

			// Retrieve response
			try {
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				response_inStream = entity.getContent();
			} catch (Exception e) {
				Log.e(TAG, "Failed to retrieve response.");
				return null;
			}

			// Read response
			StringBuilder response_builder = new StringBuilder();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response_inStream));
				String response_line = null;
				while ((response_line = reader.readLine()) != null) {
					response_builder.append(response_line + "\n");
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to read response.");
				return null;
			}
			Log.d(TAG, "Response length: " + response_builder.toString());
			return response_builder.toString();
			
		}

	}
}
