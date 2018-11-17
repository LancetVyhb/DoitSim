package com.DoIt;

import android.app.Activity;
import android.app.ProgressDialog;

public class Progress {
    private ProgressDialog progressDialog;
    private Thread thread;

    public Progress(Activity activity){
        progressDialog = new ProgressDialog(activity);
    }

    public Progress setThread(Runnable runnable){
        thread = new Thread(runnable);
        return this;
    }

    public void startProgress(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
        thread.run();
    }

    public void finishProgress(){
        progressDialog.cancel();
    }
}
