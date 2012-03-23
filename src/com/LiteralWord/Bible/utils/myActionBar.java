package com.LiteralWord.Bible.utils;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.LiteralWord.Bible.BibleReader;
import com.LiteralWord.Bible.R;

public class myActionBar extends myTitleBar{

	private boolean home = true;

	public myActionBar(Context c, View ab, MenuSelector menuSelector) {
		super(c, ab, menuSelector);
		
		if (ActionBar != null) {
			
			((Button) ActionBar.findViewById(R.id.home_but))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (home) {
								Intent i = new Intent(context,
										BibleReader.class);
								i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								context.startActivity(i);
							}
						}
					});
		}
	}

	public void homeButton(boolean w) {
		home = w;
	}
}