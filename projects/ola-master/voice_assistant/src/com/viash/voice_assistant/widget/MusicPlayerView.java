package com.viash.voice_assistant.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.activity.assistant.NotifyUiHandler;
import com.viash.voice_assistant.entity.MusicEntity;
import com.viash.voice_assistant.media.Playlist;
import com.viash.voice_assistant.service.MusicService;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.utils.CallBackInterface;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.ImageLoaderUtil;
import com.viash.voicelib.utils.LocalPathUtil;
import com.viash.voicelib.utils.LogOutput;

public class MusicPlayerView extends RelativeLayout implements CallBackInterface{
	private static final String TAG="MusicPlayerView"; 
	private Button btn_playOrPause;
	private Button btn_last;
	private Button btn_next;
	private Button btn_music_list;
	private Button btn_close_music;
	private ImageButton btn_close;
	private ImageView imgv_author;
	private ImageView btn_close2;
	//private ImageView imgv_author2;
	private TextView tv_name;	
	private ListView lv_music;
	private View layout_music;
	private View layout_musicSmall;
	private boolean isPause = true;
	private boolean isLoop = false;
	private Context context;
	public int position=0;
	public static MusicAdapter musicAdapter;
	protected SeekBar playProgress;
	public static List<MusicEntity> musicList;
	private boolean musicListShow=true;
	
	private Playlist mPlaylist = null;
	
	/* add by Loneway for auto play judgement */
	private boolean m_bAutoplay = false;
	
	private boolean m_bSpeakPause = false;
	private ProgressBar progressBar_SeekWait = null;
	public void setAutoplay(boolean m_bAutoplay) {
		this.m_bAutoplay = m_bAutoplay;
	}
	/*end*/
	
	protected static final int MSG_CONTRACT_MUSIC_LIST = 50;
	protected static final int MSG_SHOW_MUSIC_LIST = 51;
	protected static final int MSG_LOGAD_MUSIC_IMAGE= 52;
	protected static final int MSG_REFRESH_MUSIC_LIST= 53;
	
	
	public static final String MUSIC_PLAY ="play";
	public static final String MUSIC_PAUSE ="pause";
	public static final String MUSIC_RESUME ="resume";
	public static final String MUSIC_NEXT ="next";
	public static final String MUSIC_PREV ="prev";
	public static final String MUSIC_CLOSE ="close";
	public static final String MUSIC_FIRST ="first";
	public static final String MUSIC_LAST ="last";
	public static final String MUSIC_INDEX ="index";
	public static final String MUSIC_LOOP ="loop";
	public static final String MUSIC_RANDOM ="random";
	public static final String MUSIC_INDEX_LOOP ="index_loop";
	
	private Handler mHandler;
	
	public MusicPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(context);
	}
	private void init(Context context) {
		position=0;
		m_bSpeakPause = false;
		View view = LayoutInflater.from(context).inflate(R.layout.layout_music, null);
		btn_playOrPause = (Button) view.findViewById(R.id.btn_play);
		btn_last = (Button) view.findViewById(R.id.btn_last);
		btn_next = (Button) view.findViewById(R.id.btn_next);
		btn_close = (ImageButton) view.findViewById(R.id.btn_close);
		//btn_close2 = (ImageButton) view.findViewById(R.id.btn_close2);
		btn_music_list = (Button) view.findViewById(R.id.btn_music_list);
		btn_close_music = (Button) view.findViewById(R.id.btn_close_music);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
				
		playProgress=(SeekBar) view.findViewById(R.id.playProgress);
		imgv_author=(ImageView) view.findViewById(R.id.imgv_head);
		layout_music = view.findViewById(R.id.layout_music);
        progressBar_SeekWait = (ProgressBar)view.findViewById(R.id.progressBar_SeekWait);
		progressBar_SeekWait.setVisibility(View.VISIBLE);
		btn_playOrPause.setOnClickListener(PlayOnclickLister);
		btn_last.setOnClickListener(PlayOnclickLister);
		btn_next.setOnClickListener(PlayOnclickLister);
		btn_music_list.setOnClickListener(PlayOnclickLister);
		btn_close_music.setOnClickListener(PlayOnclickLister);
		
		addView(view, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT ));
		if(btn_playOrPause.isEnabled())
			btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
		
		playProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser)
					seekTo(progress);
				
			}
		});
	}
	
	OnClickListener musicFormChange = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			if(musicListShow){
				/*if(imgv_author2.getTag() != null){
					boolean isLoad = (Boolean) imgv_author2.getTag();
					if(isLoad == false){
						ImageLoaderUtil.loadImageAsync(imgv_author2, musicList.get(position).getPhoto(), null, getResources().getDrawable(R.drawable.icon_defalut_music_author),150);
						imgv_author2.setTag(true);
					}
				}*/
				layout_music.setVisibility(View.GONE);
				layout_musicSmall.setVisibility(View.VISIBLE);
				musicListShow=false;
//				mHandler.sendEmptyMessage(NewAssistActivity.MSG_LISTVIEW_TO_LAST_PAGE);
				mHandler.sendEmptyMessage(NotifyUiHandler.MSG_LISTVIEW_TO_LAST_PAGE);
				
			}else{
				layout_music.setVisibility(View.VISIBLE);
				layout_musicSmall.setVisibility(View.GONE);
				musicListShow=true;
			}
			
		}
	};
	
	public void initMusicListView(ListView listView)
	{
		if (listView == null) {
			return;
		}
		lv_music = listView;
		lv_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
				   VoiceSdkService.mSpeechRecognizer.abort();
				else
				   VoiceAssistantService.mSpeechRecognizer.abort();
				skipTo(arg2);
				handler.sendEmptyMessage(MSG_REFRESH_MUSIC_LIST);
			}
		});
		//progressBar_SeekWait.setVisibility(View.VISIBLE);	
		//if(lv_music.getVisibility() != View.VISIBLE)
		{
		  Animation animation = AnimationUtils.loadAnimation(context, R.anim.translate_visible);
		  lv_music.startAnimation(animation);
		  lv_music.setVisibility(View.VISIBLE);
		  animation.setAnimationListener(new AnimationStopListener());
		  
		}		
	}
	public class AnimationStopListener implements AnimationListener
	{

		@Override
		public void onAnimationStart(Animation animation) {		
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			//progressBar_SeekWait.setVisibility(View.GONE);
			new CountDownTimer(5000,5000)
			{
				@Override
				public void onTick(long millisUntilFinished) {
	              
				}
				@Override
				public void onFinish() {
					if(lv_music != null)
					{
						if(lv_music.getVisibility() == View.VISIBLE)
						{
						  Animation animation = AnimationUtils.loadAnimation(MusicPlayerView.this.context, R.anim.translate_invisible);
						  lv_music.startAnimation(animation);
						  lv_music.setVisibility(View.INVISIBLE);					  
						}
					}
					
				}
				
			}.start();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {		
		}	
	}
	
	public void contractMusicList(){
		handler.sendEmptyMessage(MSG_CONTRACT_MUSIC_LIST);
	}
	
	public void showMusicList(){
		handler.sendEmptyMessage(MSG_SHOW_MUSIC_LIST);
	}

	public MusicPlayerView(Context context) {
		super(context);
		this.context = context;
		init(context);
	}

	public void setPlayList(List<MusicEntity> list,Handler mainHandler){
		this.mHandler = mainHandler;
		position=0;
		musicList=list;
		if(musicList!=null&&musicList.size()>0){
			String showName = "";
			showName += musicList.get(position).getName();
			if(musicList.get(position).getAuthor() != null )
				showName +="/" + musicList.get(position).getAuthor();
			tv_name.setText(showName);
			layout_music.setVisibility(View.VISIBLE);
			//layout_musicSmall.setVisibility(View.GONE);
			btn_playOrPause.setEnabled(true);
			//String[] arra = new String[list.size()];
			//int[] time = new int[list.size()];
			//for (int i = 0; i < list.size(); i++) {
				//arra[i] = list.get(i).getUrl();
				//time[i] =(int)list.get(i).getTime();
				//LogOutput.i(TAG, "music url-->"+list.get(i).getUrl());
			//}
			musicAdapter=new MusicAdapter(musicList);
			if (lv_music != null) {
				lv_music.setAdapter(musicAdapter);
			}
			Intent intent = new Intent(context, MusicService.class);
			intent.setAction(MusicService.INIT);
			//intent.putExtra("musicList", arra);
			//intent.putExtra("time", time);
			intent.putExtra("Auto_play", m_bAutoplay);
			context.startService(intent);
			MusicService.initHandler(handler);
			mPlaylist = Playlist.Instance();
			mPlaylist.setMusicPlayList(list);
			//playlist.setPlayMode(musicList..);
			MusicService.setPlaylist(mPlaylist);
			
			/*if(musicList.size()<=1){
				isPause = true;
				btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
				btn_last.setBackgroundResource(R.drawable.bg_play_last_disabled);
				btn_next.setBackgroundResource(R.drawable.bg_play_next_disable);
				btn_last.setEnabled(false);
				btn_next.setEnabled(false);
			}else{*/
				isPause = true;
				btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
				btn_last.setBackgroundResource(R.drawable.icon_play_last);
				btn_next.setBackgroundResource(R.drawable.icon_play_next);
				btn_last.setEnabled(true);
				btn_next.setEnabled(true);
			//}
		}else{
			LogOutput.e(TAG,"music list is null");
		}
		if(list.size() > 0)
		{
		  m_bSpeakPause	= false;//stop play when refresh music list
		  handler.sendEmptyMessage(MSG_LOGAD_MUSIC_IMAGE);
		 /* new CountDownTimer(3000,3000){//hide progressbar after 3 seconds
				@Override
				public void onTick(long millisUntilFinished) {
	              
				}
				@Override
				public void onFinish() {					
					progressBar_SeekWait.setVisibility(View.INVISIBLE);
				}	
			}.start();*/
		}
	}

	public void play(boolean loop) {
		btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
		isLoop = loop;
		m_bSpeakPause = false;
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.PLAY);
		intent.putExtra("loop", loop);
		context.startService(intent);
		isPause = false;
	    Log.i("MusicPlayerview","play() isPause = "+isPause);
	}
	
	public void skipTo(int position_int) {
		btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
		position=position_int;
		m_bSpeakPause = false; 
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.PLAY_TO);
		intent.putExtra("position", position);
		context.startService(intent);
		isPause = false;
		if (lv_music != null){
			lv_music.setSelection(position);
		}
	}

	public void pause() {
		m_bSpeakPause = false; 
		if(lv_music != null && musicList !=null && musicList.size() > 0){
			btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
			Intent intent = new Intent(context, MusicService.class);
			intent.setAction(MusicService.PAUSE);
			context.startService(intent);
			isPause = true;
			//Log.i("MusicPlayerview","pause() isPause = "+isPause);
			
			if(lv_music.getVisibility() == View.VISIBLE)
			{
			  Animation animation = AnimationUtils.loadAnimation(context, R.anim.translate_invisible);
			  lv_music.startAnimation(animation);
			  lv_music.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void stop() {
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.STOP);
		context.startService(intent);
	}

	public void prev() {
		m_bSpeakPause = false; 
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.LAST);
		context.startService(intent);
	}

	public void next() {
		m_bSpeakPause = false; 
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.NEXT);
		context.startService(intent);
	}
	
	public void seekTo(int seek) {
		m_bSpeakPause = false; 
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.SEEK_TO);
		intent.putExtra("seek", seek);
		context.startService(intent);
	}
	
	public void loop() {
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.LOOP);
		context.startService(intent);
	}
	
	public void indexloop(int index) {
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.INDEX_LOOP);
		intent.putExtra("index", index);
		context.startService(intent);
	}
	
	public void random(){
		Intent intent = new Intent(context, MusicService.class);
		intent.setAction(MusicService.RANDOM);
		context.startService(intent);
	}
	
	public void closeMusicServer(){
		btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
		//musicList = null;
		if(MusicService.serverIsStart){
			MusicService.serverIsStart=false;
			Intent intent = new Intent(context, MusicService.class);
			context.stopService(intent);
		}
		
		
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MusicService.RESULT_PLAY_NEXT_OR_LAST:
				position = msg.arg1;
				isPause=false;
				handler.post(new Runnable(){
					@Override
					public void run() {
						musicAdapter.notifyDataSetChanged();
						if(musicList != null)
						{
							String showName = "";
							showName += musicList.get(position).getName();
							if(musicList.get(position).getAuthor() != null )
								showName +="/" + musicList.get(position).getAuthor();
							tv_name.setText(showName);
							btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
							/*if(position ==0){
								btn_last.setBackgroundResource(R.drawable.icon_play_last_pressed);
								btn_last.setEnabled(false);
							}else{*/
								//btn_last.setEnabled(true);
								//btn_last.setBackgroundResource(R.drawable.icon_play_last);
							//}
							/*if(position == musicList.size() -1){
								btn_next.setBackgroundResource(R.drawable.icon_play_next_pressed);
								btn_next.setEnabled(false);
							}else{*/
								//btn_next.setEnabled(true);
								//btn_next.setBackgroundResource(R.drawable.icon_play_next);
							//}
							handler.sendEmptyMessage(MSG_LOGAD_MUSIC_IMAGE);
						}
					}
				});
				break;
			case MusicService.RESULT_PLAY_END:
				setVisibility(View.GONE);
				break;
			case MusicService.RESULT_MUSIC_MAX_PROGRESS:
				final int maxProgress = msg.arg1;
				//Log.i(TAG, "maxProgress is " + maxProgress);
				handler.post(new Runnable(){
					@Override
					public void run() {
						playProgress.setMax(maxProgress);
						playProgress.setProgress(0);
					}
				});
				break;
			case MusicService.RESULT_MUSIC_UPDATE_PROGRESS:
				final int progress = msg.arg1;
				//Log.i(TAG, "progress is " + progress);
				handler.post(new Runnable(){
					@Override
					public void run() {
						playProgress.setProgress(progress);
						if (MusicService.isPlaying()) {
							btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
						}
					}
				});
				break;
			case MusicService.RESULT_MUSIC_MSG:
				if(msg.getData().getString("msg") != null){
					CustomToast.showShortText(context, msg.getData().getString("msg"));
				}
				break;
			case MusicService.RESULT_MUSIC_PLAY_FAIL:
				handler.post(new Runnable(){
					@Override
					public void run() {
						btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
						CustomToast.showShortText(context,getResources().getString(R.string.music_play_fail));
					}
				});
				break;
			case MusicService.RESULT_MUSIC_DOWNLOAD_FAIL:
				handler.post(new Runnable(){
					@Override
					public void run() {
						CustomToast.showShortText(context,getResources().getString(R.string.music_download_fail));
					}
				});
				break;
			case MusicService.RESULT_MUSIC_PAUSE_DONE:
				handler.post(new Runnable(){
					@Override
					public void run() {
						btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
					}
				});
				break;
			case MSG_CONTRACT_MUSIC_LIST:
				layout_music.setVisibility(View.GONE);
				layout_musicSmall.setVisibility(View.VISIBLE);
				musicListShow=false;
				break;
			case MSG_SHOW_MUSIC_LIST:				
				if(lv_music != null && lv_music.getVisibility() != View.VISIBLE)
				{
				  Animation animation = AnimationUtils.loadAnimation(context, R.anim.translate_visible);
				  lv_music.startAnimation(animation);
				  lv_music.setVisibility(View.VISIBLE);
				}
				musicListShow=true;
				break;
			case MusicService.RESULT_MUSIC_PLAY_OVER:
				handler.post(new Runnable(){
					@Override
					public void run() {
						playProgress.setProgress(0);
						if(btn_playOrPause.isEnabled())
							btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
						isPause = true;
					}
				});
				break;
			case MSG_LOGAD_MUSIC_IMAGE:
				handler.post(new Runnable(){
					@Override
					public void run() {
						if(musicList != null)
						{
							String url = musicList.get(position).getPhoto();
							int musicImageId = musicList.get(position).getId();
							setMusicPhoto(url,musicImageId);
						}
					}
				});
				break;
			case MSG_REFRESH_MUSIC_LIST:
				musicAdapter.notifyDataSetChanged();
				break;
			case MusicService.RESULT_MUSIC_PROGRESS_SEEK_WAITTING://快进时更新进度条显示
				final int displayFlag = msg.arg1;
				handler.post(new Runnable(){
					@Override
					public void run() {
						progressBar_SeekWait.setVisibility(displayFlag);						
					}
				});
				break;
			default:
				Message newMsg = new Message();
				newMsg.copyFrom(msg);
				mHandler.sendMessage(newMsg);
				break;
			}

		};
	};

	OnClickListener PlayOnclickLister = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
			   VoiceSdkService.mSpeechRecognizer.abort();
			else
			   VoiceAssistantService.mSpeechRecognizer.abort();
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
//			mHandler.sendEmptyMessage(NewAssistActivity.MSG_LISTVIEW_TO_LAST_PAGE);
			mHandler.sendEmptyMessage(NotifyUiHandler.MSG_LISTVIEW_TO_LAST_PAGE);
			if (view == btn_playOrPause) {
				if (isPause) {
					play(isLoop);
				} else {
					pause();
				}
				return;
			}
			if(view == btn_music_list)
			{
				if (lv_music == null) {
					return;
				}				
				if(lv_music.getVisibility() == View.VISIBLE)
				{
				  Animation animation = AnimationUtils.loadAnimation(context, R.anim.translate_invisible);
				  lv_music.startAnimation(animation);
				  lv_music.setVisibility(View.INVISIBLE);
				}
				else
				{
				  Animation animation = AnimationUtils.loadAnimation(context, R.anim.translate_visible);
				  lv_music.startAnimation(animation);
				  lv_music.setVisibility(View.VISIBLE);
				}
				return;
			}
			if (view == btn_last) {
				//if(musicList !=null && position == 0 )
					//CustomToast.showShortText(getContext(),getResources().getString(R.string.music_is_last_music));
				btn_playOrPause.setEnabled(true);
				prev();
				return;
			}
			if (view == btn_next) {
				btn_playOrPause.setEnabled(true);
				//if(musicList !=null && position == musicList.size()-1 )
					//CustomToast.showShortText(getContext(),getResources().getString(R.string.music_last_song));
				next();
				return;
			}
			if (view == btn_close || view == btn_close2) {
				position=0;
				closeMusicServer();
				setVisibility(View.GONE);
				isPause=false;
				return;
			}
			if(view == btn_close_music)
			{
				if(lv_music != null && lv_music.getVisibility() == View.VISIBLE){
					lv_music.setVisibility(View.GONE);
				}
				//pause();
				closeMusicServer();
				setVisibility(View.GONE);
				if(lv_music != null ) {
					lv_music.setVisibility(View.GONE);
				}
				isPause=true;
				return;
			}
		}
	};
	
	private class MusicAdapter extends BaseAdapter{
		private List<MusicEntity> list;
		public MusicAdapter(List<MusicEntity> list){
			this.list=list;
		}
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int positionlocal, View convertView, ViewGroup parent) {
			MusicCacheView  musicCacheView;
			if(convertView==null){
				musicCacheView=new MusicCacheView();
				convertView=LayoutInflater.from(getContext()).inflate(R.layout.layout_music_item,null);
				musicCacheView.tv_index=(TextView) convertView.findViewById(R.id.tv_index);
				musicCacheView.tv_name=(TextView) convertView.findViewById(R.id.tv_name);
				musicCacheView.tv_author=(TextView) convertView.findViewById(R.id.tv_author);
				musicCacheView.imgv_recource_status=(ImageView) convertView.findViewById(R.id.imgv_recource_status);
				musicCacheView.view_bar = convertView.findViewById(R.id.view_bar);
				convertView.setTag(musicCacheView);
			}else{
				musicCacheView=(MusicCacheView) convertView.getTag();
			}
			musicCacheView.tv_index.setText(String.valueOf(positionlocal + 1));
			if(position == positionlocal )
				musicCacheView.view_bar.setVisibility(View.VISIBLE);
			else
				musicCacheView.view_bar.setVisibility(View.GONE);
			MusicEntity music=list.get(positionlocal);
			if(music.getName()!=null)
				musicCacheView.tv_name.setText(music.getName());
			String des = "";
			if(music.getAuthor()!= null)
				des += music.getAuthor();
			if(music.getAlbum() != null)
				des += "·" + music.getAlbum();
			if(des.length() > 0){
				des = des.replaceAll("<unknown>", getResources().getString(R.string.unknow));
				des = des.replaceAll("download", getResources().getString(R.string.unknow));
			}
			musicCacheView.tv_author.setText(des);
			if(music.getUrl()!=null&&!"".equals(music.getUrl())){
				if("http".equals(music.getUrl().subSequence(0, 4))){
					musicCacheView.imgv_recource_status.setImageResource(R.drawable.music_type_cloud);
				}else{
					musicCacheView.imgv_recource_status.setImageResource(R.drawable.music_type_local);
				}
			}
			return convertView;
		}
		
	}
	
	private static class MusicCacheView{
		TextView tv_index;
		TextView tv_name;
		TextView tv_author;
		ImageView imgv_recource_status;
		View view_bar;
	}

	/**
	 * 
	 * @param command  static music
	 * @param param  play position,Starting from 0
	 * @return
	 */
	public boolean controlMusic(String command, int param)
	{
		if(checkClosePlay()){
			//Log.i("MusicPlayerview","controlMusic() command = "+command);
			if(command.equalsIgnoreCase(MUSIC_PLAY)){
				play(false);
			}
			if(command.equalsIgnoreCase(MUSIC_PAUSE)){
				pause();		
			}
			if(command.equals(MUSIC_RESUME)){
				skipTo(position);
			}
			if(command.equalsIgnoreCase(MUSIC_PREV)){
				if(position >0){
					position--;
					skipTo(position);
				}else{
					CustomToast.showShortText(getContext(), getResources().getString(R.string.music_is_last_music));
				}
			}
			if(command.equalsIgnoreCase(MUSIC_NEXT)){
				if(musicList !=null && musicList.size() > 0){
					if(position < musicList.size() -1){
						position ++;
						skipTo(position);
					}
				}else{
					LogOutput.e("TAG",getResources().getString(R.string.music_list_is_null));
				}
			}
		
			if(command.equalsIgnoreCase(MUSIC_CLOSE)){
				closeMusicServer();
				if (lv_music != null && lv_music.getVisibility()== View.VISIBLE) {
					lv_music.setVisibility(View.GONE);
				}
				this.setVisibility(View.GONE);
			}
			
			if(command.equalsIgnoreCase(MUSIC_FIRST)){
				skipTo(0);
				position = 0;
			}
			
			if(command.equalsIgnoreCase(MUSIC_LAST)){
				if(musicList !=null && musicList.size() > 0){
					skipTo(musicList.size() -1);
					position = musicList.size() -1;
				}else{
					LogOutput.e("TAG",getResources().getString(R.string.music_list_is_null));
				}
			}
			if(MUSIC_INDEX.equalsIgnoreCase(command)){
				position = param ;
				skipTo(param);
			}
			
			if(MUSIC_LOOP.equalsIgnoreCase(command)){
				if (mPlaylist != null) {
					mPlaylist.setPlayMode(Playlist.PLAY_MODE_LIST_LOOP);
				}
				//loop();
			}
			if(MUSIC_RANDOM.equalsIgnoreCase(command)){
				if (mPlaylist != null) {
					mPlaylist.setPlayMode(Playlist.PLAY_MODE_RANDOM);
				}
				//random();
			}
			if(MUSIC_INDEX_LOOP.equalsIgnoreCase(command)){
				if (mPlaylist != null) {
					mPlaylist.setPlayMode(Playlist.PLAY_MODE_SINGLE_LOOP);
				}
				if (mPlaylist.getSelectedIndex() != param){
					skipTo(param);
				}
				//position = param ;
				//indexloop(param);
			}
		}
		return true;
	}
	
	
	private boolean checkClosePlay(){
		if(musicList != null && musicList.size() > 0 ){
			return true;
		}else{
			//CustomToast.showShortText(getContext(), getResources().getString(R.string.music_the_player_is_closed));
			return false;
		}
	}
	
	private void setMusicPhoto(String url,int id){
		if(url == null || "".equals(url) || ( url.length()>4&&!"http".equalsIgnoreCase(url.substring(0, 4)))){
			url = getAlbumArt(id);
			if(url !=null && !"".equals(url)){
				File file = new File(url);
				if(file.exists()){
					try {
						FileInputStream fs = new FileInputStream(file);
						Bitmap bitmap = BitmapFactory.decodeStream(fs);
						if(bitmap !=null){
							imgv_author.setImageBitmap(bitmap);
							Playlist.setCurrentEntryArtWork(bitmap);
							Intent intent = new Intent(context, MusicService.class);
							intent.setAction(MusicService.UPDATE_ARTWORK);
							context.startService(intent);
							//imgv_author2.setImageBitmap(bitmap);
						}else{
							imgv_author.setImageResource(R.drawable.icon_defalut_music_author);
							//imgv_author2.setImageResource(R.drawable.icon_defalut_music_author);
						}
							
					} catch (FileNotFoundException e) {
						LogOutput.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				}else{
					imgv_author.setImageResource(R.drawable.icon_defalut_music_author);
					//imgv_author2.setImageResource(R.drawable.icon_defalut_music_author);
				}
			}else{
				imgv_author.setImageResource(R.drawable.icon_defalut_music_author);
				//imgv_author2.setImageResource(R.drawable.icon_defalut_music_author);
			}
		}else if(url !=null && !"".equals(url)){
			ImageLoaderUtil.loadImageAsync(imgv_author, url, LocalPathUtil.IMAGE_MUSIC, getResources().getDrawable(R.drawable.icon_defalut_music_author),150, this);
			//ImageLoaderUtil.loadImageAsync(imgv_author2, url, LocalPathUtil.IMAGE_MUSIC, getResources().getDrawable(R.drawable.icon_defalut_music_author),150);
			//imgv_author2.setTag(false);
		}else{
			imgv_author.setImageResource(R.drawable.icon_defalut_music_author);
			//imgv_author2.setImageResource(R.drawable.icon_defalut_music_author);
		}
		
	}
	
	
	/**
	 * 获得专辑图片路径
	 * @param trackId
	 * @return
	 */
	private String getAlbumArt(int trackId) {//trackId是音乐的id
        String mUriTrack = "content://media/external/audio/media/#";
        String[] projection = new String[] {"album_id"};
        String selection = "_id = ?";
        String[] selectionArgs = new String[] {Integer.toString(trackId)};        
        Cursor cur = context.getContentResolver().query(Uri.parse(mUriTrack), projection, selection, selectionArgs, null);
        int album_id = 0;
        if(cur != null){
	        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
	            cur.moveToNext();
	            album_id = cur.getInt(0);
	        }
	        cur.close();
	        cur = null;
        }
        
        if (album_id < 0) {
            return null;
        }
        String mUriAlbums = "content://media/external/audio/albums";
        projection = new String[] {"album_art"};
        cur = context.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);

        String album_art = null;
        if(cur != null){
	        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
	            cur.moveToNext();
	            album_art = cur.getString(0);
	        }
	        cur.close();
	        cur = null;
        }
        
        return album_art;
    } 
	
	
	public void speakPause(){
		if(musicList != null && musicList.size() > 0 ){
			//Log.i("MusicPlayerview","speakPause() isPause = "+isPause);
			if (!isPause || m_bSpeakPause) {
				//Log.i("MusicPlayerview","speakPause()11 isPause = "+isPause);
				btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
				Intent intent = new Intent(context, MusicService.class);
				intent.setAction(MusicService.SPEAK_PAUSE);
				context.startService(intent);
				m_bSpeakPause = true;
				isPause = true;
				
				if(lv_music != null && lv_music.getVisibility() == View.VISIBLE)
				{
				  Animation animation = AnimationUtils.loadAnimation(context, R.anim.translate_invisible);
				  lv_music.startAnimation(animation);
				  lv_music.setVisibility(View.INVISIBLE);
				}	
			}		
		}
	}
	
	public void speakRecover(){
		if(musicList != null && musicList.size() > 0 ){
			//Log.i("MusicPlayerview","speakRecoer() isPause = "+isPause);
			if (m_bSpeakPause) {
				//Log.i("MusicPlayerview","speakRecoer()22 isPause = "+isPause);
				btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
				Intent intent = new Intent(context, MusicService.class);
				intent.setAction(MusicService.SPEAK_RECOVER);
				context.startService(intent);
				m_bSpeakPause = false;
				isPause = false;
			}
		}
	}
	
	public void updateMusicViewStatus() {
		if (lv_music != null) {
			if (!MusicService.isPlaying()) {
				btn_playOrPause.setBackgroundResource(R.drawable.icon_pause);
				isPause = true;
			}
			else {
				btn_playOrPause.setBackgroundResource(R.drawable.icon_play);
				isPause = false;
			}
		}
	}

	public void hideMusicList() {
		if (lv_music != null) {
			if (lv_music.getVisibility() == View.VISIBLE) {
				Animation animation = AnimationUtils.loadAnimation(context,
						R.anim.translate_invisible);
				lv_music.startAnimation(animation);
				lv_music.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	public void callBack(Object obj, int param1, int param2) {
		if (obj instanceof Bitmap) {
			Playlist.setCurrentEntryArtWork((Bitmap) obj);

			Intent intent = new Intent(context, MusicService.class);
			intent.setAction(MusicService.UPDATE_ARTWORK);
			context.startService(intent);
		}
	}
}
