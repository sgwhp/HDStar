package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.component.activity.HelpActivity;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.component.activity.MiscActivity;
import org.hdstar.component.activity.RemoteActivity;
import org.hdstar.component.activity.SettingActivity;
import org.hdstar.component.activity.SlidingFragmentActivity;
import org.hdstar.component.activity.TorrentActivity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MenuListFragment extends ListFragment {
	protected SampleAdapter adapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list, null);
		view.setBackgroundResource(R.drawable.sliding_menu_bg);
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new SampleAdapter(getActivity());
		adapter.add(new SampleItem(getString(R.string.forums),
				R.drawable.menu_forum));
		adapter.add(new SampleItem(getString(R.string.torrent),
				R.drawable.menu_torrent));
		adapter.add(new SampleItem(getString(R.string.remote),
				R.drawable.menu_remote));
		adapter.add(new SampleItem(getString(R.string.message),
				R.drawable.menu_message));
		adapter.add(new SampleItem(getString(R.string.setting),
				R.drawable.menu_setting));
		adapter.add(new SampleItem(getString(R.string.misc),
				R.drawable.menu_misc));
		adapter.add(new SampleItem(getString(R.string.help),
				R.drawable.menu_help));
		adapter.add(new SampleItem(getString(R.string.exit),
				R.drawable.menu_exit));
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		@SuppressWarnings("rawtypes")
		Class klass = null;
		switch (position) {
		case 0:
			klass = ForumsActivity.class;
			break;
		case 1:
			klass = TorrentActivity.class;
			break;
		case 2:
			// if (HDStarApp.remote != null) {
			klass = RemoteActivity.class;
			// intent.putExtra("remote", HDStarApp.remote);
			// } else {
			// klass = RemoteLoginActivity.class;
			// }
			break;
		case 3:
			klass = MessageActivity.class;
			break;
		case 4:
			klass = SettingActivity.class;
			break;
		case 5:
			klass = MiscActivity.class;
			break;
		case 6:
			klass = HelpActivity.class;
			break;
		default:
			checkExit();
			return;
		}
		SlidingFragmentActivity sliding = (SlidingFragmentActivity) this
				.getActivity();
		sliding.toggle();
		if (!klass.isInstance(sliding)) {
			intent.setClass(this.getActivity(), klass);
			this.startActivity(intent);
			this.getActivity().finish();
		}
	}

	private void checkExit() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm)
				.setIcon(R.drawable.ic_launcher)
				.setMessage(R.string.exit_message)
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								((NotificationManager) getActivity()
										.getSystemService(
												Context.NOTIFICATION_SERVICE))
										.cancelAll();
								getActivity().finish();
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

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.menu_row, null);

			ImageView icon = (ImageView) convertView
					.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);
			if (position == 3 && HDStarApp.hasNewMessage) {
				convertView.findViewById(R.id.new_icon).setVisibility(
						View.VISIBLE);
			} else {
				convertView.findViewById(R.id.new_icon)
						.setVisibility(View.GONE);
			}
			return convertView;
		}

	}
}
