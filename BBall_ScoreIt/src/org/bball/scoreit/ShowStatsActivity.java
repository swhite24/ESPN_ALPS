package org.bball.scoreit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ShowStatsActivity extends Activity {

	private static final String TAG = "BBALL_SCOREIT::SHOWSTATS";
	private TableLayout home_table, away_table;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "ShowStatsActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showstats);

		// Main control layout
		LinearLayout ll = (LinearLayout) findViewById(R.id.show_stats_ll);

		// create home team text
		TextView home_team = new TextView(this);
		home_team.setText(ScoreGameActivity.home_team.getName());
		home_team.setTextSize(15);
		home_team.setPadding(5, 0, 0, 5);
		home_team.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		// add home team text
		ll.addView(home_team, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		// create and fill home table
		home_table = new TableLayout(this);
		add_home_headers();
		fill_home_players();
		// add home table
		ll.addView(home_table, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		
		// create away team text
		TextView away_team = new TextView(this);
		away_team.setText(ScoreGameActivity.away_team.getName());
		away_team.setTextSize(15);
		away_team.setPadding(5, 15, 0, 5);
		away_team.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		// add home team text
		ll.addView(away_team, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		// create and fill away table
		away_table = new TableLayout(this);
		add_away_headers();
		fill_away_players();
		// add away table
		ll.addView(away_table, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

	}

	/**
	 * Fills home_table with header TableRow.  Background LTGRAY, 
	 * text DKGRAY.
	 * Columns are: NAME, FGM-A, REB, AST, BLK, TO, PF, PTS
	 */
	private void add_home_headers() {
		TableRow h_header = new TableRow(this);
		h_header.setBackgroundColor(Color.LTGRAY);

		TextView name = new TextView(this);
		name.setTextColor(Color.DKGRAY);
		name.setText("NAME");
		name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(name);

		TableRow.LayoutParams params = (TableRow.LayoutParams) name
				.getLayoutParams();
		params.column = 0;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		name.setGravity(Gravity.CENTER);
		name.setPadding(5, 3, 5, 3);
		name.setLayoutParams(params);

		TextView fg = new TextView(this);
		fg.setTextColor(Color.DKGRAY);
		fg.setText("FGM-A");
		fg.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(fg);

		params = (TableRow.LayoutParams) fg.getLayoutParams();
		params.column = 1;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		fg.setGravity(Gravity.CENTER);
		fg.setPadding(5, 3, 5, 3);
		fg.setLayoutParams(params);

		TextView reb = new TextView(this);
		reb.setTextColor(Color.DKGRAY);
		reb.setText("REB");
		reb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(reb);

		params = (TableRow.LayoutParams) reb.getLayoutParams();
		params.column = 2;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		reb.setGravity(Gravity.CENTER);
		reb.setPadding(5, 3, 5, 3);
		reb.setLayoutParams(params);

		TextView ast = new TextView(this);
		ast.setTextColor(Color.DKGRAY);
		ast.setText("AST");
		ast.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(ast);

		params = (TableRow.LayoutParams) ast.getLayoutParams();
		params.column = 3;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		ast.setGravity(Gravity.CENTER);
		ast.setPadding(5, 3, 5, 3);
		ast.setLayoutParams(params);

		TextView blk = new TextView(this);
		blk.setTextColor(Color.DKGRAY);
		blk.setText("BLK");
		blk.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(blk);

		params = (TableRow.LayoutParams) blk.getLayoutParams();
		params.column = 4;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		blk.setGravity(Gravity.CENTER);
		blk.setPadding(5, 3, 5, 3);
		blk.setLayoutParams(params);

		TextView to = new TextView(this);
		to.setTextColor(Color.DKGRAY);
		to.setText("TO");
		to.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(to);

		params = (TableRow.LayoutParams) to.getLayoutParams();
		params.column = 5;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		to.setGravity(Gravity.CENTER);
		to.setPadding(5, 3, 5, 3);
		to.setLayoutParams(params);

		TextView pf = new TextView(this);
		pf.setTextColor(Color.DKGRAY);
		pf.setText("PF");
		pf.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(pf);

		params = (TableRow.LayoutParams) pf.getLayoutParams();
		params.column = 6;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		pf.setGravity(Gravity.CENTER);
		pf.setPadding(5, 3, 5, 3);
		pf.setLayoutParams(params);

		TextView pts = new TextView(this);
		pts.setTextColor(Color.DKGRAY);
		pts.setText("PTS");
		pts.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(pts);

		params = (TableRow.LayoutParams) pts.getLayoutParams();
		params.column = 7;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		pts.setGravity(Gravity.CENTER);
		pts.setPadding(5, 3, 5, 3);
		pts.setLayoutParams(params);

		home_table.addView(h_header, new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	/**
	 * Fills away_table with header TableRow.  Background LTGRAY, 
	 * text DKGRAY.
	 * Columns are: NAME, FGM-A, REB, AST, BLK, TO, PF, PTS
	 */
	private void add_away_headers() {
		TableRow h_header = new TableRow(this);
		h_header.setBackgroundColor(Color.LTGRAY);

		TextView name = new TextView(this);
		name.setTextColor(Color.DKGRAY);
		name.setText("NAME");
		name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(name);

		TableRow.LayoutParams params = (TableRow.LayoutParams) name
				.getLayoutParams();
		params.column = 0;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		name.setGravity(Gravity.CENTER);
		name.setPadding(5, 3, 5, 3);
		name.setLayoutParams(params);

		TextView fg = new TextView(this);
		fg.setTextColor(Color.DKGRAY);
		fg.setText("FGM-A");
		fg.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(fg);

		params = (TableRow.LayoutParams) fg.getLayoutParams();
		params.column = 1;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		fg.setGravity(Gravity.CENTER);
		fg.setPadding(5, 3, 5, 3);
		fg.setLayoutParams(params);

		TextView reb = new TextView(this);
		reb.setTextColor(Color.DKGRAY);
		reb.setText("REB");
		reb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(reb);

		params = (TableRow.LayoutParams) reb.getLayoutParams();
		params.column = 2;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		reb.setGravity(Gravity.CENTER);
		reb.setPadding(5, 3, 5, 3);
		reb.setLayoutParams(params);

		TextView ast = new TextView(this);
		ast.setTextColor(Color.DKGRAY);
		ast.setText("AST");
		ast.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(ast);

		params = (TableRow.LayoutParams) ast.getLayoutParams();
		params.column = 3;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		ast.setGravity(Gravity.CENTER);
		ast.setPadding(5, 3, 5, 3);
		ast.setLayoutParams(params);

		TextView blk = new TextView(this);
		blk.setTextColor(Color.DKGRAY);
		blk.setText("BLK");
		blk.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(blk);

		params = (TableRow.LayoutParams) blk.getLayoutParams();
		params.column = 4;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		blk.setGravity(Gravity.CENTER);
		blk.setPadding(5, 3, 5, 3);
		blk.setLayoutParams(params);

		TextView to = new TextView(this);
		to.setTextColor(Color.DKGRAY);
		to.setText("TO");
		to.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(to);

		params = (TableRow.LayoutParams) to.getLayoutParams();
		params.column = 5;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		to.setGravity(Gravity.CENTER);
		to.setPadding(5, 3, 5, 3);
		to.setLayoutParams(params);

		TextView pf = new TextView(this);
		pf.setTextColor(Color.DKGRAY);
		pf.setText("PF");
		pf.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(pf);

		params = (TableRow.LayoutParams) pf.getLayoutParams();
		params.column = 6;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		pf.setGravity(Gravity.CENTER);
		pf.setPadding(5, 3, 5, 3);
		pf.setLayoutParams(params);

		TextView pts = new TextView(this);
		pts.setTextColor(Color.DKGRAY);
		pts.setText("PTS");
		pts.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		h_header.addView(pts);

		params = (TableRow.LayoutParams) pts.getLayoutParams();
		params.column = 7;
		params.width = TableRow.LayoutParams.FILL_PARENT;
		pts.setGravity(Gravity.CENTER);
		pts.setPadding(5, 3, 5, 3);
		pts.setLayoutParams(params);

		away_table.addView(h_header, new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	/**
	 * Fills home_table with data for each player on home team.
	 * Consideration is not given for starter or non-starter.
	 */
	private void fill_home_players() {
		Team home = ScoreGameActivity.home_team;
		for (int i = 1; i < home.getPlayers().size(); i++) {
			Player current = home.get_player(i);
			TableRow tr = new TableRow(this);

			TextView name = new TextView(this);
			name.setText(current.getLast_name());
			tr.addView(name);
			TableRow.LayoutParams params = (TableRow.LayoutParams) name
					.getLayoutParams();
			params.column = 0;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			name.setPadding(5, 3, 5, 3);
			name.setGravity(Gravity.CENTER);
			name.setLayoutParams(params);
			
			TextView fgm = new TextView(this);
			fgm.setText("" + current.getFGM() + "-" + current.getFGA());
			tr.addView(fgm);
			params = (TableRow.LayoutParams) fgm.getLayoutParams();
			params.column = 1;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			fgm.setPadding(5, 3, 5, 3);
			fgm.setGravity(Gravity.CENTER);
			fgm.setLayoutParams(params);

			TextView reb = new TextView(this);
			reb.setText("" + current.getTotRbs());
			tr.addView(reb);
			params = (TableRow.LayoutParams) reb.getLayoutParams();
			params.column = 2;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			reb.setPadding(5, 3, 5, 3);
			reb.setGravity(Gravity.CENTER);
			reb.setLayoutParams(params);

			TextView ast = new TextView(this);
			ast.setText("" + current.getAssists());
			tr.addView(ast);
			params = (TableRow.LayoutParams) ast.getLayoutParams();
			params.column = 3;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			ast.setPadding(5, 3, 5, 3);
			ast.setGravity(Gravity.CENTER);
			ast.setLayoutParams(params);

			TextView blk = new TextView(this);
			blk.setText("" + current.getBlocks());
			tr.addView(blk);
			params = (TableRow.LayoutParams) blk.getLayoutParams();
			params.column = 4;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			blk.setPadding(5, 3, 5, 3);
			blk.setGravity(Gravity.CENTER);
			blk.setLayoutParams(params);

			TextView to = new TextView(this);
			to.setText("" + current.getTurnovers());
			tr.addView(to);
			params = (TableRow.LayoutParams) to.getLayoutParams();
			params.column = 5;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			to.setPadding(5, 3, 5, 3);
			to.setGravity(Gravity.CENTER);
			to.setLayoutParams(params);

			TextView pf = new TextView(this);
			pf.setText("" + current.getFouls());
			tr.addView(pf);
			params = (TableRow.LayoutParams) pf.getLayoutParams();
			params.column = 6;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			pf.setPadding(5, 3, 5, 3);
			pf.setGravity(Gravity.CENTER);
			pf.setLayoutParams(params);

			TextView pts = new TextView(this);
			pts.setText("" + current.getPts());
			tr.addView(pts);
			params = (TableRow.LayoutParams) pts.getLayoutParams();
			params.column = 7;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			pts.setGravity(Gravity.CENTER);
			pts.setPadding(5, 3, 5, 3);
			pts.setLayoutParams(params);

			home_table.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}
	
	/**
	 * Fills away_table with data for each player on home team.
	 * Consideration is not given for starter or non-starter.
	 */
	private void fill_away_players() {
		Team away = ScoreGameActivity.away_team;
		for (int i = 1; i < away.getPlayers().size(); i++) {
			Player current = away.get_player(i);
			TableRow tr = new TableRow(this);

			TextView name = new TextView(this);
			name.setText(current.getLast_name());
			tr.addView(name);
			TableRow.LayoutParams params = (TableRow.LayoutParams) name
					.getLayoutParams();
			params.column = 0;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			name.setPadding(5, 3, 5, 3);
			name.setGravity(Gravity.CENTER);
			name.setLayoutParams(params);
			
			TextView fgm = new TextView(this);
			fgm.setText("" + current.getFGM() + "-" + current.getFGA());
			tr.addView(fgm);
			params = (TableRow.LayoutParams) fgm.getLayoutParams();
			params.column = 1;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			fgm.setPadding(5, 3, 5, 3);
			fgm.setGravity(Gravity.CENTER);
			fgm.setLayoutParams(params);

			TextView reb = new TextView(this);
			reb.setText("" + current.getTotRbs());
			tr.addView(reb);
			params = (TableRow.LayoutParams) reb.getLayoutParams();
			params.column = 2;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			reb.setPadding(5, 3, 5, 3);
			reb.setGravity(Gravity.CENTER);
			reb.setLayoutParams(params);

			TextView ast = new TextView(this);
			ast.setText("" + current.getAssists());
			tr.addView(ast);
			params = (TableRow.LayoutParams) ast.getLayoutParams();
			params.column = 3;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			ast.setPadding(5, 3, 5, 3);
			ast.setGravity(Gravity.CENTER);
			ast.setLayoutParams(params);

			TextView blk = new TextView(this);
			blk.setText("" + current.getBlocks());
			tr.addView(blk);
			params = (TableRow.LayoutParams) blk.getLayoutParams();
			params.column = 4;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			blk.setPadding(5, 3, 5, 3);
			blk.setGravity(Gravity.CENTER);
			blk.setLayoutParams(params);

			TextView to = new TextView(this);
			to.setText("" + current.getTurnovers());
			tr.addView(to);
			params = (TableRow.LayoutParams) to.getLayoutParams();
			params.column = 5;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			to.setPadding(5, 3, 5, 3);
			to.setGravity(Gravity.CENTER);
			to.setLayoutParams(params);

			TextView pf = new TextView(this);
			pf.setText("" + current.getFouls());
			tr.addView(pf);
			params = (TableRow.LayoutParams) pf.getLayoutParams();
			params.column = 6;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			pf.setPadding(5, 3, 5, 3);
			pf.setGravity(Gravity.CENTER);
			pf.setLayoutParams(params);

			TextView pts = new TextView(this);
			pts.setText("" + current.getPts());
			tr.addView(pts);
			params = (TableRow.LayoutParams) pts.getLayoutParams();
			params.column = 7;
			params.width = TableRow.LayoutParams.FILL_PARENT;
			pts.setGravity(Gravity.CENTER);
			pts.setPadding(5, 3, 5, 3);
			pts.setLayoutParams(params);

			away_table.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

}
