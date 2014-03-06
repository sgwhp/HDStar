package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.slidingmenu.lib.SlidingMenu;

/**
 * 帮助
 * 
 * @author robust
 * 
 */
@SuppressLint("ValidFragment")
public class HelpActivity extends BaseActivity {
	private WebView aboutPage;
	private PullToRefreshLayout mPullToRefreshLayout;
	private boolean loading = false;

	public HelpActivity() {
		super(R.string.help);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
		aboutPage = (WebView) findViewById(R.id.about_page);
		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
		ActionBarPullToRefresh
				.from(this)
				.options(
						Options.create().refreshOnUp(true)
								.headerLayout(R.layout.cancelable_header)
								.headerTransformer(transformer).build())
				.theseChildrenArePullable(R.id.about_page)
				.listener(new OnRefreshListener() {

					@Override
					public void onRefreshStarted(View view) {
						// 避免onPageStarted调用刷新时重复加载
						if (!loading) {
							aboutPage.reload();
						}
					}
				}).setup(mPullToRefreshLayout);

		transformer.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				loading = false;
				mPullToRefreshLayout.setRefreshComplete();
			}
		});
		aboutPage.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 在当前的webview中跳转到新的url，防止弹出浏览器选择
				view.loadUrl(url);

				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				loading = true;
				mPullToRefreshLayout.setRefreshing(true);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				loading = false;
				mPullToRefreshLayout.setRefreshComplete();
			}
		});
		aboutPage.getSettings().setJavaScriptEnabled(true);
		mPullToRefreshLayout.post(new Runnable() {
			@Override
			public void run() {
				mPullToRefreshLayout.setRefreshing(true);
				aboutPage.loadUrl(CommonUrls.HDStar.SERVER_ABOUT_URL);
			}
		});
	}

	@Override
	public void onBackPressed() {
		// 返回
		if (aboutPage.canGoBack()) {
			aboutPage.goBack();
			return;
		}
		super.onBackPressed();
		return;
	}
}
