package uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class CancelableHeaderTransformer extends BothHeaderTransformer {
	private TextView mCancelBtn;
	private OnCancelListener mOnCancelListener;

	@Override
	public void onViewCreated(Activity activity, View headerView) {
		super.onViewCreated(activity, headerView);
		mCancelBtn = (TextView) headerView.findViewById(R.id.ptr_cancel);
		if (mCancelBtn != null) {
			mCancelBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mOnCancelListener != null) {
						mOnCancelListener.onCancel();
					}
				}
			});
		}
	}

	@Override
	public void onReset() {
		super.onReset();
		if (mCancelBtn != null) {
			mCancelBtn.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onRefreshStarted() {
		super.onRefreshStarted();
		if (mCancelBtn != null) {
			mCancelBtn.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onRefreshMinimized() {
	}

	public void setOnCancelListener(OnCancelListener onCancelListener) {
		mOnCancelListener = onCancelListener;
	}

	public static interface OnCancelListener {
		public void onCancel();
	}
}
