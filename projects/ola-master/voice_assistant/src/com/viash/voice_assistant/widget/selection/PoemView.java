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
 * 选择诗歌
 * @author Harlan Song
 * @createDate 2013-3-20
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class PoemView extends SelectionBaseView {
	private List<String> mOptionData;
	public PoemView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
		
	
	/*private void addItemView(int position){
		String content=poems.get(position);
		if(content!=null&&content.indexOf("$$")>0){
			String[] array=content.split("\\Q$$\\E");
			View itemView = LayoutInflater.from(getContext()).inflate(R.layout.selection_poem_item, null);
			TextView tv_index = (TextView) itemView.findViewById(R.id.tv_index);
			TextView tv_title = (TextView) itemView.findViewById(R.id.tv_title);
			TextView tv_author = (TextView) itemView.findViewById(R.id.tv_author);
			layout_content.addView(itemView);
			if(position <poems.size()-1){
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
			tv_index.setText(String.valueOf(position + 1));
			if(array.length > 0 && array[0] != null)
				tv_title.setText(array[0]);
			if(array.length > 1 && array[1] != null)
				tv_author.setText(array[1]);
			if(operationEnable){
				itemView.setTag(position);
				itemView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int position=(Integer) v.getTag();
						position=1<<position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						mHandler.sendMessage(msg);
					}
				});
			}
		}
	
	}*/
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_poetry;
		mViewData.mPrimaryTitleText = "诗词";
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
			String content = mOptionData.get(position);
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.selection_poem_item, null);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			if(content!=null && content.indexOf("$$")>0)
			{
				String[] array=content.split("\\Q$$\\E");
				holder.tv_title.setText(array[0]);
				holder.tv_author.setText(array[1]);
			}
			if(operationEnable)
			{
				convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						int position = Integer.parseInt((String) ((ViewHolder)v.getTag()).tv_index.getText()) - 1;
						position = 1<<position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						mHandler.sendMessage(msg);
						if(isFullScreen)
							mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);	
					}					
				});
			}
			return convertView;
		}
		private class ViewHolder
		{
			TextView tv_index;
			TextView tv_title;
			TextView tv_author;
		}
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
