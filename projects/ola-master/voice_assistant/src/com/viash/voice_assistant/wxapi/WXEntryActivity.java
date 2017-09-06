package com.viash.voice_assistant.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.viash.voice_assistant.R;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.LogOutput;
import com.viash.voicelib.utils.WeixinUtil;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	public static final String TAG = "WXEntryActivity";
	
	public static final String APP_ID = "wxa1151194c03010f0";//调试用app_id，正式app_id是:"wx8e684dd5e2295a6f";
	public static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	private IWXAPI api;
	private Button toFriend;
	private Button toFriendCircle;
	private Button capturescreen2weixin;
	
	public static final String STitle = "showmsg_title";
	public static final String SMessage = "showmsg_message";
	public static final String BAThumbData = "showmsg_thumb_data";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share2weixin);
		
		api = WeixinUtil.register2Weixin(this);
		initView();
		initListener();
		api.handleIntent(getIntent(), this);
	}

	private void initView() {
		toFriend = (Button) this.findViewById(R.id.tofriend);
		toFriendCircle = (Button) this.findViewById(R.id.tofriendcircle);
		capturescreen2weixin = (Button) this.findViewById(R.id.capturescreen2weixin);
	}
	
	private void initListener() {
		toFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!WeixinUtil.isWXAppInstalledAndSupported(WXEntryActivity.this,api)) {
					return;
				}
				WeixinUtil.sendText(WXEntryActivity.this, api, true, "test ola.");
			}
		});
		
		toFriendCircle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!WeixinUtil.isWXAppInstalledAndSupported(WXEntryActivity.this,api)) {
					return;
				}
				WeixinUtil.sendText(WXEntryActivity.this, api, false, "test ola.");
			}
		});
		
		capturescreen2weixin.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!WeixinUtil.isWXAppInstalledAndSupported(WXEntryActivity.this,api)) {
					return;
				}
				Bitmap bmp = loadBitmapFromView(capturescreen2weixin);
				WeixinUtil.sendScreenCapture(WXEntryActivity.this, api, true, bmp, "title", "description");
			}
		});
	}
	
	private Bitmap loadBitmapFromView(View view) {
		Bitmap bitmap = null;
		try {
			int width = view.getWidth();
			int height = view.getHeight();
			if(width != 0 && height != 0){
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				
				view.layout(0, 0, width, height);
				view.draw(canvas);
			}
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			Log.v(TAG, "COMMAND_GETMESSAGE_FROM_WX");
			//goToGetMsg();		
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			Log.v(TAG, "COMMAND_SHOWMESSAGE_FROM_WX");
			//goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		String result;
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = "分享成功";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "分享请求被取消";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "分享请求被拒绝";
			break;
		default:
			result = "";
			break;
		}
		
		Log.v(TAG, "onResp result:"+result);
		CustomToast.makeToast(this, result);//, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onDestroy() {
		if (api != null) {
			//api.unregisterApp();
		}
		super.onDestroy();
	}
	
	private void goToGetMsg() {
		LogOutput.d(TAG, "goToGetMsg");
		Intent intent = new Intent(this, WXEntryActivity.class);
		intent.putExtras(getIntent());
		startActivity(intent);
		finish();
	}
	
	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		LogOutput.d(TAG, "goToShowMsg");
		WXMediaMessage wxMsg = showReq.message;		
		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
		
		StringBuffer msg = new StringBuffer();
		msg.append("description: ");
		msg.append(wxMsg.description);
		msg.append("\n");
		msg.append("extInfo: ");
		msg.append(obj.extInfo);
		msg.append("\n");
		msg.append("filePath: ");
		msg.append(obj.filePath);
		
		Intent intent = new Intent(this, WXEntryActivity.class);
		intent.putExtra(STitle, wxMsg.title);
		intent.putExtra(SMessage, msg.toString());
		intent.putExtra(BAThumbData, wxMsg.thumbData);
		startActivity(intent);
		finish();
	}
}
