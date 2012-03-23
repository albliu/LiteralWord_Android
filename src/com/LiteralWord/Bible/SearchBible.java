package com.LiteralWord.Bible;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.LiteralWord.Bible.Notes.NoteEdit;

public class SearchBible extends FragmentActivity implements SearchFragment.OnSearchSelectedListener, BibleReaderFragment.OnVerseActionListener {

	public final String TAG = "SearchBible";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_fragment);
		handleIntent(getIntent());
	}

	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);   
	}
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String q = intent.getStringExtra(SearchManager.QUERY);
            ((SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment)).performSearch(q);
        }
	}


	public void onSearchSelected(String book, int chapter, int verse) {
        
	    	Intent i = new Intent(this, BibleReader.class);
	        i.putExtra(LiteralWord.VERSES_BOOK_ROWID,book);
	        i.putExtra(LiteralWord.VERSES_CHAPTERS_ROWID, chapter);
	        i.putExtra(LiteralWord.VERSES_NUM_ROWID, verse);
	        i.putExtra(BibleReader.SEARCH_TAG, true);
	        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        startActivity(i);
		
	}


	@Override
	public void onVersesNotes(String book, int chapter, ArrayList<Integer> verse) {
		// TODO link note with verses
			for (int i = 0; i < verse.size() ; i++) 
				Log.d(LiteralWord.TAG, TAG + " Tagged : " + book + " " + Integer.toString(chapter) + ":" + Integer.toString(verse.get(i)));

		Intent i = new Intent(this, NoteEdit.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

}
