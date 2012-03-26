package com.LiteralWord.Bible;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Picture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.LiteralWord.Bible.utils.myContextMenu;
import com.LiteralWord.Bible.utils.myGestureListener;
import com.LiteralWord.Bible.utils.myGestureListener.GestureState;
import com.LiteralWord.Bible.utils.myGestureListener.myMotions;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;
import com.LiteralWord.Bible.utils.myVerseActionFunctions;
import com.LiteralWord.Bible.utils.myVerseActionFunctions.VersePair;


public class BibleReaderFragment extends Fragment implements OnTouchListener {
	
	static final public int DEFAULT_VIEW = 0;
	static final public int LITERARY_VIEW = DEFAULT_VIEW + 1;
	static final public int STUDY_VIEW = LITERARY_VIEW + 1;
	
	static final private String CUR_BOOK_TAG = "currBook";
	static final private String CUR_CHAP_TAG = "currChapter";
	static final private String CUR_MAX_CHAP_TAG = "currMaxChapter";
	static final private String CUR_MAX_VER = "currMaxV";
	static final private String CUR_BOOK_ID_TAG = "bookID";
	static final private String CUR_STYLE_TAG = "myStyle";
	static final private String CUR_TEXTSIZE_TAG = "tSize";
	static final private String CUR_SEL_VIS_TAG = "selVis";
	static final private String CUR_NAV_VIS_TAG = "navVis";
	static final private String CUR_DISPLAY_TAG = "txt";
	static final private String CUR_SCROLL_TAG = "scroll";
	static final private String CUR_HIGHLIGHT_TAG = "v_sel";
	
	private static String TAG = "BibleReaderFragment";
	private static SQLiteDatabase myBible;

	private int myStyle = DEFAULT_VIEW;
	
	private WebView displayBookPanel;
	private boolean loading = false;
	

	private String currBook = "Genesis";
	private int currBookId = 1;
	private int currChapter = 1; 
	private int currMaxChapter = 50;
	private int currMaxVerse = 31;
	private String currDisplay = "";
	private Vector<SearchVerses> searchVerse;
	private float textSize = LiteralWord.textSize;

	private class SearchVerses {
		public int verse = 0;
		public boolean search = false;
		
		public SearchVerses(int v, boolean s) {
			verse = v;
			search = s;
		}
		
		@Override
		public String toString() {
			return Integer.toString(verse);
		}
	}
	
	private Context myContext;
	private Activity myActivity;
	private View myView;
	private myGestureListener myGesture;

	// for saving webView state
	
	private float scrollY = 0;
	private boolean passage_dirty = false;
	
	// for book / chapter / verse
	private ListView BookList;
	private GridView VerseGrid, ChapGrid;
	private AlertDialog bdialog, vdialog, cdialog;
	
	// for selected Verses since it runs on different thread
	private verseSelectedArray verseSel;
	

	private Handler mHandler = new Handler();
	private View verse_sel_button;
	
	// for verse selection menu
	private AlertDialog verse_menu;
	private myContextMenu v_context_menu;
	private OnVerseActionListener myVerseAction;
	
	/****************************************
	 * Fragment Functions
	 **************************************/

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			myVerseAction = (OnVerseActionListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, "Bad class", e);
			throw new ClassCastException(activity.toString()
					+ " must implement OnVerseActionListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LiteralWord.TAG, TAG + " - onCreateView");

		myView = inflater.inflate(R.layout.bible_book, container, false);

		myActivity = getActivity();
		myContext = myView.getContext();

		myBible = LiteralWord.myBible;
		myGesture = new myGestureListener(myContext);

		setUpDisplay();
		setUpButtons();
		setUpSelection();

		searchVerse = new Vector<SearchVerses>();
		
		if (savedInstanceState != null) {
			textSize = savedInstanceState.getFloat(CUR_TEXTSIZE_TAG);
			displayBookPanel.getSettings().setDefaultFontSize((int) textSize);
			if (savedInstanceState.getInt(CUR_SEL_VIS_TAG) == View.GONE) {
				myView.findViewById(R.id.selection).setVisibility(View.GONE);
				myView.findViewById(R.id.show_sel).setBackgroundDrawable(
						getResources().getDrawable(R.drawable.ic_menu_more));
			}

			if (savedInstanceState.getInt(CUR_NAV_VIS_TAG) == View.GONE) {
				myView.findViewById(R.id.nav_menu).setVisibility(View.GONE);
				myView.findViewById(R.id.show_menu).setBackgroundDrawable(
						getResources()
								.getDrawable(R.drawable.ic_menu_more_side));
			}
			myStyle = savedInstanceState.getInt(CUR_STYLE_TAG);
			updatePassageVariables(savedInstanceState.getString(CUR_BOOK_TAG),
					savedInstanceState.getInt(CUR_BOOK_ID_TAG),
					savedInstanceState.getInt(CUR_MAX_CHAP_TAG),
					savedInstanceState.getInt(CUR_CHAP_TAG));

			currMaxVerse = savedInstanceState.getInt(CUR_MAX_VER);
			currDisplay = savedInstanceState.getString(CUR_DISPLAY_TAG);
			scrollY = savedInstanceState.getFloat(CUR_SCROLL_TAG);
			verseSel = new verseSelectedArray(
					savedInstanceState.getBooleanArray(CUR_HIGHLIGHT_TAG));

		} else {
			// check shared preferences
			SharedPreferences preferences = myActivity
					.getPreferences(Activity.MODE_PRIVATE);
			textSize = preferences.getFloat(CUR_TEXTSIZE_TAG, textSize);
			displayBookPanel.getSettings().setDefaultFontSize((int) textSize);
			if (preferences.getInt(CUR_SEL_VIS_TAG, View.VISIBLE) == View.GONE) {
				myView.findViewById(R.id.selection).setVisibility(View.GONE);
				myView.findViewById(R.id.show_sel).setBackgroundDrawable(
						getResources().getDrawable(R.drawable.ic_menu_more));
			}

			if (preferences.getInt(CUR_NAV_VIS_TAG, View.VISIBLE) == View.GONE) {
				myView.findViewById(R.id.nav_menu).setVisibility(View.GONE);
				myView.findViewById(R.id.show_menu).setBackgroundDrawable(
						getResources()
								.getDrawable(R.drawable.ic_menu_more_side));
			}
			myStyle = preferences.getInt(CUR_STYLE_TAG, myStyle);
			updatePassageVariables(
					preferences.getString(CUR_BOOK_TAG, currBook),
					preferences.getInt(CUR_BOOK_ID_TAG, currBookId),
					preferences.getInt(CUR_MAX_CHAP_TAG, currMaxChapter),
					preferences.getInt(CUR_CHAP_TAG, currChapter));

			currMaxVerse = preferences.getInt(CUR_MAX_VER, currMaxVerse);
			currDisplay = preferences.getString(CUR_DISPLAY_TAG, currDisplay);
			scrollY = preferences.getFloat(CUR_SCROLL_TAG, scrollY);
			
			verseSel = new verseSelectedArray(currMaxVerse);
		}

		handleIntent(getActivity().getIntent());
		return myView;
	}

	// we will always enter Fragment from handleIntent!!
	public void handleIntent(Intent I) {
		Log.d(LiteralWord.TAG, TAG + " handle Intent");
		if (myView == null)
			return;
		
		boolean restore = false;
		// this is added as a semi hack to handle cases where both new intent and create View is being called
		int searchV = 0;
		boolean isSearch = false;
		
		Bundle extras = I.getExtras();
		if (extras != null) {
			Cursor tmp = myBible.rawQuery(
					"SELECT * FROM " + LiteralWord.BOOKS_TABLE + " WHERE "
							+ LiteralWord.BOOK_HUMAN_ROWID + "='"
							+ extras.getString(LiteralWord.VERSES_BOOK_ROWID)
							+ "'", null);
			if (tmp.moveToFirst()) {
				isSearch = extras.getBoolean(BibleReader.SEARCH_TAG);
				searchV = extras.getInt(LiteralWord.VERSES_NUM_ROWID);
				
				updatePassageVariables(
						extras.getString(LiteralWord.VERSES_BOOK_ROWID),
						tmp.getInt(tmp
								.getColumnIndexOrThrow(LiteralWord.KEY_ROWID)),
						tmp.getInt(tmp
								.getColumnIndexOrThrow(LiteralWord.BOOK_CHAPTERS_ROWID)),
						extras.getInt(LiteralWord.VERSES_CHAPTERS_ROWID));

				
				
				scrollY = 0;

				// if it's a search in, clear all previous saved state content
				verseSel.clearPreserve();
				Log.d(LiteralWord.TAG,
						" Searhcing The book index = "
								+ Integer.toString(currBookId) + "; chapter ="
								+ Integer.toString(currChapter) + "; verse = "
								+ Integer.toString(searchV));
				
				currDisplay = "";
			}
			
			I.replaceExtras((Bundle) null);
/*			I.removeExtra(BibleReader.SEARCH_TAG);
			I.removeExtra(LiteralWord.VERSES_BOOK_ROWID);
			I.removeExtra(LiteralWord.VERSES_CHAPTERS_ROWID);
			I.removeExtra(LiteralWord.VERSES_NUM_ROWID);
*/			
			

		} 
		searchVerse.add(new SearchVerses(searchV, isSearch));
		
		if (!currDisplay.equals(""))
			restore = true;
		displayPassage(restore);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CUR_BOOK_TAG, currBook);
		outState.putInt(CUR_CHAP_TAG, currChapter);
		outState.putInt(CUR_MAX_CHAP_TAG, currMaxChapter);
		outState.putInt(CUR_MAX_VER, currMaxVerse);
		outState.putInt(CUR_BOOK_ID_TAG, currBookId);
		outState.putInt(CUR_STYLE_TAG, myStyle);
		
		outState.putFloat(CUR_TEXTSIZE_TAG, textSize);
		outState.putInt(CUR_SEL_VIS_TAG, myView.findViewById(R.id.selection).getVisibility());
		outState.putInt(CUR_NAV_VIS_TAG, myView.findViewById(R.id.nav_menu).getVisibility());
		outState.putString(CUR_DISPLAY_TAG, currDisplay);
		outState.putFloat(CUR_SCROLL_TAG, saveScroll());
		outState.putBooleanArray(CUR_HIGHLIGHT_TAG, verseSel.verseSel);
	}
	
    @Override
	public void onDestroy() {
    	Log.d(LiteralWord.TAG, "onDestroy ");
		super.onDestroy();
	}

	/***********************************************
	 * INIT / SETUP FUNCTIONS
	 ********************************************/
	
	private void setUpDisplay() {
		
		//flipPanel = (ViewSwitcher) myView.findViewById(R.id.flipper);

		displayBookPanel = (WebView) myView.findViewById(R.id.dispPanel);
		
		
		WebSettings webSettings = displayBookPanel.getSettings();
		
		webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setSupportZoom(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultFontSize((int) textSize);
		
        displayBookPanel.setOnTouchListener(this);
        displayBookPanel.addJavascriptInterface(new VerseJavaScriptInterface(myContext), "verse");
 

		displayBookPanel.setPictureListener(new afterScreenRefresh());
		
	}
	
	private void setUpSelection() {
		
		LayoutInflater myLI = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		
		BookList = (ListView) myLI.inflate(R.layout.book_select_list, null);
		// set the book spinner
		ArrayAdapter<String> bookAdapter = new ArrayAdapter<String>(myContext, android.R.layout.simple_list_item_1, LiteralWord.bookNames);
		//bookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		BookList.setAdapter(bookAdapter);

		BookList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				// if nothing changed, then just return
				bdialog.dismiss();
				if ((arg2 + 1) == currBookId) return ;
				// update the current Book
				
				updatePassageVariables(
						LiteralWord.bookNames.get(arg2), 
						arg2 + 1, 
						LiteralWord.bookMaxChap.get(arg2),
						1
						);
				Log.d(LiteralWord.TAG, " The book changed =" + currBook);
				
				displayPassage(false);
				
			}
		});
		
		bdialog = new AlertDialog.Builder(myContext).create();
		bdialog.setView(BookList);
 
		
		VerseGrid = (GridView) myLI.inflate(R.layout.selection_grids, null);
		VerseGrid.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				Log.d(LiteralWord.TAG, "going to verse! pos = " + (pos + 1));
				displayBookPanel.loadUrl("javascript:jumpToElement('"+ Integer.toString(pos + 1) + "')");
				vdialog.dismiss();
			}
		});
		vdialog = new AlertDialog.Builder(myContext).create();
		vdialog.setView(VerseGrid);
		
		
		
		ChapGrid = (GridView) myLI.inflate(R.layout.selection_grids, null);
		ChapGrid.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				cdialog.dismiss();
				if ((pos + 1) == currChapter) return;
				// update the current Book
				Log.d(LiteralWord.TAG, " The chapter changed =" + Integer.toString(currChapter));
				updatePassageVariables(currBook, currBookId, currMaxChapter, pos + 1);
				displayPassage(false);
				
			}
		});
		cdialog = new AlertDialog.Builder(myContext).create();
		cdialog.setView(ChapGrid);
	}
	
	private void setUpButtons() {
		
		myView.findViewById(R.id.zout).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				changeTextSize(textSize - 2);
				//setActive();
			}
		});

	
		myView.findViewById(R.id.zin).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				changeTextSize(textSize + 2);

				//setActive();
			}
		});
		
		

		
		myView.findViewById(R.id.nchap).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				if (rightPassage()) displayPassage(false);
				//setActive();
				
			}
		});
		
		myView.findViewById(R.id.pchap).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				if (leftPassage()) displayPassage(false);
				//setActive();
			}
		});

		verse_sel_button = myView.findViewById(R.id.vmenu);
		//registerForContextMenu(verse_sel_button);
		myView.findViewById(R.id.vmenu).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				
				if (verse_menu == null) {
					verse_menu = new AlertDialog.Builder(myContext).create();
					v_context_menu = new myContextMenu(myContext, verse_menu, R.layout.context_menu, new MenuSelector() {
						@Override
						public void onMenuItemSelected(int id) {
							onMenuIndexSelected(id);
						}
					});
					v_context_menu.setTitle("Selected Verses");
					v_context_menu.addButton(R.id.clear_hl, "Clear Highlights");
				}
				verse_menu.show();
				
			}
		});
		
		

		myView.findViewById(R.id.goto_verse).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {		
				
				vdialog.show();
			}
		});
		
		myView.findViewById(R.id.spinner_chapters).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {		
				
				cdialog.show();
			}
		});
		
		myView.findViewById(R.id.spinner_books).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {		
				
				bdialog.show();
			}
		});
		myView.findViewById(R.id.show_sel).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {	
				if (myView.findViewById(R.id.selection).getVisibility() == View.VISIBLE) {
					myView.findViewById(R.id.selection).setVisibility(View.GONE);
					v.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_more));
				} else {
					myView.findViewById(R.id.selection).setVisibility(View.VISIBLE);	
					v.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_close_clear_cancel));
				}
			}
		});
		
		myView.findViewById(R.id.show_menu).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {	
				toggleNavMenu();
			}
		});
		
	}
	
	
	/*******************************************
	 * DISPLAY FUNCTIONS
	 ************************************/

	class afterScreenRefresh implements PictureListener {

		@Override
		public void onNewPicture(WebView view, Picture picture) {

			if (loading) {
				// only care when we're done parsing
				return;
			}
			Log.d(LiteralWord.TAG, " new Picture");
			
			if (scrollY != 0) {
				Log.d(LiteralWord.TAG, " new Picture scrollY = " + scrollY);
				float webviewsize = view.getContentHeight() - view.getTop();
				float positionInWV = webviewsize * scrollY;
				int positionY = (int) (view.getTop() + positionInWV);
				Log.d(LiteralWord.TAG, " new Picture pos = " + positionY);
				
				// prevent it from going too high
				if (positionY < 0) positionY = 0;
				// prevent it from going too low 
				int myMaxY = view.getContentHeight() - view.getHeight();
				if (positionY > myMaxY) positionY = myMaxY;
				
				view.scrollTo(0, positionY);
				scrollY = 0;
			}
			
			if (!searchVerse.isEmpty()) {
				int ver = searchVerse.firstElement().verse;
				if (ver != 0) {
					Log.d(LiteralWord.TAG, " new Picture verse = " + searchVerse);
					view.loadUrl("javascript:jumpToElement('"
							+ Integer.toString(ver) + "')");
					if(searchVerse.firstElement().search)
						view.loadUrl("javascript:highlight('"
								+ Integer.toString(ver) + "')");
				
				}
				searchVerse.removeElementAt(0);
				
			}

			// highlight if needed!
			if (verseSel.isPreserve()) {
				for (int i = 1; i <= currMaxVerse; i++) {
					if (verseSel.verseSel[i])
						view.loadUrl("javascript:highlight("
								+ Integer.toString(i) + ")");
				}
				verseSel.clearPreserve();
			}

			Log.d(LiteralWord.TAG, " new Picture done");
		}

	}
	
	private void displayPassage(boolean restore){
		
		
		
		new disp().execute(restore);
		// change the button to show the right book and chapter we're on
		((Button) myView.findViewById(R.id.spinner_chapters)).setText(Integer.toString(currChapter));
		((Button) myView.findViewById(R.id.spinner_books)).setText(currBook);
		updateChapterSpinner();
		
		
		// in the background set up passage
		
		Log.d(LiteralWord.TAG, " Printing Book =" + currBook + " ; Chapter = " + Integer.toString(currChapter));

			// display passage
		

	}

	// moved the refresh function out for when we need to copy, to refresh the display if it's dirty
	void refreshPassage() {
		
		
		String style = "<html><head>";
		
		if (myStyle == LITERARY_VIEW)
			style += "<link href=\"literary.css\" rel=\"stylesheet\" type=\"text/css\" />";
		else if (myStyle == STUDY_VIEW)
			style += "<link href=\"study.css\" rel=\"stylesheet\" type=\"text/css\" />";
		else 
			style += "<link href=\"default.css\" rel=\"stylesheet\" type=\"text/css\" />";
		
		style += "<link href=\"body.css\" rel=\"stylesheet\" type=\"text/css\" /></head><script language=\"javascript\" type=\"text/javascript\" src=\"jumpTo.js\"></script><body>";
		
		displayBookPanel.loadDataWithBaseURL("file:///android_asset/", style
				+ currDisplay + "</body></html>", "text/html", "utf-8", null);
		passage_dirty = false;
		
	}

	class disp extends AsyncTask<Boolean, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			loading = true;
			displayBookPanel.loadData("loading...","text/html","utf-8");
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			updateVerseSelection();
			loading = false;
			refreshPassage();
			
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			super.onProgressUpdate(values);
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			
			boolean hl_tag = false;
			
			if (params[0]) {
				return true;
			}
			
			String text = "";
			Cursor c = myBible.query(LiteralWord.VERSES_TABLE, 
					new String[] {LiteralWord.KEY_ROWID, LiteralWord.VERSES_HEADER_TAG, LiteralWord.VERSES_NUM_ROWID, LiteralWord.VERSES_TEXT_ROWID}, 
					LiteralWord.VERSES_BOOK_ROWID + "=? AND " + LiteralWord.VERSES_CHAPTERS_ROWID + "=?", 
					new String[] {currBook, Integer.toString(currChapter)}, 
					null, 
					null, 
					null);	
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String passage = c.getString(c
						.getColumnIndexOrThrow(LiteralWord.VERSES_TEXT_ROWID));

				int ver = c.getInt(c.getColumnIndex(LiteralWord.VERSES_NUM_ROWID)); 
				if (c.getInt(c.getColumnIndex(LiteralWord.VERSES_HEADER_TAG)) == LiteralWord.HEADER_NONE)
				
				
					Log.d(LiteralWord.TAG, "getting verse: " + passage);
				
				passage = passage.replace("<br><vn>", "<vn>");
				
				passage = "<V id=\"" + Integer.toString(ver) 
						+ "\">" + passage + " <sv><br></sv></V>"; 
							//+ "\" onClick=\"select()\">" + passage + "</V>"; 
				if (passage.contains("<PM>")) { 
					//passage = "<p></p>" + passage;
					passage = passage.replace("<PM>", "<p></p>");
				}
				
				if (passage.contains("<dd>")) {
					if (!hl_tag) {
						passage = "<div class=\"HL\">" + passage;
						//passage = "<dd class=\"HL\">" + passage;
						hl_tag = true;
					}
				} else {
					if (hl_tag) {
						passage = "</div>" + passage;
						hl_tag = false;
					}
				}
				
				
				passage = passage.replace("<dt>", "<div style=\"margin-left:4em;\"><span style=\"margin-left:-2em;\">");
				passage = passage.replace("</dt>", "</span></div>");
				passage = passage.replace("<dd>", "");
				passage = passage.replace("</dd>", "<br>");
				
				text += passage;
				
				c.moveToNext();
				currMaxVerse = ver;
			}

			c.close();
			
			String copyright = "<br><br><br><cp>New American Standard Bible <br>Copyright (c) 1960, 1962, 1963, 1968, 1971, 1972, 1973, 1975, 1977, 1995 by The Lockman Foundation</cp><br>";
			currDisplay = text + copyright + "<br /><br />";
			
			
			return false;
		}

	};

	/**********************************
	 * UPDATE FUNCTIONS
	 **************************/
	
	private void updatePassageVariables(String book, int bookId, int maxChapter, int Chapter) {
		
		currBookId = bookId;	
		currBook = book;
		currMaxChapter = maxChapter; 
		currChapter = Chapter;
	}
	
	private void updateChapterSpinner() {
		Log.d(LiteralWord.TAG, " update chapter spinner =" + Integer.toString(currBookId) );

		int nChapters = LiteralWord.bookMaxChap.get(currBookId - 1);
		String [] from = new String[nChapters];
		for (int i=0; i < nChapters; i++)
			from[i] = Integer.toString(i+1);

		ArrayAdapter <String> chapter = new ArrayAdapter<String>(myContext, R.layout.grid_item, from);
		ChapGrid.setAdapter(chapter);
		
		

	}

	private void updateVerseSelection() {
		
		// HIGHLIGHTING
		// HERE we are making a big assumption that the passed in currMaxVerse and verseSel array
		// are going to be matching. verseSel[0] is the bit set when we are in initialization phase.
		// if it's the case, then saveInstanceState will have the correct currMaxVerse and verseSel
		// array, so we only need to set up the grid, and we shouldn't change the values in verseSel
		boolean preserve = (verseSel == null) ? false : verseSel.isPreserve();
		if (!preserve) 
			verseSel = new verseSelectedArray(currMaxVerse);
		myView.findViewById(R.id.vmenu).setVisibility(View.GONE);
		
		// SELECTION
		String [] verseArr  = new String[currMaxVerse];
		for (int i=0; i < currMaxVerse ; i++) {
			verseArr[i] = Integer.toString(i+1);
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(myContext,R.layout.grid_item,verseArr);
		VerseGrid.setAdapter(adapter);
		
	}
	
	
	/****************************************
	 * BIBLE READER EVENTS
	 *********************************/
	
	
    final public class VerseJavaScriptInterface {
    	
    	Context c;
    	Vector<Integer> arr;
    	Vector<Boolean> active;
		Vector<String> vtext;
        VerseJavaScriptInterface(Context ct) {
        	c = ct;
        	arr = new Vector<Integer>();
        	active = new Vector<Boolean>();
        	vtext = new Vector<String>();
        }

        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        public void displayPos(String x, String y) { 
        	Toast.makeText(c, "(" + Integer.parseInt(x) + "," + Integer.parseInt(y) + ")", Toast.LENGTH_SHORT).show();
        	
        }
        public void clickVerse(String v, String t, String c) {
        	//Log.d(LiteralWord.TAG, TAG + " - click verse " + v);
        	
        	arr.add(Integer.parseInt(v));
        	if (c.equals("active")) {
        		active.add(true);
        	} else {
        		active.add(false);
        	}
        	vtext.add(t);
        	mHandler.post(new Runnable() {

    			@Override
    			public void run() {	
    				if (arr.size() == 0) return;
    				
    				boolean hl = active.firstElement();
    				active.removeElementAt(0);
    				
    				int verse = arr.firstElement();
    				arr.removeElementAt(0);
    				
    				String txt = vtext.firstElement();
    				vtext.removeElementAt(0);
    				
    	        	if (hl) {
    	        		//Log.d(LiteralWord.TAG, TAG + " - " + verse + " : active");
    	        		verseSel.set(verse, true, txt);
    	        	} else {
    	        		//Log.d(LiteralWord.TAG, TAG + " - " + verse + " : cleared");
    	        		verseSel.set(verse, false, txt);
    	        	}    	
    			}
    		});  
        }  
    }
	
	private boolean leftPassage() {
		
		if ((currChapter - 1) == 0) {
			//switch books
			if (!currBook.equals("Genesis")) {
				
				int ind = currBookId - 1 -1; // minus 1 for position, minus 1 for previous book
				int maxChp = LiteralWord.bookMaxChap.get(ind);
				updatePassageVariables(
						LiteralWord.bookNames.get(ind), 
						(currBookId - 1),
						maxChp,
						maxChp
						);

			} else {
				// nothing changed
				return false;
			}

		} else {
			updatePassageVariables(
					currBook, 
					currBookId,
					currMaxChapter,
					currChapter - 1
					);
			
		}
		return true;
	}
	
	private boolean rightPassage() {
		
		
		if ((currChapter + 1) > currMaxChapter) {
			if (!currBook.equals("Revelation")) {
				int ind = currBookId; // plus 1 for next book, minus 1 for pos
				
				updatePassageVariables(
						LiteralWord.bookNames.get(ind), 
						(currBookId + 1),
						LiteralWord.bookMaxChap.get(ind),
						1
						);
				 
			} else {
				// nothing changed
				return false;
			}
		} else  {
			updatePassageVariables(
					currBook, 
					currBookId,
					currMaxChapter,
					currChapter + 1
					);
		}
		return true;
	}
	
	
	public void changeTextSize(float tSize) {
		
		if (myGesture.myState == GestureState.COPY) {
			return;
		}
		
		if (tSize > LiteralWord.maxTextSize)
			textSize = LiteralWord.maxTextSize;
		else if (tSize < LiteralWord.minTextSize)
			textSize = LiteralWord.minTextSize;
		else 
			textSize = tSize;
		
		passage_dirty = true;
		
		scrollY = saveScroll();
		Log.d(LiteralWord.TAG, "scale = " + scrollY);
		
		displayBookPanel.getSettings().setDefaultFontSize((int) textSize);

	}

	public void copyText() {
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)  {
			// if honeycomb, then Android webVIew itself handles text selection
			// TODO:
			// Figure out how to get notification when text selection is done...
			// temp hack: we need to lock the screen and prevent any gestures from happening when we are selecting text
			
			if (myView.findViewById(R.id.nav_menu).getVisibility() == View.GONE) toggleNavMenu();
			return;
		}
		
		myGesture.myState = GestureState.COPY;
		Log.d(LiteralWord.TAG, "BibleReader - select and copy");
		
		myView.findViewById(R.id.goto_verse).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_verse_highlight));
		Toast.makeText(displayBookPanel.getContext(), "Select text to be copied", Toast.LENGTH_SHORT).show(); 
		if (passage_dirty) {
			scrollY = saveScroll();
			refreshPassage();
		}
		myView.findViewById(R.id.zin).setEnabled(false);
		myView.findViewById(R.id.zout).setEnabled(false);
		myView.findViewById(R.id.goto_verse).setEnabled(false);
		
		KeyEvent shiftPressEvent = new KeyEvent(0,0,
             KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SHIFT_LEFT,0,0);
        shiftPressEvent.dispatch(displayBookPanel);
	}
	
	public boolean doneCopyText(boolean done) {
		// returns true if we were in copy state, or else returns false
		
		if (myGesture.myState == GestureState.COPY) {
			
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			
			// every version handles webView copy different!
			if (currentapiVersion > android.os.Build.VERSION_CODES.FROYO) {
				if (done) {
					KeyEvent shiftPressEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_SHIFT_LEFT,0,0);
					shiftPressEvent.dispatch(displayBookPanel);
				} else {
					
					KeyEvent shiftPressEvent = new KeyEvent(0,0, KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SPACE,0,0);
					shiftPressEvent.dispatch(displayBookPanel);
					KeyEvent shiftPressEvent2 = new KeyEvent(0,0, KeyEvent.ACTION_UP,KeyEvent.KEYCODE_SPACE,0,0);
					shiftPressEvent2.dispatch(displayBookPanel);
				}

			} else {
				
				if (!done) {								
					KeyEvent shiftPressEvent = new KeyEvent(0,0, KeyEvent.ACTION_UP,KeyEvent.KEYCODE_SHIFT_LEFT,0,0);
					shiftPressEvent.dispatch(displayBookPanel);						
				}
			}
			
			
			myGesture.myState = GestureState.NONE;
			myView.findViewById(R.id.goto_verse).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_verse));
			myView.findViewById(R.id.zin).setEnabled(true);
			myView.findViewById(R.id.zout).setEnabled(true);
			myView.findViewById(R.id.goto_verse).setEnabled(true);
			
	        return true;
		}
		return false;
	}
	
	/*************************************************
	 * GESTURE CODE
	 *****************************************/
	
	public boolean onTouch(View v, MotionEvent event) {
		
		
		myGesture.myDetector.onTouchEvent(event);
		
		myMotions m = myMotions.getMotion(event, myGesture.myState);
		
		//Log.d(LiteralWord.TAG, "BibleReader - Gesture = " + myGesture.myState.toString() + "; Motion = " + m.toString());

		boolean gest_state = false;
		if (myView.findViewById(R.id.nav_menu).getVisibility() == View.GONE) gest_state = true;
		
		switch (m) {
			case COPYING: 
				
				float myYevent = event.getY();
				int YHeight = displayBookPanel.getHeight() - 10;
				if (myYevent < 0) {
					int myY = displayBookPanel.getScrollY();
					if (myY != 0 ) { 
						if ((myY - 10) < 0)
							displayBookPanel.scrollTo(0, 0);
						else 
							displayBookPanel.scrollBy(0, -10);
					}
				} else if (myYevent >= YHeight) {
					
					int myY = displayBookPanel.getScrollY() + displayBookPanel.getTop();
					int myMaxY = displayBookPanel.getContentHeight() - displayBookPanel.getHeight();
					Log.d(LiteralWord.TAG, "BibleReader - X = " + event.getX() + "; Y = " + event.getY() + " ; maxY = " + myMaxY + " ; screenY = " + myY + " ; getHeight : " + displayBookPanel.getHeight());
					if (myY  != myMaxY) { 
						if ((myY + 20) > myMaxY)
							displayBookPanel.scrollTo(0, myMaxY);
						else 
							displayBookPanel.scrollBy(0, 20);
					}
				}
				
				break;
			case DOUBLE_T:
				Log.d(LiteralWord.TAG, TAG + "(X, Y) = (" + event.getX() + "," + event.getY() + ")");
				displayBookPanel.loadUrl("javascript:highlightPoint(" + Integer.toString((int) event.getX()) + "," + Integer.toString((int) event.getY()) + ")" );
				break;
			case SWIPE_LEFT:
				if (gest_state)
					if (rightPassage())
						displayPassage(false);
				break;
			case SWIPE_RIGHT:
				if (gest_state)
					if (leftPassage())
						displayPassage(false);
				break;
			case LONG_T:
					copyText();
				break;
			case ZOOM:
				if (gest_state)
					changeTextSize(myMotions.scale * textSize);
				break;
			case SINGLE_T:
				break;
			case NONE:
				if (myGesture.myState == GestureState.COPY)  {
					doneCopyText(true);			
				}
				break;
		}

		return false;
	}

	/********************
	 * Menus
	 *******************************/
	
	
	public interface OnVerseActionListener {
	    public void onVersesNotes(String book, int chapter, ArrayList<Integer> verse);
	}
	
	
	public boolean onMenuIndexSelected(int id) {
		
		// this is IMPORTANT! Because we assume the passed in ArrayList is in order!
		ArrayList<VersePair> sel = verseSel.toArrayList();
		ArrayList<Integer> vnum = verseSel.toArrayIntList();
		
		switch (id) {
		case R.id.copy_to_clipboard:
			clearHighlight();
			myVerseActionFunctions.onVersesClipboard(myContext, currBook, currChapter, sel);
			return true;
		case R.id.add_memory:
			clearHighlight();
			myVerseActionFunctions.onVersesMemory(myContext, currBook, currChapter, sel);
			return true;
		case R.id.add_note:
			clearHighlight();
			myVerseAction.onVersesNotes(currBook, currChapter, vnum);
			return true;
		case R.id.add_bookmark:
			clearHighlight();
			myVerseActionFunctions.onVersesBookMark(myContext, currBook, currChapter, sel);
			return true;
		case R.id.clear_hl:
			clearHighlight();
			return true;
		}
		
		
		return false;
		
	}

	/************************
	 * Helper Functions
	 ******************/
	private void clearHighlight() {
		// hackish here, but because the handler runs not in sync, so it won't clear the values correctly, we do a manual clean here
		for (int i = 1; i <= currMaxVerse; i++)
			if (verseSel.verseSel[i]) {
				
				displayBookPanel.loadUrl("javascript:unhighlight(" + Integer.toString(i) + ")");
			}	
	}
	
	private float saveScroll() {
		Log.d(LiteralWord.TAG, "save scroll = " + Integer.toString(displayBookPanel.getScrollY()));
		float positionTopView = displayBookPanel.getTop();
		float contentHeight = displayBookPanel.getContentHeight();
		float currentScrollPosition = displayBookPanel.getScrollY();
		return (currentScrollPosition - positionTopView) / contentHeight;
	}

	private void toggleNavMenu() {
		if (myView.findViewById(R.id.nav_menu).getVisibility() == View.VISIBLE) {
			myView.findViewById(R.id.nav_menu).setVisibility(View.GONE);
			myView.findViewById(R.id.show_menu).setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_more_side));
		} else {
			myView.findViewById(R.id.nav_menu).setVisibility(View.VISIBLE);	
			myView.findViewById(R.id.show_menu).setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_close_clear_cancel));
		}
	}
	
	public int toggleStyle() {
		
		if (myStyle == LITERARY_VIEW) myStyle = STUDY_VIEW;
    	else if (myStyle == STUDY_VIEW) myStyle = DEFAULT_VIEW;
    	else myStyle = LITERARY_VIEW;
    	scrollY = saveScroll();
    	refreshPassage();
    	
    	return myStyle;
	}
	
	public void savePlace() {
		
		SharedPreferences preferences = myActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor outState = preferences.edit();
		
		outState.putString(CUR_BOOK_TAG, currBook);
		outState.putInt(CUR_CHAP_TAG, currChapter);
		outState.putInt(CUR_MAX_CHAP_TAG, currMaxChapter);
		outState.putInt(CUR_MAX_VER, currMaxVerse);
		outState.putInt(CUR_BOOK_ID_TAG, currBookId);
		outState.putInt(CUR_STYLE_TAG, myStyle);
		
		outState.putFloat(CUR_TEXTSIZE_TAG, textSize);
		outState.putInt(CUR_SEL_VIS_TAG, myView.findViewById(R.id.selection).getVisibility());
		outState.putInt(CUR_NAV_VIS_TAG, myView.findViewById(R.id.nav_menu).getVisibility());
		outState.putString(CUR_DISPLAY_TAG, currDisplay);
		outState.putFloat(CUR_SCROLL_TAG, saveScroll());
		
		Log.d(LiteralWord.TAG, "save content scale = " + Float.toString(displayBookPanel.getScale()) + " scroll = " + Integer.toString(displayBookPanel.getScrollY()));

		
		outState.commit();
	}
	
	private class verseSelectedArray {
		public boolean[] verseSel; // 0 bit is used for preserve
		private int verseSelCount;
		public String[] verseTxt;
		
		public verseSelectedArray(int Ver) {
			verseTxt = new String[Ver + 1];
			verseSel = new boolean[Ver + 1];
			verseSel[0] = false;
			verseSelCount = 0;
		}
		
		public verseSelectedArray(boolean[] arr) {
			
			// initialize with existing array, set to preserve mode
			verseSel = arr;
			verseTxt = new String[arr.length];
			// preserve mode will be cleared after we highligh the verses
			verseSel[0] = true;
			
			// this will get updated when we highlight the verses if we are in preserve mode
			verseSelCount = 0;
	
		}
		
		public boolean isPreserve() {
			return verseSel[0];
			
		}
		public void clearPreserve() {
			verseSel[0] = false;
		}
		
		public void set(int index, boolean value, String txt) {
			if (index == 0) return;
			verseSel[index] = value;
			verseTxt[index] = txt;
			
			if (value) verseSelCount++;
			else verseSelCount--;
			
			if (verseSelCount == 0) verse_sel_button.setVisibility(View.GONE);
			else if (verseSelCount == 1)verse_sel_button.setVisibility(View.VISIBLE);
		}
		
		public ArrayList<VersePair> toArrayList() {
			ArrayList<VersePair> vP = new ArrayList<VersePair>();
			for (int i = 1; i < verseSel.length; i++) {
				if (verseSel[i]) {
					vP.add(new VersePair(i, verseTxt[i]));
				}
			}
			return vP;
		}
		
		public ArrayList<Integer> toArrayIntList() {
			ArrayList<Integer> vP = new ArrayList<Integer>();
			for (int i = 1; i < verseSel.length; i++) {
				if (verseSel[i]) {
					vP.add(i);
				}
			}
			return vP;
		}
	}
}