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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.AppData;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;


public class NicknameActivity extends Activity implements OnClickListener {
	private static final String TAG = "NicknameActivity";
	
	private Button btn_submit;
	private EditText et_nickname;
	private final String registerKey = "nickname";
	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected VoiceAssistantServiceConnection mVoiceAssistantServiceConnection = null;
	private ProgressDialog dialog;
	private TextView title_back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);	
		setContentView(R.layout.activity_nickname);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(NicknameActivity.this);
		dialog.setTitle(getResources().getString(R.string.login_loading_title));
		dialog.setMessage(getResources().getString(R.string.login_loading_message));
	}

	private void getView() {
		btn_submit = (Button) findViewById(R.id.btn_submit);
		et_nickname = (EditText) findViewById(R.id.et_nickname);
		et_nickname.setText(AppData.getNickname(this));
		et_nickname.setText(AppData.getNickname(this));
		title_back = (TextView) this.findViewById(R.id.setting_title);
		et_nickname.addTextChangedListener(new TextWatcher() {
			  @Override
			  public void onTextChanged(CharSequence s, int start, int before, int count) {}
			  @Override
			  public void beforeTextChanged(CharSequence s, int start, int count,
					int after) { }
			  @Override
			  public void afterTextChanged(Editable s) {
				    String nickname=s.toString();
				    if(nickname.length()>15){
				    	  s.delete(nickname.length()-1, nickname.length());
						  CustomToast.showShortText(NicknameActivity.this, getResources().getString(R.string.nickname_toolong));
					 }
				    else if(nickname.length()>0){
					      if(!checkNickNameByRegex(nickname)){
						      s.delete(nickname.length()-1, nickname.length());
						      CustomToast.showShortText(NicknameActivity.this, getResources().getString(R.string.nickname_wrongful)); 
					      }		
				    }			        
		     }
		 });
   }
	
	private void setOnItemClick() {
		btn_submit.setOnClickListener(this);
		title_back.setOnClickListener(this);
	}

	protected void initVoiceAssistantService() {
		mVoiceAssistantServiceConnection = new VoiceAssistantServiceConnection();
		VoiceSdkServiceInterface.bindToVoiceService(this, mVoiceAssistantServiceConnection);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_submit:
			if(checkInputInfo()){
				AppData.setNickname(this, et_nickname.getText().toString());
				AppData.setIsNicknameSendToServer(this, true);
				new AlertDialog.Builder(NicknameActivity.this)
				.setMessage(getResources().getString(R.string.nickname_submit_ok))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						NicknameActivity.this.finish();
					}
				}).create().show();
			}
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
		if(et_nickname.getText().toString().length()== 0) {
			CustomToast.showShortText(NicknameActivity.this, getResources().getString(R.string.login_please_input_username));
			result = false;
		}	
		else{
		   result=true;
		}
		return result;
	}
	/**BensonZhang
	 * Nickname verification
	 * Nickname rules :1-15 characters, can use letters, numbers, Chinese
	 */
	public boolean checkNickNameByRegex(String content) {
		boolean result = true;
		String format = "^([A-Za-z]|[0-9]|[\u4E00-\u9FA5]){0,}$";
		result = content.matches(format);
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
