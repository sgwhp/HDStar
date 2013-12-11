package org.hdstar.common;

import java.io.File;
import java.util.HashMap;

import org.hdstar.R;

import android.graphics.Color;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

public class Const {
	public static final int APP_CODE = 1;

	public static final class Urls {
		/** web服务器地址 */
		public static final String BASE_URL = "http://hdsky.me";
		public static final String TAKE_LOGIN_URL = BASE_URL + "/takelogin.php";
		public static final String HOME_PAGE = Const.Urls.BASE_URL
				+ "/index.php";
		public static final String GET_SECURITY_IMAGE_URL = BASE_URL
				+ "/image.php?action=regimage&imagehash=";
		public static final String FORUM_BASE_URL = BASE_URL + "/forums.php";
		public static final String NEW_TOPIC_URL = FORUM_BASE_URL
				+ "?action=post";
		public static final String VIEW_FORUM_BASE_URL = FORUM_BASE_URL
				+ "?action=viewforum&forumid=";
		public static final String CHAT_ROOM_URL = FORUM_BASE_URL
				+ "?action=viewforum&forumid=1";
		public static final String NEWBIE_URL = FORUM_BASE_URL
				+ "?action=viewforum&forumid=2";
		public static final String MOVIE_URL = FORUM_BASE_URL
				+ "?action=viewforum&forumid=11";
		public static final String ORDER_URL = FORUM_BASE_URL
				+ "?action=viewforum&forumid=9";
		public static final String VIEW_TOPIC_BASE_URL = FORUM_BASE_URL
				+ "?action=viewtopic";
		public static final String[] FORUM_URLS = { CHAT_ROOM_URL, NEWBIE_URL,
				MOVIE_URL, ORDER_URL };
		public static final String REPLY_URL = FORUM_BASE_URL + "?action=post";
		public static final String VIEW_MESSAGE_URL = FORUM_BASE_URL
				+ "/messages.php?action=viewmessage&id=";
		public static final String REPLY_PM_URL = BASE_URL + "/takemessage.php";
		public static final String SEND_PM_URL = BASE_URL
				+ "/sendmessage.php?reciever=";
		public static final String BOOKMARK_URL = BASE_URL
				+ "/bookmark.php?torrentid=";
		public static final String DELETE_POST_URL = FORUM_BASE_URL
				+ "?action=deletepost&postid=%d&sure=1";
		public static final String DELETE_TOPIC_URL = FORUM_BASE_URL
				+ "?action=deletetopic&topicid=%d&sure=1";

		/** 客户端服务器地址 */
		// public static final String SERVER_ADDRESS =
		// "http://hdstar.ap01.aws.af.cm/";
		public static String SERVER_ADDRESS;
		public static String SERVER_VIEW_FORUM_URL;
		public static String SERVER_VIEW_TOPIC_URL;
		public static String SERVER_GET_IMAGE_URL;
		public static String SERVER_VIEW_MESSAGES_URL;
		public static String SERVER_SEND_BOX_URL;
		public static String SERVER_STAFF_BOX_URL;
		public static String SERVER_REPORT_BOX_URL;
		public static String SERVER_CHEATER_BOX_URL;
		public static String[] MESSAGE_BOX_URLS;
		public static String SERVER_VIEW_MESSAGE_URL;
		public static String COMMON_MESSAGE_BOX_URL;
		public static String SERVER_CHECK_MESSAGE_URL;
		public static String SERVER_TORRENTS_URL;
		public static String SERVER_DOWNLOAD_URL;
		public static String SERVER_CHECK_UPDATE_URL;

		/** rutorrent地址 */
		public static final String RUTORRENT_HOME_PAGE = "http://%s/rutorrent";
		public static final String RUTORRENT_RPC_ACTION_URL = RUTORRENT_HOME_PAGE
				+ "/plugins/httprpc/action.php";
		public static final String RUTORRENT_RSS_ACTION_URL = RUTORRENT_HOME_PAGE
				+ "/plugins/rss/action.php";
		// 刷新rss标签地址
		public static final String RUTORRENT_RSS_REFRESH_URL = RUTORRENT_RSS_ACTION_URL
				+ "?mode=refresh&rss=%s";
		public static final String RUTORRENT_DISK_SPACE_URL = RUTORRENT_HOME_PAGE
				+ "/plugins/diskspace/action.php?_=";

		/**
		 * μtorrent地址 。为了统一格式，方便调用String.format()，token参数务必放在首位
		 */
		public static final String UTORRENT_HOME_PAGE = "http://%s/gui";
		public static final String UTORRENT_TOKEN_URL = UTORRENT_HOME_PAGE
				+ "/token.html";
		public static final String UTORRENT_BASE_REQ_URL = UTORRENT_HOME_PAGE
				+ "/?token=%s";
		public static final String UTORRENT_GET_LIST_URL = UTORRENT_BASE_REQ_URL
				+ "&list=1";
		public static final String UTORRENT_ACTION_URL = UTORRENT_BASE_REQ_URL
				+ "&action=%s";

		public static void initServerAddr(final String serverAddr) {
			SERVER_ADDRESS = serverAddr;
			SERVER_VIEW_FORUM_URL = SERVER_ADDRESS + "viewForum";
			SERVER_VIEW_TOPIC_URL = SERVER_ADDRESS + "viewTopic";
			SERVER_GET_IMAGE_URL = SERVER_ADDRESS + "getImage?url=";
			SERVER_VIEW_MESSAGES_URL = SERVER_ADDRESS + "messages";
			SERVER_SEND_BOX_URL = SERVER_ADDRESS + "sendbox";
			SERVER_STAFF_BOX_URL = SERVER_ADDRESS + "staffbox";
			SERVER_REPORT_BOX_URL = SERVER_ADDRESS + "reportbox";
			SERVER_CHEATER_BOX_URL = SERVER_ADDRESS + "cheaterbox";
			MESSAGE_BOX_URLS = new String[] { SERVER_VIEW_MESSAGES_URL,
					SERVER_SEND_BOX_URL, SERVER_STAFF_BOX_URL,
					SERVER_REPORT_BOX_URL, SERVER_CHEATER_BOX_URL };
			SERVER_VIEW_MESSAGE_URL = SERVER_ADDRESS + "viewMessage?id=";
			COMMON_MESSAGE_BOX_URL = BASE_URL + "/messages.php";
			SERVER_CHECK_MESSAGE_URL = SERVER_ADDRESS + "haveNewMsg";
			SERVER_TORRENTS_URL = SERVER_ADDRESS + "torrents";
			SERVER_DOWNLOAD_URL = SERVER_ADDRESS + "download";
			SERVER_CHECK_UPDATE_URL = SERVER_ADDRESS + "checkVersion";
		}
	}

	public final class ResponseCode {
		public static final int PARAMETER_ERROR = 1000;
		public static final int PARSE_ERROR = 1001;
		public static final int NO_APK_FOUND = 1002;
	}

	public static final String CHARSET = "UTF-8";

	public static final String SETTING_SHARED_PREFS = "setting";
	public static final String DOWNLOAD_SHARED_PREFS = "download";
	public static final String RUTORRENT_SHARED_PREFS = "rutorrent";
	public static final String REMOTE_SHARED_PREFS = "remote";
	public static final String DOWNLOAD_DIR = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "download";

	public static final String NEW_MESSAGE_ACTION = "org.hdstar.NEW_MESSAGE";
	public static final int NEW_MESSAGE_REQ_REFRESH = 1;
	public static final int NEW_MESSAGE_REQ_VIEW = 2;

	// public static final int[] forumIds = { 1, 2, 11, 9 };

	public static final int[] boxTypes = { 1, -1 };

	/**
	 * 种子优惠类型
	 * 
	 * @author robust
	 * 
	 */
	public static final class FreeType {
		public static final String FREE = "pro_free";
		public static final String _2X_FREE = "pro_free2up";
		public static final String _2X_50_PTC = "pro_50pctdown2up";
		public static final String _2X = "pro_2up";
		public static final String _50_PTC = "pro_50pctdown";
		public static final String _30_PTC = "pro_30pctdown";

		/**
		 * 为保持种子列表整齐，尽量限制返回的字符串长度为6
		 * 
		 * @param freeType
		 * @return
		 */
		public static SpannableString getFreeTag(String freeType) {
			SpannableString ss;
			if (FREE.equals(freeType)) {
				ss = new SpannableString(" Free ");
				ss.setSpan(new BackgroundColorSpan(Color.rgb(0, 52, 206)), 0,
						6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
			if (_2X_FREE.equals(freeType)) {
				ss = new SpannableString("2xFree");
				ss.setSpan(new BackgroundColorSpan(Color.rgb(15, 164, 100)), 0,
						6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
			if ((_2X_50_PTC.equals(freeType))) {
				ss = new SpannableString(" 2x50%");
				ss.setSpan(new BackgroundColorSpan(Color.rgb(0, 153, 0)), 0, 3,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new BackgroundColorSpan(Color.rgb(220, 0, 3)), 3, 6,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
			if (_2X.equals(freeType)) {
				ss = new SpannableString("  2x  ");
				ss.setSpan(new BackgroundColorSpan(Color.rgb(0, 153, 0)), 0, 6,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
			if (_50_PTC.equals(freeType)) {
				ss = new SpannableString(" 50%  ");
				ss.setSpan(new BackgroundColorSpan(Color.rgb(220, 0, 3)), 0, 6,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
			if (_30_PTC.equals(freeType)) {
				ss = new SpannableString(" 30%  ");
				ss.setSpan(new BackgroundColorSpan(Color.rgb(65, 23, 73)), 0,
						6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
			return new SpannableString("");
		}
	}

	public static final int[] DEFAULT_SMILEY_RES_IDS = { R.drawable.yct00,
			R.drawable.yct01, R.drawable.yct02, R.drawable.yct03,
			R.drawable.yct04, R.drawable.yct05, R.drawable.yct06,
			R.drawable.yct07, R.drawable.yct08, R.drawable.yct09,
			R.drawable.yct10, R.drawable.yct11, R.drawable.yct12,
			R.drawable.yct13, R.drawable.yct14, R.drawable.yct15,
			R.drawable.yct16, R.drawable.yct17, R.drawable.yct18,
			R.drawable.yct19, R.drawable.yct20, R.drawable.yct21,
			R.drawable.yct22, R.drawable.yct23, R.drawable.yct24,
			R.drawable.yct25, R.drawable.yct26, R.drawable.yct27,
			R.drawable.yct28, R.drawable.yct29, R.drawable.yct30,
			R.drawable.yct31, R.drawable.yct32, R.drawable.yct33,
			R.drawable.yct34, R.drawable.yct35, R.drawable.yct36,
			R.drawable.yct37, R.drawable.yct38, R.drawable.yct39,
			R.drawable.yct40, R.drawable.yct41, R.drawable.yct42,
			R.drawable.yct43, R.drawable.yct44, R.drawable.yct45,
			R.drawable.yct46, R.drawable.yct47, R.drawable.yct48 };
	public static final String[] links = { "http://i.imgur.com/YpTUo.gif",
			"http://i.imgur.com/VHSh2.gif", "http://i.imgur.com/uExGO.gif",
			"http://i.imgur.com/i8TYK.gif", "http://i.imgur.com/zCFyu.gif",
			"http://i.imgur.com/PE9lH.gif", "http://i.imgur.com/jAJ06.gif",
			"http://i.imgur.com/HEQUa.gif", "http://i.imgur.com/UWiqn.gif",
			"http://i.imgur.com/jm2h0.gif", "http://i.imgur.com/fR9ep.gif",
			"http://i.imgur.com/rtuO7.gif", "http://i.imgur.com/MAwIT.gif",
			"http://i.imgur.com/8mjQV.gif", "http://i.imgur.com/r0rbi.gif",
			"http://i.imgur.com/lRTf3.gif", "http://i.imgur.com/xf3ek.gif",
			"http://i.imgur.com/HL1pu.gif", "http://i.imgur.com/9COvH.gif",
			"http://i.imgur.com/GyMST.gif", "http://i.imgur.com/V9JfG.gif",
			"http://i.imgur.com/Fg7hE.gif", "http://i.imgur.com/BU3dp.gif",
			"http://i.imgur.com/waVNo.gif", "http://i.imgur.com/SH15A.gif",
			"http://i.imgur.com/ns9rP.gif", "http://i.imgur.com/gy3sY.gif",
			"http://i.imgur.com/kXA8b.gif", "http://i.imgur.com/ZX60r.gif",
			"http://i.imgur.com/b9g70.gif", "http://i.imgur.com/LSmuC.gif",
			"http://i.imgur.com/Sa0HP.gif", "http://i.imgur.com/f0k7B.gif",
			"http://i.imgur.com/VI6Vn.gif", "http://i.imgur.com/YA52z.gif",
			"http://i.imgur.com/RLQSf.gif", "http://i.imgur.com/T0VBt.gif",
			"http://i.imgur.com/7Wg9w.gif", "http://i.imgur.com/DHgP2.gif",
			"http://i.imgur.com/WC0Xd.gif", "http://i.imgur.com/RXcNS.gif",
			"http://i.imgur.com/MnMWz.gif", "http://i.imgur.com/lDBHq.gif",
			"http://i.imgur.com/1UV0h.gif", "http://i.imgur.com/hqfGC.gif",
			"http://i.imgur.com/TrscT.gif", "http://i.imgur.com/QBe2Q.gif",
			"http://i.imgur.com/d7clv.gif", "http://i.imgur.com/wjRQj.gif" };

	public static final HashMap<String, Integer> USER_CLASS = new HashMap<String, Integer>();

	public static HashMap<String, Integer> getUserClass() {
		if (USER_CLASS.size() == 0) {
			USER_CLASS.put("banned", R.drawable.banned);
			USER_CLASS.put("peasant", R.drawable.peasant);
			USER_CLASS.put("user", R.drawable.user);
			USER_CLASS.put("power", R.drawable.power);
			USER_CLASS.put("elite", R.drawable.elite);
			USER_CLASS.put("crazy", R.drawable.crazy);
			USER_CLASS.put("insane", R.drawable.insane);
			USER_CLASS.put("veteran", R.drawable.veteran);
			USER_CLASS.put("extreme", R.drawable.extreme);
			USER_CLASS.put("ultimate", R.drawable.ultimate);
			USER_CLASS.put("nexus", R.drawable.nexus);
			USER_CLASS.put("uploader", R.drawable.uploader);
			USER_CLASS.put("seeder", R.drawable.seeder);
			USER_CLASS.put("encoder", R.drawable.encoder);
			;
			USER_CLASS.put("vip", R.drawable.vip);
			USER_CLASS.put("forummoderator", R.drawable.forummoderator);
			USER_CLASS.put("moderator", R.drawable.moderator);
			USER_CLASS.put("administrator", R.drawable.administrator);
			USER_CLASS.put("sysop", R.drawable.sysop);
			USER_CLASS.put("staffleader", R.drawable.staffleader);
		}
		return USER_CLASS;
	}
}
