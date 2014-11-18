
package org.kaaproject.kaa.demo.qrcode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Finishes an activity after a period of inactivity if the device is on battery power.
 */
final class InactivityTimer {

  private static final String TAG = InactivityTimer.class.getSimpleName();

  private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

  private final Activity activity;
  private final BroadcastReceiver powerStatusReceiver;
  private boolean registered;
  private AsyncTask<Object,Object,Object> inactivityTask;

  InactivityTimer(Activity activity) {
    this.activity = activity;
    powerStatusReceiver = new PowerStatusReceiver();
    registered = false;
    onActivity();
  }

  synchronized void onActivity() {
    cancel();
    inactivityTask = new InactivityAsyncTask();
    inactivityTask.execute();
  }

  public synchronized void onPause() {
    cancel();
    if (registered) {
      activity.unregisterReceiver(powerStatusReceiver);
      registered = false;
    } else {
      Log.w(TAG, "PowerStatusReceiver was never registered?");
    }
  }

  public synchronized void onResume() {
    if (registered) {
      Log.w(TAG, "PowerStatusReceiver was already registered?");
    } else {
      activity.registerReceiver(powerStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
      registered = true;
    }
    onActivity();
  }

  private synchronized void cancel() {
    AsyncTask<?,?,?> task = inactivityTask;
    if (task != null) {
      task.cancel(true);
      inactivityTask = null;
    }
  }

  void shutdown() {
    cancel();
  }

  private final class PowerStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
      if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
        // 0 indicates that we're on battery
        boolean onBatteryNow = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) <= 0;
        if (onBatteryNow) {
          InactivityTimer.this.onActivity();
        } else {
          InactivityTimer.this.cancel();
        }
      }
    }
  }

  private final class InactivityAsyncTask extends AsyncTask<Object,Object,Object> {
    @Override
    protected Object doInBackground(Object... objects) {
      try {
        Thread.sleep(INACTIVITY_DELAY_MS);
        Log.i(TAG, "Finishing activity due to inactivity");
        activity.finish();
      } catch (InterruptedException e) {
        // continue without killing
      }
      return null;
    }
  }

}
