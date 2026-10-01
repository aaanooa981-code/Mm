package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class NajmLegacyYouTube {
    private static final String PACKAGE_NAME = "free.rm.skytube.oss";
    private static final String ASSET_PATH = "preload/SkyTube-Oss-2.999.apk";
    private static final String FILE_NAME = "SkyTube-Oss-2.999.apk";

    private NajmLegacyYouTube() {}

    public static void open(final Activity activity) {
        if (activity == null) return;

        if (Build.VERSION.SDK_INT < 19) {
            new AlertDialog.Builder(activity)
                    .setTitle("YouTube")
                    .setMessage("هذا الوضع يحتاج Android 4.4 أو أحدث.")
                    .setPositiveButton("موافق", null)
                    .show();
            return;
        }

        if (NajmContainer.isInstalled(PACKAGE_NAME)) {
            NajmContainer.launch(activity, PACKAGE_NAME);
            return;
        }

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    File dir = new File(activity.getCacheDir(), "najm_container");
                    if (!dir.exists()) dir.mkdirs();
                    File apk = new File(dir, FILE_NAME);

                    if (!apk.exists() || apk.length() < 8000000L) {
                        InputStream in = activity.getAssets().open(ASSET_PATH);
                        FileOutputStream out = new FileOutputStream(apk);
                        byte[] buffer = new byte[32768];
                        int n;
                        while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                        out.flush();
                        out.close();
                        in.close();
                    }

                    final File readyApk = apk;
                    activity.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            NajmContainer.installAndLaunch(activity, readyApk, PACKAGE_NAME, 19);
                        }
                    });
                } catch (final Throwable e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            new AlertDialog.Builder(activity)
                                    .setTitle("YouTube 4.4")
                                    .setMessage("تعذر تجهيز مشغل YouTube داخل Najm Space: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()))
                                    .setPositiveButton("موافق", null)
                                    .show();
                        }
                    });
                }
            }
        }, "NajmYouTube44Prepare").start();
    }
}
