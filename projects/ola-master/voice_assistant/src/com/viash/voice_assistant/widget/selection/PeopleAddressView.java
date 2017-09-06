package com.viash.voice_assistant.widget.selection;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContactUtil;


@SuppressLint("ViewConstructor")
public class PeopleAddressView extends SelectionBaseView {

	private JSONObject mJsonObject;
	private List<String> mOptionData;
	public PeopleAddressView(Context context,OptionData optionData,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,optionData,operationEnable,mHandler,showInMainScreen);
		reSetTitle();
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
	
	private class ContentAdapter extends BaseAdapter
	{

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
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_people_address_item,null);
				holder = new ViewHolder();
				holder.tv_index  = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
				//holder.imgv_goto = (ImageView) convertView.findViewById(R.id.imgv_goto);
				convertView.setTag(holder);
				
			}else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			String addressContent = mOptionData.get(position);
			if(addressContent!=null && addressContent.indexOf("$$")>0)
			{
				String[] array = addressContent.split("\\Q$$\\E");
				String str = array[0].trim();
				str = str.replaceAll("\n", "");
				holder.tv_address.setText(str);
			}
			if(operationEnable)
			{
			   convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					String index = (String) ((ViewHolder)v.getTag()).tv_index.getText();
					String str = "导航到第" +index+ "个";
					Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,str);
					mHandler.sendMessage(msg);
				}
			   });
			}
	        return convertView;
		}
		
		private class ViewHolder{
			TextView  tv_index;
			TextView  tv_address;
			//ImageView imgv_goto;
		}
	}
	@Override
	protected void initSelectionViewData() {
		
		mViewData.mPrimaryTitleImg = R.drawable.default_contact_img;
		mViewData.mPrimaryTitleText = null;
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
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	
	private void reSetTitle()
	{
		String contact_id = null;
		String contact_name = null;
		mJsonObject = ((OptionData)mCommunicationData).getJsonObject();
		if(mJsonObject != null)
		{
		   try {
			   contact_id = mJsonObject.getString("id");
			   contact_name = mJsonObject.getString("title");
		   } catch (JSONException e) {
			e.printStackTrace();
		   }
		  
		}
		byte[] photo=ContactUtil.getPhoto(getContext(), String.valueOf(contact_id));
		if(photo!=null && photo.length>0){
			Bitmap bitmapPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
			mIV_PrimaryTitle.setImageResource(0);
			mIV_PrimaryTitle.setImageBitmap(bitmapPhoto);
			mIV_PrimaryTitle.setVisibility(View.VISIBLE);
		}
		mTV_PrimaryTitle.setText(contact_name);
		mTV_PrimaryTitle.setVisibility(View.VISIBLE);
		mTV_Secondary_Title.setVisibility(View.GONE);
		
	}
}
