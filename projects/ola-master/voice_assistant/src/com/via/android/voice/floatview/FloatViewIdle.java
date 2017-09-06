package com.via.android.voice.floatview;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.util.DensityUtil;
import com.viash.voice_assistant.widget.RotateView;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.HelpStatisticsUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FloatViewIdle {

	public final int FLOAT_ICON_VIEW_TYPE = 1;
	public final int FLOAT_RECORD_VIEW_TYPE = 2;
	public final int FLOAT_GUIDE_VIEW_TYPE = 3;
	private final String SPEAK_AGAIN = "再说一遍";
	private final int icon_width = 67;
	private final int icon_width_side = 30;
	private static final int MSG_REFRESH_VOLUME = 1;
	private static final int MSG_REMOVE_FLOAT_VIEW = 2;
	private static final int MSG_UPDATE_VIEW_SENDING_TO_SERVER = 4;
	private static final int MSG_FLOAT_VIEW_MOVE_TO_EDGE = 5;
	private static final int MSG_START_GUIDE_ACTIVITY = 6;
	private static final int MSG_UPDATE_ROTATE_VIEW = 7;
	private static final int MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED = 8;
	private static final int MSG_UPDATE_FLOAT_VIEW_ON_SIDE = 9;
	public  static final String START_FROM_FLOAT_VIEW = "needAddCommondata";
    private static FloatViewIdle floatViewManager;
    private static Context mContext;
    private static WindowManager winManager;
    private static int displayWidth;
    private static int displayHeight;
    private FloatIconView floatIconView;
    private FloatRecordView floatRecordView;
    private FloatGuideView floatGuideView;
    private WindowManager.LayoutParams params;
    private Handler handler;
    private int floatViewType = 0;
    private Handler mHandler;
    private static boolean isHide = true;
    private String recordString = null;
    private CommunicationData mCommData = null;
    private int icon_width_side_temp = 0;
    public static boolean IS_START_FROM_FLOAT_VIEW_IDLE = false;
    public static boolean IS_RECORD_FROM_FLOAT_VIEW_IDLE = false;
    public static boolean IS_CANCEL_RECORD = false;
    private FloatViewIdle()
    {
    	initHandler();
    }
    
	public static synchronized FloatViewIdle getInstance(Context context)
	{
		if(floatViewManager == null)
		{
			mContext = context.getApplicationContext();;
			winManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			displayWidth = winManager.getDefaultDisplay().getWidth();
			displayHeight = winManager.getDefaultDisplay().getHeight();
			floatViewManager = new FloatViewIdle();
		}
		return floatViewManager;
	}
	
	 private void initHandler(){
		   handler = new Handler(){
			  @Override
			  public void handleMessage(Message msg) 
			  {
					switch (msg.what) 
					{
					case MSG_REFRESH_VOLUME:
						if(floatRecordView != null)
							floatRecordView.updateVolume((int)msg.arg1);
						break;
					case MSG_FLOAT_VIEW_MOVE_TO_EDGE:
						moveAnimation((View)msg.obj);
						break;
					case MSG_REMOVE_FLOAT_VIEW:
						if(msg.arg1 == 1)
						{
							if(floatIconView != null)
							{
								winManager.removeView(floatIconView);
								floatIconView = null;
								floatRecordView = getFloatRecordView();
								if(floatRecordView != null)
								{	
								   if(floatRecordView.getParent() == null)
								   {
									  winManager.addView(floatRecordView, params);
									  floatViewType = FLOAT_RECORD_VIEW_TYPE;
								   }
								   if(mHandler != null)
								   {
								     mHandler.sendMessage(mHandler.obtainMessage(MsgConst.MSG_START_CAPTURE));
								     IS_RECORD_FROM_FLOAT_VIEW_IDLE = true;
								   }
								}
							}
						}
						else
						{
						   if(floatRecordView != null)
						   {
							   winManager.removeView(floatRecordView);
							   floatRecordView = null;
						   }
						   floatIconView = getFloatIconView();
						   if(floatIconView != null)
						   {
							  if(floatIconView.getParent() == null)
							  {
								  winManager.addView(floatIconView, params);
								  floatViewType = FLOAT_ICON_VIEW_TYPE;
								  setViewOnClickListener(floatIconView);
							  }
							  moveAnimation(floatIconView);
						   }
						}
						break;
					case MSG_UPDATE_VIEW_SENDING_TO_SERVER:
						if(floatRecordView != null)
						{
							floatRecordView.updateSendingToServerView();
							floatRecordView.setTitle("努力识别中");
						}
						break;
					case MSG_UPDATE_ROTATE_VIEW:
						if(floatRecordView != null)
						{
							floatRecordView.rotateview.startRotate();	
						}
						break;
					case MSG_START_GUIDE_ACTIVITY:
						hide();
						Intent intent = new Intent(mContext,GuideActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra(START_FROM_FLOAT_VIEW, true);
						IS_START_FROM_FLOAT_VIEW_IDLE = true;
						mContext.startActivity(intent);
						break;
					case MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED:
						if(msg.arg1 == 1)
							changeFloatIconToSide(false);
						else if(msg.arg1 == 2)
							changeFloatIconToSide(true);
						else if(msg.arg1 == 3)
							changeFloatIconToNormal();				
						break;
					case MSG_UPDATE_FLOAT_VIEW_ON_SIDE:
						if(msg.arg1 == 1)
							updateFloatIconOnSide(true);
						else if(msg.arg1 == 2)
							updateFloatIconOnSide(false);
						break;
					}
			  }
	       };
	 }
		   
	public void setHandler(Handler handler)
	{
		mHandler = handler;
	}
	
	 public void move(View view, int delatX,int delatY)
	 {
	   if(view == floatIconView)
	   {
		  params.x += delatX;
		  params.y += delatY;
		  winManager.updateViewLayout(view, params);
	   }	   
     }
	
	public void moveAnimation(View view)
	{
	   int currentX = params.x;
	   int viewWidth = params.width;
	   int delt = DensityUtil.dip2px(mContext,6);	   
	   if(currentX + viewWidth/2 >= displayWidth/2-1)
	   {
		   /*if(params.width == icon_width_side)
			   handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED,3,0));
		   move(view,delt,0);
		   if(params.x+icon_width_side_temp <= displayWidth)
			   handler.sendMessage(handler.obtainMessage(MSG_FLOAT_VIEW_MOVE_TO_EDGE,view));
		   else*/
		   {
			   if(view == floatIconView)
			   {
				   floatIconView.isMoveToEdge = false;
				   handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED, 1, 0));
			   }				   
		   }
	   }
	   else
	   {
		   /*if(params.width == icon_width_side)
			   handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED,3,0));
		   move(view,0-delt,0);
		   if(params.x >= 0)
			   handler.sendMessage(handler.obtainMessage(MSG_FLOAT_VIEW_MOVE_TO_EDGE,view));
		   else*/
		   {
			   if(view == floatIconView)
			   {	   
				   floatIconView.isMoveToEdge = false;
				   handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED,2,0));
			   }			  
		   }
	   }
	}
	 
	class FloatIconView extends LinearLayout{

		private int mWidth;
		private int mHeight;
		private int preX;
	    private int preY;
	    private int x;
	    private int y;
	    public boolean isMove;
	    public boolean isMoveToEdge;	
		private FloatViewIdle manager;
		public ImageView imgv_icon_left;
		public ImageView imgv_icon_center;
		public ImageView imgv_icon_right;
		public int mWidthSide;
		
		public FloatIconView(Context context) {
			super(context);
			
			View view = LayoutInflater.from(mContext).inflate(R.layout.layout_floatview_icon, this);
			LinearLayout layout_content = (LinearLayout) view.findViewById(R.id.layout_content);
			imgv_icon_left = (ImageView) view.findViewById(R.id.imgv_icon_left);
			imgv_icon_center = (ImageView) view.findViewById(R.id.imgv_icon_center);
			imgv_icon_right = (ImageView) view.findViewById(R.id.imgv_icon_right);
			imgv_icon_left.setVisibility(View.GONE);
			imgv_icon_center.setVisibility(View.GONE);
			
			mWidth = layout_content.getWidth();
			mHeight = layout_content.getHeight();
			if((mWidth == 0)||(mHeight == 0))
			{
				int temp = DensityUtil.dip2px(mContext, icon_width);
				mHeight = temp;
				icon_width_side_temp = DensityUtil.dip2px(mContext, icon_width_side);
				mWidth = icon_width_side_temp;
			}
			manager = FloatViewIdle.getInstance(mContext);
			if(params != null)
			{
				params.x = displayWidth - icon_width_side_temp;
				params.y = displayHeight/2;
			}
		}
		
		public int getFloatViewWidth()
		{
			return mWidth;
		}
		public int getFloatViewHeight()
		{
			return mHeight;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				 preX = (int)event.getRawX();
				 preY = (int)event.getRawY();
				 isMove = false;
				 if(params.width == icon_width_side_temp)
					 handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED, 3, 0));
				 break;
			case MotionEvent.ACTION_UP:				 
				 if(isMoveToEdge == true)
				 {
					 if(params.width == icon_width_side_temp)
						 handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED, 3, 0));
					 handler.sendMessage(handler.obtainMessage(MSG_FLOAT_VIEW_MOVE_TO_EDGE,this));					 
				 }
				 break;
			case MotionEvent.ACTION_MOVE:
				 x = (int)event.getRawX();
				 y = (int)event.getRawY();				 
				 if(Math.abs(x-preX)>1||Math.abs(y-preY)>1)
				 {	 				 
				  isMoveToEdge = true;
				 }
				 if(Math.abs(x-preX)>5||Math.abs(y-preY)>5)
					 isMove = true;
				 if(params.width == icon_width_side_temp)
					 handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_AFTER_CHANGED, 3, 0));
				 manager.move(this, x-preX, y-preY);
				 preX = x;
				 preY = y;
				 break;
			}
			return super.onTouchEvent(event);
		}
	}
	
	class FloatRecordView extends LinearLayout{

		private int mWidth;
	    private int mHeight;
		private ImageView imgv_voice_volume;
	    private TextView tv_title;
	    private TextView tv_content;
		private Button bt_left;
		private Button bt_right;
	    public RotateView rotateview;
	    private FloatViewIdle manager;
	    private RelativeLayout layout_volume;
	    
		public FloatRecordView(Context context) {
			super(context);
			View view = LayoutInflater.from(mContext).inflate(R.layout.layout_floatview_record_full_screen, this);
			LinearLayout layout_content = (LinearLayout) view.findViewById(R.id.layout_content);
			layout_volume = (RelativeLayout) view.findViewById(R.id.layout_volume);
			imgv_voice_volume = (ImageView) view.findViewById(R.id.imgv_volume_in_call);
			tv_title = (TextView) view.findViewById(R.id.tv_title);
			tv_content = (TextView) view.findViewById(R.id.tv_content);
			bt_left = (Button) view.findViewById(R.id.bt_left);
			bt_right = (Button) view.findViewById(R.id.bt_right);
			rotateview = (RotateView) view.findViewById(R.id.rotateview);
			rotateview.setImageDrawable(R.drawable.voice_loading_circle);
			rotateview.setVisibility(View.INVISIBLE);			
			
			
			bt_right.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(rotateview != null)
					   rotateview.stopRotate();
					handler.sendMessage(handler.obtainMessage(MSG_REMOVE_FLOAT_VIEW, 2, 0));
					if(mHandler != null)
					{
					  IS_CANCEL_RECORD = true;
					  mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_CANCEL_RECORD));
					  IS_RECORD_FROM_FLOAT_VIEW_IDLE = false;
					}
				}				
			});
			
			bt_left.setOnClickListener(new OnClickListener(){				
				@Override
				public void onClick(View v) {
					if(bt_left.getText().equals(SPEAK_AGAIN))
					{
						if(mHandler != null)
						{
						  mHandler.sendEmptyMessage(MsgConst.MSG_START_CAPTURE);
						  IS_RECORD_FROM_FLOAT_VIEW_IDLE = true;
						}
						tv_content.setVisibility(View.GONE);
						layout_volume.setVisibility(View.VISIBLE);
						rotateview.setVisibility(View.INVISIBLE);
						tv_title.setText("请说话");
						bt_left.setText("说完了");
					}
					else
					{
					   if(mHandler != null)	
					     mHandler.sendEmptyMessage(MsgConst.MSG_STOP_CAPTURE);
					   updateViewSendToServer();
					}
				}				
			});
            
            mWidth = layout_content.getWidth();
			mHeight = layout_content.getHeight();
			if((mWidth == 0)||(mHeight == 0))
			{
				int temp = DensityUtil.dip2px(mContext, 277);
				mWidth = temp;
				mHeight = DensityUtil.dip2px(mContext, 68);
			}
			manager = FloatViewIdle.getInstance(mContext);
		}
		
		public int getFloatViewWidth()
		{
			return mWidth;
		}
		public int getFloatViewHeight()
		{
			return mHeight;
		}
		
		public void updateVolume(int volume)
		{
			if(imgv_voice_volume == null)
				return;
			switch (volume) {
			case 1:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_1);
				break;
			case 2:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_2);
				break;
			case 3:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_3);
				break;
			case 4:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_4);
				break;
			case 5:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_5);
				break;
			case 6:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_6);
				break;
			case 7:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_7);
				break;
			case 8:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_8);
				break;
			case 9:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_9);
				break;
			case 10:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_10);
				break;
			case 11:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_10);
				break;
			case 12:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_10);
				break;
			default:
				imgv_voice_volume.setImageResource(R.drawable.voice_mic_runing_1);
				break;
			}
		}
		
		public void updateSendingToServerView()
		{
			if(rotateview != null)
			{			   
			   rotateview.setVisibility(View.VISIBLE);
			   handler.sendMessage(handler.obtainMessage(MSG_UPDATE_ROTATE_VIEW));
			}
		}
		
		public void setTitle(String value)
		{
			tv_title.setText(value);
		}
		
		public void setContent(String value)
		{
			tv_content.setText(value);
			tv_content.setVisibility(View.VISIBLE);
			layout_volume.setVisibility(View.GONE);
			bt_left.setText(SPEAK_AGAIN);
		}
	}
	
	class FloatGuideView extends LinearLayout{
		
		public FloatGuideView(Context context) {
			super(context);
			
			View view = LayoutInflater.from(mContext).inflate(R.layout.layout_floatview_guide, this);
			LinearLayout layout_content = (LinearLayout) view.findViewById(R.id.layout_content);
			layout_content.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(floatGuideView != null)
					{
					  winManager.removeView(floatGuideView);
					  floatGuideView = null;
					  floatViewType = 0;	
					}
				}				
			});
		}
		
		
	}
	
	private FloatIconView getFloatIconView()
	{
		if(floatIconView == null)
			floatIconView = new FloatIconView(mContext);
		if(params == null)
		{
			params = new LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_PHONE;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明 
			params.gravity = Gravity.LEFT | Gravity.TOP;
			params.width = floatIconView.getFloatViewWidth();
			params.height = floatIconView.getFloatViewHeight();
			params.x = displayWidth - params.width;
			params.y = displayHeight/2;			
		}
		else
		{
			params.width = floatIconView.getFloatViewWidth();
			params.height = floatIconView.getFloatViewHeight();
		}			
		return floatIconView;		
	}
	
	private FloatRecordView getFloatRecordView()
	{
		if(floatRecordView == null)
			floatRecordView = new FloatRecordView(mContext);
		
		if(params == null)
		{
			params = new LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_PHONE;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明 
			params.gravity = Gravity.LEFT | Gravity.TOP;
			params.width = displayWidth;//floatRecordView.getFloatViewWidth();
			params.height = displayHeight;//floatRecordView.getFloatViewHeight();
			params.x = 0;//displayWidth - params.width;
			params.y = 0;//displayHeight/2;
		}
		else
		{
			params.width = displayWidth;//floatRecordView.getFloatViewWidth();
			params.height = displayHeight;//floatRecordView.getFloatViewHeight();
		}
		return floatRecordView;		
	}
	
	private FloatGuideView getFloatGuideView()
	{
		if(floatGuideView == null)
			floatGuideView = new FloatGuideView(mContext);
		
		if(params == null)
		{
			params = new LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_PHONE;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明 
			params.gravity = Gravity.LEFT | Gravity.TOP;
			params.width = displayWidth;
			params.height = displayHeight;
			params.x = 0;
			params.y = 0;
		}
		else
		{
			params.width = displayWidth;
			params.height = displayHeight;
		}
		return floatGuideView;		
	}
	
	private void changeFloatIconToSide(boolean left_side)
	{
		if(left_side)
		{
			if(floatIconView != null)
			{
				floatIconView.imgv_icon_center.setVisibility(View.VISIBLE);
				floatIconView.imgv_icon_left.setVisibility(View.GONE);
				floatIconView.imgv_icon_right.setVisibility(View.GONE);
				params.x = 0;
				params.width = icon_width_side_temp;				   
				winManager.updateViewLayout(floatIconView, params);
				handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_ON_SIDE, 1, 0));
			}
		}
		else
		{	
			if(floatIconView != null)
			{
				floatIconView.imgv_icon_center.setVisibility(View.VISIBLE);
				floatIconView.imgv_icon_left.setVisibility(View.GONE);
				floatIconView.imgv_icon_right.setVisibility(View.GONE);
				params.width = icon_width_side_temp;
				params.x = displayWidth -icon_width_side_temp + 1;
				winManager.updateViewLayout(floatIconView, params);
				handler.sendMessage(handler.obtainMessage(MSG_UPDATE_FLOAT_VIEW_ON_SIDE, 2, 0));
			}
		}
		
	}
	
	private void updateFloatIconOnSide(boolean left_side)
	{
		if(left_side)
		{
			if(floatIconView != null)
			{
				floatIconView.imgv_icon_center.setVisibility(View.GONE);
				floatIconView.imgv_icon_left.setVisibility(View.VISIBLE);
				floatIconView.imgv_icon_right.setVisibility(View.GONE);				   
				winManager.updateViewLayout(floatIconView, params);
			}
		}
		else
		{
			if(floatIconView != null)
			{
				floatIconView.imgv_icon_center.setVisibility(View.GONE);
				floatIconView.imgv_icon_left.setVisibility(View.GONE);
				floatIconView.imgv_icon_right.setVisibility(View.VISIBLE);
				winManager.updateViewLayout(floatIconView, params);	
			}
		}
	}
	private void changeFloatIconToNormal()
	{
		if(floatIconView != null)
		{
			floatIconView.imgv_icon_center.setVisibility(View.VISIBLE);
			floatIconView.imgv_icon_left.setVisibility(View.GONE);
			floatIconView.imgv_icon_right.setVisibility(View.GONE);
			params.width = DensityUtil.dip2px(mContext, icon_width);				   
			winManager.updateViewLayout(floatIconView, params);
		}
	}
	
	public void show()
	{
		isHide = false;

		if(HelpStatisticsUtil.isNeedShowFloatGuideView(mContext))
		{
			floatGuideView = getFloatGuideView();
			winManager.addView(floatGuideView, params);
			floatViewType = FLOAT_GUIDE_VIEW_TYPE;
			HelpStatisticsUtil.setFloatViewGuide(mContext, 1);
			return;
		}
		
		floatIconView = getFloatIconView();
		if(floatIconView != null)
		{
			 if(floatIconView.getParent() == null)
			 {
				  winManager.addView(floatIconView, params);
				  floatViewType = FLOAT_ICON_VIEW_TYPE;
			 }
			 if(floatRecordView != null)
			 {
				 handler.sendMessage(handler.obtainMessage(MSG_REMOVE_FLOAT_VIEW, 2, 0));		
			 }
			 floatIconView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(floatIconView.isMove || floatIconView.isMoveToEdge)
					{
						floatIconView.isMove = false;
						return;
					}
					winManager.removeView(floatIconView);
					floatIconView = null;
					floatRecordView = getFloatRecordView();
					if(floatRecordView != null)
					{
						if(floatRecordView.getParent() == null)
						{
							winManager.addView(floatRecordView, params);
							floatViewType = FLOAT_RECORD_VIEW_TYPE;
						}
						if(mHandler != null)
						{
						  mHandler.sendMessage(mHandler.obtainMessage(MsgConst.MSG_START_CAPTURE));
						  IS_RECORD_FROM_FLOAT_VIEW_IDLE = true;
						}
					}
					
				}				 
			 });
		}
	}
	public void hide()
	{
		try{
			if(floatIconView != null)
			{
				winManager.removeView(floatIconView);
				floatIconView = null;
			}
			if(floatRecordView != null)
			{
				winManager.removeView(floatRecordView);
				floatRecordView = null;
			}
			if(floatGuideView != null)
			{
				winManager.removeView(floatGuideView);
				floatGuideView = null;
			}
			floatViewType = 0;
			isHide = true;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isHide()
	{
		return isHide;
	}
	
	public void showErrorString(String value)
	{
		  if(floatRecordView != null)
		  {
			  if(floatRecordView.rotateview != null)
			  	 floatRecordView.rotateview.stopRotate();
			  if(isHide == false)
			  {
				  floatRecordView.setTitle("友情提示");				  
				  floatRecordView.setContent(value);
			  }
			  IS_RECORD_FROM_FLOAT_VIEW_IDLE = false;
		  }
	}
	
	public void startGuideActivity(CommunicationData commData)
	{
		mCommData = commData;
		if(floatRecordView != null)
		{
		   if(floatRecordView.rotateview != null)
			   floatRecordView.rotateview.stopRotate();			 
		}
		handler.sendMessage(handler.obtainMessage(MSG_START_GUIDE_ACTIVITY));
		HelpStatisticsUtil.putContentToJsonObject(HelpStatisticsUtil.FLOAT_VIEW_COUNT, 1);
	}
	public void updateVolumeView(int volume)
	{
	  if(floatRecordView != null)
		  handler.sendMessage(handler.obtainMessage(MSG_REFRESH_VOLUME,volume,0));
	}
	
	public void updateViewSendToServer()
    {
	  if(floatRecordView != null)
		  handler.sendMessage(handler.obtainMessage(MSG_UPDATE_VIEW_SENDING_TO_SERVER));
    }
	
	public int getFloatViewType()
	{
		return floatViewType;
	}
	
	public void setFloatViewType(int value)
	{
		floatViewType = value;
	}
	
	private void setViewOnClickListener(View view)
	{
		if(view == floatIconView)
		  {
			  view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						if(floatIconView.isMove || floatIconView.isMoveToEdge)
						{
							floatIconView.isMove = false;
							return;
						}
						handler.sendMessage(handler.obtainMessage(MSG_REMOVE_FLOAT_VIEW, 1, 0));					
					}				  
				  });		  
		  }
	}
	
	public void setRecordString(String value)
	{
		recordString = value;
	}
	
	public String getRecordString()
	{
		return recordString;
	}
		
	public CommunicationData getCommonData()
	{
		return mCommData;
	}
	
	public void swapWidthAndHeight()
	{
		int temp = displayWidth;
		displayWidth = displayHeight;
		displayHeight = temp;
		if(floatViewType == FLOAT_ICON_VIEW_TYPE)
		{
			if(floatIconView != null)
			{
				winManager.removeView(floatIconView);
				floatIconView = null;
			}
			show();
		}else if(floatViewType == FLOAT_RECORD_VIEW_TYPE)
		{
			if(floatRecordView.rotateview != null)
				floatRecordView.rotateview.stopRotate();
			handler.sendMessage(handler.obtainMessage(MSG_REMOVE_FLOAT_VIEW, 2, 0));
			if(mHandler != null)
			{
			  mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_CANCEL_RECORD));
			  IS_RECORD_FROM_FLOAT_VIEW_IDLE = false;
			}
		}
	}
}
