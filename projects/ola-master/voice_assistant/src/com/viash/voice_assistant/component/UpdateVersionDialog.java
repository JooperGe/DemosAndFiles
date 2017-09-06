package com.viash.voice_assistant.component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.AutoUpdate;
import com.viash.voice_assistant.handler.AutoUpdateHandler;

public class UpdateVersionDialog extends Dialog{

	private int mBuild_version;
	private String  mVersion;
	private String  mDescription;
	private String  mUpdate_url;
	private long mFile_size;
	private String mFile_name;
	private Button bt_left;
	private Button bt_right;
	private Context mContext;
	
	public UpdateVersionDialog(Context context,int build_version,String version,String url,String descripton,long file_size,String file_name) {
		super(context);
		mContext = context;
		mBuild_version = build_version;
		mVersion = version;
		mDescription = descripton;
		mUpdate_url = url;
		mFile_size = file_size;
		mFile_name = file_name;
		/*this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		this.setTitle(null);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);*/
				
		/*View view = LayoutInflater.from(mContext).inflate(R.layout.layout_update_version, null);
		TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
		TextView tv_discription = (TextView) view.findViewById(R.id.tv_discription);		
		tv_content.setText(mContent);
		tv_discription.setText(descripton);
		bulider = new AlertDialog.Builder(mContext);
		bulider.setView(view);
		showDialog();*/
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		this.setTitle(null);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_update_version);
		TextView tv_content = (TextView)findViewById(R.id.tv_content);
		TextView tv_discription = (TextView)findViewById(R.id.tv_discription);	
		bt_left = (Button) findViewById(R.id.bt_left);
		bt_right = (Button) findViewById(R.id.bt_right);
		tv_content.setText(mVersion+"版本更新内容:");
		tv_discription.setText(mDescription);
		
		bt_left.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				AutoUpdateHandler autoUpdateHandler = new AutoUpdateHandler(mContext, false);
				// UpdateNotification.init(this);
				AutoUpdate.init(mContext, autoUpdateHandler);
				AutoUpdate.startAutoUpdateFromDialog(mBuild_version, mUpdate_url,mFile_size,mFile_name);
				UpdateVersionDialog.this.dismiss();
			}			
		});
		
		bt_right.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				UpdateVersionDialog.this.dismiss();
			}		
		});
	}
		
}
