package com.harmoneye.tonecircle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

//	private TextView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		titleView = (TextView) findViewById(R.id.title);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
