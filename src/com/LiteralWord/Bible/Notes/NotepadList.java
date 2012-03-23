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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.Notes.NoteEditFragment.OnNoteEditListener;
import com.LiteralWord.Bible.Notes.NoteListFragment.OnNoteSelectedListener;
import com.LiteralWord.Bible.utils.myActionBar;
import com.LiteralWord.Bible.utils.myTitleBar.MenuSelector;

public class NotepadList extends FragmentActivity implements OnNoteSelectedListener, OnNoteEditListener {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notelist_fragment);
       
		View ab = findViewById(R.id.my_action_bar);
		if (ab != null) {
			myActionBar myAB = new myActionBar(this, ab, new MenuSelector() {
				@Override
				public void onMenuItemSelected(int id) {
					onMenuIndexSelected(id);

				}
			});

			myAB.addButton(R.id.notes_p, android.R.drawable.ic_menu_agenda);
		
		}
		
		
    }

	@Override
	public void onNoteSelected(long id) {
		Intent i = new Intent(this, NoteEdit.class);
		i.putExtra(MyDbAdapter.KEY_ROWID, id);
		
		if (findViewById(R.id.notes_edit_panel) != null) {
			NoteEditFragment myNotes = (NoteEditFragment) getSupportFragmentManager().findFragmentById(R.id.noteedit_fragment);
			myNotes.onHandleIntent(i);
		}
		else  {
			startActivityForResult(i, ACTIVITY_EDIT);
		}
	}

	@Override
	public void onNoteCreate() {
		
		Intent i = new Intent(this, NoteEdit.class);
		if (findViewById(R.id.notes_edit_panel) != null) {
			NoteEditFragment myNotes = (NoteEditFragment) getSupportFragmentManager().findFragmentById(R.id.noteedit_fragment);
			myNotes.onHandleIntent(i);
		} else {
			startActivityForResult(i, ACTIVITY_CREATE);
		}
	}
	
	@Override
	public void onEditDone() {
		
		((NoteEditFragment) getSupportFragmentManager().findFragmentById(R.id.noteedit_fragment)).saveState();	
		((NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.notelist_fragment)).fillData();
		
		
	}

	@Override
	public void onEditDismiss() {
		onNoteCreate();	
	}

    
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.findItem(INSERT_ID).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_add));
        
        //MenuCompat.setShowAsAction(menu.findItem(INSERT_ID), 1);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (onMenuIndexSelected(item.getItemId())) return true;
        return super.onMenuItemSelected(featureId, item);
    }
*/	
    public boolean onMenuIndexSelected(int Id) {
    	switch (Id) {
    	case R.id.notes_p:
            View folders = findViewById(R.id.notes_folder_panel);
            
            if (folders == null) return false;
            if (folders.getVisibility() == View.GONE) folders.setVisibility(View.VISIBLE);
            else folders.setVisibility(View.GONE);
            return true;
    	}
    	return false;
    }


    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ((NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.notelist_fragment)).fillData();
    }
    
}
