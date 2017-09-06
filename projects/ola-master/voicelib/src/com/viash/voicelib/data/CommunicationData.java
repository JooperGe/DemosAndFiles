package com.viash.voicelib.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.viash.voicelib.utils.HelpStatisticsUtil;

import android.content.Context;

public class CommunicationData {
	
	protected int mFrom = DataConst.FROM_SERVER;
	
	List<BaseData> mLstData 			= null;
	
	private SentenceData 	mSentence 	= null;
	private QuestionData 	mQuestion 	= null;
	private OptionData 		mOption		= null;
	private AppData			mApp		= null;
	private ConfirmData		mConfirm	= null;
	private List<PreFormatData>	mPreFormatList	= null;
	private SdkCommandData	mSdkCommand	= null;
	private SilentInfoData	mSilentInfo	= null;
	private NotifyData 		mNotifyData = null;
	private SdkActionData	mActionData = null;
	private HelpData        mHelpData   = null;
	
	public class NotifyData extends BaseData
	{
		public String mTitle;
		public String mDetail;
		public String mUrl;
	}
	
	public CommunicationData(int from)
	{
		mFrom = from;
	}

	public int getFrom() {
		return mFrom;
	}

	public void setFrom(int mFrom) {
		this.mFrom = mFrom;
	}

	public String getDisplayText() {
		if (mSentence == null) {
			return null;
		}
		
		return mSentence.getDisplayString();
	}
	
	public void setDisplayText(String mDisplayText) {
		if(mDisplayText==null)return;
		if (mSentence == null) {	
			mSentence = new SentenceData();
		}
		mSentence.updateDisplayString(mDisplayText); 
	}

	public String getTtsText() {
		if (mSentence == null) {
			return null;
		}
		return mSentence.getTtsString();
	}
	
	public NotifyData getNotifyData() {
		return mNotifyData;
	}

	public void setNotifyInfo(String title, String url)
	{
		mNotifyData = new NotifyData();
		mNotifyData.mTitle = title;
		mNotifyData.mUrl = url;
	}
	
	public boolean setJsonString(Context context, String str)
	{
		boolean ret = false;
		JSONObject jsonObj = null;
		
	/*	str = "{\"Just Talk Dialog Outputs\":";
		str += "[{\"App\":{\"App Name\":\"SetCalendarEvent\"";
		str += ",\"RepeatRule\":{";
		str += "\"UNTIL\":\"1354599380000\"";
		str += ",\"FREQ\":\"DAILY\"";
		str += "}";
		str += ",\"StartTime\":\"1352599380000\"";
		str += ",\"Title\":\"这是标题\"";
		str += ",\"Location\":\"这是位置\"";
		str += ",\"Description\":\"这是描述\"";
		str += ",\"HasAlarm\": 1";
		str += "}}]}";*/
		
		///str = "{\"Just Talk Dialog Outputs\":[{\"App\":{\"App Name\":[\"modify_calendar_event_by_id\"],\"id\":[\"762\"],\"StartTime\":[\"1359439200000\"],\"Title\":[\"去上海\"],\"Duration\":[\"0000\"],\"Location\":[\"\"],\"Description\":[\"\"],\"attendees\":[\"老王\"],\"AdvanceTime\":[\"0\"]}},{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"calendar\",\"data_obj\":[{    \"id\": \"762\",    \"start_time\": \"1359439200000\",    \"end_time\": \"1359439200000\",    \"title\": \"\u53BB\u4E0A\u6D77\",    \"attendees\": [\"\u8001\u738B\"],    \"location\": \"\",    \"description\": \"\",    \"all_day\": \"\",    \"advance_time\": \"0\",    \"repeat_rule\": \"\"}]}}},{\"Sentence\":{\"Present\":\"2\",\"Content\":\"\"}},{\"Sentence\":{\"Present\":\"2\",\"Content\":\"修改成功：\"}},{\"Sentence\":{\"Present\":\"1\",\"Content\":\"修改成功：\"}},{\"Sentence\":{\"Present\":\"2\",\"Content\":\"修改后变成: 明天下午2点钟和老王去上海\"}},{\"Sentence\":{\"Present\":\"1\",\"Content\":\"修改后变成: 明天下午两点钟和老王去上海\"}}]}";

		//str = "{\"Just Talk Dialog Outputs\":[{\"Sentence\":{\"Present\":\"3\",\"Content\":\"安徽卫视有播。\"}},{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"tv_program\",\"data_obj\":[{\"time\":[\"00:04\",\"00:50\",\"02:19\",\"02:32\",\"03:14\",\"04:00\",\"05:19\",\"06:30\",\"07:32\",\"08:19\",\"09:08\",\"10:51\",\"11:45\",\"12:45\",\"13:35\",\"14:31\",\"15:27\",\"16:21\",\"17:14\",\"18:00\",\"18:30\",\"18:55\",\"19:00\",\"19:34\",\"20:26\",\"21:17\",\"21:24\",\"22:13\",\"23:04\",\"23:58\"],\"name\":[\"非常静距离\",\"超级演说家\",\"新安夜空,旅游风景区天气预报\",\"无法逃脱(17)\",\"无法逃脱(18)\",\"飞跃童真\",\"超级新闻场\",\"超级新闻场\",\"甄嬛传\",\"甄嬛传\",\"甄嬛传\",\"甄嬛传\",\"男生女生向前冲\",\"男生女生向前冲\",\"善良的男人(19)\",\"善良的男人(20)\",\"辣妈正传(1)\",\"辣妈正传(2)\",\"辣妈正传(3)\",\"每日新闻报\",\"安徽新闻联播\",\"天气预报\",\"转播中央台新闻联播\",\"辣妈正传(23)\",\"辣妈正传(24)\",\"第一剧场明日精彩\",\"爱传万家说出你的故事\",\"糟糠之妻俱乐部2(17)\",\"糟糠之妻俱乐部2(18)\",\"非常静距离\"],\"is_highlight\":3840}],\"description_obj\":{\"station_name\":\"安徽卫视\",\"filters\":\"1379433600000$$1379520000000$$1379606400000$$1379692800000$$1379779200000$$1379692800000$$1379606400000\",\"filter\":\"5\",\"url\":\"\",\"logo\":\"\"}}}}]}";
		
		//str = "{{\"Just Talk Dialog Outputs\":[{\"Selection\":{\"SelectionBody\":[\"穿越火线$$http://www.bobo1314.com/poster/vfolder//upimage/201306//20130621100140_120_160.jpg$$0$$http://www.bobo1314.com/wap/PlayMovieWap.aspx?hid=3122143&modular=10\",\"僵尸世界大战[B...$$http://www.yes80.net/poster/vfolder//upimage/201306//20130623160414_120_160.jpg$$0$$http://www.yes80.net/wap/PlayMovieWap.aspx?hid=3311328&modular=10\",\"了不起的盖茨比...$$http://www.bobo1314.com/poster/vfolder//upimage/201305//20130524113720_120_160.jpg$$0$$http://www.bobo1314.com/wap/PlayMovieWap.aspx?hid=3104755&modular=10\",\"环太平洋$$http://www.19taoba.com/poster/vfolder//upimage/201306//20130606175019_120_160.jpg$$0$$http://www.19taoba.com/wap/PlayMovieWap.aspx?hid=3210285&modular=10\",\"被偷走的那五年$$http://www.100soo.cn/poster/vfolder//upimage/201308//20130815100606_120_160.jpg$$0$$http://www.100soo.cn/wap/PlayMovieWap.aspx?hid=3340648&modular=10\",\"一路向西[国语]$$http://www.19taoba.com/poster/201208/13/20120813103518_120_160.jpg$$0$$http://www.19taoba.com/wap/PlayMovieWap.aspx?hid=2816052&modular=10\"],\"selection_id\":\"120001\",\"Display\":\"哦啦为你找到了6个，请问想看哪一个呢？\",\"Speak\":\"哦啦为你找到了[n2]6[n0]个，请问想看哪一个呢？\"}}}]}}";
		 
		//str ="{\"Just Talk Dialog Outputs\":[{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"bus_info\",\"data_obj\":[{\"bus_start\":\"南浦大桥\",\"bus_end\":\"田林路合川路\",\"early_bus_up\":\"04:05\",\"last_bus_up\":\"23:00\",\"stop_names_up\":[\"南浦大桥\",\"国货路海潮路\",\"斜土东路西藏南路\",\"斜土路制造局路\",\"斜土路蒙自路\",\"斜土路打浦路\",\"斜土路瑞金南路\",\"斜土路大木桥路\",\"斜土路枫林路\",\"斜土路东安路\",\"斜土路宛平南路\",\"南丹东路天钥桥路\",\"天钥桥路南丹路\",\"上海体育馆\",\"中山西路漕溪北路\",\"中山西路宜山路\",\"第六人民医院\",\"柳州路宜山路\",\"田林路柳州路\",\"田林路桂林路\",\"田林路虹漕路\",\"田林路桂平路\",\"宜山路虹许路\",\"古美路宜山路\",\"古美路田林路\",\"田林路莲花路\",\"田林路合川路\"],\"mileage_up\":\"15.0公里\",\"early_bus_down\":\"04:30\",\"last_bus_down\":\"22:50\",\"stop_names_down\":[\"田林路合川路\",\"田林路莲花路\",\"古美路田林路\",\"古美路宜山路\",\"宜山路虹许路\",\"田林路桂平路\",\"田林路虹漕路\",\"桂林路田林路\",\"桂林路宜山路\",\"第六人民医院\",\"中山西路宜山路\",\"零陵路上海体育馆\",\"零陵路双峰北路\",\"宛平南路斜土路\",\"斜土路东安路\",\"斜土路枫林路\",\"斜土路大木桥路\",\"斜土路瑞金南路\",\"斜土路打浦路\",\"斜土路鲁班路\",\"斜土路蒙自路\",\"斜土路制造局路\",\"斜土东路西藏南路\",\"国货路南车站路\",\"国货路海潮路\",\"陆家浜路海潮路\",\"南浦大桥\"],\"mileage_down\":\"15.2公里\"}],\"description_obj\":{\"bus_number\":\"89路空调\",\"city\":\"上海市\",\"filter\":\"0\",\"filters\":[\"上行\",\"下行\"]}}}}]}";

		//str= "{\"Just Talk Dialog Outputs\":[{\"Sentence\":{\"Present\":\"3\",\"Content\":\"主人，请看屏幕显示：\",\"tag\":\"poi\"}},{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"bus_info\",\"data_obj\":[{\"bus_start\":[\"广兰路\",\"广兰路\",\"浦东国际机场\",\"徐泾东\"],\"bus_end\":[\"浦东国际机场\",\"徐泾东\",\"广兰路\",\"广兰路\"],\"stop_names\":[\"广兰路\",\"广兰路\",\"浦东国际机场-海天三路\",\"徐泾东\"],\"early_bus\":[\"06:00\",\"05:30\",\"06:00\",\"05:30\"],\"last_bus\":[\"22:00\",\"22:45\",\"22:00\",\"22:45\"],\"mileage\":[\"24.0公里\",\"37.3公里\",\"24.0公里\",\"37.3公里\"]}],\"description_obj\":{\"bus_number\":\"地铁2号线\",\"city\":\"上海市\",\"filter\":\"0\",\"filters\":[\"广兰路-浦东国际机场\",\"广兰路-徐泾东\",\"浦东国际机场-广兰路\",\"徐泾东-广兰路\"]}}}}]}";
		
		//str = "{\"Just Talk Dialog Outputs\":[{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"exchange_rate\", \"data_obj\":[{\"target_currency\":[\"6.13300人民币\", \"0.65730英镑\", \"0.76560欧元\", \"99.81450日元\", \"7.75530港币\", \"7.98820澳门元\", \"1.05200加元\", \"1.09210澳大利亚元\", \"1.26700新加坡元\", \"43.34500菲律宾比索\"]}],\"description_obj\":{\"source_currency\": \"1美元\"}}}}	]}";
		
		//str = "{\"Just Talk Dialog Outputs\":[{\"Sentence\":{\"Present\":\"3\",\"Content\":\"双截棍的歌词如下：\",\"tag\":\"musicplay\"}},{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"lyrics\",\"data_obj\":[{\"lyrics\":\"岩烧店的烟味弥漫 隔壁是国术馆\n店里面的妈妈桑 茶道 有三段\n教拳脚武术的老板 练铁沙掌 耍杨家枪\n硬底子功夫最擅长 还会金钟罩铁步衫\n他们儿子我习惯 从小就耳濡目染\n什么刀枪跟棍棒 我都耍的有模有样\n什么兵器最喜欢 双截棍柔中带刚\n想要去河南嵩山 学少林跟武当\n干什么（客） 干什么（客）\n呼吸吐纳心自在\n干什么（客） 干什么（客）\n气沉丹田手心开\n干什么（客） 干什么（客）\n日行千里系沙袋\n飞檐走壁莫奇怪 去去就来\n一个马步向前 一记左钩拳 右钩拳\n一句惹毛我的人有危险 一再重演\n一根我不抽的菸 一放好多年\n它一直在身边\n干什么（客） 干什么（客）\n我打开任督二脉\n干什么（客） 干什么（客）\n东亚病夫的招牌\n干什么（客） 干什么（客）\n已被我一脚踢开\n\n快使用双截棍 哼哼哈兮\n快使用双截棍 哼哼哈兮\n习武之人切记 仁者无敌\n是谁在练太极 风生水起\n快使用双截棍 哼哼哈兮\n快使用双截棍 哼哼哈兮\n如果我有轻功 飞檐走壁\n为人耿直不屈 一身正气\n\n他们儿子我习惯 从小就耳濡目染\n什么刀枪跟棍棒 我都耍的有模有样\n什么兵器最喜欢 双截棍柔中带刚\n想要去河南嵩山 学少林跟武当\n\n快使用双截棍 哼哼哈兮\n快使用双截棍 哼哼哈兮\n习武之人切记 仁者无敌\n是谁在练太极 风生水起\n快使用双截棍 哼哼哈兮\n快使用双截棍 哼哼哈兮\n如果我有轻功 飞檐走壁\n为人耿直不屈 一身正气\n他们儿子我习惯 从小就耳濡目染\n什么刀枪跟棍棒 我都耍的有模有样\n什么兵器最喜欢 双截棍柔中带刚\n想要去河南嵩山 学少林跟武当\n\n快使用双截棍 哼哼哈兮\n快使用双截棍 哼哼哈兮\n习武之人切记 仁者无敌\n是谁在练太极 风生水起\n快使用双截棍 哼哼哈兮\n快使用双截棍 哼哼哈兮\n如果我有轻功 飞檐走壁\n为人耿直不屈 一身气\n\n快使用双截棍 哼\n我用手刀防御 哼\n\n漂亮的回旋踢\n\n\"}]}}}]}";
		
		//str = "{\"Just Talk Dialog Outputs\":[{\"Selection\":{\"SelectionBody\":[\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\",\"毛泽东$$国家主席$$中国$$1949年9月$$1959年4月$$第一届\"],\"selection_id\":\"150001\",\"Display\":\"合起来有6个，需要为你打开哪一个？\",\"Speak\":\"合起来有[n2]6[n0]个，需要为你打开哪一个？\"}}]}";
		
//		str = "{\"Just Talk Dialog Outputs\":[{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"poi_reference\",\"data_obj\":[{\"ref_name\":\"东方商厦(南东店)\",\"ref_address\":\"黄浦区南京东路800号东方商厦7楼(近西藏中路)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121475606\",\"ref_latitude\":\"31235155\",\"ref_distance\":\"12700\"},{\"ref_name\":\"东方商厦杨浦店\",\"ref_address\":\"杨浦区四平路2500号东方商厦内(近黄兴路)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121515601\",\"ref_latitude\":\"31299024\",\"ref_distance\":\"13694\"},{\"ref_name\":\"东方商厦\",\"ref_address\":\"上海市徐汇区漕溪北路8号5楼\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121438070\",\"ref_latitude\":\"31193363\",\"ref_distance\":\"15730\"},{\"ref_name\":\"东方商厦(奉贤店)\",\"ref_address\":\"近郊奉贤区南桥镇百齐路588号东方商厦奉贤店(南桥汽车站往东200米)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121483820\",\"ref_latitude\":\"30916056\",\"ref_distance\":\"33725\"},{\"ref_name\":\"东方商厦嘉定店\",\"ref_address\":\"上海市嘉定区城中路66号\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121248693\",\"ref_latitude\":\"31380419\",\"ref_distance\":\"39161\"},{\"ref_name\":\"东方商厦\",\"ref_address\":\"青浦1路;青浦1路\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121111847\",\"ref_latitude\":\"31148845\",\"ref_distance\":\"47142\"},{\"ref_name\":\"东方商厦(金山店)\",\"ref_address\":\"近郊金山区卫清西路168弄99号百联金山购物中心内(近松卫南路)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121348336\",\"ref_latitude\":\"30730424\",\"ref_distance\":\"57779\"}]}}},{\"Sentence\":{\"Present\":\"3\",\"Content\":\"主人，查到多个起始地址，请选择一个：\",\"tag\":\"poi\"}}]}{\"Just Talk Dialog Outputs\":[{\"PreFormatted\":{\"Type\":\"JSON\",\"json_data\":{\"type\":\"poi_reference\",\"data_obj\":[{\"ref_name\":\"东方商厦(南东店)\",\"ref_address\":\"黄浦区南京东路800号东方商厦7楼(近西藏中路)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121475606\",\"ref_latitude\":\"31235155\",\"ref_distance\":\"12700\"},{\"ref_name\":\"东方商厦杨浦店\",\"ref_address\":\"杨浦区四平路2500号东方商厦内(近黄兴路)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121515601\",\"ref_latitude\":\"31299024\",\"ref_distance\":\"13694\"},{\"ref_name\":\"东方商厦\",\"ref_address\":\"上海市徐汇区漕溪北路8号5楼\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121438070\",\"ref_latitude\":\"31193363\",\"ref_distance\":\"15730\"},{\"ref_name\":\"东方商厦(奉贤店)\",\"ref_address\":\"近郊奉贤区南桥镇百齐路588号东方商厦奉贤店(南桥汽车站往东200米)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121483820\",\"ref_latitude\":\"30916056\",\"ref_distance\":\"33725\"},{\"ref_name\":\"东方商厦嘉定店\",\"ref_address\":\"上海市嘉定区城中路66号\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121248693\",\"ref_latitude\":\"31380419\",\"ref_distance\":\"39161\"},{\"ref_name\":\"东方商厦\",\"ref_address\":\"青浦1路;青浦1路\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121111847\",\"ref_latitude\":\"31148845\",\"ref_distance\":\"47142\"},{\"ref_name\":\"东方商厦(金山店)\",\"ref_address\":\"近郊金山区卫清西路168弄99号百联金山购物中心内(近松卫南路)\",\"ref_city\":\"上海市\",\"ref_longitude\":\"121348336\",\"ref_latitude\":\"30730424\",\"ref_distance\":\"57779\"}]}}},{\"Sentence\":{\"Present\":\"3\",\"Content\":\"主人，查到多个起始地址，请选择一个：\",\"tag\":\"poi\"}}]}";
		
		JSONObject dataObj = null;
		try {
			JSONObject objRoot = new JSONObject(str);
			if(objRoot != null)
			{
				JSONArray array = objRoot.optJSONArray(DataConst.DATA_TYPE_RESPONSE);
				if(array != null && array.length() > 0)
				{
					for(int i = 0; i < array.length(); i++)
					{
						jsonObj = array.optJSONObject(i);
						if(jsonObj != null)
						{
							//BaseData data = null;
							
							dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_SENTENCE);
							if(dataObj != null){
								mSentence = new SentenceData(context, dataObj, mSentence);
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_QUESTION);
								if(dataObj != null){
									mQuestion = new QuestionData(context, dataObj, mQuestion);
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_OPTION);
								if(dataObj != null){
									mOption = new OptionData(context, dataObj);
									setDisplayText(mOption.getDisplayString());
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_APP);
								if(dataObj != null){
									mApp = new AppData(context, dataObj);
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_CONFIRMATION);
								if(dataObj != null){
									mConfirm = new ConfirmData(context, dataObj, mConfirm);
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_PREFORMAT);
								if(dataObj != null){
									PreFormatData mPreFormat = new PreFormatData(context, dataObj);
									if (mPreFormatList == null) {
										mPreFormatList = new ArrayList<PreFormatData>();
									}
									mPreFormatList.add(mPreFormat);
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_SDKCOMMAND);
								if(dataObj != null) {
									mSdkCommand = new SdkCommandData(context, dataObj);								
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_SILENTINFO);
								if(dataObj != null) {
									mSilentInfo = new SilentInfoData(context, dataObj);								
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_APPEX);
								if(dataObj != null) {
									mActionData = new SdkActionData(context, dataObj);								
								}
							}
							
							if(dataObj == null)
							{
								dataObj = jsonObj.optJSONObject(DataConst.DATA_TYPE_HELP);
								if(dataObj != null) {
									mHelpData = new HelpData(context, dataObj);							
								}
							}
							/*if(data != null && data.parseFromJson(context, dataObj))
								mLstData.add(data);*/
						}
					}
					initListData();
				}
				else
				{
					String sDataType = objRoot.optString(DataConst.DATA_TYPE_QUERY, null);
					if(sDataType != null)
					{
						jsonObj = objRoot.optJSONObject(DataConst.DATA_TYPE_KEY_DATA);
						if(jsonObj != null)
						{
							
						}
					}
				}
				ret = true;
			}			
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}

	private void initListData() {
		if (mLstData == null) {
			mLstData = new ArrayList<BaseData>();
		}
		mLstData.clear();
		if (mSentence 	!= null) {
			mLstData.add(mSentence);
		}
		if (mQuestion 	!= null) {
			mLstData.add(mQuestion);
		}
		if (mConfirm 	!= null) {
			if (mPreFormatList != null) {
				mConfirm.setContainData(true);
			}
			mLstData.add(mConfirm);
		}
		if (mApp 	!= null) {
			mLstData.add(mApp);
		}
		if (mOption 	!= null) {
			mLstData.add(mOption);
		}
		if (mPreFormatList 	!= null) {
			if (mConfirm 	!= null) {
				for (PreFormatData data : mPreFormatList) {
					data.setConfirmedData(true);
				}
			}
			mLstData.addAll(mPreFormatList);
		}
		if (mSdkCommand 	!= null) {
			mLstData.add(mSdkCommand);
		}
		if (mSilentInfo 	!= null) {
			mLstData.add(mSilentInfo);
		}
		if (mNotifyData		!= null) {
			mLstData.add(mNotifyData);
		}
		
		if (mActionData != null)
			mLstData.add(mActionData);
		
		if(mHelpData != null)
		{
			/*for(BaseData data : mLstData)
			{
				if(data instanceof HelpData)
				{
					mLstData.remove(data);
					break;
				}
			}*/
			if(mLstData.size() == 1)
			{
				if(mLstData.get(0) instanceof NotifyData)
					mLstData.add(0, mHelpData);
			}
			else
			{
		      mLstData.add(mHelpData);
			}
		}
	}

	public List<BaseData> getLstData() {
		if (mLstData == null) {
			initListData();
		}
		return mLstData;
	}
	
	public boolean isModified() {
		if(mLstData != null)
		{
			for(BaseData baseData : mLstData)
			{
				if (baseData.isModified()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public SilentInfoData getSilentInfoData()
	{
		SilentInfoData data = null;
		
		if(mLstData != null)
		{
			for(BaseData baseData : mLstData)
			{
				if(baseData instanceof SilentInfoData)
				{
					data = (SilentInfoData)baseData;
					break;
				}
			}
		}
		
		return data;
	}
	
	public boolean isSilentInfoMsg() {
		if(mLstData != null)
		{
			for(BaseData baseData : mLstData)
			{
				if (mLstData.size() == 1 && baseData instanceof SilentInfoData) {
					return true;
				}else if(baseData instanceof SentenceData) {								
					BaseData data = (SentenceData)baseData;
					if("登录成功".equals(data.getDisplayString()) || "帐号登录失败,切换到匿名状态".equals(data.getDisplayString())){
						return true;
					}
				}
			}
		}
		return false;
	}
}
