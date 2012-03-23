package com.LiteralWord.Bible.VerseLists;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.VerseLists.MemorizeAction.MemorizeActionFragment;
import com.LiteralWord.Bible.VerseLists.MemorizeAction.MemoryVerseListener;
import com.LiteralWord.Bible.VerseLists.VersesListFragment.OnVerseSelectedListener;
import com.LiteralWord.Bible.utils.VerseListStringConverter;
import com.LiteralWord.Bible.utils.myTitleBar.ExtendedMenuSelector;

public class MemoryVerses extends FragmentActivity implements OnVerseSelectedListener, MemoryVerseListener{

	private static final String TAG = "MemoryVerses";
	private MemoryVerseFragment vf;

	
	public static class MemoryVerseFragment extends VersesListFragment {
		private final String TAG = "MemoryVersesFragment";
		MemoryVerseListener mvl;
		
		public MemoryVerseFragment() {
			super(VerseDbAdapter.MEMVERSE_TABLE);
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			try {
				mvl = (MemoryVerseListener) activity;
			} catch (ClassCastException e) {
				Log.e(TAG, "Bad class", e);
				throw new ClassCastException(activity.toString()
						+ " must implement MemoryVerseListener");
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View ret = super.onCreateView(inflater, container, savedInstanceState);
			mTBar.addButton(R.id.start_memory, android.R.drawable.ic_media_play);
			ExtendedMenuSelector m = new ExtendedMenuSelector(mTBar.getMenuSelector()) {
				
				@Override
				public void onMenuItemSelected(int id) {
					switch (id) {
					case R.id.start_memory:
						mvl.MemoryAction(myVerses);
						break;
					default:
						old.onMenuItemSelected(id);
						break;
					}
					
				}
			};
			mTBar.setNewMenuSelector(m);
			return ret;
		}
		
		public VerseDbAdapter getVerseDb() {
			return myVerses;
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
		vf = (MemoryVerseFragment) getSupportFragmentManager().findFragmentByTag(TAG);
		if ( vf == null) {
			vf = new MemoryVerseFragment();
			myFragLayout.add(R.id.body , vf, TAG);
			myFragLayout.commit();
		}
		
		
	}

	@Override
	public void onVersesSelected(long id, String book, int chapter, String verse) {
		MemoryAction(id, vf.getVerseDb());
	}
	
	@Override
	public void MemoryAction(VerseDbAdapter mDB) {
		fireAction(mDB.fetchAllverses());
		
	}

	@Override
	public void MemoryAction(String tag, VerseDbAdapter mDB) {
		fireAction(mDB.fetchVerse(tag));
	}

	@Override
	public void MemoryAction(long id, VerseDbAdapter mDB) {
		
		fireAction(mDB.fetchVerse(id));
		
	}
	
	private void fireAction(Cursor cur) {
		ArrayList<String> title_l = new ArrayList<String>();
		ArrayList<String> text_l = new ArrayList<String>();
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			
			title_l.add(VerseListStringConverter.toVerseString(
					cur.getString(cur.getColumnIndex(LiteralWord.VERSES_BOOK_ROWID)), 
					cur.getInt(cur.getColumnIndex(LiteralWord.VERSES_CHAPTERS_ROWID)), 
					cur.getString(cur.getColumnIndex(LiteralWord.VERSES_NUM_ROWID))));
					
			text_l.add(cur.getString(cur.getColumnIndex(LiteralWord.VERSES_TEXT_ROWID)));
			
			cur.moveToNext();
		}
		cur.close();
		
		if (title_l.size() == 0) return;
		Intent i = new Intent(this, MemorizeAction.class);
		i.putStringArrayListExtra(MemorizeActionFragment.TITLE_TAG, title_l);
		i.putStringArrayListExtra(MemorizeActionFragment.TEXT_TAG, text_l);
		startActivity(i);
	}
}
