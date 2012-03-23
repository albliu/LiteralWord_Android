/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.LiteralWord.Bible.Notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.LiteralWord.Bible.LiteralWord;
import com.LiteralWord.Bible.R;

public class NoteEditFragment extends Fragment {
	private static String TAG = "NotepadEditFragment";
	
	private final String ID_TAG = "rowID";
	private final String TITLE_TAG = "title";
	private final String BODY_TAG = "body";
	
	private View myView;
    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    
    
    private InputMethodManager imm;
    // if we want to do lock mode
    @SuppressWarnings("unused")
	private boolean lock;
    
    private MyDbAdapter mDbHelper;

	private OnNoteEditListener myNoteEditListener;

	public interface OnNoteEditListener {
		public void onEditDone();

		public void onEditDismiss();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			myNoteEditListener = (OnNoteEditListener) activity;
		} catch (ClassCastException e) {
			Log.d(LiteralWord.TAG, TAG + " Bad class");
			throw new ClassCastException(activity.toString()
					+ " must implement OnNoteEditListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.note_edit, container, false);
		mTitleText = (EditText) myView.findViewById(R.id.title);
		mBodyText = (EditText) myView.findViewById(R.id.body);
		mBodyText.setTextSize(LiteralWord.textSize);

		myView.findViewById(R.id.confirm).setOnClickListener(
				new OnClickListener() {

					public void onClick(View view) {
						saveState();
						myNoteEditListener.onEditDone();
					}
				});

		myView.findViewById(R.id.dismiss).setOnClickListener(
				new OnClickListener() {

					public void onClick(View view) {
						myNoteEditListener.onEditDismiss();
					}
				});
/*
		lock = false;
		myView.findViewById(R.id.readonly).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						lock = !lock;
						// if you want to keep the background, just a custom background for editText
						if (lock) { 
							mTitleText.requestFocus();
							imm.hideSoftInputFromWindow(mTitleText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
						
					}
				});
		
*/		
		imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mTitleText.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		imm.hideSoftInputFromWindow(mBodyText.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		
		
		return myView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mDbHelper = new MyDbAdapter(myView.getContext());
		mDbHelper.open();

		if (savedInstanceState != null) {

			if (savedInstanceState.containsKey(ID_TAG))
				mRowId = savedInstanceState.getLong(ID_TAG);
			mTitleText.setText(savedInstanceState.getString(TITLE_TAG));
			mBodyText.setText(savedInstanceState.getString(BODY_TAG));
			Log.d(LiteralWord.TAG, TAG + " Saved Activity Created, ID = "
					+ mRowId);
			return;
		}

		if (mRowId == null) {
			Bundle extras = getActivity().getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(MyDbAdapter.KEY_ROWID)
					: null;

		}
		Log.d(LiteralWord.TAG, TAG + " Activity Created, ID = " + mRowId);
		populateFields();
	}

	public void onHandleIntent(Intent I) {
		getActivity().setIntent(I);
		Bundle extras = I.getExtras();

		mRowId = extras == null ? null : extras
				.getLong(MyDbAdapter.KEY_ROWID);
		Log.d(LiteralWord.TAG, TAG + " New Intent, ID = " + mRowId);
		populateFields();
	}

	private void populateFields() {

		Log.d(LiteralWord.TAG, TAG + " Populating Field, ID = " + mRowId);
		if (mRowId != null) {
			Cursor note = mDbHelper.fetchNote(mRowId);
			if (note != null) {
				mTitleText.setText(note.getString(note
						.getColumnIndexOrThrow(MyDbAdapter.KEY_TITLE)));
				mBodyText.setText(note.getString(note
						.getColumnIndexOrThrow(MyDbAdapter.KEY_BODY)));
				note.close();
				return;
			}
		}
		mTitleText.setText("");
		mTitleText.setHint("New Note");
		mBodyText.setText("");

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRowId != null)
			outState.putLong(ID_TAG, mRowId);

		outState.putString(TITLE_TAG, mTitleText.getText().toString());
		outState.putString(BODY_TAG, mBodyText.getText().toString());

	}

	public void saveState() {
		String title = mTitleText.getText().toString();
		if (title.equals(""))
			title = "(untitled)";
		String body = mBodyText.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createNote(title, body);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateNote(mRowId, title, body);
		}
	}
}
