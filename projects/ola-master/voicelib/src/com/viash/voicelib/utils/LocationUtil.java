package com.viash.voicelib.utils;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class LocationUtil {
	public static Location getCurLocation(Context context)
	{		
		Location location = null;
		LocationManager loctionManager; 
        loctionManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
//        Criteria criteria = new Criteria(); 
//        criteria.setAltitudeRequired(false);//不要求海拔 
//        criteria.setBearingRequired(false);//不要求方位 
//        criteria.setCostAllowed(true);//允许有花费 
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
       // String provider = loctionManager.getBestProvider(criteria, true); 
        String provider = LocationManager.NETWORK_PROVIDER;
       	location = loctionManager.getLastKnownLocation(provider); 
       	
       	if(location == null)
       	{
       		provider = LocationManager.GPS_PROVIDER;
           	location = loctionManager.getLastKnownLocation(provider); 
       	}

        return location;
	}
}
