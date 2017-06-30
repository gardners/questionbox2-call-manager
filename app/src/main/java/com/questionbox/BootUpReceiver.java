package com.questionbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent target = new Intent(context, EarphoneControl.class);
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(target);
    }
}
