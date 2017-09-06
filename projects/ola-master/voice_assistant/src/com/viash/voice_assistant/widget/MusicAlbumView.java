package com.viash.voice_assistant.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData.MusicAlbumJsonData.MusicAlbumData;

public class MusicAlbumView extends LinearLayout {
	List<MusicAlbumData> mLstMusicData;
	public MusicAlbumView(Context context,List<MusicAlbumData> mLstMusicData) {
		super(context);
		this.mLstMusicData = mLstMusicData;
		init();
	}

	public MusicAlbumView(Context context, AttributeSet attrs,List<MusicAlbumData> mLstMusicData) {
		super(context, attrs);
		this.mLstMusicData = mLstMusicData;
		init();
	}
	
	private void init(){
		setOrientation(LinearLayout.VERTICAL);
		if(mLstMusicData !=null && mLstMusicData.size() > 0){
			for (int i = 0; i < mLstMusicData.size(); i++) {
				MusicAlbumData musicAlbumData = mLstMusicData.get(i);
				View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_music_album_item, null);
				TextView tv_musicName= (TextView) itemView.findViewById(R.id.tv_music_name);
				TextView tv_musicAuthor= (TextView) itemView.findViewById(R.id.tv_music_author);
				TextView tv_musicAlbum= (TextView) itemView.findViewById(R.id.tv_music_album);
				if(musicAlbumData.title != null)
					tv_musicName.setText(musicAlbumData.title);
				if(musicAlbumData.artist != null)
					tv_musicAuthor.setText(musicAlbumData.artist);
				if(musicAlbumData.album != null)
					tv_musicAlbum.setText(musicAlbumData.album);
				addView(itemView,android.widget.RelativeLayout.LayoutParams.FILL_PARENT,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
				if(i < mLstMusicData.size() -1 ){
					View viewLineBG  = new View(getContext());
					viewLineBG.setBackgroundResource(R.drawable.bg_line);
					addView(viewLineBG,LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
				}
			}
		}
	}

}
