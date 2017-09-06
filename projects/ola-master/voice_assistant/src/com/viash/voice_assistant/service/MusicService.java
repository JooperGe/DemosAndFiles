package com.viash.voice_assistant.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.entity.MusicEntity;
import com.viash.voice_assistant.media.Playlist;
import com.viash.voice_assistant.widget.MusicPlayerView;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.LogOutput;

/**
 * 播放音乐服务
 */
@SuppressLint("HandlerLeak")
public class MusicService extends Service implements StreamDownloaderListener {
	private static final boolean DEBUG = false;
	private static final String TAG = "MusicService";
	private static MediaPlayer mp;
	private static Handler musicHandler;
	private static Handler localHandler;
	public static final int RESULT_PLAY_NEXT_OR_LAST = 1;
	public static final int RESULT_PLAY_END = 2;// 所以音乐播放完成
	public static final int RESULT_IS_FIRST = 3;// 已经是第一首
	public static final int RESULT_IS_LAST = 4;// 已经是最后一首
	public static final int RESULT_MUSIC_MAX_PROGRESS = 5;// 最大进度
	public static final int RESULT_MUSIC_UPDATE_PROGRESS = 6;// 最大进度
	public static final int RESULT_MUSIC_PAUSEING = 7;// 音乐文件还没有准备好
	public static final int RESULT_MUSIC_PAUSE_DONE = 8;// 音乐准备完成。
	public static final int RESULT_MUSIC_MSG = 9;// 消息
	public static final int RESULT_MUSIC_PLAY_FAIL = 10;
	public static final int RESULT_MUSIC_PLAY_OVER = 11;
	
	public static final int SPEAK_PAUSE_MSG = 12; //Loneway, when VR or TTS playing, pause the music.
	public static final int SPEAK_RECOVER_MSG = 13; //Loneway recover pause when TTS playing.
    public static final int RESULT_MUSIC_PROGRESS_SEEK_WAITTING = 14;// 最大进度
	public static final int RESULT_MUSIC_DOWNLOAD_FAIL = 15; //下载失败，自动下一首。
	
	private static int playFailedTimes = 0;
	private static int downloadFailedTimes = 0;
    /**
     * control command
     */
	public static final String INIT = "init";
	public static final String PLAY = "play";
	public static final String LAST = "last";
	public static final String NEXT = "next";
	public static final String PAUSE = "pause";
	public static final String STOP = "stop";
	public static final String PLAY_TO = "playTo";
	public static final String SEEK_TO = "seekTo";
	public static final String LOOP = "loop";
	public static final String RANDOM = "random";
	public static final String INDEX_LOOP ="index_loop";
	public static final String SPEAK_PAUSE = "speak_pause";
	public static final String SPEAK_RECOVER = "speak_recover";
	public static final String UPDATE_ARTWORK = "update_artwork";
	
	private static final int MSG_PLAY = 1;
	private static final int MSG_PREV = 2;
	private static final int MSG_NEXT = 3;
	private static final int MSG_PAUSE = 4;
	private static final int MSG_STOP = 5;
	private static final int MSG_FILE_NOT_EXIST = 6;
	private static final int MSG_START_STREAMING = 7;
	
	
	// Stream Media Player
	private static final String PLAY_CACHE_FILE = "playcache.bat";
	private static final int DOWNLOAD_INTERVAL = 100;	// kb
	private static final int MIN_PLAY_BUFFER = 5000;
	private static int nowInterval = 0;
	private static StreamDownloader mStreamDownloader = null;
	private static File bufferedFile;
	private static File downloadSaveFile = null;
	private static boolean isStreamStart = false;
	private static int readPoint = 0;
	public static boolean isUsingStreaming = false;
	public static boolean serverIsStart = false;
	public static boolean pauseDone = false;
	

	/* add by Loneway for auto play judgement */
	private boolean m_bAutoplay = false;
	/*end*/
	
    private int progress_seekTo = 0;
    private CountDownTimer mSeekingTimer = null;
    private int downloadPercent = 0;//下载百分比
    private int maxLength = 0;//下载的文件的长度
    private int totalKbRead = 0;//总共下载的千字节数
    private boolean seeking = false;//是否是快进状态
    
    private static AudioManager mAudioManager = null;
    private static OnAudioFocusChangeListener mAudioFocusChangeListener = null;
   
    private static boolean mIsPrepareOK = false;
    private static boolean playAfterPrepare = false;
    private static boolean isUserPause = false;
    
    private static MusicEntity mEntry = null;
    
    private static Playlist mPlaylist;

    /**
     * fail handle params.
     */
	private static final long FAIL_TIME_FRAME = 1000;
	private static final int ACCEPTABLE_FAIL_NUMBER = 2;//Acceptable number of fails within FAIL_TIME_FRAME
	private long mLastFailTime; //Beginning of last FAIL_TIME_FRAME
	private long mTimesFailed; //Number of times failed within FAIL_TIME_FRAME
    
    private MusicNotificationButtonReceiver mMusicNotificationButtonReceiver;
    
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onCreate() {
		if (Build.VERSION.SDK_INT >= 8) {
			mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
			mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
				private boolean canRecover = false;
			    @TargetApi(Build.VERSION_CODES.FROYO)
				public void onAudioFocusChange(int focusChange) {  
			        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
			        	if (!isPlaying()) {
				        	isUserPause= true;
			        		return;
			        	}
			        	canRecover = true;
			        	//Log.e("ppp", "MusicService AUDIOFOCUS_LOSS_TRANSIENT");
			        	pauseMusic();
			        	notificationMusicIsPauseDone();

			        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			        	mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
			        	if (!isPlaying()) {
				        	isUserPause= true;
				        	return;
			        	}
			        	//Log.e("ppp", "MusicService AUDIOFOCUS_LOSS");
			        	pauseMusic();
			        	notificationMusicIsPauseDone();
			        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
			        	if (!isPlaying()) {
			        		canRecover = true;
			        		//Log.e("ppp", "MusicService AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
				        	pauseMusic();
				        	notificationMusicIsPauseDone();
			        	}
			        	isUserPause= true;
			        }else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
			        	//Log.e("ppp", "MusicService AUDIOFOCUS_GAIN canRecover = " + canRecover);
			        	if (canRecover) {
			        		resumeMusicPlay();
			        		startPlayMusicProgressThread();
			        		canRecover = false;
			        	}
			        	isUserPause= false;
			        }
			    }  
			};
			//Log.e("ppp", "MusicService requestAudioFocus ");
			mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
		}
		mStreamDownloader = new StreamDownloader(this, this);
		serverIsStart = true;
		initLocalHandler();
		mMusicNotificationButtonReceiver = new MusicNotificationButtonReceiver();
		IntentFilter filter = new IntentFilter(MusicNotificationService.NOTIFICATION_MUSIC_CANCEL_ACTION);
		filter.addAction(MusicNotificationService.NOTIFICATION_MUSIC_PLAYPAUSE_ACTION);
		filter.addAction(MusicNotificationService.NOTIFICATION_MUSIC_NEXT_ACTION);
		registerReceiver(mMusicNotificationButtonReceiver, filter);
		
	}
	
	private void initLocalHandler() {
		localHandler = new Handler () {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_PLAY:
					play();
					break;
				case MSG_PREV:
					prev();
					break;
				case MSG_NEXT:
					next();
					break;
				case MSG_PAUSE:
					pause();
					break;
				case MSG_STOP:
					stop();
					break;
				case MSG_FILE_NOT_EXIST:
					playFail();
					//next();
					break;
				case MSG_START_STREAMING:
					String path = "";
					if (msg.obj != null) {
						path = (String) msg.obj;
					}
					startStreaming(path);
					break;
				}
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if(intent != null){
			if (INIT.equalsIgnoreCase(intent.getAction())) {
				if (isPlaying()) {
					mp.pause();
				}
				m_bAutoplay = intent.getBooleanExtra("Auto_play", false);
				isUserPause = false;
				if (m_bAutoplay){
					playChange();
				};
				return;
			}
			if (PLAY.equalsIgnoreCase(intent.getAction())) {
				isUserPause = false;
				play();
				return;
			}
			if (PAUSE.equalsIgnoreCase(intent.getAction())) {
				pause();
				isUserPause = true;
				return;
			}
			if (STOP.equalsIgnoreCase(intent.getAction())) {
				seekCancel();
				stop();
				isUserPause = true;
				return;
			}
			if (LAST.equalsIgnoreCase(intent.getAction())) {
				isUserPause = false;
				prev();
				return;
			}
			if (NEXT.equalsIgnoreCase(intent.getAction())) {
				isUserPause = false;
				next();
				return;
			}
			if (PLAY_TO.equalsIgnoreCase(intent.getAction())) {
				isUserPause = false;
				int playPosition = intent.getIntExtra("position", 0);
				skipTo(playPosition);
				return ;
			}
			if (SEEK_TO.equalsIgnoreCase(intent.getAction())) {
				isUserPause = false;
				int seek = intent.getIntExtra("seek", 0);
				seekTo(seek);
				return ;
			}
			/*if (SPEAK_PAUSE.equalsIgnoreCase(intent.getAction())) {
				isUserPause = true;
				pause();
				return ;
			}
			if (SPEAK_RECOVER.equalsIgnoreCase(intent.getAction())) {
				isUserPause = false;
				play();
				return ;
			}*//*
			
			if(RANDOM.equalsIgnoreCase(intent.getAction())){
				PLAY_MODEL =PLAY_MODE_RANDOM;
				next();
				return ;
			}
			if(LOOP.equalsIgnoreCase(intent.getAction())){
				PLAY_MODEL =PLAY_MODE_LIST_LOOP;
				return ;
			}
			if(INDEX_LOOP.equalsIgnoreCase(intent.getAction())){
				PLAY_MODEL =PLAY_MODE_SINGLE_LOOP;
				return ;
			}*/
			if (UPDATE_ARTWORK.equalsIgnoreCase(intent.getAction())) {
				if (MusicNotificationService.isStarted) {
					Intent newintent = new Intent(this, MusicNotificationService.class);
					newintent.setAction(MusicService.UPDATE_ARTWORK);
					startService(newintent);
					return ;
				}
			}
		}
		
	}

	public static void initHandler(Handler handler) {
		musicHandler = handler;
	}
	
	public static void setPlaylist(Playlist playlist) { 
		if (playlist != null && !playlist.isEmpty()) {
			mPlaylist = playlist;
		}
		else {
			mPlaylist = null;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	private void cleanUp() {
		// clean-up job		
		mIsPrepareOK = false;
		if (mStreamDownloader != null) {
			mStreamDownloader.stop();
			isUsingStreaming = false;
		}
		if (mp != null) {
			try {
				mp.stop();
			} catch (IllegalStateException e) {
				// this may happen sometimes
			} finally {
				mp.release();
				mp = null;
			}
		}
		if (Build.VERSION.SDK_INT >= 8) {
			mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		}
	}

	public void play() {
		Log.i(TAG, "play");	
		if(mPlaylist != null){
			if(mp == null){
				mp = createMediaPlayer(mPlaylist.getSelectedTrack());
			}

			if( (mp != null && mEntry != mPlaylist.getSelectedTrack()) || progress_seekTo != 0){
				Log.i(TAG, "Play(), need cleanUp. and the progress_seek_to is " + progress_seekTo);
				mp.reset();//cleanUp(); // reset the media player
				mp = createMediaPlayer(mPlaylist.getSelectedTrack());
			}
			
			if(mp == null) {
				return;
			}

			if(mIsPrepareOK){
				// prevent double-press
				if(!mp.isPlaying() && !isUserPause){
					// can play the song
					//Log.i(TAG, "Player [playing] "+mCurrentMediaPlayer.playlistEntry.getTrack().getName());
					requestAudioFocesAndPlay();
				}
			} else {
				// tell the media player to play the song as soon as it ends preparing
				playAfterPrepare = true;
			}

			Intent intent = new Intent(this, MusicNotificationService.class);
			intent.setAction(MusicService.PLAY);
			startService(intent);
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public void pause() {
		//Log.e("ppp", "pause");
		if(mp != null){
			// not prepared
			if(!mIsPrepareOK){
				playAfterPrepare = false;
				return;
			}

			// check if we play, then pause
			if(mp.isPlaying()){
				mp.pause();
				Intent intent = new Intent(this, MusicNotificationService.class);
				intent.setAction(MusicService.PAUSE);
				startService(intent);
				return;
			}
		}
		//isUserPause = true;
    	notificationMusicIsPauseDone();		
    	if (Build.VERSION.SDK_INT >= 8) {
			mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		}
	}

	public void stop() {
		Log.i(TAG, "stop");
		cleanUp();
		isStreamStart = false;
	}
	
	public void skipTo(int position) {
		Log.i(TAG, "skipTo " + position);
		if(!serverIsStart) return;
		if (mPlaylist.select(position)) {
			playChange();
		}
		else {
			sendMessageToMusicView(RESULT_IS_LAST, 0, 0);
		}
	}

	public void prev() {
		Log.i(TAG, "prev");
		if(!serverIsStart) return;
		if (mPlaylist.selectPrev()) {
			playChange();
		}
		else {
			sendMessageToMusicView(RESULT_IS_FIRST, 0, 0);
		}
	}

	public void next() {
		Log.i(TAG, "next");
		if(!serverIsStart) return;
		if (mPlaylist.selectNext()) {
			playChange();
			Intent intent = new Intent(this, MusicNotificationService.class);
			intent.setAction(MusicService.NEXT);
			startService(intent);
		}else {
			sendMessageToMusicView(RESULT_IS_LAST, 0, 0);
		}
	}

	private void playChange() {
		Log.i(TAG, "playChange");
		progress_seekTo = 0;
		seekCancel();
		if (mStreamDownloader!= null) {
			mStreamDownloader.stop();
		}
		isUsingStreaming = false;
		updateProgressWhileSeek(View.VISIBLE);
		if(isPlaying()) {
			mp.pause();
		}
		MusicEntity entry = mPlaylist.getSelectedTrack();
		String path = entry.getUrl();
		updateMaxProgress((int) entry.getTime());
		notificationPlayNextOrLastMusic();
		if (path != null && !"".equals(path) && path.length() > 4) {
			
			LogOutput.i(TAG, "音乐准备播放，URL:" + path);
			
			if(path.length() > 4 && "http".equalsIgnoreCase(path.substring(0, 4))){
				fileExist_HttpCheck(path);
			} else {
				boolean fileExist = fileExist(path);
				if (fileExist) {
					isUsingStreaming = false;
					downloadSaveFile = null;
					play();
				} else {
					LogOutput.e(TAG, path + "  找不到文件");
					playFail();
					updateMaxProgress(0);
				}
			}

		} else {
			Log.i(TAG, "music path is null");
		}
	}

	private void startStreaming(String path){
		if (mStreamDownloader!= null) {
			mStreamDownloader.stop();
		}
		isUsingStreaming = true;
		String cookie = null;
		if(path.indexOf("http://cc.stream.qqmusic.qq.com") >= 0){
			cookie = "qqmusic_uin=12345678; qqmusic_key=12345678; qqmusic_fromtag=30;";
		}
		else if(path.indexOf("qqmusic.qq.com") > 0){
			cookie = "qqmusic_fromtag=10; qqmusic_sosokey=4D96476733A6D833E90FEA9E590408D171B92452775E15FB;";
		}

		mStreamDownloader.init(path, cookie);
		isStreamStart = false;
		readPoint = 0;
		bufferedFile = new File(this.getCacheDir(), PLAY_CACHE_FILE);
		if (bufferedFile.exists()) {
			bufferedFile.delete();
			/*
			while(bufferedFile.exists()){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			*/
		}
		mStreamDownloader.start();
	}
	
	protected void notificationCloseMusicFrame() {
		sendMessageToMusicView(RESULT_PLAY_END, 0, 0);
	}

	private void notificationPlayNextOrLastMusic() {
		sendMessageToMusicView(RESULT_PLAY_NEXT_OR_LAST, mPlaylist.getSelectedIndex(), 0);
	}
	
	//currently this notification is not used.
	@SuppressWarnings("unused")
	private void notificationPlayOver(){
		sendMessageToMusicView(RESULT_MUSIC_PLAY_OVER, 0, 0);
	}

	private void notificationMusicIsPauseDone() {
		sendMessageToMusicView(RESULT_MUSIC_PAUSE_DONE, 0, 0);
	}

	private void updateMaxProgress(int progress) {
		maxLength = progress;
		sendMessageToMusicView(RESULT_MUSIC_MAX_PROGRESS, progress, 0);
	}

	private void updateProgress() {
		if(isPlaying()){
			sendMessageToMusicView(RESULT_MUSIC_UPDATE_PROGRESS, mp.getCurrentPosition(), 0);
		}
	}
	
	private void updateProgressWhileSeek(int displayFlag) {//快进时同步更新进度条显示
		if(mp != null){
			sendMessageToMusicView(RESULT_MUSIC_PROGRESS_SEEK_WAITTING, displayFlag,0);
		}
	}
	
	private void updateProgressToInt(int progress) {//更新进度条到指定的进度
		if(mp != null){
			sendMessageToMusicView(RESULT_MUSIC_UPDATE_PROGRESS, progress,0);
		}
	}
	
	private void playFail() {
		playFailedTimes ++;
		sendMessageToMusicView(RESULT_MUSIC_PLAY_FAIL, 0, 0);
		if (playFailedTimes > Playlist.Instance().size()) {
			stop();
			//playFailedTimes = 0;
			return;
		}
		next();
		if (isUserPause) {
			pause();
		}
	}	

	private void downLoadFail() {
		sendMessageToMusicView(RESULT_MUSIC_DOWNLOAD_FAIL, 0, 0);
		downloadFailedTimes ++;
		if (downloadFailedTimes > Playlist.Instance().size()) {
			stop();
			return;
		}
		next();
		if (isUserPause) {
			pause();
		}
	}
	
	Thread progresThread = null;
	private void startPlayMusicProgressThread() {
		if (progresThread != null) {
			try {
				progresThread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		progresThread = new Thread() {
			@Override
			public void run() {
				while (isPlaying()) {
					updateProgress();
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		progresThread.start();
	}

	private void seekTo(int progress) {
		if((maxLength == 0)||(mp == null))
		{
		   updateProgressToInt(progress_seekTo); 	
		   return;
		}
		int seek_percent = 100*progress/maxLength;
		if(isUsingStreaming == false) {//如果已经下载则直接快进
			mp.seekTo(progress);
			return;
		}
		Log.i(TAG, "progress is " + progress);
		
		progress_seekTo = progress;

		if(seek_percent < downloadPercent ) {//快进小于已下载则直接快进
			Log.i("MusicService","seekTo()11 progress_seekTo = "+progress_seekTo+" mp.getCurrentPosition() = "+mp.getCurrentPosition()+" seek_percent = "+seek_percent+" downloadPercent = "+downloadPercent);
			mp.seekTo(progress);
			mStreamDownloader.transferDataNow();
		}
		else {//快进大于已经下载的百分比，需等待下载好再播放	
			seeking = true;
			mp.pause();	
			updateProgressWhileSeek(View.VISIBLE);
			if (mSeekingTimer != null) {
				mSeekingTimer.cancel();
				mSeekingTimer = null;
			}
			mSeekingTimer = new CountDownTimer(3*60000,5000){ //一个是倒计时时间量，另一个是处理onTick()回调时间间隔，均是毫秒计数  
				public void onTick(long millisUntilFinished) {
					mStreamDownloader.transferDataNow();					
				} 

				public void onFinish() {// 规定时间仍没下载完到快进位置，则网络超时
					seeking = false;
					updateProgressWhileSeek(View.GONE);
					if (isPlaying()) {
						return;
					}
					CustomToast.makeToast(MusicService.this, "网络超时,播放下一首歌曲。");//,Toast.LENGTH_LONG).show();
					next();
				}
		        						            
		    }.start();
		    
		}
	}
	private void seekCancel()//取消快进回调
	{
		if(seeking)
		{
			updateProgressWhileSeek(View.GONE);
			if(mSeekingTimer!=null) {
				mSeekingTimer.cancel();
				mSeekingTimer = null;
			}
			seeking = false;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onDestroy() {
		stop();
		serverIsStart = false;
		if(mStreamDownloader != null){
			mStreamDownloader.stop();
		}
		if(mSeekingTimer!=null)
		  mSeekingTimer.cancel();
		
		if (Build.VERSION.SDK_INT >= 8) {
			mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		}
		Intent intent = new Intent(MusicService.this, MusicNotificationService.class);
		intent.setAction(MusicService.STOP);
		startService(intent);
		
		unregisterReceiver(mMusicNotificationButtonReceiver);
	}

	private boolean fileExist(final String filePath) {
		boolean exist=false;
		File file = new File(filePath);
		if (file.exists()) {
			exist = true;
		} else {
			exist = false;
		}
		return exist;
	}
	
	private void fileExist_HttpCheck(final String filePath) {
		new Thread () {
			public void run() {
				try {
					String cookie = null;
					if(filePath.indexOf("http://cc.stream.qqmusic.qq.com") >= 0){
						cookie = "qqmusic_uin=12345678; qqmusic_key=12345678; qqmusic_fromtag=30;";
					}
					else if(filePath.indexOf("qqmusic.qq.com") > 0){
						cookie = "qqmusic_fromtag=10; qqmusic_sosokey=4D96476733A6D833E90FEA9E590408D171B92452775E15FB;";
					}
					URL url = new URL(filePath);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					if(cookie != null){
						conn.setRequestProperty("Cookie", cookie);
					}
					
					if (conn.getResponseCode() == 200) {
						fileExist_HttpResult(true, filePath);
					} else {
						fileExist_HttpResult(false, filePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
					fileExist_HttpResult(false, filePath);
				}
			}
		}.start();
	}
	
	private void fileExist_HttpResult(boolean result, String path) {
		if (!result) {
			/*playFail();//notify view and play next song.
			Log.i(TAG, "this music is not exist.");*/
			localHandler.sendEmptyMessage(MSG_FILE_NOT_EXIST);
			return;
		}
		String filename = getMD5(path);
		LogOutput.d(TAG, "filename: " + filename);
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			// downloadSaveFile = new File(Environment.getExternalStorageDirectory() + "/" + SavedData.getMusicDownloadPath(), filename);
			File downloadPath = new File(Environment.getExternalStorageDirectory() + "/voice_assist/music");
			if(!downloadPath.exists()) downloadPath.mkdirs();
			downloadSaveFile = new File(downloadPath.getAbsolutePath() + "/" + filename);
			if(!downloadSaveFile.exists()){
				LogOutput.i(TAG, "download file is not exist, starting to downloading...");
				downloadSaveFile = null;
				Message msg =localHandler.obtainMessage(MSG_START_STREAMING);
				msg.obj = path;
				localHandler.sendMessage(msg);
			}else{
				LogOutput.i(TAG, "download file is exist, starting to playing...");
				isUsingStreaming = false;
				localHandler.sendEmptyMessage(MSG_PLAY);
			}
		}
	}

	@Override
	public void statusUpdate(int status) {
		switch(status){
		case StreamDownloader.REQUEST_SUCCESS:
			if(DEBUG) LogOutput.i(TAG, "request file success.");
			break;
		case StreamDownloader.REQUEST_FAILURE:
			LogOutput.e(TAG, "request file failure.");
			isStreamStart = false;
			downLoadFail();
			break;
		case StreamDownloader.DOWNLOAD_SUCCESS:
			LogOutput.i(TAG, "download success.");
			downloadFailedTimes = 0;
			break;
		case StreamDownloader.DOWNLOAD_FAILURE:
			LogOutput.e(TAG, "download failure.");
			isStreamStart = false;
			downLoadFail();//notify view and play next song.
			break;
		case StreamDownloader.DOWNLOAD_STOP:
			LogOutput.i(TAG, "download stop.");
			//stopPlayMusicProgressThread();
			break;
		}
	}	

	@Override
	public void downloadUpdate(int totalKbRead, File downloadFile, boolean force) {
		if(!serverIsStart) return;
		this.totalKbRead = totalKbRead*1024;//保存已经下载的千字节数
		if (!isStreamStart) {
			if (totalKbRead >= DOWNLOAD_INTERVAL) {
				nowInterval = 5;
				try {
					readPoint = moveFile(downloadFile, bufferedFile, readPoint);
					isStreamStart = true;
					play();
				}catch (IOException e) {
					e.getStackTrace();
					LogOutput.e(getClass().getName(), "Error initializing the MediaPlayer.");
				} catch (Exception e) {
					e.getStackTrace();
					LogOutput.e(getClass().getName(), "Error copying buffered content.");
				}
			}
		}else if(isStreamStart){
			if(DEBUG) LogOutput.d(getClass().getName(), "---downloadUpdate---: " + nowInterval + "/" + totalKbRead / DOWNLOAD_INTERVAL + " - " + mp.getCurrentPosition() + "/" + mp.getDuration());

			if(force){
				if(DEBUG) LogOutput.d(getClass().getName(), "Force transfer!");
				transferBufferToMediaPlayer(downloadFile, false, true);
				if(nowInterval != Integer.MAX_VALUE)
					nowInterval = (totalKbRead / DOWNLOAD_INTERVAL) * 2;
			}else{
				if(isPlaying() && mp.getCurrentPosition() > (mp.getDuration() - MIN_PLAY_BUFFER)){
					if(DEBUG) LogOutput.d(getClass().getName(), "play buffering...");
					transferBufferToMediaPlayer(downloadFile, false, false);
					
					// doesn't use interval buffering...
					nowInterval = Integer.MAX_VALUE;
				}else{
					if(totalKbRead / DOWNLOAD_INTERVAL > nowInterval){
						if(DEBUG) LogOutput.d(getClass().getName(), "interval buffering...");
						transferBufferToMediaPlayer(downloadFile, false, false);
						nowInterval = (totalKbRead / DOWNLOAD_INTERVAL) * 2;
					}
				}
			}
		}
	}
	
	@Override 
	public void setDownloadTotalSize(int totalSize)
	{
		downloadPercent = 100*totalKbRead/totalSize;
	}
	
	public static boolean isPlaying(){
		try {
			return mp != null && mIsPrepareOK && mp.isPlaying();
		}catch (Exception e){
			return false;
		}
	}
	
	public static boolean isPlayingState()
	{
		boolean ret = false;
		try {
			ret = mp != null && mIsPrepareOK && mp.isPlaying() |isUserPause;
		}catch (Exception e){
			return false;
		}
		return ret;
	}
	
	private void transferBufferToMediaPlayer(final File downloadFile, boolean isSave, boolean isForce) {
		Log.i(TAG, "transferBufferToMediaPlayer");
		updateProgressWhileSeek(View.GONE);
		try {
			readPoint = moveFile(downloadFile, bufferedFile, readPoint);
			
			if(progress_seekTo == 0 || (100 * progress_seekTo / maxLength) < downloadPercent)
			{
				if(isPlaying()) {
					mp.pause();
				}
				if (progress_seekTo == 0) {
					progress_seekTo = mp.getCurrentPosition();
				} 
				
				play();
			}
		} catch (Exception e) {
			LogOutput.e(getClass().getName(), "Error updating to newly loaded content.");
		}
		
		if(isSave){
			// update to show music duration time
			if (mp != null && mIsPrepareOK) {
				//notificationMusicIsPauseDone();
				updateMaxProgress(mp.getDuration());
			}
			// start to save file
			Runnable saveThread = new Runnable(){
				@Override
				public void run() {
					try {
						// check SD Card
						if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
							String filename = getMD5(mEntry.getUrl());
							File downloadPath = new File(Environment.getExternalStorageDirectory() + "/voice_assist/music");
							if(!downloadPath.exists()) downloadPath.mkdirs();
							downloadSaveFile = new File(downloadPath.getAbsolutePath() + "/" + filename);
							moveFile(downloadFile, downloadSaveFile, 0);
							// downloadFile.deleteOnExit();
							if (downloadFile.exists()) {
								downloadFile.delete();
							}
							isStreamStart = false;
							LogOutput.i(getClass().getName(), "file is saved.");
						}else{
							LogOutput.i(getClass().getName(), "sdcard is unmount!");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(saveThread).start();
		}
	}
	
	public int moveFile(File oldLocation, File newLocation, int skipPoint) throws IOException {
		if (oldLocation.exists()) {
			if(DEBUG) LogOutput.d(getClass().getName(), "moveFile: skipPoint=" + skipPoint);
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldLocation));
			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newLocation, true));
			reader.skip(skipPoint);
			
			try {
				byte buff[] = new byte[16384];
				int numChars;
				while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
					writer.write(buff, 0, numChars);
					skipPoint += numChars;
				}
			} catch (IOException e) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
					return skipPoint;
				} catch (IOException e) {
					LogOutput.e(getClass().getName(), "Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
				}
			}
		} else {
			throw new IOException( "Old location does not exist when transferring" + oldLocation.getPath() + " to " + newLocation.getPath());
		}
		return skipPoint;
	}
	
	private String getMD5(String str){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return toHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "null";
	}
	private String toHexString(byte[] in) {
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < in.length; i++){
			String hex = Integer.toHexString(0xFF & in[i]);
			if (hex.length() == 1){
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	@Override
	public void downloadSuccess(File downloadFile) {
		transferBufferToMediaPlayer(downloadFile, true, false);
		isUsingStreaming = false;
	}
	
	protected void sendMessageToMusicView(int what, int param1, int param2)
	{
		if(musicHandler != null)
		{
			Message msg = musicHandler.obtainMessage(what, param1, param2);
			musicHandler.handleMessage(msg);
		}
	}

	public static void pauseMusic()
	{
		//Log.e("ppp", "pauseMusic");
		if (isPlaying()) {
			mp.pause();
		}
		isUserPause= true;
	}

	public static void resumeMusicPlay()
	{
		Log.i(TAG, "resumeMusicPlay");
		if (mp != null && mIsPrepareOK) {
			mp.start();//requestAudioFocesAndPlay();
		}
		isUserPause= false;
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	public void requestAudioFocesAndPlay() {
		//Log.e("ppp", "requestAudioFocesAndPlay");
		mp.start();
		if (Build.VERSION.SDK_INT >= 8 && mAudioFocusChangeListener != null && mAudioManager != null) {
	    	mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
			mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
	                AudioManager.STREAM_MUSIC,
	                AudioManager.AUDIOFOCUS_GAIN);
		}
		startPlayMusicProgressThread();
		playFailedTimes = 0;
		updateProgressWhileSeek(View.GONE);
	}


	private MediaPlayer createMediaPlayer(MusicEntity entry){
		final MediaPlayer mp = new MediaPlayer();
		mEntry = entry;
		String path = entry.getUrl();
		
		if(path.length() == 0){ //path error. TODO
			stop();
			return null;
		}
		
		try {
			if (isUsingStreaming) {
				Log.i(TAG, "createMediaPlayer isUsingStreaming ");
				FileInputStream fis = new FileInputStream(bufferedFile);
				mp.setDataSource(fis.getFD());
				fis.close();
			}else {
				if (downloadSaveFile != null) {
					Log.i(TAG, "createMediaPlayer play the downloadSaveFile ");
					mp.setDataSource(downloadSaveFile.getPath());
				}
				else {
					Log.i(TAG, "createMediaPlayer play the Localfile");					
					mp.setDataSource(path);
				}
			}

			mp.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer mp) {
					Log.i(TAG, "onCompletion");
					if (isStreamStart) {
						needLoading();
					}
					else {
						next();
					}
				}

			});

			mp.setOnPreparedListener(new OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer mp) {
					Log.i(TAG, "onPrepared");
					/*mIsPrepareOK = true;
					if (progress_seekTo != 0) {
						mp.seekTo(progress_seekTo);
						Log.i(TAG, "onPrepared, need Seek, seek to " + progress_seekTo);
						progress_seekTo = 0;
					}
					if (isUserPause) {
						return;
					}

					// start playing
					if(mPlaylist.getSelectedTrack() == mEntry && playAfterPrepare){
						playAfterPrepare = false;
						requestAudioFocesAndPlay();
					}*/
				}

			});
			
			mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {

				@Override
				public void onSeekComplete(MediaPlayer mp) {
					Log.i(TAG, "onSeekComplete");
					if (!isPlaying() && !isUserPause) {
						requestAudioFocesAndPlay();
					}
					if(mSeekingTimer!=null) {
						mSeekingTimer.cancel();
						Log.i("MusicService","onSeekComplete()11  seeking = "+seeking);
						mSeekingTimer = null;
					}
				}
				
			});
			
			mp.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e(TAG, "Player fail, what ("+what+") extra ("+extra+")");
					
					if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN){
						//stop();
						playFail();
						return true;
					}
					
					// not sure what error code -1 exactly stands for but it causes player to start to jump songs
					// if there are more than 2 jumps without playback during 1 second then we abort 
					// further playback
					if(what == -1){
						long failTime = System.currentTimeMillis();
						if(failTime - mLastFailTime > FAIL_TIME_FRAME){
							// outside time frame
							mTimesFailed = 1;
							mLastFailTime = failTime;
							Log.w(TAG, "PlayerEngineImpl "+mTimesFailed+" fail within FAIL_TIME_FRAME");
						} else {
							// inside time frame
							mTimesFailed++;
							if(mTimesFailed > ACCEPTABLE_FAIL_NUMBER){
								Log.w(TAG, "PlayerEngineImpl too many fails, aborting playback");
								stop();
								return true;
							}
						}
					}
					return false;
				}
			});

			// start preparing
			Log.i(TAG, "Player [buffering] "+ entry.getName());
			mIsPrepareOK = false;
			updateProgressWhileSeek(View.VISIBLE);
			mp.prepare();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			{
				mIsPrepareOK = true;
				if (progress_seekTo != 0) {
					mp.seekTo(progress_seekTo);
					Log.i(TAG, "need Seek, seek to " + progress_seekTo);
					progress_seekTo = 0;
				}
				if (isUserPause) {
					return mp;
				}
				
				if (!isUsingStreaming && downloadSaveFile == null) {
					updateMaxProgress(mp.getDuration());
				}

				// start playing
				if(mPlaylist.getSelectedTrack() == mEntry && playAfterPrepare){
					playAfterPrepare = false;
					requestAudioFocesAndPlay();
				}
			}
			return mp;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void needLoading() {
		updateProgressWhileSeek(View.VISIBLE);
		if(mp != null && mp.getCurrentPosition() < mp.getDuration()){
			if(DEBUG) Log.i(TAG, "download buffering...");
			mStreamDownloader.transferDataNow();
		}
	}
	
	
	
	private class MusicNotificationButtonReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MusicNotificationButtonReceiver", intent.getAction());
			if (intent.getAction().equals(MusicNotificationService.NOTIFICATION_MUSIC_CANCEL_ACTION)) {
				Message msg = musicHandler.obtainMessage(MsgConst.MSG_MUSIC_STOP);
				musicHandler.sendMessage(msg);
			}
			if (intent.getAction().equals(MusicNotificationService.NOTIFICATION_MUSIC_PLAYPAUSE_ACTION)) {
				Message msg = musicHandler.obtainMessage(MsgConst.MSG_MUSIC_CONTROL);
				if (isPlaying()){
					msg.obj = MusicPlayerView.MUSIC_PAUSE;
				}
				else {
					msg.obj = MusicPlayerView.MUSIC_PLAY;
				}
				musicHandler.sendMessage(msg);
			}
			if (intent.getAction().equals(MusicNotificationService.NOTIFICATION_MUSIC_NEXT_ACTION)) {
				Message msg = musicHandler.obtainMessage(MsgConst.MSG_MUSIC_CONTROL);
				msg.obj = MusicPlayerView.MUSIC_NEXT;
				musicHandler.sendMessage(msg);
			}
		}
	}
	
	
}
