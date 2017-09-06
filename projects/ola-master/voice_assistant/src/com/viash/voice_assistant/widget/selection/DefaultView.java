package com.viash.voice_assistant.widget.selection;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 默认选择，只是第一个字段
 */
@SuppressLint("ViewConstructor")
public class DefaultView extends SelectionBaseView{
	private List<String> datas;	
	
	public DefaultView(Context context, OptionData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler,showInMainScreen);
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		setListContentavaible(true);//Content view is a list view.
		datas = ((OptionData) mCommunicationData).getOptions();
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_ask;
		mViewData.mPrimaryTitleText = "请选择";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((OptionData) mCommunicationData).getOptions().size();
		mViewData.mHighLight = 0;
		
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int count = datas.size();
			if (isFullScreen)
			{
				return count;
			}else{
				if (count >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else {
					return count;
				}
			} 
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			final int current_position = position;
			if(convertView == null)
			{
				convertView =LayoutInflater.from(getContext()).inflate(R.layout.selection_default_item, null);
				holder = new ViewHolder();
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_name=(TextView) convertView.findViewById(R.id.tv_name);
				//holder.position = position;
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			String defaultContent=datas.get(position);
			String show_str = "";
			if(defaultContent!=null){
				if(defaultContent.indexOf("$$")>0){
					String[] defaultArray=defaultContent.split("\\Q$$\\E");
					show_str=defaultArray[0];
				}else{
					show_str=defaultContent;
				}				
			}

			holder.tv_index.setText(String.valueOf(position + 1));
			holder.tv_name.setText(show_str);
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(operationEnable)
					{
						//ViewHolder holder = (ViewHolder) v.getTag();
						int position = current_position;
						position= 1<<position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						mHandler.sendMessage(msg);
						mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
					}
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				}
			});
			return convertView;
		}
		
		private class ViewHolder{
			TextView tv_index;
			TextView tv_name;
			//int position;
		}
	}
}
