package com.questionbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class EarphoneButtonReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
			if (intent.getExtras().getInt("state") == 1)
				Toast.makeText(context, "earphones connected", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(context, "earphones disconnected", Toast.LENGTH_LONG).show();
		}
		abortBroadcast();
	}
}
