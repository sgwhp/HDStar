package org.hdstar.common;

public class CommonUrls {
	/**
	 * 
	 * web服务器地址. <br/>
	 * 
	 * @author robust
	 */
	public static class HDStar {
		public static final String BASE_URL = "http://hdsky.me";
		public static final String TAKE_LOGIN_URL = BASE_URL + "/takelogin.php";
		public static final String HOME_PAGE = BASE_URL + "/index.php";
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
		public static String SERVER_ABOUT_URL;

		public static void initServerAddr(final String serverAddr) {
			SERVER_ADDRESS = serverAddr;
			SERVER_VIEW_FORUM_URL = SERVER_ADDRESS + "viewForum";
			SERVER_VIEW_TOPIC_URL = SERVER_ADDRESS + "viewtopic";
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
			SERVER_ABOUT_URL = SERVER_ADDRESS + "about.jsp";
		}
	}

	public static class BTClient {
		/** ruTorrent地址 */
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
		public static final String RUTORRENT_ADD_URL = RUTORRENT_HOME_PAGE
				+ "/php/addtorrent.php?";

		/**
		 * μTorrent地址 。为了统一格式，方便调用String.format()，token参数务必放在首位
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
		public static final String UTORRENT_ADD_URL = UTORRENT_BASE_REQ_URL
				+ "&action=add-url&s=%s";
	}

	// 获取网站图标工具地址
	public static final String GETFVO_URL = "http://g.etfv.co/%1$s";

	// nexusphp获取验证码地址
	public static final String NEXUSPHP_FETCH_SECURITY_IMAGE_URL = "%s/image.php?action=regimage&imagehash=%s";
	public static final String NEXUSPHP_LOGIN_URL = "%s/login.php";
	public static final String NEXUSPHP_TAKE_LOGIN_URL = "%s/takelogin.php";
	public static final String NEXUSPHP_HOME_PAGE = "%s/index.php";

	public static class PTSiteUrls {
		public static final String CHDBITS = "https://chdbits.org";
		public static final String CMCT = "http://hdcmct.org";
		public static final String M_TEAM = "http://tp.m-team.cc";
		public static final String OPEN_CD = "http://open.cd";
	}
}
