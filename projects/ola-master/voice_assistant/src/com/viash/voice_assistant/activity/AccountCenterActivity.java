package com.viash.voice_assistant.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.common.Log;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.util.UserManagerHttpUtil;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;


public class AccountCenterActivity extends Activity implements OnClickListener {
	private static final String TAG = "AccountCenterAct";
	
	private Button btn_login_exit;
	private RelativeLayout layout_account_phone;
	private TextView tv_identifying_status;
	private ImageView imageview_authenticate_user;
	private final String registerKey = "accountcenter";
	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected VoiceAssistantServiceConnection mVoiceAssistantServiceConnection = null;
	private ProgressDialog dialog;
	private TextView title_back;
	
	private String username = "";
	private String pwd = "";
	private String phone = "";
	private MyHandler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		mHandler = new MyHandler();
		
		setContentView(R.layout.activity_accountcenter);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(AccountCenterActivity.this);
		dialog.setTitle(getResources().getString(R.string.login_loading_title));
		dialog.setMessage(getResources().getString(R.string.login_loading_message));
		tv_identifying_status.setText(R.string.account_obtain_identify_status);
	}

	private void getView() {
		btn_login_exit = (Button) findViewById(R.id.btn_login_exit);
		layout_account_phone = (RelativeLayout) findViewById(R.id.layout_account_phone);
		tv_identifying_status = (TextView) findViewById(R.id.tv_identifying_status);
		imageview_authenticate_user = (ImageView) findViewById(R.id.imageview_authenticate_user);
		title_back = (TextView) this.findViewById(R.id.setting_title);
		updateLayoutbyBindStatus();
	}
	
	private void updateLayoutbyBindStatus() {
		String[] userInfo = UserData.getUserInfo(this);
		if (userInfo.length >= 2) {
			username = userInfo[0];
			pwd = userInfo[1];
		}
		exeGetBindedPhone();
	}
	
	private void exeGetBindedPhone() {
		Runnable getPhoneThread = new Runnable() {
			@Override
			public void run() {
				phone = UserManagerHttpUtil.getBindedPhone(AccountCenterActivity.this, username, pwd);
				if (phone != null && phone.length() > 0) {
					Log.d(TAG, "~~~~~~~~~~~~mobilephone is:" + phone);
					UserData.setPhone(AccountCenterActivity.this, phone);
				}
				if (UserData.isPhoneBinded(AccountCenterActivity.this)) {
					Log.d(TAG, "isPhoneBinded true");
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_PHONE_BINDED);
				} else {
					Log.d(TAG, "isPhoneBinded false");
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_PHONE_NOT_BIND);
				}
			}
		};
		new Thread(getPhoneThread).start();
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MsgConst.MSG_SENDCMD_PHONE_BINDED:
				long currentDate = System.currentTimeMillis();
				long expireDate = UserData.getVCodeExpireDate(AccountCenterActivity.this);
				if (currentDate < expireDate){
					tv_identifying_status.setText(R.string.account_wait_user_to_identifying);
					imageview_authenticate_user.setVisibility(View.GONE);
				}else{
					tv_identifying_status.setText(R.string.account_identified);
					imageview_authenticate_user.setVisibility(View.VISIBLE);
				}
				break;
			case MsgConst.MSG_SENDCMD_PHONE_NOT_BIND:
				long currentDate2 = System.currentTimeMillis();
				long expireDate2 = UserData.getVCodeExpireDate(AccountCenterActivity.this);
				if (currentDate2 < expireDate2){
					tv_identifying_status.setText(R.string.account_wait_user_to_identifying);
					imageview_authenticate_user.setVisibility(View.GONE);
				}else{
					tv_identifying_status.setText(R.string.account_not_identify);
					imageview_authenticate_user.setVisibility(View.GONE);
				}
				break;
			}
		}
	}
	
	private void setOnItemClick() {
		btn_login_exit.setOnClickListener(this);
		layout_account_phone.setOnClickListener(this);
		title_back.setOnClickListener(this);
	}

	protected void initVoiceAssistantService() {
		mVoiceAssistantServiceConnection = new VoiceAssistantServiceConnection();
		VoiceSdkServiceInterface.bindToVoiceService(this, mVoiceAssistantServiceConnection);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_login_exit:
			ColorDrawable drawable = new ColorDrawable(0);
			drawable.setBounds(0, 0, 1, 1);
			new AlertDialog.Builder(AccountCenterActivity.this)
			.setIcon(drawable)
			.setTitle(getResources().getString(R.string.alert))
			.setMessage(getResources().getString(R.string.exit_message))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					logout();
					UserData.exit(AccountCenterActivity.this);
					UserData.setPhone(AccountCenterActivity.this, null);
					UserData.setVCodeExpireDate(AccountCenterActivity.this, 0);
					UserData.setVCodeMobile(AccountCenterActivity.this, null);
					AccountCenterActivity.this.finish();
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).create().show();
			break;
		case R.id.layout_account_phone:
			if (UserData.isPhoneBinded(this)) {
				Intent intent = new Intent(AccountCenterActivity.this, PhoneChangeActivity.class);
				startActivity(intent);
			}
			else {
				Intent intent = new Intent(AccountCenterActivity.this, PhoneAuthenticationActivity.class);
				startActivity(intent);
			}
			break;
		case R.id.setting_title:
			this.finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		registerClient();
		super.onResume();

		updateLayoutbyBindStatus();
	}

	class VoiceAssistantServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (mVoiceAssistantServiceMessenger == null){
				mVoiceAssistantServiceMessenger = new Messenger(service);
				//registerClient();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mVoiceAssistantServiceMessenger = null;
		}
	}

	private void registerClient() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (mVoiceAssistantServiceMessenger == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Bundle bundle = new Bundle();
				bundle.putString("key", registerKey);
				Message msg = Message
						.obtain(null,
								MsgConst.CLIENT_ACTION_REGISTER_CLIENT_MESSENGER);
				msg.replyTo = mMessenger;
				msg.setData(bundle);
				try {
					if(mVoiceAssistantServiceMessenger != null)
						mVoiceAssistantServiceMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
					stopVoiceAssistantService();
				}
			}
		}).start();
	}

	private Messenger mMessenger = new Messenger(new InComingHandler());

	@SuppressLint("HandlerLeak")
	class InComingHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
		 dialog.dismiss();
		 switch (msg.what) {
		 case MsgConst.SERVICE_ACTION_SERVER_RESPONSE:
			
			 /*JSONObject jsonLoginResult = (JSONObject) msg.obj;
				jsonLoginResult=JsonUtil.optJsonObj(jsonLoginResult, "data");
				String type = JsonUtil.optString(jsonLoginResult, "type", null);
				if("login".equalsIgnoreCase(type)){
					int status_code = JsonUtil.optInt(jsonLoginResult, "status_code", -1);
					//String resultMsg = JsonUtil.optString(jsonLoginResult, "status_msg", null);
					if(status_code == 0){
						UserData.saveUserInfo(ForgetPwdAct.this, et_username.getText().toString(), et_password.getText().toString());
						
						Message msgSend = Message.obtain(null, MsgConst.CLIENT_ACTION_USER_LOGINED);
						try {
							mVoiceAssistantServiceMessenger.send(msgSend);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
						ForgetPwdAct.this.finish();
						CustomToast.showShortText(ForgetPwdAct.this,getResources().getString(R.string.login_success));
					}else{
						CustomToast.showShortText(ForgetPwdAct.this,getResources().getString(R.string.login_fail));
					}
				}	*/
             break;
         default:
             break;
         }
	  }
	}

	protected void stopVoiceAssistantService() {
		if (mVoiceAssistantServiceConnection != null) {
			// remove unregister
			/*Bundle bundle = new Bundle();
			bundle.putString("key", registerKey);
			Message msg = Message
					.obtain(null,
							MsgConst.CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER);
			msg.setData(bundle);
			try {
				if(mVoiceAssistantServiceMessenger != null)
					mVoiceAssistantServiceMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
*/
			this.unbindService(mVoiceAssistantServiceConnection);
			mVoiceAssistantServiceMessenger = null;
			mVoiceAssistantServiceConnection = null;
		}
	}

	@Override
	protected void onDestroy() {
		stopVoiceAssistantService();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		unregisterClient();
		
		super.onPause();
	}
	
	
	
	private void unregisterClient() {
		// remove unregister
		Bundle bundle = new Bundle();
		bundle.putString("key", registerKey);
		Message msg = Message
				.obtain(null,
						MsgConst.CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER);
		msg.replyTo = mMessenger;
		msg.setData(bundle);
		try {
			if(mVoiceAssistantServiceMessenger != null)
				mVoiceAssistantServiceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
			stopVoiceAssistantService();
		}
	}
	
	private void logout() {
		JSONObject jsonObj = new JSONObject();
		try {
			JSONObject jsonSpeech = new JSONObject();
			jsonSpeech.put("type", "logout");
			jsonObj.put("data_type", "command");
			jsonObj.put("data", jsonSpeech);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		MsgAsk msgAsk = new MsgAsk(jsonObj);
		byte[] data = msgAsk.prepareRawData();
		Bundle bundle = new Bundle();
		bundle.putByteArray("data", data);

		Message msg = new Message();
		msg.what = MsgConst.CLIENT_ACTION_SEND_DATA_TO_SERVER;
		msg.setData(bundle);
		try {
			if(mVoiceAssistantServiceMessenger != null)
				mVoiceAssistantServiceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
