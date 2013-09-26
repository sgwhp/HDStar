package org.hdstar.component.activity;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.util.CustomHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

public class MiscActivity extends BaseActivity {
	HashMap<String, Integer> map = new HashMap<String, Integer>();

	public MiscActivity() {
		super(R.string.misc);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		new MyAsyncTask().execute("");
	}

	class MyAsyncTask extends AsyncTask<String, Integer, String> {

		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_LONG)
					.show();
		};

		@Override
		protected void onProgressUpdate(Integer... values) {
			Toast.makeText(getApplicationContext(), values[0] + "/14",
					Toast.LENGTH_LONG).show();
		}

		@Override
		protected String doInBackground(String... params) {
			String cookie = HDStarApp.cookies;
			HttpClient client;
			HttpGet get = null;
			for (int i = 0; i < 15; i++) {
				BufferedInputStream in = null;
				try {
					publishProgress(i);
					client = CustomHttpClient.getHttpClient();
					get = new HttpGet(
							"http://hdsky.me/forums.php?action=viewtopic&topicid=20"
									+ "&page=" + i);
					get.setHeader("Cookie", cookie);
					HttpResponse response = client.execute(get);
					in = new BufferedInputStream(response.getEntity()
							.getContent(), 8192);
					Document doc = Jsoup
							.parse(in, "utf-8", Const.Urls.BASE_URL);
					/*
					 * nowrap ��һ����ԭʼ���ߵ���Ϣ������id��ͷ�Ρ�����ʱ���
					 * (��һ����)�ڶ��������༭��������Ϣ���ѱ��ڶ���rowfollow��������
					 * ��������nowrap�е��׸���ҳ�涥���ĸ�����Ϣ
					 */
					Elements userNames = doc.select("span.nowrap");
					/*
					 * rowfollow ��һ���ǻظ���������Ϣ����ͷ�񡢵ȼ����ϴ������������� �ڶ����ǻ������������ݡ�ǩ����
					 * ������pm���ߺ;ٱ����ӵ�����
					 */
					String username;
					Elements es;
					for (int n = 1; n < userNames.size(); n++) {
						int value = 1;
						if (!userNames.get(n).html().contains("(")) {
							n++;
						}
						if (n >= userNames.size()) {
							break;
						}
						es = userNames.get(n).getElementsByTag("a");
						if (es == null || es.size() == 0) {
							continue;
						}
						username = es.get(0).text();
						if (map.containsKey(username)) {
							value = map.get(username) + 1;
						}
						map.put(username, value);
						Log.v("whp", username);
					}
				} catch (ClientProtocolException e) {
					get.abort();
					e.printStackTrace();
				} catch (ConnectTimeoutException e) {
					CustomHttpClient.restClient();
					e.printStackTrace();
				} catch (SocketException e) {
					e.printStackTrace();
					get.abort();
					if ("Connection reset by peer".equals(e.getMessage())) {
						CustomHttpClient.restClient();
					}
				} catch (IOException e) {
					get.abort();
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath() + "/data.txt");
				Iterator<String> it = map.keySet().iterator();
				String str;
				while (it.hasNext()) {
					str = it.next();
					str = str + ": " + map.get(str) + "\n";
					out.write(str.getBytes("utf-8"));
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return null;
		}

	};
}
