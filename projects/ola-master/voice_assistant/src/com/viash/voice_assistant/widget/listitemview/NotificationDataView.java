package com.viash.voice_assistant.widget.listitemview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.CommunicationData.NotifyData;

/**
 * NotificationDataView
 * @author Loneway
 * @createDate 2013-11-14
 */
@SuppressLint("ViewConstructor")
public class NotificationDataView extends RelativeLayout {
	private WebView webView;
	private View view;
	public NotificationDataView(Context context, NotifyData data,boolean operationEnable, Handler handler) {
		super(context);
		view = LayoutInflater.from(getContext()).inflate(R.layout.layout_server_notification_item, this);
		webView = (WebView) view.findViewById(R.id.webView);
		String url = data.mUrl;
		if(url != null)
		  webView.loadUrl(url);
	}
}
