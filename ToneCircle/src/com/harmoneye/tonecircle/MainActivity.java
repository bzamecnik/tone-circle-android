package com.harmoneye.tonecircle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.harmoneye.tonecircle.ToneCircleView.OnTonesChangedListener;

public class MainActivity extends Activity {

	private static final String ACTIVE_TONES = "active_tones";

	private PitchClassSetNamer namer;
	private ToneCircleView toneCircleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		namer = PitchClassSetNamer.fromJson(getResources()
			.openRawResource(R.raw.chord_names));

		final TextView titleView = (TextView) findViewById(R.id.title);
		toneCircleView = (ToneCircleView) findViewById(R.id.toneCircle);

		toneCircleView.setOnTonesChangedListener(new OnTonesChangedListener() {
			@Override
			public void onTonesChanged(PitchClassSet activeTones) {
				titleView.setText(namer.getName(activeTones));
			}
		});
	}

	@Override
	protected void onResume() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		int activeTones = prefs.getInt(ACTIVE_TONES, 0);
		if (activeTones > 0) {
			toneCircleView.setActiveTones(PitchClassSet.fromIndex(activeTones));
		}
		super.onResume();
	}

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
