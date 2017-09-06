package com.viash.voice_assistant.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.ContactJsonData;
import com.viash.voicelib.data.PreFormatData.ContactJsonData.ContactData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContactUtil;

@SuppressLint("ViewConstructor")
public class ContactDetailView extends SelectionBaseView {
	private List<ContactData> mContactData;
//	private ImageView imagev_head;
//	private TextView tv_name;
//	private LinearLayout layoutPhone;
//	private RelativeLayout layout_left;
	private CountDownTimer countDownTimer;
	private View tempView;
	private boolean operationEnable;
	private Handler mHandler;
	private List<Map<String, Object>> nums;
	
	public ContactDetailView(Context context, PreFormatData data,boolean operationEnable,Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
		
		this.operationEnable = operationEnable;
		this.mHandler = handler;
		/*LayoutInflater.from(getContext()).inflate(
				R.layout.layout_contact_detail, this, true);*/
		//layoutPhone = (LinearLayout) findViewById(R.id.layout_content);
		
		reSetTitle();			
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		
		setListContentavaible(true);//Content view is a list view.
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
		mContactData = ((ContactJsonData)mData).mContactData;
		if (mContactData != null && mContactData.size() > 0) {
			init();
		}
		mViewData.mPrimaryTitleImg = R.drawable.default_contact_img;
		mViewData.mPrimaryTitleText = ((ContactJsonData)mData).mContactData.get(0).display_name;
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = nums.size();
		mViewData.mHighLight = 0;
		
	}
	
	private void reSetTitle()
	{
		ContactData contactData = mContactData.get(0);
		byte[] photo=ContactUtil.getPhoto(getContext(), String.valueOf(contactData.id));
		if(photo!=null && photo.length>0){
			Bitmap bitmapPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
			mIV_PrimaryTitle.setImageResource(0);
			mIV_PrimaryTitle.setImageBitmap(bitmapPhoto);
		}
		mTV_Secondary_Title.setVisibility(View.GONE);
		
	}
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (isFullScreen)
				return nums.size();
			else{
				if (nums.size() >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else {
					return nums.size();
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
			Map<String, Object> map = nums.get(position);
			final int current_position = position; 
			
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_contact_num_item,null);
				holder = new ViewHolder();
				holder.tv_index  = (TextView) convertView.findViewById(R.id.tv_index);
				holder.imgv_type = (ImageView) convertView.findViewById(R.id.imgv_type);
				holder.tv_num  = (TextView) convertView.findViewById(R.id.tv_num);
				holder.imgv_call = (ImageView) convertView.findViewById(R.id.imgv_call);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}
			holder.tv_index.setText(String.valueOf(position+1));
			holder.imgv_type.setImageResource(Integer.valueOf(map.get("type").toString()));
			holder.tv_num.setText(map.get("num").toString());
			holder.imgv_call.setImageResource(R.drawable.icons_call);
			if(operationEnable)
			{	
				//holder.index = position;
				convertView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {

						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(isFullScreen)
						{
							mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
							if (layout_filter_list.getVisibility() == View.VISIBLE) {
								layout_filter_list.setVisibility(View.GONE);
							}
						}
						v.setBackgroundColor(getResources().getColor(R.color.weather_bg1));
						tempView = v;
						countDownTimer = new CountDownTimer(200,200){

							@Override
							public void onTick(long millisUntilFinished) {
								
							} 
							@Override
							public void onFinish() {
								tempView.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_bg_bluetooth));
								int position = current_position + 1;
								String str = "拨打第" + position + "个";
								Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,str);
								mHandler.sendMessage(msg);
							}
							
						};
						countDownTimer.start();
					}
				});
			}
			return convertView;
		}
		
		private class ViewHolder{
			TextView  tv_index;
			ImageView imgv_type;
			TextView  tv_num;
			ImageView imgv_call;
			//int index;
		}
		
	}
	/*private void addTitleView()
	{
		View titleView =LayoutInflater.from(getContext()).inflate(R.layout.selection_call_people_address_title, null);
		ImageView imgv_head=(ImageView) titleView.findViewById(R.id.imgv_head);
		
		ContactData contactData = mContactData.get(0);
		byte[] photo=ContactUtil.getPhoto(getContext(), String.valueOf(contactData.id));
		if(photo!=null && photo.length>0){
			Bitmap bitmapPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
			imgv_head.setImageBitmap(bitmapPhoto);
		}
		TextView tv_name=(TextView) titleView.findViewById(R.id.tv_name);
		if (contactData.display_name != null&& contactData.display_name.length() > 0)
			tv_name.setText(contactData.display_name);
		layoutPhone.addView(titleView,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));	
	}*/
	
	private void init() {
		for (int i = 0; i < mContactData.size(); i++) {
			ContactData contactData = mContactData.get(i);

			String[] numHome = null, numWork = null, numMobile = null, numOther = null;
			numHome = contactData.home_phone;
			numWork = contactData.work_phone;
			numMobile = contactData.mobile_phone;
			numOther = contactData.other_phone;
			nums = new ArrayList<Map<String,Object>>();
			Map<String,Object> map;
			if (numMobile != null && numMobile.length > 0) {
				for (int j = 0; j < numMobile.length; j++) {
					map = new HashMap<String, Object>();
					map.put("type", R.drawable.icon_phone_type_mobile);
					map.put("num", numMobile[j]);
					nums.add(map);
				}
			}
			if (numWork != null && numWork.length > 0) {
				for (int j = 0; j < numWork.length; j++) {
					map = new HashMap<String, Object>();
					map.put("type", R.drawable.icon_phone_type_work);
					map.put("num", numWork[j]);
					nums.add(map);
				}
			}
			if (numHome != null && numHome.length > 0) {
				for (int j = 0; j < numHome.length; j++) {
					map = new HashMap<String, Object>();
					map.put("type", R.drawable.icon_phone_type_home);
					map.put("num", numHome[j]);
					nums.add(map);
				}
			}
			if (numOther != null && numOther.length > 0) {
				for (int j = 0; j < numOther.length; j++) {
					map = new HashMap<String, Object>();
					map.put("type", R.drawable.icon_phone_type_other);
					map.put("num", numOther[j]);
					nums.add(map);
				}
			}			
		}
	}


	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	
	
	/*private void loadNums(List<Map<String, Object>> nums){
		if(nums !=null && nums.size() > 0){
			for (int i = 0; i < nums.size(); i++) {
				View numView = LayoutInflater.from(getContext()).inflate(R.layout.layout_contact_num_item,null);
				TextView tv_index=(TextView) numView.findViewById(R.id.tv_index);
				TextView tv_num = (TextView) numView.findViewById(R.id.tv_num);
				ImageView imgv_type = (ImageView) numView.findViewById(R.id.imgv_type);
				Map<String, Object> map = nums.get(i);
				tv_index.setText(String.valueOf(i+1));
				imgv_type.setImageResource(Integer.valueOf(map.get("type").toString()));
				tv_num.setText(map.get("num").toString());
				numView.setTag(i);
				if(operationEnable)
				{	
					numView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							
							v.setBackgroundColor(getResources().getColor(R.color.weather_bg1));
							tempView = v;
							countDownTimer = new CountDownTimer(200,200){
	
								@Override
								public void onTick(long millisUntilFinished) {
									
								} 
								@Override
								public void onFinish() {
									tempView.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_bg_bluetooth));
									int position = (Integer)tempView.getTag() +1;
									String str = "拨打第" + position + "个";
									Message msg = mHandler.obtainMessage(MsgConst.MSG_OUTGOING_CALL_SEND_WITH_TEXT,str);
									mHandler.sendMessage(msg);
								}
								
							};
							countDownTimer.start();
						}
					});
				}
				layoutPhone.addView(numView);
				if(i<nums.size()-1){
					ImageView viewbg = new ImageView(getContext());
					viewbg.setImageResource(R.drawable.bg_line);
					viewbg.setScaleType(ScaleType.FIT_XY);
					layoutPhone.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				}
			}			
		}
	}*/
	
}
