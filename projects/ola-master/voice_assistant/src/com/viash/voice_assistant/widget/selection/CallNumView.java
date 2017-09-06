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
import android.widget.ImageView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 选择电话号码
 * @author Leo
 * @createDate 2013-12-23
 * 
 */
@SuppressLint("ViewConstructor")
public class CallNumView extends SelectionBaseView {
	private List<String> mOptionData;
	public CallNumView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
			
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_contact;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.phone_book);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((OptionData) mCommunicationData).getOptions().size();
		mViewData.mHighLight = 0;		
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		mOptionData = ((OptionData) mCommunicationData).getOptions();
		setListContentavaible(true);//Content view is a list view.
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}		
	}
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (isFullScreen)
			{
				return mOptionData.size();
			}
			else
			{
				if (mOptionData.size() >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else {
					return mOptionData.size();
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
			String data = mOptionData.get(position);
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.selection_call_number_item, null);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.imgv_type = (ImageView) convertView.findViewById(R.id.imgv_type);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.imgv_call = (ImageView) convertView.findViewById(R.id.imgv_call);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			if(data!=null && data.indexOf("$$")>0)
			{
				String[] array=data.split("\\Q$$\\E");
				if(array != null && array.length > 0)
				{
					if(array.length>1)
						holder.tv_name.setText(array[1]);
					if("mobile".equalsIgnoreCase(array[0]))
						holder.imgv_type.setImageResource(R.drawable.icon_phone_type_mobile);
					if("home".equalsIgnoreCase(array[0]))
						holder.imgv_type.setImageResource(R.drawable.icon_phone_type_home);
					if("work".equalsIgnoreCase(array[0]))
						holder.imgv_type.setImageResource(R.drawable.icon_phone_type_work);
					if("other".equalsIgnoreCase(array[0]))
						holder.imgv_type.setImageResource(R.drawable.icon_phone_type_other);
				}
			}
			
			if(operationEnable)
			{
				convertView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						int position = Integer.parseInt((String) ((ViewHolder)v.getTag()).tv_index.getText())-1;
						position =1<<position;
						if(isFullScreen)
						   mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						mHandler.sendMessage(msg);
					}					
				});
			}
			return convertView;

		}
		
		private class ViewHolder{
			TextView tv_index;
			ImageView imgv_type;
			TextView tv_name;
			ImageView imgv_call;
		}
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}

}
