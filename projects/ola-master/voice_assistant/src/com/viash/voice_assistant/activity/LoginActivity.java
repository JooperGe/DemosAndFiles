package com.viash.voice_assistant.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.common.Log;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.PopupDialog;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.util.UserManagerHttpUtil;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.JsonUtil;

/**
 * 登录
 * @author Harlan Song
 * @createDate 2013-3-26
 * @email:mark_dev@163.com
 */
public class LoginActivity extends Activity implements OnClickListener {
	private final String TAG = "LoginAct";
	private Button btn_login;
	private Button btn_register;
	private EditText et_username;
	private EditText et_password;
	private TextView tv_forget_pwd;
	private TextView tv_why_register;
	private final String registerKey = "login";
	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected VoiceAssistantServiceConnection mVoiceAssistantServiceConnection = null;
	private ProgressDialog dialog;
	private TextView title_back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		setContentView(R.layout.login);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(LoginActivity.this);
		dialog.setTitle(getResources().getString(R.string.login_loading_title));
		dialog.setMessage(getResources().getString(R.string.login_loading_message));
	}

	private void getView() {
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (Button) findViewById(R.id.btn_register);
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);
		tv_why_register = (TextView) findViewById(R.id.tv_why_register);
		title_back = (TextView) this.findViewById(R.id.setting_title);
		String defaultName = UserData.getUserName(this);
		if (defaultName != null) {
			et_username.setText(defaultName);
		}
	}

	private void setOnItemClick() {
		btn_login.setOnClickListener(this);
		btn_register.setOnClickListener(this);
		tv_forget_pwd.setOnClickListener(this);
		tv_why_register.setOnClickListener(this);
		title_back.setOnClickListener(this);
	}

	protected void initVoiceAssistantService() {
		mVoiceAssistantServiceConnection = new VoiceAssistantServiceConnection();
		VoiceSdkServiceInterface.bindToVoiceService(this, mVoiceAssistantServiceConnection);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_login:
			if(checkInputInfo()){
				dialog.show();
				login(et_username.getText().toString(), et_password.getText().toString());
			}
			break;
		case R.id.btn_register:
			Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_forget_pwd:
			//if(checkForgetPwdInputInfo())
			//	getPassword();
			Intent forgetIntent = new Intent(LoginActivity.this, ForgetPwdActivity.class);
			startActivity(forgetIntent);
			break;
		case R.id.tv_why_register:
			String content = getResources().getString(R.string.register_cause);//readUserAgreementFromAsset("License.txt");
			PopupDialog popupDialog = new PopupDialog(this);
			popupDialog.setTitle(getResources().getString(R.string.register_cause));
			popupDialog.setMessage(content);
			popupDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.register_license_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			popupDialog.show();
			break;
		case R.id.setting_title:
			this.finish();
			break;
		default:
			break;
		}
	}

	private boolean checkInputInfo() {
		boolean result = true;
		if(et_username.getText().toString().trim().length() == 0 ){
			result =false;
			et_username.setText("");
			et_username.setError(getResources().getString(R.string.login_please_input_username));
		}else if(ContentUtil.checkUserName(et_username.getText().toString().trim()) == false){
			result =false;
			et_username.setError(getResources().getString(R.string.login_username_to_long_alert));
		}else if(et_password.getText().toString().length() == 0){
			result =false;
			et_password.setError(getResources().getString(R.string.login_please_input_password));
		}
		
		return result;
	}
	
	private void exeGetBindedPhone() {
		Runnable getPhoneThread = new Runnable() {
			@Override
			public void run() {
				String phone = UserManagerHttpUtil.getBindedPhone(LoginActivity.this, et_username.getText().toString(), et_password.getText().toString());
				if (phone != null && phone.length() > 0) {
					UserData.setPhone(LoginActivity.this, phone);
				}
			}
		};
		new Thread(getPhoneThread).start();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
		if(GlobalData.isUserLoggedin())
		{
			Log.d(TAG, "logged");
			finish();
		}
		else
		{
			Log.d(TAG, "not logged");
			registerClient();
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
				Log.d(TAG, "registerClient");
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
			
			 JSONObject jsonLoginResult = (JSONObject) msg.obj;
				jsonLoginResult=JsonUtil.optJsonObj(jsonLoginResult, "data");
				String type = JsonUtil.optString(jsonLoginResult, "type", null);
				if("login".equalsIgnoreCase(type)){
					int status_code = JsonUtil.optInt(jsonLoginResult, "status_code", -1);
					//String resultMsg = JsonUtil.optString(jsonLoginResult, "status_msg", null);
					if(status_code == 0){
						UserData.saveUserInfo(LoginActivity.this, et_username.getText().toString(), et_password.getText().toString());
						exeGetBindedPhone();
						
						Message msgSend = Message.obtain(null, MsgConst.CLIENT_ACTION_USER_LOGINED);
						try {
							if(mVoiceAssistantServiceMessenger != null)
								mVoiceAssistantServiceMessenger.send(msgSend);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						GlobalData.setUserLoggedin(true);
						
						LoginActivity.this.finish();
						CustomToast.showShortText(LoginActivity.this,getResources().getString(R.string.login_success));
					}else{
						GlobalData.setUserLoggedin(false);
						CustomToast.showShortText(LoginActivity.this,getResources().getString(R.string.login_fail));
					}
				}	
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

	private void login(String user_id, String password) {
		JSONObject jsonLogin = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonData.put("type", "login");
			jsonData.put("user_id", user_id);
			jsonData.put("password", password);
			jsonLogin.put("data", jsonData);
			jsonLogin.put("data_type", "command");
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		MsgAsk msgAsk = new MsgAsk(jsonLogin);
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
