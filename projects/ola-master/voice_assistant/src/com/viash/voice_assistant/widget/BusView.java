package com.viash.voice_assistant.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.util.DensityUtil;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.BusJsonData;
import com.viash.voicelib.data.PreFormatData.BusJsonData.BusData;

/**
 * 公交线路
 * @author Leo
 * @createDate 2013-12-17
 */
@SuppressLint("ViewConstructor")
public class BusView extends SelectionBaseView {
	private List<BusData> busDatas;
	private static int childNumber = 0;
	private BusData busData;
	private ArrayList<View> lView = null;
	private static int highlight = 0;
	private static View highlightView = null;
	public BusView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);		
	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_bus;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.bus);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((BusJsonData)mData).busDatas.size();
		mViewData.mHighLight = 0;
		
	}
   
	@Override
	protected void initDataView(){
		super.initDataView();
		busDatas = ((BusJsonData)mData).busDatas;
		setListContentavaible(false);//Content view is not a list view.
		setDataView();
	}
	
	private void setDataView()
	{
		lView = new ArrayList<View>();
		if(!isFullScreen)
		{
			int count = 0;
			if(mViewData.mTotleItemNumber <= mViewData.mMinItemNumber)
			    count = mViewData.mTotleItemNumber;
			else
				count = mViewData.mMinItemNumber;
			for(int i= 0; i<count; i++)
			{
				mNormalContent.addView(getView(i));
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				mNormalContent.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		else
		{
			ScrollView sView = new ScrollView(mContext);
			LayoutParams param= new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
			LinearLayout layout_content = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_bus, null).findViewById(R.id.layout_content);
			sView.setLayoutParams(param);
			for(int i= 0; i< mViewData.mTotleItemNumber; i++)
			{
				layout_content.addView(getView(i));
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
			sView.addView(layout_content);
			mNormalContent.addView(sView);
		}
	}
	
	private View getView(int position) {
		busData = busDatas.get(position);
		View convertView = null;
		ViewHolder holder;
		if(lView != null)
		  if(lView.size() >= position+1)
		  {
		    convertView = lView.get(position);
		  }
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bus_item, null);
			holder = new ViewHolder();
			holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_station1 = (TextView) convertView.findViewById(R.id.tv_station1);
			holder.tv_station2 = (TextView) convertView.findViewById(R.id.tv_station2);
			holder.img_more = (ImageView) convertView.findViewById(R.id.img_more);
			holder.layout_child = (LinearLayout) convertView.findViewById(R.id.layout_child);
			convertView.setTag(holder);
			lView.add(convertView);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tv_index.setText(Integer.toString(position+1));
		holder.tv_title.setText(busData.stop_name);
		holder.img_more.setBackgroundResource(R.drawable.icon_bus_enter);
		if(busData.bus_name.length > 1)
		{
			holder.tv_station1.setText(busData.bus_name[0]+"  "+busData.bus_start[0]+" - "+busData.bus_stop[0]);
			holder.tv_station2.setText(busData.bus_name[1]+" "+busData.bus_start[1]+" "+busData.bus_stop[1]);
			for(int i=2; i<busData.bus_name.length; i++)
			{
				TextView textView = new TextView(mContext);
				textView.setText(busData.bus_name[i]+"  "+busData.bus_start[i]+" - "+busData.bus_stop[i]);
				int size = DensityUtil.px2dip(mContext, getResources().getDimension(R.dimen.content_second_text_height));
				textView.setTextSize(size);
				textView.setTextColor(getResources().getColor(R.color.list_item_text));
				LinearLayout.LayoutParams param= new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
				if(i != 2)
				    param.topMargin = 15-1;
				if(i == busData.bus_name.length -1)
					param.bottomMargin = 15;
				textView.setMaxLines(1);
				textView.setEllipsize(TruncateAt.END);
				textView.setLayoutParams(param);				
				holder.layout_child.addView(textView);								
			}
			
		}
		else
		{
			holder.tv_station1.setText(busData.bus_name[0]+"  "+busData.bus_start[0]+" - "+busData.bus_stop[0]);
			holder.tv_station2.setText(null);
			holder.tv_station2.setVisibility(View.INVISIBLE);
		}
		//holder.layout_child.setVisibility(View.GONE);
		holder.childNumber = busData.bus_name.length;
		if(holder.childNumber <= 2)
			holder.img_more.setVisibility(View.GONE);
		if(operationEnable)
		{			
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ViewHolder holderTemp = (ViewHolder) v.getTag();
					childNumber = holderTemp.childNumber;
					if(childNumber > 2)
					{						
						if(holderTemp.layout_child.getVisibility() == View.GONE)
						{	
							holderTemp.layout_child.setVisibility(View.VISIBLE);
							holderTemp.img_more.setBackgroundResource(R.drawable.icon_bus_back);
						}
						else
						{
							holderTemp.layout_child.setVisibility(View.GONE);
							holderTemp.img_more.setBackgroundResource(R.drawable.icon_bus_enter);
						}
					}
					int currentSelect = Integer.parseInt(holderTemp.tv_index.getText().toString()) - 1;
					if(highlight != currentSelect)
					{	
						  if(highlightView != null)
							  highlightView.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
						  v.setBackgroundResource(R.drawable.background_press);
						  highlight = currentSelect;
						  highlightView = v;
					}
					else
					{
						v.setBackgroundResource(R.drawable.background_press);
						highlightView = v;
					}
					
				}					
			});
		}
		return convertView;
	}

	private class ViewHolder
	{
		TextView tv_index;
		TextView tv_title;
		TextView tv_station1;
		TextView tv_station2;
		ImageView img_more;
		LinearLayout layout_child;
		int childNumber;
	}
	

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}		
}
	
	
	
		

