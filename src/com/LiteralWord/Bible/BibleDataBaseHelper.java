package com.LiteralWord.Bible;


import java.io.IOException;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class BibleDataBaseHelper extends SQLiteOpenHelper
{
	private static String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	private static String DB_NAME = "nasb.db";
	private SQLiteDatabase myDataBase;

	
	
	public BibleDataBaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		
	}

	/*
	 * Creates an empty database on the system and rewrites it with your own database
	 */
	public void createDataBase() throws IOException
	{
		boolean dbExist = checkDataBase();
		
		if (dbExist) {
			// do nothing
		}
		else {
			//TODO need to handle this better	
			try {
				
				makeDataBase();
			} catch (Exception e) {
				
				throw new Error("Error downloading database");
			}
		}

	
	}

	private void makeDataBase() throws Exception {
		Log.d(LiteralWord.TAG, "downloading database");
	}


	private boolean checkDataBase() {
		
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);	
		} catch (SQLiteException e) {
			Log.d(LiteralWord.TAG, "Database doesn't exist");
		}
		
		if (checkDB != null) {
			checkDB.close();
			return true;
		}
		
		return false;
	}

	public boolean indexDataBase(boolean c) throws SQLException {
		// make sure database is opened
		openDataBase();
		
		try {
			if (c)
				myDataBase.execSQL("CREATE INDEX all_verses ON " + LiteralWord.VERSES_TABLE + "(" + LiteralWord.VERSES_TEXT_ROWID + ")");
			else 
				myDataBase.execSQL("DROP INDEX all_verses");
		} catch (SQLException e) {
			if (c)
				Log.d(LiteralWord.TAG, "BibleDataBaseHelper -- already indexed");
			else
				Log.d(LiteralWord.TAG, "BibleDataBaseHelper -- indexed doesn't exist");

			return false;
		}
		
		return true;
	}
	public void openDataBase() throws SQLException {
		//Open the database
        if (myDataBase == null)
    	    myDataBase = SQLiteDatabase.openDatabase( DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
 
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
			
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO figure out what to do on upgrade
		
	}
	
	@Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		if (myDataBase != null && myDataBase.isOpen()) {
            return myDataBase;  // The database is already open for business
        }
		try {
			this.openDataBase();
		} catch (SQLException e) {
			return null;
		}
		return myDataBase;
	}
/*
	private class myDataBaseOpenHelper extends SQLiteOpenHelper {
		 

		
		myDataBaseOpenHelper(Context context) {
	         super(context, DB_PATH + DB_NAME, null, DB_VERSION);
	      }
	 
	      @Override
	      public void onCreate(SQLiteDatabase db) {
	    	  db.execSQL("CREATE TABLE " + MY_BOOKS_TABLE + "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + BOOK_NAME_TAG + " TEXT, " + BOOK_CHAPTER_TAG +" INTEGER)");
	    	  db.execSQL("CREATE TABLE " + MY_VERSES_TABLE + "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + VERSES_BOOK_TAG + " TEXT, " + VERSES_CHAPTER_TAG + " INTEGER, " + VERSES_NUMBER_TAG + " INTEGER, " + VERSES_HEADER_TAG + " INTEGER, " + VERSES_VERSE_TAG + " TEXT)");
	      }
	 
	      @Override
	      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	         
	         db.execSQL("DROP TABLE IF EXISTS " + MY_BOOKS_TABLE);
	         db.execSQL("DROP TABLE IF EXISTS " + MY_VERSES_TABLE);
	         onCreate(db);
	      }
	   }
	
	class makeDB extends AsyncTask<Integer, Integer, Integer> {

	@Override
	protected Integer doInBackground(Integer... params) {
		Log.d(LiteralWord.TAG, "parsing xml");
		try {
		FileInputStream fstream = new FileInputStream("/sdcard/nasb.xml");

		 myDataBaseOpenHelper openHelper = new  myDataBaseOpenHelper(myContext);

	     myDataBase = openHelper.getWritableDatabase();

		 // Get a SAXParser from the SAXPArserFactory. 
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();

        // Get the XMLReader of the SAXParser we created. 
        XMLReader xr = sp.getXMLReader();
        /* Create a new ContentHandler and apply it to the XML-Reader
        myXMLtoDBHandler xml2db = new myXMLtoDBHandler();
        xr.setContentHandler(xml2db);

        // Parse the xml-data from our URL. 
        xr.parse(new InputSource(fstream));
        // Parsing has finished. 

        openHelper.close();
        myDataBase = null;
		fstream.close();
		} catch (Exception e) {
			Log.d(LiteralWord.TAG, "parsing xml error " + e.getMessage());
			return 1;
		}
		return 0;
	}


};
*/
/*	
	private class myXMLtoDBHandler extends DefaultHandler {



		private boolean in_booktag = false;
		private boolean in_chaptertag = false;
		private boolean in_versetag = false;
		private boolean in_headertag = false;
		private boolean in_headerss = false;
		private boolean in_headersf = false;
		
		private int curr_chapter_num = 0;
		private int curr_verse_num = 0;
		private String curr_book;
		private String curr_verse;
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			
			//super.characters(ch, start, length);
			if (in_booktag) { 
				Log.d(LiteralWord.TAG, "got book");
				curr_book = new String(ch, start, length);
				curr_chapter_num = 0;
			}
			else if (in_chaptertag) {
				Log.d(LiteralWord.TAG, "got chapter");
				curr_chapter_num++;
				curr_verse_num = 0;
			}
			else if (in_versetag) {
				curr_verse_num++;
				curr_verse = new String(ch, start, length);
				curr_verse = curr_verse.replaceAll("'", "''");
				myDataBase.execSQL("INSERT into " + MY_VERSES_TABLE + "(" + VERSES_BOOK_TAG + ", " + VERSES_CHAPTER_TAG + ", " + VERSES_NUMBER_TAG + ", " + VERSES_HEADER_TAG + ", " + VERSES_VERSE_TAG + ") VALUES ('" + curr_book + "', " + Integer.toString(curr_chapter_num) + ", " + Integer.toString(curr_verse_num) + ", " + Integer.toString(HEADER_NONE) + ", '" + curr_verse + "');");
				Log.d(LiteralWord.TAG, "CDB - " + curr_book + " " + Integer.toString(curr_chapter_num) + ":" + Integer.toString(curr_verse_num));
			} else if (in_headertag || in_headersf || in_headerss) {
				Log.d(LiteralWord.TAG, "got header");
				curr_verse = new String(ch, start, length);
				curr_verse = curr_verse.replaceAll("'", "''");
				int headerType = in_headertag ? HEADER_NORMAL : (in_headerss ? HEADER_PSALM_SUB : HEADER_PSALM_HEB); 
				myDataBase.execSQL("INSERT into " + MY_VERSES_TABLE + "(" + VERSES_BOOK_TAG + ", " + VERSES_CHAPTER_TAG + ", " + VERSES_NUMBER_TAG + ", " + VERSES_HEADER_TAG + ", " + VERSES_VERSE_TAG + ") VALUES ('" + curr_book + "', " + Integer.toString(curr_chapter_num) + ", " + Integer.toString(curr_verse_num) + ", " + Integer.toString(headerType) + ", '" + curr_verse + "');");
			}
			
		}


		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			
			//super.endElement(uri, localName, qName);
			

			if (localName.equals("BN")) {
				Log.d(LiteralWord.TAG, "out book");
				in_booktag = false;
			}
			else if (localName.equals("CN")) {
				Log.d(LiteralWord.TAG, "out chapter");
				in_chaptertag = false;
			}
			else if (localName.equals("V")) {
				Log.d(LiteralWord.TAG, "out verse");
				in_versetag = false;
			}
			else if (localName.equals("SH")) {
				Log.d(LiteralWord.TAG, "out header");
				in_headertag = false;
			}
			else if (localName.equals("SS")) {
				in_headerss = false;
			}
			else if (localName.equals("SF")) {
				in_headersf = false;
			}
			else {
				// unknown tag, don't do anything
				return;	
			}
		}


		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			
			// super.startElement(uri, localName, qName, attributes);
			

			if (localName.equals("BN")) {
				Log.d(LiteralWord.TAG, "in book");
				in_booktag = true;
			}
			else if (localName.equals("CN")) {
				Log.d(LiteralWord.TAG, "in chapter");
				in_chaptertag = true;
				
			}
			else if (localName.equals("V")) {
				Log.d(LiteralWord.TAG, "in verse");
				in_versetag = true;
			}
			else if (localName.equals("SH")) {
				Log.d(LiteralWord.TAG, "in header");
				in_headertag = true;
			}
			else if (localName.equals("SS")) {
				in_headerss = true;
			}
			else if (localName.equals("SF")) {
				in_headersf = true;
			}
			else {
				skippedEntity(localName);
				return;	
			}
			
		}
		
	
	}
*/
}