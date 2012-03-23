package com.LiteralWord.Bible.utils;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.LiteralWord.Bible.R;

public class myTitleBar {

	public interface MenuSelector {
		public void onMenuItemSelected(int id);

	}

	public static class ExtendedMenuSelector implements MenuSelector {
		public MenuSelector old;
		public ExtendedMenuSelector(MenuSelector m) {
			old = m;
		}
		
		@Override
		public void onMenuItemSelected(int id) {
			// Others to implement this
			old.onMenuItemSelected(id);
		}
		
	}
	protected MenuSelector mySelector;
	protected View ActionBar;
	protected TextView Title;
	protected LinearLayout MenuBar;
	protected Context context;

	public myTitleBar(Context c, View ab, MenuSelector menuSelector) {
		context = c;
		ActionBar = ab;
		if (ActionBar != null) {
			ab.setVisibility(View.VISIBLE);
			Title = (TextView) ActionBar.findViewById(R.id.title);
			MenuBar = (LinearLayout) ActionBar
					.findViewById(R.id.my_title_menu);
			mySelector = menuSelector;
			
		}
	}

	public void addButton(int id, int drawable_id) {
		Button newB = new Button(context);
		newB.setId(id);
		newB.setBackgroundResource(drawable_id);
		newB.setOnClickListener(new myClickListener(id));

		MenuBar.addView(newB, new LayoutParams(45, 45));
	}

	public void setNewMenuSelector(MenuSelector m) {
		mySelector = m;
	}
	
	public MenuSelector getMenuSelector() {
		return mySelector;
	}
	public void setTitle(String t) {
		Title.setText(t);
	}

	private class myClickListener implements OnClickListener {
		int id;

		public myClickListener(int i) {
			id = i;
		}

		@Override
		public void onClick(View arg0) {
			mySelector.onMenuItemSelected(id);
		}

	}
}