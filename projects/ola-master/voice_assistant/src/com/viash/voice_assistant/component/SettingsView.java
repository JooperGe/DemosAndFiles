package com.viash.voice_assistant.component;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;


public class SettingsView extends LinearLayout{

	protected RadioButton mRdoHttpServer;
	protected RadioButton mRdoSocketServer;
	
	protected RadioButton mRdoInternet;
	protected RadioButton mRdoIntranet;
	protected RadioButton mRdoCustom;
	protected EditText mEdtIntranetIp;
	protected EditText mEdtCustomIp;
	protected EditText mEdtPort;
	
	protected CheckBox mCbxAutoRecord;



	public SettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SettingsView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/*@Override
	protected void onFinishInflate() {
		final EditText edtIp = (EditText) this.findViewById(R.id.edt_setting_ip);
		String ip = SavedData.getmIP();
		edtIp.setText(ip);	    
	       
        InputFilter[] filters = new InputFilter[1]; 
        filters[0] = new InputFilter() { 
            @Override 
            public CharSequence filter(CharSequence source, int start, 
                    int end, Spanned dest, int dstart, int dend) { 
				if (end > start) {
					String destTxt = dest.toString();
					String resultingTxt = destTxt.substring(0, dstart)
							+ source.subSequence(start, end)
							+ destTxt.substring(dend);
					if (!resultingTxt
							.matches("^\\d{1,3}(\\."
									+ "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
						return "";
					} else {
						String[] splits = resultingTxt.split("\\.");
						for (int i = 0; i < splits.length; i++) {
							if (Integer.valueOf(splits[i]) > 255) {
								return "";
							}
						}
					}
				}
				return null;
            }
        }; 
        edtIp.setFilters(filters); 
        
        mCbxAutoRecord = (CheckBox) findViewById(R.id.cbx_auto_start_record);
       	mCbxAutoRecord.setChecked(SavedData.getmAutoStartRecord());

		super.onFinishInflate();
	}

	public String getIPAddress() {
		final EditText edtIp = (EditText) this.findViewById(R.id.edt_setting_ip);
		return edtIp.getText().toString();		
	}*/
	
	@Override
	protected void onFinishInflate() {
		mRdoHttpServer = (RadioButton) findViewById(R.id.rdo_http);
		mRdoSocketServer = (RadioButton) findViewById(R.id.rdo_socket);
		
		mRdoInternet = (RadioButton) findViewById(R.id.rdo_internet_ip);
		mRdoIntranet = (RadioButton) findViewById(R.id.rdo_intranet_ip);
		mRdoCustom = (RadioButton) findViewById(R.id.rdo_custom_ip);
		mEdtIntranetIp = (EditText) this.findViewById(R.id.edt_intranet_server_ip);
		mEdtCustomIp = (EditText) this.findViewById(R.id.edt_custom_ip);
		mEdtPort = (EditText) findViewById(R.id.edt_setting_port);
		
		String ipIntranet = SavedData.getmIntranetIp();
		mEdtIntranetIp.setText(ipIntranet);
		String ipCustom = SavedData.getmCustomIp();
		mEdtCustomIp.setText(ipCustom);
		
		String ip = SavedData.getmIP();
		if (SavedData.isHttpMode()) {
			mRdoHttpServer.setChecked(true);
			if(ip.equals(SavedData.INTERNET_SERVER_IP_HTTP))
				mRdoInternet.setChecked(true);
			else if(ip.equals(ipIntranet))
				mRdoIntranet.setChecked(true);
			else
				mRdoCustom.setChecked(true);
		}else {
			mRdoSocketServer.setChecked(true);
			if(ip.equals(SavedData.INTERNET_SERVER_IP))
				mRdoInternet.setChecked(true);
			else if(ip.equals(ipIntranet))
				mRdoIntranet.setChecked(true);
			else
				mRdoCustom.setChecked(true);
		}
		
		Integer port = SavedData.getmPort();
		mEdtPort.setText(port.toString());    
	       
        InputFilter[] filters = new InputFilter[1]; 
        filters[0] = new InputFilter() { 
            @Override 
            public CharSequence filter(CharSequence source, int start, 
                    int end, Spanned dest, int dstart, int dend) { 
				if (end > start) {
					String destTxt = dest.toString();
					String resultingTxt = destTxt.substring(0, dstart)
							+ source.subSequence(start, end)
							+ destTxt.substring(dend);
					if (!resultingTxt
							.matches("^\\d{1,3}(\\."
									+ "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
						return "";
					} else {
						String[] splits = resultingTxt.split("\\.");
						for (int i = 0; i < splits.length; i++) {
							if (Integer.valueOf(splits[i]) > 255) {
								return "";
							}
						}
					}
				}
				return null;
            }
        }; 
        mEdtCustomIp.setFilters(filters); 
        mEdtIntranetIp.setFilters(filters); 
        
        //mCbxAutoRecord = (CheckBox) findViewById(R.id.cbx_auto_start_record);
       	//mCbxAutoRecord.setChecked(SavedData.getmAutoStartRecord());
       	
       	if(GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_RELEASE)
       	{
       		mRdoCustom.setVisibility(View.GONE);
       		mEdtCustomIp.setVisibility(View.GONE);
       		mEdtPort.setVisibility(View.GONE);
       		mRdoHttpServer.setVisibility(View.GONE);
       		
       		View v = findViewById(R.id.grp_port);
       		if(v != null)
       			v.setVisibility(View.GONE);
       	}

		super.onFinishInflate();
	}
	
	public String getAndSaveIPAddress()
	{
		String ip = "";
		String tempip = null;
		if (SavedData.isHttpMode()) {
			tempip = SavedData.INTERNET_SERVER_IP_HTTP;
		}
		else {
			tempip = SavedData.INTERNET_SERVER_IP;
		}
		
		if(mRdoInternet.isChecked()) {
			ip = tempip;
		}
		else if(mRdoIntranet.isChecked()) {
			ip = mEdtIntranetIp.getText().toString();	
			SavedData.setmIntranetIp(ip);
		}
		else {
			ip = mEdtCustomIp.getText().toString();	
			SavedData.setmCustomIp(ip);
		}
		SavedData.setmIP(ip);
		return ip;
	}
	
	public int getAndSavePort()
	{
		String port = mEdtPort.getText().toString();
		int nPort = 0;
		if(port.length() > 0)
		{
			try
			{
				nPort = Integer.parseInt(port);
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		
		SavedData.setmPort(nPort);
		return nPort;
	}

	public boolean getAndSaveServerMode() {
		boolean serverMode = true; //http mode;
		
		if(mRdoHttpServer.isChecked())
		{
			serverMode = true;
		}		
		else if(mRdoSocketServer.isChecked())
		{
			serverMode = false;	
		}
		SavedData.setIsHttpMode(serverMode);
		return serverMode;
	}
	
	//public boolean getAutoStartRecord()
	//{
	//	return mCbxAutoRecord.isChecked();
	//}
}
