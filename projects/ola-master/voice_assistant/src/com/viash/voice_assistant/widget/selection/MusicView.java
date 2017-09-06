package com.viash.voice_assistant.widget.selection;

import java.util.List;

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
import com.viash.voicelib.utils.LogOutput;

/**
 * 选择音乐
 * @author Harlan Song
 * @createDate 2013-3-16
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class MusicView extends RelativeLayout {
	private String TAG = "MusicView";
	private List<String> musics;
	protected boolean operationEnable;
	protected Handler mHandler;
	private LinearLayout layout_content;
	public MusicView(Context context,List<String> musics,boolean operationEnable,Handler mHandler) {
		super(context);
		this.musics =musics;
		this.operationEnable = operationEnable;
		this.mHandler = mHandler;
		LayoutInflater.from(getContext()).inflate(R.layout.selection_music,this,true);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		if(musics !=null && musics.size() > 0 ){
			setData();
		}
	}
	private void setData(){
		if(musics.size() > 3){
			for (int i = 0; i < 3; i++) {
				addItemView(i);
			}
			addMoreView();
		}else{
			for (int i = 0; i < musics.size(); i++) {
				addItemView(i);
			}
		}
	}
	private void addMoreView(){
		View moreView = LayoutInflater.from(getContext()).inflate(R.layout.selection_music_more, null);
		if(operationEnable){
			moreView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					layout_content.removeAllViews();
					for (int i = 0; i < musics.size(); i++) {
						addItemView(i);
					}
				}
			});
		}
		layout_content.addView(moreView);
	}
	
	
	private void addItemView(int position){
		View musicLine=LayoutInflater.from(getContext()).inflate(R.layout.selection_music_item, null);
		TextView tv_index=(TextView) musicLine.findViewById(R.id.tv_index);
		TextView tv_name=(TextView) musicLine.findViewById(R.id.tv_name);
		TextView tv_author=(TextView) musicLine.findViewById(R.id.tv_author);
		ImageView imgv_recource_status=(ImageView) musicLine.findViewById(R.id.imgv_recource_status);
		String musicContent=musics.get(position);
		if(musicContent!=null&&musicContent.indexOf("$$")>0){
			String[] musicArray=musicContent.split("\\Q$$\\E");
			if(musicArray != null && musicArray.length > 0){
				tv_index.setText(String.valueOf(position + 1));
				tv_name.setText(musicArray[0]);
				String des ="";
				if(musicArray.length >1)
					des += musicArray[1];
				if(musicArray.length >2)
					des +="·" + musicArray[2];
				tv_author.setText(des);
				if(musicArray[3]!=null&&musicArray[3].length()>4){
					if("http".equals(musicArray[3].substring(0, 4))){	
						imgv_recource_status.setImageResource(R.drawable.music_type_cloud);
					}else{
						imgv_recource_status.setImageResource(R.drawable.music_type_local);
					}
				}else{
					LogOutput.e(TAG,"音乐路径为空");
				}
			}
			
		}else{
			LogOutput.e(TAG,"音乐内容为空或者内容格式错误！");
		}
		
		if(operationEnable){
			musicLine.setTag(position);
			musicLine.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int position=(Integer) v.getTag();
					position=1<<position;
					Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
					mHandler.sendMessage(msg);
				}
			});
		}
		layout_content.addView(musicLine, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		if(position < musics.size()-1){
			ImageView viewbg = new ImageView(getContext());
			viewbg.setImageResource(R.drawable.bg_line);
			viewbg.setScaleType(ScaleType.FIT_XY);
			layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		}
	}
}
