/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
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

package com.viash.voice_assistant.media;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;

import com.viash.voice_assistant.entity.MusicEntity;

public class Playlist implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private static Playlist instance = null;
	private static Bitmap currentEntryArtWork;

	/**
	 * Keeps playlist's entries
	 */
	private ArrayList<MusicEntity> playlist;
	
	/**
	 * Keeps record of currently selected track
	 */
	private int selected = -1;
	
	/**
	 * play Mode
	 */
	public static final int PLAY_MODE_NO_LOOP = 0;
	public static final int PLAY_MODE_LIST_LOOP = 1;
	public static final int PLAY_MODE_SINGLE_LOOP = 2;
	public static final int PLAY_MODE_RANDOM = 3;	
	private int mPlayMode =  PLAY_MODE_LIST_LOOP;
			
	private Playlist(){
		playlist = new ArrayList<MusicEntity>();
	}
	
	public static Playlist Instance() {
		if (instance == null) {
			instance = new Playlist();
		}
		return instance;
	}
	
	/**
	 * Checks if the playlist is empty 
	 * 
	 * @return boolean value
	 */
	public boolean isEmpty(){
		return playlist.size() == 0;
	}
	
	/**
	 * Selects next song from the playlist
	 */
	public boolean selectNext(){
		currentEntryArtWork = null;
		if(!isEmpty()){
			selected++;
			if (selected > playlist.size()-1) {
				switch (mPlayMode) {
				case PLAY_MODE_NO_LOOP:
					return false;
				case PLAY_MODE_LIST_LOOP:
					selected = 0;
					return true;
				case PLAY_MODE_SINGLE_LOOP:
					selected --;
					return true;
				case PLAY_MODE_RANDOM:
					Random random = new Random();
					selected = random.nextInt(playlist.size());
					return true;
				default:
					return false;
				}
			}			
			return true;//selected %= playlist.size();
		}
		return false;
	}
	
	/**
	 * Selects previous song from the playlist
	 */
	public boolean selectPrev(){
		currentEntryArtWork = null;
		if(!isEmpty()){
			selected--;
			if(selected < 0) {
				switch (mPlayMode) {
				case PLAY_MODE_NO_LOOP:
					return false;
				case PLAY_MODE_LIST_LOOP:
					selected = playlist.size() - 1;
					return true;
				case PLAY_MODE_SINGLE_LOOP:
					selected ++;
					return true;
				case PLAY_MODE_RANDOM:
					Random random = new Random();
					selected = random.nextInt(playlist.size());
					return true;
				default:
					return false;
				}				
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Select song with a given index
	 * 
	 * @param index
	 */
	public boolean select(int index){
		currentEntryArtWork = null;
		if(!isEmpty()){
			if(index >= 0 && index < playlist.size()) {
				selected = index;
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}
	
	public void setMusicPlayList(List<MusicEntity> list){
		playlist = (ArrayList<MusicEntity>) list;
		selected = 0;
	}
	
	/**
	 * Return index of the currently selected song
	 * 
	 * @return int value (-1 if the playlist is empty)
	 */
	public int getSelectedIndex(){
		if(isEmpty()){
			selected = -1;
		}
		if(selected == -1 && !isEmpty()){
			selected = 0;
		}

		if(selected >= playlist.size() ){
			selected = playlist.size() -1;
		}
		return selected;
	}
	
	/**
	 * Return currently selected song
	 * 
	 * @return <code>PlaylistEntry</code> instance
	 */
	public MusicEntity getSelectedTrack(){
		MusicEntity musicEntry = null;
		
		if(!isEmpty()){
			musicEntry = playlist.get(getSelectedIndex());
		}
		
		return musicEntry;
		
	}
	
	/**
	 * Count of playlist entries
	 * 
	 * @return
	 */
	public int size(){
		return playlist == null ? 0 : playlist.size();
	}
	
	public void setPlayMode(int mode) {
		if (mode < 0 || mode > 3) {
			mPlayMode = PLAY_MODE_LIST_LOOP;
		}
		mPlayMode = mode;
	}

	public static Bitmap getCurrentEntryArtWork() {
		return currentEntryArtWork;
	}

	public static void setCurrentEntryArtWork(Bitmap currentEntryArtWork) {
		Playlist.currentEntryArtWork = currentEntryArtWork;
	}
}
