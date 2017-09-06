package com.viash.voice_assistant.renren;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;
import com.renn.rennsdk.RennExecutor.CallBack;
import com.renn.rennsdk.RennResponse;
import com.renn.rennsdk.exception.RennException;
import com.renn.rennsdk.param.UploadPhotoParam;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voicelib.utils.CustomToast;

public class PhotoServiceActivity extends Activity {
	private File picFile;
	private RennClient rennClient;
	private ProgressDialog mProgressDialog;
	//private int flag;
	private static final String API_KEY = "4ebc2d7f843047f29532a861ad8b5044";
	private static final String SECRET_KEY = "edbff35583624fc09ecce1a5bc488966";
	private static final String APP_ID = "236542";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}
	private void initView() {
		rennClient = RennClient.getInstance(this);
		rennClient.init(APP_ID, API_KEY, SECRET_KEY);
		rennClient.setScope("read_user_blog read_user_photo read_user_status read_user_album "
						+ "read_user_comment read_user_share publish_blog publish_share "
						+ "send_notification photo_upload status_update create_album "
						+ "publish_comment publish_feed");
		rennClient.setTokenType("bearer");
		rennClient.setLoginListener(new LoginListener() {
			@Override
			public void onLoginSuccess() {
				CustomToast.makeToast(PhotoServiceActivity.this, "登录成功");//,
						//Toast.LENGTH_SHORT).show();
				 upLoad();
			}
			@Override
			public void onLoginCanceled() {
				CustomToast.makeToast(PhotoServiceActivity.this, "登录失败");//,
						//Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		rennClient.login(this);
	}
	//上传到人人网
	public void upLoad(){
		String rootPath = PhotoServiceActivity.this.getFilesDir().getAbsolutePath();
		String imageUrl = rootPath + "/cachefile.png";
		picFile = new File(imageUrl);
		if (picFile.exists()) {
			UploadPhotoParam param = new UploadPhotoParam();
			try {
				param.setFile(picFile);
			} catch (Exception e) {
			}
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(PhotoServiceActivity.this);
				mProgressDialog.setCancelable(true);
				mProgressDialog.setTitle("请等待");
				mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
				mProgressDialog.setMessage("正在获取信息");
				mProgressDialog.show();
			}
			try {
				rennClient.getRennService().sendAsynRequest(param,
						new CallBack() {
							@Override
							public void onSuccess(RennResponse response) {
								CustomToast.makeToast(PhotoServiceActivity.this,
										"上传成功");//, Toast.LENGTH_SHORT).show();
								if (mProgressDialog != null) {
									mProgressDialog.dismiss();
									mProgressDialog = null;
								}
								Intent intent2 = new Intent(
										PhotoServiceActivity.this,
										NewAssistActivity.class);
								intent2.setFlags(intent2.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent2);
							}
							@Override
							public void onFailed(String errorCode,
									String errorMessage) {
								CustomToast.makeToast(PhotoServiceActivity.this,
										"获取失败");//, Toast.LENGTH_SHORT).show();
								if (mProgressDialog != null) {
									mProgressDialog.dismiss();
									mProgressDialog = null;
								}
							}
						});
			} catch (RennException e1) {
				e1.printStackTrace();
			}
		}
	}
}
