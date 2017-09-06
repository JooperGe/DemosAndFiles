package com.viash.voice_assistant.widget;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.HelpData.HelpGuideData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.HelpStatisticsUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class HelpGuideDetailView extends LinearLayout {
	private Context mContext;
	private Handler mHandler;
	private HelpGuideData helpGuideData;
	private ListView layout_content_list;
	private ImageView imgv_title;
	public HelpGuideDetailView(Context context,Handler handler,HelpGuideData helpGuideData) {
		super(context);
		mContext = context;
		mHandler = handler;
		this.helpGuideData = helpGuideData;
		HelpStatisticsUtil.currentType = helpGuideData.type;
	}
    
	public View initView()
	{
		View view = LayoutInflater.from(mContext).inflate(R.layout.layout_guide_help_detail, null);
		imgv_title = (ImageView) view.findViewById(R.id.imgv_title);
		layout_content_list = (ListView) view.findViewById(R.id.layout_content_list);
		
		if(helpGuideData.url.equals("local"))
        {
			if(helpGuideData.type.equals("weather"))
				imgv_title.setImageResource(R.drawable.icn_weather);
			else if(helpGuideData.type.equals("music"))
				imgv_title.setImageResource(R.drawable.icn_music);
			else if(helpGuideData.type.equals("joke"))
				imgv_title.setImageResource(R.drawable.icn_joke);
			else if(helpGuideData.type.equals("poi"))
				imgv_title.setImageResource(R.drawable.icn_poi);
			else if(helpGuideData.type.equals("tv"))
				imgv_title.setImageResource(R.drawable.icn_tv);
			else if(helpGuideData.type.equals("more"))
				imgv_title.setImageResource(R.drawable.icn_more);        	  	
        }
		else
		{
		    String fileName = HelpGuideView.iconSDPath + helpGuideData.icon_name;
		    Bitmap bitmap = HelpGuideView.getLocalBitmap(fileName);
		    if(bitmap != null)
		      imgv_title.setImageBitmap(bitmap);
		}
		//imgv_title.setImageDrawable(getResources().getDrawable(R.drawable.icn_weather));
		layout_content_list.setVisibility(View.VISIBLE);	   	
    	layout_content_list.setAdapter(new ContentAdapter());
    	layout_content_list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HelpStatisticsUtil.touchIndex = position;
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT, helpGuideData.contentArray[position]));
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_CLOSE_HELP_GUIDE_DETAIL));
			}
    		
    	});
		return view;
	}
	
	 private class ContentAdapter extends BaseAdapter
	 {

		@Override
		public int getCount() {			
			return helpGuideData.contentArray.length;
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
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_guide_help_detail_item, null);
				holder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
				convertView.setTag(holder);
				/*holder.layout_content.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ViewHolder holder = (ViewHolder) v.getTag();
						HelpStatisticsUtil.touchIndex = holder.index;
						//holder.layout_content.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_help_content_press));
						//mHandler.sendMessage(mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT, holder.tv_description.getText()));
						//mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
					}					
				});*/
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_description.setText(helpGuideData.contentArray[position]);
			
			return convertView;
		}
		
		class ViewHolder{
			TextView   tv_description;
		}
		 
	 }
}
