package org.hdstar.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
	private boolean isInterrupted = false;
	private WeakReference<Context> ref = null;
	Bitmap downloadImage = null;
	String imageHash = null;
	HttpGet get = null;

	public DownloadImageTask(Context context) {
		// mContext = context;
		ref = new WeakReference<Context>(context);
		// mToast = new MyToast(context);
	}

	protected void setContext(Context context) {
		// mContext = context;
		ref = new WeakReference<Context>(context);
		/*
		 * if(progress >= 0){ publishProgress(this.progress); }
		 */
	}

	public void interrupt(boolean isInterrupted) {
		this.isInterrupted = isInterrupted;
		if (get != null)
			get.abort();
	}

	protected void onPreExecute() {
		Context context = ref.get();
		if (context != null) {
			Toast.makeText(context, R.string.get_security_code,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		String url = getImageUrl(params[0]);
		if (url != null) {
			imageHash = url;
			url = "http://hdsky.me/image.php?action=regimage&imagehash=" + url;
			return downloadImage(url);
		}
		return null;
	}

	protected void onProgressUpdate(Integer... progress) {
		// TextView mText =
		// (TextView)((Activity)mContext).findViewById(R.id.textView1);
		// mText.setText("Progress so far: " + progress[0]);
	}

	protected void onPostExecute(Bitmap result) {
		// mToast.hide();
		if (result != null) {
			downloadImage = result;
			setImageInView();
		} else {
			Context context = ref.get();
			if (context != null) {
				Toast.makeText(ref.get(),
						R.string.failed_to_download_security_code,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	String getImageUrl(String url) {
		// System.out.println(NetType(mContext));
		// System.out.println(url);
		HttpClient client = CustomHttpClient.getHttpClient();
		get = new HttpGet(url);
		BufferedReader reader = null;
		InputStream in = null;
		try {
			String str;
			Pattern pattern = Pattern.compile("imagehash=(.*?)\"");
			Matcher matcher;
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			reader = new BufferedReader(new InputStreamReader(in));
			while ((str = reader.readLine()) != null) {
				matcher = pattern.matcher(str);
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		} catch (ConnectTimeoutException e) {
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			get.abort();
			if ("Connection reset by peer".equals(e.getMessage())) {
				CustomHttpClient.restClient();
			}
		} catch (Exception e) {
			get.abort();
			e.printStackTrace();
		} finally {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			get.releaseConnection();
		}
		return null;
	}

	public Bitmap downloadImage(String... urls) {
		HttpClient httpClient = CustomHttpClient.getHttpClient();
		get = new HttpGet(urls[0]);
		try {
			if (isInterrupted)
				return null;
			HttpResponse response = httpClient.execute(get);
			byte[] image = EntityUtils.toByteArray(response.getEntity());
			Bitmap mBitmap = BitmapFactory.decodeByteArray(image, 0,
					image.length);
			return mBitmap;
		} catch (ConnectTimeoutException e) {
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			if ("Connection reset by peer".equals(e.getMessage())) {
				CustomHttpClient.restClient();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.abort();
		}
		return null;
	}

	protected void setImageInView() {
		if (downloadImage != null && ref.get() != null) {
			ImageView mImage = (ImageView) ((Activity) ref.get())
					.findViewById(R.id.security_image);
			// downloadImage.setDensity(160);
			mImage.setImageBitmap(downloadImage);
			downloadImage = null;
		}
	}

	public String getHash() {
		return imageHash;
	}

	/*
	 * public String NetType(Context context) { try { ConnectivityManager cm =
	 * (ConnectivityManager) context
	 * .getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo info =
	 * cm.getActiveNetworkInfo(); String typeName =
	 * info.getTypeName().toLowerCase(); // WIFI/MOBILE if
	 * (typeName.equalsIgnoreCase("wifi")) { } else { typeName =
	 * info.getExtraInfo().toLowerCase(); //
	 * 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap } return typeName; }
	 * catch (Exception e) { return null; } }
	 */

}