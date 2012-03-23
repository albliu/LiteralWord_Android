package com.LiteralWord.Bible;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Application;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/* TODO :
 * MUST FIX
 * - Generate new Database for text selection
 * - download database from internet
 * 
 * MUST HAVE
 * - Verse adding screen for Memory Verse and Bookmarks
 * - Memory Verse play Screen (with Random too)
 * - parse multi verse selection for Memory Verse
 * - better Search string parsing
 * - cool design
 * 
 * NICE TO HAVE
 * - Reading Plan
 * - Use Notes List code (add dates and lines)
 * - add option to index if want better performance. 
 * - During copy text mode, we need to disable the gesture controls (work around done)
 * 
*/

public class LiteralWord extends Application {
	
	public static final String TAG="LiteralWord";
	public static final String KEY_ROWID = "_id";
	public static final String BOOK_CHAPTERS_ROWID = "num_chapter";
	public static final String BOOK_HUMAN_ROWID = "name";
	public static final String BOOKS_TABLE = "books";
	public static final String VERSES_TABLE = "verses";
	public static final String VERSES_BOOK_ROWID = "book";
	public static final String VERSES_NUM_ROWID = "num_verse";
	public static final String VERSES_TEXT_ROWID = "verse";
	public static final String VERSES_CHAPTERS_ROWID = "chapter";
	public static final String VERSES_HEADER_TAG = "header";
	public static final int HEADER_NONE = 0;
	public static final int HEADER_NORMAL = 1;

	
	public static BibleDataBaseHelper dbHelper;
	public static SQLiteDatabase myBible;
	public static ArrayList<String>  bookNames;
	public static ArrayList<Integer>  bookMaxChap;
	
	public static float textSize = 16;
	public static final int maxTextSize = 30;
	public static final int minTextSize = 6;
	
	// Book Category and Hash must match in Size!
	// must also match with array string in values string!
	public enum BookCategories {
		
		
		All, Pentateuch, History, Wisdom_and_Poetry, Major_Prophets, Minor_Prophets, 
		Gospels_and_Acts, Paul_Letters, General_Letters, FILTER_SIZE;
		
		private static final int[] BookCategoryHash = {0, 5, 17, 22, 27, 39, 44, 57, 66};
		
		
		static String getName(int c) {
			
			
			if (c == Pentateuch.ordinal()) return "Pentateuch";
			else if (c == History.ordinal()) return "History";
			else if (c == Wisdom_and_Poetry.ordinal()) return "Wisdom and Poetry";
			else if (c == Major_Prophets.ordinal()) return "Major Prophets";
			else if (c == Minor_Prophets.ordinal()) return "Minor Prophets";
			else if (c == Gospels_and_Acts.ordinal()) return "Gospels and Acts";
			else if (c == Paul_Letters.ordinal()) return "Paul's Letters";
			else if (c == General_Letters.ordinal()) return "General Letters";
			return "All";
		};
		
		static BookCategories getEnum(int c) {
			
			if (c == Pentateuch.ordinal()) return Pentateuch;
			else if (c == History.ordinal()) return History;
			else if (c == Wisdom_and_Poetry.ordinal()) return Wisdom_and_Poetry;
			else if (c == Major_Prophets.ordinal()) return Major_Prophets;
			else if (c == Minor_Prophets.ordinal()) return Minor_Prophets;
			else if (c == Gospels_and_Acts.ordinal()) return Gospels_and_Acts;
			else if (c == Paul_Letters.ordinal()) return Paul_Letters;
			else if (c == General_Letters.ordinal()) return General_Letters;
			
			return All;
			
		}
		static int getHashLow(int c) {
			if (c == All.ordinal()) return 0;
			return BookCategoryHash[c - 1];
		}
		static int getHashHigh(int c) {
			if (c == All.ordinal()) return getHashHigh(General_Letters.ordinal());
			return BookCategoryHash[c];
		}
		int getHashLow() {
			
			return getHashLow(this.ordinal());
		}
		int getHashHigh() {
			
			return getHashHigh(this.ordinal());
		}
		String getName() {
			return getName(this.ordinal());
		};
	}
	
	@Override
	public void onCreate() {
		Log.d(LiteralWord.TAG, TAG + " - onCreate");
		
		super.onCreate();
		
		
		
		setUpDataBase();

		// create the fields for our spinner
		bookNames = new ArrayList<String>();
		bookMaxChap = new ArrayList<Integer>();
		
		Cursor c = myBible.query(LiteralWord.BOOKS_TABLE, 
				new String[] {LiteralWord.KEY_ROWID, LiteralWord.BOOK_HUMAN_ROWID, LiteralWord.BOOK_CHAPTERS_ROWID}, 
				null, null, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {
			bookNames.add(c.getString(c.getColumnIndexOrThrow(BOOK_HUMAN_ROWID)));
			bookMaxChap.add(c.getInt(c.getColumnIndexOrThrow(BOOK_CHAPTERS_ROWID)));
			c.moveToNext();
		}
		c.close();
		
		
		
	}
	
	
	private void setUpDataBase() {

		if (dbHelper == null)
			dbHelper = new BibleDataBaseHelper(this);
		try {
			dbHelper.createDataBase();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");
		}

		try {
			dbHelper.openDataBase();
		}catch(SQLException sqle){

			throw sqle;
		}
		myBible = dbHelper.getReadableDatabase();
		
	}

	
	
		
	
}
