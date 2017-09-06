package com.viash.voice_assistant.widget;

import java.util.List;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.msg.MsgConst;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NetworkWarningDialog extends AlertDialog {

	public static final int NONE_KIND_TO_REMIND = 0;
	public static final int MUSIC_PLAY_TO_REMIND = 1;
	public static final int VIDEO_PLAY_TO_REMIND = 2;
	public static final int DOWNLOAD_TO_REMIND = 3;
	private Context mContext;
	private AlertDialog.Builder builder;
	private Handler mHandler;
	private CommunicationData mData;
	private static boolean isAppMusic = false;
	private List<String[]> mMusicData = null;
	private static int whichKindRemind = NONE_KIND_TO_REMIND;

	public NetworkWarningDialog(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
		builder = new Builder(mContext);
		builder.setMessage(mContext.getResources().getString(
				R.string.network_warning));
		builder.setTitle(mContext.getResources().getString(R.string.warning));

		builder.setNegativeButton(
				mContext.getResources().getString(R.string.cancel),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						/*Message msg = mHandler
								.obtainMessage(MsgConst.CLIENT_ACTION_REMOVE_DATA);
						msg.obj = mData;
						msg.arg1 = 0;
						mHandler.sendMessage(msg);*/
						String str = "算了";
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,str);
						mHandler.sendMessage(msg);						
						dialog.dismiss();
					}
				});

		builder.setPositiveButton(mContext.getResources()
				.getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message msg = mHandler
						.obtainMessage(MsgConst.CLIENT_ACTION_ADD_DATA);
				msg.obj = mData;
				msg.arg1 = whichKindRemind;
				mHandler.sendMessage(msg);
				if(isAppMusic)
				{
					Message msg1 = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_SHOW_MUSIC_LIST);
					msg1.obj = mMusicData;
					msg1.arg1 = whichKindRemind;
					mHandler.sendMessage(msg1);
				}	
				dialog.dismiss();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// this.setContentView(R.layout.activity_network_tips);
		// this.setTitle(null);
	}

	public void showDialog() {
		builder.show();
	}

	public void setHandlerAndData(Handler handler, CommunicationData data, boolean appMusicFlag,int whichTypeRemind) {
		mHandler = handler;
		mData = data;
		isAppMusic = appMusicFlag;
		whichKindRemind = whichTypeRemind;
	}
	
	public void setMusicData(List<String[]> data)
	{
		mMusicData = data;
	}
}
