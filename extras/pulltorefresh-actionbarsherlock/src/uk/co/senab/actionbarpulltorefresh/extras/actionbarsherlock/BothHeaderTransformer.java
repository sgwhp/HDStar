package uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock;

import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Mode;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class BothHeaderTransformer extends DefaultHeaderTransformer {
	protected Animation mHeaderInAnimation, mHeaderOutAnimation,
			mEndHeaderInAnimation, mEndHeaderOutAnimation;
	private CharSequence mPullFromStartLabel, mReleaseFromStartLabel,
			mPullFromEndLabel, mReleaseFromEndLabel;

	@Override
	public void onViewCreated(Activity activity, View headerView) {
		super.onViewCreated(activity, headerView);

		mPullFromStartLabel = mPullRefreshLabel;
		mReleaseFromStartLabel = mReleaseLabel;

		// Create animations for use later
		mHeaderInAnimation = AnimationUtils.loadAnimation(activity,
				R.anim.ptr_slide_down_in);
		mHeaderOutAnimation = AnimationUtils.loadAnimation(activity,
				R.anim.ptr_push_up_out);
		mEndHeaderInAnimation = AnimationUtils.loadAnimation(activity,
				R.anim.ptr_push_up_in);
		mEndHeaderOutAnimation = AnimationUtils.loadAnimation(activity,
				R.anim.ptr_slide_down_out);

		if (mHeaderOutAnimation != null || mHeaderInAnimation != null) {
			final AnimationCallback callback = new AnimationCallback();
			if (mHeaderOutAnimation != null) {
				mHeaderOutAnimation.setAnimationListener(callback);
			}
			if (mEndHeaderOutAnimation != null) {
				mEndHeaderOutAnimation.setAnimationListener(callback);
			}
		}
	}

	@Override
	protected Drawable getActionBarBackground(Context context) {
		// Get action bar style values...
		TypedArray abStyle = obtainStyledAttrsFromThemeAttr(context,
				R.attr.actionBarStyle, R.styleable.SherlockActionBar);
		try {
			return abStyle
					.getDrawable(R.styleable.SherlockActionBar_background);
		} finally {
			abStyle.recycle();
		}
	}

	@Override
	protected int getActionBarSize(Context context) {
		TypedArray values = context
				.obtainStyledAttributes(R.styleable.SherlockTheme);
		try {
			return values.getDimensionPixelSize(
					R.styleable.SherlockTheme_actionBarSize, 0);
		} finally {
			values.recycle();
		}
	}

	@Override
	protected int getActionBarTitleStyle(Context context) {
		// Get action bar style values...
		TypedArray abStyle = obtainStyledAttrsFromThemeAttr(context,
				R.attr.actionBarStyle, R.styleable.SherlockActionBar);
		try {
			return abStyle.getResourceId(
					R.styleable.SherlockActionBar_titleTextStyle, 0);
		} finally {
			abStyle.recycle();
		}
	}

	@Override
	public boolean showHeaderView(Mode mode) {
		if (mPullFromEndLabel != null) {
			if (mode == Mode.PULL_FROM_END) {
				if (!mPullFromEndLabel.equals(mPullRefreshLabel)) {
					setPullText(mPullFromEndLabel);
				} else {
					mPullRefreshLabel = mPullFromEndLabel;
				}
				mReleaseLabel = mReleaseFromEndLabel;
			} else {
				if (!mPullFromStartLabel.equals(mPullRefreshLabel)) {
					setPullText(mPullFromStartLabel);
				} else {
					mPullRefreshLabel = mPullFromStartLabel;
				}
				mReleaseLabel = mReleaseFromStartLabel;
			}
		}
		if (Build.VERSION.SDK_INT >= super.getMinimumApiLevel()) {
			return super.showHeaderView(mode);
		}
		final View headerView = getHeaderView();
		final boolean changeVis = headerView != null
				&& headerView.getVisibility() != View.VISIBLE;
		if (changeVis) {
			// Show Header
			Animation anim = getAnimation(true, mode);
			if (anim != null) {
				// AnimationListener will call HeaderViewListener
				headerView.startAnimation(anim);
			}
			headerView.setVisibility(View.VISIBLE);
		}
		return changeVis;
	}

	@Override
	public boolean hideHeaderView(Mode mode) {
		// 4.0以上的动画无法启动
		if (Build.VERSION.SDK_INT >= super.getMinimumApiLevel()) {
			return super.hideHeaderView(mode);
		}
		final View headerView = getHeaderView();
		final boolean changeVis = headerView != null
				&& headerView.getVisibility() != View.GONE;
		if (changeVis) {
			// Hide Header
			Animation anim = getAnimation(false, mode);
			if (anim != null) {
				// AnimationListener will call HeaderTransformer and
				// HeaderViewListener
				headerView.startAnimation(anim);
				anim.startNow();
			} else {
				// As we're not animating, hide the header + call the header
				// transformer now
				headerView.setVisibility(View.GONE);
				onReset();
			}
		}
		return changeVis;
	}

	@Override
	protected int getMinimumApiLevel() {
		return Build.VERSION_CODES.ECLAIR_MR1;
	}

	@Override
	public void onRefreshMinimized() {
		// Here we fade out most of the header, leaving just the progress bar
		View contentLayout = getHeaderView().findViewById(R.id.ptr_content);
		if (contentLayout != null) {
			contentLayout.startAnimation(AnimationUtils.loadAnimation(
					contentLayout.getContext(), R.anim.fade_out));
			contentLayout.setVisibility(View.INVISIBLE);
		}
	}

	public void setFromEndLabel(CharSequence pullLabel,
			CharSequence releaseLabel) {
		mPullFromEndLabel = pullLabel;
		mReleaseFromEndLabel = releaseLabel;
	}

	protected Animation getAnimation(boolean in, Mode mode) {
		if (in) {
			if (mode == Mode.PULL_FROM_END) {
				return mEndHeaderInAnimation;
			}
			return mHeaderInAnimation;
		}
		if (mode == Mode.PULL_FROM_END) {
			return mEndHeaderOutAnimation;
		}
		return mHeaderOutAnimation;
	}

	class AnimationCallback implements Animation.AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animation == mHeaderOutAnimation
					|| animation == mEndHeaderOutAnimation) {
				View headerView = getHeaderView();
				if (headerView != null) {
					headerView.setVisibility(View.GONE);
				}
				onReset();
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}

}
