package com.viash.voice_assistant.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.TimeCountButton;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.util.UserManagerHttpUtil;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;


public class PhoneAuthenticationActivity extends Activity implements OnClickListener {
	private static final String TAG = "PhoneAuthenticationAct";
	
	private Button btn_get_code;
	private EditText et_phone;
	private LinearLayout layout_get_code;
	private LinearLayout layout_submit_code;
	private Button btn_change_phone;
	private Button btn_submit_code;
	private Button btn_repeat_code;
	private TextView tv_phone;
	private EditText et_code;
	private TimeCountButton timeCountRepeatCode;
	private TimeCountButton timeCountGetCode;
	
	private final String registerKey = "phoneauthentication";
	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected VoiceAssistantServiceConnection mVoiceAssistantServiceConnection = null;
	private ProgressDialog dialog;
	
	private MyHandler mHandler;
	private TextView title_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		mHandler = new MyHandler();
		
		setContentView(R.layout.activity_phone_authentication);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(PhoneAuthenticationActivity.this);
		dialog.setTitle(getResources().getString(R.string.login_loading_title));
		dialog.setMessage(getResources().getString(R.string.login_loading_message));
	}

	private void getView() {
		layout_get_code = (LinearLayout) findViewById(R.id.layout_get_code);
		btn_get_code = (Button) findViewById(R.id.btn_get_code);
		et_phone = (EditText) findViewById(R.id.et_phone);
		
		layout_submit_code = (LinearLayout) findViewById(R.id.layout_submit_code);
		btn_change_phone = (Button) findViewById(R.id.btn_change_phone);
		btn_submit_code = (Button) findViewById(R.id.btn_submit_code);
		btn_repeat_code = (Button) findViewById(R.id.btn_repeat_code);
		tv_phone = (TextView) findViewById(R.id.tv_phone);
		et_code = (EditText) findViewById(R.id.et_code);

		layout_get_code.setVisibility(View.VISIBLE);
		layout_submit_code.setVisibility(View.GONE);
		
		timeCountRepeatCode = new TimeCountButton(TimeCountButton.TIME_COUNT_FUTURE, TimeCountButton.TIME_COUNT_INTERVAL);
		timeCountRepeatCode.init(this, btn_repeat_code);
		timeCountGetCode = new TimeCountButton(TimeCountButton.TIME_COUNT_FUTURE, TimeCountButton.TIME_COUNT_INTERVAL);
		timeCountGetCode.init(this, btn_get_code);
		timeCountGetCode.setTickDrawable(getResources().getDrawable(R.drawable.login_btn_gray));
		title_back = (TextView) this.findViewById(R.id.setting_title);
	}

	private boolean setPhoneAsteriskText(TextView tv_phone, String phone) {
		if (phone.length() >= 11) {
			String phoneAsterisk = phone.substring(0, 3) + "*****" + phone.substring(8);
			tv_phone.setText(phoneAsterisk);
			return true;
		}
		return false;
	}
	
	private void setOnItemClick() {
		btn_get_code.setOnClickListener(this);
		btn_change_phone.setOnClickListener(this);
		btn_submit_code.setOnClickListener(this);
		btn_repeat_code.setOnClickListener(this);
		title_back.setOnClickListener(this);
	}

	protected void initVoiceAssistantService() {
		mVoiceAssistantServiceConnection = new VoiceAssistantServiceConnection();
		VoiceSdkServiceInterface.bindToVoiceService(this, mVoiceAssistantServiceConnection);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_get_code:
			if(checkPhoneInputInfo()){
				dialog.show();
				exeIsMobileBeUsed();
				dialog.dismiss();
				timeCountGetCode.start();
			}
			break;
		case R.id.btn_change_phone:
			gotoGetCodeLayout();
			break;
		case R.id.btn_submit_code:
			if(checkCodeInputInfo()){
				dialog.show();
				exeSubmitCode();
				dialog.dismiss();
			}
			break;
		case R.id.btn_repeat_code:
			dialog.show();
			exeRepeatCode();
			dialog.dismiss();
			break;
		case R.id.setting_title:
			this.finish();
			break;
		default:
			break;
		}
	}
	
	private void exeIsMobileBeUsed() {
		Runnable isMobileBeUsedThread = new Runnable() {
			@Override
			public void run() {
				boolean ret = UserManagerHttpUtil.isMobileBeUsed(PhoneAuthenticationActivity.this, et_phone.getText().toString());
				if (ret) {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_PHONE_BE_USED);
				} else {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_PHONE_NOT_BE_USED);
				}
			}
		};
		new Thread(isMobileBeUsedThread).start();
	}
	
	private void exeGetCode() {
		Runnable getCodeThread = new Runnable() {
			@Override
			public void run() {
				int status = UserManagerHttpUtil.getCode(PhoneAuthenticationActivity.this, et_phone.getText().toString());
				if (status == UserManagerHttpUtil.RET_SUCCESS) {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_GET_CODE_SUCCESS);
				}else if(status == UserManagerHttpUtil.RET_INTERVAL_LESS_THAN_60S){
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S);
				}else {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_GET_CODE_FAILURE);
				}
			}
		};
		new Thread(getCodeThread).start();
	}
	
	private void exeSubmitCode() {
		Runnable submitCodeThread = new Runnable() {
			@Override
			public void run() {
				String[] userInfo = UserData.getUserInfo(PhoneAuthenticationActivity.this);
				String username = "";
				String pwd = "";
				if (userInfo.length >= 2) {
					username = userInfo[0];
					pwd = userInfo[1];
				}
				int status = UserManagerHttpUtil.submitCode(PhoneAuthenticationActivity.this, username, pwd, et_phone.getText().toString(), et_code.getText().toString());
				if (status == UserManagerHttpUtil.RET_SUCCESS) {
					UserData.setPhone(PhoneAuthenticationActivity.this, et_phone.getText().toString());
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_SUBMIT_CODE_SUCCESS);
				} else if(status == UserManagerHttpUtil.RET_MOBILE_VCODE_ERROR){
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_SUBMIT_CODE_VCODE_ERROR);
				} else if(status == UserManagerHttpUtil.RET_UID_PWD_NOT_MATCHED){
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_SUBMIT_CODE_ID_PWD_NOT_MATCHED);
				} else {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_SUBMIT_CODE_OTHER_ERROR);
				}
			}
		};
		new Thread(submitCodeThread).start();
	}
	
	private void exeRepeatCode() {
		Runnable repeatCodeThread = new Runnable() {
			@Override
			public void run() {
				int status = UserManagerHttpUtil.getCode(PhoneAuthenticationActivity.this, et_phone.getText().toString());
				if (status == UserManagerHttpUtil.RET_SUCCESS){
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_REPEAT_CODE_SUCCESS);
				} else if(status == UserManagerHttpUtil.RET_INTERVAL_LESS_THAN_60S){
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S);
				} else {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_REPEAT_CODE_FAILURE);
				}
			}
		};
		new Thread(repeatCodeThread).start();
	}
	
	private class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MsgConst.MSG_SENDCMD_GET_CODE_SUCCESS:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_send_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						long expireDate = System.currentTimeMillis() + UserManagerHttpUtil.VCODE_AVAILABLE_PERIOD;
						UserData.setVCodeExpireDate(PhoneAuthenticationActivity.this, expireDate);
						UserData.setVCodeMobile(PhoneAuthenticationActivity.this, et_phone.getText().toString());
						gotoSubmitLayout(et_phone.getText().toString());
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_GET_CODE_FAILURE:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_submit_failure))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_SUCCESS:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_bind_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						UserData.setVCodeExpireDate(PhoneAuthenticationActivity.this, 0);
						UserData.setVCodeMobile(PhoneAuthenticationActivity.this, null);
						PhoneAuthenticationActivity.this.finish();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_VCODE_ERROR:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_input_error))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_ID_PWD_NOT_MATCHED:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_id_pwd_not_matched))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_OTHER_ERROR:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_submit_failure))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_REPEAT_CODE_SUCCESS:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_send_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						long expireDate = System.currentTimeMillis() + UserManagerHttpUtil.VCODE_AVAILABLE_PERIOD;
						UserData.setVCodeExpireDate(PhoneAuthenticationActivity.this, expireDate);
						UserData.setVCodeMobile(PhoneAuthenticationActivity.this, et_phone.getText().toString());
						timeCountRepeatCode.start();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_REPEAT_CODE_FAILURE:
				break;
			case MsgConst.MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_interval_less_than_60s))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeCountRepeatCode.start();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_PHONE_BE_USED:
				new AlertDialog.Builder(PhoneAuthenticationActivity.this)
				.setMessage(getResources().getString(R.string.phone_binded_please_input_other))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_PHONE_NOT_BE_USED:
				dialog.show();
				exeGetCode();
				dialog.dismiss();
				break;
			}
		}
	}


	private boolean checkPhoneInputInfo() {
		boolean result = true;
		if (et_phone.getText().toString().trim().length() == 0) {
			result = false;
			et_phone.setError(getResources().getString(R.string.phone_please_input_phone));
		} else if (ContentUtil.checkMobileCN(et_phone.getText().toString().trim()) == false) {
			result = false;
			et_phone.setError(getResources().getString(R.string.phone_input_error));
		}
		return result;
	}
	
	private boolean checkCodeInputInfo() {
		boolean result = true;
		if (et_code.getText().toString().trim().length() <= 0) {
			result = false;
			et_code.setError(getResources().getString(R.string.forgetpwd_please_input_code));
		}
		return result;
	}
	
	private boolean gotoSubmitLayout(String phone) {
		boolean result = true;
		layout_get_code.setVisibility(View.GONE);
		layout_submit_code.setVisibility(View.VISIBLE);
		setPhoneAsteriskText(tv_phone, phone);
		return result;
	}
	
	private boolean gotoGetCodeLayout() {
		boolean result = true;
		layout_get_code.setVisibility(View.VISIBLE);
		layout_submit_code.setVisibility(View.GONE);
		et_phone.setText("");
		et_phone.setHint(R.string.forgetpwd_please_input_code);
		return result;
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		registerClient();
		super.onResume();
		long currentDate = System.currentTimeMillis();
		long expireDate = UserData.getVCodeExpireDate(this);
		String vcodeMobile = UserData.getVCodeMobile(this);
		if (currentDate < expireDate && vcodeMobile != null && vcodeMobile.length() > 0){
			et_phone.setText(vcodeMobile);
			gotoSubmitLayout(vcodeMobile);
		}
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
			}*/

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
}
