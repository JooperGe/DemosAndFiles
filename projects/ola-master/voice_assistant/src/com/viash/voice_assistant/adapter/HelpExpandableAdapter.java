package com.viash.voice_assistant.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.HelpStatisticsUtil;

public class HelpExpandableAdapter extends BaseExpandableListAdapter {
	private ArrayList<HashMap<String, Object>> groupArray = new ArrayList<HashMap<String, Object>>();
	private ArrayList<ArrayList<HashMap<String, Object>>> childArray = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private Context mContext;
    private Handler mHandler;
	public HelpExpandableAdapter(Context context,Handler handler) {
		mContext = context;
		mHandler = handler;
		initData();
	}

	private void initData() {
		// child
		childArray.add(addChild(R.array.help_child_poi));
		childArray.add(addChild(R.array.help_child_music));
		childArray.add(addChild(R.array.help_child_weather));
		childArray.add(addChild(R.array.help_child_wiki));
		childArray.add(addChild(R.array.help_child_video));
		childArray.add(addChild(R.array.help_child_phone));
		childArray.add(addChild(R.array.help_child_sms));
		childArray.add(addChild(R.array.help_child_contact));
		childArray.add(addChild(R.array.help_child_open));
		childArray.add(addChild(R.array.help_child_calendar));
		childArray.add(addChild(R.array.help_child_tvintr));
		childArray.add(addChild(R.array.help_child_news));
		childArray.add(addChild(R.array.help_child_poem));
		childArray.add(addChild(R.array.help_child_joke));
		childArray.add(addChild(R.array.help_child_funny));
		childArray.add(addChild(R.array.help_child_setting));
		childArray.add(addChild(R.array.help_child_clock));
		childArray.add(addChild(R.array.help_child_route));
		childArray.add(addChild(R.array.help_child_remind));
		childArray.add(addChild(R.array.help_child_note));
		childArray.add(addChild(R.array.help_child_stock));
		childArray.add(addChild(R.array.help_child_booking_hotel));
		childArray.add(addChild(R.array.help_child_booking_train));
		childArray.add(addChild(R.array.help_child_booking_air));
		childArray.add(addChild(R.array.help_child_buy));
		childArray.add(addChild(R.array.help_child_compute));
		childArray.add(addChild(R.array.help_child_unit));
		childArray.add(addChild(R.array.help_child_exchangerate));
		childArray.add(addChild(R.array.help_child_24));
		childArray.add(addChild(R.array.help_child_businfo));
		childArray.add(addChild(R.array.help_child_search));
		//childArray.add(addChild(R.array.help_child_weibo));
		
		// parent
		String[] helpTitle = mContext.getResources().getStringArray(R.array.helptitle);
        String[] helpDescription = mContext.getResources().getStringArray(R.array.helpdescription);
        Integer[] helpIcon = {
        		R.drawable.icons_poi,
        		R.drawable.icons_music,
        		R.drawable.help_icon_weather,
        		R.drawable.icons_baike,
        		R.drawable.icons_video,
        		R.drawable.help_icon_phone,
        		R.drawable.icons_message,        		
        		R.drawable.icons_contact,
        		R.drawable.help_icon_open,
        		R.drawable.icons_calendar,
        		R.drawable.help_icon_tv,
        		R.drawable.icons_news,
        		R.drawable.icons_poetry,
        		R.drawable.help_icon_funny,
        		R.drawable.help_icon_funny,
        		R.drawable.help_icon_setting,
        		R.drawable.icons_alarm,
        		R.drawable.icons_navigation,
        		R.drawable.help_icon_remind,
        		R.drawable.help_icon_note,
        		R.drawable.icons_stocks,
        		R.drawable.icons_hotel,
        		R.drawable.help_icon_booking_train,
        		R.drawable.icons_plantickets,
        		R.drawable.help_icon_buy,
        		R.drawable.help_icon_compute,
        		R.drawable.icons_money,
        		R.drawable.help_icon_unit,
        		R.drawable.help_icon_24,
        		R.drawable.icons_bus,        		
        		R.drawable.help_icon_search,
        		//R.drawable.icons_weibo, 		
        };
        
        // start to add
        if(helpIcon.length >= helpTitle.length){
	        for(int i = 0 ; i < helpTitle.length ; i++){
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put("ItemImageIcon", helpIcon[i]);
	            map.put("ItemTitle", helpTitle[i]);
	            map.put("ItemDescription", helpDescription[i]);
	            groupArray.add(map);
	        }
        }
	}

	private ArrayList<HashMap<String, Object>> addChild(int resId) {
		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
		String[] child_contact = mContext.getResources().getStringArray(resId);
		for(int i = 0 ; i < child_contact.length ; i++){
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ChildItem", child_contact[i]);
			child.add(map);
		}
		return child;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childArray.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		convertView = (LinearLayout) LinearLayout.inflate(mContext, R.layout.layout_help_child, null);
		TextView help_child_item = (TextView) convertView.findViewById(R.id.help_child_item);

		String childItem = (String) childArray.get(groupPosition).get(childPosition).get("ChildItem");
		int color;
		/*if((Integer.parseInt(childItem.substring(0, 1)) % 2) == 1){
			color = Color.rgb(0x6e, 0x6e, 0x6e);
		}else{
			color = Color.rgb(0x33, 0xb5, 0xe5);
		}*/
		color = Color.rgb(0x33, 0xb5, 0xe5);
		help_child_item.setText(childItem.substring(2, childItem.length()));
		help_child_item.setTextColor(color);
		help_child_item.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {   
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT, ((TextView)v).getText().toString()));
			}			
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(groupPosition < childArray.size()){
			return childArray.get(groupPosition).size();
		}else{
			return 0;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupArray.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupArray.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		convertView = (RelativeLayout) RelativeLayout.inflate(mContext, R.layout.layout_help, null);
		ImageView help_icon = (ImageView) convertView.findViewById(R.id.help_icon);
		ImageView help_select_image = (ImageView) convertView.findViewById(R.id.help_select_image);
		TextView help_title = (TextView) convertView.findViewById(R.id.help_title);
		TextView help_description = (TextView) convertView.findViewById(R.id.help_description);

		help_icon.setImageResource((Integer) groupArray.get(groupPosition).get("ItemImageIcon"));
		if(isExpanded){
			help_select_image.setImageResource(R.drawable.help_expand);
		}else{
			help_select_image.setImageResource(R.drawable.help_nonexpand);
		}
		help_title.setText((String) groupArray.get(groupPosition).get("ItemTitle"));
		help_description.setText((String) groupArray.get(groupPosition).get("ItemDescription"));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
    
	@Override
	 public void onGroupExpanded(int groupPosition) {
		String[] nameList = mContext.getResources().getStringArray(R.array.help_expand_name_list);
		if(groupPosition >= nameList.length)
			return;
		if(HelpStatisticsUtil.jsonObj == null)
			HelpStatisticsUtil.initJsonObjectFromFile();
		HelpStatisticsUtil.putContentToJsonObject(nameList[groupPosition], 1,true);
    }
}
