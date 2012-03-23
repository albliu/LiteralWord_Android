package com.LiteralWord.Bible.VerseLists;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.R;

public class MemorizeAction extends FragmentActivity {
	
	private static final String TAG = "MemorizeAction";
	
	public interface MemoryVerseListener {
		public void MemoryAction(VerseDbAdapter mDB);
		public void MemoryAction(String tag, VerseDbAdapter mDB);
		public void MemoryAction(long id, VerseDbAdapter mDB);
	}
	
	
	static public class MemorizeActionFragment extends Fragment implements OnClickListener {

		private static final String TAG = "MemorizeActionFragment";
		
		public static final String VERSE_ACTION_TAG = "verse";
		public static final String ALL_ACTION_TAG = "all";
		public static final String TAG_ACTION_TAG = "tag";
		
		public static final String TITLE_TAG = "titles";
		public static final String SHOW_TAG = "verseShow?";
		public static final String INDEX_TAG = "where we are at!";
		public static final String TEXT_TAG = "texts";
		public static final String RESTART_TAG = "restart";
		
		class iVerse {
			public String title;
			public String text;
			public iVerse(String tit, String txt) {
				title = tit;
				text = txt;
			}
		}
		
		View myView;
		TextView mTitle;
		TextView mText;
		Context myContext;
		ArrayList<iVerse> mVerses;
		int indx = -1;
		boolean verShow = false;
		int restart = View.GONE;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d(LiteralWord.TAG, TAG + " onCreateView");
			myView = inflater.inflate(R.layout.memory_action, container, false);
			mTitle = (TextView) myView.findViewById(R.id.verse_title);
			mText = (TextView) myView.findViewById(R.id.verse_text);
			myView.setOnClickListener(this);
			
			myView.findViewById(R.id.verse_text).setOnClickListener(this);
			myView.findViewById(R.id.verse_title).setOnClickListener(this);
			myView.findViewById(R.id.verse_screen).setOnClickListener(this);
			myView.findViewById(R.id.mem_restart).setOnClickListener(this);
			myView.findViewById(R.id.mem_prev).setOnClickListener(this);
			myView.findViewById(R.id.mem_next).setOnClickListener(this);
			myView.findViewById(R.id.mem_exit).setOnClickListener(this);
			
			
			myContext = myView.getContext();
			mVerses = new ArrayList<iVerse>();
			
			
			if (savedInstanceState != null) {
				restart = savedInstanceState.getInt(RESTART_TAG);
				verShow = savedInstanceState.getBoolean(SHOW_TAG);
				indx = savedInstanceState.getInt(INDEX_TAG);
				ArrayList<String> ti = savedInstanceState.getStringArrayList(TITLE_TAG);
				ArrayList<String> tx = savedInstanceState.getStringArrayList(TEXT_TAG);
				for (int i =0; i < ti.size(); i++) {
					mVerses.add(new iVerse(ti.get(i), tx.get(i)));
				}
				
				showMemorizeVerse(indx, verShow);
				myView.findViewById(R.id.mem_restart).setVisibility(restart);
				
			} else {
				onHandleIntent(getActivity().getIntent());
			}
			return myView;
		}

		public void onHandleIntent(Intent it) {
			Bundle extra = it.getExtras();
			if (extra == null) return;

			ArrayList<String> ti = extra.getStringArrayList(TITLE_TAG);
			ArrayList<String> tx = extra.getStringArrayList(TEXT_TAG);
			for (int i =0; i < ti.size(); i++) {
				mVerses.add(new iVerse(ti.get(i), tx.get(i)));
			}
			
			// if there's only 1 verse, just show it first
			if (ti.size() == 1)
				showMemorizeVerse(0, true);
			else 
				showMemorizeVerse(0, false);
			// if we're coming back from a orientation change, set the view accordingly
			
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			
			super.onSaveInstanceState(outState);
			ArrayList<String> lTitle = new ArrayList<String>();
			ArrayList<String> lText = new ArrayList<String>();
			for (int i = 0; i < mVerses.size(); i++) {
				lText.add(mVerses.get(i).text);
				lTitle.add(mVerses.get(i).title);
			}
			
			outState.putInt(RESTART_TAG, myView.findViewById(R.id.mem_restart).getVisibility());
			outState.putBoolean(SHOW_TAG, verShow);
			outState.putInt(INDEX_TAG, indx);
			outState.putStringArrayList(TITLE_TAG, lTitle);
			outState.putStringArrayList(TEXT_TAG, lText);
		}



		
		/*****************************
		 * memorize action starting!
		 ***************************/
		private void showMemorizeVerse(int idx, boolean reveal) {
			
			if (idx < 0 ) return;
			if (idx >= mVerses.size()) {
				myView.findViewById(R.id.mem_restart).setVisibility(View.VISIBLE);
				return;
			}
			
			myView.findViewById(R.id.mem_restart).setVisibility(View.GONE);
			if (mVerses.size() == 1) {
				myView.findViewById(R.id.mem_next).setVisibility(View.GONE);
				myView.findViewById(R.id.mem_prev).setVisibility(View.GONE);
			}
			
			indx = idx;
			verShow = reveal;
			mTitle.setText(mVerses.get(indx).title);
			if (reveal) { 
				mText.setText(mVerses.get(indx).text);
			} else {
				mText.setText("");
			}
			
			// set the current verse. 
			// everything else will be based on clicks
		}

		private void revealVerse(int idx) {
			verShow = true;
			Log.d(LiteralWord.TAG, TAG + " showing: " + mVerses.get(idx).text);
			mText.setText(mVerses.get(idx).text);
		}

		@Override
		public void onClick(View v) {
			Log.d(LiteralWord.TAG, TAG + " onClick ");
			
			switch (v.getId()) {

			case R.id.mem_restart:
				showMemorizeVerse(0, false);
				break;
			case R.id.mem_next:
				showMemorizeVerse(indx + 1, false);		
				break;
			case R.id.mem_prev:
				showMemorizeVerse(indx - 1, false);
				break;
			case R.id.mem_exit:
				// figure it out later
				break;
			default:
				if (!verShow) {
					revealVerse(indx);
					return;
				}
				showMemorizeVerse(indx + 1, false);
				break;
			}
			
		}
		
	}
	
	
	@Override
	protected void onCreate(Bundle arg0) {
		
		
		super.onCreate(arg0);
		Log.d(LiteralWord.TAG, TAG + " onCreate");
		FrameLayout f = new FrameLayout(this);
		f.setId(R.id.body);
		addContentView(f, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


		FragmentTransaction myFragLayout = getSupportFragmentManager().beginTransaction();
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			MemorizeActionFragment vf = new MemorizeActionFragment();
			myFragLayout.add(R.id.body , vf, TAG);
			myFragLayout.commit();
		}
		
	}	
	
}
