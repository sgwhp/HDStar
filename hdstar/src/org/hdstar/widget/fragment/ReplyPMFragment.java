package org.hdstar.widget.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.OriginTask;
import org.hdstar.util.MyTextParser;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.ResizeLayout;
import org.hdstar.widget.SmilesAdapter;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;
import android.widget.ToggleButton;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class ReplyPMFragment extends StackFragment {

	private int msgId;
	private int receiverId;
	private String subject;
	private String text;
	private EditText body = null;
	private Button button = null;
	private CustomDialog dialog = null;
	private View v;

	public static ReplyPMFragment newInstance(int msgId, String subject,
			String text, int receiverId) {
		Bundle args = new Bundle();
		ReplyPMFragment fragment = new ReplyPMFragment();
		args.putInt("msgId", msgId);
		args.putString("subject", subject);
		args.putString("text", text);
		args.putInt("receiverId", receiverId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		msgId = bundle.getInt("msgId");
		subject = bundle.getString("subject");
		receiverId = bundle.getInt("receiverId");
		text = bundle.getString("text");
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
		init(text);
	}

	void init(String text) {
		final Context context = getActivity();
		final InputMethodManager im = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		final MyTextParser parser = new MyTextParser(context);
		body = (EditText) v.findViewById(R.id.body);
		text = MyTextParser.toReplyPM(getActivity(), receiverId, text);
		body.setText(text);
		body.setSelection(text.length());
		body.clearFocus();
		button = (Button) v.findViewById(R.id.commit);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (body.getText().toString().equals("")) {
					Toast.makeText(context, R.string.reply_is_empty,
							Toast.LENGTH_SHORT).show();
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
					// body += "\n��ʹ��" + CustomSetting.DEVICE + "�ظ���";
					List<NameValuePair> nvp = new ArrayList<NameValuePair>();
					nvp.add(new BasicNameValuePair("origmsg", msgId + ""));
					nvp.add(new BasicNameValuePair("body", body));
					nvp.add(new BasicNameValuePair("color", 0 + ""));
					nvp.add(new BasicNameValuePair("delete", "yes"));
					nvp.add(new BasicNameValuePair("font", 0 + ""));
					nvp.add(new BasicNameValuePair("receiver", receiverId + ""));
					nvp.add(new BasicNameValuePair("size", 0 + ""));
					nvp.add(new BasicNameValuePair("subject", "Re: " + subject));
					nvp.add(new BasicNameValuePair("returnto",
							Const.Urls.VIEW_MESSAGE_URL + msgId));
					try {
						task.execPost(Const.Urls.REPLY_PM_URL, nvp, "");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}

		});

		final SmilesAdapter smiles = new SmilesAdapter(context);
		final GridView grid = (GridView) v.findViewById(R.id.content);
		final ResizeLayout root = (ResizeLayout) v
				.findViewById(R.id.root_layout);
//		 im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//		 body.requestFocus();
//		 grid.setVisibility(View.GONE);
		final LinearLayout lin = (LinearLayout) v
				.findViewById(R.id.linearLayout3);
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
					// sd.open();
					lin.setVisibility(View.VISIBLE);
					// im.hideSoftInputFromWindow(root.getWindowToken(), 0);
					im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					smile.setBackgroundResource(R.drawable.keyboard);
				} else {
					// sd.close();
					lin.setVisibility(View.GONE);
					im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					smile.setBackgroundResource(R.drawable.smile);
				}
			}

		});
		// smile.performClick();

		root.setOnResizeListener(new ResizeLayout.OnResizeListener() {

			@Override
			public void OnResize(int w, int h, int oldw, int oldh) {
				if (h < oldh) {
					// ���̵���
					if (smile.isChecked()) {
						// lin.setVisibility(View.GONE);
						im.hideSoftInputFromWindow(root.getWindowToken(), 0);
						smile.performClick();
						im.toggleSoftInput(0,
								InputMethodManager.HIDE_NOT_ALWAYS);
						// im.showSoftInput(root,
						// InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
		});
	}

	private TaskCallback<Void> mCallback = new TaskCallback<Void>() {

		@Override
		public void onComplete(Void result) {
			dialog.dismiss();
			// detachTask();
			Toast.makeText(getActivity(), R.string.reply_succeeded,
					Toast.LENGTH_SHORT).show();
			getViewPager().setCurrentItem(getViewPager().getCurrentItem() - 1);
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			// detachTask();
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
			// detachTask();
		}

	};
}
