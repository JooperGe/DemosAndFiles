package com.viash.voice_assistant.component;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.GlobalData;

import com.viash.voice_assistant.widget.HelpGuideView;
import com.viash.voice_assistant.widget.MusicAlbumView;
import com.viash.voice_assistant.widget.MusicArtistAlbumView;
import com.viash.voice_assistant.widget.PoemView;
import com.viash.voice_assistant.widget.ShopingView;
import com.viash.voice_assistant.widget.TrainView;
import com.viash.voice_assistant.widget.WidgetViewFactory;
import com.viash.voice_assistant.widget.confirmation.ConfirmCancelView;
import com.viash.voice_assistant.widget.confirmation.ContactConfimView;
import com.viash.voice_assistant.widget.confirmation.SendSMSView;
import com.viash.voice_assistant.widget.listitemview.NotificationDataView;

import com.viash.voice_assistant.widget.selection.MusicView;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.AppData;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData.NotifyData;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.data.HelpData;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.ArtistAlbumJsonData;

import com.viash.voicelib.data.PreFormatData.MusicAlbumJsonData;
import com.viash.voicelib.data.PreFormatData.PoemJsonData;
import com.viash.voicelib.data.PreFormatData.ShoppingItemJsonData;
import com.viash.voicelib.data.PreFormatData.TrainJsonData;
import com.viash.voicelib.data.QuestionData;
import com.viash.voicelib.data.SentenceData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;
import com.viash.voicelib.widget.NetImageTextView;

public class CommunicationItemView extends RelativeLayout {
	private static final String TAG = "CommunicationItemView";
	
	private TextView mRightView = null;
	private Handler mHandler;
	private ConfirmCancelView confrimLayout;
	private BaseData mData;
	private SendSMSView mSendSMSView;
	private final String VIEW_SINGLE = "single";
	//private final String VIEW_OPTION = "option";
	private final String VIEW_FORM = "form";
	private Boolean mEnabled = null;
	private boolean textCreate = false;
	private NetImageTextView mTxtView = null;
	private TextView tv_more = null;
	private View view_line = null;
	private RelativeLayout.LayoutParams layoutContentParam;
	private RelativeLayout mChatLeftLayout = null;
	

	public CommunicationItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initControls();
	}

	public CommunicationItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initControls();
	}

	public CommunicationItemView(Context context) {
		super(context);
		initControls();
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	private void initControls() {
		this.setBackgroundColor(0);
		layoutContentParam = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutContentParam.leftMargin = (int) getResources().getDimension(
				R.dimen.main_item_left_form_margin_left);
		layoutContentParam.rightMargin = (int) getResources().getDimension(
				R.dimen.main_item_left_form_margin_right);
		layoutContentParam.topMargin = (int) getResources().getDimension(
				R.dimen.main_item_left_form_margin_top);
		layoutContentParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	}

	/**
	 * Add Selection View
	 * 
	 * @param optionData
	 * @param lstView
	 * @param operationEnable
	 */
	protected void addOptionViews(OptionData optionData, 
			boolean operationEnable) {
		View view = null;
		final List<String> lstOption = optionData.getOptions();
		if (lstOption == null ||lstOption.size() == 0) {
			return;
		}
		if (lstOption != null && lstOption.size() > 0) {
			int type = optionData.getOptionId();
			switch (type) {
			case OptionData.OPTION_MUSIC_NAME:
				MusicView musicView = new MusicView(getContext(), lstOption,
						operationEnable, mHandler);
				musicView.setTag(VIEW_SINGLE);
				view = musicView;
				break;			
			case OptionData.OPTION_MEMO_CONTENT:
				com.viash.voice_assistant.widget.selection.MemoView memoView = new com.viash.voice_assistant.widget.selection.MemoView(
						getContext(), lstOption, operationEnable, mHandler);
				memoView.setTag(VIEW_SINGLE);
				view = memoView;
				break;
			case OptionData.OPTION_CALENDAR_ALARM:
				com.viash.voice_assistant.widget.selection.AlarmView alarmView = new com.viash.voice_assistant.widget.selection.AlarmView(
						getContext(), lstOption, operationEnable, mHandler);
				alarmView.setTag(VIEW_SINGLE);
				//lstView.add(alarmView);
				view = alarmView;
				break;
			case OptionData.OPTION_SHOPING:
				com.viash.voice_assistant.widget.selection.ShopingView shopingView = new com.viash.voice_assistant.widget.selection.ShopingView(
						getContext(), lstOption, operationEnable, mHandler);
				shopingView.setTag(VIEW_SINGLE);
				view = shopingView;
				break;

			}
		}
		if (view == null) {
			view = WidgetViewFactory.getWidgetView(getContext(), optionData, operationEnable, mHandler, false);
		}
		if (view != null) {
			view.setLayoutParams(layoutContentParam);
			removeAllViews();
			addView(view);
		}
	}

	protected void addPreformatDataViews(PreFormatData preformatData,
			boolean operationEnable) {
		View view = null;
		switch (preformatData.getmDataType()) {
		case PreFormatData.JSON_POEM:
			PoemJsonData poemJsonData = (PoemJsonData) preformatData
					.getJsonData();
			PoemView poemView = new PoemView(getContext(), null,
					poemJsonData.mPoemData, mHandler);
			poemView.setTag(VIEW_SINGLE);
			view = poemView;
			break;		
		case PreFormatData.JSON_MUSIC_ALBUM:
			MusicAlbumJsonData musicAlbumJsonData = (MusicAlbumJsonData) preformatData
					.getJsonData();
			MusicAlbumView musicAlbumView = new MusicAlbumView(getContext(),
					musicAlbumJsonData.mLstMusicData);
			musicAlbumView.setTag(VIEW_FORM);
			view = musicAlbumView;
			break;
		case PreFormatData.JSON_ARTIST_ALBUM:
			ArtistAlbumJsonData artistAlbumJsonData = (ArtistAlbumJsonData) preformatData
					.getJsonData();
			MusicArtistAlbumView artistAlbumView = new MusicArtistAlbumView(
					getContext(), artistAlbumJsonData.mLstArtistData);
			artistAlbumView.setTag(VIEW_FORM);
			view = artistAlbumView;
			break;
		case PreFormatData.JSON_SHOPPING_ITEM:
			ShoppingItemJsonData shoppingItemJsonData = (ShoppingItemJsonData) preformatData
					.getJsonData();
			ShopingView shopingView = new ShopingView(getContext(),
					shoppingItemJsonData.mLstItem);
			shopingView.setTag(VIEW_FORM);
			view = shopingView;
			break;
		case PreFormatData.JSON_TRAIN:
			TrainJsonData trainJsonData = (TrainJsonData) preformatData
					.getJsonData();
			TrainView trainView = new TrainView(getContext(),
					trainJsonData.mTrainData, mHandler);
			trainView.setTag(VIEW_FORM);
			view = trainView;
			break;	
		}
		if (view == null) {
			view = WidgetViewFactory.getWidgetView(getContext(), preformatData, operationEnable, mHandler, false);
		}
		if (view != null) {
			view.setLayoutParams(layoutContentParam);
			removeAllViews();
			addView(view);
		}
	}

	protected void addConfirmViews(ConfirmData confirmData, boolean operationEnable) {
		if (confirmData.getSmsData() != null) {
			if (mSendSMSView == null) {
				mSendSMSView = new SendSMSView(getContext(), confirmData,
						operationEnable, mHandler);
				/*mSendSMSView.setData(confirmData, operationEnable, 
						mHandler);*/
				mSendSMSView.setTag(VIEW_SINGLE);
				addView(mSendSMSView);
			} else {
				/*mSendSMSView.setData(confirmData, operationEnable, 
						mHandler);*/
				mSendSMSView.setOperationEnableDisable();
			}
		} else {
			
			//addHtmlViews(confirmData.getDisplayString());
			if (!operationEnable && confrimLayout != null) {
				if (mChatLeftLayout != null) {
					mChatLeftLayout.removeView(confrimLayout);
				}
				else {
					removeAllViews();
				}
				return;
			}
			
			if (!confirmData.isContainData()) {
				if(confirmData.getContactData() != null){
					mContactConfimView = new ContactConfimView(getContext(),confirmData,operationEnable, mHandler);
					mContactConfimView.setTag(VIEW_SINGLE);
					addView(mContactConfimView);
//					if (Tts.isPlaying()) {
//						Tts.stop(Tts.TTS_NORMAL_PRIORITY);
//					}
					return;
				}else{
					confrimLayout = new ConfirmCancelView(getContext(), confirmData,
							operationEnable, mHandler);
					addView(confrimLayout);	
				}
		
				//if (mChatLeftLayout == null) {
				/*}else {
					RelativeLayout.LayoutParams txtParams = new LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					if (mTxtView != null) {
						txtParams.addRule(RelativeLayout.BELOW, mTxtView.getId());
					}
					confrimLayout.setLayoutParams(txtParams);
					mChatLeftLayout.addView(confrimLayout);
				}*/
			}
		}
	}

	protected void addHelpDataViews(HelpData helpData,boolean operationEnable) {
		HelpGuideView helpGuideView = new HelpGuideView(getContext(), helpData, operationEnable, mHandler);//HelpGuideView(getContext(), helpData, operationEnable, mHandler);
		View view = helpGuideView.initView();
		if (view != null) {
			view.setLayoutParams(layoutContentParam);
			removeAllViews();
			addView(view);
		}
	}
	
	String htmlContent = "";

	private ContactConfimView mContactConfimView;

	protected void addHtmlViews(String text) {
		final int minCount = 5;
		final int maxCount = 1000;
		final int txtContentID = 1000001;
		final int txtMoreID = 1000002;
		final int bgLineID = 1000003;
		
		if (mChatLeftLayout != null) {
			return;
		}
		

		int width = getContext().getResources().getDisplayMetrics().widthPixels;
		int countTxtNum = width * 3 / 4;
		if (htmlContent != null && htmlContent.length() > 0) {
			htmlContent += "<br>";
		}
		if (GlobalData.getmServer_version() != null) {
			text = text + GlobalData.getmServer_version();
			GlobalData.setmServer_version(null);
		}
		htmlContent += text;
		if (textCreate == false) {
			mChatLeftLayout = new RelativeLayout(getContext());
			RelativeLayout.LayoutParams txtParams = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams layoutContainerParams = new LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutContainerParams.rightMargin = (int) getResources()
					.getDimension(R.dimen.main_item_left_chat_margin_right);
			mChatLeftLayout.setLayoutParams(layoutContainerParams);
			txtParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			txtParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mTxtView = new NetImageTextView(getContext());
			mTxtView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES |Linkify.MAP_ADDRESSES |Linkify.WEB_URLS);
			mTxtView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_FORCE_STOP_TTS);
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				}
			});
			mTxtView.setClickable(true);
			mTxtView.setId(txtContentID);
			mTxtView.setSingleLine(false);
			mTxtView.setTextColor(getResources()
					.getColor(R.color.txt_chat_left));
			mTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
					.getDimension(R.dimen.txt_chat_size));
			mTxtView.setPadding(
					(int) getResources().getDimension(
							R.dimen.main_item_left_chat_txt_padding_left),
					(int) getResources().getDimension(
							R.dimen.main_item_left_chat_txt_padding_top),
					(int) getResources().getDimension(
							R.dimen.main_item_left_chat_txt_padding_right),
					(int) getResources().getDimension(
							R.dimen.main_item_left_chat_txt_padding_bottom));
			mTxtView.setLayoutParams(txtParams);
			mChatLeftLayout.addView(mTxtView);
			RelativeLayout.LayoutParams lineParams = new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			lineParams.addRule(RelativeLayout.BELOW, mTxtView.getId());
			view_line = new View(getContext());
			view_line.setId(bgLineID);
			view_line.setBackgroundResource(R.drawable.bg_line);
			view_line.setLayoutParams(lineParams);
			mChatLeftLayout.addView(view_line);
			RelativeLayout.LayoutParams moreParams = new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			moreParams.addRule(RelativeLayout.BELOW, view_line.getId());
			moreParams.setMargins(0, 5, 0, 10);
			tv_more = new TextView(getContext());
			tv_more.setId(txtMoreID);
			tv_more.setTag(mTxtView);
			tv_more.setText("查看更多");
			tv_more.setPadding(0, ContentUtil.dip2px(getContext(), 8), 0,
					ContentUtil.dip2px(getContext(), 8));
			tv_more.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
					.getDimension(R.dimen.txt_chat_size));
			tv_more.setTextColor(getResources().getColor(R.color.txt_color));
			tv_more.setGravity(Gravity.CENTER_HORIZONTAL);
			tv_more.setLayoutParams(moreParams);
			tv_more.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					TextView tv_more = (TextView) v;
					NetImageTextView txt = (NetImageTextView) v.getTag();
					int count = (Integer) txt.getTag();
					if (count == 5) {
						txt.setMaxLines(maxCount);
						txt.setTag(maxCount);
						tv_more.setText("收起全部");
					} else {
						txt.setMaxLines(minCount);
						txt.setTag(minCount);
						tv_more.setText("查看更多");
					}
				}
			});
			mChatLeftLayout.addView(tv_more);
			textCreate = true;
			view_line.setVisibility(View.GONE);
			tv_more.setVisibility(View.GONE);
		}

		if (mTxtView != null && htmlContent != null && htmlContent.length() > 0
				&& tv_more != null) {
			if (htmlContent.length() > countTxtNum) {
				tv_more.setVisibility(View.VISIBLE);
				view_line.setVisibility(View.VISIBLE);
				mTxtView.setMaxLines(minCount);
				mTxtView.setTag(minCount);
			} else {
				if (tv_more != null) {
					view_line.setVisibility(View.GONE);
					tv_more.setVisibility(View.GONE);
				}
				mTxtView.setMaxLines(maxCount);
				mTxtView.setTag(maxCount);
			}
			mTxtView.setHtmlText(htmlContent);
		}
		
		mChatLeftLayout.setBackgroundResource(R.drawable.bg_chat_left);
		addView(mChatLeftLayout);
		mChatLeftLayout.setClickable(mEnabled);
		if (mChatLeftLayout instanceof ViewGroup) {
			ViewGroup subGroup = (ViewGroup) mChatLeftLayout;
			for (int k = 0; k < subGroup.getChildCount(); k++) {
				subGroup.getChildAt(k).setClickable(
						mEnabled);
			}
		}
		if (tv_more != null && tv_more.getVisibility() ==View.VISIBLE) {
			tv_more.setClickable(true);
		}
	}

	public void setData(BaseData data, boolean operationEnable) {
		if (mEnabled != null && mEnabled == operationEnable) {
			return;
		}
		if(data.isSelectionData())
		   operationEnable = true;
		mEnabled = operationEnable;
		mData = data;
		RelativeLayout.LayoutParams layoutContainer = new LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams layoutCopyText = new LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		if (data.getFrom() == DataConst.FROM_NOTIFY) {
			NotifyData notify = (NotifyData)data;
			if (notify != null) {
				addView(new NotificationDataView(getContext(), notify, operationEnable, mHandler));
			}
		} else {
			if (data.getFrom() == DataConst.FROM_SERVER) {
				layoutContainer.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				layoutContainer.rightMargin = (int) getResources()
						.getDimension(R.dimen.main_item_left_chat_margin_right);
				textCreate = false;
				htmlContent = "";
				mTxtView = null;
				tv_more = null;
				view_line = null;
				addViewFromServerData(data, operationEnable);
				
				for(int i = 0; i < this.getChildCount(); i++) {
					View v = this.getChildAt(i);
					v.setOnTouchListener(new View.OnTouchListener(){

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								Log.i(TAG, "On View Touch");
								mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
								break;
							case MotionEvent.ACTION_MOVE:						
								break;
							case MotionEvent.ACTION_UP:
								break;
							}
							return true;
						}
						
					});
				}

				if (getChildCount() == 0){
					Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REMOVE_DATA);
					msg.obj = mData;
					mHandler.sendMessage(msg);
				}
			} else if (data.getFrom() == DataConst.FROM_MIC) {
				if (mRightView != null) {
					return;
				}
				mRightView = new TextView(getContext());
				ImageButton mImgCopyText = new ImageButton(getContext());

				layoutContainer.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				layoutCopyText.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				layoutContainer.addRule(RelativeLayout.LEFT_OF,mImgCopyText.getId());
				
				mImgCopyText.setLayoutParams(layoutCopyText);
				mImgCopyText.setVisibility(View.VISIBLE);
				layoutContainer.leftMargin = (int) getResources().getDimension(
						R.dimen.main_item_right_chat_margin_left);
				mRightView.setBackgroundResource(R.drawable.bg_chat_right);
				mRightView.setSingleLine(false);
				mRightView.setTextColor(getResources().getColor(
						R.color.txt_chat_right));
				mRightView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
						.getDimension(R.dimen.txt_chat_size));
				mRightView.setText(data.getDisplayString());
				
				mRightView.setPadding(
						(int) getResources().getDimension(
								R.dimen.main_item_right_chat_txt_padding_left),
						(int) getResources().getDimension(
								R.dimen.main_item_right_chat_txt_padding_top),
						(int) getResources().getDimension(
								R.dimen.main_item_right_chat_txt_padding_right),
						(int) getResources()
								.getDimension(
										R.dimen.main_item_right_chat_txt_padding_bottom));
				
				mImgCopyText.setBackgroundColor(0);
				mImgCopyText.setPadding(
						(int) getResources().getDimension(
								R.dimen.main_item_edit_chat_txt_padding_left),
						(int) getResources().getDimension(
								R.dimen.main_item_edit_chat_txt_padding_top),
						(int) getResources().getDimension(
								R.dimen.main_item_edit_chat_txt_padding_right),
						(int) getResources().getDimension(
								R.dimen.main_item_edit_chat_txt_padding_bottom));
				mImgCopyText.setImageResource(R.drawable.icon_chat_edit);
				mImgCopyText.setOnClickListener(new ImageView.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mData != null) {
							Message msg = mHandler
									.obtainMessage(MsgConst.MSG_COPY_TEXT_FROM_ITEM);
							msg.obj = mData.getDisplayString();
							mHandler.sendMessage(msg);
						}
					}
				});
				mRightView.setLayoutParams(layoutContainer);
				mRightView.setEnabled(true);
				addView(mRightView);
				addView(mImgCopyText);
			}
		}

	}
	
	private void addViewFromServerData(BaseData baseData, boolean operationEnable) {
		if (baseData instanceof SentenceData) {
			String sDisp = baseData.getDisplayString();
			addHtmlViews(sDisp);
		}
		
		if (baseData instanceof QuestionData) {
			String sDisp = baseData.getDisplayString();
			addHtmlViews(sDisp);
		}
		
		if (baseData instanceof PreFormatData) {
			addPreformatDataViews((PreFormatData) baseData, operationEnable);
		}

		if (baseData instanceof OptionData) {
			addOptionViews((OptionData) baseData, operationEnable);
		}

		if (baseData instanceof ConfirmData) {
			addConfirmViews((ConfirmData) baseData, operationEnable);
		}
		
		if(baseData instanceof HelpData){
			addHelpDataViews((HelpData)baseData,operationEnable);
		}
	}

	public boolean ismEnabled() {
		if (mEnabled == null) {
			return true;
		}
		return mEnabled;
	}
	
	public void handlerMessage(AppData.ServerCommand cmd) {
		for (int k = 0; k < this.getChildCount(); k++) {
			View item = this.getChildAt(k); 
			if (item instanceof SelectionBaseView) {
				((SelectionBaseView) item).handleServerCmd();
			}
		}
	}
	
	public void handlerMessage(String cmd, String param1, String param2) {
		for (int k = 0; k < this.getChildCount(); k++) {
			View item = this.getChildAt(k); 
			if (item instanceof SelectionBaseView) {
				((SelectionBaseView) item).handleServerCmd(cmd, param1, param2);
				break;
			}
		}
	}
}
