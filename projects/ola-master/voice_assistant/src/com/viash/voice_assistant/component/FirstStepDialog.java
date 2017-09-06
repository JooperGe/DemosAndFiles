package com.viash.voice_assistant.component;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.viash.voice_assistant.R;

@SuppressLint("ParserError")
public class FirstStepDialog extends Dialog {
    Context context;
    private static final String SCHEME = "package";  
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";  
    private static final String APP_PKG_NAME_22 = "pkg";  
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings"; 
    private static String msPackageName;

    public FirstStepDialog(Context context) {
        super(context);
        this.context = context;
    }
    public FirstStepDialog(Context context, int theme){
        super(context, theme);
        this.context = context;
    }
    public FirstStepDialog(Context context, int theme, String packageName){
        super(context, theme);
        this.context = context;
        this.msPackageName = packageName;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.steponedialog);
        Button operatebtn=(Button)findViewById(R.id.f_operatebtn);
        operatebtn.setTextColor(Color.BLACK);
        operatebtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showInstalledAppDetails(context, FirstStepDialog.msPackageName);	
				cancel();
			}
		});
    }
    @SuppressLint("ParserError")
	public void showInstalledAppDetails(Context context,String packageName){  
        Intent intent = new Intent();  
        final int apiLevel = Build.VERSION.SDK_INT;  
        if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口  
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  
            Uri uri = Uri.fromParts(SCHEME, packageName, null);  
            intent.setData(uri);  
        } else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）  
            // 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。  
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22  
                    : APP_PKG_NAME_21);  
            intent.setAction(Intent.ACTION_VIEW);  
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,  
            		packageName);  
            intent.putExtra(appPkgName, packageName);  
        }  
         context.startActivity(intent);  
    } 
    
}
