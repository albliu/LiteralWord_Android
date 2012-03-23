package com.LiteralWord.Bible.utils;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.VerseLists.VerseDbAdapter;

public class myVerseActionFunctions {
	
	static private final String TAG = "myVerseAction";

	static public class VersePair {
		public int num;
		public String txt;
		
		public VersePair (int n, String t) {
			num = n;
			txt = t;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return txt;
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public static void onVersesMemory(Context c, String book, int chapter, ArrayList<VersePair> verses) {
		Log.d(LiteralWord.TAG, TAG + " onVersesMemory");
		new DBwrite(c, book, chapter, VerseDbAdapter.MEMVERSE_TABLE).execute(verses);
	}

	@SuppressWarnings("unchecked")
	public static void onVersesBookMark(Context c, String book, int chapter,
			ArrayList<VersePair> verses) {
		Log.d(LiteralWord.TAG, TAG + " onVersesBookMark");
		// if we have multiple verses, we only bookmakr the first one
		ArrayList<VersePair> ver = new ArrayList<VersePair>();
		ver.add(verses.get(0));
		new DBwrite(c, book, chapter, VerseDbAdapter.BOOKMARK_TABLE).execute(ver);
		
	}
	
	public static void onVersesClipboard(Context c, String book, int chapter,
			ArrayList<VersePair> verses) {
		
		Toast.makeText(c, "Added to Clipboard", Toast.LENGTH_SHORT).show();
		for (int i = 0; i < verses.size() ; i++) 
			Log.d(LiteralWord.TAG, TAG + " Copied : " + book + " " + Integer.toString(chapter) + ":" + verses.get(i).num);
		
	}


	
	static class DBwrite extends AsyncTask<ArrayList<VersePair>, Integer, Boolean> {
		String book;
		int chapter;
		Context c;
		int t;
		public DBwrite(Context con, String b, int ch, int table) {
			c = con;
			book = b;
			chapter = ch;
			t = table;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (t == VerseDbAdapter.MEMVERSE_TABLE)
				Toast.makeText(c, "Added to Memory Verses", Toast.LENGTH_SHORT).show();
			else if (t == VerseDbAdapter.BOOKMARK_TABLE)
				Toast.makeText(c, "Added to Bookmarks", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(ArrayList<VersePair>... params) {
			
			String txt = "";
			ArrayList<VersePair> verses = params[0];
			ArrayList<Integer> vNums = new ArrayList<Integer>();
			for (int i = 0; i < verses.size(); i++) {
				vNums.add(verses.get(i).num);
				txt += verses.get(i).toString() + " ";
			}
			
			String vString = VerseListStringConverter.List2Verse(vNums);
			
			VerseDbAdapter myMemoryVerses = new VerseDbAdapter(c, t);
			myMemoryVerses.open();
			// TODO, open Bible database to get the text
			
			Log.d(LiteralWord.TAG, TAG + " Memorize : " + book + " " + Integer.toString(chapter) + ":" + vString);
			myMemoryVerses.addVerse(book, chapter, vString, txt);
			/*for (int i = 0; i < verses.size() ; i++) {
				Log.d(LiteralWord.TAG, TAG + " Memorize : " + book + " " + Integer.toString(chapter) + ":" + Integer.toString(verses.get(i)));
				myMemoryVerses.addVerse(book, chapter, Integer.toString(verses.get(i)), "hahah");
			}*/
			
			myMemoryVerses.close();
			return null;
		}

	};
}
