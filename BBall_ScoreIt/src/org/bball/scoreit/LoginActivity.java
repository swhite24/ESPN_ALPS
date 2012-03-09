package org.bball.scoreit;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private EditText username;
	private EditText password;
	private API_Calls api_calls;
	private TokenReceiver token_receiver;
	private ProgressDialog login_dialog;
	static final int LOGIN = 0;
	private static final String TAG = "BBALL_SCOREIT::LOGINACTIVITY";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		api_calls = new API_Calls(this);

		Button submit_btn = (Button) findViewById(R.id.login_submit_btn);
		username = (EditText) findViewById(R.id.login_username_et);
		password = (EditText) findViewById(R.id.login_password_et);
		submit_btn.setOnClickListener(this);
	}

	@Override
	protected void onPause() {
		// unregister receiver when in background
		unregisterReceiver(token_receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// register receiver on startup
		IntentFilter filter = new IntentFilter(
				(String) API_Calls.api_map.get(0));
		token_receiver = new TokenReceiver();
		registerReceiver(token_receiver, filter);
		super.onResume();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		// call login with desired user-name and password
		case R.id.login_submit_btn:
			String pw = password.getText().toString();
			String un = username.getText().toString();
			showDialog(LOGIN);
			// api_calls.login(Constants.USERNAME, Constants.PASSWORD);
			api_calls.login(un, pw);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOGIN:
			login_dialog = new ProgressDialog(this,
					ProgressDialog.STYLE_SPINNER);
			login_dialog.setMessage("Logging in....");
			login_dialog.setCancelable(false);
			return login_dialog;
		default:
			return null;
		}
	}

	/**
	 * Initiates ShowGames activity with generated token.
	 * 
	 */
	private void show_games() {
		Intent i = new Intent(this, ShowGamesActivity.class);
		startActivity(i);
	}

	/**
	 * BroadcastReceiver to receive update from HTTPRequest on login status.
	 * Sends generated token to ShowGames on success.
	 * 
	 * @author Steve
	 * 
	 */
	private class TokenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// dismiss logging in dialog
			dismissDialog(LOGIN);
			String token = null;
			try {
				JSONObject result_obj = new JSONObject(
						intent.getStringExtra("result"));
				JSONObject response_obj = new JSONObject(result_obj.get(
						"response").toString());
				token = response_obj.get("token").toString();
				// update token in API_Calls and show list of games
				API_Calls.setToken(token);
				show_games();
			} catch (Exception e) {
				Log.d(TAG, "Unable to extract token.");
				password.setText("");
				Toast.makeText(LoginActivity.this, "Invalid username or password",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

}