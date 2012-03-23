package com.LiteralWord.Bible.VerseLists;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.utils.myTitleBar;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;

public class VersesListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final  String TAG = "VerseListFragment";

	private static final int DELETE_ID = Menu.FIRST;
	private static final String BUTTON_TAG = "del_but";
	 
	private Context myContext;
	private SimpleCursorAdapter nList;
	protected myTitleBar mTBar;
	protected View myView;
	protected VerseDbAdapter myVerses;
	private boolean showDel = false;
	private int t;
	private OnVerseSelectedListener listener;

	public interface OnVerseSelectedListener {
	    public void onVersesSelected(long id ,String book, int chapter, String verse);
	}
	
	
	public VersesListFragment(int table) {
		t = table;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnVerseSelectedListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, "Bad class", e);
			throw new ClassCastException(activity.toString()
					+ " must implement OnVerseSelectedListener");
		}
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		myView = inflater.inflate(R.layout.list_fragment, container, false);
				
		((TextView) myView.findViewById(android.R.id.empty)).setText("No Verses Yet");
		myContext = myView.getContext();
		myVerses = new VerseDbAdapter(myContext, t);
		myVerses.open();
		
		mTBar = new myTitleBar(myContext, myView.findViewById(R.id.action_bar), new MenuSelector() {
			
			@Override
			public void onMenuItemSelected(int id) {
				switch (id) {
				case R.id.new_note:
					
					break;
				case R.id.delete:
					showDel = !showDel;
					fillData();
					break;
				}
				
			}
		});
		mTBar.addButton(R.id.new_note, android.R.drawable.ic_menu_add);
		mTBar.addButton(R.id.delete, android.R.drawable.ic_menu_delete);
		if (t == VerseDbAdapter.MEMVERSE_TABLE)
			mTBar.setTitle("Memory Verses");
		else if (t == VerseDbAdapter.BOOKMARK_TABLE)
			mTBar.setTitle("Bookmarks");
		return myView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			showDel = savedInstanceState.getBoolean(BUTTON_TAG);
		}
		
        Log.d(LiteralWord.TAG, TAG + t + " - onActivityCreated");
        
        // Create an empty adapter we will use to display the loaded data.
    
     	String[] from = new String[]{LiteralWord.VERSES_BOOK_ROWID, LiteralWord.VERSES_CHAPTERS_ROWID, LiteralWord.VERSES_NUM_ROWID, VerseDbAdapter.KEY_ROWID};   			
     	int[] to = new int[]{R.id.result_book,  R.id.result_chapter, R.id.result_verse, R.id.delete_note};

		nList = new SimpleCursorAdapter(myView.getContext(), R.layout.verse_row,
				null, from, to, 0);
		setListAdapter(nList);

		getLoaderManager().initLoader(0, null, this);

		nList.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (view.getId() == R.id.delete_note) {
					if (showDel) {
						view.setVisibility(View.VISIBLE);
						view.setOnClickListener(new deleteVerse(cursor.getLong(columnIndex)));
					}
					else {
						view.setVisibility(View.GONE);
					}
					return true;
				}
				return false;
			}
		});
		registerForContextMenu(getListView());
	}

	private class deleteVerse implements OnClickListener {
		long id;
		public deleteVerse(long i) {
			id = i;
		}
		@Override
		public void onClick(View v) {
			myVerses.deleteVerse(id);
			fillData();
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(BUTTON_TAG, showDel);
	}

	
	@Override
	public void onResume() {

		super.onResume();
		fillData();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(LiteralWord.TAG, TAG + t + " -- onCreateLoader");
		
		return new CursorLoader(myView.getContext()) {

			@Override
			public Cursor loadInBackground() {
			
				return myVerses.fetchAllverses();
			}
			
			
		};
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		nList.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
		nList.swapCursor(null);
	}



    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	String book = ((TextView) v.findViewById(R.id.result_book)).getText().toString();
        int chapter = Integer.parseInt(((TextView) v.findViewById(R.id.result_chapter)).getText().toString());
        String verse = ((TextView) v.findViewById(R.id.result_verse)).getText().toString();

        listener.onVersesSelected(id, book, chapter, verse);
        
    }
	

	
    public void fillData() {
    	getLoaderManager().restartLoader(0, null, this);
        
    }
    


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.verse_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                myVerses.deleteVerse(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }


	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
    
 /*   
    public long addVerses(String book, int chapter, String verses, String text) {
    	return myVerses.addVerse(book, chapter, verses, text);
    }
 */
    
}
