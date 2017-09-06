package com.viash.voice_assistant.component;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.FeedBackActivity;

public class SendDataIndicationDialog extends Dialog{

	private Context mContext;
	private String  mContent;
	private Handler mHandler;
	private AlertDialog.Builder bulider;
	public static final String SEND_SUCCESS = "提交成功";
	public static final String SEND_FAILED = "提交失败";
	
	public SendDataIndicationDialog(Context context,String content,Handler handler) {
		super(context);
		mContext = context;
		mContent = content;
		mHandler = handler;
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		//this.setTitle(null);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        bulider = new AlertDialog.Builder(mContext);
        bulider.setTitle(mContent);
        bulider.setIcon(mContext.getResources().getDrawable(R.drawable.icons_remaind));
        if(mContent.equals(SEND_SUCCESS))
        	bulider.setMessage("感谢您反馈的宝贵信息");
 		else if(mContent.equals(SEND_FAILED))
 			bulider.setMessage("请麻烦您重新提交");
		bulider.setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if(mContent.equals(SEND_SUCCESS))
				   closeDialog();
			}	
		});
		
		/*View view = LayoutInflater.from(mContext).inflate(R.layout.layout_send_data_indication, null);
		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		TextView tv_content = (TextView) view.findViewById(R.id.tv_content);		
		tv_title.setText(mContent);		
		bulider.setView(view);*/
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);			
	}
	
	public void showDialog()
	{
		bulider.show();
	}
	public void closeDialog()
	{			
		SendDataIndicationDialog.this.cancel();
		if(mContent.equals(SEND_SUCCESS))
		{
			FeedBackActivity.instance.finish();
		}
	}
}
