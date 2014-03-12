package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.component.DownloadService;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.DownloadActivity;
import org.hdstar.component.activity.LoginActivity;
import org.hdstar.model.NewApkInfo;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.ptadapter.HDSky;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SettingFragment extends StackFragment implements OnClickListener {
	private Button downloadBtn;
	private CustomDialog loadingDialog = null;
	private BaseAsyncTask<Boolean> logoutTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.setting, null);
		downloadBtn = (Button) v.findViewById(R.id.download_btn);
		downloadBtn.setOnClickListener(this);
		v.findViewById(R.id.logOut).setOnClickListener(this);
		v.findViewById(R.id.clearCache).setOnClickListener(this);
		v.findViewById(R.id.checkUpdate).setOnClickListener(this);
		v.findViewById(R.id.animation).setOnClickListener(this);
		v.findViewById(R.id.remote_server).setOnClickListener(this);
		v.findViewById(R.id.rss).setOnClickListener(this);
		v.findViewById(R.id.pt_site).setOnClickListener(this);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentTransaction transaction = getActivity()
				.getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.common_setting,
				CommonSettingFragment.getInstance());
		transaction.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences shared = getActivity().getSharedPreferences(
				Const.DOWNLOAD_SHARED_PREFS, Activity.MODE_PRIVATE);
		if (shared.getInt("status", -1) != -1) {
			downloadBtn.setVisibility(View.VISIBLE);
		} else {
			downloadBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		final Activity act = getActivity();
		switch (v.getId()) {
		case R.id.logOut:
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.log_out)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(R.string.log_out_message)
					.setPositiveButton(R.string.log_out,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									loadingDialog = new CustomDialog(
											getActivity(), R.string.connecting);
									loadingDialog
											.setOnDismissListener(new OnDismissListener() {

												@Override
												public void onDismiss(
														DialogInterface dialog) {
													detachLogoutTask();
												}
											});
									loadingDialog.show();
									logoutTask = new HDSky().logout();
									logoutTask.attach(logoutCallback);
									BaseAsyncTask.commit(logoutTask);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();
			break;
		case R.id.clearCache:
			ImageLoader.getInstance().clearDiscCache();
			Crouton.makeText(act, R.string.cache_cleared, Style.INFO).show();
			break;
		case R.id.checkUpdate:
			Crouton.makeText(act, R.string.searching_for_update, Style.INFO)
					.show();
			DelegateTask<NewApkInfo> task = DelegateTask.newInstance("");
			task.attach(mCallback);
			attachTask(task);
			task.execGet(
					CommonUrls.HDStar.SERVER_CHECK_UPDATE_URL + "?appCode="
							+ Const.APP_CODE + "&packageName="
							+ act.getPackageName() + "&versionCode="
							+ Util.getVersionCode(act),
					new TypeToken<ResponseWrapper<NewApkInfo>>() {
					}.getType());
			break;
		case R.id.download_btn:
			Intent dIntent = new Intent(act, DownloadActivity.class);
			startActivity(dIntent);
			break;
		case R.id.animation:
			push(new AnimSettingFragment());
			break;
		case R.id.remote_server:
			push(new RemoteListFragment());
			break;
		case R.id.rss:
			push(new RssListFragment());
			break;
		case R.id.pt_site:
			push(new PTListFragment());
			break;
		}
	}

	private void detachLogoutTask() {
		if (logoutTask != null) {
			logoutTask.detach();
		}
	}

	private TaskCallback<NewApkInfo> mCallback = new TaskCallback<NewApkInfo>() {

		@Override
		public void onComplete(final NewApkInfo result) {
			if (result == null) {
				Crouton.makeText(getActivity(), R.string.latest_version,
						Style.CONFIRM).show();
				return;
			}
			CharSequence updateInfo = getString(R.string.update_info);
			CharSequence fullSize = Util.formatFileSize(result.size);
			updateInfo = String.format(updateInfo.toString(),
					result.versionName, fullSize);
			if (result.patchSize != 0) {
				// 增量升级
				SpannableString ss = new SpannableString(updateInfo.toString()
						+ "  " + Util.formatFileSize(result.patchSize));
				ss.setSpan(new StrikethroughSpan(), updateInfo.length()
						- fullSize.length() - 1, updateInfo.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				updateInfo = ss;
			}
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.update)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(updateInfo)
					.setPositiveButton(R.string.update,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SharedPreferences shared = getActivity()
											.getSharedPreferences(
													Const.DOWNLOAD_SHARED_PREFS,
													Activity.MODE_PRIVATE);
									Editor editor = shared.edit();
									editor.putString("packageName",
											result.packageName);
									editor.putString("desc", result.desc);
									StringBuilder pics = new StringBuilder();
									for (String pic : result.pics) {
										pics.append(pic).append(" ");
									}
									pics.deleteCharAt(pics.length() - 1);
									editor.putString("pics", pics.toString());
									editor.putString("updateDate",
											result.updateDate);
									editor.putString("versionName",
											result.versionName);
									editor.putInt("versionCode",
											result.versionCode);
									editor.putLong("size", result.size);
									editor.putLong("patchSize",
											result.patchSize);
									editor.commit();
									Intent dlIntent = new Intent(getActivity(),
											DownloadActivity.class);
									startActivity(dlIntent);
									dlIntent = new Intent(getActivity(),
											DownloadService.class);
									dlIntent.putExtra(
											"command",
											DownloadService.COMMAND_DOWNLOAD_ADD);
									dlIntent.putExtra("isPatch",
											result.patchSize != 0);
									getActivity().startService(dlIntent);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

	};

	private TaskCallback<Boolean> logoutCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			loadingDialog.dismiss();
			Activity act = getActivity();
			Editor edit = act.getSharedPreferences(Const.SETTING_SHARED_PREFS,
					Activity.MODE_PRIVATE).edit();
			edit.remove("cookies");
			edit.commit();
			HDStarApp.cookies = null;
			Intent intent = new Intent(act, LoginActivity.class);
			startActivity(intent);
			act.finish();
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			loadingDialog.dismiss();
			Crouton.showText(getActivity(), msgId, Style.ALERT);
		}
	};
}
