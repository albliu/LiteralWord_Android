package com.LiteralWord.Bible;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewFlipper;

import com.LiteralWord.Bible.BibleReaderFragment.OnVerseActionListener;
import com.LiteralWord.Bible.SearchFragment.OnSearchSelectedListener;
import com.LiteralWord.Bible.Notes.MyDbAdapter;
import com.LiteralWord.Bible.Notes.NoteEdit;
import com.LiteralWord.Bible.Notes.NoteEditFragment;
import com.LiteralWord.Bible.Notes.NoteEditFragment.OnNoteEditListener;
import com.LiteralWord.Bible.Notes.NoteListFragment;
import com.LiteralWord.Bible.Notes.NoteListFragment.OnNoteSelectedListener;
import com.LiteralWord.Bible.Notes.NotepadList;
import com.LiteralWord.Bible.VerseLists.BookmarkManager;
import com.LiteralWord.Bible.VerseLists.MemoryVerses;
import com.LiteralWord.Bible.utils.myActionBar;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;

public class BibleReader extends FragmentActivity implements OnVerseActionListener, OnSearchSelectedListener, OnNoteEditListener, OnNoteSelectedListener {
	
	private static String TAG = "BibleReader";
	public static final String SEARCH_PANEL = "search panel";
	public static final String NOTEEDIT_PANEL = "note edit panel";
	public static final String NOTELIST_PANEL = "note list panel";
	public static final String SPLIT_PANEL = "second panel";
	public static final String BOOKMARK_PANEL = "bookmark panel";

	public static final String SEARCH_TAG = "isSearch";
	

	BibleReaderFragment primaryReader;
	int myStyle;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.d(LiteralWord.TAG, TAG + " - onCreate");
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.reader_fragment);
		
		
		
		
		View ab = findViewById(R.id.my_action_bar);
		if (ab != null) {
			myActionBar myAB = new myActionBar(this, ab, new MenuSelector() {
				@Override
				public void onMenuItemSelected(int id) {
					onMenuIndexSelected(id);

				}
			});
			myAB.homeButton(false);
			myAB.addButton(R.id.gotoSearch, android.R.drawable.ic_menu_search);
			myAB.addButton(R.id.toggleView, android.R.drawable.ic_menu_add);
			myAB.addButton(R.id.gotoNotes, android.R.drawable.ic_menu_edit);
		}
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(SEARCH_PANEL)) {
				ViewFlipper view = (ViewFlipper) findViewById(R.id.notes_p);
				view.setVisibility(savedInstanceState.getInt(NOTELIST_PANEL));
				view.setDisplayedChild(savedInstanceState.getInt(NOTEEDIT_PANEL));
				findViewById(R.id.search_panel).setVisibility(savedInstanceState.getInt(SEARCH_PANEL));
				findViewById(R.id.split_screen_panel).setVisibility(savedInstanceState.getInt(SPLIT_PANEL));
				
			}
		}
		
		
		primaryReader = (BibleReaderFragment) getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		
		Log.d(LiteralWord.TAG, TAG + " - onNewIntent");
		setIntent(intent);
		primaryReader.handleIntent(getIntent());
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		
		ViewFlipper view = (ViewFlipper) findViewById(R.id.notes_p);
		if (view == null) return;
		
		outState.putInt(SEARCH_PANEL, findViewById(R.id.search_panel).getVisibility());
		outState.putInt(NOTELIST_PANEL, findViewById(R.id.notes_p).getVisibility());
		outState.putInt(SPLIT_PANEL, findViewById(R.id.split_screen_panel).getVisibility());
		outState.putInt(NOTEEDIT_PANEL, view.getDisplayedChild());
	}

	@Override
	protected void onPause() {
		
		Log.d(LiteralWord.TAG, TAG + " - onPause");
		primaryReader.savePlace();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		
		Log.d(LiteralWord.TAG, TAG + " - onDestroy");
		super.onDestroy();
		
	}

	/***************************************
	 * Menu Code
	 *********************************/
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	getMenuInflater().inflate(R.menu.reader_menu, menu);
    	
    	if (findViewById(R.id.my_action_bar) != null) {
    		menu.removeItem(R.id.gotoSearch);
    		menu.removeItem(R.id.toggleView);
    		
    	}
    	return true;
        
    }
   
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	// override the gotoNotes when selected from Menu
    	if (item.getItemId() == R.id.gotoNotes) {
    		Intent i = new Intent(this, NotepadList.class);
    		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
			return true;
    	}
    	if (onMenuIndexSelected(item.getItemId())) return true;
		return super.onOptionsItemSelected(item);
	}

    public boolean onMenuIndexSelected(int Id) {
    	switch (Id) {
    	case R.id.gotoSearch:

			View sPanel = findViewById(R.id.search_panel);
			if (sPanel == null) {
				Intent i = new Intent(this, SearchBible.class);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
				return true;
				
			}
			
			openSearchPage(sPanel);
			
			return true;
		
		case R.id.gotoNotes: 
			
			View nPanel = findViewById(R.id.notes_p);
			if (nPanel == null) {
				Intent i = new Intent(this, NotepadList.class);
				startActivity(i);
				return true;
			}
			
			openNotesList(nPanel);
			return true;
		case R.id.toggleView:

			// my Style = primaryReader.toggleView();

			View sScreen = findViewById(R.id.split_screen_panel);
			if (sScreen == null) {
				break;
			}
			
			BibleReaderFragment second_screen = (BibleReaderFragment) getSupportFragmentManager().findFragmentByTag(SPLIT_PANEL);
			if (second_screen == null) {
				
				sScreen.setVisibility(View.VISIBLE);
				((BibleReaderFragment) getSupportFragmentManager()
						.findFragmentById(R.id.reader_fragment)).savePlace();
				
				FragmentTransaction myFragLayout = getSupportFragmentManager().beginTransaction();
				second_screen = new BibleReaderFragment();
				myFragLayout.add(R.id.split_screen_panel, second_screen, SPLIT_PANEL);
				myFragLayout.commit();
				
			} else {
				if (sScreen.getVisibility() == View.GONE) sScreen.setVisibility(View.VISIBLE);
				else sScreen.setVisibility(View.GONE);
			}
			return true;
		case R.id.gotoMemoryV:
			startActivity(new Intent(this, MemoryVerses.class));
			return true;
		
		case R.id.book_marks:
			openBookmarks();
			return true;
		case R.id.help_screen:
			startActivity(new Intent(this, Help.class));
			return true;
    	}
    	return false;
    }

    private void openBookmarks() {
    	
    	Intent i = new Intent(this, BookmarkManager.class);
    	i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    	startActivity(i);
    }
    
    /*    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemToggle = menu.findItem(R.id.toggleView);
		if (myStyle == BibleReaderFragment.LITERARY_VIEW) 
			itemToggle.setTitle("Literal View");
		else if (myStyle == BibleReaderFragment.STUDY_VIEW) 
			itemToggle.setTitle("Study View");
		 else
			itemToggle.setTitle("Default View");
		
		return true;
	}
*/		
	private void openNotesList(View nPanel) {
		
		Fragment editNote = getSupportFragmentManager().findFragmentByTag(NOTEEDIT_PANEL);
		if (editNote == null) {
			FragmentTransaction myFragLayout = getSupportFragmentManager().beginTransaction();
			NoteEditFragment eNote = new NoteEditFragment();
			NoteListFragment lNote = new NoteListFragment();
			
			myFragLayout.add(R.id.notes_edit_panel, eNote, NOTEEDIT_PANEL);
			myFragLayout.add(R.id.notes_folder_panel, lNote, NOTELIST_PANEL);
			myFragLayout.commit();
			
			nPanel.setVisibility(View.VISIBLE);
		} else {
			if (nPanel.getVisibility() == View.GONE) nPanel.setVisibility(View.VISIBLE);
			else nPanel.setVisibility(View.GONE);
		}
	}

	private void openSearchPage(View sPanel) {
		

		
		Fragment search= getSupportFragmentManager()
				.findFragmentByTag(SEARCH_PANEL);
		
		if (search == null) {
			
			FragmentTransaction myFragLayout = getSupportFragmentManager().beginTransaction();
			SearchFragment SearchP = new SearchFragment();
			myFragLayout.add(R.id.search_panel, SearchP, SEARCH_PANEL);
			myFragLayout.commit();
			sPanel.setVisibility(View.VISIBLE);
		} else {
			if (sPanel.getVisibility() == View.GONE) sPanel.setVisibility(View.VISIBLE);
			else sPanel.setVisibility(View.GONE);

		}
	}
	
	// this function is only useful when we are in landscape mode
	@Override
	public void onSearchSelected(String book, int chapter, int verse) {
			BibleReaderFragment viewer = (BibleReaderFragment) getSupportFragmentManager()
		            .findFragmentById(R.id.reader_fragment);
	
		    	Intent i = new Intent();
		    	i.putExtra(LiteralWord.VERSES_BOOK_ROWID,book);
		        i.putExtra(LiteralWord.VERSES_CHAPTERS_ROWID, chapter);
		        i.putExtra(LiteralWord.VERSES_NUM_ROWID, verse);
		        i.putExtra(SEARCH_TAG, true);
		        viewer.handleIntent(i);
		
	}

	@Override
	public void onNoteSelected(long id) {
		ViewFlipper editP = (ViewFlipper) findViewById(R.id.notes_p);
		if (editP == null) return;
		
		
		NoteEditFragment myNotes = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(NOTEEDIT_PANEL);
		Intent edit = new Intent();
		edit.putExtra(MyDbAdapter.KEY_ROWID, id);
		myNotes.onHandleIntent(edit);
		editP.showNext();
	}

	@Override
	public void onNoteCreate() {
		ViewFlipper editP = (ViewFlipper) findViewById(R.id.notes_p);
		if (editP == null) return;
		
		((NoteEditFragment) getSupportFragmentManager().findFragmentByTag(NOTEEDIT_PANEL)).onHandleIntent(new Intent());
		editP.showNext();
	}

	@Override
	public void onEditDismiss() {
		ViewFlipper editP = (ViewFlipper) findViewById(R.id.notes_p);
		if (editP == null) return;
		editP.showPrevious();
		
	}
	
	@Override
	public void onEditDone() {
		onEditDismiss();
		((NoteListFragment) getSupportFragmentManager().findFragmentByTag(NOTELIST_PANEL)).fillData();
	}

	@Override
	public void onVersesNotes(String book, int chapter, ArrayList<Integer> verses) {
		// TODO link note with verses
		for (int i = 0; i < verses.size() ; i++) 
			Log.d(LiteralWord.TAG, TAG + " Tagged : " + book + " " + Integer.toString(chapter) + ":" + Integer.toString(verses.get(i)));

		ViewFlipper myNotePanel = ((ViewFlipper) findViewById(R.id.notes_p));
		if (myNotePanel != null) {
			
			// pull up notes page
			if (myNotePanel.getVisibility() == View.GONE)
				openNotesList(myNotePanel);
			
			if (myNotePanel.getDisplayedChild() == 0)
				myNotePanel.showNext();

			
			// create a new note
			NoteEditFragment myNote = ((NoteEditFragment) getSupportFragmentManager().findFragmentByTag(
					NOTEEDIT_PANEL));
			if (myNote == null) {
				Log.d(LiteralWord.TAG, TAG + " - Why do i not exist?!");
			} else {
				myNote.onHandleIntent(new Intent());
			}
			
			Log.d(LiteralWord.TAG, TAG + " - Handling Intent");

		} else {
			Intent i = new Intent(this, NoteEdit.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
		}

	}
	
}
