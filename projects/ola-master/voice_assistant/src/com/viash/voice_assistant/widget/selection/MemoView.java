package com.viash.voice_assistant.widget.selection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.msg.MsgConst;

/**
 *选择备忘录
 * @author Harlan Song
 * @createDate 2013-3-16
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class MemoView extends RelativeLayout{
	private List<String> memos;
	protected boolean operationEnable;
	protected Handler mHandler;
	private LinearLayout layout_content;
	public MemoView(Context context,List<String> memos,boolean operationEnable,Handler mHandler) {
		super(context);
		this.memos =memos;
		this.operationEnable = operationEnable;
		this.mHandler = mHandler;
		LayoutInflater.from(getContext()).inflate(R.layout.selection_memo,this,true);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		if(memos !=null && memos.size() > 0 ){
			setData();
		}
	}
	private void setData(){
		if(memos.size() > 3){
			for (int i = 0; i < 3; i++) {
				addItemView(i);
			}
			addMoreView();
		}else{
			for (int i = 0; i < memos.size(); i++) {
				addItemView(i);
			}
		}
	}
	
	private void addMoreView(){
		View moreView = LayoutInflater.from(getContext()).inflate(R.layout.selection_bluetooth_more, null);
		moreView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				layout_content.removeAllViews();
				for (int i = 0; i < memos.size(); i++) {
					addItemView(i);
				}
			}
		});
		layout_content.addView(moreView);
	}
	
	private void addItemView(int position){
		String musicContent=memos.get(position);
		if(musicContent!=null&&musicContent.indexOf("$$")>0){
			String[] musicArray=musicContent.split("\\Q$$\\E");
			View memoView=LayoutInflater.from(getContext()).inflate(R.layout.selection_memo_item, null);
			TextView tv_time=(TextView) memoView.findViewById(R.id.tv_time);
			TextView tv_content=(TextView) memoView.findViewById(R.id.tv_content);
			TextView tv_index = (TextView) memoView.findViewById(R.id.tv_index);
			if(musicArray !=null && musicArray.length > 0){
				Date date=new Date(musicArray[0]);
				SimpleDateFormat sdf  = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
				sdf.applyPattern("yyyy/MM/dd HH:mm");
				String time_str=sdf.format(date); 
				tv_index.setText(String.valueOf(position + 1));
				tv_time.setText(time_str);
					if(musicArray.length > 1)
				tv_content.setText(musicArray[1]);
				if(operationEnable){
					memoView.setTag(position);
					memoView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							int position=(Integer) v.getTag();
							position=1<<position;
							Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
							mHandler.sendMessage(msg);
						}
					});
				}
				layout_content.addView(memoView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				if(position < memos.size()-1){
					ImageView viewbg = new ImageView(getContext());
					viewbg.setImageResource(R.drawable.bg_line);
					viewbg.setScaleType(ScaleType.FIT_XY);
					layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				}
			}
		}
	
	}
}
