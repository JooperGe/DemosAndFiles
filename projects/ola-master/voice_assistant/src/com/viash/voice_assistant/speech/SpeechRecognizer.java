package com.viash.voice_assistant.speech;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import cn.yunzhisheng.common.util.ErrorUtil;
import cn.yunzhisheng.common.util.LogUtil;
import cn.yunzhisheng.vui.grammar.IGrammarListener;
import cn.yunzhisheng.vui.grammar.IGrammarOperate;
import cn.yunzhisheng.vui.recognizer.IRecognizerTalkListener;
import cn.yunzhisheng.vui.recognizer.RecognizerTalk;
import cn.yunzhisheng.vui.wakeup.IWakeupListener;
import cn.yunzhisheng.vui.wakeup.IWakeupOperate;

import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.aitalk4.Aitalk4;
import com.iflytek.asr.Recognizer.IRecognitionListener;
import com.iflytek.asr.AsrService.AsrRecord;
import com.iflytek.tts.TtsService.Tts;
import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.common.LogRecordData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.media.BeepPlayer;
import com.viash.voice_assistant.service.MusicService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.ScreenAndKeyguard;
import com.viash.voicelib.utils.ThreadUtil;

public class SpeechRecognizer implements ISpeechRecognizer{
	private static boolean DEBUG_SPEECH = false;
	private static final String TAG = "SpeechRecognizer";
	protected int ISR_EP_LOOKING_FOR_SPEECH = 0;//			还没有检测到音频的前端点。
	protected int ISR_EP_IN_SPEECH = 1;//	已经检测到了音频前端点，正在进行正常的音频处理。
	protected int ISR_EP_AFTER_SPEECH = 3;//	检测到音频的后端点，后继的音频会被MSC忽略。
	protected int ISR_EP_TIMEOUT = 4;//	超时。
	protected int ISR_EP_ERROR = 5;//	出现错误。
	protected int ISR_EP_MAX_SPEECH = 6;//	音频过大。

	protected int ISR_REC_STATUS_SUCCESS = 0; //	识别成功，此时用户可以调用QISRGetResult来获取（部分）结果。
	protected int ISR_REC_STATUS_NO_MATCH = 1; //	识别结束，没有识别结果
	protected int ISR_REC_STATUS_INCOMPLETE = 2;//	正在识别中
	protected int ISR_REC_STATUS_NON_SPEECH_DETECTED = 3;//	保留

	protected int ISR_REC_STATUS_SPEECH_DETECTED = 4;//	发现有效音频
	protected int ISR_REC_STATUS_SPEECH_COMPLETE = 5;//	识别结束
	protected int ISR_REC_STATUS_MAX_CPU_TIME = 6;//	保留
	protected int ISR_REC_STATUS_MAX_SPEECH = 7;//	保留
	protected int ISR_REC_STATUS_STOPPED = 8;//	保留
	protected int ISR_REC_STATUS_REJECTED = 9;//	保留
	protected int ISR_REC_STATUS_NO_SPEECH_FOUND = 10;//	没有发现音频

	protected int ISR_AUDIO_SAMPLE_FIRST = 1;//			第一块音频
	protected int ISR_AUDIO_SAMPLE_CONTINUE = 2;//			还有后继音频
	protected int ISR_AUDIO_SAMPLE_LAST = 4;//			最后一块音频


	public static final int ERR_MIC_CREATE = 10001;

	private static final int RECORDER_SAMPLERATE = 16000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	protected IRecognizeListener mListener;
	protected String mSessionId;
	protected AudioRecord mRecorder;
	protected int mBufSize;
	protected boolean mStopRecord = false;
	protected boolean mIsAborted = false;
	protected Thread mRecordingThread;
	protected Thread mRecordWriteToList;//Leo
	protected Thread mDetectRecordData;//Leo
	protected boolean mSaveRecordData = false;
	protected boolean mIsLogined = false;

	private static int STATE_FREE = 1;
	private static int STATE_RECORDING = 2;

	public final static int VOLUME_LEVEL = 12;
	public static Context mContext;
	private long mStartTime = 0;

	private static int nowVolumeMax = 10000;
	private static SpeechRecognizer mInstance;

	protected int mState = STATE_FREE;
	public static final boolean USE_TONE = true;
	public static boolean USE_OFFLINE_VR = false;
	public static final String START_FROM_OFFLINE_RECORD = "needStartCapture";
	private static Boolean mMutex = new Boolean(true); //Leo
	public ArrayList<byte[]> mList = new ArrayList<byte[]>();//Leo
	public ArrayList<byte[]> mListToSend = new ArrayList<byte[]>();//Leo
	private Aitalk4 aitalk4 = null;//Leo

	private IRecognizerTalkListener mRecognizerTalkListener  = null;
	private RecognizerTalk mYZSRecognizer;
	private IWakeupOperate mWakeupOperate; 
	private IGrammarOperate mGrammarOperate = null;
	
	String buffer ="";
	int j;
	private ArrayList<byte[]> yzsList = new ArrayList<byte[]>();
	private static final int READ_DELAY = 10;
	protected AudioManager mAudioManager = null; 
	private static OnAudioFocusChangeListener mAudioFocusChangeListener = null;
	public  static boolean recognizeSuccess = false;
	
	private static String mVideoString = "mVideoString";//"萧11郎,新12生肖,机器猫,爱国者,勇敢的心,新十二生肖,太窘,人在窘途之泰窘,人在窘途,泰囧,2012,007,007大破量子危机,007大破天幕杀机,007大破天幕危机观影调查,007大战皇家赌场,007天降杀机,1001个真相,100俱乐部,101次求婚,墨西哥往事,微电影外星人与限价房首映,鸿门宴2005,女人的村庄,老马家的幸福往事,南极大冒险,高度怀疑,寻觅,空宅鬼影,我是小甜甜,幸福迷途广州举行活动,金福南杀人事件始末,勇往直前,东方之珠,鬼马双星,维兰德第二季,一九四二发布会,2012年10月第2周独家播报,巴黎恋人,新世纪福音战士,爱上总经理,神医侠侣,第23届台湾金曲奖颁奖,我爱钟无艳,小卓玛,果酱男孩,2013伦敦春夏时装周,家有色邻,蔷花红莲,花花世界花家姐 ,迷失洛杉矶,生死大营救,危险关系,钢甲卡卡龙第二部,龙太子,刀客外传,神雕侠侣古天乐版,我的秘密花园,好人,依然爱你,迷煳天使,海上传奇,周围的事,深入阴宅,诛寇行动,爱无罪,封神演义仙界传,胜者为王之王者之战,三十极夜,变性双面杀手,民间传说之封神榜 ,W的悲剧,文娱播报20120524,黑心鬼,蛇年春晚之歌曲串烧,老甲A联赛,王刚讲故事,入地无门,君子协定,2011城市之音至尊金榜,第24届东京电影节独家专访,月嫂,中国神秘宝藏之谜,玻璃花,釜山电影节相关报道,魅力研习社第47期片段,追爱总动员,梦之玉奇谈,东方朔,爽食行天下,中国好声音云杰,出包王女第3季,魅力研习社第122期片段,侍铳,书香北京,香奈儿秘密情史,四叠半神话大系,中国男足亚洲杯回顾,糖尿病寻求奇迹,变形金刚之机甲英雄,毛骨悚然撞鬼经2012夏季特别篇,八卦宗师,冯绍峰出席某男装品牌总部落成庆典,孖宝闯八关,五世同堂,技之旅人,神探朱古力,拜托了机长,夺命回声,激情燃烧的岁月,拉手帮,可以说的秘密星座运势02060212,第六届亚洲电影大奖颁奖礼,明朗少女成功记,华人精英盛典颁奖典礼,星猫之顽童历险记,巨人,亲爱的医生,传颂之物OVA,胜女的时代,养女,欢乐元帅,龙凤智多星,纸醉金迷,十万个冷笑话,铁板烧,音乐节频道,凤狸二人行,2011年9月日本MV  ,圣少女,父母爱情京郊热拍,柳叶刀,凉宫春日的消失,旋风管家,杀戮都市,毒战发布会,魅力研习社第177期花絮,阳光普照,大姐,高地,1213赛季NBA常规赛,自体发光的那女子,飞轮少年,徐箭隋兰大婚,两个永恒之烟锁重楼,少年四大名捕,神秘博士第七季,圣枪修女,高机动幻想2,罪爱,猫街,雄吕血,各怀心事,女巫季节,残酷冰雪,雷丁利兹音乐节2012,天使爱美丽片段,娱人碎碎念,妻子的诱惑,了不起的亡灵,我的野蛮女友,女王蜂,足迹,敢死队,红色的雪,关云长,中国正能量,东方卡萨布兰卡,文娱播报20120503,豪门情仇,最后判决,出水芙蓉,魅力研习社第10期花絮,决战紫禁之巅,哆啦A梦1998剧场版,新闻直通车,墨尔本风云,中国好声音之阿蜜丝女孩,不公正抽签OVA,魅力研习社第32期精彩片段,生死朗读,2012彼岸花开音乐节,去南极,为你守候,谍战之特殊较量,斯巴达克斯,冰火龙战,神秘博士第一季,独家专访天天,黑魔女学园,赵露,美丽贺岁第19期精彩片段,武状元苏乞儿,孝庄秘史,阿容,最后的王爷,我为校花狂第二季预告,每日文娱播报20120502,每日文娱播报20120503,每日文娱播报20120501,东成西就2011,刀剑神域,一夫当关,陆军情报部三科,摩登女婿,山椒大夫,拘束衣,假装情侣,特警风云,浪漫青春时光3,浪漫青春时光2,浪漫青春时光1,开学第一课,火天之城,每日文娱播报20120511,超时空要塞7,万历首辅张居正,2012李宇春whyme深圳演唱会,每日文娱播报20120512,每日文娱播报20120513,每日文娱播报20120514,每日文娱播报20120510,每日文娱播报20120509,每日文娱播报20120508,河东狮吼,每日文娱播报20120504,每日文娱播报20120507,每日文娱播报20120506,拥抱,凄艳断肠花,农业娘,五星上将,中国非物质文化遗产,笨贼妙探,生化危机4来生,飞篮扣杀Basquash,谁怜天下慈母心,中国宫殿与传说,每日文娱播报20120522,每日文娱播报20120523,姨妈的后现代生活,每日文娱播报20120520,功夫熊猫,每日文娱播报20120521,两个女人,BRAVE10,每日文娱播报20120519,欢天喜地七仙女,正德演义,每日文娱播报20120518,每日文娱播报20120517,每日文娱播报20120516,每日文娱播报20120515,新网球王子,兄弟,青春不下线,飞天舞,被称为早海的日子,创圣的大天使EVOL,长安宝宝大赛,淘气星猫1,娱乐现场20120724,娱乐现场20120723,一次完美的逃亡,淘气星猫2,元年,娱乐现场20120728,娱乐现场20120727,状王宋世杰,每日文娱播报20120419,娱乐现场20120729,拯救爱,疯狂小怪物,每日文娱播报20120425,每日文娱播报20120421,娱乐现场20120722,山药2,山药1,爱的初恋,蜡笔小新,娱乐现场20120715,娱乐现场20120714,娱乐现场20120713,娱乐现场20120712,魅力研习社第18期片段,娱乐现场20120719,娱乐现场20120718,娱乐现场20120717,娱乐现场20120716,百鬼夜行抄,金毛犬段景柱,替身杀手,娱乐现场20120710,娱乐现场20120711,一号档案守护者,爱就爱了,寒蝉鸣泣之时解,魅力研习社第124期花絮,无法坦诚相对,娱乐现场20120709,娱乐现场20120706,娱乐现场20120705,新生六居客,娱乐现场20120708,娱乐现场20120707,娱乐现场20120702,我是特种兵之利刃出鞘,马文的战争,娱乐现场20120701,少年张三丰,娱乐现场20120704,2012MTV音乐录影带大奖,娱乐现场20120703,狂野的禁区,妈妈的罗曼史,风云纪录,麦蒂肘击吉喆,爵士兔之奇幻之旅,血色玫瑰之女子别动队,破戒,挑情,双雄,超级女特工,犬夜叉完结篇,马丁的早晨,我为校花狂第13期个人秀,废柴同盟,火山边缘之恋,夜宴,军医,青春失乐园,你为我着迷,开心鬼放暑假,生活帮,我的酒鬼女友,高清预告片集锦,201213赛季西甲各轮五佳进球,霹雳娇娃2,机器人9号,绿光森林,守望者,空投柏林,漫游记,上有老,蓝兰岛漂流记,仍想结婚的女人,老婆是魔法少女,骗局,多兰的卡迪拉克,长梦不醒,新世纪GPX高智能方程式SIN,怪医黑杰克21,胜券在握,可以说的秘密第10期片段,魅力研习社第37期精彩花絮,小鹤屋,战国美少女的野望,我是间谍,经典人文地理,保持通话,七虹香电击大作战,夏色奇迹,危险代理人,大寒,我是歌手沙宝亮,夺命佳人,御伽草子,小马哥开会,太极发布会,注文津,部美雪4周连续悬疑SP无止境的杀人,桃花小妹,高达SeedDestiny,2012年11月韩娱播报,恋研~我们变成动画啦~,超人马大姐,魅力研习社第90期片段,潘多拉2饥饿列岛,快乐寻回犬,老人与海,喜羊羊与灰太狼3,七华617,浮沉首播,最佳拍档大显神通,学校2013,黄金大劫案首映,听说,新少林五祖,愿此刻永恒OVA遥路线,神枪狙击2013,我的妹妹,转山,匹夫观影,七夕血案,EVA新世纪福音战士新剧场版序,黑夜传说2进化,哆啦A梦1983剧场版,当狗爱上猫,媒体分享,中国好声音歌浴森,冲呀瘦薪兵团,告密者,李小璐音乐特辑,活着真好,非笑不可,CCTVMTV音乐盛典最新报道,老公万岁,空霸,中国好声音澳门威尼斯人跨年演唱会,冒险乐园,怒海雄心,世界奇妙物语20周年秋季特别篇,2012年12月娱乐播报,赏金猎手,误人子弟,爱情公寓2,雨中的树,星月私房话粉丝明星献祝福,溏心风暴,半夜不要照镜子,爱情公寓3,上锁的房间,神之谜题第二季,新警事,风尘三侠之红拂女,挚爱奇缘,疑心劫,中国家庭,天蝎行动,黑暗入侵,蛇年春晚之春暖花开,海豚爱上猫,大侦探福尔摩斯,中国紫砂陶文化,广告风云,流星之洛克人Tribe,正者无敌,绝代商骄,新安琪莉可 ,乱世三义,僵尸刑警,高达0083,短角情事,虹猫蓝兔阿木星,夺命金,测试下小小的清晰度,名媛望族,机动警察,女人本色,娱乐现场20120731,娱乐现场20120730,北京青年,新娱乐在线十周年庆典,高达0080,糖衣陷阱,夏娃的时间,魅力研习社第22期片段,安娜的部落,高达0079,自然的力量,最后的格格,灾难之城,温暖心情频道,珠江形象大使泳装秀,逝影留情,火神骑士,冠军,高梁地里大麦熟,天才钓鱼郎,最终幻想灵魂深处,现代美女,雾里看花,魅力研习社第36期精彩片段,市长之死,嫁个100分男人广州发布会,37首映,我为校花狂第22期个人秀,慌失失,兵团岁月,超能勇士,昏迷不醒,中国好声音之吉克隽逸,2009传说的故乡,美丽青年全泰壹,双重身份,我为校花狂第9期精彩片段,致命快感,512中国爱盛典精彩集锦,从爱情开始,明天就出发,勐龙过江,中国好声音精彩看点,法内情2002,变身男女,豪情盖天,了不起的盖茨比,良心无悔,黄昏腕轮传说,才气盖天,永恒之恋曲,悲伤恋歌,交响情人梦巴黎篇,错在新宿,美丽贺岁第3期精彩片段,美食从天而降,电影伤心童话发布会,一个陌生女人的来信,极速特警第十二季,我是歌手杨宗纬,迷情姐妹,寄生虫,天龙特攻队,阮玲玉1992年版,江阴要塞,春蚕织梦,冢原卜传,龙脉传奇第一部,落难夫妻,新娱乐在线,真爱之吻,连环大斗法,窃听风暴,比天国陌生,空中战将,我要冲线,海底淘法,柏林影展策划,魔神坛斗士,孤岛秘密战,中国好声音之吉克隽逸好声音蜕变之路,东方直播室,魅力研习社第60期片段,大块头有大智慧,操作系统和系统编程,济公传奇,吉林卫视新年之约2013跨年演唱会,逃离德黑兰,冰山独家策划,婆娑罗,信蜂,稀有出口圣诞传说,卧底神算,大清药王,喜羊羊与灰太狼之开心闯龙年,向前向后,雷哥老范,英格兰足总杯,真情满天下,药师寺凉子之怪奇事件簿,祝福的钟声,瞬死的爱人,浮草,雪域天路,神的晚餐,战旗发布会,怪医美女,中国好声音成长教室,有仇必报,旋转木马,武动青春";

	private static final String mContact = "mContact";//"人工台,邵小东, 贾培艳, 段海磊, 王晓飞, 赵昭阳, 陈正宇, 王松, 周兰君, 戴莎, 张坤, 黄涛, 张晓燕, 李燕, 林深啸, 郭伟亮, 袁鹏家, 杨钦, 外婆, 邵燕, 李银朋, 余川, 魏丛, 赵昌文, 杨静, 王若冲, 田飞, 王延龙, 朱守翔, 王书鑫, 李燕, 李怀东, 闫少彬, 武尧, 杨林青, 吴慧慧, 王新英, 程静涛, 刘军会, 王强, 金会玲, 赵统国, 朱涛, 王海瑞, 谷丽芹, 孙满仓, 王宇婷, 张兆玲, 刘小杰, 李蔷薇, 张俊杰, 王波, 高志明, 翟晓斌, 赵昭阳, 顾军宝, 杜玉三, 邓涛, 徐铖铖, 柴豪, 占有名, 李树斌, 腊泽勇, Roxy, 贾笑明, Gong le' home, 唐旭阳, 王琰, 王娟, 彭飞, 杨鹏, 魏立鑫, 叶旭阳, 张青峰, 葛飞, 邓峰, 连岩, 张磊磊, 王立辉, 杜海洋, 于俊洋, 张晓飞, 张耀元, 杨晓楠, 管庆虎, 赵鹏, 路飞飞, 刘杰, 齐景, 刘东杰, 王仕敏, 吴冰波, 袁露, 汪辉, 叶晓阳, 罗冲, 郑姗姗, 高娅楠, 余焕军, 姬鹏展, 李燕, 贺新征, 程亚丽, 董小坤, 张文杰, 李明浩, 张圣祥, 刘高丽, 米秀秀, 寇元鹏, 王道宪, 李娟, 张晓东, 刘淑琰, 程高飞, 王飞, 魏璐媛, 苏志超, 孟令彬, 身份证号, 张天龙, 张爱玲, 鲁邓娟, 候彦娥, 刘晶, 孙帅武, 郭伟亮, 靳石勇, 张亚男, 贾龙腾, 马利民, 李前令, 吴旭, 李明阳, 刘爱分, 刘丙飞, 杜松宇, 张良, 李照辉, Stone, Emily, 黄哲, brant.yuan@mobileares.com, 许晓岚, 王碧洲, 杜良乾, 许俊, Frank, Heidi, Jim, 冷老师, 王丽, 郭振坤, 张毅, 秦凤, 马石, 郭涛, 申斐, 刘翔, LEVEL-9tommy, Apache Asia Roadshow 2011 JTC, 贾臣, 杨明亮, 邹玉静, 郭宽, 龚乐, 姚冠颖, sunboyzhe@gmail.com, 张政, 李海风, 傅重添, 曹贺辉, 杨钦, 武晓洋, 陈宁宁, 龚乐, 孙堂欢, 二妈, 吕闯, 林深啸, 王艺龙, 刘高丽, 梁岩平, 李巧玲, 侯玉申, 二爹, 姚志彬, 成峰, 234277804@qq.com, 赵林静, 谢秉栩, 田金川, 吴慧慧, 张磊, 叶文龙, 张永磊, 李前令, 王世敏, 李金辉, 袁哲, 谢鑫, 张毅, 李照辉, 陈明睿, 段海磊, 刘丙飞, 王海瑞, 赵薇, 池猛, 郝毅敏, 张月, 徐勇, 杨珂, 于泓宇, 贾龙腾, 刘爱分, 陈泓光, 吴旭, 王慧, 耿京京, 王晶晶, 郭超, 成鹏, 吴相超, 孟令彬, 赵昭阳, 王璟, 刘东杰, 李乾, 吕小虎, 刘彦美, 王碧州, 申欣欣, 冯峰, null, 池猛, 郭超, 韩冰, 李书盼, 靳石勇, 刘小杰, 马利民, 王新英, 顾军保, 陈智殷, 韩天, 郝增玉, 孙帅武, 张良, 李新峰, 王晓飞, 校亚南, 郭辉, 白天增, 东梁, 小耗子, 邵小东, 申欣欣, 赵鹏（成都）, 卢云涛, 吴相超, 陈智殷, 段海磊, 张敏, 黄琦婷, 张子龙, 沈净瑄, 田培, 汤曌, 陈鹏, 李婉露, yuanzhe8688@vip.qq.com, shanghai-gtug@googlegroups.com, 李博, 黄涛, 马玉泉, 刘亚格, 张媛媛, 何刚, Xiaohan Li, 赵明喜, 王若冲, huyayanew2@hotmail.com, 秦凤轩, Lujiayu19910209, 周雪峰, 曾海林, 邢远, 畅真, Appfeedback@ifeng.com, Billy, 楊钦, 袁哲, 赵昌文, 黄盼, Tony, 范卫星, 杨端端, 潘青华, lfzhufeng@hotmail.com, 吴晓鹂, 沪南科帕奇小诸, 无锡, jiangxiaomeng@snda.com, 盛蓉, 郭丰俊, 解焱陆, 黄燕, fc@miui.com, 宋业文, 思践, 朱峰, 薛歌央, 黄光成, 周伟, ericfeige17@gmail.com, 解迎春, 肖庆平, 杜可越, 思践, yihang.chao@gmail.com, 陈吉胜, zhuzhongqing@konka.com, 杨端端, 文超, 张俊钦, 姚俊银, 360张凯峰, 杨路, 陈大年, rarnu1985@gmail.com, 周涛, 何昕, 刘文, 小钱, 李风, 梁伟文, 老婆, 杜可越, 沪南科帕奇小诸, dreamgzx@gtalk.com, 丁勇, 易小舟, 李曜, cailingxiao2010@gmail.com, 鹿晓亮, 冯子藤, 赵博晨, zwtt205@hotmail.com, 吴冬冬, 何昕, wenjing.yang@gmail.com, 刘升平, 任晓林, inna6677@163.com, 谢颜辉, 黄盼苏州, jason, zhongjinhong, 张利权, odyssey@fit.vutbr.cz, 冯叔叔南京, yannfmm@hotmail.com, 志婷巩, 栾欢, 杨雯静, 杨路, 冯叔叔, 赵志伟, 朱凯升优浪, 邱莎瑶, 张亮, 黄鹏, Roger Chai, 黄浩1, 李华军, hld_2002@163.com, 严挺, 黄浩1, George Hall, Xin He, 张可可, 鲍晴峰, 黄晓东微软, 明杏娇阿姨, 周涛, shan179@hotmail.com, 杨浩宇, lucywu, zhiwei shuang, 郑一新, 周涛, 朱忠庆, 衣架维修, 张明清, 马康驰, 胡国平, 李驿杰, 周伟, 黄小Q, <未知>, 王海青, 韦海军, <未知>, 黄燕, <未知>, HUANG PAN, 周慧, 鲍晴峰, 程先生捷克, 杜可越, 刘升平, 谢超老师, 王峥, 杜可越, 何昕, Renata Kohlova, 唐剑云, 杜可越, ddyangster@gmail.com, sumu2003@gmail.com, 俞, 诺基亚热线, 姚志强, yinghua.piao@lge.com, 栾欢, 捷克领事馆, 玮黄, 周涛, yuedjk@shu.edu.cn, 胡婵君, 查局, 黄燕, 沈丽清, <未知>, <未知>, 梁家恩, 胡浩main, 曹振海, Zhao Ran, jchuang33467@gmail.com, 金露蓉, <未知>, 解焱陆, 白宁, 吴琪珑律帅, 姚志强, 徐科, 王鹏, 赵志伟, 黄秋原, <未知>, 翟鲁峰, Wei Lai, 沈海涛, 徐行, 孙华山, <未知>, 黄晓东微软, 王士进, 锐音, 孙俊, 张利权, 杨杨琦, 文超, 周曦, 陈桂林, 程会计, 崔, 肃成叔叔, 火车站订票电, odyssey@fit.vutbr.cz, 鲍晴峰, 尚攀攀, 陈华, CEO-肖庆平, 李晓先, 李宵寒, 黄玲, 黄秋原, 李忠德, 国安张科长, 蔡洪滨, 黄玲, 张东东, lewisking.liu@gmail.com, 周韦, 庄姨, george@fbcinc.com, 胡浩main, 丁晨, <未知>, <未知>, 李霄寒, <未知>, 刘文, 吴悦, 陈万思, 黄鹏, 黄燕, 李驿杰, 儿保门诊, 黄光生, 程沉, 钟金宏, 杨杨琦, 吴琪珑律帅, <未知>, <未知>, 何涛, 翟鲁峰, dreamgzx@gtalk.com, 陈桂林, George Hall, 葛建新, yaojunyin@huawei.com, 韦海军, 郭忠祥, 徐颖, 康佳电视安装, 朱峰, Wei Lai, <未知>, 詹灿生, 严挺, 衣架维修, 无锡, 郭忠祥, 周曦, 杨端端, 吴翠翠, 邵志华, 李卫平院长, 韦海军, 邵国栋, 朱峰, 黄光明, 王海青, 李靖, 蔡洪滨, 吉胜陈, 姚俊银, 曾蓉, <未知>, <未知>, 赵路, 吕欣, 黄燕, <未知>, 仲海兵, 解迎春, 严挺, 穆向禹, <未知>, 徐颖, 周慧, 奇虎张凯峰, 易立夫, 刘华玲, 杜可越, <未知>, 吴翠翠, 梁家恩, 卢凯, 吴阿姨老公, li yijie, 姚志强, <未知>, 何楠, 谢颜辉, 李宵寒, 徐其东, 明杏娇阿姨, wangxm@szu.edu, 施勤, Michael Zhen, 谢颜辉, 杜可越, 黄盼, 姚志强, 360张凯峰, 朱军, 朱峰, 王刚毅, 吴兴俊, dong-jian yue, 卫武姜, 吉建明律师, jinhong. zhong, 朱忠庆, ecgnahz@gmail.com, 吴晓鹂, 周辉, 冯叔叔南京, jason@qq.com, 王进, <未知>, 郭忠祥, 周涛, 刘亚林, Sue Zhang, 陈学超, 康佳电视安装, 新月会陈佳芸, YiJian Wu, 香岛, 刘路路, 罗西, 徐科, 杜可越, jason, 周辉, <未知>, 智鹰王, zhou michael, lipeng31@gmail.com, <未知>, 谢颜辉, 李辉, 猎头lucy, 江立源朋友, <未知>, 龙艳花, 黄晓东微软, 陈桂林, 吴兴俊, 杨其森, 黄鹏, tonyhwei@gmail.com, 赵路, 黄玲, xstarse, 刘医生, 姚俊银, wangxm@szu.edu, 章钊, 吉建明律师, 蔡洪滨, 刘升平, 文超, Jie Bao, 黄蔚, 吴冬冬, 赖伟, 李宵寒, 郑一新, 解焱陆, 杜可越, 沈蕴婕科委, 盛蓉, 刘航, 李佳, 王士进, 欧智坚, wusajian@ustc.edu, zhongjinhong, 梁家恩, 朱凯升优浪, 胡金辉, 李曜, 发票, <未知>, 程先生捷克, cnwangxm@edu.21cn.com, 易小舟, 刘野, 杜可越, 吴琪珑律帅, 王刚毅, 邱莎瑶, 张亚昕, charlotte_ursula@msn.com, <未知>, wenjing yang, lizhipeng@wingtech.com, 欧阳峥峥, 穆向禹, 徐凯, 程会计, 何楠, 刘至斌, 李宵寒, jinyuiii@msn.com, 国安局顾世勋, 搬家张先生, 姚俊银, 周伟, 任晓林, <未知>, 王鹏, 黄光明, 杜可越, 惠扬QQ, 小钱, panda.jpc@gmail.com, <未知>, 杨, <未知>, hxliu_ustc@hotmail.com, 李驿杰, 刘路路, Kathy Du, 韦海军, Wei Lu, 黄燕, 谢磊西工大, 黄璜, 吴悦, 胡金辉, <未知>, 杜道秀, 经纬城市绿州, 李佳, 殷铭, 顾维灏, <未知>, 王言, 庄姨, zengqf@csvc.com.cn, 何昕悦, 雷琴辉, 杨小平, 沈丽清, 老婆, 卢威, 邵国栋, wangdazhou@ccbsip.com, 苗露, 沈先生, 严挺, 郭丰俊, Roger.char@sap.com, 周曦, 新月汇前台, 庄姨, 陈吉胜, 卢威, 潘青华, wenji.jin@gmail.com, 刘华玲, 杨琦, 刘华玲, 刘红星, 黄蔚, 解迎春, 杨路, 欧智坚, 潘德重, 杜可越, <未知>, 丁勇, 李忠德, 杨雯静, 国安局孙华山, 刘亚林, 黄盼, 6919959@qq.com, 卢凯-LuKai, 姜晓檬, 蔡洪滨, 陈学超, lewisking.liu@gmail.com, 118房间, 鲍晴峰, 王一先, 解迎春, 石文峰, 李佳, 徐科, 姚俊银, 润强闫, 闫润强, xi zhou, 任姨, 严吕慈, 奚昊, jialei_friend@hotmail.com, 雍震, 虾米王昊, 杜可越, 袁梅, 张劲松, xh_nc@hotmail.com, 赖伟, zjsxhy@gmail.com, Renata Kohlova, 党峰, 谢超老师, 游笑珊, 杨琦, 葛杰, 胡国平, 严挺, 黄盼苏州, 庄薇妈妈, 陈吉胜, 思践, 张连城师父, 陆伟, 周干民, 潘青华, 刘医生, 卉葛, 吴帅, 何昕, 杨琦, 王鹏, 张峰, 周涛, 姚志强, 修车小梁, 周涛, lisa, shenghai xu, 高继业, 黄玲, 杨琦, 熊立, 徐燃, 邵志华, 刘欣舟, Gang余钢-Yu, 赵博晨, qin shi, 杜可越, 王玉平, 王言, 陈桂林, 何德海, fang yitang, 石今金, 新月汇沈总, 庄律师, 惠扬QQ, 谢超老师, 曾蓉, 王刚毅, 康佳电视安装, huhao@360.cn, 董博, 穆向禹, 赵哲, 韦海军, XIE Lei, 王刚毅, 何德海, 庄明浩, 石文峰, zsrcyj@163.com, 鲍晴峰, 冯叔叔, 胡婵君, 黄光成, 范, 黄三急, 何德海, 张明清, 梁家恩, 党峰, <未知>, 王海青, 刘亚林, 许东星, 李晓先, 沈蕴婕, 沈蕴婕科委, 洗衣机维修, zhiwei shuang, 杨琦, 老梁, yswei@eyou.com, YiJian Wu, 梁家恩, 徐颖, 彭芬, 陈大年, 张劲松, Ye Danzhao, peng wang, ChaiRoger, 王刚毅, haochengba yang, 白宁, lijia@snda.com, 姚志强";
	private static final String mApp = "mApp";//"Google Talk, Spare Parts, 录音机, 设置, 车载主屏幕, 音乐, 视频, 信息, 发送电子邮件, 时钟, 拨号, 通讯录, 相机, 日历, 浏览器, 大众点评, Google 设置, 豌豆荚, Play 商店, Gmail, 搜狗输入法, 地图, 新闻和天气, 语音拨号器, 手电筒, 主题选择, 固件管理器, 图库, 文件管理器, FM收音机, 下载内容, Dev Tools, DSP 管理器, 计算器, 终端模拟器, 陌陌, 人人, OfflineTTSDemo, 网易云阅读, Letv命令和语音输入, Temple Run, 有信, Catch, 百度地图, 连我, 优衣库, 遇见, Weico, TalkBox, FileDivider, QQ空间, 微信,  佐佐日历 , 微语音输入, Adobe Reader, Superuser,  美团 , 淘伴, 名片王中王,  语控精灵, 天天动听, iWeekly, Endomondo , LoveBox, 音乐雷达, 本地, 导航,  云中书城, 网易新闻, QQ, Manuganu, 墨迹天气, Pocket, 云端硬盘, ABViewer, 搜索, 随手记, 必读名著60部, 豆瓣阅读, USCPlatform, Viber,  云知声语音助手";
	private static final String mSetting = "mSetting";//"关掉蓝牙, 关闭静音, 铃声设置, 启动静音, 退出震动模式, 打开移动网络, 取消静音模式, 退出wifi热点, 取消静音, 停止蓝牙, 打开震动, 关闭wifi, 关闭蓝牙, 开启wifi热点, 静音模式, 退出飞行模式, 关闭震动, 打开wifi热点, 设置壁纸, 退出静音, 设置时间, 启动蓝牙, 打开静音模式, 开启静音, 退出静音模式, 开启飞行模式, 取消wifi, 开启wifi, 打开数据, 关闭3G, 打开3G, 打开蓝牙, 退出震动, 退出wifi, 关闭自动旋转, 震动模式, 打开静音, 震动, 打开wifi, 取消震动模式, 关闭静音模式, 打开飞行模式, 关闭移动网络, 开启震动模式, 打开震动模式, 启动震动模式, 开启静音模式, 关闭GPRS, 打开自动旋转, 开启蓝牙, 关闭数据, 关闭wifi热点, 启动震动, 启动wifi, 取消wifi热点, 关闭震动模式, 取消自动旋转, 启动wifi热点, 开启自动旋转, 关闭飞行模式, 静音, 启动自动旋转, 打开GPRS, 退出自动旋转, 开启震动, 启动静音模式, 启动飞行模式, 取消震动";
	private static List<String> tvChannelList = new ArrayList<String>();
	public static final String TAG_CONTACT = "Contact"; // 联系人
	public static final String TAG_APPS = "Apps"; // 应用名
	public static final String TAG_SONG = "Song"; // 音乐名
	public static final String TAG_VIDEO = "Video"; // 影视剧
	public static final String TAG_SINGER = "Singer"; // 歌手
	public static final String TAG_CHANNEL = "Channel"; // 电视频道
	public static final String TAG_SETTING = "Setting"; // 设置
	
	static {
		tvChannelList.add("乐视直播台");
		/*tvChannelList.add("CCTV-1");
		tvChannelList.add("CCTV-2");
		tvChannelList.add("CCTV-3");
		tvChannelList.add("CCTV-4");
		tvChannelList.add("CCTV-5");
		tvChannelList.add("CCTV-6");
		tvChannelList.add("CCTV-7");
		tvChannelList.add("CCTV-8");
		tvChannelList.add("CCTV-9");
		tvChannelList.add("CCTV-10");
		tvChannelList.add("CCTV-11");
		tvChannelList.add("CCTV-12");
		tvChannelList.add("CCTV-音乐");
		tvChannelList.add("CCTV-少儿");
		tvChannelList.add("CCTV-新闻");
		tvChannelList.add("内蒙卫视");
		tvChannelList.add("云南卫视");
		tvChannelList.add("贵州卫视");
		tvChannelList.add("广东卫视");
		tvChannelList.add("新疆卫视");
		tvChannelList.add("宁夏卫视");
		tvChannelList.add("旅游卫视");
		tvChannelList.add("甘肃卫视");
		tvChannelList.add("重庆卫视");
		tvChannelList.add("广西卫视");
		tvChannelList.add("陕西卫视");
		tvChannelList.add("江苏卫视");
		tvChannelList.add("东南卫视");
		tvChannelList.add("黑龙江卫视");
		tvChannelList.add("山东卫视");
		tvChannelList.add("吉林卫视");
		tvChannelList.add("深圳卫视");
		tvChannelList.add("辽宁卫视");
		tvChannelList.add("河南卫视");
		tvChannelList.add("江西卫视");
		tvChannelList.add("山西卫视");
		tvChannelList.add("河北卫视");
		tvChannelList.add("西藏卫视");
		tvChannelList.add("青海卫视");
		tvChannelList.add("湖北卫视");
		tvChannelList.add("四川卫视");
		tvChannelList.add("湖南卫视");
		tvChannelList.add("南方卫视");
		tvChannelList.add("BTV卡酷");
		tvChannelList.add("风云音乐");
		tvChannelList.add("风云足球");
		tvChannelList.add("高尔夫");
		tvChannelList.add("国家地理");
		tvChannelList.add("国防军事");*/
	}
	
	static
	{
		try
		{
			//System.loadLibrary("msc_1030_htc");
			System.loadLibrary("olamsc"); 
		}
		catch( UnsatisfiedLinkError e)
		{
			e.printStackTrace();
		}

		try
		{
			System.loadLibrary("speechrecognizer");
		}
		catch( UnsatisfiedLinkError e)
		{
			e.printStackTrace();
		}
	}

	private SpeechRecognizer(Context context)
	{
		mContext = context;		
		init_yzs();
		mBufSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);  
	}

	public static SpeechRecognizer getInstance (Context context) {
		mContext = context;
		if (mInstance == null ) {
			mInstance = new SpeechRecognizer(context);
		}
		return mInstance;
	}

	public void allowSaveRecoredData(boolean allow)
	{
		mSaveRecordData = allow;
	}

	public boolean isRecognizing()
	{
		return (mState != STATE_FREE);
	}

	public void abort()
	{
		mIsAborted = true;
		mStopRecord = true;		
		//USE_OFFLINE_VR = false;
		Log.d(TAG, "abort()");
		mState = STATE_FREE;
		AiTalkShareData.setRecognizeFlag(3);//Leo 退出循环录音
	}

	//初始化 云知声监听
	
	public void init_yzs(){
		if(mRecognizerTalkListener == null){
			mRecognizerTalkListener = new IRecognizerTalkListener(){

				@Override
				public void onDataDone() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onInitDone() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTalkCancel() {
					// TODO Auto-generated method stub
					if(mListener != null)
					  mListener.onCancel();
					mIsAborted = true;
				}

				@Override
				public void onTalkError(ErrorUtil arg0) {
					// TODO Auto-generated method stub					
					mListener.onError(0);
					mStopRecord = true;	
				}

				@Override
				public void onTalkParticalResult(String arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTalkProtocal(String arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTalkRecordingStart() {
					// TODO Auto-generated method stub
					if(mListener != null)
						mListener.onBeginningOfSpeech();
				}

				@Override
				public void onTalkRecordingStop() {
					// TODO Auto-generated method stub
					BeepPlayer.play(mContext, "stop.mp3", false);
					mStopRecord = true;	
					if(mListener != null)
						mListener.onEndOfSpeech();
				}

				@Override
				public void onTalkResult(String arg0) {
					// TODO Auto-generated method stub
					if(mListener != null)
						mListener.onResults(arg0, null);
				}

				@Override
				public void onTalkStart() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTalkStop() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onUserDataCompile() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onUserDataCompileDone() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onVolumeUpdate(int arg0) {
					// TODO Auto-generated method stub
					Log.i(TAG, "volume is "+arg0);
					if(mListener != null)
						mListener.onVolumeUpdate(arg0*12/100);
				}};
		}
		if(mYZSRecognizer == null)
			mYZSRecognizer = new RecognizerTalk(mContext);
		mYZSRecognizer.init();
		
		List<String> appList = new ArrayList<String>();
		String[] apps = mApp.split(",");
		for (String v : apps) {
			appList.add(v);
		}

		List<String> nameList = new ArrayList<String>();
		String[] contacts = mContact.split(",");
		for (String v : contacts) {
			nameList.add(v);
		}

		List<String> settingList = new ArrayList<String>();
		String[] settings = mSetting.split(",");
		for (String v : settings) {
			settingList.add(v);
		}

		List<String> videoList = new ArrayList<String>();

		String[] videos = mVideoString.split(",");
		for (String v : videos) {
			videoList.add(v);
		}

		Map<String, List<String>> data = new HashMap<String, List<String>>();
		data.put(TAG_APPS, appList);
		data.put(TAG_CONTACT, nameList);
		data.put(TAG_SETTING, settingList);
		data.put(TAG_VIDEO, videoList);
		data.put(TAG_CHANNEL, tvChannelList);

		// TODO 设置个性化数据
		// 注意，若没有个性化数据，该方法也需要调用一次，参数可以设置为null
		mYZSRecognizer.setUserData(data);

		// 设置录音超时时间，设置0或者负数则会取消录音超时功能
		mYZSRecognizer.setRecordingTimeout(5000);
	}

	public boolean startRecognize(long waitTime,boolean OfflineVR)
	{
		boolean ret = false;
		if(waitTime != 0)
			mStartTime = System.currentTimeMillis() + waitTime;
		else
			mStartTime = 0;

		if(mRecordingThread != null)
		{
			try {
				Log.i(TAG, "waiting for record thread to finish!");
				abort();
				mRecordingThread.join(5000);
				mRecordingThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}          
		}
		
		USE_OFFLINE_VR = OfflineVR;	
		mIsAborted = false;
		mStopRecord = false;
		ret = startRecording();		
		return ret;
	}
	//int i = 1;
	public void stopRecognize()
	{
		mStopRecord = true;	
	}

	public void setListener(IRecognizeListener listener)
	{
		mListener = listener;
	}

	public boolean create()
	{
		boolean ret = false;
		int value = 0;
		String configs = "appid=57c3a3c7"; //此处因为我们使用的是第三定制的APPID，所以请自行在第三方申请开通语音识别功能。
		if(DEBUG_SPEECH)
		{
			configs += ",vad_enable=true";
		}
		else
		{
			configs += ",vad_enable=true";

		}

		//value = jniQISRInit(configs);
		value = jniMSPLogin("", "", configs);

		if(value == 0)
		{
			mIsLogined = true;
			ret = true;
		}
		registerAudioFocusChangeListener();	
		return ret;
	}

	@SuppressLint("NewApi") private void registerAudioFocusChangeListener()
	{
		if (Build.VERSION.SDK_INT >= 8) {
			if (mAudioManager == null) {
				mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			}
			mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
				
			    @TargetApi(Build.VERSION_CODES.FROYO)
				public void onAudioFocusChange(int focusChange) {  
			        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
			        	//Log.e("ppp", "speech AUDIOFOCUS_LOSS_TRANSIENT USE_OFFLINE_VR = "+USE_OFFLINE_VR);
			        	if(USE_OFFLINE_VR)
			        	{
			        	  stopWakeUp();	
			        	  abort();	
			        	}
			        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			        	//Log.e("ppp", "speech AUDIOFOCUS_LOSS USE_OFFLINE_VR = "+USE_OFFLINE_VR);
			        	if(USE_OFFLINE_VR)
			        	{
			        	  stopWakeUp();	
			        	  abort();
			        	}
			        	//mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
			        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
			        	//TODO
			        	//Log.e("ppp", "speech AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
			        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) { 
			        	Log.e("ppp", "speech AUDIOFOCUS_GAIN MusicService.isPlayingState()11 == "+MusicService.isPlayingState());
			        	if(SavedData.isVoiceWakeUpOpen() && USE_OFFLINE_VR)
			        	{
			        		//Log.e("ppp", "speech AUDIOFOCUS_GAIN MusicService.isPlayingState()22 == "+MusicService.isPlayingState());
			        		if(MusicService.isPlayingState() == false)
			        		  startRecognize(0,true);			        		
			        	}else if(SavedData.isVoiceWakeUpOpen() && MusicService.isPlayingState())
			        	{
			        		//Log.e("ppp", "speech AUDIOFOCUS_GAIN abandonAudioFocus");
			        		mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
			        	}
			        }  
			    }  
			}; 			
		}
	}
	@SuppressLint("NewApi") private void unregisterAudioFocusChangeListener()
	{
		mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		mAudioFocusChangeListener = null;
		mAudioManager = null;
	}
	public boolean isIsLogined() {
		return mIsLogined;
	}

	public void destroy()
	{
		//jniQISRFini();
		if(mIsLogined)
		{
			jniMSPLogout();
		}
		mIsLogined = false;
		//Leo Start
		AiTalkShareData.setRecognizeFlag(3);//退出循环录音
		//releaseAudioRecord();
		//Leo End
		unregisterAudioFocusChangeListener();
		if(mYZSRecognizer != null)
		{
			mYZSRecognizer.cancel();
			mYZSRecognizer.release();
		}
	}

	private boolean startRecording(){
		boolean ret = false;

		mState = STATE_RECORDING;				
		mRecordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try
				{
					if (Tts.isPlaying()) {
						Tts.stop();
					}

					/*int size = mBufSize * 80;
					while(size > 256 * 1024)
						size /= 2;
					mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
							RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, size);
					int retry = 0;
					while(mRecorder.getState() != AudioRecord.STATE_INITIALIZED && retry < 4)
					{
						ThreadUtil.sleep(500);
						retry++;
					}*/

					long waitTime = 0;
					if(USE_TONE && !USE_OFFLINE_VR)
					{
						stopWakeUp();
						BeepPlayer.play(mContext, "start.mp3", false);
						//Log.e("PPP", "start.mp3");
//						waitTime = 250;
						waitTime = 2500;
					}
					else
					{					
						if(mStartTime != 0 && mStartTime < System.currentTimeMillis())
						{
							waitTime = System.currentTimeMillis() - mStartTime;
							if(waitTime > 200 || waitTime < 0)
								waitTime = 0;
						}	
					}

					if(waitTime > 0)
					{
						ThreadUtil.sleep(waitTime);
					}
					
					sendAudioToSer();
				}
				catch(IllegalArgumentException e)
				{
					e.printStackTrace();
					if(mListener != null)
						mListener.onError(ERR_MIC_CREATE);
					releaseAudioRecord();
				}
				catch (IllegalStateException e) {
					e.printStackTrace();
					if(mListener != null)
						mListener.onError(ERR_MIC_CREATE);
					releaseAudioRecord();
				} 

				mState = STATE_FREE;
			}
		},"AudioRecorder Thread");		

		mRecordingThread.start();
		ret = true;

		return ret;
	}
	//Leo Begin
	private boolean ExistSDCard() 
	{
		if (android.os.Environment.getExternalStorageState().equals(  
				android.os.Environment.MEDIA_MOUNTED)) {  
			return true;  
		} else  
			return false;  
	}  

	@SuppressLint("NewApi") public void sendAudioToSer(){
		if(USE_OFFLINE_VR)
		{	
			if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
				//Log.e("ppp", "speech requestAudioFocus ");
				if(MusicService.isPlayingState() == false)
				{
					mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
	                    AudioManager.STREAM_VOICE_CALL,
	                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
				}
			}
			
			if(mYZSRecognizer == null)
			   init_yzs();
			mGrammarOperate = (IGrammarOperate) mYZSRecognizer.getOperate("OPERATE_GRAMMAR");
			if (mGrammarOperate != null) {
				mGrammarOperate.setGrammarListener(new IGrammarListener() {

					@Override
					public String onInitModel(String tag, String path, boolean define) {
						if (define && tag.equals("poi")) {
							// 根据path可以区分出当前是否存在poi模型，或者当前的poi模型是否是当前城市的
						}
						return path;
					}

					@Override
					public void onResetModelStart(String tag) {
						
					}

					@Override
					public void onResetModelDone(String tag) {

					}

					@Override
					public void onModelCompile() {
						
					}

					@Override
					public void onModelCompileDone() {
						
					}
				});
			}
			
			mWakeupOperate = (IWakeupOperate) mYZSRecognizer.getOperate("OPERATE_WAKEUP");
			if (mWakeupOperate != null) {
				mWakeupOperate.setWakeupListener(new IWakeupListener() {

					@Override
					public void onSuccess(String reString) {
						mWakeupOperate.stopWakeup();
						recognizeSuccess = true;
						if(ScreenAndKeyguard.isScreenON(mContext) == false)
							ScreenAndKeyguard.turnOnScreen(mContext);
						else
						{
							if(ScreenAndKeyguard.isScreenLock(mContext))
							   ScreenAndKeyguard.unlockScreen(mContext);
							Intent intent1 = new Intent(mContext,GuideActivity.class);
							intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent1.putExtra(SpeechRecognizer.START_FROM_OFFLINE_RECORD, true);
							mContext.startActivity(intent1);
						}
					}

					@Override
					public void onStop() {						

					}

					@Override
					public void onStart() {				

					}

					@Override
					public void onInitDone() {

					}

					@Override
					public void onError(ErrorUtil error) {

					}
				});
				// TODO 若需测试唤醒，则启用下面这一行代码
				mWakeupOperate.startWakeup();
			}		
		}
		else
		{
			try{				
				if(!Constant.flag){
					sendAudioToUsc();
				}else{
					
					int size = mBufSize * 80;
					while(size > 256 * 1024)
						size /= 2;
					mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
							RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, size);
					int retry = 0;
					while(mRecorder.getState() != AudioRecord.STATE_INITIALIZED && retry < 4)
					{
						ThreadUtil.sleep(500);
						retry++;
					}
					
					sendAudioToMsc();
				}
				
			}catch(IllegalArgumentException e)
			{
				e.printStackTrace();
				stopWakeUp();
				if(mListener != null)
					mListener.onError(ERR_MIC_CREATE);
				releaseAudioRecord();
			}
			catch (IllegalStateException e) {
				e.printStackTrace();
				stopWakeUp();
				if(mListener != null)
					mListener.onError(ERR_MIC_CREATE);
				releaseAudioRecord();
			} 			
		}
	}

	public void stopWakeUp()
	{
		if(mWakeupOperate != null)
			mWakeupOperate.stopWakeup();
	}
	
	public void sendAudioToUsc(){		
		if(mRecognizerTalkListener!=null)
			mYZSRecognizer.setListener(mRecognizerTalkListener);

		mYZSRecognizer.start();		

	}



	//Leo End
	private void sendAudioToMsc(){
		final int RECORD_BUFFER_SIZE = 4096;
		byte data[] = new byte[RECORD_BUFFER_SIZE];
		SpeechRecognizeData dataRet = new SpeechRecognizeData();
		boolean first = true;
		int audioStatus = ISR_AUDIO_SAMPLE_FIRST;
		String recordFileName = null;		
		int errCode = 0;
		
		mRecorder.startRecording();
		if(mListener != null)
			mListener.onBeginningOfSpeech();

		String params= "ssm=1,sub=iat,aue=speex-wb;7,auf=audio/L16;rate=16000,ent=sms16k,rst=plain,rse=utf8,vad_speech_tail=800";		
		mSessionId = jniQISRSessionBegin(params);

		if(DEBUG_SPEECH)
		{
			Log.i(TAG,"jniQISRSessionBegin begin");
		}
		if(mSessionId != null)
		{		
			int old_volume = -1;
			int read = 0;
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			LogRecordData.startNewData(NetWorkUtil.isWIFIConnected(mContext));
			//LogRecordData.startNewData(false);
			//long time4 = System.currentTimeMillis();
			//	PcmFileManager.ClearBuffer();
			Log.i(TAG,"sendAudioToMsc() mList.size() = "+mList.size());
			mListToSend.clear();
			/*mListToSend.add(data);
			mListToSend.add(data);*/
			while(true){

				/*if(mListToSend.size() > 0)
				{
					data = mListToSend.remove(0);
					//	  PcmFileManager.WriteBuffer(data, RECORD_BUFFER_SIZE);
				}
				else if(mList.size() > 0)
				{
					data = mList.remove(0);
					read = 4096;
					//	  PcmFileManager.WriteBuffer(data, RECORD_BUFFER_SIZE);
				}
				else*/
				{
					read = mRecorder.read(data, 0, RECORD_BUFFER_SIZE);
					//	  PcmFileManager.WriteBuffer(data, read);
				}
				if(AudioRecord.ERROR_INVALID_OPERATION != read){
					if(read == 0)
					{
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
					// compute voice's volume
					int volume = getNormalizaVolume(getVoiceVolume(data));
					if(old_volume != volume){
						mListener.onVolumeUpdate(volume);
						old_volume = volume;
					}

					if(first)
					{
						audioStatus = ISR_AUDIO_SAMPLE_FIRST;
						first = false;
					}
					else if(!mStopRecord)
					{						
						audioStatus = ISR_AUDIO_SAMPLE_CONTINUE;
					}
					else
					{
						//time4 = System.currentTimeMillis();
						//Log.e("CalculateTime", "LastSegment:" + (time4 % 10000));
						audioStatus = ISR_AUDIO_SAMPLE_LAST;
					}

					LogRecordData.writeData(data, read);

					if(DEBUG_SPEECH)
					{
						Log.i(TAG,"jniQISRAudioWrite begin");
					}

					errCode = jniQISRAudioWrite(mSessionId, data, read, audioStatus, dataRet);

					if(DEBUG_SPEECH)
					{
						Log.i(TAG,"jniQISRAudioWrite end, error code:" + errCode);
					}
					if(errCode != 0 )
					{	
						break;
					}

					if(audioStatus == ISR_AUDIO_SAMPLE_LAST)
						break;
					if(dataRet.epStatus >= ISR_EP_AFTER_SPEECH)
					{
						break;
					}
				}
			}

			//	PcmFileManager.WriteToFile();

			releaseAudioRecord();			

			String recognizedText = "";
			mListener.onVolumeUpdate(0);
			if(FloatViewIdle.IS_CANCEL_RECORD)
				FloatViewIdle.IS_CANCEL_RECORD = false;
			else
			{
			    BeepPlayer.play(mContext, "stop.mp3", false);
			    //Log.e("PPP", "stop.mp3");
			}
			if(mIsAborted)
			{
				jniQISRSessionEnd(mSessionId);
				mListener.onCancel();
			}
			else
			{
				if(mListener != null)
					mListener.onEndOfSpeech();
				String result = "";

				// do not call QIsrGetResult if there is error in QIsrWrite
				if(errCode == 0)
				{				
					dataRet.recogStatus = 0;
					long time = System.currentTimeMillis();
					while(dataRet.recogStatus != ISR_REC_STATUS_SPEECH_COMPLETE && errCode == 0)
					{
						if(DEBUG_SPEECH)
						{
							Log.i(TAG,"jniQISRGetResult start");
						}
						String temp = jniQISRGetResult(mSessionId, 5000, dataRet);

						if(temp != null)
							result += temp;
						errCode = dataRet.errCode;

						if(DEBUG_SPEECH)
						{
							Log.i(TAG,"jniQISRGetResult end, error code:" + errCode + " recogStatus:" + dataRet.recogStatus);
						}
						//Log.i("CalculateTime", "Recognize Time:" + (System.currentTimeMillis() - time));

						if(dataRet.recogStatus == ISR_REC_STATUS_SPEECH_COMPLETE || errCode != 0)
							break;					
						if(System.currentTimeMillis() > time + 8000)
							break;
						ThreadUtil.sleep(20);
					}

					long time3 = System.currentTimeMillis();
					Log.i("CalculateTime", "Recognize Time:" + (time3 - time) + "  Text: " + result);	
				}

				if(errCode == 0 && result.length() > 0)
				{
					Log.i(TAG, "raw:" + result);
					recognizedText = result;
					if(mListener != null)
					{
						result = modifyResult(result);
						mListener.onResults(result, recordFileName);
					}
				}
				else
				{
					if(mListener != null)
						mListener.onError(errCode);
				}
				jniQISRSessionEnd(mSessionId);	
			}

			LogRecordData.writefinish(recognizedText);
		}
		else
		{
			if(mListener != null)
				mListener.onError(3);
		}
	}

	private String modifyResult(String result)
	{
		//result = "{\"sn\":1,\"ls\":true,\"bg\":0,\"ed\":0,\"ws\":[{\"bg\":0,\"cw\":[{\"w\":\"给\",\"sc\":0}]},{\"bg\":0,\"cw\":[{\"w\":\"屌\",\"sc\":0}]},{\"bg\":0,\"cw\":[{\"w\":\"丝\",\"sc\":0}]},{\"bg\":0,\"cw\":[{\"w\":\"打电话\",\"sc\":0}]},{\"bg\":0,\"cw\":[{\"w\":\"。\",\"sc\":0}]}]}";

		if(result.startsWith("{"))
		{
			try {
				JSONObject json = new JSONObject(result);
				result = "";
				JSONArray array = json.optJSONArray("ws");
				if(array != null && array.length() > 0)
				{
					for(int i = 0; i < array.length(); i++)
					{
						JSONObject objSub = array.optJSONObject(i);
						if(objSub != null)
						{
							JSONArray arraySub = objSub.optJSONArray("cw");
							if(arraySub != null && arraySub.length() > 0)
							{
								for(int j = 0; j < arraySub.length(); j++)
								{
									JSONObject objSub1 = arraySub.optJSONObject(j);
									if(objSub1 != null)
									{
										result += objSub1.optString("w", "");
									}
								}
							}
						}										
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Log.i(TAG, "modified:" + result);
		}

		return result;
	}

	private void releaseAudioRecord()
	{
		if(mRecorder != null)
		{
			try
			{
				mRecorder.stop();
			}
			catch(IllegalStateException e)
			{
				e.printStackTrace();
			}
			mRecorder.release();
			mRecorder = null;
		}
	}

	private int getVoiceVolume(byte[] data){
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		int v = 0;
		// 将 buffer 内容取出，做絕對值的總和
		for (int i = 0 ; i < data.length ; i+=2) {
			//	v += Math.abs(byteBuffer.getShort(i));
			if(byteBuffer.getShort(i) > v){
				v = Math.abs(byteBuffer.getShort(i));
			}
		}

		// 平方和除以数据总长度，得到音量大小。可以获取白噪声值，然后对实际采样进行标准化。
		//v /= (data.length / 2);
		return v;
	}

	private int getNormalizaVolume(int volume) {
		//final int MIN_VOLUME = 32;
		//final int MAX_VOLUME = 41;
		final int MIN_VOLUME = 1;
		final int MAX_VOLUME = 32767;
		if(volume > nowVolumeMax) nowVolumeMax = (int) (volume * 1.5);
		if(nowVolumeMax > MAX_VOLUME) nowVolumeMax = MAX_VOLUME;

		int v = volume - MIN_VOLUME;
		if(v < 0){
			v = 0;
		}else if(v > (nowVolumeMax - MIN_VOLUME)){
			v = (nowVolumeMax - MIN_VOLUME);
		}

		int normalizaVolume = (int) ((v / (float) (nowVolumeMax - MIN_VOLUME + 1)) * (VOLUME_LEVEL + 1));
		//Log.d(TAG, "volumeTest-->" +nowVolumeMax + ") volume: " + volume + " / " + normalizaVolume);
		return normalizaVolume;
	}

	protected native int jniQISRInit(String configs);
	protected native String jniQISRSessionBegin(String params);
	protected native String jniQISRGetResult(String sessionID, int waitTime, SpeechRecognizeData data);
	protected native int jniQISRAudioWrite(String sessionID, byte[] waveData, int waveLen, int audioStatus, SpeechRecognizeData data);
	protected native int jniQISRSessionEnd(String sessionID);
	protected native int jniQISRFini();
	protected native int jniMSPLogin(String user, String password, String configs);
	protected native int jniMSPLogout();

	@Override
	public boolean isRecognizeSuccess() {
		return recognizeSuccess;
	}

	@Override
	public void setRecognizeSuccess(boolean suc) {
		recognizeSuccess = suc;
	}

}
