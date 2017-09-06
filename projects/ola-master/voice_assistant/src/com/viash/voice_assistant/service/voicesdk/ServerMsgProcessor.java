package com.viash.voice_assistant.service.voicesdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.tts.TtsService.Tts;
import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.receiver.SmsReceiver;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.SdkActionData;
import com.viash.voicelib.data.SdkCommandData;
import com.viash.voicelib.data.PreFormatData.SMSJsonData;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgDumpResponse;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.msg.MsgServerQuery;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.CallUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.CallUtil.SmsData;

/**
 * 
 * 拆分 主 service 功能
 * 
 * @author fenglei
 *
 */
public class ServerMsgProcessor {

	private static final boolean DEBUG = true;
	private static final String TAG = "ServerMsgProcessor";
	
	private VoiceSdkService mainService;
	private static ServerMsgProcessor _instance = null;
	
	private ServerMsgProcessor(VoiceSdkService service){
		this.mainService = service;
	}
	public static ServerMsgProcessor init(VoiceSdkService s){
		if(null == _instance)
			_instance = new ServerMsgProcessor(s);
		return _instance;
	}
	
	public static ServerMsgProcessor getInstance(){
		if(null == _instance)
			throw new RuntimeException("please init ServerMsgProcessor");
		return _instance;
	}

	public void processServerMsg(MsgRaw msg) {

		switch (msg.getmId()) {
		case MsgConst.TS_S_PROMPT:
			mainService.mSendingPrompt = null;
			MsgAnswer msgAnswer = (MsgAnswer) msg;
			processServerAnswer(msgAnswer);
			break;
		case MsgConst.TS_S_DUMP:
			MsgDumpResponse msgDump = new MsgDumpResponse(msg);
			String fileName = msgDump.saveToFile(mainService);

			if (fileName != null) {
				CustomToast.makeToast(mainService, mainService.getString(R.string.save_file_in)
						+ fileName);// ,
				// Toast.LENGTH_LONG).show();
			} else {
				CustomToast.makeToast(mainService,
						mainService.getString(R.string.save_file_failed));// Toast.LENGTH_SHORT).show();
			}
			break;
		case MsgConst.TS_S_QUERY:
			mainService.mSendingPrompt = null;
			MsgServerQuery msgQuery = (MsgServerQuery) msg;
			processServerQuery(msgQuery);
			break;
		default:
			break;
		}
	}

	public void processServerQuery(MsgServerQuery msgQuery) {
		JSONObject obj = msgQuery.getQueryData();
		if (obj != null) {
			String queryType = obj.optString("type");
			if (queryType != null) {
				new Thread(new ProcessServerQueryThread(mainService, queryType, obj))
						.start();
			}
		}
	}

	public void processServerAnswer(MsgAnswer msgAnswer) {
		JSONObject obj = msgAnswer.getJsonData();
		if (obj != null) {
			String data_type = obj.optString("data_type");
			if (data_type != null && data_type.equals("answer")) {
				CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_RESPONSE, obj);
			} else {
				mainService.switchToSpecialStatus(mainService.SPECIAL_STATUS_NORMAL);

				CommunicationData commData = msgAnswer
						.getCommunicationData(mainService);
				mainService.mLatestServerData = commData;

				if (commData != null) {
					boolean isSdkCommand = false;
					boolean isActionCommand = false;
					List<BaseData> lstData = commData.getLstData();

					if (lstData != null) {
						for (BaseData baseData : lstData) {
							if (baseData instanceof SdkCommandData) {
								isSdkCommand = true;
								processSpecialSdkCommandData((SdkCommandData) baseData);
							}

							if (baseData instanceof SdkActionData) {
								isActionCommand = true;
								processSpecialSdkActionData((SdkActionData) baseData);
							}
						}

						if (isActionCommand) {
							if (mainService.mVoiceSdkUi != null) {
								mainService.mVoiceSdkUi.hideUI(false);
							}
						} else if (isSdkCommand) {
							if (mainService.mVoiceSdkUi != null) {
								mainService.mVoiceSdkUi.hideUI(false);
							}
						} else {
							if (mainService.mVoiceSdkUi != null
									&& mainService.mVoiceSdkUi.isVoiceViewVisible()) {
								mainService.mVoiceSdkUi.newServerData(commData);
							} else {
								if (mainService.floatViewIdle != null) {
									if (!mainService.floatViewIdle.isHide()) {
										if (mainService.isRunAppDirect(commData)) {
											return;
										} else
											mainService.floatViewIdle
													.startGuideActivity(commData);
										return;
									}
								}
								CallClientHelper.getInstance().sendBackToClient(
										MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
										commData);
							}

							String tts = "";

							mainService.mNeedAnswer = false;
							boolean isSilentInfoData = false;
							boolean isNeedUsePriority = false;
							for (BaseData baseData : lstData) {
								if (!mainService.mNeedAnswer) {
									if (commData.isSilentInfoMsg()) {
										mainService.mNeedAnswer = true;
										isSilentInfoData = true;
									} else {
										mainService.mNeedAnswer = baseData
												.isDataNeedAnswer();
									}
								}
								if (baseData.getTtsString() != null)
									tts += baseData.getTtsString();

								baseData.doAction(mainService, mainService.mHandler);
								String actionResult = baseData
										.getActionResult(mainService);
								if (actionResult != null) {
									tts += actionResult;
								}

								if (baseData instanceof ConfirmData) {
									processSpecialConfirmData((ConfirmData) baseData);
								}

								if (baseData instanceof PreFormatData) {
									processSpecialPreformatData((PreFormatData) baseData);
									int type = ((PreFormatData) baseData)
											.getmDataType();
									if ((type == PreFormatData.TYPE_HTML)
											|| (type == PreFormatData.JSON_POEM)
											|| (type == PreFormatData.JSON_JOKE)
											|| (type == PreFormatData.JSON_BAIKE_OTHER)
											|| (type == PreFormatData.JSON_NEWS))
										isNeedUsePriority = true;
								}

								if (baseData instanceof OptionData) {
									int type = ((OptionData) baseData)
											.getOptionId();
									if (type == OptionData.OPTION_NEWS_NAME)
										isNeedUsePriority = true;
								}
							}

							if (tts.length() > 0) {
								parseTtsData(tts);
								if (isNeedUsePriority)
									mainService.playTts(0, 1);
								else
									mainService.playTts(0);
							} else {
								if (SavedData.getmAutoStartRecord()
										&& mainService.mNeedAnswer && !isSilentInfoData) {
									mainService.startCapture();
								}
							}
						}
					}
				}

				mainService.setWatiServerResponse(false);

				if (mainService.mProcessState != MsgConst.UI_STATE_SPEAKING)
					CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} else {
			CustomToast
					.makeToast(
							mainService,
							mainService.getString(R.string.voiceassistantservice_server_data_error));// ,
																							// Toast.LENGTH_SHORT).show();
		}
	}
	
	

	public void parseTtsData(String ttsData) {
		// ttsData = "this is a |*com1*|part1|*com1*|part2|*com1*|part3";
		ttsData = ttsData.replace(
				mainService.getString(R.string.voiceassistantservice_ola),
				mainService.getString(R.string.voiceassistantservice_ola_nick));
		int indexStart = 0;
		int indexEnd = 0;
		int indexCurrent = 0;
		String commentStart = "[*";
		String commentEnd = "*]";
		mainService.mTtsIndex = 0;
		mainService.mLstTts.clear();
		mainService.mLstTtsFlag.clear();

		indexStart = ttsData.indexOf(commentStart, indexCurrent);
		if (indexStart > 0) {
			mainService.mLstTts.add(ttsData.substring(indexCurrent, indexStart));
			mainService.mLstTtsFlag.add(null);
			indexCurrent = indexStart;
		} else if (indexStart == -1) {
			mainService.mLstTts.add(ttsData.substring(indexCurrent));
			mainService.mLstTtsFlag.add(null);
			indexCurrent = ttsData.length();
		}

		while (indexCurrent < ttsData.length()) {
			indexStart = indexCurrent;
			indexEnd = ttsData.indexOf(commentEnd, indexStart + 2);
			if (indexEnd == -1) {
				mainService.mLstTts.add(ttsData.substring(indexCurrent));
				mainService.mLstTtsFlag.add(null);
				break;
			} else {
				indexCurrent = ttsData.indexOf(commentStart, indexEnd + 2);
				mainService.mLstTtsFlag.add(ttsData.substring(indexStart + 2, indexEnd));
				if (indexCurrent != -1) {
					mainService.mLstTts.add(ttsData.substring(indexEnd + 2, indexCurrent));
				} else {
					mainService.mLstTts.add(ttsData.substring(indexEnd + 2));
					break;
				}
			}

		}
	}

	public void processSpecialSdkCommandData(SdkCommandData sdkData) {
		if (DEBUG)
			Log.d(TAG,
					"SdkCommand: " + sdkData.getCommand() + " - ["
							+ sdkData.getParam1() + "]");
		// send back to third-party
		Bundle bundle = new Bundle();
		String command = sdkData.getCommand();
		String param1 = sdkData.getParam1();
		String param2 = sdkData.getParam2();
		if (command != null) {
			bundle.putString("commandname", command);
			if (param1 != null)
				bundle.putString("param1", param1);
			if (param2 != null)
				bundle.putString("param2", param2);
			CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_SDKCOMMAND_RESPONSE,
					bundle);
		}
	}

	public void processSpecialSdkActionData(SdkActionData sdkData) {
		String action = sdkData.getActionName();
		String param = sdkData.getParam();
		if (action != null) {
			ComponentName component = null;
			Intent intent = new Intent();
			intent.setAction(sdkData.getActionName());

			List<ComponentName> lstComponent = AppUtil.findIntentByActionName(
					mainService, action);
			if (lstComponent != null && lstComponent.size() > 0) {
				component = lstComponent.get(0);
				if (lstComponent.size() > 1) {
					ComponentName componentTop = AppUtil.findTopApp(mainService);
					if (componentTop != null) {
						for (int i = 0; i < lstComponent.size(); i++) {
							if (lstComponent.get(i).equals(componentTop)) {
								component = lstComponent.get(i);
								break;
							}
						}
					}
				}
			}

			if (component != null) {
				intent = new Intent();
				intent.setComponent(component);
				intent.setAction(sdkData.getActionName());

				if (param != null)
					intent.putExtra("json_data", param);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.addCategory(Intent.CATEGORY_DEFAULT);

				try {
					mainService.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				Tts.playText(mainService, mainService.getString(R.string.no_app_for_action), null);
			}
		}
	}

	public void processSpecialConfirmData(ConfirmData confirmData) {
		if (confirmData.getType() == ConfirmData.TYPE_SMS) {
			mainService.switchToSpecialStatus(mainService.SPECIAL_STATUS_SMS);
		}
	}

	public void processSpecialPreformatData(PreFormatData preformatData) {
		if (preformatData.getmDataType() == PreFormatData.JSON_SMS) {
			SMSJsonData jsonSms = (SMSJsonData) preformatData.getJsonData();
			if (jsonSms != null && jsonSms.mLstSms != null) {
				List<Long> lstSmsIds = new ArrayList<Long>();
				for (SmsData smsData : jsonSms.mLstSms) {
					if (smsData.getmType() == SmsData.TYPE_UNREAD) {
						lstSmsIds.add(smsData.getmId());
					}
				}

				if (lstSmsIds.size() > 0) {
					CallUtil.updateSms2Read(mainService, lstSmsIds);
				}
			}
		}
	}


	public void processQueryAnswer(String type, JSONArray array,
			int total_num) {
		JSONObject objAnswer = new JSONObject();
		try {
			objAnswer.put("type", type);
			if (array != null)
				objAnswer.put("result", array);
			if (total_num != 0)
				objAnswer.put("total_num", total_num);
			processQueryAnswer(objAnswer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void processQueryAnswer(JSONObject objAnswer) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("data_type", "answer");
			jsonObj.put("data", objAnswer);

			Log.i(TAG, jsonObj.toString());

			MsgAsk msgAsk = new MsgAsk(jsonObj, MsgConst.TS_C_ANSWER);
			byte[] data = msgAsk.prepareRawData();
			if (data != null) {
				mainService.mSocketUtil.sendRawMessage(data, true);
				mainService.setWatiServerResponse(true);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void processVoiceMsg(String sVoice) {
		JSONObject jsonObj = new JSONObject();
		try {
			JSONObject jsonSpeech = new JSONObject();
			jsonSpeech.put("text", sVoice);
			jsonSpeech.put("input_type", mainService.mInputType);
			if ((HelpStatisticsUtil.currentType != null)
					|| (HelpStatisticsUtil.helpType != null)) {
				jsonSpeech.putOpt(HelpStatisticsUtil.HELP_SECOND,
						HelpStatisticsUtil.getHelpSecondJsonObject());
				HelpStatisticsUtil.currentType = null;
				HelpStatisticsUtil.touchIndex = -1;
			}
			if (FloatViewIdle.IS_RECORD_FROM_FLOAT_VIEW_IDLE) {
				jsonSpeech.put("input_mode", 1);// floatview input type
				FloatViewIdle.IS_RECORD_FROM_FLOAT_VIEW_IDLE = false;
			}
			ParamHelper.getInstance().appendLocationInfo(jsonSpeech);
			jsonObj.put("data_type", "stt");
			jsonObj.put("data", jsonSpeech);
			ParamHelper.getInstance().appendModificationInfo(jsonSpeech);
			ParamHelper.getInstance().appendTtsHint(jsonSpeech);
			// appendTestSdkCommand(jsonSpeech);
			ParamHelper.getInstance().appendSdkCommand(jsonSpeech);
			if (IncomingCallShareState.isIncomgingCall()) {
				JSONObject objSpecialEvent = new JSONObject();
				objSpecialEvent.put("event_type", 1);// 1.incoming call
														// 2,receive message.
				objSpecialEvent.put("caller_name",
						IncomingCallShareState.getName());
				objSpecialEvent.put("caller_number",
						IncomingCallShareState.getNumber());
				jsonSpeech.put("special_event", objSpecialEvent);
			}

			if (SmsReceiver.isReplySMS()) {
				JSONObject objSpecialEvent = new JSONObject();
				objSpecialEvent.put("event_type", 2);// 1.incoming call
														// 2,receive message.
				objSpecialEvent
						.put("caller_name", SmsReceiver.getContactName());
				objSpecialEvent.put("caller_number",
						SmsReceiver.getPhoneNumber());
				jsonSpeech.put("special_event", objSpecialEvent);
			}

			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null) {
				mainService.mSendingPrompt = data;
				if (mainService.mSocketUtil != null)
					mainService.mSocketUtil.sendRawMessage(data, true);

				CommunicationData commData = new CommunicationData(
						DataConst.FROM_MIC);
				commData.setDisplayText(sVoice);

				if (mainService.mVoiceSdkUi != null && mainService.mVoiceSdkUi.isVoiceViewVisible()) {
					mainService.mVoiceSdkUi.newVoiceData(commData);
				} else {
					CallClientHelper.getInstance().sendBackToClient(
							MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
							commData);
				}

				mainService.setWatiServerResponse(true);

				CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
