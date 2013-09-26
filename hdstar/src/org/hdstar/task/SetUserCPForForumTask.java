package org.hdstar.task;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.util.CustomHttpClient;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import android.os.AsyncTask;

public class SetUserCPForForumTask extends AsyncTask<String, String, Boolean> {
	private HttpPost post = null;
	private final String URL = "http://www.hdstar.org/usercp.php";
	/*
	 * https://www.hdstar.org/usercp.php
	 * action forum 
	 * avatars on 
	 * clicktopic firstpage 
	 * postsperpage 20 (0)
	 * signature 施主请自重，老衲把持不住~
	 * [url=http://weibo.com/1874459180?s=6uyXnP][img]http://service
	 * .t.sina.com.cn/widget/qmd/1874459180/d18450f9/4.png[/img][/url]
	 * signatures on 
	 * topicsperpage 20 (0)
	 * ttlastpost on 
	 * type save
	 * 
	 * 
	 * action	forum
clicktopic	lastpage
postsperpage	0
signature	施主请自重，老衲把持不住~ [url=http://weibo.com/1874459180?s=6uyXnP][img]http://service.t.sina.com.cn/widget/qmd/1874459180/d18450f9/4.png[/img][/url]
topicsperpage	0
type	save
	 */

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		HttpClient client = CustomHttpClient.getHttpClient();
		post = new HttpPost(URL);
		post.setHeader("Cookie", params[0]);
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("action", "forum"));
		nvp.add(new BasicNameValuePair("avatars", params[1]));
		nvp.add(new BasicNameValuePair("clicktopic", params[2]));
		nvp.add(new BasicNameValuePair("postsperpage", params[3]));
		nvp.add(new BasicNameValuePair("signature", params[4]));
		nvp.add(new BasicNameValuePair("signatures", params[5]));
		nvp.add(new BasicNameValuePair("topicsperpage", params[6]));
		nvp.add(new BasicNameValuePair("ttlatpost", params[7]));
		nvp.add(new BasicNameValuePair("type", "save"));
		try{
			post.setEntity(new UrlEncodedFormEntity(nvp, "UTF-8"));
			HttpResponse response = client.execute(post);
			if(response.getStatusLine().getStatusCode() == 302){
				return true;
			}
		} catch(ConnectTimeoutException e){
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch(SocketException e){
			e.printStackTrace();
			post.abort();
			if("Connection reset by peer".equals(e.getMessage())){
				CustomHttpClient.restClient();
			}
		} catch(IOException e){
			post.abort();
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean result){
		
	}
	
	public void abort(){
		post.abort();
	}

}