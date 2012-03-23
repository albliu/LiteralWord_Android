package com.LiteralWord.Bible.utils;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;

	public class myContextMenu {
		
		private MenuSelector mySelector;
		private AlertDialog diag;
		private View menu;
		private LinearLayout menuItems;
		private Context context;
		private TextView title;
	
		
		public myContextMenu(Context c, AlertDialog m, int layout, MenuSelector menuSelector) {
			context = c;
			diag = m;
			if (diag != null) { 
				LayoutInflater myLI = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				menu = myLI.inflate(layout, null);
				title = (TextView) menu.findViewById(R.id.context_menu_title);
				menuItems = (LinearLayout) menu.findViewById(R.id.context_menu);
				mySelector = menuSelector;
				
				// set to add all these buttons
				addButton(R.id.copy_to_clipboard, "Copy To Clipboard");
				addButton(R.id.add_note, "Add New Note");
				addButton(R.id.add_memory, "Add to Memorize List");
				addButton(R.id.add_bookmark, "Add to Bookmarks");
				
				ScrollView scr = new ScrollView(c);
				scr.addView(menu);
				m.setView(scr);
			}			
		}	
		public void setTitle(String t) {
			title.setText(t);
			
		}
		
		public void addButton(int id, String text) {
			Button newB = new Button(context);
			newB.setId(id);
			newB.setText(text);
			//newB.setBackgroundColor(android.R.color.white);
			newB.setSingleLine();
			newB.setTextSize(20);
			newB.setOnClickListener(new myClickListener(id, diag));
			
			menuItems.addView(newB, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

		private class myClickListener implements OnClickListener {
			int id;
			AlertDialog d;
			public myClickListener(int i, AlertDialog m) {
				id = i;
				d = m;
			}
			@Override
			public void onClick(View arg0) {
				d.dismiss();
				mySelector.onMenuItemSelected(id);
			}
			
		}
	}