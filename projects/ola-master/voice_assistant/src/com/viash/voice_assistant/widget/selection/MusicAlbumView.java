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
 * 选择音乐
 * @author Leo
 * @createDate 2013-12-27
 * @
 */
@SuppressLint("ViewConstructor")
public class MusicAlbumView extends SelectionBaseView {
	private List<String> mOptionData;
	public MusicAlbumView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_music;
		mViewData.mPrimaryTitleText = "专辑";
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
            String musicContent = mOptionData.get(position);
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.selection_music_item, null);
				holder = new ViewHolder();
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			
			if(musicContent!=null && musicContent.indexOf("$$")>0){
				String[] array=musicContent.split("\\Q$$\\E");
				if(array !=null && array.length>0)
				{										
				    holder.tv_name.setText(array[0]);
					if(array.length > 1)
						holder.tv_author.setText(array[1]);
				}
			}
			if(operationEnable){
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = Integer.parseInt((String) ((ViewHolder) v.getTag()).tv_index.getText())-1; 
						position = 1<<position;
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
			TextView  tv_index;
			TextView  tv_name;
			TextView  tv_author;
		}		
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}

}
