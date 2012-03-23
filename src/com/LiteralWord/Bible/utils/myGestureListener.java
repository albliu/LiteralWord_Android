package com.LiteralWord.Bible.utils;

import android.content.Context;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.LiteralWord.Bible.LiteralWord;

public class myGestureListener implements OnDoubleTapListener,
		OnGestureListener {

	private static final String TAG = "myGestureListener";

	public GestureDetector myDetector;
	public GestureState myState = GestureState.NONE;

	public enum GestureState {
		NONE, DOWN, DOUBLE_TAP, LONG_PRESS, COPY
	}

	public myGestureListener(Context c) {
		myDetector = new GestureDetector(c, this);
		myDetector.setOnDoubleTapListener(this);
	}

	public boolean onDoubleTap(MotionEvent arg0) {
		// do nothing
		return false;
	}

	public boolean onDoubleTapEvent(MotionEvent arg0) {

		// Log.d(LiteralWord.TAG, "BibleReader - double tap event!");
		if (myState == GestureState.COPY)
			return false;
		myState = GestureState.DOUBLE_TAP;
		return false;
	}

	public boolean onSingleTapConfirmed(MotionEvent arg0) {

		Log.d(LiteralWord.TAG, "BibleReader - single tap.. do?");
		// myState = GestureState.NONE;
		return false;
	}

	public boolean onDown(MotionEvent arg0) {
		// clear state
		// Log.d(LiteralWord.TAG, "BibleReader - onDown!");
		if (myState == GestureState.COPY)
			return false;
		myState = GestureState.DOWN;
		return false;
	}

	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// do nothing
		return false;
	}

	public void onLongPress(MotionEvent arg0) {
		// Log.d(LiteralWord.TAG, "BibleReader - onLongPress!");
		if (myState == GestureState.COPY)
			return;
		if (myState != GestureState.DOUBLE_TAP)
			myState = GestureState.LONG_PRESS;

	}

	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// do nothing
		return false;
	}

	public void onShowPress(MotionEvent arg0) {
		// do nothing for now

	}

	public boolean onSingleTapUp(MotionEvent arg0) {
		// do nothing for now
		return false;
	}

	public enum myMotions {

		NONE, SWIPE_LEFT, SWIPE_RIGHT, ZOOM, LONG_T, SINGLE_T, DOUBLE_T, COPYING;

		static final float swipe_max_height = 80;
		static final float swipe_min_width = 120;

		static private PointF start;
		static private PointF end;
		static private float distance;
		static myMotions mode;
		static public float scale;

		public static myMotions getMotion(MotionEvent event, GestureState g) {
			// dumpEvent(event);

			switch (event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				if (g == GestureState.COPY)
					return COPYING;

				if (mode == NONE)
					mode = SINGLE_T;
				start = new PointF(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if (g == GestureState.COPY)
					return COPYING;
				distance = spacing(event);
				if (distance > 5f)
					mode = ZOOM;
				break;

			case MotionEvent.ACTION_MOVE:
				if (g == GestureState.COPY)
					return COPYING;
				// check zoom first
				if (mode == ZOOM) {
					// Log.d(TAG, "myMotions --- ZOOMING!");
					// Log.d(LiteralWord.TAG, TAG + " -- " +
					// Float.toString(scale));
					final float oldDist = distance;
					distance = spacing(event);
					scale = distance / oldDist;

					mode = ZOOM;
					return ZOOM;

				}

				break;

			case MotionEvent.ACTION_POINTER_UP:
				if (g == GestureState.COPY)
					return COPYING;
				if (mode != ZOOM) {
					Log.d(TAG, "myMotions --- SOMETHING'S WRONG!");
					mode = NONE;
					return NONE;
				}

				int keep = 1 - event.getActionIndex();
				start = new PointF(event.getX(keep), event.getY(keep));
				Log.d(TAG, "myMotions --- keeping finger: " + event.getX(keep));
				mode = SINGLE_T;

				return ZOOM;

			case MotionEvent.ACTION_UP:

				myMotions ret = NONE;
				if (g == GestureState.COPY) {
					mode = NONE;

					return NONE;

				} else if (g == GestureState.LONG_PRESS) {
					ret = LONG_T;
				} else if (g == GestureState.DOUBLE_TAP) {
					ret = DOUBLE_T;
				} else {
					end = new PointF(event.getX(), event.getY());
					if (Math.abs(start.y - end.y) < swipe_max_height) {
						float dist = start.x - end.x;
						if (Math.abs(dist) > swipe_min_width) {
							if (dist > 0) {
								// Log.d(TAG, "myMotions --- SWIPE_LEFT!");
								ret = SWIPE_LEFT;
							} else {
								// Log.d(TAG, "myMotions --- SWIPE_RIGHT!");
								ret = SWIPE_RIGHT;
							}
						}
					} else {
						ret = SINGLE_T;
					}
				}
				mode = NONE;
				return ret;
			}

			// if nothing happens in the switch statement
			return NONE;

		};

		static private float spacing(MotionEvent event) {
			final float x = event.getX(0) - event.getX(1);
			final float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		/** Show an event in the LogCat view, for debugging */
		static void dumpEvent(MotionEvent event) {
			String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
					"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
			StringBuilder sb = new StringBuilder();
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;
			sb.append("event ACTION_").append(names[actionCode]);
			if (actionCode == MotionEvent.ACTION_POINTER_DOWN
					|| actionCode == MotionEvent.ACTION_POINTER_UP) {
				sb.append("(pid ").append(
						action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				sb.append(")");
			}
			sb.append("[");
			for (int i = 0; i < event.getPointerCount(); i++) {
				sb.append("#").append(i);
				sb.append("(pid ").append(event.getPointerId(i));
				sb.append(")=").append((int) event.getX(i));
				sb.append(",").append((int) event.getY(i));
				if (i + 1 < event.getPointerCount())
					sb.append(";");
			}
			sb.append("]");
			Log.d(LiteralWord.TAG, TAG + " -- " + sb.toString());
		}
	};

};