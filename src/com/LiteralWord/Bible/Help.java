package com.LiteralWord.Bible;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


public class Help extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.help);
		setContentView(R.layout.help_html);
		((WebView) findViewById(R.id.help_html)).loadUrl("file:///android_asset/help.html");
	}

}
