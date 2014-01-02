/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final String PACKAGE_NAME = "com.google.zxing.client.android";
	private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
	private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
	private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };
	
	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	// update
	private String characterSet;
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private CameraManager cameraManager;
	private ViewfinderView viewfinderView;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;
	private TextView statusView;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType,?> decodeHints;
	private Result savedResultToShow;
	
	public ViewfinderView getViewfinderView() 
	{
		return viewfinderView;
	}

	public Handler getHandler() 
	{
		return handler;
	}

	public CameraManager getCameraManager() 
	{
		return cameraManager;
	}
	
	private void resetStatusView() 
	{
	    statusView.setText(R.string.msg_default_status);
	    statusView.setVisibility(View.VISIBLE);
	    viewfinderView.setVisibility(View.VISIBLE);
	}
	
	private static boolean isZXingURL(String dataString) 
	{
		if (dataString == null) {
			return false;
		}
		for (String url : ZXING_URLS) {
			if (dataString.startsWith(url)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onCreate(Bundle icicle) 
	{
		super.onCreate(icicle);
		Window window = getWindow();
	    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
		setContentView(R.layout.camera);
		
		hasSurface = false;
	    inactivityTimer = new InactivityTimer(this);
	    beepManager = new BeepManager(this);
	    ambientLightManager = new AmbientLightManager(this);

	    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	    showHelpOnFirstLaunch();
	    
	}

	@Override
	protected void onResume() 
	{
		super.onResume();

		//
		cameraManager = new CameraManager(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		statusView = (TextView) findViewById(R.id.status_view);
		
		handler = null;
	    resetStatusView();
		
	    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
	    SurfaceHolder surfaceHolder = surfaceView.getHolder();
	    if (hasSurface) 
	    {
	      // The activity was paused but not stopped, so the surface still exists. Therefore
	      // surfaceCreated() won't be called, so init the camera here.
	      initCamera(surfaceHolder);
	    } 
	    else 
	    {
	      // Install the callback and wait for surfaceCreated() to init the camera.
	      surfaceHolder.addCallback(this);
	      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }
	    
	    beepManager.updatePrefs();
	    ambientLightManager.start(cameraManager);

	    inactivityTimer.onResume();
	    
	    Intent intent = getIntent();

	    decodeFormats = null;
	    characterSet = null;
	    
	    if (intent != null) {

	        String action = intent.getAction();
	        String dataString = intent.getDataString();

	        if (Intents.Scan.ACTION.equals(action)) {

	          decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
	          decodeHints = DecodeHintManager.parseDecodeHints(intent);

	          if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
	            int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
	            int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
	            if (width > 0 && height > 0) {
	              cameraManager.setManualFramingRect(width, height);
	            }
	          }
	          
	          String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
	          if (customPromptMessage != null) {
	            statusView.setText(customPromptMessage);
	          }

	        } else if (dataString != null &&
	                   dataString.contains(PRODUCT_SEARCH_URL_PREFIX) &&
	                   dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {

	          decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

	        } 
	        else if (isZXingURL(dataString)) 
	        {

	        	Log.w(TAG, "Data is zxing url, it shouldn't be here");
//	        	// Scan formats requested in query string (all formats if none specified).
//	        	// If a return URL is specified, send the results there. Otherwise, handle it ourselves.
//	        	source = IntentSource.ZXING_LINK;
//	        	sourceUrl = dataString;
//	        	Uri inputUri = Uri.parse(dataString);
//	        	scanFromWebPageManager = new ScanFromWebPageManager(inputUri);
//	        	decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
//	        	// Allow a sub-set of the hints to be specified by the caller.
//	        	decodeHints = DecodeHintManager.parseDecodeHints(inputUri);

	        }

	        characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

	      }
	}

	@Override
	protected void onPause() 
	{
		if (handler != null) 
		{
		      handler.quitSynchronously();
		      handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() 
	{
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		if (resultCode == RESULT_OK) 
		{
			if (requestCode == HISTORY_REQUEST_CODE) 
			{
				int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1);
				if (itemNumber >= 0) 
				{
					Log.i(TAG, "onActivityResult");
//					HistoryItem historyItem = historyManager.buildHistoryItem(itemNumber);
//					decodeOrStoreSavedBitmap(null, historyItem.getResult());
				}
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		if (holder == null) 
		{
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) 
		{
			hasSurface = true;
			initCamera(holder);
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{

	}
	
	
	private void initCamera(SurfaceHolder surfaceHolder) 
	{
		if (surfaceHolder == null) 
		{
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) 
		{
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try 
		{
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} 
		catch (IOException ioe) 
		{
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} 
		catch (RuntimeException e) 
		{
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) 
	{
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} 
		else 
		{
			if (result != null) 
			{
				savedResultToShow = result;
			}
			if (savedResultToShow != null) 
			{
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}
	  
	private void displayFrameworkBugMessageAndExit() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("BarCode Scanner");
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}
	
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) 
	{
		inactivityTimer.onActivity();
	    beepManager.playBeepSoundAndVibrate();
		String resultString = rawResult.getText();
		//FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		}else {
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		CaptureActivity.this.finish();
	}
	
	public void drawViewfinder() 
	{
		viewfinderView.drawViewfinder();
	}
	
	/**
	 * We want the help screen to be shown automatically the first time a new version of the app is
	 * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
	 * it to a value stored as a preference.
	 */
	private boolean showHelpOnFirstLaunch() 
	{
		try {
			PackageInfo info = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			int currentVersion = info.versionCode;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0);
			if (currentVersion > lastVersion) {
				prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
				Intent intent = new Intent(this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				// Show the default page on a clean install, and the what's new page on an upgrade.
				String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
				intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
				startActivity(intent);
				return true;
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, e);
		}
		return false;
	}

}
