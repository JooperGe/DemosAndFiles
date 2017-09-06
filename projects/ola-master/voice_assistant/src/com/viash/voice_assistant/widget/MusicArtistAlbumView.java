package com.viash.voice_assistant.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData.ArtistAlbumJsonData.ArtistAlbumData;

public class MusicArtistAlbumView extends LinearLayout {
	private List<ArtistAlbumData> mLstArtistData;
	public MusicArtistAlbumView(Context context,List<ArtistAlbumData> mLstArtistData) {
		super(context);
		this.mLstArtistData = mLstArtistData;
		init();
	}

	public MusicArtistAlbumView(Context context, AttributeSet attrs,List<ArtistAlbumData> mLstArtistData) {
		super(context, attrs);
		this.mLstArtistData = mLstArtistData;
		init();
	}
	
	private void init(){
		setOrientation(LinearLayout.VERTICAL);
		if(mLstArtistData !=null && mLstArtistData.size() > 0){
			for (int i = 0; i < mLstArtistData.size(); i++) {
				ArtistAlbumData artistAlbumData = mLstArtistData.get(i);
				View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_music_album_item, null);
				TextView tv_musicName= (TextView) itemView.findViewById(R.id.tv_music_name);
				TextView tv_musicAuthor= (TextView) itemView.findViewById(R.id.tv_music_author);
				TextView tv_musicAlbum= (TextView) itemView.findViewById(R.id.tv_music_album);
				if(artistAlbumData.album != null)
					tv_musicName.setText(artistAlbumData.album);
				if(artistAlbumData.artist != null)
					tv_musicAuthor.setText(artistAlbumData.artist);
				tv_musicAlbum.setVisibility(View.GONE);
				addView(itemView,android.widget.RelativeLayout.LayoutParams.FILL_PARENT,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
				if(i < mLstArtistData.size() -1 ){
					View viewLineBG  = new View(getContext());
					viewLineBG.setBackgroundResource(R.drawable.bg_line);
					addView(viewLineBG,LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
				}
			}
		}
	}

}
