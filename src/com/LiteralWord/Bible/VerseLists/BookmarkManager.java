package com.LiteralWord.Bible.VerseLists;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import com.LiteralWord.Bible.BibleReader;
import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.VerseLists.VersesListFragment.OnVerseSelectedListener;


public class BookmarkManager extends FragmentActivity implements OnVerseSelectedListener{

	private static final String TAG = "BookmarkManager";
	
	public static class BookmarkFragment extends VersesListFragment {

		public BookmarkFragment() {
			super(VerseDbAdapter.BOOKMARK_TABLE);
		}

	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		
		super.onCreate(arg0);
		FrameLayout f = new FrameLayout(this);
		f.setId(R.id.body);
		addContentView(f, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		FragmentTransaction myFragLayout = getSupportFragmentManager().beginTransaction();
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			BookmarkFragment vf = new BookmarkFragment();
			myFragLayout.add(R.id.body , vf, TAG);
			myFragLayout.commit();
		}
		
	}

	@Override
	public void onVersesSelected(long id, String book, int chapter, String verse) {
		
		int ver = Integer.parseInt(verse);
		Log.d(LiteralWord.TAG, TAG + " verse " + ver);
		Intent res = new Intent(this, BibleReader.class);
		res.putExtra(BibleReader.SEARCH_TAG, false);
		res.putExtra(LiteralWord.VERSES_BOOK_ROWID, book);
		res.putExtra(LiteralWord.VERSES_CHAPTERS_ROWID, chapter);
		res.putExtra(LiteralWord.VERSES_NUM_ROWID, ver);
		res.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(res);
		
	}


}
