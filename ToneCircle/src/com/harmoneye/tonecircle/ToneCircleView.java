package com.harmoneye.tonecircle;

import java.util.BitSet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ToneCircleView extends View {

	private static final int TONE_COUNT = 12;

	private static final double TWO_PI = 2 * Math.PI;
	private static final double HALF_PI = 0.5 * Math.PI;

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };

	// the model
	BitSet activeTones = new BitSet(TONE_COUNT);

	// x, y coordinates of bead centers
	float[] centers = new float[TONE_COUNT * 2];

	private Paint textPaint;
	private Paint beadPaint;
	private Paint activeBeadPaint;

	private Rect textRect = new Rect();

	private int width;
	private int height;
	private float beadRadius;
	private float bigRadius;

	private Integer touchedTone;

	// private double touchedAngle;

	public ToneCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		//textPaint.setStyle(Style.STROKE);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setAntiAlias(true);

		beadPaint = new Paint();
		beadPaint.setAntiAlias(true);

		float[] hsvColor = new float[] { 360 * 0.25f, 0.25f, 0.85f };
		int color = Color.HSVToColor(hsvColor);
		beadPaint.setColor(color);

		activeBeadPaint = new Paint(beadPaint);
		hsvColor[2] = 0.6f;
		int activeColor = Color.HSVToColor(hsvColor);
		activeBeadPaint.setColor(activeColor);
	}

	public BitSet getActiveTones() {
		return activeTones;
	}

	public void setActiveTones(BitSet activeTones) {
		this.activeTones = activeTones;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		canvas.drawColor(Color.WHITE);

		canvas.save();

		canvas.translate(0.5f * width, 0.5f * height);

		textPaint.setTextSize(beadRadius);

		for (int i = 0; i < TONE_COUNT; i++) {
			float x = centers[2 * i];
			float y = centers[2 * i + 1];
			canvas.drawCircle(x,
				y,
				beadRadius,
				activeTones.get(i) ? activeBeadPaint : beadPaint);

			String toneName = TONE_NAMES[i];
			textPaint.getTextBounds(toneName, 0, toneName.length(), textRect);
			canvas.drawText(toneName,
				x,
				y + textRect.height() * 0.5f,
				textPaint);
		}

		// canvas.drawLine(0,
		// 0,
		// (float) (bigRadius * Math.cos(touchedAngle)),
		// (float) (bigRadius * Math.sin(touchedAngle)),
		// textPaint);
		//
		// String toneName = String.valueOf(touchedTone);
		// textPaint.getTextBounds(toneName, 0, toneName.length(), textRect);
		// canvas.drawText(toneName, 0, textRect.height() * 0.5f, textPaint);

		canvas.restore();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// float xpad = (float) (getPaddingLeft() + getPaddingRight());
		// float ypad = (float) (getPaddingTop() + getPaddingBottom());

		width = w;
		height = h;

		float windowRadius = 0.5f * (float) Math.min(w, h);
		beadRadius = 0.19f * windowRadius;
		bigRadius = 0.8f * windowRadius;

		float toneCountInv = 1.0f / TONE_COUNT;
		for (int i = 0; i < TONE_COUNT; i++) {
			float p = i * toneCountInv;

			double angle = p * TWO_PI - HALF_PI;
			float x = bigRadius * (float) Math.cos(angle);
			float y = bigRadius * (float) Math.sin(angle);
			centers[2 * i] = x;
			centers[2 * i + 1] = y;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pointerIndex = event.getActionIndex();
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			touchedTone = getHoveredTone(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if (touchedTone != null) {
				Integer hoveredTone = getHoveredTone(x, y);
				if (hoveredTone == null) {
					activeTones.clear(touchedTone);
				} else if (hoveredTone.equals(touchedTone)) {
					activeTones.flip(hoveredTone);
				} else {
					activeTones.set(hoveredTone, activeTones.get(touchedTone));
					activeTones.clear(touchedTone);
				}
				touchedTone = null;
				invalidate();
			}
			break;
		}

		return true;
	}

	private Integer getHoveredTone(float x, float y) {
		float cy = y - height * 0.5f;
		double cx = x - width * 0.5;
		double angle = Math.atan2(cy, cx);
		double radius = Math.sqrt(cx * cx + cy * cy);
		if (radius < bigRadius - beadRadius || radius > bigRadius + beadRadius) {
			return null;
		}
		int index = (int) (Math
			.round(((angle + HALF_PI) / TWO_PI) * TONE_COUNT));
		return (index + TONE_COUNT) % TONE_COUNT;
	}

}
