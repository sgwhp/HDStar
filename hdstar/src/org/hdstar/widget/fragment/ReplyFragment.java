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
import org.hdstar.widget.adapter.SmilesAdapter;

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
import android.widget.TextView;
import android.widget.ToggleButton;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ReplyFragment extends StackFragment {

	private String topicID = "";
	private String type;
	private EditText body = null;
	private Button button = null;
	private CustomDialog dialog = null;
	private View v;
	private ToggleButton smile;
	private ResizeLayout root;
	private InputMethodManager im;
	private LinearLayout lin;

	public static ReplyFragment newInstance(String topicID, String text,
			String username, String type) {
		Bundle args = new Bundle();
		ReplyFragment fragment = new ReplyFragment();
		args.putString("topicID", topicID);
		args.putString("text", text);
		args.putString("username", username);
		args.putString("type", type);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		topicID = getArguments().getString("topicID");
		type = getArguments().getString("type");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.reply, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
		init(getArguments().getString("text"),
				getArguments().getString("username"));
	}

	void init(String text, String username) {
		final Activity context = getActivity();
		im = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if ("edit".equals(type)) {
			((TextView) v.findViewById(R.id.title)).setText(R.string.edit);
		}
		final MyTextParser parser = new MyTextParser(context);
		body = (EditText) v.findViewById(R.id.body);
		if (username != null) {
			text = MyTextParser.toQuote(text, username);
			body.setText(text);
			body.setSelection(text.length());
		} else if (text != null) {
			// text = parser.toImg(text);
			text = MyTextParser.toEdit(text);
			body.setText(text);
		}
		button = (Button) v.findViewById(R.id.commit);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (body.getText().toString().equals("")) {
					Crouton.makeText(context, R.string.reply_is_empty,
							Style.CONFIRM).show();
				} else {
					dialog = new CustomDialog(context, R.string.reply_is_adding);
					dialog.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface arg0) {
							detachTask();
						}

					});
					dialog.show();
					detachTask();
					OriginTask<Void> task = OriginTask
							.newInstance(HDStarApp.cookies);
					task.attach(mCallback);
					attachTask(task);
					String body = ((EditText) v.findViewById(R.id.body))
							.getText().toString();
					body = parser.toImg(body);
					body += "\n（使用" + CustomSetting.device + "回复）";
					List<NameValuePair> nvp = new ArrayList<NameValuePair>();
					nvp.add(new BasicNameValuePair("id", topicID));
					nvp.add(new BasicNameValuePair("type", type));
					nvp.add(new BasicNameValuePair("body", body));
					try {
						task.execPost(Const.Urls.REPLY_URL, nvp, "");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}

		});

		final SmilesAdapter smiles = new SmilesAdapter(context);
		final GridView grid = (GridView) v.findViewById(R.id.content);
		root = (ResizeLayout) v.findViewById(R.id.root_layout);
		lin = (LinearLayout) v.findViewById(R.id.smilies);
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

		smile = (ToggleButton) v.findViewById(R.id.smile);
		smile.setOnCheckedChangeListener(checkListener);

		root.setOnResizeListener(resizeListener);
	}

	private OnCheckedChangeListener checkListener = new OnCheckedChangeListener() {

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

	};

	private ResizeLayout.OnResizeListener resizeListener = new ResizeLayout.OnResizeListener() {

		@Override
		public void OnResize(int w, int h, int oldw, int oldh) {
			if (h < oldh) {
				// 键盘弹出
				if (smile.isChecked()) {
					im.hideSoftInputFromWindow(root.getWindowToken(), 0);
					smile.performClick();
					im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		}
	};

	private TaskCallback<Void> mCallback = new TaskCallback<Void>() {

		@Override
		public void onComplete(Void result) {
			dialog.dismiss();
			Crouton.makeText(getActivity(), R.string.reply_succeeded,
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
