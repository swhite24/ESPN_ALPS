package org.bball.scoreit;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BallOverlay extends View {

	private static final String TAG = "BBALL_SCOREIT::BALLOVERLAY";
	private Bitmap ball;
	float x, y;
	int width = -1, height = -1;

	public BallOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
		x = getWidth() / 2;
		y = getHeight() / 2;
		ball = BitmapFactory.decodeResource(getResources(),
				R.drawable.basketball);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Rect src = new Rect(0, 0, ball.getWidth(), ball.getHeight());
		Rect dst = new Rect((int) x - ball.getWidth() / 2, (int) y
				- ball.getHeight() / 2, (int) x + ball.getWidth() / 2, (int) y
				+ ball.getHeight() / 2);
		canvas.drawBitmap(ball, src, dst, null);

		Log.d(TAG, "iv width/height: " + width + "/" + height);
		Log.d(TAG, "x/y: " + x + "/" + y);
		super.onDraw(canvas);
	}

	public void setX(float x) {
		this.x = Math.max(0, x);
	}

	public void setY(float y) {
		this.y = Math.max(0, y);
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public int get_width(){
		return width;
	}

	public JSONArray get_court_location() {
		float x_ratio = x / width;
		float y_ratio = y / height;
		JSONArray location = new JSONArray();
		location.put((int)(x_ratio * 940));
		location.put((int)(y_ratio * 500));
		return location;
	}
}
