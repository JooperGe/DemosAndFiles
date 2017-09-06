package com.viash.voice_assistant.widget.selection;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ImageLoaderUtil;
import com.viash.voicelib.utils.LocalPathUtil;

/**
 * 商品选择
 * @author Harlan Song
 * @createDate 2013-3-16
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class ShopingView extends RelativeLayout{
	private List<String> shops;
	protected boolean operationEnable;
	protected Handler mHandler;
	private LinearLayout layout_content;
	public ShopingView(Context context,List<String> shops,boolean operationEnable,Handler mHandler) {
		super(context);
		this.shops =shops;
		this.operationEnable = operationEnable;
		this.mHandler = mHandler;
		LayoutInflater.from(getContext()).inflate(R.layout.selection_shopping,this,true);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		if(shops !=null && shops.size() > 0 ){
			setData();
		}
	}
	private void setData(){
		if(shops.size() > 3){
			for (int i = 0; i < 3; i++) {
				addItemView(i);
			}
			addMoreView();
		}else{
			for (int i = 0; i < shops.size(); i++) {
				addItemView(i);
			}
		}
	}
	
	private void addMoreView(){
		View moreView = LayoutInflater.from(getContext()).inflate(R.layout.selection_bluetooth_more, null);
		moreView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				layout_content.removeAllViews();
				for (int i = 0; i < shops.size(); i++) {
					addItemView(i);
				}
			}
		});
		layout_content.addView(moreView);
	}
	
	private void addItemView(int position){
		String musicContent=shops.get(position);
		if(musicContent!=null&&musicContent.indexOf("$$")>0){
			String[] itemArray=musicContent.split("\\Q$$\\E");
			View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_shoping_item, null);
			TextView tv_index = (TextView) itemView.findViewById(R.id.tv_index);
			TextView tv_name = (TextView) itemView.findViewById(R.id.tv_name);
			TextView tv_price = (TextView) itemView.findViewById(R.id.tv_price);
			TextView tv_cost_price = (TextView) itemView.findViewById(R.id.tv_cost_price);
			TextView tv_address = (TextView) itemView.findViewById(R.id.tv_address);
			ImageView imgv_clothes = (ImageView) itemView.findViewById(R.id.imgv_clothes);
			tv_cost_price.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG);
			tv_index.setText(String.valueOf(position + 1));
			if(itemArray !=null && itemArray.length > 0){
				tv_name.setText(itemArray[0]);
				if(itemArray.length > 1)
					tv_price.setText("￥" + itemArray[1]);
				layout_content.addView(itemView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				if(itemArray.length > 2 && itemArray[2] !=null)
					ImageLoaderUtil.loadImageAsync(imgv_clothes, itemArray[2], LocalPathUtil.CACHE_IMG_SHOPPING, getResources().getDrawable(R.drawable.default_contact_img),150,null);
				if(itemArray.length > 3 && itemArray[3] !=null)
					tv_cost_price.setText("￥" + itemArray[3]);
				if(itemArray.length > 4 && itemArray[4] !=null)
					tv_address.setText(itemArray[4]);
				if(position < shops.size()-1){
					View viewbg = new  View(getContext());
					viewbg.setBackgroundResource(R.drawable.bg_line);
					layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				}
				itemView.setTag(position);
				if(operationEnable){
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
			
		}
	
	}
}
