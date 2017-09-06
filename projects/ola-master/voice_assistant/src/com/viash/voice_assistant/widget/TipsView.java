package com.viash.voice_assistant.widget;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.TipsData;

/**
 * Tips
 * @author Colin Wu
 * @createDate 2013-5-3
 */
@SuppressLint("ViewConstructor")
public class TipsView extends RelativeLayout {
	private static final String TAG = "TipsView";
	
	public static final int TIPS_TIME_DELAY_SHORT = 2000;
	public static final int TIPS_TIME_DELAY = 8000;//5000
	public static final long TIME_INTERVAL_REPEAT = 1000 * 60 * 60 * 24 * 7;//1000 * 60 * 30;//

	private Context mContext;
	private static int indexTips = 0;
	private int lengthOfDisplayText = 0;
	
	private ImageView help_iconbig;
	private ImageView help_icon_emotion;
	private LinearLayout textGroup1;
	private LinearLayout textGroup2;
	private LinearLayout textGroup3;
	private LinearLayout textGroup4;
	private TextView text1;
	private TextView text2;
	private TextView text3;
	private TextView text4;
	private View view;
	
	// arraylist of emotion
	private ArrayList<Integer> emotionArray = new ArrayList<Integer>();
	// arraylist of helpIcon
	private ArrayList<Integer> helpIconArray = new ArrayList<Integer>();
	// arraylist of tip text
	private ArrayList<ArrayList<ArrayList<String>>> childArray = new ArrayList<ArrayList<ArrayList<String>>>();
	
	protected Handler durationHandler = new Handler();
	
    Runnable durationRunnable = new Runnable() {
		
		@Override
		public void run() {
			//Log.d(TAG, "durationRunnable.run");
			if (TipsView.this.getVisibility() == View.VISIBLE) {
				TipsView.this.setVisibility(View.GONE);
				durationHandler.postDelayed(durationRunnable, TIPS_TIME_DELAY);
			}
			else {
				TipsView.this.setVisibility(View.VISIBLE);
				indexTips++;
				updateView(indexTips % helpIconArray.size());
				durationHandler.postDelayed(durationRunnable, getDurationTime());
			}

		}
	};
	
	
	public TipsView(Context context) {
		super(context);
		this.mContext = context;
		initData();
		init(context);
	}
	
	public TipsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initData();
		init(context);
	}
	
	private void initData() {
		// child
		childArray.add(addChild(R.array.help_child_contact));
		childArray.add(addChild(R.array.help_child_phone));
		childArray.add(addChild(R.array.help_child_sms));
		childArray.add(addChild(R.array.help_child_calendar));
		childArray.add(addChild(R.array.help_child_remind));
		childArray.add(addChild(R.array.help_child_note));
		childArray.add(addChild(R.array.help_child_clock));
		childArray.add(addChild(R.array.help_child_open));
		childArray.add(addChild(R.array.help_child_joke));
		childArray.add(addChild(R.array.help_child_setting));
		childArray.add(addChild(R.array.help_child_music));
		childArray.add(addChild(R.array.help_child_video));
		childArray.add(addChild(R.array.help_child_poem));
		childArray.add(addChild(R.array.help_child_news));
		childArray.add(addChild(R.array.help_child_weather));
		childArray.add(addChild(R.array.help_child_search));
		childArray.add(addChild(R.array.help_child_wiki));
		childArray.add(addChild(R.array.help_child_ask));
		childArray.add(addChild(R.array.help_child_weibo));
		childArray.add(addChild(R.array.help_child_poi));
		childArray.add(addChild(R.array.help_child_route));
		childArray.add(addChild(R.array.help_child_stock));
		childArray.add(addChild(R.array.help_child_booking_train));
		childArray.add(addChild(R.array.help_child_booking_air));
		childArray.add(addChild(R.array.help_child_booking_hotel));
		childArray.add(addChild(R.array.help_child_buy));
		childArray.add(addChild(R.array.help_child_compute));
		childArray.add(addChild(R.array.help_child_unit));
		childArray.add(addChild(R.array.help_child_24));
		childArray.add(addChild(R.array.help_child_funny));
		childArray.add(addChild(R.array.help_child_exchangerate));
		childArray.add(addChild(R.array.help_child_tvintr));
		childArray.add(addChild(R.array.help_child_businfo));

		// helpIconArray
        Integer[] helpIcon = {
        		R.drawable.help_iconbig_contact,
				R.drawable.help_iconbig_phone,
        		R.drawable.help_iconbig_sms,
        		R.drawable.help_iconbig_calendar,
        		R.drawable.help_iconbig_remind,
        		R.drawable.help_iconbig_note,
        		R.drawable.help_iconbig_clock,
        		R.drawable.help_iconbig_open,
        		R.drawable.help_iconbig_download_app,
        		R.drawable.help_iconbig_setting,
        		R.drawable.help_iconbig_music,
        		R.drawable.help_iconbig_video,
        		R.drawable.help_iconbig_poem,
        		R.drawable.help_iconbig_news,
        		R.drawable.help_iconbig_weather,
        		R.drawable.help_iconbig_search,
        		R.drawable.help_iconbig_wiki,
        		R.drawable.help_iconbig_ask,
        		R.drawable.help_iconbig_weibo,
        		R.drawable.help_iconbig_poi,
        		R.drawable.help_iconbig_route,
        		R.drawable.help_iconbig_stock,
        		R.drawable.help_iconbig_booking_train,
        		R.drawable.help_iconbig_booking_air,
        		R.drawable.help_iconbig_booking_hotel,
        		R.drawable.help_iconbig_buy,
        		R.drawable.help_iconbig_compute,
        		R.drawable.help_iconbig_unit,
        		R.drawable.help_iconbig_24,
        		R.drawable.help_iconbig_funny,
        		R.drawable.help_iconbig_exchangerate,
        		R.drawable.help_iconbig_tv,
        		R.drawable.help_iconbig_bus
        };

    	for (int i = 0; i < helpIcon.length; i++) {
    		helpIconArray.add(helpIcon[i]);
    	}
        if (helpIconArray.size() != childArray.size()) {
        	Log.e(TAG, "number of help icon is not equal tips text.");
        }
        
        //emotionArray
        Integer[] emotionIcon = {
        		R.drawable.tips_emotion_01,
				R.drawable.tips_emotion_03,
        		R.drawable.tips_emotion_05,
        		R.drawable.tips_emotion_07,
        		R.drawable.tips_emotion_09,
        		R.drawable.tips_emotion_11,
        		R.drawable.tips_emotion_13,
        		R.drawable.tips_emotion_15,
        		R.drawable.tips_emotion_25,
        };
        for(int i = 0 ; i < emotionIcon.length ; i++){
        	emotionArray.add(emotionIcon[i]);
        }
        
    	//int countTips = childArray.size();
    	//int countEmotions = emotionArray.size();
        //Log.d(TAG, "initData countTips:"+countTips+",countEmotions:"+countEmotions);
	}
	
	private ArrayList<ArrayList<String>> addChild(int resId) {
		ArrayList<ArrayList<String>> childs = new ArrayList<ArrayList<String>>();
		ArrayList<String> child = new ArrayList<String>();
		String[] child_contact = mContext.getResources().getStringArray(resId);
		if (child_contact.length <= 0) {
			Log.e(TAG, "some tips string is empty.");
			return null;
		}
		String firStr = child_contact[0];
		int firNo = Integer.parseInt(firStr.substring(0, 1));
		child.add(firStr.substring(2, firStr.length()));
		for(int i = 1 ; i < child_contact.length ; i++){
			String secStr = child_contact[i];
			int secNo = Integer.parseInt(secStr.substring(0, 1));
			//Log.d(TAG, "i:"+i+",secStr:"+secStr);
			if (secNo == firNo) {
				child.add(secStr.substring(2, secStr.length()));
			}
			else {
				childs.add(child);
				child = new ArrayList<String>();
				child.add(secStr.substring(2, secStr.length()));
				firStr = secStr;
				firNo = secNo;
			}
		}
		if (child.size() > 0) {
			childs.add(child);
		}
		
		return childs;
	}
	
	private void init(Context context) {
		view = LayoutInflater.from(context).inflate(R.layout.layout_tipsview, null);
		help_iconbig = (ImageView)view.findViewById(R.id.help_iconbig);
		help_icon_emotion = (ImageView)view.findViewById(R.id.help_icon_emotion);
		textGroup1 = (LinearLayout)view.findViewById(R.id.textGroup1);
		textGroup2 = (LinearLayout)view.findViewById(R.id.textGroup2);
		textGroup3 = (LinearLayout)view.findViewById(R.id.textGroup3);
		textGroup4 = (LinearLayout)view.findViewById(R.id.textGroup4);
		text1 = (TextView)view.findViewById(R.id.text1);
		text2 = (TextView)view.findViewById(R.id.text2);
		text3 = (TextView)view.findViewById(R.id.text3);
		text4 = (TextView)view.findViewById(R.id.text4);
		if (helpIconArray.size() > 0) {
			help_iconbig.setImageResource((Integer) helpIconArray.get(0));
		}
		
		addView(view, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		indexTips++;
		updateView(indexTips % helpIconArray.size());
	}
	
	/*
	 * updateView
	 * parameters:
	 * 		index:第index个大类，如短信、通讯录、电话等
	 * return:对于第index个大类
	 * 		true:有未提示过的小项（sample sentences），
	 * 		false:全部提示过
	 */
	private boolean updateView(int index) {
		//Log.d(TAG, "updateView index:"+index);
		if (index < 0 && index >= helpIconArray.size()) {
			Log.e(TAG, "parameter is outofrange.");
			return false;
		}
		
		long lastDate = 0;
		long currentDate = 0;
		String key;
		int indexChild = 0;
		
		// 对于第index大类，取一个随机的小项：如果该小项一周内提示过，则跳过并对下一个小项判断；否则提示该小项
		ArrayList<ArrayList<String>> childs = childArray.get(index);
		int randomChild = getRandomInt(childs.size()) % childs.size();
		currentDate = System.currentTimeMillis();
		int i = 0;
		for (i = 0; i< childs.size(); i++) {
			indexChild = (randomChild + i) % childs.size();
			key = String.valueOf(index) + "_" + String.valueOf(indexChild);
			lastDate = TipsData.getLastDate(mContext, key);
			currentDate = System.currentTimeMillis();
			boolean isRepeatInOneWeek = false;
			if (lastDate != 0) {
				long dDate = currentDate - lastDate;
				if (dDate <= TIME_INTERVAL_REPEAT) {
					isRepeatInOneWeek = true;
				}
			}
			//Log.d(TAG, "[" + key + "]"+"currentDate-lastDate = " + currentDate + "-" + lastDate +" = " + (currentDate-lastDate));
			
			if (isRepeatInOneWeek) {
				//Log.d(TAG, "小项" + key + "一周内提示过");
				continue;
			}
			else {
				//Log.d(TAG, "小项" + key + "一周内未提示过");
				break;
			}
		}
		
		if (i >= childs.size()) {
			//Log.d(TAG, "大类" + index + "一周内全部提示过");
			return false;
		}
		
		// 对于一周内未提示过的第index大类第indexChild小项：
		//Log.d(TAG, "indexChild:" + indexChild);
		ArrayList<String> child = childs.get(indexChild);
		updateTipsStatus(child);
			
		// update help icon
		help_iconbig.setImageResource((Integer) helpIconArray.get(index));

		// switch to random emotion
		int indexEmotion = 0;
		if (emotionArray.size() > 0) {
			indexEmotion = getRandomInt(emotionArray.size()) % emotionArray.size();
			//Log.d(TAG, "updateView indexEmotion:"+indexEmotion);
			help_icon_emotion.setVisibility(View.VISIBLE);
			help_icon_emotion.setImageResource(emotionArray.get(indexEmotion));
		}
		else {
			Log.e(TAG, "have not any emotion.");
			help_icon_emotion.setVisibility(View.GONE);
		}

		// 保存当前的key-value对  <key,currentDate>
		key = String.valueOf(index) + "_" + String.valueOf(indexChild);
		TipsData.setCurrentDate(mContext, key, currentDate);
		
		return true;
	}
	
	private void updateTipsStatus(ArrayList<String> child) {
		int numberOfSentences = child.size();
		if (numberOfSentences <= 0 || numberOfSentences > 4) {
			Log.e(TAG, "numbers of sentences is outofrange.");
		}
		switch (numberOfSentences){
		case 1:
			textGroup2.setVisibility(View.GONE);
			textGroup3.setVisibility(View.GONE);
			textGroup4.setVisibility(View.GONE);
			text1.setText(child.get(0));
			lengthOfDisplayText = 5 + child.get(0).length();
			break;
		case 2:
			textGroup2.setVisibility(View.VISIBLE);
			textGroup3.setVisibility(View.GONE);
			textGroup4.setVisibility(View.GONE);
			text1.setText(child.get(0));
			text2.setText(child.get(1));
			lengthOfDisplayText = 5 * 2 + child.get(0).length() + child.get(1).length();
			break;
		case 3:
			textGroup2.setVisibility(View.VISIBLE);
			textGroup3.setVisibility(View.VISIBLE);
			textGroup4.setVisibility(View.GONE);
			text1.setText(child.get(0));
			text2.setText(child.get(1));
			text3.setText(child.get(2));
			lengthOfDisplayText = 5 * 3 + child.get(0).length() + child.get(1).length() + 
					child.get(2).length();
			break;
		case 4:
			textGroup2.setVisibility(View.VISIBLE);
			textGroup3.setVisibility(View.VISIBLE);
			textGroup4.setVisibility(View.VISIBLE);
			text1.setText(child.get(0));
			text2.setText(child.get(1));
			text3.setText(child.get(2));
			text4.setText(child.get(3));
			lengthOfDisplayText = 5 * 4 + child.get(0).length() + child.get(1).length() + 
					child.get(2).length() + child.get(3).length();
			break;
		}

	}
	
	private int getRandomInt(int upper) {
		return Integer.valueOf((int) (Math.random()*upper));
	}
	
	public int getDurationTime() {
		int duration;
		duration = lengthOfDisplayText * 350;
		return duration;
	}
	
	public boolean refreshTipData() {
		int tempIndex = 0;
		int i = 0;
		for (i = 0; i < helpIconArray.size(); i++) {
			tempIndex = (indexTips + i) % helpIconArray.size();
			if (updateView(tempIndex)) {
				//Log.d(TAG, "现在提示大类：" + tempIndex);
				break;
			}
		}
		
		if (i >= helpIconArray.size()) {
			//Log.d(TAG, "所有大类全部提示过");
			return false;
		}
		
		indexTips = (tempIndex + 1) % helpIconArray.size();
		return true;
	}
	
	@TargetApi(8)
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if(visibility==GONE){
			Animation animation = (Animation) AnimationUtils.loadAnimation(this.getContext(), R.anim.voice_alpha_out);
			this.startAnimation(animation);
		}
		if(visibility==View.VISIBLE){
			Animation animation = (Animation) AnimationUtils.loadAnimation(this.getContext(), R.anim.voice_alpha);
			this.startAnimation(animation);
		}
	}
}
