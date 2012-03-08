package org.bball.scoreit;

import org.json.JSONObject;

import android.app.Activity;
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
	protected void onDestroy() {
		// unregister receiver when in background
		unregisterReceiver(token_receiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// register receiver on startup
		IntentFilter filter = new IntentFilter(Constants.RESULTS);
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
			api_calls.login(un, pw);
			break;
		}
	}

	private class TokenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				JSONObject result_obj = new JSONObject(
						intent.getStringExtra("result"));
				JSONObject response_obj = new JSONObject(result_obj.get(
						"response").toString());
				String token = response_obj.get("token").toString();
				Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT)
				.show();
			} catch (Exception e) {
				Log.d(TAG, "Unable to extract token.");
				password.setText("");
				Toast.makeText(LoginActivity.this, "Unable to login",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}