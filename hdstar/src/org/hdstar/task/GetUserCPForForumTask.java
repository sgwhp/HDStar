package org.hdstar.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;

import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;

import android.os.AsyncTask;

public class GetUserCPForForumTask extends AsyncTask<String, String, HashMap<String, String>> {
	private HttpGet get = null;
	private final String URL = "http://www.hdstar.org/usercp.php?action=forum";

	@Override
	protected HashMap<String, String> doInBackground(String... params) {
		// TODO Auto-generated method stub
		HttpClient client = CustomHttpClient.getHttpClient();
		InputStream in = null;
		get = new HttpGet(URL);
		get.setHeader("Cookie", params[0]);
		try{
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			Document doc = Jsoup.parse(in, "utf-8", "");
			Elements es = doc.getElementsByTag("input");
			for(int i = 0; i < es.size(); i++){
				System.out.println("-->" + es.get(i).attr("value"));
			}
		} catch(SocketException e){
			e.printStackTrace();
			get.abort();
			if("Connection reset by peer".equals(e.getMessage())){
				CustomHttpClient.restClient();
			}
		} catch(IOException e){
			get.abort();
			e.printStackTrace();
		} finally{
			IOUtils.closeInputStreamIgnoreExceptions(in);
			get.releaseConnection();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(HashMap<String, String> result){
		
	}
	
	public void abort(){
		get.abort();
	}

}



/*
 * <form method=post action=usercp.php><input type=hidden name=action value=forum><input type=hidden name=type value=save><tr><td colspan=2 class="heading" valign="top" align="center"><font color=red>�ѱ��棡</font></td></tr>
<tr><td width="1%" class="rowhead nowrap" valign="top" align="right">ÿҳ������</td><td width="99%" class="rowfollow" valign="top" align="left"><input type=text size=10 name=topicsperpage value=20>(0=ʹ��Ĭ�ϣ����=100)</td></tr>
<tr><td width="1%" class="rowhead nowrap" valign="top" align="right">ÿҳ������</td><td width="99%" class="rowfollow" valign="top" align="left"><input type=text size=10 name=postsperpage value=20> (0=ʹ��Ĭ�ϣ����=100)</td></tr>
<tr><td width="1%" class="rowhead nowrap" valign="top" align="right">�鿴ͷ��</td><td width="99%" class="rowfollow" valign="top" align="left"><input type=checkbox name=avatars checked>(խ���û�������Ҫ�رմ���)</td></tr>
<tr><td width="1%" class="rowhead nowrap" valign="top" align="right">�鿴ǩ����</td><td width="99%" class="rowfollow" valign="top" align="left"><input type=checkbox name=signatures checked>(խ���û�������Ҫ�رմ���)</td></tr>
<tr><td class="rowhead nowrap" valign="top" align="right">������ʾ��������</td><td class="rowfollow" valign="top" align="left"><input type=checkbox name=ttlastpost checked>(�����ϲ��������ʾ���ɹرմ���)</td></tr>
<tr><td width="1%" class="rowhead nowrap" valign="top" align="right">�������ʱ</td><td width="99%" class="rowfollow" valign="top" align="left"><input type=radio name=clicktopic checked value="firstpage">�鿴��һҳ<input type=radio name=clicktopic value="lastpage">�鿴���ҳ</td></tr>
<tr><td width="1%" class="rowhead nowrap" valign="top" align="right">�ҵ�ǩ����</td><td width="99%" class="rowfollow" valign="top" align="left"><textarea name=signature style="width:700px" rows=10>ʩ�������أ����İѳֲ�ס~

[url=http://weibo.com/1874459180?s=6uyXnP][img]http://service.t.sina.com.cn/widget/qmd/1874459180/d18450f9/4.png[/img][/url]</textarea><br />����ʹ��<a class=faqlink href=tags.php target=_new>BBCode����</a>�����ͼƬ�ߴ�Ϊ500*200��ֻ�е�һ��ͼƬ����ʾ��</td></tr>
<tr><td class="rowhead" valign="top" align="right">�����趨</td><td class="rowfollow" valign="top" align=left><input type=submit value="�����趨��&nbsp;(ֻ����һ��)"></td></tr></form>*/
