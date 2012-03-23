package com.LiteralWord.Bible.utils;

import java.util.ArrayList;

import android.util.Log;

import com.LiteralWord.Bible.LiteralWord;

public class VerseListStringConverter {

	private static final String TAG = "VerseListStringConverter";
	
	/* verses 4 5 6 7 8 will be converted to 4-8
	 * verses 4 7 9 will be converted to 4,7,9
	 * verses 4 5 6 8 9 will be converted to 4-6,8-9
	 */
	
	static public String List2Verse(ArrayList<Integer> verses) {
		// We assume the ArrayList is in order
		
		int size_t = verses.size();
		String ret = "";
		int lastV = -1;
		boolean dash = false;
		
		for (int i = 0; i < size_t; i++) {
			int v = verses.get(i);
			if (lastV == -1){
				ret = Integer.toString(v);
				lastV = v; 
				continue;
			}
			
			if (!dash) {
				if ((v - lastV) == 1) {
					ret += "-";
					dash = true;
				} else {
					ret += "," + Integer.toString(v);
				}
			} else {
				if ((v - lastV) != 1) {
					ret += Integer.toString(lastV);
					ret += ",";
					ret += Integer.toString(v);
					dash = false;
				} // else just keep going
			}
			
			lastV = v; 
		}
		if (dash) ret += Integer.toString(lastV);
		Log.d(LiteralWord.TAG, TAG + " " + ret);
		return ret;
	}
	
	
	static public ArrayList<Integer> Verse2List(String verses) {
		// We assume the ArrayList is in order
		ArrayList<Integer> ret = new ArrayList<Integer>();
		String com_del = "[,]";
		String dash_del = "[-]";
		
		String[] str_tok = verses.split(com_del);
		
		for (int i = 0; i < str_tok.length; i++) {
			String[] str = str_tok[i].split(dash_del);
			if (str.length == 1) {
				//Log.d(LiteralWord.TAG, TAG + "added" + str[0]);
				ret.add(Integer.parseInt(str[0]));
			}
			else if (str.length == 2) {
				for (int j = Integer.parseInt(str[0]) ; j <= Integer.parseInt(str[1]) ; j++) {
					//Log.d(LiteralWord.TAG, TAG + "added" + j);
					ret.add(j);
				}
			} else {
				throw new Error ("not expecting more than 2!");
			}
			
		}
		
		
		return ret;
	}
	
	static public String toVerseString(String book, int chap, String verse) {
		return book + " " + Integer.toString(chap) + ":" + verse;
	}
}
