package com.LiteralWord.Bible.Notes;

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

import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.utils.myTitleBar;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;

public class NoteListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private static final int DELETE_ID = Menu.FIRST;
    
    private static String TAG = "NotepadListFragment";
    
    private static final String BUTTON_TAG = "del_but";
	
	private Context myContext;
	private SimpleCursorAdapter nList;
	private View myView;
	private MyDbAdapter mDbHelper;
	private boolean showDel = false;
	private myTitleBar mTBar;
	
	private OnNoteSelectedListener myNoteListener; 
	public interface OnNoteSelectedListener {
	    public void onNoteSelected(long id);
	    public void onNoteCreate();
	}
	
	  @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            myNoteListener = (OnNoteSelectedListener) activity;
	        } catch (ClassCastException e) {
	            Log.e(TAG, "Bad class", e);
	            throw new ClassCastException(activity.toString()
	                    + " must implement OnNoteSelectedListener");
	        }
	    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			showDel = savedInstanceState.getBoolean(BUTTON_TAG);
		}
		
		
		

        String[] from = new String[]{MyDbAdapter.KEY_TITLE, MyDbAdapter.KEY_ROWID};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.delete_note};
        
        nList = new SimpleCursorAdapter(myView.getContext(), R.layout.notes_row, null, from, to, 0);
        setListAdapter(nList);
        
        getLoaderManager().initLoader(0, null, this);
        
        nList.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (view.getId() == R.id.delete_note) {
					if (showDel) {
						view.setVisibility(View.VISIBLE);
						view.setOnClickListener(new deleteNote(cursor.getLong(columnIndex)));
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


	private class deleteNote implements OnClickListener {
		long id;
		public deleteNote(long i) {
			id = i;
		}
		@Override
		public void onClick(View v) {
			mDbHelper.deleteNote(id);
			fillData();
		}
		
	}

	@Override
	public void onResume() {
		
		super.onResume();
		fillData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		myView = inflater.inflate(R.layout.list_fragment, container, false);
		myContext = myView.getContext();
		mDbHelper = new MyDbAdapter(myContext);
        mDbHelper.open();

        mTBar = new myTitleBar(myContext, myView.findViewById(R.id.action_bar), new MenuSelector() {
			
			@Override
			public void onMenuItemSelected(int id) {
				switch (id) {
				case R.id.new_note:
					myNoteListener.onNoteCreate();
					break;
				case R.id.delete:
					showDel = !showDel;
					fillData();
					break;
				}
				
			}
		});
      
        mTBar.setTitle("My Notes");
        mTBar.addButton(R.id.new_note, android.R.drawable.ic_menu_add);
		mTBar.addButton(R.id.delete, android.R.drawable.ic_menu_delete);
		return myView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(BUTTON_TAG, showDel);
	}

	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(LiteralWord.TAG, TAG + " -- onCreateLoader");
		
		return new CursorLoader(myView.getContext()) {

			@Override
			public Cursor loadInBackground() {
			
				return mDbHelper.fetchAllNotes();
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
        super.onListItemClick(l, v, position, id);
        myNoteListener.onNoteSelected(id);
/*        
*/    }
	

	
    public void fillData() {
    	getLoaderManager().restartLoader(0, null, this);
        
    }
}
