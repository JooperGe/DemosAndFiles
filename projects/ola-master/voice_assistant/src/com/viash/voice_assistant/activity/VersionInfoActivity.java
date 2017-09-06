package com.viash.voice_assistant.activity;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.AutoUpdate;
import com.viash.voicelib.utils.ClientPropertyUtil;
import com.viash.voicelib.utils.CommunicationUpdateUtil;
import com.viash.voicelib.utils.CustomToast;

public class VersionInfoActivity extends Activity {
	private static final String TAG = "VersionInfoActivity";
	
	private TextView software_type;
	private TextView software_classification;
	private TextView software_version;
	private TextView update_time;
	private TextView system_requirement;
	private Button check_update;
	private TextView title_back;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		setContentView(R.layout.activity_versioninfo);

		initView();
		initListener();
	}
	
	/*UmengUpdateListener updateListener = new UmengUpdateListener() {
		@Override
		public void onUpdateReturned(int updateStatus,
				UpdateResponse updateInfo) {
			switch (updateStatus) {
			case 0: // has update
				Log.i(TAG, "有新版本可以更新");
				UmengUpdateAgent.showUpdateDialog(VersionInfoActivity.this, updateInfo);
				break;
			case 1: // has no update
				CustomToast.makeToast(VersionInfoActivity.this, "没有新版本");//, Toast.LENGTH_SHORT)
						//.show();
				break;
			case 2: // none wifi
				CustomToast.makeToast(VersionInfoActivity.this, "没有wifi连接， 只在wifi下更新");//, Toast.LENGTH_SHORT)
						//.show();
				break;
			case 3: // time out
				CustomToast.makeToast(VersionInfoActivity.this, "超时");//, Toast.LENGTH_SHORT)
						//.show();
				break;
			case 4: // is updating
				/*Toast.makeText(mContext, "正在下载更新...", Toast.LENGTH_SHORT)
						.show();
				break;
			}

		}
	};*/

	private void initListener() {
		/*if(GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG)
		{
			final AutoUpdateHandler autoUpdateHandler = new AutoUpdateHandler(VersionInfoActivity.this);
			check_update.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					AutoUpdate.init(VersionInfoActivity.this, autoUpdateHandler);
					if(!AutoUpdate.start()){
						Log.e(VersionInfoActivity.this.getPackageName(), "start auto update fail.");
					}
				}
			});
		}
		else*/
		{
			check_update.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					/*com.umeng.common.Log.LOG = true;
					UmengUpdateAgent.setUpdateAutoPopup(false);
					//UmengUpdateAgent.setUpdateOnlyWifi(false); // 目前我们默认在Wi-Fi接入情况下才进行自动提醒。如需要在其他网络环境下进行更新自动提醒，则请添加该行代码
					UmengUpdateAgent.setUpdateListener(updateListener);
					UmengUpdateAgent.setDownloadListener(new UmengDownloadListener(){

						@Override
						public void OnDownloadEnd(int arg0, String arg1) {
							Log.i(TAG, "download result : " + arg0);
							CustomToast.makeToast(VersionInfoActivity.this, "下载结果 : " + arg0 );// , Toast.LENGTH_SHORT)
							//.show();							
						}

						@Override
						public void OnDownloadStart() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void OnDownloadUpdate(int arg0) {
							// TODO Auto-generated method stub
							
						}
					});
					UmengUpdateAgent.update(VersionInfoActivity.this);*/
					if(AutoUpdate.isDownloading() == false)
					{
						CommunicationUpdateUtil communicationUpdateUtil = new CommunicationUpdateUtil(VersionInfoActivity.this);
						communicationUpdateUtil.setIsFromSettingMenu(true);
						communicationUpdateUtil.getDataFromServer();
					}
					else
					{
						CustomToast.makeToast(VersionInfoActivity.this, "新版本已经在下载中!");
					}
				}
			});
		}		
	}

	private void initView() {
		software_type = (TextView) this.findViewById(R.id.software_type);
		software_classification = (TextView) this.findViewById(R.id.software_classification);
		software_version = (TextView) this.findViewById(R.id.software_version);
		update_time = (TextView) this.findViewById(R.id.update_time);
		system_requirement = (TextView) this.findViewById(R.id.system_requirement);
		check_update = (Button) this.findViewById(R.id.check_update);
		title_back = (TextView) this.findViewById(R.id.setting_title);
		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VersionInfoActivity.this.finish();				
			}
			
		});
		// set value
		software_type.setText(R.string.free);
		software_classification.setText(R.string.convenience_of_life);
		software_version.setText(ClientPropertyUtil.getVersionName(this));
		
		String time = "2013-03-19";
		AssetManager am = getAssets();  
		InputStream is;
		try {
			is = am.open("build_time.txt");
			if(is != null)
			{
				DataInputStream dIs = new DataInputStream(is);
				//FileInputStream fIs;
				time = dIs.readLine();
				dIs.close();
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		
		update_time.setText(time);
		system_requirement.setText(R.string.need_version);
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}
	
	
}
