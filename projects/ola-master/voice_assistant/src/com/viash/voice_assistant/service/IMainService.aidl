package com.viash.voice_assistant.service;
import com.viash.voice_assistant.service.LocationActionData;

interface IMainService{
	boolean addLocationAlert(in LocationActionData action);
	List<LocationActionData> queryLocationAlert();
	boolean deleteLocationAlert(in int[] ids);
	
	void trigger(String param);  // called by app to show the input interface
}