package org.bball.scoreit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

public class HTTPRequest extends Service {
	private Get_Response get_response;
	private String URL;
	private String payload;

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
			payload = intent.getStringExtra(Constants.PAYLOAD);
			get_response = new Get_Response();
			get_response.execute((Void[]) null);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void announce_results(String result) {
		// on completion, announce results to calling activity
		Intent intent = new Intent(Constants.RESULTS);
		intent.putExtra("result", result);
		sendBroadcast(intent);
		stopSelf();
	}

	private class Get_Response extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
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
					System.out.println("Failed to build StringEntity.");
				}
			}

			// Retrieve response
			try {
				post.setEntity(se);
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
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response_inStream));
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

		@Override
		protected void onPostExecute(String result) {
			// announce results
			announce_results(result);
		}

	}
}
