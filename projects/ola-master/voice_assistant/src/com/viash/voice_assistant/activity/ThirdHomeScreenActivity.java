package com.viash.voice_assistant.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.viash.voice_assistant.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ThirdHomeScreenActivity extends Activity {
    private static final String TAG = "ThirdHomeScreenActivity";

    private static ArrayList<ResolveInfo> mApplications2 = null;  
    private GridView mGrid;
    private PackageManager manager;
    
    @SuppressWarnings("unused")
	private View mShowApplications;
    @SuppressWarnings("unused")
	private CheckBox mShowApplicationsCheck;
	private TextView title_back;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.thirdhomelayout);
        manager = getPackageManager();
       	title_back = (TextView) this.findViewById(R.id.setting_title);
    	title_back.setOnClickListener(new OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			ThirdHomeScreenActivity.this.finish();				
    		}
    		
    	});
    	loadApplications();
        bindApplications();
        bindButtons();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        }
    }
    
    private void bindApplications() {
        if (mGrid == null) {
            mGrid = (GridView) findViewById(R.id.all_apps);
        }
        mGrid.setAdapter(new ApplicationsAdapter(this, mApplications2));
        mGrid.setSelection(0);
    }


    private void bindButtons() {
        mShowApplications = findViewById(R.id.show_all_apps);
        mShowApplicationsCheck = (CheckBox) findViewById(R.id.show_all_apps_check);

        mGrid.setOnItemClickListener(new ApplicationLauncher());
    }

    /**
     * Loads the list of installed applications in mApplications.
     */
    @SuppressLint("Recycle")
	private void loadApplications() {
        if (mApplications2 != null) {
        	mApplications2.clear();
        }else {
            mApplications2 = new ArrayList<ResolveInfo>();
        }
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);

        final List<ResolveInfo> apps = (ArrayList<ResolveInfo>) manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
        if (apps != null) {
            final int count = apps.size();

            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                             

                Parcel dest = Parcel.obtain();
                info.writeToParcel(dest, 0);
                Log.i(TAG, info.toString());
               
                dest.setDataPosition( dest.dataSize()- 4);
                int system = dest.readInt();
                Log.i(TAG, "" + dest.dataSize() + "      "+ system);
               
                if (system == 1) {
                	continue;
                }
                if (info.activityInfo.packageName.equals("com.viash.voice_assistant") ) {
	               	continue;
                }                
                mApplications2.add(info);
            }
        }
    }

    private class ApplicationsAdapter extends ArrayAdapter<ResolveInfo> {
        private Rect mOldBounds = new Rect();

        public ApplicationsAdapter(Context context, ArrayList<ResolveInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = mApplications2.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.application, parent, false);
            }

            Drawable icon = info.activityInfo.loadIcon(manager);
    
            int width = 42;
            int height = 42;

            final int iconWidth = icon.getIntrinsicWidth();
            final int iconHeight = icon.getIntrinsicHeight();

            if (icon instanceof PaintDrawable) {
            	PaintDrawable painter = (PaintDrawable) icon;
            	painter.setIntrinsicWidth(width);
            	painter.setIntrinsicHeight(height);
            }

            if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
            	final float ratio = (float) iconWidth / iconHeight;

            	if (iconWidth > iconHeight) {
            		height = (int) (width / ratio);
            	} else if (iconHeight > iconWidth) {
            		width = (int) (height * ratio);
            	}

            	final Bitmap.Config c =
            			icon.getOpacity() != PixelFormat.OPAQUE ?
            					Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            	final Bitmap thumb = Bitmap.createBitmap(width, height, c);
            	final Canvas canvas = new Canvas(thumb);
            	canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
            	mOldBounds.set(icon.getBounds());
            	icon.setBounds(0, 0, width, height);
            	icon.draw(canvas);
            	Log.i(TAG, "Bottom:"+icon.getBounds().bottom + "  Left:"+ icon.getBounds().left + "  Right:" + icon.getBounds().right + "  Top:" + icon.getBounds().top);
            	//icon.setBounds(mOldBounds);
            }
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
            imageView.setImageDrawable(icon);
            final TextView textView = (TextView) convertView.findViewById(R.id.label_app);
            textView.setText(info.loadLabel(manager));
            Log.i(TAG, (String) info.loadLabel(manager));

            return convertView;
        }
    }
    private class ApplicationLauncher implements AdapterView.OnItemClickListener {
        @SuppressWarnings("rawtypes")
		public void onItemClick(AdapterView parent, View v, int position, long id) {
        	ResolveInfo app = (ResolveInfo) parent.getItemAtPosition(position);
        	
        	PackageManager manager = getPackageManager();

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

            ComponentName comp = null;
            if (apps != null) {
                final int count = apps.size();

                for (int i = 0; i < count; i++) {
                    ResolveInfo info = apps.get(i);
                    if (info.activityInfo.packageName.equals(app.activityInfo.packageName)) {
                    	comp = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                    }
                }
            }

            Intent intent = new Intent();
            if (comp == null) {
            	comp = new ComponentName(app.activityInfo.packageName, app.activityInfo.name);
            }
            intent.setComponent(comp); 
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); 
			startActivity(intent);	
        }
    }
}
