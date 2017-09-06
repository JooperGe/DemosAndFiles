package com.viash.voice_assistant.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData.PoemJsonData.PoemData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 诗歌详情
 * @author Harlan Song
 * 2013-4-1
 */
@SuppressLint("ViewConstructor")
public class PoemView extends LinearLayout {
	private List<PoemData> mPoemData;
	private boolean isContentFormat = true;
	private static Handler mHandler;
	public PoemView(Context context, AttributeSet attrs,
			List<PoemData> mPoemData,Handler handler) {
		super(context, attrs);
		this.mPoemData = mPoemData;
		mHandler = handler;
		init();
	}

	public PoemView(Context context, List<PoemData> mPoemData) {
		super(context);
		this.mPoemData = mPoemData;
		init();
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		if (mPoemData != null && mPoemData.size() > 0) {
			for (int i = 0; i < mPoemData.size(); i++) {
				PoemData poemData=mPoemData.get(i);
				View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_poem_item, null);
				TextView tv_name = (TextView) itemView.findViewById(R.id.tv_name);
				TextView tv_author = (TextView) itemView.findViewById(R.id.tv_author);
				TextView tv_content = (TextView) itemView.findViewById(R.id.tv_content);
				//tv_content.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
				tv_name.setText(poemData.title);
				tv_author.setText("作者：" + poemData.author);
				String content_str =poemData.content;
				tv_content.setText(contentFormat(content_str));
				if(isContentFormat)
					tv_content.setGravity(Gravity.CENTER_HORIZONTAL);
				else
					tv_content.setGravity(Gravity.LEFT);
				addView(itemView,new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				itemView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessage(MsgConst.MSG_FORCE_STOP_TTS);
					}					
				});
			}
		}
	}
	
	private  String contentFormat(String content) {
		if (content == null || "".equals(content.trim()))
			return "";
		String result = "";
		int tag = 0;
		int sentenceLength = 0;
		List<String> list = new ArrayList<String>();
		for (int i = 1; i < content.length(); i++) {
			if ("，".equals(content.substring(i - 1, i))
					|| "。".equals(content.substring(i - 1, i))
					|| "！".equals(content.substring(i - 1, i))
					|| "？".equals(content.substring(i - 1, i))
					|| "；".equals(content.substring(i - 1, i))
					|| ",".equals(content.substring(i - 1, i))
					|| "!".equals(content.substring(i - 1, i))
					|| "?".equals(content.substring(i - 1, i))
					|| ";".equals(content.substring(i - 1, i))) {
				list.add(content.substring(tag, i));
				if (list.size() == 1)
					sentenceLength = list.get(0).length();
				if (sentenceLength != content.substring(tag, i).length()) {
					isContentFormat = false;
					break;
				}

				tag = i;
			}
		}
		if (tag < content.length()) {
			list.add(content.substring(tag, content.length()));
			if (sentenceLength != content.substring(tag, content.length()).length()) {
				isContentFormat = false;
			}
		}
		if (isContentFormat) {
			String contentFormat = "";
			for (int i = 1; i < list.size() + 1; i++) {
				contentFormat += list.get(i - 1);
				if(sentenceLength < 9){
					if (i % 2 == 0 && i != list.size())
						contentFormat += "\n";
				}else if(i != list.size() ){
					contentFormat += "\n";
				}
				
			}
			result = contentFormat;
		} else {
			result = content;
		}
		return result;
	}

}
