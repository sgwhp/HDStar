package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.component.activity.BaseActivity;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.component.activity.HelpActivity;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.component.activity.MiscActivity;
import org.hdstar.component.activity.RemoteActivity;
import org.hdstar.component.activity.SettingActivity;
import org.hdstar.component.activity.SlidingFragmentActivity;
import org.hdstar.component.activity.TorrentActivity;

import android.app.AlertDialog;
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

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list, null);
		view.setBackgroundResource(R.drawable.sliding_menu_bg);
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity());
		// for (int i = 0; i < 20; i++) {
		// adapter.add(new SampleItem("Sample List",
		// android.R.drawable.ic_menu_search));
		// }
		adapter.add(new SampleItem("��̳", R.drawable.menu_forum));
		adapter.add(new SampleItem("����", R.drawable.menu_torrent));
		adapter.add(new SampleItem("Զ�̿���", R.drawable.menu_remote));
		adapter.add(new SampleItem("����", R.drawable.menu_misc));
		adapter.add(new SampleItem("��Ϣ����", R.drawable.menu_message));
		adapter.add(new SampleItem("����", R.drawable.menu_setting));
		adapter.add(new SampleItem("����", R.drawable.menu_help));
		adapter.add(new SampleItem("�˳�", R.drawable.menu_exit));
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
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
			klass = RemoteActivity.class;
			break;
		case 3:
			klass = MiscActivity.class;
			break;
		case 4:
			klass = MessageActivity.class;
			break;
		case 5:
			klass = SettingActivity.class;
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
			Intent intent = new Intent(this.getActivity(), klass);
			this.startActivity(intent);
			this.getActivity().finish();
		}
	}

	private void checkExit() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm)
				.setMessage(R.string.exit_message)
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								getActivity().finish();
							}

						})
				.setNegativeButton(R.string.cancel,
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
			TextView tip = (TextView) convertView.findViewById(R.id.tip);
			if (position == 4 && BaseActivity.newMessageNum > 0) {
				tip.setText(BaseActivity.newMessageNum + "");
				tip.setVisibility(View.VISIBLE);
			} else {
				tip.setVisibility(View.GONE);
			}
			return convertView;
		}

	}
}