package com.LiteralWord.Bible.utils;

import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;

public class myItemClickDialogListener implements AdapterView.OnItemClickListener {


	Dialog d;
	AdapterView.OnItemClickListener l;
	
	public myItemClickDialogListener(Dialog dialog, AdapterView.OnItemClickListener lis) {
	
		d = dialog;
		l = lis;
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		d.dismiss();
		l.onItemClick(arg0, arg1, arg2, arg3);
		
	}

}
