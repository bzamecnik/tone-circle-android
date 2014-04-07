package com.harmoneye.tonecircle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

//	private TextView titleView;
	private static final String ACTIVE_TONES = "active_tones";

	private ToneCircleView toneCircleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		titleView = (TextView) findViewById(R.id.title);
		toneCircleView = (ToneCircleView) findViewById(R.id.toneCircle);

//		CircleLayout circle = (CircleLayout) findViewById(R.id.circleLayout);
//
//		for (int i = 0; i < 12; i++) {
//			ToggleButton toggle = new ToggleButton(getApplicationContext());
//			CharSequence title = TONE_NAMES[i];
//			toggle.setText(title);
//			toggle.setTextOn(title);
//			toggle.setTextOff(title);
//			//toggle.setBackgroundResource(R.drawable.magenta_shape);
//			toggle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
//			toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView,
//					boolean isChecked) {
//					updateChordName();
//				}
//			});
//			toggles.add(toggle);
//			circle.addView(toggle);
//		}
	@Override
	protected void onResume() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		int activeTones = prefs.getInt(ACTIVE_TONES, 0);
		if (activeTones > 0) {
			toneCircleView.setActiveTones(PitchClassSet.fromIndex(activeTones));
		}
		super.onResume();
	}

//	protected void updateChordName() {
//		StringBuilder title = new StringBuilder();
//		for () {
//			if (toggle.isChecked()) {
//				title.append(toggle.getText()).append(" ");
//			}
//		}
//		titleView.setText(title.toString());
//	}
	@Override
	protected void onPause() {
		int activeTones = toneCircleView.getActiveTones().getIndex();
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(ACTIVE_TONES, activeTones);
		editor.commit();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
