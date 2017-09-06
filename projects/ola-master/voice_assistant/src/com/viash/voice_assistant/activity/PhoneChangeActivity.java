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


public class PhoneChangeActivity extends Activity implements OnClickListener {
	private static final String TAG = "PhoneChangeAct";
	
	private LinearLayout layout_phone_number;
	private LinearLayout layout_change_phone;
	private LinearLayout layout_get_code;
	private LinearLayout layout_submit_code;
	private Button btn_change_phone_2;
	private Button btn_change_phone;
	private EditText et_new_phone;
	private EditText et_code;
	private EditText et_pwd;
	private Button btn_get_code;
	private Button btn_submit_code;
	private Button btn_repeat_code;
	private TextView tv_phone;
	private TimeCountButton timeCountRepeatCode;
	private TimeCountButton timeCountGetCode;
	
	private final String registerKey = "phonechange";
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
		
		setContentView(R.layout.activity_phone_change);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(PhoneChangeActivity.this);
		dialog.setTitle(getResources().getString(R.string.login_loading_title));
		dialog.setMessage(getResources().getString(R.string.login_loading_message));
	}

	private void getView() {
		layout_phone_number = (LinearLayout) findViewById(R.id.layout_phone_number);
		btn_change_phone_2 = (Button) findViewById(R.id.btn_change_phone_2);
		tv_phone = (TextView) findViewById(R.id.tv_phone);
		
		layout_change_phone = (LinearLayout) findViewById(R.id.layout_change_phone);
		btn_change_phone = (Button) findViewById(R.id.btn_change_phone);
		
		layout_get_code = (LinearLayout) findViewById(R.id.layout_get_code);
		et_new_phone = (EditText) findViewById(R.id.et_new_phone);
		btn_get_code = (Button) findViewById(R.id.btn_get_code);
		
		layout_submit_code = (LinearLayout) findViewById(R.id.layout_submit_code);
		et_code = (EditText) findViewById(R.id.et_code);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		btn_submit_code = (Button) findViewById(R.id.btn_submit_code);
		btn_repeat_code = (Button) findViewById(R.id.btn_repeat_code);
		
		gotoChangePhoneLayout();
		
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
		btn_change_phone_2.setOnClickListener(this);
		btn_change_phone.setOnClickListener(this);
		btn_get_code.setOnClickListener(this);
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
		case R.id.btn_change_phone_2:
			gotoGetCodeLayout();
			break;
		case R.id.btn_change_phone:
			gotoGetCodeLayout();
			break;
		case R.id.btn_get_code:
			if (checkPhoneInputInfo()) {
				dialog.show();
				exeIsMobileBeUsed();//exeGetCode();
				dialog.dismiss();
			}else {
				timeCountGetCode.start();
			}
			break;
		case R.id.btn_submit_code:
			if (checkCodeInputInfo()) {
				if (checkPwdInputInfo()) {
					dialog.show();
					exeSubmitCode();
					dialog.dismiss();
				}
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
				boolean ret = UserManagerHttpUtil.isMobileBeUsed(PhoneChangeActivity.this, et_new_phone.getText().toString());
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
				int status = UserManagerHttpUtil.getCode(PhoneChangeActivity.this, et_new_phone.getText().toString());
				if (status == UserManagerHttpUtil.RET_SUCCESS){
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
				String[] userInfo = null;
				String username = "";
				String pwd = "";
				userInfo = UserData.getUserInfo(PhoneChangeActivity.this);
				if (userInfo.length >= 2) {
					username = userInfo[0];
					pwd = userInfo[1];
				}
				int status = UserManagerHttpUtil.submitCode(PhoneChangeActivity.this, username, pwd, et_new_phone.getText().toString(), et_code.getText().toString());
				if (status == UserManagerHttpUtil.RET_SUCCESS) {
					UserData.setPhone(PhoneChangeActivity.this, et_new_phone.getText().toString());
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
				int status = UserManagerHttpUtil.getCode(PhoneChangeActivity.this, et_new_phone.getText().toString());
				if (status == UserManagerHttpUtil.RET_SUCCESS) {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_REPEAT_CODE_SUCCESS);
				}else if(status == UserManagerHttpUtil.RET_INTERVAL_LESS_THAN_60S){
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S);
				}else {
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
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_send_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						long expireDate = System.currentTimeMillis() + UserManagerHttpUtil.VCODE_AVAILABLE_PERIOD;
						UserData.setVCodeExpireDate(PhoneChangeActivity.this, expireDate);
						UserData.setVCodeMobile(PhoneChangeActivity.this, et_new_phone.getText().toString());
						gotoSubmitLayout(et_new_phone.getText().toString());
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_GET_CODE_FAILURE:
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_SUCCESS:
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.new_phone_bind_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,int which) {
						UserData.setVCodeExpireDate(PhoneChangeActivity.this, 0);
						UserData.setVCodeMobile(PhoneChangeActivity.this, null);
						PhoneChangeActivity.this.finish();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_VCODE_ERROR:
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_input_error))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_ID_PWD_NOT_MATCHED:
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.phone_id_pwd_not_matched))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_SUBMIT_CODE_OTHER_ERROR:
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_submit_failure))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_REPEAT_CODE_SUCCESS:
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_send_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						long expireDate = System.currentTimeMillis() + UserManagerHttpUtil.VCODE_AVAILABLE_PERIOD;
						UserData.setVCodeExpireDate(PhoneChangeActivity.this, expireDate);
						UserData.setVCodeMobile(PhoneChangeActivity.this, et_new_phone.getText().toString());
						timeCountRepeatCode.start();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_REPEAT_CODE_FAILURE:
				break;
			case MsgConst.MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S:
				new AlertDialog.Builder(PhoneChangeActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_interval_less_than_60s))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeCountRepeatCode.start();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_PHONE_BE_USED:
				new AlertDialog.Builder(PhoneChangeActivity.this)
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
		String newPhone = et_new_phone.getText().toString().trim();
		if (newPhone.length() == 0) {
			result = false;
			et_new_phone.setError(getResources().getString(R.string.phone_please_input_phone));
		}else if (ContentUtil.checkMobileCN(newPhone) == false) {
			result = false;
			et_new_phone.setError(getResources().getString(R.string.phone_input_error));
		}else if (newPhone.equals(UserData.getPhone(PhoneChangeActivity.this))) {
			result = false;
			et_new_phone.setError(getResources().getString(R.string.new_phone_same_as_old));
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
	
	private boolean checkPwdInputInfo() {
		boolean result = true;
		if (et_pwd.getText().toString().trim().length() <= 0) {
			result = false;
			et_pwd.setError(getResources().getString(R.string.login_please_input_password));
		}else{
			String[] userInfo = UserData.getUserInfo(PhoneChangeActivity.this);
			String pwd = "";
			if (userInfo.length >= 2) {
				pwd = userInfo[1];
			}
			if (!pwd.equals(et_pwd.getText().toString().trim())){
				result = false;
				et_pwd.setError(getResources().getString(R.string.pwd_error_please_input_again));
			}
		}
		return result;
	}
	
	private boolean gotoChangePhoneLayout() {
		boolean result = true;
		layout_phone_number.setVisibility(View.VISIBLE);
		btn_change_phone_2.setVisibility(View.GONE);
		layout_change_phone.setVisibility(View.VISIBLE);
		layout_get_code.setVisibility(View.GONE);
		layout_submit_code.setVisibility(View.GONE);
		setPhoneAsteriskText(tv_phone, UserData.getPhone(this));
		return result;
	}
	
	private boolean gotoGetCodeLayout() {
		boolean result = true;
		layout_phone_number.setVisibility(View.GONE);
		layout_change_phone.setVisibility(View.GONE);
		layout_get_code.setVisibility(View.VISIBLE);
		layout_submit_code.setVisibility(View.GONE);
		et_new_phone.setText("");
		et_new_phone.setHint(R.string.phone_please_input_new_phone);
		return result;
	}
	
	private boolean gotoSubmitLayout(String phone) {
		boolean result = true;
		layout_phone_number.setVisibility(View.VISIBLE);
		btn_change_phone_2.setVisibility(View.VISIBLE);
		layout_change_phone.setVisibility(View.GONE);
		layout_get_code.setVisibility(View.GONE);
		layout_submit_code.setVisibility(View.VISIBLE);
		setPhoneAsteriskText(tv_phone, phone);
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
			et_new_phone.setText(vcodeMobile);
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
