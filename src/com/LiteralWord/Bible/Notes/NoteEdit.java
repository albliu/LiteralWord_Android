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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.LiteralWord.Bible.R;
import com.LiteralWord.Bible.Notes.NoteEditFragment.OnNoteEditListener;

public class NoteEdit extends FragmentActivity implements OnNoteEditListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteedit_fragment);

    }

	@Override
	public void onEditDone() {
		setResult(RESULT_OK);
        finish();
		
	}

	@Override
	public void onEditDismiss() {
		setResult(RESULT_CANCELED);
        finish();
	}


}
