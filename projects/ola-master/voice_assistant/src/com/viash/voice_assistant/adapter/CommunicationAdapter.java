package com.viash.voice_assistant.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.viash.voice_assistant.component.CommunicationItemView;
import com.viash.voicelib.data.AppData;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.CommunicationData.NotifyData;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.QuestionData;
import com.viash.voicelib.data.SentenceData;
import com.viash.voicelib.data.HelpData;;

public class CommunicationAdapter extends BaseAdapter{
	
	protected Context mContext;
	protected Handler mHandler;
	protected List<BaseData> mLstData = new ArrayList<BaseData>();
	private int mNewDataNumber = 0;
	private int mOldDataNumber = 0;
	private CommunicationData mNewData;
	private BaseData mCurrentSelectionData = null;
	private static List<CommunicationItemView> mHandlerServerViewList = null;
	
	public CommunicationAdapter(Context context)
	{
		mContext = context;
		mHandlerServerViewList = new ArrayList<CommunicationItemView>();
	}
	
	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}
		
	public void addData(CommunicationData data)
	{
		//mLstData.add(data);
		mNewData = data;
		int newAddedNumber = 0;
		for (BaseData item : data.getLstData()) {
			item.setFrom(data.getFrom());

			if (item instanceof SentenceData) {
				newAddedNumber ++;
				mLstData.add(item);
			}
			
			if (item instanceof QuestionData) {
				newAddedNumber ++;
				mLstData.add(item);
			}

			if (item instanceof PreFormatData) {
				
				if (item.isSelectionData()) {
					if (mCurrentSelectionData != null && mCurrentSelectionData != item) {
						mLstData.remove(mCurrentSelectionData);
					}
					mCurrentSelectionData = item;
				}
				
				newAddedNumber ++;
				mLstData.add(item);
				mHandlerServerViewList.clear();
			}

			if (item instanceof OptionData) {
				if (item.isSelectionData()) {
					if (mCurrentSelectionData != null && mCurrentSelectionData != item) {
						mLstData.remove(mCurrentSelectionData);
					}
					mCurrentSelectionData = item;
				}
				
				newAddedNumber ++;
				mLstData.add(item);
			}

			if (item instanceof ConfirmData) {
				newAddedNumber ++;
				mLstData.add(item);
			}
			
			if (item instanceof NotifyData) {
				newAddedNumber ++;
				mLstData.add(item);
			}
			
			if (item instanceof HelpData) {
				/*for(BaseData data1 : mLstData)
				{
					if(data1 instanceof HelpData)
					{
						mLstData.remove(data1);
						newAddedNumber--;
						break;
					}
				}*/				
				newAddedNumber ++;
				if(mLstData.size() == 1)
				{
					if(mLstData.get(0) instanceof NotifyData)
						mLstData.add(0, item);
				}
				else
				{
				   mLstData.add(item);	
				}
			}
		}
		mOldDataNumber = mNewDataNumber;
		if (data.getLstData() != null) {
			mNewDataNumber = newAddedNumber;
		}else {
			mNewDataNumber = 0;
		}
		this.notifyDataSetChanged();
	}
	
	public void clearData()
	{
		mLstData.clear();
	}

	@Override
	public int getCount() {		
		return mLstData.size();
	}

	@Override
	public Object getItem(int position) {
		
		
		return mLstData.get(position);
	}

	@Override
	public long getItemId(int position) {
		//return getItem(position).hashCode();
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BaseData item = mLstData.get(position);
		if (convertView == null) {
			
			CommunicationItemView v = new CommunicationItemView(mContext);
			v.setmHandler(mHandler);
			v.setTag(item);
			if (position == getCount() -mNewDataNumber - 1) {
				v.setData(item, true);
				if (mHandlerServerViewList != null && item instanceof PreFormatData) {
					mHandlerServerViewList.add(v);
				}
			}
			else {
				v.setData(item, false);
			}
			convertView = v;
		}
		else {
			BaseData olditem = (BaseData) convertView.getTag();
			if (olditem != item) {
				CommunicationItemView v = new CommunicationItemView(mContext);
				v.setmHandler(mHandler);
				v.setTag(item);
				if (position > getCount() - mNewDataNumber - 1) {
					v.setData(item, true);
					if (mHandlerServerViewList != null && item instanceof PreFormatData) {						
						mHandlerServerViewList.add(v);
					}
				}
				else {
					v.setData(item, false);
				}
				convertView = v;
			}
			if (position > getCount() - mNewDataNumber - mOldDataNumber - 1 && position <= getCount() - mNewDataNumber - 1)  {
				BaseData itemtag = (BaseData) convertView.getTag();
				((CommunicationItemView)convertView).setData(itemtag, false);	
			}
		}
				
		return convertView;
		
		
		
		
		/*boolean needRefresh = false;
		boolean enabled = (position == mLstData.size() - 1);
		BaseData item = mLstData.get(position);
		
		com.viash.voice_assistant.CommunicationItemView v = (com.viash.voice_assistant.CommunicationItemView)convertView;
		
		if (v != null) {
			BaseData olditem = (BaseData) v.getTag();
			//if ( olditem == item)
			v = new CommunicationItemView(mContext);
			v.setmHandler(mHandler);
			needRefresh = true;
		}
		
		if (position == getCount() - 1) {
			v.setData(item, true);			
		}else if (position == getCount() - 2) {
			v.setData(item, false);
		}else {
			return v;
		}
		*/
		
		/*if(!needRefresh)
		{			
			CommunicationData itemOld = v.getData();
			if (item != null)
			{				
				if(item != itemOld) 
				{
					needRefresh = true;
				}
				else
				{
					needRefresh = item.ismModified();
					if(!needRefresh)
					{
						needRefresh = (v.ismEnabled() != enabled);
					}
				}
			}
		}
		
		if(needRefresh)
		{
			item.setmModified(false);
			v.setData(item, enabled);
		}*/
		
		//return v;
	}

	public void removeData(BaseData obj) {
		mLstData.remove(obj);
		for (BaseData item : mNewData.getLstData()) {
			if (item.equals(obj)) {
				mNewDataNumber --;
			}
		}
		if (mNewDataNumber < 0) {
			mNewDataNumber = 0;
		}
		this.notifyDataSetChanged();
	}
	
	public void removeData(CommunicationData obj) {
		if (obj == null){
			return;
		}
		for(BaseData data : obj.getLstData()) {
			removeData(data);
			if (data instanceof PreFormatData) {
				mHandlerServerViewList.clear();
			}
		}
		
		//this.notifyDataSetChanged();
	}

	public void handlerMsg(AppData.ServerCommand cmd) {
		int position = getCount() - mNewDataNumber - mOldDataNumber;
		while (position > 0 ) {
			CommunicationItemView item = null;
			if (getItem(position) instanceof PreFormatData) {
				item = (CommunicationItemView)getView(position, null, null);
			}
			if (item != null) {
				item.handlerMessage(cmd);
				break;
			}
			position--;
		}
				
		/*for (CommunicationItemView item : mHandlerServerViewList) {
			item.handlerMessage(cmd);
		}*/
	}
	
	public void handlerMsg(String cmd, String param1, String param2) {
		int position = getCount() - mNewDataNumber;
		while (position > 0 ) {
			CommunicationItemView item = null;
			if (getItem(position) instanceof PreFormatData) {
				item = (CommunicationItemView)getView(position, null, null);
			}
			if (getItem(position) instanceof OptionData) {
				item = (CommunicationItemView)getView(position, null, null);
			}
			if (item != null) {
				item.handlerMessage(cmd, param1, param2);
				break;
			}
			position--;
		}
				
		/*for (CommunicationItemView item : mHandlerServerViewList) {
			item.handlerMessage(cmd);
		}*/
	}
}
