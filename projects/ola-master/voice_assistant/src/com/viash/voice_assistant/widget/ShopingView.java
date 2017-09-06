package com.viash.voice_assistant.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData.ShoppingItemJsonData.ShoppingItemData;

public class ShopingView extends LinearLayout {
	private List<ShoppingItemData> mLstItem;

	public ShopingView(Context context, List<ShoppingItemData> mLstItem) {
		super(context);
		this.mLstItem = mLstItem;
		setOrientation(LinearLayout.VERTICAL);
		init();
	}

	private void init() {
		if (mLstItem != null && mLstItem.size() > 0) {
			for (int i = 0; i < mLstItem.size(); i++) {
				ShoppingItemData data=mLstItem.get(i);
				View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_shoping_item, null);
				TextView tv_name = (TextView) itemView.findViewById(R.id.tv_name);
				TextView tv_price = (TextView) itemView.findViewById(R.id.tv_price);
				addView(itemView,android.widget.RelativeLayout.LayoutParams.FILL_PARENT,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
				if(i < mLstItem.size() -1 ){
					View viewLineBG  = new View(getContext());
					viewLineBG.setBackgroundResource(R.drawable.bg_line);
					addView(viewLineBG,LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
				}
				if(data.name != null)
					tv_name.setText(data.name);
				if(data.price != null)
					tv_price.setText(data.price);
			}
		}
	}
}
