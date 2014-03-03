package org.hdstar.common;

public class CommonUrls {
	/**
	 * 
	 * web��������ַ. <br/>
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
		public static final String TORRENTS_URL = BASE_URL
				+ "/torrents.php?page=%d";

		/** �ͻ��˷�������ַ */
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
		/** ruTorrent��ַ */
		public static final String RUTORRENT_HOME_PAGE = "http://%s/rutorrent";
		public static final String RUTORRENT_RPC_ACTION_URL = RUTORRENT_HOME_PAGE
				+ "/plugins/httprpc/action.php";
		public static final String RUTORRENT_RSS_ACTION_URL = RUTORRENT_HOME_PAGE
				+ "/plugins/rss/action.php";
		/** ˢ��rss��ǩ��ַ */
		public static final String RUTORRENT_RSS_REFRESH_URL = RUTORRENT_RSS_ACTION_URL
				+ "?mode=refresh&rss=%s";
		public static final String RUTORRENT_DISK_SPACE_URL = RUTORRENT_HOME_PAGE
				+ "/plugins/diskspace/action.php?_=";
		public static final String RUTORRENT_ADD_URL = RUTORRENT_HOME_PAGE
				+ "/php/addtorrent.php?";

		/** ��Torrent��ַ ��Ϊ��ͳһ��ʽ���������String.format()��token������ط�����λ */
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
		public static final String UTORRENT_SET_LABEL_URL = UTORRENT_BASE_REQ_URL
				+ "&action=setprops";

		/** transmission��ַ */
		public static final String TRANSMISSION_HOME_PAGE = "http��//%s/transmission/rpc";
	}

	// ��ȡ��վͼ�깤�ߵ�ַ
	public static final String GETFVO_URL = "http://g.etfv.co/%1$s";

	// nexusphp��ȡ��֤���ַ
	public static final String NEXUSPHP_FETCH_SECURITY_IMAGE_URL = "%s/image.php?action=regimage&imagehash=%s";
	public static final String NEXUSPHP_LOGIN_URL = "%s/login.php";
	public static final String NEXUSPHP_TAKE_LOGIN_URL = "%s/takelogin.php";
	public static final String NEXUSPHP_LOGOUT_URL = "%s/logout.php";
	public static final String NEXUSPHP_HOME_PAGE = "%s/index.php";
	public static final String NEXUSPHP_BOOKMARK = "%s/bookmark.php?torrentid=%s";
	public static final String NEXUSPHP_TORRENTS = "%s/torrents.php?page=%d";

	public static class PTSiteUrls {
		// chd
		public static final String CHD = "http://chdbits.org";
		public static final String CHD_RSS_DOWNLOAD_URL = CHD
				+ "/myrss.php?ajax=1&torrentid=%s";
		public static final String CHD_TORRENTS_URL = CHD
				+ "/torrents.php?page=%d";

		// cmct
		public static final String CMCT = "http://hdcmct.org";
		public static final String CMCT_RSS_DOWNLOAD_URL = CMCT
				+ "/subscribe.php?ajax=1&torrentid=%s";
		public static final String CMCT_TORRENTS_URL = CMCT
				+ "/torrents.php?page=%d";

		// hdw
		public static final String HDW = "http://hdwing.com";
		public static final String HDW_HOME_PAGE = HDW + "/index.php";
		public static final String HDW_TORRENTS_URL = HDW
				+ "/browse.php?page=%d";
		// Rnd��ʱ�䣬��λ����
		public static final String HDW_RSS_DOWNLOAD_URL = HDW
				+ "/_ajax_addtobasket.php?Rnd=%d";
		public static final String HDW_LOGIN = HDW + "/login.php";
		public static final String HDW_TAKE_LOGIN = HDW + "/takelogin.php";
		public static final String HDW_LOGOUT = HDW + "/logout.php";
		// tm �������ˢ��ʱʹ��
		// public static final String HDW_GET_SECURITY_IMG =
		// "/validatecode.php?tm=%d";
		public static final String HDW_GET_SECURITY_IMG = HDW
				+ "/validatecode.php";
		public static final String HDW_BOOKMARK = HDW
				+ "/bookmark.php?torrent=?%d";
		// post������delbookmark������id����
		public static final String HDW_DEL_BOOKMARK = HDW
				+ "/takedelbookmarks.php";

		// mt
		public static final String MT = "https://tp.m-team.cc";
		public static final String MT_TORRENTS_URL = MT
				+ "/torrents.php?page=%d";
		public static final String MT_ADULT_URL = MT + "/adult.php?page=%d";

		// OpenCD�ĵ�¼ҳ��ֻ����https������ͳһʹ��https
		public static final String OPEN_CD = "https://open.cd";
		public static final String OPEN_CD_RSS_DOWNLOAD_URL = OPEN_CD
				+ "/bookmark.php?type=1&level=0&cmd=&torrentid=%s";
		public static final String OPEN_CD_TORRENTS_URL = OPEN_CD
				+ "/torrents.php?boardid=2&page=%d";
		public static final String OPEN_CD_MUSIC_URL = OPEN_CD
				+ "/torrents.php?boardid=1&page=%d";

		// ttg
		public static final String TTG = "http://ttg.im";
		public static final String TTG_TORRENTS = TTG
				+ "/browse.php?c=M&page=%d";
		public static final String TTG_GAME = TTG + "/browse.php?c=G&page=%d";
		public static final String TTG_LOGOUT = TTG + "/logout.php";
		public static final String TTG_TAKE_LOGIN = TTG + "/takelogin.php";
		public static final String TTG_MY = TTG + "/my.php";
		public static final String TTG_BOOKMARK = TTG
				+ "/bookmark.php?torrent=?%d";
		public static final String TTG_RSS_DOWNLOAD_URL = TTG + "/mycart.php";

	}
}
