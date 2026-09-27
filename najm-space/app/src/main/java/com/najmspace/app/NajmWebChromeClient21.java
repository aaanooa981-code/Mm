package com.najmspace.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class NajmWebChromeClient21 extends WebChromeClient {
    private final Activity activity;
    private final ProgressBar progress;

    public NajmWebChromeClient21(Activity activity, ProgressBar progress){
        this.activity=activity;
        this.progress=progress;
    }

    @Override public void onProgressChanged(WebView view,int newProgress){
        if(progress!=null){
            progress.setProgress(newProgress);
            progress.setVisibility(newProgress>=100?View.GONE:View.VISIBLE);
        }
    }

    @Override public void onPermissionRequest(final PermissionRequest request){
        boolean mic=true,camera=true;
        if(Build.VERSION.SDK_INT>=23){
            mic=activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
            camera=activity.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
        }
        if(mic && camera)request.grant(request.getResources());
        else request.deny();
    }
}
