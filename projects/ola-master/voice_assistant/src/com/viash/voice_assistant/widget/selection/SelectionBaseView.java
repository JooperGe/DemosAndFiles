package com.viash.voice_assistant.widget.selection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.ConfirmData.ContactData;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.JsonData;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public abstract class SelectionBaseView extends RelativeLayout {
	//private static final String TAG = "SelectionBaseView";
	//private static final int LIST_DATA_IS_READY  = 1;
	private static final int DATA_IS_READY  = 2;
	
	public static final int UNKNOW_DATA_TYPE = 0;
	public static final int PERFORMATED_DATA_TYPE = 1;
	public static final int OPTION_DATA_TYPE = 2;
	public static final int CONFIRM_DATA_TYPE = 3;
	
	protected boolean needConfirm = false;
	/**
	 * title View:
	 */
	protected ImageView mIV_PrimaryTitle;
	protected TextView mTV_PrimaryTitle;
	protected TextView mTV_Secondary_Title;
	protected ImageView mIV_Secondary_Img;
	
	protected LinearLayout m_SubTitle_layout;
	/**
	 * filter view;
	 */
	protected TextView mTv_filter;
	protected ImageView miv_filter_image_view;
	protected RelativeLayout layout_filter;
	protected RelativeLayout layout_filter_list;
	private List<TextView> filter_tvs = new ArrayList<TextView>();
	private List<ImageView> filter_ivs = new ArrayList<ImageView>();
	
	/**
	 * content view;
	 */
	protected RelativeLayout layout_Content;
	protected ListView mlsvContent; //content is a list view
	protected LinearLayout mNormalContent; //content is a customer view;
	protected RelativeLayout layout_selection_base;
	protected RelativeLayout layout_title_style1;
	protected RelativeLayout layout_title_style2;//another style of title
	/**
	 * common function View;
	 */
	protected LinearLayout layout_common_button;
	
	/**
	 * bottom view;
	 */
	protected RelativeLayout layout_bottom;
	
	/**
	 * Yes No Layout view;
	 */
	protected LinearLayout layout_yes_no_btns;
	protected Button btn_ok;
	protected Button btn_cancel;
	
	
	protected boolean operationEnable;
	protected Handler mHandler;
	protected Context mContext;
		
	protected SelectionViewData  mViewData = new SelectionViewData();
	
	protected boolean isFullScreen = false;
	protected int mIStartNumber = 0;
	protected JsonData mData; //for per-formated data
	protected BaseData mCommunicationData; // in List selection data. it is OptionData.
	protected int Datatype = UNKNOW_DATA_TYPE;
	
	private int mExpandType = 0; //0: don't support expand/un-expand; 1, support expand; 2 support un-expand; 3 suport both expand and un-expand;
	
	public int getDatatype() {
		return Datatype;
	}

	private ISelectionViewListeners listeners = null;
	private IYesNoListeners mYes_NoListener = null;
	private IDataLoadingListeners mDataLoadingListener = null;
	
	//protected boolean mbIsDataReady = false;
	//private BaseAdapter mListAdapter;	
	//private LinearLayout loadingLayout;

	
	private Thread mloadingDataThread = new Thread() {
		 
        @Override
        public void run() {
        	if (mDataLoadingListener != null) {
        		mDataLoadingListener.onLoadStart();
        	}
			//mbIsDataReady =true;
            handler.sendEmptyMessageDelayed(DATA_IS_READY, 50);
        }
    };

	public SelectionBaseView(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
	}
    
	public SelectionBaseView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context);
		this.Datatype = PERFORMATED_DATA_TYPE;
		this.operationEnable = operationEnable;
		this.mHandler = handler;
		this.isFullScreen = showInMainScreen;
		this.mCommunicationData = data;
		this.mData = data.getJsonData();
		this.mContext = context;
		this.needConfirm = data.isConfirmedData();
		if (data.getDescriptionData() != null) {
			mViewData.mFilterIndex = data.getDescriptionData().filter;
			mViewData.mFilters = data.getDescriptionData().filters;
			mViewData.mDefaultFilterClickString = data.getDescriptionData().default_input;
			if(mViewData.mFilters != null)
			{	
				if (mViewData.mFilterIndex < mViewData.mFilters.length) {
					mViewData.mFilterOrCommFunText = mViewData.mFilters[mViewData.mFilterIndex];
				}
			}
		}	
		layout_selection_base = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_selection_base,this,true);
		if(this.mData != null)
		  initViews();   
	}
	
	public SelectionBaseView(Context context, OptionData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context);

		layout_selection_base = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_selection_base,this,true);
		this.Datatype = OPTION_DATA_TYPE;
		this.operationEnable = operationEnable;
		this.mHandler = handler;
		this.isFullScreen = showInMainScreen;
		this.mCommunicationData = data;
		this.mContext = context;
		
		if (data.getDescriptionData() != null) {
			mViewData.mFilterIndex = data.getDescriptionData().filter;
			mViewData.mFilters = data.getDescriptionData().filters;
			mViewData.mDefaultFilterClickString = data.getDescriptionData().default_input;
			if (mViewData.mFilterIndex < mViewData.mFilters.length) {
				mViewData.mFilterOrCommFunText = mViewData.mFilters[mViewData.mFilterIndex];
			}
		}
		
		if (data.getOptions() != null) {
			mViewData.mTotleItemNumber = data.getOptions().size();
		}
		
		initViews();
	}
	
	public SelectionBaseView(Context context, ConfirmData data, boolean operationEnable, Handler handler) {
		super(context);
		this.Datatype = CONFIRM_DATA_TYPE;
		this.operationEnable = operationEnable;
		this.mHandler = handler;
		this.isFullScreen = false;
		this.mCommunicationData = data;
		this.mContext = context;
		this.needConfirm = true;
		layout_selection_base = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_selection_base,this,true);
		initViews();
	}
	
	private void initViews() {
		initSelectionViewData();
		
		layout_Content = (RelativeLayout) findViewById(R.id.layout_content_base_parent); //
		mlsvContent = (ListView) findViewById(R.id.layout_content_list_base);//for context use list view.		
		mNormalContent = (LinearLayout) findViewById(R.id.layout_content_normal_base);// for content use custom view.
	
		layout_title_style1 = (RelativeLayout)findViewById(R.id.layout_title_style1);
		layout_title_style2 = (RelativeLayout)findViewById(R.id.layout_title_style2);//for another style title
		layout_filter = (RelativeLayout) findViewById(R.id.layout_filter);
		layout_filter_list = (RelativeLayout) findViewById(R.id.layout_filter_list);
		
		m_SubTitle_layout = (LinearLayout)findViewById(R.id.layout_sub_title);//some view contains this sub title. and this sub title should be wrote by children View.
		
		layout_common_button = (LinearLayout) findViewById(R.id.common_layout);
		
		layout_bottom = (RelativeLayout) findViewById(R.id.layout_bottom);
		
		mTv_filter = (TextView) findViewById(R.id.tv_filter);
		
		mIV_PrimaryTitle = (ImageView)findViewById(R.id.imgv_source);
		mTV_PrimaryTitle = (TextView) findViewById(R.id.tv_source);
		mTV_Secondary_Title = (TextView) findViewById(R.id.tv_source_secondary);
		

		mIV_Secondary_Img = (ImageView) findViewById(R.id.iv_source_secondary);
		mIV_Secondary_Img.setVisibility(View.GONE);

		miv_filter_image_view = (ImageView) findViewById(R.id.iv_filter);
		
		layout_yes_no_btns = (LinearLayout)findViewById(R.id.layout_yes_no_btns);//for confirmdata view;
		btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_cancel = (Button)findViewById(R.id.btn_cancel);
		if (needConfirm) {
			layout_yes_no_btns.setVisibility(View.VISIBLE);
			
				btn_ok.setEnabled(true);
				btn_cancel.setEnabled(true);
				btn_ok.setOnClickListener(new OnClickListener() {
	
					@SuppressLint("NewApi") @Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if (operationEnable)
						{
							if (isFullScreen) {
								mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
							}
							if (mYes_NoListener != null) {
								mYes_NoListener.onYesButtonClickListener(v);
							}else {								
								Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, 1, 1);
								
								EditText contactName = (EditText) findViewById(R.id.contact_name);
								EditText contactNumber = (EditText) findViewById(R.id.contact_number);
								if(contactName != null && contactNumber != null){
									
									String newName = contactName.getText().toString();
									String newNumber = contactNumber.getText().toString();
									ContactData contactData = ((ConfirmData)mCommunicationData).getContactData();
									contactData.setmName(newName);
									contactData.setmNumber(newNumber);
									String[] confim = {newName,newNumber};
									msg.obj = confim;
								}
								mHandler.sendMessage(msg);
							}
						}
					}
					
				});
				btn_cancel.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if (operationEnable)
						{
							if (isFullScreen) {
								mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
							}
							if (mYes_NoListener != null) {
								mYes_NoListener.onNoButtonClickListener(v);
							} else {								
								Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, 0, 1);
								mHandler.sendMessage(msg);
							}
						}
					}
					
				});				
		}
		else {
			layout_yes_no_btns.setVisibility(View.GONE);
		}
		
		mlsvContent.setVisibility(View.GONE);
		mNormalContent.setVisibility(View.GONE);
		layout_common_button.setVisibility(View.GONE);
		layout_bottom.setVisibility(View.GONE);
		layout_filter.setVisibility(View.GONE);
		
		if (!isFullScreen) {
			mIStartNumber = getStartItem();//when in short view, if the content contains some highlight data. it should shows the highlight data in the short view.
		}
		initTitleViewData();// initialize title
		initFilterView();
		initContentFunView();
		
		initDataView();
		afterInitData();
	}
	
    protected void initTitle2()
    {
    	layout_title_style1.setVisibility(View.GONE);
    	layout_title_style2.setVisibility(View.VISIBLE);
		
    	mIV_PrimaryTitle = (ImageView)findViewById(R.id.imgv_source_title2);
		mTV_PrimaryTitle = (TextView) findViewById(R.id.tv_source_title2);
		mTV_Secondary_Title = (TextView) findViewById(R.id.tv_source_sub_title2);
		initTitleViewData();
    }
	
	private void initTitleViewData(){
		if (mViewData.mPrimaryTitleImg != 0) {
			mIV_PrimaryTitle.setVisibility(View.VISIBLE);
			mIV_PrimaryTitle.setImageResource(mViewData.mPrimaryTitleImg);
			
		}else {
			mIV_PrimaryTitle.setVisibility(View.GONE);
		}
		if (mViewData.mPrimaryTitleText != null) {
			mTV_PrimaryTitle.setVisibility(View.VISIBLE);
			mTV_PrimaryTitle.setText(mViewData.mPrimaryTitleText);
		}else {
			mTV_PrimaryTitle.setVisibility(View.GONE);
		}
		if (mViewData.mSecondaryTitleText != null) {
			mTV_Secondary_Title.setVisibility(View.VISIBLE);
			mTV_Secondary_Title.setText(mViewData.mSecondaryTitleText);
		}else {
			mTV_Secondary_Title.setVisibility(View.GONE);
			if (mViewData.mSecondaryTitleImg != 0) {
				mIV_Secondary_Img.setVisibility(View.VISIBLE);
				mIV_Secondary_Img.setImageResource(mViewData.mSecondaryTitleImg);
			}
		}
	}
	
	protected void setSubTitle(View v) {
		LinearLayout title_separater = (LinearLayout)findViewById(R.id.base_separate0);
		if (v != null) {
			title_separater.setVisibility(View.VISIBLE);
			m_SubTitle_layout.setVisibility(View.VISIBLE);
			m_SubTitle_layout.addView(v);
		}
		else {
			m_SubTitle_layout.setVisibility(View.GONE);
			title_separater.setVisibility(View.GONE);
		}
	}

	protected void initFilterView(){
		if(mViewData.mFilters != null)
		{	
			if (mViewData.mFilters.length == 1 && mViewData.mCommFunImg != 0) {
				layout_filter.setVisibility(View.VISIBLE);
				miv_filter_image_view.setImageResource(mViewData.mCommFunImg);
				if (mViewData.mFilterOrCommFunText != null){
					mTv_filter.setText(mViewData.mFilterOrCommFunText);
				}
				layout_filter.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if (listeners != null) {
							listeners.onFilterTitleClickListener(v);
						}
					}
				});
			}else if (mViewData.mFilters.length > 1) {
				layout_filter.setVisibility(View.VISIBLE);
				if (mViewData.mFilterOrCommFunText != null){
					mTv_filter.setText(mViewData.mFilterOrCommFunText);
				}
				layout_filter.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if (mViewData.mFilters.length == 1 && listeners != null) {
							listeners.onFilterTitleClickListener(v);
							return;
						}
						
						if (layout_filter_list.getVisibility() == View.GONE) {
							layout_filter_list.setVisibility(View.VISIBLE);
						}else {
							layout_filter_list.setVisibility(View.GONE);
						}
					}
					
				});
				
				if (mViewData.mFilterOrCommFunText != null){
					mTv_filter.setText(mViewData.mFilterOrCommFunText);
				}
				
				for (int i = 0; i < mViewData.mFilters.length; i++) {
					addFilter(i);
				}
			}
			else {
				layout_filter.setVisibility(View.GONE);
			}
		}
		else {
			if (mViewData.mCommFunImg != 0) {
				layout_filter.setVisibility(View.VISIBLE);
				miv_filter_image_view.setImageResource(mViewData.mCommFunImg);
				if (mViewData.mFilterOrCommFunText != null){
					mTv_filter.setText(mViewData.mFilterOrCommFunText);
				}
				layout_filter.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if (listeners != null) {
							if (isFullScreen) 
							   mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
							listeners.onFilterTitleClickListener(v);
						}
					}
				});
				
			}else if (mViewData.mFilterOrCommFunText != null){
				layout_filter.setVisibility(View.VISIBLE);
				miv_filter_image_view.setVisibility(View.GONE);
				mTv_filter.setText(mViewData.mFilterOrCommFunText);
			}
		}
	}
	
	private void initContentFunView() {
		if (mViewData.mContentFunImg == 0) {
			layout_common_button.setVisibility(View.GONE);
			return;
		}
		layout_common_button.setVisibility(View.VISIBLE);
		
		//ImageView iv_CommonFunbutton = (ImageView)findViewById(R.id.iv_common);
		layout_common_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				if (listeners != null ){
					listeners.onContentViewClickListener(v);	
				}
			}
			
		});	
	}
	
	protected void initDataView(){
		LinearLayout bottom_separater = (LinearLayout)findViewById(R.id.base_separate2);
		if(mViewData.mTotleItemNumber > mViewData.mMinItemNumber && !isFullScreen){
			bottom_separater.setVisibility(View.VISIBLE);
			addMoreView();
		}else if (mViewData.mTotleItemNumber <= mViewData.mMinItemNumber){			
			bottom_separater.setVisibility(View.GONE);
		}else {
			bottom_separater.setVisibility(View.VISIBLE);
			addMoreViewBack();
		}
	}
	
	protected void afterInitData() {
		if (!operationEnable) {
			return;
		}
		if (mExpandType == 0 || layout_bottom.getVisibility() != View.VISIBLE) {
			return;
		}
		else {
			JSONObject obj = new JSONObject();
			JSONArray arrayCmd = new JSONArray();
			switch (mExpandType) {
			case 1:
				arrayCmd.put("Expand");				
				break;
			case 2:
				arrayCmd.put("Unexpand");
				break;
			case 3:
				arrayCmd.put("Expand");
				arrayCmd.put("Unexpand");
				break;
			default:
				break;
			}
			try {
				obj.put("cmdlist", arrayCmd);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REPORT_UI_INFO);
			msg.obj= obj.toString();
			mHandler.sendMessage(msg);
		}
	}
	
	private void addFilter(int i) {
		LinearLayout layout_filter_list_top = (LinearLayout) findViewById(R.id.layout_filter_list_top);
		View viewLine=LayoutInflater.from(getContext()).inflate(R.layout.layout_selection_base_filter_item, null);
		LinearLayout filter_item_layout = (LinearLayout)viewLine.findViewById(R.id.filter_item_layout);
		ImageView iv_filter_item = (ImageView) viewLine.findViewById(R.id.iv_filter_item);
		TextView tv_filter_item = (TextView) viewLine.findViewById(R.id.tv_filter_item);
		tv_filter_item.setText(mViewData.mFilters[i]);

		filter_item_layout.setTag(i);
		filter_tvs.add(tv_filter_item);
		filter_ivs.add(iv_filter_item);
		
		if ( mViewData.mFilters[i].equals(mViewData.mFilterOrCommFunText)) {
			iv_filter_item.setImageResource(R.drawable.check_on);
			tv_filter_item.setTextColor(getResources().getColor(R.color.text_title_color));
		}else {
			tv_filter_item.setTextColor(getResources().getColor(R.color.text_title_color));
			iv_filter_item.setImageResource(0);
		}
		
		filter_item_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				layout_filter_list.setVisibility(View.GONE);
				mViewData.mFilterIndex = (Integer) v.getTag();
				String sendText = null;
				if (mViewData.mDefaultFilterClickString != null) {
					sendText = mViewData.mDefaultFilterClickString.replace("%s", mViewData.mFilters[mViewData.mFilterIndex]);
				}
				else  {
					sendText = mViewData.mFilters[mViewData.mFilterIndex];
				}
				if (listeners != null ){
					listeners.onFilterItemClickListener(v);					
				} else {
					Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,sendText);
					mHandler.sendMessage(msg);
				}
				if(isFullScreen)
				   mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
				mTv_filter.setText(mViewData.mFilters[mViewData.mFilterIndex]);
				updatefilterViews(mViewData.mFilterIndex);
			}

		});

		layout_filter_list_top.addView(viewLine, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
	}
	
	private void addMoreView(){
		mExpandType = 1;
		layout_bottom.setVisibility(View.VISIBLE);
		//ImageView iv_bottom = (ImageView)findViewById(R.id.iv_bottom);
		/*TextView tv_bottom = (TextView)findViewById(R.id.tv_bottom);
		iv_bottom.setImageResource(R.drawable.get_more);
		tv_bottom.setText("查看更多");*/
		layout_bottom.setBackgroundResource(0);
		layout_bottom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN);
				msg.obj = mCommunicationData;
				
				if (operationEnable) {
					msg.arg1 = 1; 
				}
				else {
					msg.arg1 = 0;
				}
				msg.arg2 = Datatype;
				mHandler.sendMessage(msg);
				if (layout_filter_list.getVisibility() == View.VISIBLE) {
					layout_filter_list.setVisibility(View.GONE);
				}
			}
		});
	}
	
	
	
	private void addMoreViewBack(){
		mExpandType = 2;
		layout_bottom.setVisibility(View.VISIBLE);
		ImageView iv_bottom = (ImageView)findViewById(R.id.iv_bottom);
		//TextView tv_bottom = (TextView)findViewById(R.id.tv_bottom);
		iv_bottom.setImageResource(R.drawable.list_retracting_btn);
		//tv_bottom.setText("收起列表");*/
		layout_bottom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
				if (layout_filter_list.getVisibility() == View.VISIBLE) {
					layout_filter_list.setVisibility(View.GONE);
				}
			}
		});
	}
	
	protected void setListViewHeight(ListView listView){
		View listItem = listView.getAdapter().getView(0, null, listView);		
		listItem.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int itemHeight = listItem.getMeasuredHeight();
		int height = 0; 
		int dividerHeight = listView.getDividerHeight();
		if (mViewData.mTotleItemNumber >=  mViewData.mMinItemNumber){
			height = listView.getAdapter().getCount() * (itemHeight + dividerHeight);
		}
		else {
			height = listView.getAdapter().getCount() * (itemHeight + dividerHeight) ;
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = height;
		listView.setLayoutParams(params);
	}
	
	private int getStartItem(){
		int ret = 0;
		long i = mViewData.mHighLight;
		int itemNumber = mViewData.mTotleItemNumber; 
		if (i == 0) {
			return ret;
		}else {
			for (int index = 0; index < itemNumber; index++) {
				if (((i >> index) & 1) == 1) {
					ret = index;
					break;
				}
			}
		}
		int trueMinNumber = 0;
		if (mViewData.mMinItemNumber >= itemNumber) {
			trueMinNumber = itemNumber;
		} else {
			trueMinNumber = mViewData.mMinItemNumber;
		}
		if (ret + trueMinNumber >= itemNumber){
			ret = itemNumber - trueMinNumber;
		}
		return ret;
	}
	
	
		
	protected void setListContentavaible(boolean value){
		if(value) {			
			/*LayoutParams mLayoutParams = new LayoutParams(
		            LinearLayout.LayoutParams.WRAP_CONTENT,
		            LinearLayout.LayoutParams.WRAP_CONTENT);
			LayoutParams ffLayoutParams = new LayoutParams(
		            LinearLayout.LayoutParams.FILL_PARENT,
		            LinearLayout.LayoutParams.FILL_PARENT);
			
			LinearLayout layout = new LinearLayout(mContext);
	        layout.setOrientation(LinearLayout.HORIZONTAL);
	        ProgressBar progressBar = new ProgressBar(mContext);
	        layout.addView(progressBar, mLayoutParams);
	        TextView textView = new TextView(mContext);
	        textView.setText("加载�?..");
	        textView.setGravity(Gravity.CENTER_VERTICAL);
	        layout.addView(textView, ffLayoutParams);
	        layout.setGravity(Gravity.CENTER);
	        loadingLayout = new LinearLayout(mContext);
	        loadingLayout.addView(layout, mLayoutParams);
	        loadingLayout.setGravity(Gravity.CENTER);*/
	        
			mlsvContent.setVisibility(View.VISIBLE);
			//mlsvContent.addFooterView(loadingLayout);
			mNormalContent.setVisibility(View.GONE);
		}
		else {
			mlsvContent.setVisibility(View.GONE);
			mNormalContent.setVisibility(View.VISIBLE);
		}
	}

	private void updatefilterViews(int index) {
		for (int i = 0; i < mViewData.mFilters.length; i++) {
			if (index == i) {
				filter_tvs.get(i).setTextColor(getResources().getColor(R.color.text_title_color));
				filter_ivs.get(i).setImageResource(R.drawable.check_on);
			}else {
				filter_tvs.get(i).setTextColor(getResources().getColor(R.color.text_title_color));
				filter_ivs.get(i).setImageResource(0);
			}
		}
		
	}
	
	protected void setOnListeners(ISelectionViewListeners listeners) {
		this.listeners = listeners;
	}
	
	protected void setOnYesNoListeners(IYesNoListeners listeners) {
		this.mYes_NoListener = listeners;
	}
	
	protected void setDataLoadingListeners(IDataLoadingListeners listeners) {
		this.mDataLoadingListener = listeners;
	}
	
	protected abstract void initSelectionViewData();
	
	public abstract void handleServerCmd();
	
	public void handleServerCmd(String cmd, String param1, String param2) {
		if (cmd == null) {
			return;
		}
		if (cmd.equalsIgnoreCase("expand")) {
			Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN);
			msg.obj = mCommunicationData;
			if (operationEnable) {
				msg.arg1 = 1; 
			}
			else {
				msg.arg1 = 0;
			}
			msg.arg2 = Datatype;
			mHandler.sendMessage(msg);
			if (layout_filter_list.getVisibility() == View.VISIBLE) {
				layout_filter_list.setVisibility(View.GONE);
			}
		}else if (cmd.equalsIgnoreCase("unexpand")) {
			mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
		}
	}
	

	public interface ISelectionViewListeners {
		void onContentViewClickListener(View v);
		void onFilterItemClickListener(View v);
		void onFilterTitleClickListener(View v);
	}
	
	public interface IYesNoListeners {
		void onYesButtonClickListener(View v);
		void onNoButtonClickListener(View v);
	}
	
	public interface IDataLoadingListeners {
		void onLoadStart();
		void onLoadFinish();
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new  Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mDataLoadingListener != null) {
				mDataLoadingListener.onLoadFinish();
			}
			/*if (msg.what == LIST_DATA_IS_READY) {
				mlsvContent.removeFooterView(loadingLayout);
				setListViewHeight(mlsvContent);
				mListAdapter.notifyDataSetChanged();
				return;
			}*/
			
		}
	};
	
	protected void setAdapter(BaseAdapter adapter){
		mlsvContent.setAdapter(adapter);
//		this.mListAdapter = adapter;
		//mloadingDataThread.start(); //TODO
	}
	
	protected void loadingDataStart() {
		mloadingDataThread.start();
	}
	
}
