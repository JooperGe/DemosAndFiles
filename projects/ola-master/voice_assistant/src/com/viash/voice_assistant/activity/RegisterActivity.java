package com.viash.voice_assistant.activity;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.PopupDialog;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.JsonUtil;

public class RegisterActivity extends Activity  implements OnClickListener{
	private final String TAG = "RegisterAct";
	private EditText et_username;
	private EditText et_password;
	private EditText et_password_again;
	private EditText et_phone;
	private EditText et_email;
	private CheckBox checkbox1;
	private TextView tv_protocol;
	private Button btn_register;
	private Button btn_cancel;
	private final String registerKey = "register";
	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected VoiceAssistantServiceConnection mVoiceAssistantServiceConnection = null;
	private ProgressDialog dialog;
	private TextView title_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		setContentView(R.layout.register);
		getView();
		setOnItemClick();
		initVoiceAssistantService();
		dialog =new ProgressDialog(RegisterActivity.this);
		dialog.setTitle(getResources().getString(R.string.register_loading_title));
		dialog.setMessage(getResources().getString(R.string.register_loading_message));
	}
	
	private void getView() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		et_phone = (EditText) findViewById(R.id.et_phone_num);
		et_email = (EditText) findViewById(R.id.et_email);
		et_password_again = (EditText) findViewById(R.id.et_password_again);
		checkbox1 = (CheckBox)findViewById(R.id.checkbox1);
		checkbox1.setChecked(true);
		tv_protocol = (TextView)findViewById(R.id.tv_protocol);
		tv_protocol.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		btn_register = (Button) findViewById(R.id.btn_register);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		title_back = (TextView) this.findViewById(R.id.setting_title);
	}
	
	private void setOnItemClick(){
		tv_protocol.setOnClickListener(this);
		btn_register.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		title_back.setOnClickListener(this);
	}
	

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.tv_protocol:
			String protocolContent = readUserAgreementFromAsset("License.txt");
			PopupDialog popupDialog = new PopupDialog(this);
			popupDialog.setTitle(getResources().getString(R.string.register_license_title));
			popupDialog.setMessage(protocolContent);
			popupDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.register_license_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			popupDialog.show();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		case R.id.btn_register:
			if(checkInputInfo()){
				register(et_username.getText().toString(),et_password.getText().toString(),et_email.getText().toString(),et_phone.getText().toString());
				dialog.show();
			}
			break;
		case R.id.setting_title:
			this.finish();				
			break;
		}
	}

	private boolean checkInputInfo(){
		boolean info = true;
		if(checkbox1.isChecked() == false) {
			info =false;
			CustomToast.showShortText(RegisterActivity.this, getResources().getString(R.string.register_license_not_agree));
		}
		else if(et_username.getText().toString().trim().length() == 0 ){
			info =false;
			et_username.setText("");
			et_username.setError(getResources().getString(R.string.register_username_is_null_alert));
		}else if(ContentUtil.checkUserNameByRegex(et_username.getText().toString().trim()) == false){
			info =false;
			et_username.setError(getResources().getString(R.string.register_username_wrongful));
		}else if(et_password.getText().toString().length() == 0){
			info =false;
			et_password.setError(getResources().getString(R.string.register_password_is_null_alert));
		}else if(et_password.getText().toString().length() > 15 || et_password.getText().toString().length() < 3){
			info =false;
			et_password.setError(getResources().getString(R.string.register_password_length_alert));
		}else if(!et_password.getText().toString().equals(et_password_again.getText().toString())){
			info =false;
			CustomToast.showShortText(RegisterActivity.this, getResources().getString(R.string.register_password_unlike));
		}/*else if(et_email.getText().toString().length() == 0){
			info =false;
			et_email.setError(getResources().getString(R.string.register_email_is_null));
		}else if(ContentUtil.checkEmail(et_email.getText().toString()) == false){
			info =false;
			et_email.setError(getResources().getString(R.string.register_email_wrongful));
		}*/
		return info;
	}
	protected void initVoiceAssistantService() {
		mVoiceAssistantServiceConnection = new VoiceAssistantServiceConnection();
		VoiceSdkServiceInterface.bindToVoiceService(this, mVoiceAssistantServiceConnection);
	}
	
	class VoiceAssistantServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (mVoiceAssistantServiceMessenger == null)
				mVoiceAssistantServiceMessenger = new Messenger(service);
			//registerClient();
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
				JSONObject jsonRegisterResult = (JSONObject) msg.obj;
				jsonRegisterResult = JsonUtil.optJsonObj(jsonRegisterResult,"data");
				String type = JsonUtil.optString(jsonRegisterResult, "type",null);
				if ("register".equalsIgnoreCase(type)) {
					int status_code = JsonUtil.optInt(jsonRegisterResult,"status_code", -1);
					// String resultMsg = JsonUtil.optString(jsonRegisterResult, "status_msg", null);
					if (status_code == 0) {
						UserData.saveUserInfo(getApplicationContext(),et_username.getText().toString(), et_password.getText().toString());

						Message msgSend = Message.obtain(null,MsgConst.CLIENT_ACTION_USER_LOGINED);
						try {
							if (mVoiceAssistantServiceMessenger != null)
								mVoiceAssistantServiceMessenger.send(msgSend);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						RegisterActivity.this.finish();
						CustomToast.showShortText(getApplicationContext(),R.string.register_success);
					} else if (status_code == 2) {
						CustomToast.showShortText(getApplicationContext(),getResources().getString(R.string.register_username_disabled));
					} else {
						CustomToast.showShortText(getApplicationContext(),getResources().getString(R.string.register_fail));
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

	private void register(String user_id, String password,String email,String phone) {
		JSONObject jsonRegister = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonData.put("type", "register");
			jsonData.put("user_id", user_id);
			jsonData.put("password", password);
			//jsonData.put("email", email);
			jsonRegister.put("data", jsonData);
			jsonRegister.put("data_type", "command");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		MsgAsk msgAsk = new MsgAsk(jsonRegister);
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

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		registerClient();
		super.onResume();
	}
	
	private String readUserAgreementFromAsset(String assetName) {
		String content = "";
		try {
			InputStream is = getAssets().open(assetName);
			if (is != null) {
				DataInputStream dIs = new DataInputStream(is);
				int length = dIs.available();
				byte[] buffer = new byte[length];
				dIs.read(buffer);
				content = EncodingUtils.getString(buffer, "UTF-8");
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
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
