package com.viash.voice_assistant.activity;

import android.annotation.SuppressLint;
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
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.util.UserManagerHttpUtil;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;


public class ForgetPwdActivity extends Activity implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "ForgetPwdAct";
	
	private Button btn_get_code;
	private EditText et_username;
	private EditText et_phone;
	private LinearLayout layout_get_code;
	private LinearLayout layout_reset_pwd;
	private EditText et_pwd;
	private EditText et_pwd_cfm;
	private EditText et_code;
	private Button btn_modify_pwd;
	private Button btn_repeat_code;
	private TimeCountButton timeCountRepeatCode;
	private TimeCountButton timeCountGetCode;
	
	private final String registerKey = "forgetpassword";
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
		
		setContentView(R.layout.forget_password);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(ForgetPwdActivity.this);
		dialog.setTitle(getResources().getString(R.string.login_loading_title));
		dialog.setMessage(getResources().getString(R.string.login_loading_message));
	}

	private void getView() {
		layout_get_code = (LinearLayout) findViewById(R.id.layout_get_code);
		btn_get_code = (Button) findViewById(R.id.btn_get_code);
		et_username = (EditText) findViewById(R.id.et_username);
		et_phone = (EditText) findViewById(R.id.et_phone);
		
		layout_reset_pwd = (LinearLayout) findViewById(R.id.layout_reset_pwd);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_pwd_cfm = (EditText) findViewById(R.id.et_pwd_confirm);
		et_code = (EditText) findViewById(R.id.et_code);
		btn_modify_pwd = (Button) findViewById(R.id.btn_modify_pwd);
		btn_repeat_code = (Button) findViewById(R.id.btn_repeat_code);
		
		layout_get_code.setVisibility(View.VISIBLE);
		layout_reset_pwd.setVisibility(View.GONE);
		
		timeCountRepeatCode = new TimeCountButton(TimeCountButton.TIME_COUNT_FUTURE, TimeCountButton.TIME_COUNT_INTERVAL);
		timeCountRepeatCode.init(this, btn_repeat_code);
		timeCountGetCode = new TimeCountButton(TimeCountButton.TIME_COUNT_FUTURE, TimeCountButton.TIME_COUNT_INTERVAL);
		timeCountGetCode.init(this, btn_get_code);
		timeCountGetCode.setTickDrawable(getResources().getDrawable(R.drawable.login_btn_gray));
		title_back = (TextView) this.findViewById(R.id.setting_title);
	}

	private void setOnItemClick() {
		btn_get_code.setOnClickListener(this);
		btn_modify_pwd.setOnClickListener(this);
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
			if(checkUsernameInputInfo()){
				if (checkPhoneInputInfo()){
					dialog.show();
					exeGetCode();//status = UserManagerHttpUtil.getCode(this, et_username.getText().toString(), et_phone.getText().toString());
					dialog.dismiss();
				}
			}
			else {
				timeCountGetCode.start();
			}
			break;
		case R.id.btn_modify_pwd:
			if(checkPwdInputInfo()){
				if(checkCodeInputInfo()) {
					dialog.show();
					exeModifyPwd();
					//boolean ret = UserManagerHttpUtil.resetPwd(this, et_username.getText().toString(), et_pwd.getText().toString(), 
					//		et_phone.getText().toString(), et_code.getText().toString());
					dialog.dismiss();
				}
			}
			break;
		case R.id.btn_repeat_code:
			dialog.show();
			exeRepeatCode();//UserManagerHttpUtil.getCode(this, UserData.getPhone(this));
			dialog.dismiss();
			break;
		case R.id.setting_title:
			this.finish();
			break;
		default:
			break;
		}
	}
	
	private void exeGetCode() {
		Runnable getCodeThread = new Runnable() {
			@Override
			public void run() {
				int status = UserManagerHttpUtil.getCode(ForgetPwdActivity.this, et_username.getText().toString(), et_phone.getText().toString());
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
	
	private void exeModifyPwd() {
		Runnable modifyPwdThread = new Runnable() {
			@Override
			public void run() {
				boolean ret = UserManagerHttpUtil.resetPwd(ForgetPwdActivity.this, et_username.getText().toString(), et_pwd.getText().toString(), 
						et_phone.getText().toString(), et_code.getText().toString());
				if (ret == true) {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_MODIFY_PWD_SUCCESS);
				} else {
					mHandler.sendEmptyMessage(MsgConst.MSG_SENDCMD_MODIFY_PWD_FAILURE);
				}
			}
		};
		new Thread(modifyPwdThread).start();
	}
	
	private void exeRepeatCode() {
		Runnable repeatCodeThread = new Runnable() {
			@Override
			public void run() {
				int status = UserManagerHttpUtil.getCode(ForgetPwdActivity.this, et_username.getText().toString(), et_phone.getText().toString());
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
	
	@SuppressLint("HandlerLeak") 
	private class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MsgConst.MSG_SENDCMD_GET_CODE_SUCCESS:
				new AlertDialog.Builder(ForgetPwdActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_send_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						gotoResetLayout();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_GET_CODE_FAILURE:
				new AlertDialog.Builder(ForgetPwdActivity.this)
				.setMessage(getResources().getString(R.string.account_phone_not_matched))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_MODIFY_PWD_SUCCESS:
				new AlertDialog.Builder(ForgetPwdActivity.this)
				.setMessage(getResources().getString(R.string.pwd_modify_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ForgetPwdActivity.this.finish();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_MODIFY_PWD_FAILURE:
				new AlertDialog.Builder(ForgetPwdActivity.this)
				.setMessage(getResources().getString(R.string.reset_pwd_failed))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_REPEAT_CODE_SUCCESS:
				new AlertDialog.Builder(ForgetPwdActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_send_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeCountRepeatCode.start();
					}
				}).create().show();
				break;
			case MsgConst.MSG_SENDCMD_REPEAT_CODE_FAILURE:
				break;
			case MsgConst.MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S:
				new AlertDialog.Builder(ForgetPwdActivity.this)
				.setMessage(getResources().getString(R.string.phone_code_interval_less_than_60s))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeCountRepeatCode.start();
					}
				}).create().show();
				break;
			}
		}
	}

	private boolean checkUsernameInputInfo() {
		boolean result = true;
		if (et_username.getText().toString().trim().length() <= 0) {
			result = false;
			et_username.setError(getResources().getString(R.string.login_please_input_username));
		}
		return result;
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
	
	private boolean checkPwdInputInfo() {
		boolean result = true;
		if (!et_pwd.getText().toString().equals(et_pwd_cfm.getText().toString())) {
			result = false;
			et_pwd_cfm.setError(getResources().getString(R.string.register_password_unlike));
		}
		if (et_pwd.getText().toString().trim().length() <= 0) {
			result = false;
			et_pwd.setError(getResources().getString(R.string.login_please_input_password));
		}else if(et_pwd.getText().toString().trim().length() < 6 || et_pwd.getText().toString().trim().length() > 15){
			result = false;
			et_pwd.setError(getResources().getString(R.string.register_password_length_alert));
		}
		return result;
	}
	
	private boolean checkCodeInputInfo() {
		boolean result = true;
		if (et_code.getText().toString().trim().length() <= 0) {
			result = false;
			et_code.setError(getResources().getString(R.string.forgetpwd_please_input_code));
			//CustomToast.showShortText(PhoneAuthenticationAct.this,getResources().getString(R.string.forgetpwd_please_input_code));
		}
		return result;
	}

	private boolean gotoResetLayout() {
		boolean result = true;
		layout_get_code.setVisibility(View.GONE);
		layout_reset_pwd.setVisibility(View.VISIBLE);
		return result;
	}
	
	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		registerClient();
		super.onResume();
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
