package com.viash.voice_assistant.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.iflytek.tts.TtsService.Tts;
import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.adapter.HelpExpandableAdapter;
import com.viash.voice_assistant.component.CustomSwitchButton;
import com.viash.voice_assistant.component.RecommendView;
import com.viash.voice_assistant.component.VoiceWakeUpIndicationDialog;
import com.viash.voice_assistant.data.AppData;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.speech.Constant;
import com.viash.voicelib.utils.ClientPropertyUtil;
//import android.widget.ImageView.ScaleType;
//import com.viash.voice_assistant.component.FileDialog;

public class SettingsActivity extends Activity implements OnClickListener {
	// Layout
	protected View mLayoutMain;
	//protected View mLayoutHelp;
	
	// help
	//protected ExpandableListView mHelpView;
	
	// header
	/*private ImageView voice_bg1;
	private ImageView voice_bg2;
	private ImageView voice_bg3;*/

	List<VoiceType> mVoiceType = new ArrayList<VoiceType>();
	private ImageView voicepic;
	//private ImageView camera_icon;
	private static final int SELECT_PICTURE = 1;
	private static final int PICK_FROM_CAMERA = 2;
	private static final int CROP_FROM_CAMERA = 3;	
	private Uri picUri;
	private static final String TEMP_CAMERA_IMAGE = "camera_catched_image.jpg";

	// basic setting
	private View voicesetting;
	private View voicetype;
	//private View usercall;
	//private View prompt;
	private View autostartrecord;
	//private View lockhome;
	//private View voicewakeup;
	private View voiceLock; 
	//private View voiceTips;
	private View voiceSmsIncomingCall;
	private TextView voicetype_text;
	//private TextView lockhome_text;
	//private TextView setting_lockhome;
	//private TextView usercall_text;
	private TextView tv_sign;
	private TextView tv_nickname_content;
	private CustomSwitchButton voicesetting_button;
	//private CustomSwitchButton prompt_button;
	private EditText usercallEditText;
	private CustomSwitchButton autoStartRecord_button;
	//private CustomSwitchButton voicewakeup_button;
	//private CustomSwitchButton voiceLock_button;  
	private TextView voiveLock_tv;
 	
	// map setting
	//private View map_display;
	//private View map_traveltype;
	//private View map_traffic;
	//private TextView map_display_text;
	//private TextView map_traveltype_text;
	//private CustomSwitchButton map_traffic_button;
	String[] mTravelType;
	String[] mDisplayType;

	// music setting
	//private View musicdownloadpath;
	//private View searchonlinemusic;
	//private View listenandsave;
	//private TextView musicdownloadpath_text;
	//private CustomSwitchButton searchonlinemusic_button;
	//private CustomSwitchButton listenandsave_button;
	//private FileDialog dialog_MusicDownloadPath = null;

	// system setting
	private View versionupdate;
	private View feedBack;
	private View aboutus;
	//private View help;
	private View recommendView;
	private RecommendView mRecommendView;
	private View view_sign;
	private View setting_layout_nickname;
	//private Handler handler ;
	
	private TextView title_back;

	private View voice_setting_record ;   //语音设置（选项）
	private String[]voice_typeStrings; 
	private TextView vadio_setting_voicetype;
	private CustomSwitchButton netWorkTips_button;
	private View network_tips;
	private CustomSwitchButton floatViewOnDesk_button;
	private View floatView_onDesk;
	private CustomSwitchButton voice_wakeup_button;
	private View voice_wakeup_view;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		setContentView(R.layout.settings_layout);
		initVoiceType();
		initDisplayType();
		initTravelType();
		initView();
		initVoiceSettingType();
		initTextView();
		initListener();
		//initHandler();	
	}

	@Override
	public void onResume(){
		MobclickAgent.onResume(this);
		super.onResume();
		init();
		getLoginStatus();
		updateNickname();
		updateLock();
		if(voice_wakeup_button != null)
		  voice_wakeup_button.setChecked(!SavedData.isVoiceWakeUpOpen());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			init();
			if (requestCode == SELECT_PICTURE) {
				Bitmap resultBitmap = (Bitmap) data.getExtras().get("data");
				resultBitmap = makeMaskImage(resultBitmap);
				GlobalData.setUserHeader(resultBitmap);
				voicepic.setImageBitmap(GlobalData.getUserHeader());
				//voicepic.setScaleType(ScaleType.CENTER);
				//voicepic.setBackgroundResource(R.drawable.setting_head_bg);
			}			
			else if( requestCode == PICK_FROM_CAMERA ) {		        
		    	Intent cropIntent = new Intent("com.android.camera.action.CROP"); 
		    	cropIntent.setDataAndType(picUri, "image/*");
		    	cropIntent.putExtra("crop", "true");
		    	cropIntent.putExtra("aspectX", 1);
		    	cropIntent.putExtra("aspectY", 1);
		    	cropIntent.putExtra("outputX", 256);
		    	cropIntent.putExtra("outputY", 256);
		    	cropIntent.putExtra("scale", true);
		    	cropIntent.putExtra("return-data", true);
		        startActivityForResult(cropIntent, CROP_FROM_CAMERA);
			}
			else if( requestCode == CROP_FROM_CAMERA ) {
				Bitmap resultBitmap = (Bitmap) data.getExtras().get("data");
				resultBitmap = makeMaskImage(resultBitmap);
				GlobalData.setUserHeader(resultBitmap);
				voicepic.setImageBitmap(GlobalData.getUserHeader());
				//voicepic.setScaleType(ScaleType.CENTER);
				//voicepic.setBackgroundResource(R.drawable.setting_head_bg);
				File file = new File(Environment.getExternalStorageDirectory(),TEMP_CAMERA_IMAGE);
				file.delete();
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void initListener() {
		// Header
		/*
		voice_bg1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SavedData.setBackground(1);
				setSelectedIcon();
			}
		});
		voice_bg2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SavedData.setBackground(2);
				setSelectedIcon();
			}
		});
		voice_bg3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SavedData.setBackground(3);
				setSelectedIcon();
			}
		});
		*/
		/*
		voicepic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// open Gallery
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				intent.putExtra("crop", "true");
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("outputX", 250);
				intent.putExtra("outputY", 250);
				// intent.putExtra("output", Uri.fromFile(SavedData.getVoicePicFile()));
				// intent.putExtra("outputFormat", "JPEG");
				intent.putExtra("return-data", true);
				startActivityForResult(
					Intent.createChooser(
						intent,
						getResources().getText(R.string.setting_selectpic)
					),
					SELECT_PICTURE
				);
			}
		});
		
		camera_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	try {
		        	Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		        	File tmpFile = new File(Environment.getExternalStorageDirectory(),TEMP_CAMERA_IMAGE);
		        	picUri = Uri.fromFile(tmpFile);
		        	captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);		        	
		            startActivityForResult(captureIntent, PICK_FROM_CAMERA);
	        	}catch(ActivityNotFoundException e){
	        		e.getStackTrace();
	        	}
			}
		});
		*/
		// basic setting
		voicesetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				voicesetting_button.performClick();
			}
		});
		voicesetting_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SavedData.setVoiceSetting(!isChecked);
				if(isChecked) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				}
			}
		});
		autostartrecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				autoStartRecord_button.performClick();
			}
		});
		autoStartRecord_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SavedData.setmAutoStartRecord(!isChecked);
			}
		});
		
		network_tips.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				netWorkTips_button.performClick();
			}	
		});
		
		netWorkTips_button.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {				
				  SavedData.setNetworkTips(!isChecked);
			}	
		});
		
		floatView_onDesk.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				floatViewOnDesk_button.performClick();
			}	
		});
		
		floatViewOnDesk_button.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {				
				  SavedData.setFloatViewOnDesk(!isChecked);
			}	
		});
		
		voice_wakeup_view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				voice_wakeup_button.performClick();
			}	
		});
		
		voice_wakeup_button.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(isChecked)
				{
					SavedData.setVoiceWakeUp(false);
					sendBroadcast(new Intent(VoiceWakeUpIndicationDialog.NOTIFICATION_CLOSE_VOICE_WAKE_UP));
				}
				else
				{
					VoiceWakeUpIndicationDialog dialog = new VoiceWakeUpIndicationDialog(SettingsActivity.this,null,null);
					dialog.show();
					SavedData.setVoiceWakeUp(true);
					Intent intent = new Intent();
					intent.setAction(VoiceWakeUpIndicationDialog.NOTIFICATION_START_CAPTURE_OFFLINE);
					SettingsActivity.this.sendBroadcast(intent);
				}
			}	
		});
		
		voiceLock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				// voiceLock_button.performClick();
				Intent intent = new Intent(SettingsActivity.this,LockScreenSettingActivity.class);
			    startActivity(intent);
			}
		});
		
		/*voiceTips.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v){
				Intent intent = new Intent(SettingsActivity.this,TipsActivity.class);
				startActivity(intent);
			}
		});*/
		
		voiceSmsIncomingCall.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent = new Intent(SettingsActivity.this,SmsIncomingCallSettingActivity.class);
				startActivity(intent);
			}
		});
//		voiceLock_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			@SuppressLint("ResourceAsColor")
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				SavedData.setAllowLock(!isChecked);
//				if (!isChecked) {
//					startService(new Intent(SettingsActivity.this,LockScreenService.class)); 	
//					lockhome_text.setTextColor(Color.rgb(200, 200, 200));
//					setting_lockhome.setTextColor(Color.rgb(200, 200, 200));
//				}
//				else {
//					stopService(new Intent(SettingsActivity.this,LockScreenService.class));
//					lockhome_text.setTextColor(Color.GRAY);
//					setting_lockhome.setTextColor(Color.GRAY);
//				}
//			}
//		});
//		voicewakeup.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
//				voicewakeup_button.performClick();
//			}
//		});
//		voicewakeup_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				SavedData.setAllowWakeupByAudio(!isChecked);
//			}
//		});
		
		ColorDrawable drawable = new ColorDrawable(0);
		drawable.setBounds(0, 0, 1, 1);
	final Dialog dialog = new AlertDialog.Builder(this)
		.setIcon(drawable)
		.setTitle(R.string.setting_voicetype_vadio)
		.setSingleChoiceItems(voice_typeStrings, SavedData.getVoiceTypeNum(), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//editor = getSharedPreferences("voice_type", 0).edit();

				if(which == 0){
					SavedData.setVoiceTypes(voice_typeStrings[0]);
					Constant.set_voice_type(true);
				}else{
					SavedData.setVoiceTypes(voice_typeStrings[1]);
					Constant.set_voice_type(false);
				}
				
				SavedData.setVoiceTypeNum(which);
				vadio_setting_voicetype.setText(SavedData.getVoiceTypes());
				dialog.dismiss();
			}
		}).create();
		
		//语音设置
		voice_setting_record.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.show();
			}
		});

		final Dialog dialog_VoiceType = new AlertDialog.Builder(this)
				.setIcon(drawable)
				.setTitle(R.string.setting_voicetype)
				.setSingleChoiceItems(getAllVoiceType(),
						getVoiceTypeIndex(SavedData.getVoiceType()),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SavedData.setVoiceType(mVoiceType.get(which).voiceId);
								String showText = getVoiceTypeName(SavedData.getVoiceType());
								showText = showText.substring(0, showText.indexOf(" "));
								voicetype_text.setText(showText);
								dialog.dismiss();
								allRefresh();
							}
						}).create();
		dialog_VoiceType.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				allRefresh();
			}
		});
		voicetype.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				dialog_VoiceType.show();
			}
		});
//		lockhome.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(SavedData.isAllowLock()){
//					Intent intent = new Intent(SettingsActivity.this,LockHomeAct.class);
//					startActivity(intent);
//					}
//			}
//		});
		
		usercallEditText = new EditText(this);
		usercallEditText.setText(SavedData.getUserCall());
/*		final Dialog dialog_UserCall = new AlertDialog.Builder(this)
				.setIcon(drawable)
				.setTitle(R.string.setting_usercall)
				.setView(usercallEditText)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SavedData.setUserCall(usercallEditText.getText().toString());
								usercall_text.setText(SavedData.getUserCall());
								dialog.dismiss();
							}
						}).create();*/
		/*
		usercall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				dialog_UserCall.show();
			}
		});

		prompt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				prompt_button.performClick();
			}
		});
		prompt_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SavedData.setPrompt(!isChecked);
			}
		});
		*/
		
		// map setting
		/*
		final Dialog dialog_Map_Display = new AlertDialog.Builder(this)
				.setTitle(R.string.setting_map_display)
				.setSingleChoiceItems(mDisplayType, SavedData.getMapDisplay(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SavedData.setMapDisplay(which);
								map_display_text.setText(mDisplayType[SavedData.getMapDisplay()]);
								dialog.dismiss();
							}
						}).create();
		map_display.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				dialog_Map_Display.show();
			}
		});
		
		final Dialog dialog_Map_TravelType = new AlertDialog.Builder(this)
				.setTitle(R.string.setting_map_traveltype)
				.setSingleChoiceItems(mTravelType, SavedData.getMapTravel(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SavedData.setMapTravel(which);
								map_traveltype_text.setText(mTravelType[SavedData.getMapTravel()]);
								dialog.dismiss();
							}
						}).create();
		map_traveltype.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				dialog_Map_TravelType.show();
			}
		});
		
		map_traffic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				map_traffic_button.performClick();
			}
		});
		map_traffic_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SavedData.setMapTraffic(!isChecked);
			}
		});

		// Music System
		searchonlinemusic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				searchonlinemusic_button.performClick();
			}
		});
		searchonlinemusic_button
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						SavedData.setSearchOnlineMusic(!isChecked);
					}
				});

		listenandsave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				listenandsave_button.performClick();
			}
		});
		listenandsave_button
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						SavedData.setListenAndSave(!isChecked);
					}
				});

		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File sdcardPath = new File(Environment.getExternalStorageDirectory() + "/");
			dialog_MusicDownloadPath = new FileDialog(this, sdcardPath);
			dialog_MusicDownloadPath.setSelectDirectoryOption(true);
			dialog_MusicDownloadPath.addDirectoryListener(new FileDialog.DirectorySelectedListener(){
				@Override
				public void directorySelected(File directory) {
					String path = directory.toString().replaceAll(Environment.getExternalStorageDirectory().toString() + "/", "");
					SavedData.setMusicDownloadPath(path);
					//musicdownloadpath_text.setText(SavedData.getMusicDownloadPath());
				}
			});
		}else{
			dialog_MusicDownloadPath = null;
		}
		//final EditText editText = new EditText(this);
		//final Dialog dialog_MusicDownloadPath = new AlertDialog.Builder(this)
		//	.setTitle(R.string.setting_musicdownloadpath)
		//	.setView(editText)
		//	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
		//		@Override public void onClick(DialogInterface dialog, int which) {
		//			SavedData.setMusicDownloadPath(editText.getText().toString());
		//			musicdownloadpath_text.setText(SavedData.getMusicDownloadPath());
		//			dialog.dismiss();
		//		}
		//	})
		//	.create();
		//musicdownloadpath.setOnClickListener(new OnClickListener(){
		//	@Override public void onClick(View v) {
		//		new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
		//		//dialog_MusicDownloadPath.show();
		//		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && dialog_MusicDownloadPath != null){
		//			dialog_MusicDownloadPath.showDialog();
		//		}else{
		//			Toast.makeText(SettingsActivity.this, "SD Card not found.", Toast.LENGTH_LONG).show();
		//		}
		//	}
		//});
		*/
		
		// system
		
		versionupdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				//UpdateNotification.init(SettingsActivity.this);
				Intent intent = new Intent(SettingsActivity.this, VersionInfoActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);
			}
		});

		feedBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {			
				Intent intent = new Intent(SettingsActivity.this, FeedBackActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);
			}
		});
		
		final TextView aboutusText = new TextView(this);
		final String olaURL = "http://www.olavoice.com/";
		String text = "官方网站: <a href='" + olaURL + "'>www.olavoice.com</a><br />";
		text += "版本号: " + ClientPropertyUtil.getVersionName(this);
		aboutusText.setText(Html.fromHtml(text));
		aboutusText.setMovementMethod(LinkMovementMethod.getInstance());	
		aboutusText.setTextSize(22);
		aboutusText.setPadding(15,5,15,5);
		
/*		final Dialog dialog_Aboutus = new AlertDialog.Builder(this)
		.setIcon(drawable)
		.setTitle(R.string.setting_aboutus)
		.setView(aboutusText)		
		.setPositiveButton("Back",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
			}
		}).create();*/

		aboutus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();
				
				Intent intent = new Intent(SettingsActivity.this, ContactUsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);
				//dialog_Aboutus.show();
			}
		});
		
		recommendView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, RecommendActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);   
			}
		});
		/*help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//new Thread(new ClickAnimation(SettingsActivity.this, v)).start();

				title_back.setText("帮助");
				showTopView(mLayoutHelp);
			}
		});*/
		
		view_sign.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ColorDrawable drawable = new ColorDrawable(0);
				drawable.setBounds(0, 0, 1, 1);
				if(GlobalData.isUserLoggedin()){
					/*new AlertDialog.Builder(SettingsActivity.this)
					.setIcon(drawable)
					.setTitle(getResources().getString(R.string.alert))
					.setMessage(getResources().getString(R.string.exit_message))
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UserData.exit(SettingsActivity.this);
							handler.sendEmptyMessage(REFRESH_LOGIN_STATUS);
						}
					})
					.setNegativeButton(getResources().getString(R.string.cancel),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).create().show();*/
					Intent intent = new Intent(SettingsActivity.this,AccountCenterActivity.class);
					startActivity(intent);
				}else{
					Intent intent = new Intent(SettingsActivity.this,LoginActivity.class);
					startActivity(intent);
				}
				
			}
		});
		
		setting_layout_nickname.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this,NicknameActivity.class);
				startActivity(intent);
			}
		});

		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*if (hideTopView()) {
					allRefresh();
					return;
				}*/
				SettingsActivity.this.finish();				
			}
			
		});
	
	}

	private void init() {
		GlobalData.init(this);
		SavedData.init(this);
	}

	private void initView() {
		// Layout
		mLayoutMain = this.findViewById(R.id.layout_main);
    	//mLayoutHelp = this.findViewById(R.id.layout_help);
    	//mHelpView = (ExpandableListView) mLayoutHelp.findViewById(R.id.help);
    	
    	// Help View
        //mHelpView.setAdapter(new HelpExpandableAdapter(this));
        //mHelpView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
        
		// header
		//voice_bg1 = (ImageView) this.findViewById(R.id.backgroundView1);
		//voice_bg2 = (ImageView) this.findViewById(R.id.backgroundView2);
		//voice_bg3 = (ImageView) this.findViewById(R.id.backgroundView3);
		//setSelectedIcon();

		//voicepic = (ImageView) this.findViewById(R.id.setting_voice_pic);
		//voicepic.setImageBitmap(GlobalData.getUserHeader());
		//voicepic.setScaleType(ScaleType.CENTER);
		//voicepic.setBackgroundResource(R.drawable.setting_head_bg);
		//camera_icon = (ImageView) this.findViewById(R.id.camera_icon);

        vadio_setting_voicetype = (TextView) findViewById(R.id.vadio_setting_voicetype);
        vadio_setting_voicetype.setText(SavedData.getVoiceTypes());
        voice_setting_record = (View)findViewById(R.id.voice_setting_record);
		// basic setting
		voicesetting = (View) this.findViewById(R.id.setting_layout_voicesetting);
		voicesetting_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_voicesetting);
		voicesetting_button.setChecked(!SavedData.getVoiceSetting());
		voicetype = (View) this.findViewById(R.id.setting_layout_voicetype);
		voicetype = (View) this.findViewById(R.id.setting_layout_voicetype);
		//usercall = (View) this.findViewById(R.id.setting_layout_usercall);
		//prompt = (View) this.findViewById(R.id.setting_layout_prompt);
		//prompt_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_prompt);
		//prompt_button.setChecked(!SavedData.getPrompt());
		autostartrecord = (View) this.findViewById(R.id.setting_layout_auto_start_record);
		autoStartRecord_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_auto_start_record);
		autoStartRecord_button.setChecked(!SavedData.getmAutoStartRecord());
		
		netWorkTips_button = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_network_tips);
		netWorkTips_button.setChecked(!SavedData.isNetworkTips());
		network_tips = (View) findViewById(R.id.setting_layout_network_tips);
		
		floatViewOnDesk_button = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_floatview_ondesk);
		floatViewOnDesk_button.setChecked(!SavedData.isFloatViewOnDesk());
		floatView_onDesk = (View) findViewById(R.id.setting_layout_floatview_ondesk);
		
		voice_wakeup_button = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_voice_wakeup);
		voice_wakeup_button.setChecked(!SavedData.isVoiceWakeUpOpen());
		voice_wakeup_view = (View) findViewById(R.id.setting_layout_voice_wakeup);
		
//		voicewakeup = (View) this.findViewById(R.id.setting_layout_voice_wakeup);
//		voicewakeup_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_voice_wakeup);
//		voicewakeup_button.setChecked(!SavedData.isAllowWakeupByAudio());
//		
		voiceLock = (View) this.findViewById(R.id.setting_layout_lock); 
		voiveLock_tv=(TextView)this.findViewById(R.id.setting_lock);  
//		voiceLock_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_voice_wakeup_lock);   
//		voiceLock_button.setChecked(!SavedData.isAllowLock());
//		lockhome = (View) this.findViewById(R.id.setting_layout_lockhome);
//		lockhome_text=(TextView)this.findViewById(R.id.setting_textview_lockhome);
//		setting_lockhome=(TextView)this.findViewById(R.id.setting_lockhome);
		if(!SavedData.isAllowLock()){
			voiveLock_tv.setText(R.string.setting_basicsetting_unstart);
		}else{
			voiveLock_tv.setText(R.string.setting_basicsetting_start);
		}
		int sdk_version = android.os.Build.VERSION.SDK_INT;	
		if(sdk_version >= 21)
		{
			voiceLock.setVisibility(View.GONE);
		}
		//voiceTips = (View) this.findViewById(R.id.setting_layout_tips);
		voiceSmsIncomingCall = (View) this.findViewById(R.id.setting_layout_sms_incoming_call);
		// map setting
		//map_display = (View) this.findViewById(R.id.setting_layout_map_display);
		//map_traveltype = (View) this.findViewById(R.id.setting_layout_map_traveltype);
		//map_traffic = (View) this.findViewById(R.id.setting_layout_map_traffic);
		//map_traffic_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_map_traffic);
		//map_traffic_button.setChecked(!SavedData.getMapTraffic());

		// music setting
		//searchonlinemusic = (View) this.findViewById(R.id.setting_layout_searchonlinemusic);
		//searchonlinemusic_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_searchonlinemusic);
		//searchonlinemusic_button.setChecked(!SavedData.getSearchOnlineMusic());
		//listenandsave = (View) this.findViewById(R.id.setting_layout_listenandsave);
		//listenandsave_button = (CustomSwitchButton) this.findViewById(R.id.setting_swtichbutton_listenandsave);
		//listenandsave_button.setChecked(!SavedData.getListenAndSave());
		//musicdownloadpath = (View) this.findViewById(R.id.setting_layout_musicdownloadpath);

		// system
		versionupdate = (View) this.findViewById(R.id.setting_layout_versionupdate);
		aboutus = (View) this.findViewById(R.id.setting_layout_aboutus);
		recommendView = (View) this.findViewById(R.id.setting_layout_recommend);
		//help = (View) this.findViewById(R.id.setting_layout_help);
		view_sign = (View) this.findViewById(R.id.setting_layout_sign);
		tv_sign = (TextView) findViewById(R.id.setting_textview_sign);
		setting_layout_nickname = (View) findViewById(R.id.setting_layout_nickname);
		tv_nickname_content = (TextView) findViewById(R.id.tv_nickname_content);
		title_back = (TextView) this.findViewById(R.id.setting_title);
		feedBack = (View) this.findViewById(R.id.setting_layout_feedback);
	}
	
/*	private void setSelectedIcon() {
		voice_bg1.setImageBitmap(null);
		voice_bg2.setImageBitmap(null);
		voice_bg3.setImageBitmap(null);

		switch (SavedData.getBackground()) {
		case 2:
			voice_bg2.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.setting_selected));
			break;
		case 3:
			voice_bg3.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.setting_selected));
			break;
		default:
			voice_bg1.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.setting_selected));
			break;
		}

		voice_bg1.setScaleType(ScaleType.CENTER);
		voice_bg1.setBackgroundResource(R.drawable.icon_theme_1);
		voice_bg2.setScaleType(ScaleType.CENTER);
		voice_bg2.setBackgroundResource(R.drawable.icon_theme_2);
		voice_bg3.setScaleType(ScaleType.CENTER);
		voice_bg3.setBackgroundResource(R.drawable.icon_theme_3);
	}*/

	private Bitmap makeMaskImage(Bitmap original){
		Bitmap mask = BitmapFactory.decodeResource(getResources(), R.drawable.setting_head_mask);
		Bitmap maskResult = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
		Canvas mCanvas = new Canvas(maskResult);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		mCanvas.drawBitmap(original, 0, 0, null);
		mCanvas.drawBitmap(mask, 0, 0, paint);
		paint.setXfermode(null);
		
		Bitmap result = BitmapFactory.decodeResource(getResources(), R.drawable.setting_head_bg).copy(Bitmap.Config.ARGB_8888, true);
		mCanvas = new Canvas(result);
		mCanvas.drawBitmap(maskResult, 0, 0, null);
		return result;
	}

	private void initTextView() {
		// basic setting
		voicetype_text = (TextView) this.findViewById(R.id.setting_voicetype);
		//usercall_text = (TextView) this.findViewById(R.id.setting_usercall);

		// map setting
		//map_display_text = (TextView) this.findViewById(R.id.setting_map_display);
		//map_traveltype_text = (TextView) this.findViewById(R.id.setting_map_traveltype);

		// music setting
		//musicdownloadpath_text = (TextView) this.findViewById(R.id.setting_musicdownloadpath);

		// set default value
		String showText = getVoiceTypeName(SavedData.getVoiceType());
		showText = showText.substring(0, showText.indexOf(" "));
		voicetype_text.setText(showText);
		//usercall_text.setText(SavedData.getUserCall());
		//map_display_text.setText(mDisplayType[SavedData.getMapDisplay()]);
		//map_traveltype_text.setText(mTravelType[SavedData.getMapTravel()]);
		//musicdownloadpath_text.setText(SavedData.getMusicDownloadPath());
	}

	private void initVoiceSettingType(){
		voice_typeStrings = getResources().getStringArray(R.array.voiceTypeSetting);
	}
	class VoiceType {
		public String voiceName;
		public int voiceId;
	}

	private void initDisplayType(){
		mDisplayType = getResources().getStringArray(R.array.displaytypename);
	}
	
	private void initTravelType() {
		mTravelType = getResources().getStringArray(R.array.traveltypename);
	}

	private void initVoiceType() {
		String[] voiceName = getResources().getStringArray(R.array.voicetypename);

		VoiceType voiceType = new VoiceType();
		voiceType.voiceName = voiceName[0];
		voiceType.voiceId = 3;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[1];
		voiceType.voiceId = 11;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[2];
		voiceType.voiceId = 14;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[3];
		voiceType.voiceId = 15;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[4];
		voiceType.voiceId = 22;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[5];
		voiceType.voiceId = 24;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[6];
		voiceType.voiceId = 25;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[7];
		voiceType.voiceId = 51;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[8];
		voiceType.voiceId = 52;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[9];
		voiceType.voiceId = 53;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[10];
		voiceType.voiceId = 54;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[11];
		voiceType.voiceId = 55;
		mVoiceType.add(voiceType);

		voiceType = new VoiceType();
		voiceType.voiceName = voiceName[12];
		voiceType.voiceId = 56;
		mVoiceType.add(voiceType);
	}

	private int getVoiceTypeIndex(int voiceId) {
		for (int i = 0; i < mVoiceType.size(); i++) {
			if (mVoiceType.get(i).voiceId == voiceId) {
				return i;
			}
		}
		return 0;
	}

	private String getVoiceTypeName(int voiceId) {
		for (int i = 0; i < mVoiceType.size(); i++) {
			if (mVoiceType.get(i).voiceId == voiceId) {
				return mVoiceType.get(i).voiceName;
			}
		}
		return "";
	}

	private String[] getAllVoiceType() {
		String[] type = new String[mVoiceType.size()];
		for (int i = 0; i < type.length; i++) {
			type[i] = mVoiceType.get(i).voiceName;
		}
		return type;
	}
	
	private void getLoginStatus(){
		if(GlobalData.isUserLoggedin()) {//UserData.isLogin(SettingsActivity.this)){
			tv_sign.setText(getResources().getString(R.string.account_center));
		}else{
			tv_sign.setText(getResources().getString(R.string.login));
		}

	}
	
	private void updateNickname(){
		tv_nickname_content.setText(AppData.getNickname(this));
	}
	private void updateLock(){
		if(!SavedData.isAllowLock()){
			voiveLock_tv.setText(R.string.setting_basicsetting_unstart);
		}else{
			voiveLock_tv.setText(R.string.setting_basicsetting_start);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {


		default:
			break;
		}
		
	}
	
/*	private void initHandler(){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFRESH_LOGIN_STATUS:
					if(UserData.isLogin(SettingsActivity.this)){
						tv_sign.setText(getResources().getString(R.string.login_exit));
					}else{
						tv_sign.setText(getResources().getString(R.string.login));
					}
					break;

				default:
					break;
				}
			}
		};
	}*/
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if((keyCode == KeyEvent.KEYCODE_BACK)){
			/*if(hideTopView()) {
				allRefresh();
				return true;
			}*/
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void showTopView(View topView){
		/*if(mLayoutHelp == topView){
			mLayoutMain.setVisibility(View.GONE);
		} else {
			mLayoutHelp.setVisibility(View.GONE);
		}*/
		topView.setVisibility(View.VISIBLE);
	}
	/*private boolean hideTopView(){
		boolean ret = false;
		title_back.setText("设置");
		if(mLayoutHelp.getVisibility() == View.VISIBLE){
			mLayoutHelp.setVisibility(View.GONE);
			mLayoutMain.setVisibility(View.VISIBLE);
			ret = true;
		}
		return ret;
	}*/
	private void allRefresh(){
		this.runOnUiThread(new Runnable() {
			public void run(){
				//int bg = 0x00000000;
				//voicetype.setBackgroundColor(bg);
				//versionupdate.setBackgroundColor(bg);
				//aboutus.setBackgroundColor(bg);
				//help.setBackgroundColor(bg);
			}
		});
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);	
		super.onPause();
	}  
	
}
