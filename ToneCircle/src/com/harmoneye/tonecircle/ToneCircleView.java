package com.harmoneye.tonecircle;

import java.util.ArrayList;
import java.util.BitSet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
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

	private static final int BACKGROUND_COLOR = Color.WHITE;

	// the model
	private BitSet activeTones = new BitSet(TONE_COUNT);

	private ArrayList<Bead> beads;

	private Paint textPaint;
	private Paint beadPaint;
	private Paint activeBeadPaint;
	private Paint chordPaint;

	private Rect textRect = new Rect();

	private ChordPath chordPath;

	private SingleDragNDropDetector dndDetector;
	private RotationGestureDetector rotationDetector;

	public ToneCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		textPaint = new Paint();
		textPaint.setColor(BACKGROUND_COLOR);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setAntiAlias(true);

		beadPaint = new Paint();
		beadPaint.setAntiAlias(true);

		float[] hsvColor = new float[] { 360 * 0.25f, 0.15f, 0.9f };
		int color = Color.HSVToColor(hsvColor);
		beadPaint.setColor(color);

		activeBeadPaint = new Paint(beadPaint);
		hsvColor[1] = 0.5f;
		hsvColor[2] = 0.7f;
		int activeColor = Color.HSVToColor(hsvColor);
		activeBeadPaint.setColor(activeColor);

		chordPaint = new Paint(activeBeadPaint);
		chordPaint.setStyle(Style.STROKE);
		chordPaint.setStrokeWidth(5);

		beads = new ArrayList<Bead>(TONE_COUNT);
		for (int i = 0; i < TONE_COUNT; i++) {
			beads.add(new Bead(new PointF(0, 0), new PointF(0, 0), 0));
		}

		chordPath = new ChordPath();

		dndDetector = new SingleDragNDropDetector(new OnDragNDropListener() {
			@Override
			public void onDrop(Integer sourceTone, Integer targetTone) {
				if (targetTone == null) {
					activeTones.clear(sourceTone);
				} else if (targetTone.equals(sourceTone)) {
					activeTones.flip(targetTone);
				} else if (sourceTone != null && activeTones.get(sourceTone)) {
					activeTones.set(targetTone, activeTones.get(sourceTone));
					activeTones.clear(sourceTone);
				}
				invalidate();
			}
		});

		rotationDetector = new RotationGestureDetector(
			new OnRotationGestureListener() {
				@Override
				public void onRotation(BitSet origActiveTones, float angle) {
					int translation = Modulo.modulo((int) Math.round(angle
						/ 360.0 * TONE_COUNT),
						TONE_COUNT);
					if (translation != 0) {
						activeTones = transpose(origActiveTones, translation);
						invalidate();
					}
				}
			});
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

		canvas.drawColor(BACKGROUND_COLOR);

		canvas.save();

		canvas.translate(0.5f * width, 0.5f * height);

		drawBeadCircle(canvas);
		chordPath.draw(canvas);

		canvas.restore();
	}

	private void drawBeadCircle(Canvas canvas) {
		for (int i = 0; i < TONE_COUNT; i++) {
			Bead bead = beads.get(i);
			PointF center = bead.getCenter();
			canvas.drawCircle(center.x,
				center.y,
				bead.getRadius(),
				activeTones.get(i) ? activeBeadPaint : beadPaint);

			String toneName = getToneName(i);
			textPaint.getTextBounds(toneName, 0, toneName.length(), textRect);
			canvas.drawText(toneName, center.x, center.y + textRect.height()
				* 0.5f, textPaint);
		}
	}

	// not very efficient
	protected BitSet transpose(BitSet activeTones, int transposition) {
		BitSet newBits = new BitSet(TONE_COUNT);
		int length = activeTones.cardinality();
		int prev = 0;
		for (int i = 0; i < length; i++) {
			int index = activeTones.nextSetBit(prev);
			newBits.set(Modulo.modulo(index - transposition, TONE_COUNT));
			prev = index + 1;
		}
		return newBits;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float xpad = (float) (getPaddingLeft() + getPaddingRight());
		float ypad = (float) (getPaddingTop() + getPaddingBottom());

		float width = w - xpad;
		float height = h - ypad;

		float windowRadius = 0.5f * (float) Math.min(width, height);
		float beadRadius = 0.19f * windowRadius;
		float bigRadius = 0.8f * windowRadius;
		float apexRadius = bigRadius - beadRadius;
		// apexRadius *= 0.9f;

		float toneCountInv = 1.0f / TONE_COUNT;
		for (int i = 0; i < TONE_COUNT; i++) {
			float p = i * toneCountInv;

			double angle = p * TWO_PI - HALF_PI;
			float x = (float) Math.cos(angle);
			float y = (float) Math.sin(angle);

			PointF center = new PointF(bigRadius * x, bigRadius * y);
			PointF apex = new PointF(apexRadius * x, apexRadius * y);
			Bead bead = new Bead(center, apex, beadRadius);
			beads.set(i, bead);
		}

		textPaint.setTextSize(beadRadius);

		dndDetector.setWidth(w);
		dndDetector.setHeight(h);
		dndDetector.setBigRadius(bigRadius);
		dndDetector.setBeadRadius(beadRadius);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (rotationDetector.onTouchEvent(event)) {
			return true;
		}
		dndDetector.onTouchEvent(event);
		return true;
	}

	private String getToneName(int i) {
		return TONE_NAMES[i];
	}

	private static class SingleDragNDropDetector {

		private OnDragNDropListener listener;

		private Integer touchedTone;

		private int width;
		private int height;
		private float bigRadius;
		private float beadRadius;

		public SingleDragNDropDetector(OnDragNDropListener listener) {
			this.listener = listener;
		}

		public boolean onTouchEvent(MotionEvent event) {
			if (listener == null) {
				return false;
			}
			int pointerIndex = event.getActionIndex();
			float x = event.getX(pointerIndex);
			float y = event.getY(pointerIndex);
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				touchedTone = getHoveredTone(x, y);
				break;
			case MotionEvent.ACTION_UP:
				if (touchedTone != null) {
					Integer hoveredTone = getHoveredTone(x, y);
					listener.onDrop(touchedTone, hoveredTone);
					touchedTone = null;
				}
				break;
			}
			return false;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public void setBigRadius(float bigRadius) {
			this.bigRadius = bigRadius;
		}

		public void setBeadRadius(float beadRadius) {
			this.beadRadius = beadRadius;
		}

		private Integer getHoveredTone(float x, float y) {
			float cy = y - height * 0.5f;
			double cx = x - width * 0.5;
			double angle = Math.atan2(cy, cx);
			double radius = Math.sqrt(cx * cx + cy * cy);
			if (radius < bigRadius - beadRadius
				|| radius > bigRadius + beadRadius) {
				return null;
			}
			int index = (int) (Math.round(((angle + HALF_PI) / TWO_PI)
				* TONE_COUNT));
			return (index + TONE_COUNT) % TONE_COUNT;
		}

	}

	private interface OnDragNDropListener {
		void onDrop(Integer sourceTone, Integer targetTone);
	}

	// http://stackoverflow.com/questions/10682019/android-two-finger-rotation
	public class RotationGestureDetector {
		private float fX, fY, sX, sY;
		private Integer ptrId1, ptrId2;
		private float angle;

		private BitSet origActiveTones;

		private OnRotationGestureListener listener;

		public RotationGestureDetector(OnRotationGestureListener listener) {
			this.listener = listener;
		}

		public boolean onTouchEvent(MotionEvent event) {
			if (listener == null) {
				return false;
			}
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				ptrId1 = event.getPointerId(event.getActionIndex());
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				ptrId2 = event.getPointerId(event.getActionIndex());
				sX = event.getX(event.findPointerIndex(ptrId1));
				sY = event.getY(event.findPointerIndex(ptrId1));
				fX = event.getX(event.findPointerIndex(ptrId2));
				fY = event.getY(event.findPointerIndex(ptrId2));
				origActiveTones = activeTones;
				break;
			case MotionEvent.ACTION_MOVE:
				if (ptrId1 != null && ptrId2 != null) {
					float nfX, nfY, nsX, nsY;
					nsX = event.getX(event.findPointerIndex(ptrId1));
					nsY = event.getY(event.findPointerIndex(ptrId1));
					nfX = event.getX(event.findPointerIndex(ptrId2));
					nfY = event.getY(event.findPointerIndex(ptrId2));

					angle = angleBetweenLines(fX,
						fY,
						sX,
						sY,
						nfX,
						nfY,
						nsX,
						nsY);

					listener.onRotation(origActiveTones, angle);
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				ptrId1 = null;
				origActiveTones = null;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				ptrId2 = null;
				break;
			}
			return false;
		}

		private float angleBetweenLines(float fX, float fY, float sX, float sY,
			float nfX, float nfY, float nsX, float nsY) {
			float angle1 = (float) Math.atan2((fY - sY), (fX - sX));
			float angle2 = (float) Math.atan2((nfY - nsY), (nfX - nsX));

			float angle = ((float) Math.toDegrees(angle1 - angle2)) % 360;
			return angle;
		}
	}

	public static interface OnRotationGestureListener {
		public void onRotation(BitSet origActiveTones, float angle);
	}

	private static class Bead {
		private PointF center;
		// connection point for the curves joining the beads inside the circle
		private PointF apex;
		private float radius;

		public Bead(PointF center, PointF apex, float radius) {
			this.center = center;
			this.apex = apex;
			this.radius = radius;
		}

		public PointF getCenter() {
			return center;
		}

		public PointF getApex() {
			return apex;
		}

		public float getRadius() {
			return radius;
		}
	}

	private class ChordPath {
		public void draw(Canvas canvas) {
			Path path = new Path();

			int length = activeTones.cardinality();
			if (length <= 1) {
				return;
			}

			chordPaint.setStyle(length > 2 ? Style.FILL_AND_STROKE
				: Style.STROKE);

			int first = activeTones.nextSetBit(0);
			int from = first;
			PointF firstApex = beads.get(first).getApex();
			path.moveTo(firstApex.x, firstApex.y);
			PointF prevApex = firstApex;
			for (int i = 1; i < length; i++) {
				int to = activeTones.nextSetBit(from + 1);
				PointF apex = beads.get(to).getApex();
				addQuadSegment(path,
					from,
					to,
					apex.x,
					apex.y,
					prevApex.x,
					prevApex.y);
				from = to;
				prevApex = apex;
			}

			if (length >= 3) {
				addQuadSegment(path,
					from,
					first,
					firstApex.x,
					firstApex.y,
					prevApex.x,
					prevApex.y);
			}

			canvas.drawPath(path, chordPaint);
		}

		private void addQuadSegment(Path path, int from, int to, float x,
			float y, float prevX, float prevY) {
			int interval = intervalClass(from, to);
			float stiffness = 1 - interval / 6.0f;
			float midX = (x + prevX) * 0.5f * stiffness;
			float midY = (y + prevY) * 0.5f * stiffness;
			path.quadTo(midX, midY, x, y);
		}

		private int intervalClass(int one, int other) {
			int interval = Modulo.modulo(one - other, TONE_COUNT);
			int HALF = TONE_COUNT / 2;
			return (interval > HALF) ? TONE_COUNT - interval : interval;
		}

	}
}
