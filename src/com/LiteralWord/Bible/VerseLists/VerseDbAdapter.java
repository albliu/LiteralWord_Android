/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.LiteralWord.Bible.VerseLists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.LiteralWord.Bible.LiteralWord;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class VerseDbAdapter {

	public static final int BOOKMARK_TABLE = 0;
	public static final int MEMVERSE_TABLE = 1;
	

    public static final String KEY_ROWID = "_id";
    public static final String TAG_TITLE_ROWID = "titag";

    
    private static final String TAG = "VerseDbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */

    @SuppressWarnings("unused")
	private static String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.LiteralWord/" ;
    private static final String DATABASE_NAME = "Verses.db";
    private static final String DATABASE_BOOKMARK_TABLE = "bookmarks";
    private static final String DATABASE_MEMVERSE_TABLE = "memoryverses";
    private static final int DATABASE_VERSION = 2;
   
    
    private static final String CREATE_TABLE_HEAD = "create table ";
    private static final String CREATE_TABLE_END = " (" + KEY_ROWID + " integer primary key autoincrement, " + TAG_TITLE_ROWID + " text, "
            + LiteralWord.VERSES_BOOK_ROWID + " text not null, " + LiteralWord.VERSES_CHAPTERS_ROWID + " integer, " + LiteralWord.VERSES_NUM_ROWID + " text not null, " + LiteralWord.VERSES_TEXT_ROWID + " text not null);";
    
    private static final String DATABASE_BOOKMARK_CREATE = CREATE_TABLE_HEAD + DATABASE_BOOKMARK_TABLE + CREATE_TABLE_END;
    private static final String DATABASE_MEMVERSE_CREATE = CREATE_TABLE_HEAD + DATABASE_MEMVERSE_TABLE + CREATE_TABLE_END;
    
    private final Context mCtx;
    private String tableName;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL(DATABASE_BOOKMARK_CREATE);
            db.execSQL(DATABASE_MEMVERSE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_BOOKMARK_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_MEMVERSE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public VerseDbAdapter(Context ctx, int table) {
        this.mCtx = ctx;
        
        switch (table) {
        case BOOKMARK_TABLE:
        	tableName = DATABASE_BOOKMARK_TABLE;
        	break;
        case MEMVERSE_TABLE:
        	tableName = DATABASE_MEMVERSE_TABLE;
        	break;
        default:
        	throw new Error("table doesn't exist");
        	
        }
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public VerseDbAdapter open() throws SQLException {
    	if (mDbHelper == null) mDbHelper = new DatabaseHelper(mCtx);
    	mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    
    /*=======================================
     * verses
     *=======================================*/
	
		public long addVerse(String book, int chapter, String verses, String text) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(LiteralWord.VERSES_BOOK_ROWID, book);
			initialValues.put(LiteralWord.VERSES_CHAPTERS_ROWID, chapter);
			initialValues.put(LiteralWord.VERSES_NUM_ROWID, verses);
			initialValues.put(LiteralWord.VERSES_TEXT_ROWID, text);

			return mDb.insert(tableName, null, initialValues);
		}

		public boolean deleteVerse(long rowId) {

			return mDb.delete(tableName, KEY_ROWID + "=" + rowId, null) > 0;
		}

		public void deleteAllVerse() {
			mDb.execSQL("DROP TABLE IF EXISTS " + tableName);
			mDb.execSQL(CREATE_TABLE_HEAD + tableName + CREATE_TABLE_END);
		}
		public Cursor fetchAllverses() {

			return mDb.query(tableName, new String[] { KEY_ROWID, TAG_TITLE_ROWID,
					LiteralWord.VERSES_BOOK_ROWID, LiteralWord.VERSES_CHAPTERS_ROWID, LiteralWord.VERSES_NUM_ROWID, LiteralWord.VERSES_TEXT_ROWID }, null, null, null, null, null);
		}

		public Cursor fetchVerse(long rowId) throws SQLException {

			Cursor mCursor =

			mDb.query(true, tableName, new String[] { KEY_ROWID, TAG_TITLE_ROWID,
					LiteralWord.VERSES_BOOK_ROWID, LiteralWord.VERSES_CHAPTERS_ROWID, LiteralWord.VERSES_NUM_ROWID, LiteralWord.VERSES_TEXT_ROWID }, KEY_ROWID + "=" + rowId, null, null,
					null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;

		}

		
		public Cursor fetchVerse(String t) throws SQLException {

			Cursor mCursor =

			mDb.query(true, tableName, new String[] { KEY_ROWID, TAG_TITLE_ROWID,
					LiteralWord.VERSES_BOOK_ROWID, LiteralWord.VERSES_CHAPTERS_ROWID, LiteralWord.VERSES_NUM_ROWID, LiteralWord.VERSES_TEXT_ROWID }, TAG_TITLE_ROWID + "=?", new String[] {t}, null,
					null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;

		}

		
		public static class verse_t {
			public String b;
			public int ch;
			public String ver;

			public verse_t(String book, int chapter, String verse) {
				b = book;
				ch = chapter;
				ver = verse;
			}

			public String toString() {
				return b + " " + Integer.toString(ch) + ":" + ver;
			}
		}
}
