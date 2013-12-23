package org.hdstar.widget.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.OriginTask;
import org.hdstar.util.MyTextParser;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.ResizeLayout;
import org.hdstar.widget.SmilesAdapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class NewTopicFragment extends StackFragment {
	private EditText subject, body;
	private Button button = null;
	private int id = 0;
	private CustomDialog dialog = null;
	private View v;

	public static NewTopicFragment newInstance(int id) {
		Bundle args = new Bundle();
		NewTopicFragment fragment = new NewTopicFragment();
		args.putInt("id", id);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = getArguments().getInt("id");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.new_topic, null);
		subject = (EditText) v.findViewById(R.id.subject);
		body = (EditText) v.findViewById(R.id.body);
		button = (Button) v.findViewById(R.id.commit);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	void init() {
		final Activity context = getActivity();
		final InputMethodManager im = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		final MyTextParser parser = new MyTextParser(context);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String subjectStr = subject.getText().toString();
				String bodyStr = body.getText().toString();
				if (bodyStr.equals("")) {
					Crouton.makeText(context, R.string.body_is_empty,
							Style.CONFIRM).show();
				} else if (subjectStr.equals("")) {
					Crouton.makeText(context, R.string.subject_is_empty,
							Style.CONFIRM).show();
				} else {
					dialog = new CustomDialog(context, R.string.topic_is_adding);
					dialog.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							detachTask();
						}
					});
					dialog.show();
					detachTask();
					OriginTask<Void> task = OriginTask
							.newInstance(HDStarApp.cookies);
					task.attach(mCallback);
					attachTask(task);
					bodyStr = parser.toImg(bodyStr) + "\n（使用"
							+ CustomSetting.device + "发布）";
					List<NameValuePair> nvp = new ArrayList<NameValuePair>();
					nvp.add(new BasicNameValuePair("id", id + ""));
					nvp.add(new BasicNameValuePair("type", "new"));
					nvp.add(new BasicNameValuePair("subject", subjectStr));
					nvp.add(new BasicNameValuePair("color", "0"));
					nvp.add(new BasicNameValuePair("font", "0"));
					nvp.add(new BasicNameValuePair("size", "0"));
					nvp.add(new BasicNameValuePair("body", bodyStr));
					try {
						task.execPost(Const.Urls.NEW_TOPIC_URL, nvp);
					} catch (UnsupportedEncodingException e) {
						dialog.dismiss();
						e.printStackTrace();
					}
				}
			}

		});

		final SmilesAdapter smiles = new SmilesAdapter(context);
		final GridView grid = (GridView) v.findViewById(R.id.content);
		final ResizeLayout root = (ResizeLayout) v
				.findViewById(R.id.root_layout);
		final LinearLayout lin = (LinearLayout) v.findViewById(R.id.smilies);
		lin.setVisibility(View.GONE);
		grid.setAdapter(smiles);
		grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				int index = body.getSelectionStart();
				Editable editable = body.getText();
				String img = "[sm" + position + "]";
				editable.insert(index, img);
				body.setText(parser.toSpan(editable.toString()));
				body.setSelection(index + img.length());
			}

		});

		final ToggleButton smile = (ToggleButton) v.findViewById(R.id.smile);
		smile.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					lin.setVisibility(View.VISIBLE);
					im.hideSoftInputFromWindow(root.getWindowToken(), 0);
					smile.setBackgroundResource(R.drawable.keyboard);
				} else {
					lin.setVisibility(View.GONE);
					im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					smile.setBackgroundResource(R.drawable.smile);
				}
			}

		});

		root.setOnResizeListener(new ResizeLayout.OnResizeListener() {

			@Override
			public void OnResize(int w, int h, int oldw, int oldh) {
				if (h < oldh) {
					// 键盘弹出
					if (smile.isChecked()) {
						im.hideSoftInputFromWindow(root.getWindowToken(), 0);
						smile.performClick();
						im.toggleSoftInput(0,
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
		});
	}

	private TaskCallback<Void> mCallback = new TaskCallback<Void>() {

		@Override
		public void onComplete(Void result) {
			dialog.dismiss();
			Crouton.makeText(getActivity(), R.string.add_topic_succeeded,
					Style.INFO).show();
			StackFragment f = null;
			if (CustomSetting.autoRefresh) {
				f = getStackAdapter().preItem();
			}
			getViewPager().setCurrentItem(getViewPager().getCurrentItem() - 1,
					true);
			if (f != null) {
				f.refresh();
			}
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

	};
}
