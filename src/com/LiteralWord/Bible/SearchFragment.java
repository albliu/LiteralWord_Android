package com.LiteralWord.Bible;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.LiteralWord.Bible.BibleReaderFragment.OnVerseActionListener;
import com.LiteralWord.Bible.LiteralWord.BookCategories;
import com.LiteralWord.Bible.utils.myContextMenu;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;
import com.LiteralWord.Bible.utils.myVerseActionFunctions;
import com.LiteralWord.Bible.utils.myVerseActionFunctions.VersePair;

public class SearchFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "SearchFragment";
	private final String STR_TAG = "query";
	private final String CAT_TAG = "Category";
	private final String BOOK_TAG = "Book";
	private final String CAT_ARRAY_TAG = "CategoryArray";
	private final String BOOK_ARRAY_TAG = "BookArray";
	private final String SHOWING_TAG = "vis";
	private final String EDITBOX_TAG = "edit box";
	
	private SQLiteDatabase myBible;
	
	// views
	private EditText mSearchString;
	private SimpleCursorAdapter passage;
	private View FilterBar;
	private TextView count_box;

	private Button cat_spinner, book_spinner;
	private AlertDialog cat_sel, book_sel;
	private ListView cat_list, book_list;
	// what's in the List View
	private ArrayList<String> CategoryArray, BookArray;
	private int cat_pos, book_pos;
	
	
	// current selected, these need to be updated when new search comes in
	private int [] cat_count, book_count;
	public BookCategories currCategory = BookCategories.All;
	public int currBook = 0;
	
	
	private InputMethodManager imm;
	
	
	// display view
	private boolean showingFilter = false;

	private ProgressDialog dialog;
	// count
	private ArrayList<Integer> catspin_num;
	private ArrayList<Integer> bookspin_num;
	
	//query init with random string so search result will be 0
	
	private String q = "ASDASFASDSAF";
	private myViewBinder mbinder = new myViewBinder(q);
	
	private View myView;
	private Context myContext;

	// for the Context menu
	private myContextMenu v_context_menu;
	
	private OnSearchSelectedListener mySearchListener; 
	private OnVerseActionListener myVerseAction;
	
	
	public interface OnSearchSelectedListener {
	    public void onSearchSelected(String book, int chapter, int verse);
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d(LiteralWord.TAG, TAG + " - onCreateView");
		myView = inflater.inflate(R.layout.search_page, container, false);
		myContext = myView.getContext();
	    
		cat_count = new int [BookCategories.FILTER_SIZE.ordinal()];
		book_count = new int [LiteralWord.bookNames.size() + 1]; 
		catspin_num = new ArrayList<Integer>();
		bookspin_num = new ArrayList<Integer>();
	    cat_pos = 0;
	    book_pos = 0;
	    
	    dialog = new ProgressDialog(myContext);
	    dialog.setMessage("Searching...");
	    
	    setUpEditText();
        setUpButton();
        setUpFilterBar(showingFilter);
        
        myBible = LiteralWord.myBible;

        Log.d(LiteralWord.TAG, TAG + " - onCreate text = " + mSearchString.getEditableText().toString());
        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchString.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		
		return myView;
	}

	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        
        if (savedInstanceState != null) {
        	Log.d(LiteralWord.TAG, TAG + " - saved onActivityCreated");
        	mSearchString.setText(savedInstanceState.getString(EDITBOX_TAG));
        	q = savedInstanceState.getString(STR_TAG);
        	mSearchString.setText(q);
        	cat_count = savedInstanceState.getIntArray(CAT_ARRAY_TAG);
        	book_count = savedInstanceState.getIntArray(BOOK_ARRAY_TAG);
        	
        	updateSpinners(savedInstanceState.getInt(CAT_TAG), savedInstanceState.getInt(BOOK_TAG));
    		 	
        	showingFilter = savedInstanceState.getBoolean(SHOWING_TAG);
        	toggleFilterBar(showingFilter);
        	
        }
        
        
        Log.d(LiteralWord.TAG, TAG + " - onActivityCreated");
        
        // Create an empty adapter we will use to display the loaded data.
    
     	String[] from = new String[]{LiteralWord.VERSES_BOOK_ROWID, LiteralWord.VERSES_CHAPTERS_ROWID, LiteralWord.VERSES_NUM_ROWID , LiteralWord.VERSES_TEXT_ROWID};   			
     	int[] to = new int[]{R.id.result_book,  R.id.result_chapter, R.id.result_verse, R.id.result_passage};
        
/*        passage = new SimpleCursorAdapter(myView.getContext(),
        		R.layout.search_result, null,
                from,
                to);
*/      
     	passage = new SimpleCursorAdapter(myView.getContext(), R.layout.search_result, null, from, to, 0);
     	
     	passage.setViewBinder(mbinder);
        
        setListAdapter(passage);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
        setUpContextMenu();
    }

	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		outState.putString(STR_TAG, q);
		outState.putIntArray(CAT_ARRAY_TAG, cat_count);
		outState.putIntArray(BOOK_ARRAY_TAG, book_count);
		outState.putInt(CAT_TAG, currCategory.ordinal());
		outState.putInt(BOOK_TAG, currBook);
		outState.putString(EDITBOX_TAG, mSearchString.getText().toString());
		outState.putBoolean(SHOWING_TAG, showingFilter);
	}

	public void performSearch(String s) {
		q = s;
		mSearchString.setText(q.toCharArray(), 0, q.length() );
		doMySearch(s);
	}
	
	
	  @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            mySearchListener = (OnSearchSelectedListener) activity;
	            myVerseAction = (OnVerseActionListener) activity;
	        } catch (ClassCastException e) {
	            Log.e(TAG, "Bad class", e);
	            throw new ClassCastException(activity.toString()
	                    + " must implement OnSearchSelectedListener and OnVerseSelevtedListener");
	        }
	    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String book = ((TextView) v.findViewById(R.id.result_book)).getText().toString();
        int chapter = Integer.parseInt(((TextView) v.findViewById(R.id.result_chapter)).getText().toString());
        int verse = Integer.parseInt(((TextView) v.findViewById(R.id.result_verse)).getText().toString());
        mySearchListener.onSearchSelected(book, chapter, verse);
	}

	
	
	private void doMySearch(String query) {
		if (!parseSearchString(query)) return;
		
		dialog.show();
		updateFilter(q);
		displayMySearch();
	}
	

	private boolean parseSearchString(String search) {
		if (search == " ") return false;
		q = search;
		mbinder.changeQuery(q);
		return true;
		
	}
	
	private String generateQueryString(BookCategories fCategory, int fBook, String text) {
		String query = "SELECT * FROM " + LiteralWord.VERSES_TABLE + " WHERE ";
		
		if (fBook != 0) {
			// if given a spefic book
			query += LiteralWord.VERSES_BOOK_ROWID + "='" + LiteralWord.bookNames.get(fBook - 1) + "' AND " ;
		} else {
			if (fCategory != BookCategories.All) {
				query += LiteralWord.VERSES_BOOK_ROWID + " IN (";
				int i = fCategory.getHashLow() + 1;
				for (; i < fCategory.getHashHigh(); i++) 
					 query += "'" + LiteralWord.bookNames.get(i - 1) + "',";
				query += "'" + LiteralWord.bookNames.get(i - 1) + "') AND ";
			}
		}

		query += LiteralWord.VERSES_TEXT_ROWID + " LIKE '%" + text + "%' AND " + LiteralWord.VERSES_HEADER_TAG + "=" + LiteralWord.HEADER_NONE;
		Log.d(LiteralWord.TAG, TAG + " -- setting up query :" + query );
		
		return query;
		
		
		
	}
	

	public static class myViewBinder implements ViewBinder {
		String query;
		
		public myViewBinder(String q) {
			query = q;
		}
		
		public void changeQuery(String q) {
			query = q;
		}
		
		//TODO Figure out how to not care about Capatalization =)
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			switch (view.getId()) {
			case R.id.result_book:
			case R.id.result_chapter:
			case R.id.result_verse:
				((TextView) view).setText(cursor.getString(columnIndex));
				((TextView) view).setTextSize(LiteralWord.textSize * 3 / 4);
				return true;
			case R.id.result_passage:
				
				//TODO: convert to correct format, figure out how to outline the word
				String temp = cursor.getString(columnIndex);
				if (query != null)
					temp = temp.replaceAll(query, "<u>" + query + "</u>");
				temp = temp.replaceAll("<fn>", "<!--");
				temp = temp.replaceAll("</fn>", "-->");
				temp = temp.replaceAll("<h1", "<!--");
				temp = temp.replaceAll("</h1>", "-->");
				temp = temp.replaceAll("<vn>", "<!--");
				temp = temp.replaceAll("</vn>", "-->");
				temp = temp.replaceAll("<sv>", "<!--");
				temp = temp.replaceAll("</sv>", "-->");
				
				//Log.d(LiteralWord.TAG, temp);
				((TextView) view).setTextSize(LiteralWord.textSize);
				((TextView) view).setText(Html.fromHtml(temp));
				
				return true;
		}
		
		return false;
	}
		
		
		
	}
	
	
	
	
	
	/********************
	 * Set Up FUnctions 
	 ***********************/
	
	private void setUpContextMenu() {
		
	   
	    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int arg2, long arg3) {
				
				AlertDialog	verse_menu = new AlertDialog.Builder(v.getContext()).create();
				v_context_menu = new myContextMenu(v.getContext(), verse_menu, R.layout.context_menu, new myMenu(v));
				v_context_menu.setTitle("Selected Verses");
				
				verse_menu.show();
				return true;
			}
		});
		
	}
	
	private class myMenu implements MenuSelector {
		View v;
		
		myMenu(View myView) {
			v = myView;
		}
		
		@Override
		public void onMenuItemSelected(int id) {
			
			String book = ((TextView) v.findViewById(R.id.result_book)).getText().toString();
			int chapter = Integer.parseInt(((TextView) v.findViewById(R.id.result_chapter)).getText().toString());

			ArrayList<VersePair> ver = new ArrayList<VersePair>();
			ver.add(new VersePair(Integer.parseInt(((TextView) v.findViewById(R.id.result_verse)).getText().toString()), 
					((TextView) v.findViewById(R.id.result_passage)).getText().toString()));
			
			ArrayList<Integer> sel = new ArrayList<Integer>();
			sel.add(ver.get(0).num);
			
			Log.d(LiteralWord.TAG, TAG + " menu text selected : " + ver.get(0));
			
			switch (id) {
			case R.id.copy_to_clipboard:
				myVerseActionFunctions.onVersesClipboard(myContext, book, chapter, ver);
				break;
			case R.id.add_memory:
				myVerseActionFunctions.onVersesMemory(myContext, book, chapter, ver);
				break;
			case R.id.add_bookmark:
				myVerseActionFunctions.onVersesBookMark(myContext, book, chapter, ver);
				break;
			case R.id.add_note:
				myVerseAction.onVersesNotes(book, chapter, sel);
				break;
			}
			
		}
		
		
	}
	
	private void setUpButton() {
		Button search = (Button) myView.findViewById(R.id.search_go);
		search.setOnClickListener(new Button.OnClickListener()
		{

			public void onClick(View v) {
				//imm.hideSoftInputFromWindow(mSearchString.getWindowToken(), 0);
				doMySearch(mSearchString.getText().toString());
				
			}

		});

		Button filter = (Button) myView.findViewById(R.id.search_filter);
		filter.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				if (!showingFilter) {
					// append filter bar
					showingFilter = true;
					toggleFilterBar(true);
				} else {
					//remove filter bar
					showingFilter = false;
					toggleFilterBar(false);
					
				}
			}

		});
		
	}
	
	private void setUpEditText() {
		mSearchString = (EditText) myView.findViewById(R.id.search_text);
        mSearchString.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if (mSearchString.getText().toString().equals(getResources().getText(R.string.search_bible)))
//					mSearchString.setText("");
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						
						imm.hideSoftInputFromWindow(mSearchString.getWindowToken(), 0);
						doMySearch(mSearchString.getText().toString());
						return true;
					}
				}
				return false;
			}
		});
		
	}
	
	public void setUpFilterBar(boolean showing) {
		
		LayoutInflater myLI = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		FilterBar = myView.findViewById(R.id.filter_bar);
		
		// set up Alert
		cat_list = (ListView) myLI.inflate(R.layout.book_select_list, null);
		cat_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				cat_sel.dismiss();
				BookCategories nCat;
				try {
					int index = catspin_num.get(pos);
					nCat = BookCategories.getEnum(index);
				} catch (Exception e) {
					return;
				}
				
				if (nCat.ordinal() == currCategory.ordinal()) return;
				currCategory = nCat;
				cat_pos = pos;
				updateFilterBookSpinner(0);
				displayMySearch();
				
			}
		});
		cat_sel = new AlertDialog.Builder(myContext).create();
		cat_sel.setView(cat_list);
		
		// set up Button
		cat_spinner = (Button) FilterBar.findViewById(R.id.filter_category);
		cat_spinner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cat_sel.show();
				
			}
		});
		
		book_spinner = (Button)  FilterBar.findViewById(R.id.filter_books);
		book_spinner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				book_sel.show();
				
			}
		});
		
		book_list = (ListView) myLI.inflate(R.layout.book_select_list, null);
		book_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				book_sel.dismiss();
				try {
					int index = bookspin_num.get(pos);
					if (currBook == index) return;
					currBook = index;
				} catch (Exception e) {
					return ;
				}
				book_pos = pos;
				displayMySearch();
				
			}
		});
		book_sel = new AlertDialog.Builder(myContext).create();
		book_sel.setView(book_list);
		
		// should update everything to 0
		updateSpinners(0, 0);
		
		count_box = (TextView) FilterBar.findViewById(R.id.search_count);
		count_box.setText(Integer.toString(0) + "\nresults");
		toggleFilterBar(showing);
	}
	
	/********************
	 * Update FUnctions 
	 ***********************/

	void updateFilter(String query) {
		Cursor c;
		Log.d(LiteralWord.TAG, TAG + " - update Filter Bar count");
		for (int i = 0; i < cat_count.length ; i++) cat_count[i] = 0;
		for (int i = 0; i < book_count.length ; i++) book_count[i] = 0;

		c = myBible.rawQuery("SELECT " + LiteralWord.VERSES_BOOK_ROWID + ", COUNT(*) TotalCount FROM " + 
				LiteralWord.VERSES_TABLE + 
				" WHERE "  + LiteralWord.VERSES_TEXT_ROWID + " LIKE '%" + q + "%' AND " + LiteralWord.VERSES_HEADER_TAG + "=" + LiteralWord.HEADER_NONE + 
				" GROUP BY " + LiteralWord.VERSES_BOOK_ROWID, null);
		//Log.d(LiteralWord.TAG, TAG + " --  Book[" + Integer.toString(i) + "] query done ");
		c.moveToFirst();
		while (!c.isAfterLast()) { 

			book_count[LiteralWord.bookNames.indexOf(c.getString(0)) + 1] = c.getInt(1);
			//Log.d(LiteralWord.TAG, TAG + " --  Book[" + c.getString(0) + "] = " + c.getInt(1));
			c.moveToNext();
		}
		//Log.d(LiteralWord.TAG, TAG + " --  Book[" + Integer.toString(i) + "] count done ");
		c.close();
		int j = 1;

		for (int i = 1; i < cat_count.length; i++) {
			for (; j <= BookCategories.getHashHigh(i); j++)
				cat_count[i] += book_count[j];
			cat_count[0] += cat_count[i];
		}

	
		updateSpinners(0, 0);

	}

	void updateSpinners(int cCat, int cBook) {
		Log.d(LiteralWord.TAG, TAG + " - update Filter Bar spinners");
		//update Spinner 1
		
		currCategory = BookCategories.getEnum(cCat);
		catspin_num.clear();
		CategoryArray = new ArrayList<String>();
		CategoryArray.add(BookCategories.getName(0) + " ( " + Integer.toString(cat_count[0]) + " )");
		catspin_num.add(0);
		cat_pos = 0;
		
		int y = 1;
		for (int i = 1; i < cat_count.length; i++) {

			if (cat_count[i] != 0) {
				if (i == cCat) cat_pos = y;
				CategoryArray.add(BookCategories.getName(i) + " ( " + Integer.toString(cat_count[i]) + " )");
				catspin_num.add(i);
				y++;
			}
		}
		
		ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(myView.getContext(), android.R.layout.simple_list_item_1, CategoryArray); 
		cat_list.setAdapter(catAdapter);
			
		updateFilterBookSpinner(cBook);



	}

	void updateFilterBookSpinner(int cBook) {

		currBook = cBook;
		bookspin_num.clear();
		book_count[0] = cat_count[currCategory.ordinal()];
		BookArray = new ArrayList<String>();
		BookArray.add("All (" + book_count[0] + ")");
		bookspin_num.add(0);
		book_pos = 0;
		int y = 1;
		for (int i = (currCategory.getHashLow() + 1); i <= currCategory.getHashHigh(); i++) {
			
			if (book_count[i] != 0) {
				if (i == cBook) book_pos = y;
				BookArray.add(LiteralWord.bookNames.get(i - 1) + " ( " + Integer.toString(book_count[i]) + " )");
				bookspin_num.add(i);
				y++;
			}
		}
		
		ArrayAdapter<String> bookAdapter = new ArrayAdapter<String>(myView.getContext(), android.R.layout.simple_list_item_1, BookArray);
		book_list.setAdapter(bookAdapter);
		

		

	}

	/********************
	 * Display Search Functions
	 ********************/
	
	private void toggleFilterBar(boolean show) {
		
		FilterBar.setVisibility(show ? View.VISIBLE : View.GONE);
		if (show)
			((Button) myView.findViewById(R.id.search_filter)).setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_sort_by_size_push));
		else 
			((Button) myView.findViewById(R.id.search_filter)).setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_sort_by_size));
	}
	
	private void displayMySearch() {	
		Log.d(LiteralWord.TAG, TAG + " -- displaying Search " + currCategory.getName() + " : " + Integer.toString(currBook) + " : " + q);
		getLoaderManager().restartLoader(0, null, this);
	}
	
	
	/********************
	 * Cursor Functions
	 ********************/
	
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		Log.d(LiteralWord.TAG, TAG + " -- onCreateLoader");
		
		return new CursorLoader(myView.getContext()) {

			@Override
			public Cursor loadInBackground() {
			
				return myBible.rawQuery(generateQueryString(currCategory, currBook, q), null);
			}
			
			
		};
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		Log.d(LiteralWord.TAG, TAG + " -- onLoadFinished");
		passage.swapCursor(arg1);
		count_box.setText(Integer.toString(book_count[currBook]) + "\nresults");
		cat_spinner.setText(CategoryArray.get(cat_pos));
		book_spinner.setText(BookArray.get(book_pos));
		if (dialog.isShowing())
			dialog.dismiss();
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(LiteralWord.TAG, TAG + " -- onLoaderReset");
		
		passage.swapCursor(null);
	}

	/********************
	 * Other Functions
	 ********************/
	
	@Override
	public void onDestroyView() {
		Log.d(LiteralWord.TAG, TAG + " - onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		
		Log.d(LiteralWord.TAG, TAG + " - onDestroy");
		super.onDestroy();
	}
	
}