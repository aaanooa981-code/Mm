package com.najmspace.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Preloads the exact RV Manager Plus 3.6.17 build into Najm Space and,
 * on Android 8+ where that app can run, installs it into DroidPlugin.
 */
public class BundledRvManagerProvider extends ContentProvider {
    private static final String PKG = "com.revanced.net.revancedmanager";
    private static final String ASSET = "preload/rv-manager-plus-3.6.17.apk";
    private static final String FILE = "vanced.to_revanced_manager_plus_v3.6.17.apk";

    @Override public boolean onCreate() {
        final Context context = getContext();
        if (context == null) return true;

        final File apk = new File(NajmStorage.appsDir(context), FILE);
        try {
            if (!apk.exists() || apk.length() < 1000000L) copyAsset(context, apk);
        } catch (Throwable ignored) {}

        // RV Manager Plus 3.6.17 itself requires API 26. Keep Najm Space API19-compatible,
        // but only auto-install this bundled store when the host Android can actually run it.
        if (Build.VERSION.SDK_INT >= 26 && apk.exists()) {
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        Thread.sleep(2500L);
                        PluginManager pm = PluginManager.getInstance();
                        pm.waitForConnected(10000);
                        if (!pm.isConnected()) return;

                        PackageInfo existing = null;
                        try { existing = pm.getPackageInfo(PKG, 0); } catch (Throwable ignored) {}
                        if (existing == null) {
                            pm.installPackage(apk.getAbsolutePath(), PackageManagerCompat.INSTALL_REPLACE_EXISTING);
                        }
                    } catch (Throwable ignored) {}
                }
            }, "NajmRvManagerPlusPreload").start();
        }
        return true;
    }

    private static void copyAsset(Context context, File out) throws Exception {
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        InputStream in = context.getAssets().open(ASSET);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[32768];
        int n;
        while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
        fos.flush();
        fos.close();
        in.close();
    }

    @Override public String getType(Uri uri) { return null; }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
