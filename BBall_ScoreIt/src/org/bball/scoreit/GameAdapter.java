package org.bball.scoreit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adapter class to populate rows of listview of game data
 * @author Steve
 *
 */
public class GameAdapter extends ArrayAdapter<String> {

	private int layout_resource;
	private static final String TAG = "BBALL_SCOREIT::GAMEADAPTER";

	public GameAdapter(Context context, int textViewResourceId,
			List<String> objects) {
		super(context, textViewResourceId, objects);
		layout_resource = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout rowView;


		// initialize rowView
		if (convertView == null) {
			rowView = new LinearLayout(getContext());
			String inflate = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li = (LayoutInflater) getContext().getSystemService(
					inflate);
			li.inflate(layout_resource, rowView, true);
		} else {
			rowView = (LinearLayout) convertView;
		}
		// Views in each row
		TextView sep = (TextView) rowView.findViewById(R.id.game_item_separator);
		TextView teams = (TextView) rowView.findViewById(R.id.game_item_vs_tv);
		TextView time = (TextView) rowView.findViewById(R.id.game_item_time_tv);
		TextView location = (TextView) rowView
				.findViewById(R.id.game_item_location_tv);
		
		if (position == 0)
			sep.setText("Available Games");
		else
			sep.setVisibility(View.GONE);

		try {
			// get initial data
			JSONObject game_obj = new JSONObject(getItem(position));
			JSONObject home_team = game_obj.getJSONObject("homeTeam");
			JSONObject away_team = game_obj.getJSONObject("awayTeam");
			teams.setText(away_team.get("teamName").toString() + " at "
					+ home_team.get("teamName").toString());
			
			// Format time
			SimpleDateFormat view_df = new SimpleDateFormat("MM/dd HH:mm");
			view_df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date _time = Constants.df.parse(game_obj.get("time").toString());
			time.setText(view_df.format(_time));
			
			location.setText(game_obj.get("venue").toString());
		} catch (Exception e) {
			Log.e(TAG, "Couldn't fill row info");
		}

		return rowView;
	}

}
