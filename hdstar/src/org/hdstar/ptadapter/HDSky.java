package org.hdstar.ptadapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.parser.DefaultGetParser;
import org.hdstar.task.parser.DelegateGetParser;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import com.google.gson.reflect.TypeToken;

public class HDSky extends NexusPHP {

	public HDSky() {
		super(PTSiteType.HDSky);
	}

	@Override
	public boolean needSecurityCode() {
		return true;
	}

    @Override
    public boolean rssEnable() {
        return true;
    }

    @Override
    public BaseAsyncTask<Boolean> addToRss(String torrentId) {
        HttpGet get = new HttpGet(String.format(
                CommonUrls.HDStar.RSS_DOWNLOAD_URL, torrentId));
        BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie, get,
                new DefaultGetParser());
        return task;
    }

	@Override
	public BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page,
			String keywords) {
		String url = CommonUrls.HDStar.SERVER_TORRENTS_URL + "?page=" + page;
		if (keywords != null && !"".equals(keywords)) {
			try {
				url += "&search=" + URLEncoder.encode(keywords, Const.CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		HttpGet get = new HttpGet(url);
		BaseAsyncTask<ArrayList<Torrent>> task = BaseAsyncTask.newInstance(
				cookie, get, new DelegateGetParser<ArrayList<Torrent>>(
						new TypeToken<ResponseWrapper<List<Torrent>>>() {
						}.getType()));
		return task;
	}
}
