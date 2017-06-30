package com.questionbox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

import java.lang.reflect.Method;

public class EarphoneControl extends Activity {

	// Using LinearLayout instead of R.layout.main (main.xml)
	BroadcastReceiver broadcastsHandler;


	TelephonyManager manager;
	StatePhoneReceiver myPhoneStateListener;
	boolean callFromApp = false; //Call
	boolean callFromOffHook = false; //Hang on



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);

		EarphoneButtonReceiver r = new EarphoneButtonReceiver();
		filter.setPriority(10000);
		registerReceiver(r, filter);


	}

	//Boot up receiver
	public class BootUpReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			//start boot - start this app activity
			Intent target = new Intent(context, EarphoneControl.class);
			target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(target);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		//broad cast handler for Earphone button
		broadcastsHandler = new EarphoneButtonReceiver();

		//register Receiver
		registerReceiver(broadcastsHandler, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

		//register Media button
		registerMediaButton(EarphoneControl.this);
	}

	public static void registerMediaButton(Context context) {

		//audio manager & register media button event receiver
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		ComponentName receiver = new ComponentName(context.getPackageName(), EarphoneButtonReceiver.class.getName());
		audioManager.registerMediaButtonEventReceiver(receiver);
	}

	public static void unregisterMediaButton(Context context) {

		//audio manager & unregister media button event receiver
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		ComponentName receiver = new ComponentName(context.getPackageName(), EarphoneButtonReceiver.class.getName());
		audioManager.unregisterMediaButtonEventReceiver(receiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(broadcastsHandler);
		unregisterMediaButton(EarphoneControl.this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//when destroy app unregist receiver
		unregisterReceiver(broadcastsHandler);
		unregisterMediaButton(EarphoneControl.this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		Toast.makeText(this, "Keycode:" + keyCode + " keyEvent:" + event, Toast.LENGTH_SHORT).show();
		//volume down button
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			Toast.makeText(this, "Volume Down Pressed", Toast.LENGTH_SHORT).show();
			perfomeCallVolDown();
			return true;
		}
		//volume up button
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			Toast.makeText(this, "Volume Up Pressed", Toast.LENGTH_SHORT).show();
			perfomeCalVolUp();
			return true;
		}
		//play or stop button
		if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
			Toast.makeText(this, "play/stop button pressed", Toast.LENGTH_SHORT).show();

			//flag - yes
			if (callYesOrNo) {
				//call play
				perfomeCallPlay();
			//flag - no
			} else {
				//call end call
				performEndCall();
			}

			//switch action - when false, be changed true, when true, be changed false
			callYesOrNo = !callYesOrNo;

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	//call or stop flag - when calling next time to switch stop :: role in switching
	boolean callYesOrNo = true;

	//end call
	public void performEndCall() {
		//telephony manager
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			//get class
			Class c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			Object telephonyService = m.invoke(tm);

			c = Class.forName(telephonyService.getClass().getName());
			//end call
			m = c.getDeclaredMethod("endCall");
			m.setAccessible(true);
			m.invoke(telephonyService);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//call play - play button action
	public void perfomeCallPlay() {

		myPhoneStateListener = new StatePhoneReceiver(this);
		manager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
		manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		callFromApp=true;
	}

	//volume up
	public void perfomeCalVolUp() {

		myPhoneStateListener = new StatePhoneReceiver(this);
		manager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
		manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		callFromApp=true;

		Intent i = new Intent(android.content.Intent.ACTION_CALL, Uri.parse("tel:0478-700-954"));
		startActivity(i);
	}

	//volume down
	public void perfomeCallVolDown() {

		myPhoneStateListener = new StatePhoneReceiver(this);
		manager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
		manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		callFromApp=true;

		Intent i = new Intent(android.content.Intent.ACTION_CALL, Uri.parse("tel:0478-700-954"));
		startActivity(i);
	}


	//get phone state
	public class StatePhoneReceiver extends PhoneStateListener {
		Context context;
		public StatePhoneReceiver(Context context) {
			this.context = context;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			//phone state
			switch (state) {
				//play button
				case TelephonyManager.CALL_STATE_OFFHOOK:

					if (callFromApp) {
						callFromApp = false; //switch
						callFromOffHook = true;

						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
						AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
						audioManager.setMode(AudioManager.MODE_IN_CALL); //calling
						audioManager.setSpeakerphoneOn(true);
					}
					break;

				//stop button
				case TelephonyManager.CALL_STATE_IDLE:

					if (callFromOffHook) {
						callFromOffHook = false; //switch

						AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
						audioManager.setMode(AudioManager.MODE_NORMAL);
						manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
					}
					break;
			}
		}
	}
}