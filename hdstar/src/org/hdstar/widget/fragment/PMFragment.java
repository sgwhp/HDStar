package org.hdstar.widget.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.MessageContent;
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
import android.widget.ToggleButton;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PMFragment extends StackFragment {

	private int msgId;
	private int senderId;
	private int receiverId;
	private String subject;
	private String text;
	private EditText body, subjectEt;
	private Button button;
	private CustomDialog dialog;
	private View v;

	public static PMFragment newInstance(int msgId, String subject,
			MessageContent content) {
		Bundle args = new Bundle();
		PMFragment fragment = new PMFragment();
		args.putInt("msgId", msgId);
		args.putInt("senderId", content.senderId);
		args.putString("subject", subject);
		args.putString("text", content.content);
		args.putInt("receiverId", content.receiverId);
		fragment.setArguments(args);
		return fragment;
	}

	public static PMFragment newInstance(int receiverId) {
		Bundle args = new Bundle();
		PMFragment fragment = new PMFragment();
		args.putInt("receiverId", receiverId);
		fragment.setArguments(args);
		return fragment;
	}

	public static PMFragment newInstance(String url) {
		Pattern pattern = Pattern.compile(Const.Urls.SEND_PM_URL + "(\\d)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return newInstance(Integer.parseInt(matcher.group(1)));
		}
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		msgId = bundle.getInt("msgId");
		senderId = bundle.getInt("senderId");
		subject = bundle.getString("subject");
		receiverId = bundle.getInt("receiverId");
		text = bundle.getString("text");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.new_topic, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init(text);
	}

	void init(String text) {
		final Activity context = getActivity();
		final InputMethodManager im = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		final MyTextParser parser = new MyTextParser(context);
		body = (EditText) v.findViewById(R.id.body);
		subjectEt = (EditText) v.findViewById(R.id.subject);
		if (text != null) {
			text = MyTextParser.toReplyPM(getActivity(), senderId, text);
			subjectEt.setVisibility(View.GONE);
			body.setText(text);
			body.setSelection(text.length());
			body.clearFocus();
		}
		button = (Button) v.findViewById(R.id.commit);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (body.getText().toString().equals("")) {
					Crouton.makeText(context, R.string.reply_is_empty,
							Style.CONFIRM).show();
					return;
				} else if (msgId == 0
						&& subjectEt.getText().toString().equals("")) {
					Crouton.makeText(context, R.string.subject_is_empty,
							Style.CONFIRM).show();
					return;
				}
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
				String body = ((EditText) v.findViewById(R.id.body)).getText()
						.toString();
				body = parser.toImg(body);
				List<NameValuePair> nvp = new ArrayList<NameValuePair>();
				if (msgId != 0) {
					// 回复
					nvp.add(new BasicNameValuePair("origmsg", msgId + ""));
					nvp.add(new BasicNameValuePair("subject", "Re: "
							+ MyTextParser.toReplySubject(subject)));
				} else {
					// 新增
					nvp.add(new BasicNameValuePair("subject", subjectEt
							.getText().toString()));
				}
				nvp.add(new BasicNameValuePair("body", body));
				nvp.add(new BasicNameValuePair("color", 0 + ""));
				nvp.add(new BasicNameValuePair("delete", "yes"));
				nvp.add(new BasicNameValuePair("font", 0 + ""));
				nvp.add(new BasicNameValuePair("receiver", receiverId + ""));
				nvp.add(new BasicNameValuePair("size", 0 + ""));
				nvp.add(new BasicNameValuePair("returnto", Const.Urls.HOME_PAGE));
				try {
					task.execPost(Const.Urls.REPLY_PM_URL, nvp,
							Const.Urls.HOME_PAGE);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
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
			Crouton.makeText(getActivity(), R.string.reply_succeeded,
					Style.INFO).show();
			getViewPager().setCurrentItem(getViewPager().getCurrentItem() - 1,
					true);
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
