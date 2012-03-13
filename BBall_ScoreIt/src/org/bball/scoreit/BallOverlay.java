package org.bball.scoreit;

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

	public BallOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
		x = getWidth() / 2;
		y = getHeight() / 2;
		Log.d(TAG, "width/height: " + getWidth() + "/" + getHeight());
		ball = BitmapFactory.decodeResource(getResources(),
				R.drawable.basketball);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Rect src = new Rect(0, 0, ball.getWidth(), ball.getHeight());
		Rect dst = new Rect((int) x, (int) y, (int) x + ball.getWidth(),
				(int) y + ball.getHeight());
		canvas.drawBitmap(ball, src, dst, null);
		
		Log.d(TAG,
				"canvas width/height: " + canvas.getWidth() + "/"
						+ canvas.getHeight());
		Log.d(TAG, "x/y: " + x + "/" + y);
		super.onDraw(canvas);
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

}
