/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.senab.actionbarpulltorefresh.library;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.WeakHashMap;

import uk.co.senab.actionbarpulltorefresh.library.listeners.HeaderViewListener;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener2;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

public class PullToRefreshAttacher {

	private static final boolean DEBUG = false;
	private static final String LOG_TAG = "PullToRefreshAttacher";

	/* Member Variables */

	private EnvironmentDelegate mEnvironmentDelegate;
	private HeaderTransformer mHeaderTransformer;

	private OnRefreshListener mOnRefreshListener;
	private OnRefreshListener2 mOnRefreshListener2;

	private Activity mActivity;
	private View mHeaderView;
	private HeaderViewListener mHeaderViewListener;

	private final int mTouchSlop;
	private final float mRefreshScrollDistance;

	private float mInitialMotionY, mLastMotionY, mPullBeginY;
	private float mInitialMotionX;
	private boolean mIsBeingDragged, mIsRefreshing,
			mHandlingTouchEventFromDown;
	private View mViewBeingDragged;

	private final WeakHashMap<View, ViewDelegate> mRefreshableViews;

	private final boolean mRefreshOnUp;
	private final int mRefreshMinimizeDelay;
	private final boolean mRefreshMinimize;
	private boolean mIsDestroyed = false;

	private final int[] mViewLocationResult = new int[2];
	private final Rect mRect = new Rect();
	private Mode mMode;
	private Mode currentMode;

	protected PullToRefreshAttacher(Activity activity, Options options) {
		if (activity == null) {
			throw new IllegalArgumentException("activity cannot be null");
		}
		if (options == null) {
			Log.i(LOG_TAG, "Given null options so using default options.");
			options = new Options();
		}

		mActivity = activity;
		mRefreshableViews = new WeakHashMap<View, ViewDelegate>();

		// Copy necessary values from options
		mRefreshScrollDistance = options.refreshScrollDistance;
		mRefreshOnUp = options.refreshOnUp;
		mRefreshMinimizeDelay = options.refreshMinimizeDelay;
		mRefreshMinimize = options.refreshMinimize;

		// EnvironmentDelegate
		mEnvironmentDelegate = options.environmentDelegate != null ? options.environmentDelegate
				: createDefaultEnvironmentDelegate();

		// Header Transformer
		mHeaderTransformer = options.headerTransformer != null ? options.headerTransformer
				: createDefaultHeaderTransformer();

		// mode
		mMode = options.mode != null ? options.mode : Mode.PULL_FROM_START;

		// Get touch slop for use later
		mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();

		// Get Window Decor View
		final ViewGroup decorView = (ViewGroup) activity.getWindow()
				.getDecorView();

		// Create Header view and then add to Decor View
		mHeaderView = LayoutInflater.from(
				mEnvironmentDelegate.getContextForInflater(activity)).inflate(
				options.headerLayout, decorView, false);
		if (mHeaderView == null) {
			throw new IllegalArgumentException(
					"Must supply valid layout id for header.");
		}
		// Make Header View invisible so it still gets a layout pass
		mHeaderView.setVisibility(View.INVISIBLE);

		// Notify transformer
		mHeaderTransformer.onViewCreated(activity, mHeaderView);

		// Now HeaderView to Activity
		decorView.post(new Runnable() {
			@Override
			public void run() {
				if (decorView.getWindowToken() != null) {
					// The Decor View has a Window Token, so we can add the
					// HeaderView!
					addHeaderViewToActivity(mHeaderView);
				} else {
					// The Decor View doesn't have a Window Token yet, post
					// ourselves again...
					decorView.post(this);
				}
			}
		});
	}

	/**
	 * Add a view which will be used to initiate refresh requests.
	 * 
	 * @param view
	 *            View which will be used to initiate refresh requests.
	 */
	void addRefreshableView(View view, ViewDelegate viewDelegate) {
		if (isDestroyed())
			return;

		// Check to see if view is null
		if (view == null) {
			Log.i(LOG_TAG, "Refreshable View is null.");
			return;
		}

		// ViewDelegate
		if (viewDelegate == null) {
			viewDelegate = InstanceCreationUtils.getBuiltInViewDelegate(view);
		}

		// View to detect refreshes for
		mRefreshableViews.put(view, viewDelegate);
	}

	void useViewDelegate(Class<?> viewClass, ViewDelegate delegate) {
		for (View view : mRefreshableViews.keySet()) {
			if (viewClass.isInstance(view)) {
				mRefreshableViews.put(view, delegate);
			}
		}
	}

	/**
	 * Clear all views which were previously used to initiate refresh requests.
	 */
	void clearRefreshableViews() {
		mRefreshableViews.clear();
	}

	/**
	 * This method should be called by your Activity's or Fragment's
	 * onConfigurationChanged method.
	 * 
	 * @param newConfig
	 *            The new configuration
	 */
	public void onConfigurationChanged(Configuration newConfig) {
		mHeaderTransformer.onConfigurationChanged(mActivity, newConfig);
	}

	/**
	 * Manually set this Attacher's refreshing state. The header will be
	 * displayed or hidden as requested.
	 * 
	 * @param refreshing
	 *            - Whether the attacher should be in a refreshing state,
	 */
	final void setRefreshing(boolean refreshing) {
		setRefreshingInt(null, refreshing, false);
	}

	/**
	 * @return true if this Attacher is currently in a refreshing state.
	 */
	final boolean isRefreshing() {
		return mIsRefreshing;
	}

	/**
	 * Call this when your refresh is complete and this view should reset itself
	 * (header view will be hidden).
	 * 
	 * This is the equivalent of calling <code>setRefreshing(false)</code>.
	 */
	final void setRefreshComplete() {
		setRefreshingInt(null, false, false);
	}

	/**
	 * Set the Listener to be called when a refresh is initiated.
	 */
	void setOnRefreshListener(OnRefreshListener listener) {
		mOnRefreshListener = listener;
		mOnRefreshListener2 = null;
	}
	
	void setOnRefreshListener(OnRefreshListener2 listener){
		mOnRefreshListener = null;
		mOnRefreshListener2 = listener;
	}

	void destroy() {
		if (mIsDestroyed)
			return; // We've already been destroyed

		// Remove the Header View from the Activity
		removeHeaderViewFromActivity(mHeaderView);

		// Lets clear out all of our internal state
		clearRefreshableViews();

		mActivity = null;
		mHeaderView = null;
		mHeaderViewListener = null;
		mEnvironmentDelegate = null;
		mHeaderTransformer = null;

		mIsDestroyed = true;
	}

	/**
	 * Set a {@link HeaderViewListener} which is called when the visibility
	 * state of the Header View has changed.
	 */
	final void setHeaderViewListener(HeaderViewListener listener) {
		mHeaderViewListener = listener;
	}

	/**
	 * @return The Header View which is displayed when the user is pulling, or
	 *         we are refreshing.
	 */
	final View getHeaderView() {
		return mHeaderView;
	}

	/**
	 * @return The HeaderTransformer currently used by this Attacher.
	 */
	HeaderTransformer getHeaderTransformer() {
		return mHeaderTransformer;
	}

	final boolean onInterceptTouchEvent(MotionEvent event) {
		if (DEBUG) {
			Log.d(LOG_TAG, "onInterceptTouchEvent: " + event.toString());
		}

		// If we're not enabled or currently refreshing don't handle any touch
		// events
		if (isRefreshing() || !mMode.permitsPullToRefresh()) {
			return false;
		}

		final float x = event.getX(), y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE: {
			// We're not currently being dragged so check to see if the user has
			// scrolled enough
			if (mIsBeingDragged || mInitialMotionY <= 0f) {
				break;
			}
			final float yDiff = y - mInitialMotionY;
			final float xDiff = x - mInitialMotionX;

			if (yDiff > mTouchSlop) {
				if (yDiff > xDiff && currentMode == Mode.PULL_FROM_START
						|| currentMode == Mode.BOTH) {
					currentMode = Mode.PULL_FROM_START;
					// 下拉时，在此处设置初始值为0
					mLastMotionY = 0;
					mIsBeingDragged = true;
					onPullStarted(y);
				} else {
					resetTouch();
				}
			} else if (-yDiff > mTouchSlop) {
				if (yDiff < xDiff && currentMode == Mode.PULL_FROM_END
						|| currentMode == Mode.BOTH) {
					currentMode = Mode.PULL_FROM_END;
					mIsBeingDragged = true;
					onPullStarted(y);
				} else {
					resetTouch();
				}
			}
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			// If we're already refreshing, ignore
			if (canRefresh(true)) {
				for (View view : mRefreshableViews.keySet()) {
					if (isViewBeingDragged(view, event)) {
						mInitialMotionX = x;
						mInitialMotionY = y;
						mViewBeingDragged = view;
					}
				}
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			resetTouch();
			break;
		}
		}

		if (DEBUG)
			Log.d(LOG_TAG, "onInterceptTouchEvent. Returning "
					+ mIsBeingDragged);

		return mIsBeingDragged;
	}

	final boolean isViewBeingDragged(View view, MotionEvent event) {
		if (view.isShown() && mRefreshableViews.containsKey(view)) {
			// First we need to set the rect to the view's screen co-ordinates
			view.getLocationOnScreen(mViewLocationResult);
			final int viewLeft = mViewLocationResult[0], viewTop = mViewLocationResult[1];
			mRect.set(viewLeft, viewTop, viewLeft + view.getWidth(), viewTop
					+ view.getHeight());

			if (DEBUG)
				Log.d(LOG_TAG,
						"isViewBeingDragged. View Rect: " + mRect.toString());

			final int rawX = (int) event.getRawX(), rawY = (int) event
					.getRawY();
			if (mRect.contains(rawX, rawY)) {
				// The Touch Event is within the View's display Rect
				ViewDelegate delegate = mRefreshableViews.get(view);
				if (delegate == null) {
					return false;
				}
				boolean isDragged = false;
				// Now call the delegate, converting the X/Y into the View's
				// co-ordinate system
				if (mMode.pullFromStart()
						&& delegate.isReadyForPull(view, rawX - mRect.left,
								rawY - mRect.top)) {
					currentMode = Mode.PULL_FROM_START;
					isDragged = true;
				}
				if (mMode.pullFromEnd()
						&& delegate.isReadyForPullFromEnd(view, rawX, rawY)) {
					if (isDragged) {
						// 即可上拉，也可下拉，视mMode而定。
						// 作为临时保存的值，在onInterceptTouchEvent的move事件中，
						// 会确定为上拉或下拉的其中一种，无特别意义
						currentMode = mMode;
					} else {
						isDragged = true;
						currentMode = Mode.PULL_FROM_END;
					}
					// 上拉时，要在此处设置初始值为view高度
					mLastMotionY = view.getHeight();
				}
				return isDragged;
			}
		}
		return false;
	}

	final boolean onTouchEvent(MotionEvent event) {
		if (DEBUG) {
			Log.d(LOG_TAG, "onTouchEvent: " + event.toString());
		}

		// Record whether our handling is started from ACTION_DOWN
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mHandlingTouchEventFromDown = true;
		}

		// If we're being called from ACTION_DOWN then we must call through to
		// onInterceptTouchEvent until it sets mIsBeingDragged
		if (mHandlingTouchEventFromDown && !mIsBeingDragged) {
			onInterceptTouchEvent(event);
			return true;
		}

		if (mViewBeingDragged == null) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE: {
			// If we're already refreshing ignore it
			if (isRefreshing()) {
				return false;
			}

			final float y = event.getY();

			if (mIsBeingDragged && y != mLastMotionY) {
				final float yDx = y - mLastMotionY;

				/**
				 * Check to see if the user is scrolling the right direction
				 * (down). We allow a small scroll up which is the check against
				 * negative touch slop.
				 */
				if (currentMode == Mode.PULL_FROM_START) {
					if (yDx >= -mTouchSlop) {
						onPull(mViewBeingDragged, y);
						// Only record the y motion if the user has scrolled
						// down.
						if (yDx > 0f) {
							mLastMotionY = y;
						}
					} else {
						onPullEnded();
						resetTouch();
					}
				} else if (currentMode == Mode.PULL_FROM_END) {
					if (yDx <= mTouchSlop) {
						onPull(mViewBeingDragged, y);
						// Only record the y motion if the user has scrolled
						// up.
						if (yDx < 0f) {
							mLastMotionY = y;
						}
					} else {
						onPullEnded();
						resetTouch();
					}
				}
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			checkScrollForRefresh(mViewBeingDragged);
			if (mIsBeingDragged) {
				onPullEnded();
			}
			resetTouch();
			break;
		}
		}

		return true;
	}

	void minimizeHeader() {
		if (isDestroyed())
			return;

		mHeaderTransformer.onRefreshMinimized();

		if (mHeaderViewListener != null) {
			mHeaderViewListener.onStateChanged(mHeaderView,
					HeaderViewListener.STATE_MINIMIZED);
		}
	}

	void resetTouch() {
		mIsBeingDragged = false;
		mHandlingTouchEventFromDown = false;
		mInitialMotionY = mLastMotionY = mPullBeginY = -1f;
	}

	void onPullStarted(float y) {
		if (DEBUG) {
			Log.d(LOG_TAG, "onPullStarted");
		}
		showHeaderView();
		mPullBeginY = y;
	}

	void onPull(View view, float y) {
		if (DEBUG) {
			Log.d(LOG_TAG, "onPull");
		}

		final float pxScrollForRefresh = getScrollNeededForRefresh(view);
		final float scrollLength = Math.abs(y - mPullBeginY);

		if (scrollLength < pxScrollForRefresh) {
			mHeaderTransformer.onPulled(scrollLength / pxScrollForRefresh);
		} else {
			if (mRefreshOnUp) {
				mHeaderTransformer.onReleaseToRefresh();
			} else {
				setRefreshingInt(view, true, true);
			}
		}
	}

	void onPullEnded() {
		if (DEBUG) {
			Log.d(LOG_TAG, "onPullEnded");
		}
		if (!mIsRefreshing) {
			reset(true);
		}
	}

	void showHeaderView() {
		updateHeaderViewPosition(mHeaderView);
		if (mHeaderTransformer.showHeaderView(currentMode == Mode.PULL_FROM_END)) {
			if (mHeaderViewListener != null) {
				mHeaderViewListener.onStateChanged(mHeaderView,
						HeaderViewListener.STATE_VISIBLE);
			}
		}
	}

	void hideHeaderView() {
		if (mHeaderTransformer.hideHeaderView(currentMode == Mode.PULL_FROM_END)) {
			if (mHeaderViewListener != null) {
				mHeaderViewListener.onStateChanged(mHeaderView,
						HeaderViewListener.STATE_HIDDEN);
			}
		}
	}

	protected final Activity getAttachedActivity() {
		return mActivity;
	}

	protected EnvironmentDelegate createDefaultEnvironmentDelegate() {
		return new EnvironmentDelegate() {
			@SuppressLint("NewApi")
			@Override
			public Context getContextForInflater(Activity activity) {
				Context context = null;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					ActionBar ab = activity.getActionBar();
					if (ab != null) {
						context = ab.getThemedContext();
					}
				}
				if (context == null) {
					context = activity;
				}
				return context;
			}
		};
	}

	protected HeaderTransformer createDefaultHeaderTransformer() {
		return new DefaultHeaderTransformer();
	}

	private boolean checkScrollForRefresh(View view) {
		if (mIsBeingDragged && mRefreshOnUp && view != null) {
			if (Math.abs(mLastMotionY - mPullBeginY) >= getScrollNeededForRefresh(view)) {
				setRefreshingInt(view, true, true);
				return true;
			}
		}
		return false;
	}

	private void setRefreshingInt(View view, boolean refreshing,
			boolean fromTouch) {
		if (isDestroyed())
			return;

		if (DEBUG)
			Log.d(LOG_TAG, "setRefreshingInt: " + refreshing);
		// Check to see if we need to do anything
		if (mIsRefreshing == refreshing) {
			return;
		}

		resetTouch();

		if (refreshing && canRefresh(fromTouch)) {
			startRefresh(view, fromTouch);
		} else {
			reset(fromTouch);
		}
	}

	/**
	 * @param fromTouch
	 *            Whether this is being invoked from a touch event
	 * @return true if we're currently in a state where a refresh can be
	 *         started.
	 */
	private boolean canRefresh(boolean fromTouch) {
		return !mIsRefreshing && (!fromTouch || mOnRefreshListener != null || mOnRefreshListener2 != null);
	}

	private float getScrollNeededForRefresh(View view) {
		return view.getHeight() * mRefreshScrollDistance;
	}

	private void reset(boolean fromTouch) {
		// Update isRefreshing state
		mIsRefreshing = false;

		// Remove any minimize callbacks
		if (mRefreshMinimize) {
			getHeaderView().removeCallbacks(mRefreshMinimizeRunnable);
		}

		// Hide Header View
		hideHeaderView();
	}

	private void startRefresh(View view, boolean fromTouch) {
		// Update isRefreshing state
		mIsRefreshing = true;

		// Call OnRefreshListener if this call has originated from a touch event
		if (fromTouch) {
			if (mOnRefreshListener != null) {
				mOnRefreshListener.onRefreshStarted(view);
			}
			if(mOnRefreshListener2 != null){
				if(currentMode == Mode.PULL_FROM_END){
					mOnRefreshListener2.onRefreshStartedFromEnd(view);
				} else {
					mOnRefreshListener2.onRefreshStartedFromStart(view);
				}
			}
		}

		// Call Transformer
		mHeaderTransformer.onRefreshStarted();

		// Show Header View
		showHeaderView();

		// Post a runnable to minimize the refresh header
		if (mRefreshMinimize) {
			if (mRefreshMinimizeDelay > 0) {
				getHeaderView().postDelayed(mRefreshMinimizeRunnable,
						mRefreshMinimizeDelay);
			} else {
				getHeaderView().post(mRefreshMinimizeRunnable);
			}
		}
	}

	private boolean isDestroyed() {
		if (mIsDestroyed) {
			Log.i(LOG_TAG, "PullToRefreshAttacher is destroyed.");
		}
		return mIsDestroyed;
	}

	protected void addHeaderViewToActivity(View headerView) {
		// Get the Display Rect of the Decor View
		mActivity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(mRect);

		// Honour the requested layout params
		@SuppressWarnings("deprecation")
		int width = WindowManager.LayoutParams.FILL_PARENT;
		int height = WindowManager.LayoutParams.WRAP_CONTENT;
		ViewGroup.LayoutParams requestedLp = headerView.getLayoutParams();
		if (requestedLp != null) {
			width = requestedLp.width;
			height = requestedLp.height;
		}

		// Create LayoutParams for adding the View as a panel
//		WindowManager.LayoutParams wlp = new WindowManager.LayoutParams(width,
//				height, WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
//				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//				PixelFormat.TRANSLUCENT);
		WindowManager.LayoutParams wlp = new WindowManager.LayoutParams(width,
				height, WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		wlp.x = 0;
		wlp.y = mRect.top;
		wlp.gravity = Gravity.TOP;

		// Workaround for Issue #182
		headerView.setTag(wlp);
		mActivity.getWindowManager().addView(headerView, wlp);
	}

	protected void updateHeaderViewPosition(View headerView) {
		// Refresh the Display Rect of the Decor View
		mActivity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(mRect);

		WindowManager.LayoutParams wlp = null;
		if (headerView.getLayoutParams() instanceof WindowManager.LayoutParams) {
			wlp = (WindowManager.LayoutParams) headerView.getLayoutParams();
		} else if (headerView.getTag() instanceof WindowManager.LayoutParams) {
			wlp = (WindowManager.LayoutParams) headerView.getTag();
		}

		if (wlp != null && wlp.y != mRect.top) {
			wlp.y = mRect.top;
			mActivity.getWindowManager().updateViewLayout(headerView, wlp);
		}
	}

	protected void removeHeaderViewFromActivity(View headerView) {
		if (headerView.getWindowToken() != null) {
			mActivity.getWindowManager().removeViewImmediate(headerView);
		}
	}

	private final Runnable mRefreshMinimizeRunnable = new Runnable() {
		@Override
		public void run() {
			minimizeHeader();
		}
	};

	public static enum Mode {

		/**
		 * Disable all Pull-to-Refresh gesture and Refreshing handling
		 */
		DISABLED(0x0),

		/**
		 * Only allow the user to Pull from the start of the Refreshable View to
		 * refresh. The start is either the Top or Left, depending on the
		 * scrolling direction.
		 */
		PULL_FROM_START(0x1),

		/**
		 * Only allow the user to Pull from the end of the Refreshable View to
		 * refresh. The start is either the Bottom or Right, depending on the
		 * scrolling direction.
		 */
		PULL_FROM_END(0x2),

		/**
		 * Allow the user to both Pull from the start, from the end to refresh.
		 */
		BOTH(0x3),

		/**
		 * Disables Pull-to-Refresh gesture handling, but allows manually
		 * setting the Refresh state via
		 * {@link PullToRefreshBase#setRefreshing() setRefreshing()}.
		 */
		MANUAL_REFRESH_ONLY(0x4);

		/**
		 * Maps an int to a specific mode. This is needed when saving state, or
		 * inflating the view from XML where the mode is given through a attr
		 * int.
		 * 
		 * @param modeInt
		 *            - int to map a Mode to
		 * @return Mode that modeInt maps to, or PULL_FROM_START by default.
		 */
		static Mode mapIntToValue(final int modeInt) {
			for (Mode value : Mode.values()) {
				if (modeInt == value.getIntValue()) {
					return value;
				}
			}

			// If not, return default
			return getDefault();
		}

		static Mode getDefault() {
			return PULL_FROM_START;
		}

		private int mIntValue;

		// The modeInt values need to match those from attrs.xml
		Mode(int modeInt) {
			mIntValue = modeInt;
		}

		/**
		 * @return true if the mode permits Pull-to-Refresh
		 */
		boolean permitsPullToRefresh() {
			return !(this == DISABLED || this == MANUAL_REFRESH_ONLY);
		}

		/**
		 * @return true if this mode wants the Loading Layout Header to be shown
		 */
		public boolean pullFromStart() {
			return this == PULL_FROM_START || this == BOTH;
		}

		/**
		 * @return true if this mode wants the Loading Layout Footer to be shown
		 */
		public boolean pullFromEnd() {
			return this == PULL_FROM_END || this == BOTH;
		}

		int getIntValue() {
			return mIntValue;
		}

	}
}
