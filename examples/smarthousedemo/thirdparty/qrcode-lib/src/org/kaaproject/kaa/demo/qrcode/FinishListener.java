package org.kaaproject.kaa.demo.qrcode;

import android.app.Activity;
import android.content.DialogInterface;

public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

  private final Activity activityToFinish;

  public FinishListener(Activity activityToFinish) {
    this.activityToFinish = activityToFinish;
  }

  @Override
  public void onCancel(DialogInterface dialogInterface) {
    run();
  }

  @Override
  public void onClick(DialogInterface dialogInterface, int i) {
    run();
  }

  private void run() {
    activityToFinish.finish();
  }

}
