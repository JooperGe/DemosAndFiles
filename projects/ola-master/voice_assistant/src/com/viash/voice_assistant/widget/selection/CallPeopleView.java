package com.viash.voice_assistant.widget.selection;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.viash.voicelib.utils.ContactUtil;

/**
 * 选择联系人
 * @author Leo
 * @createDate 2013-12-27
 * @
 */
@SuppressLint("ViewConstructor")
public class CallPeopleView extends SelectionBaseView {
	private List<String> mOptionData;
	public CallPeopleView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}			
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_contact;
		mViewData.mPrimaryTitleText = "联系方式";
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
			int count = mOptionData.size();
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
            String callContent = mOptionData.get(position);
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.selection_call_name_item, null);
				holder = new ViewHolder();
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.imgv_head = (ImageView) convertView.findViewById(R.id.imgv_head);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			
			if(callContent!=null && callContent.indexOf("$$")>0){
				String[] array=callContent.split("\\Q$$\\E");
				if(array !=null && array.length>0)
				{
					byte[] photo=ContactUtil.getPhoto(getContext(), array[0]);
					if(photo!=null && photo.length>0){
						Bitmap bitmapPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
						holder.imgv_head.setImageBitmap(bitmapPhoto);
					}
					if(array.length>1)
						holder.tv_name.setText(array[1]);
					if(array.length > 2)
						holder.tv_phone.setText(array[2]);
				}
			}

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					if (operationEnable) {
						int position = Integer.parseInt((String) ((ViewHolder) v.getTag()).tv_index.getText()) - 1;
						position = 1 << position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						mHandler.sendMessage(msg);
					}
				}
			});
			return convertView;
		}
		
		private class ViewHolder{
			TextView  tv_index;
			ImageView imgv_head;
			TextView  tv_name;
			TextView  tv_phone;
		}		
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	
}
